package ro.andreilarazboi.donutcore.crates.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ro.andreilarazboi.donutcore.crates.CratesModule;
import ro.andreilarazboi.donutcore.crates.model.Crate;
import ro.andreilarazboi.donutcore.crates.model.CrateItem;
import ro.andreilarazboi.donutcore.crates.model.CrateType;
import ro.andreilarazboi.donutcore.crates.util.ColorUtil;
import ro.andreilarazboi.donutcore.crates.util.ItemBuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ItemEditorGUI extends AbstractGUI {

    private final CratesModule module;
    private final Crate crate;
    private final CrateItem item;

    private static final int SLOT_ITEM_PREVIEW   = 4;
    private static final int SLOT_MATERIAL       = 10;
    private static final int SLOT_DISPLAY_NAME   = 12;
    private static final int SLOT_LORE           = 14;
    private static final int SLOT_SLOT_NUMBER    = 16;
    private static final int SLOT_COMMAND        = 19;
    private static final int SLOT_ENCHANTMENTS   = 21;
    private static final int SLOT_CHANCE         = 23;
    private static final int SLOT_BROADCAST      = 25;
    private static final int SLOT_BACK           = 49;

    public ItemEditorGUI(CratesModule module, Crate crate, CrateItem item) {
        this.module = module;
        this.crate = crate;
        this.item = item;
    }

    @Override
    public void open(Player player) {
        Component title = ColorUtil.parse("&8Edit Item: &r" + ColorUtil.strip(item.getDisplayName()))
                .decoration(TextDecoration.ITALIC, false);
        inventory = Bukkit.createInventory(this, 54, title);

        ItemStack border = ItemBuilder.filler(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inventory.setItem(i, border);

        ItemBuilder preview = new ItemBuilder(item.getMaterial()).name(item.getDisplayName());
        if (item.getLore() != null && !item.getLore().isEmpty()) preview.lore(item.getLore());
        inventory.setItem(SLOT_ITEM_PREVIEW, preview.build());

        inventory.setItem(SLOT_MATERIAL, new ItemBuilder(Material.GRASS_BLOCK)
                .name("&eMaterial")
                .lore("&7Current: &f" + item.getMaterial().name(),
                      "",
                      "&eLeft-click &7to use held item material",
                      "&eRight-click &7to type a material name")
                .build());

        inventory.setItem(SLOT_DISPLAY_NAME, new ItemBuilder(Material.NAME_TAG)
                .name("&eDisplay Name")
                .lore("&7Current: &f" + ColorUtil.strip(item.getDisplayName()),
                      "",
                      "&eClick &7to change")
                .build());

        List<String> lorePreview = item.getLore() != null ? new ArrayList<>(item.getLore()) : new ArrayList<>();
        lorePreview.add(0, "&7Current lore:");
        lorePreview.add("");
        lorePreview.add("&eLeft-click &7to set (type lines separated by |)");
        lorePreview.add("&cRight-click &7to clear lore");
        inventory.setItem(SLOT_LORE, new ItemBuilder(Material.WRITABLE_BOOK)
                .name("&eLore")
                .lore(lorePreview)
                .build());

        inventory.setItem(SLOT_SLOT_NUMBER, new ItemBuilder(Material.ITEM_FRAME)
                .name("&eSlot Number")
                .lore("&7Current: &f" + item.getSlot(),
                      "",
                      "&aLeft-click &7to increase",
                      "&cRight-click &7to decrease")
                .build());

        inventory.setItem(SLOT_COMMAND, new ItemBuilder(Material.COMMAND_BLOCK)
                .name("&eCommand")
                .lore("&7Current: &f" + (item.getCommand() == null || item.getCommand().isEmpty() ? "None" : item.getCommand()),
                      "&7Use {player} as placeholder",
                      "",
                      "&eClick &7to change")
                .build());

        List<String> enchantLore = new ArrayList<>();
        enchantLore.add("&7Current enchantments:");
        if (item.getEnchantments().isEmpty()) {
            enchantLore.add("  &8None");
        } else {
            item.getEnchantments().forEach((e, lvl) ->
                    enchantLore.add("  &7" + e.getKey().getKey().toUpperCase() + " " + lvl));
        }
        enchantLore.add("");
        enchantLore.add("&eLeft-click &7to add (hold item to copy)");
        enchantLore.add("&cRight-click &7to clear enchants");
        inventory.setItem(SLOT_ENCHANTMENTS, new ItemBuilder(Material.ENCHANTING_TABLE)
                .name("&eEnchantments")
                .lore(enchantLore)
                .build());

        if (crate.getType() == CrateType.RANDOM) {
            inventory.setItem(SLOT_CHANCE, new ItemBuilder(Material.GOLD_NUGGET)
                    .name("&eChance")
                    .lore("&7Current: &f" + item.getChance() + "%",
                          "",
                          "&aLeft-click &7+5%",
                          "&cRight-click &7-5%",
                          "&eShift+Left &7+1%",
                          "&eShift+Right &7-1%")
                    .build());
        }

        inventory.setItem(SLOT_BROADCAST, new ItemBuilder(
                item.isBroadcastOnWin() ? Material.BELL : Material.BELL)
                .name("&eBroadcast on Win")
                .lore("&7Enabled: &f" + item.isBroadcastOnWin(),
                      "&7Message: &f" + (item.getBroadcastMessage() != null ? item.getBroadcastMessage() : "None"),
                      "",
                      "&eLeft-click &7to toggle",
                      "&eRight-click &7to set message")
                .build());

        inventory.setItem(SLOT_BACK, new ItemBuilder(Material.ARROW)
                .name("&7Back")
                .build());

        player.openInventory(inventory);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getSlot();
        boolean isRight = event.isRightClick();
        boolean isShift = event.isShiftClick();

        switch (slot) {
            case SLOT_MATERIAL -> {
                if (!isRight) {
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    if (hand != null && hand.getType() != Material.AIR) {
                        item.setMaterial(hand.getType());
                        module.getCrateManager().saveCrate(crate);
                        new ItemEditorGUI(module, crate, item).open(player);
                    }
                } else {
                    player.closeInventory();
                    module.startChatInput(player, "Type the material name (e.g. DIAMOND_SWORD):", input -> {
                        Material mat = Material.getMaterial(input.toUpperCase());
                        if (mat != null) {
                            item.setMaterial(mat);
                            module.getCrateManager().saveCrate(crate);
                        } else {
                            player.sendMessage(ColorUtil.colorize("&cInvalid material: " + input));
                        }
                        new ItemEditorGUI(module, crate, item).open(player);
                    });
                }
            }
            case SLOT_DISPLAY_NAME -> {
                player.closeInventory();
                module.startChatInput(player, "Enter new display name (supports &colors):", input -> {
                    item.setDisplayName(input);
                    module.getCrateManager().saveCrate(crate);
                    new ItemEditorGUI(module, crate, item).open(player);
                });
            }
            case SLOT_LORE -> {
                if (!isRight) {
                    player.closeInventory();
                    module.startChatInput(player, "Enter lore lines separated by | (supports &colors):", input -> {
                        String[] lines = input.split("\\|");
                        List<String> lore = new ArrayList<>();
                        for (String line : lines) lore.add(line.trim());
                        item.setLore(lore);
                        module.getCrateManager().saveCrate(crate);
                        new ItemEditorGUI(module, crate, item).open(player);
                    });
                } else {
                    item.setLore(new ArrayList<>());
                    module.getCrateManager().saveCrate(crate);
                    new ItemEditorGUI(module, crate, item).open(player);
                }
            }
            case SLOT_SLOT_NUMBER -> {
                int maxSlot = crate.getSize() - 1;
                if (!isRight) {
                    if (item.getSlot() < maxSlot) item.setSlot(item.getSlot() + 1);
                } else {
                    if (item.getSlot() > 0) item.setSlot(item.getSlot() - 1);
                }
                module.getCrateManager().saveCrate(crate);
                new ItemEditorGUI(module, crate, item).open(player);
            }
            case SLOT_COMMAND -> {
                player.closeInventory();
                module.startChatInput(player, "Enter command (use {player} placeholder, no leading /):", input -> {
                    item.setCommand(input);
                    module.getCrateManager().saveCrate(crate);
                    new ItemEditorGUI(module, crate, item).open(player);
                });
            }
            case SLOT_ENCHANTMENTS -> {
                if (!isRight) {
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    if (hand != null && hand.getType() != Material.AIR && hand.hasItemMeta()) {
                        Map<Enchantment, Integer> enchants = new LinkedHashMap<>(hand.getEnchantments());
                        if (hand.getItemMeta() != null) enchants.putAll(hand.getItemMeta().getEnchants());
                        item.setEnchantments(enchants);
                        module.getCrateManager().saveCrate(crate);
                        new ItemEditorGUI(module, crate, item).open(player);
                    }
                } else {
                    item.setEnchantments(new LinkedHashMap<>());
                    module.getCrateManager().saveCrate(crate);
                    new ItemEditorGUI(module, crate, item).open(player);
                }
            }
            case SLOT_CHANCE -> {
                if (crate.getType() == CrateType.RANDOM) {
                    double delta = isShift ? 1.0 : 5.0;
                    if (!isRight) {
                        item.setChance(Math.min(100.0, item.getChance() + delta));
                    } else {
                        item.setChance(Math.max(0.0, item.getChance() - delta));
                    }
                    module.getCrateManager().saveCrate(crate);
                    new ItemEditorGUI(module, crate, item).open(player);
                }
            }
            case SLOT_BROADCAST -> {
                if (!isRight) {
                    item.setBroadcastOnWin(!item.isBroadcastOnWin());
                    module.getCrateManager().saveCrate(crate);
                    new ItemEditorGUI(module, crate, item).open(player);
                } else {
                    player.closeInventory();
                    module.startChatInput(player, "Enter broadcast message (supports &colors, {player} placeholder):", input -> {
                        item.setBroadcastMessage(input);
                        module.getCrateManager().saveCrate(crate);
                        new ItemEditorGUI(module, crate, item).open(player);
                    });
                }
            }
            case SLOT_BACK -> new CrateEditorGUI(module, crate).open(player);
        }
    }
}
