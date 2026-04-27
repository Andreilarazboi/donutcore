package ro.andreilarazboi.donutcore.crates.gui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import ro.andreilarazboi.donutcore.crates.CratesModule;
import ro.andreilarazboi.donutcore.crates.model.Crate;
import ro.andreilarazboi.donutcore.crates.model.CrateKey;
import ro.andreilarazboi.donutcore.crates.util.ColorUtil;
import ro.andreilarazboi.donutcore.crates.util.ItemBuilder;

import java.util.ArrayList;
import java.util.Arrays;

public class KeyEditorGUI extends AbstractGUI {

    private final CratesModule module;
    private final Crate crate;

    private static final int SLOT_KEY_PREVIEW    = 4;
    private static final int SLOT_MATERIAL       = 10;
    private static final int SLOT_DISPLAY_NAME   = 12;
    private static final int SLOT_LORE           = 14;
    private static final int SLOT_CUSTOM_MODEL   = 16;
    private static final int SLOT_BACK           = 49;

    public KeyEditorGUI(CratesModule module, Crate crate) {
        this.module = module;
        this.crate = crate;
    }

    @Override
    public void open(Player player) {
        Component title = ColorUtil.parse("&8Key Editor: &r" + crate.getDisplayName())
                .decoration(TextDecoration.ITALIC, false);
        inventory = Bukkit.createInventory(this, 54, title);

        ItemStack border = ItemBuilder.filler(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inventory.setItem(i, border);

        CrateKey key = crate.getPhysicalKey();

        inventory.setItem(SLOT_KEY_PREVIEW, module.getCrateManager().createKeyItem(crate));

        inventory.setItem(SLOT_MATERIAL, new ItemBuilder(Material.GRASS_BLOCK)
                .name("&eKey Material")
                .lore("&7Current: &f" + key.getMaterial().name(),
                      "",
                      "&eLeft-click &7to use held item material")
                .build());

        inventory.setItem(SLOT_DISPLAY_NAME, new ItemBuilder(Material.NAME_TAG)
                .name("&eDisplay Name")
                .lore("&7Current: &f" + ColorUtil.strip(key.getDisplayName()),
                      "",
                      "&eClick &7to change")
                .build());

        java.util.List<String> loreInfo = new ArrayList<>();
        loreInfo.add("&7Current lore:");
        if (key.getLore() != null && !key.getLore().isEmpty()) {
            key.getLore().forEach(l -> loreInfo.add("  &f" + ColorUtil.strip(l)));
        } else {
            loreInfo.add("  &8None");
        }
        loreInfo.add("");
        loreInfo.add("&eLeft-click &7to set (lines separated by |)");
        loreInfo.add("&cRight-click &7to clear");
        inventory.setItem(SLOT_LORE, new ItemBuilder(Material.WRITABLE_BOOK)
                .name("&eLore")
                .lore(loreInfo)
                .build());

        inventory.setItem(SLOT_CUSTOM_MODEL, new ItemBuilder(Material.COMPARATOR)
                .name("&eCustom Model Data")
                .lore("&7Current: &f" + key.getCustomModelData(),
                      "",
                      "&eClick &7to change")
                .build());

        inventory.setItem(SLOT_BACK, new ItemBuilder(Material.ARROW).name("&7Back").build());

        player.openInventory(inventory);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getSlot();
        CrateKey key = crate.getPhysicalKey();

        switch (slot) {
            case SLOT_MATERIAL -> {
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand != null && hand.getType() != Material.AIR) {
                    key.setMaterial(hand.getType());
                    module.getCrateManager().saveCrate(crate);
                    new KeyEditorGUI(module, crate).open(player);
                }
            }
            case SLOT_DISPLAY_NAME -> {
                player.closeInventory();
                module.startChatInput(player, "Enter new key display name (supports &colors):", input -> {
                    key.setDisplayName(input);
                    module.getCrateManager().saveCrate(crate);
                    new KeyEditorGUI(module, crate).open(player);
                });
            }
            case SLOT_LORE -> {
                if (!event.isRightClick()) {
                    player.closeInventory();
                    module.startChatInput(player, "Enter lore lines separated by | (supports &colors):", input -> {
                        String[] lines = input.split("\\|");
                        java.util.List<String> lore = new ArrayList<>();
                        for (String line : lines) lore.add(line.trim());
                        key.setLore(lore);
                        module.getCrateManager().saveCrate(crate);
                        new KeyEditorGUI(module, crate).open(player);
                    });
                } else {
                    key.setLore(new ArrayList<>());
                    module.getCrateManager().saveCrate(crate);
                    new KeyEditorGUI(module, crate).open(player);
                }
            }
            case SLOT_CUSTOM_MODEL -> {
                player.closeInventory();
                module.startChatInput(player, "Enter custom model data (0 for none):", input -> {
                    try {
                        key.setCustomModelData(Integer.parseInt(input));
                        module.getCrateManager().saveCrate(crate);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ColorUtil.colorize("&cInvalid number!"));
                    }
                    new KeyEditorGUI(module, crate).open(player);
                });
            }
            case SLOT_BACK -> new CrateEditorGUI(module, crate).open(player);
        }
    }
}
