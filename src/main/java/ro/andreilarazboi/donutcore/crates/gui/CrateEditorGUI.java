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
import ro.andreilarazboi.donutcore.crates.model.CrateItem;
import ro.andreilarazboi.donutcore.crates.model.CrateType;
import ro.andreilarazboi.donutcore.crates.util.ColorUtil;
import ro.andreilarazboi.donutcore.crates.util.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

public class CrateEditorGUI extends AbstractGUI {

    private final CratesModule module;
    private final Crate crate;
    private final List<CrateItem> itemList;

    private static final int SLOT_DISPLAY_NAME   = 0;
    private static final int SLOT_TYPE           = 2;
    private static final int SLOT_ROWS           = 4;
    private static final int SLOT_FILLER         = 6;
    private static final int SLOT_KEEP_OPEN      = 8;
    private static final int SLOT_PREVIEW        = 45;
    private static final int SLOT_HOLOGRAM       = 46;
    private static final int SLOT_KEY_EDITOR     = 47;
    private static final int SLOT_SETTINGS       = 48;
    private static final int SLOT_COPY           = 50;
    private static final int SLOT_DELETE         = 51;
    private static final int SLOT_BACK           = 53;

    public CrateEditorGUI(CratesModule module, Crate crate) {
        this.module = module;
        this.crate = crate;
        this.itemList = new ArrayList<>(crate.getItems().values());
    }

    @Override
    public void open(Player player) {
        Component title = ColorUtil.parse("&8Edit: &r" + crate.getDisplayName())
                .decoration(TextDecoration.ITALIC, false);
        inventory = Bukkit.createInventory(this, 54, title);

        ItemStack border = ItemBuilder.filler(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inventory.setItem(i, border);

        inventory.setItem(SLOT_DISPLAY_NAME, new ItemBuilder(Material.NAME_TAG)
                .name("&eDisplay Name")
                .lore("&7Current: &f" + ColorUtil.strip(crate.getDisplayName()),
                      "",
                      "&eClick &7to change name")
                .build());

        inventory.setItem(SLOT_TYPE, new ItemBuilder(Material.COMPASS)
                .name("&eCrate Type")
                .lore("&7Current: &f" + crate.getType().name(),
                      "",
                      "&eClick &7to toggle type",
                      "&7CHOOSE &8= DonutSMP style",
                      "&7RANDOM &8= Chance based")
                .build());

        inventory.setItem(SLOT_ROWS, new ItemBuilder(Material.BOOKSHELF)
                .name("&eRows")
                .lore("&7Current: &f" + crate.getRows(),
                      "",
                      "&aLeft-click &7to increase",
                      "&cRight-click &7to decrease")
                .build());

        inventory.setItem(SLOT_FILLER, new ItemBuilder(crate.getFillerMaterial())
                .name("&eFiller")
                .lore("&7Enabled: &f" + crate.isFillerEnabled(),
                      "&7Material: &f" + crate.getFillerMaterial().name(),
                      "",
                      "&eLeft-click &7to toggle",
                      "&eRight-click &7to change material (hold item)")
                .build());

        inventory.setItem(SLOT_KEEP_OPEN, new ItemBuilder(
                crate.isKeepOpenAfterClaim() ? Material.LIME_DYE : Material.GRAY_DYE)
                .name("&eKeep Open After Claim")
                .lore("&7Enabled: &f" + crate.isKeepOpenAfterClaim(),
                      "",
                      "&eClick &7to toggle")
                .build());

        int itemAreaStart = 9;
        for (int i = 0; i < itemList.size() && i < 36; i++) {
            CrateItem crateItem = itemList.get(i);
            ItemStack icon = new ItemBuilder(crateItem.getMaterial())
                    .name(crateItem.getDisplayName())
                    .lore("&7Slot: &e" + crateItem.getSlot(),
                          "&7Chance: &e" + (crate.getType() == CrateType.RANDOM ? crateItem.getChance() + "%" : "N/A"),
                          "",
                          "&eLeft-click &7to edit",
                          "&cRight-click &7to delete")
                    .build();
            inventory.setItem(itemAreaStart + i, icon);
        }

        inventory.setItem(SLOT_PREVIEW, new ItemBuilder(Material.SPYGLASS)
                .name("&ePreview Crate")
                .lore("&7See how the crate looks")
                .build());

        inventory.setItem(SLOT_HOLOGRAM, new ItemBuilder(Material.END_CRYSTAL)
                .name("&eHologram Settings")
                .lore("&7Edit lines, colors, and more")
                .build());

        inventory.setItem(SLOT_KEY_EDITOR, new ItemBuilder(Material.TRIPWIRE_HOOK)
                .name("&eKey Editor")
                .lore("&7Edit the physical key item")
                .build());

        inventory.setItem(SLOT_COPY, new ItemBuilder(Material.PAPER)
                .name("&eCopy Crate")
                .lore("&7Create a copy of this crate")
                .build());

        inventory.setItem(SLOT_DELETE, new ItemBuilder(Material.BARRIER)
                .name("&c&lDelete Crate")
                .lore("&7Permanently delete this crate",
                      "&cThis action cannot be undone!")
                .build());

        inventory.setItem(SLOT_BACK, new ItemBuilder(Material.ARROW)
                .name("&7Back")
                .lore("&7Return to crate list")
                .build());

        player.openInventory(inventory);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getSlot();
        boolean isRightClick = event.isRightClick();

        switch (slot) {
            case SLOT_DISPLAY_NAME -> {
                player.closeInventory();
                module.startChatInput(player, "Enter new display name (supports &colors):", input -> {
                    crate.setDisplayName(input);
                    module.getCrateManager().saveCrate(crate);
                    module.getHologramManager().respawnHologram(crate);
                    new CrateEditorGUI(module, crate).open(player);
                });
            }
            case SLOT_TYPE -> {
                crate.setType(crate.getType() == CrateType.CHOOSE ? CrateType.RANDOM : CrateType.CHOOSE);
                module.getCrateManager().saveCrate(crate);
                new CrateEditorGUI(module, crate).open(player);
            }
            case SLOT_ROWS -> {
                if (!isRightClick) {
                    if (crate.getRows() < 6) crate.setRows(crate.getRows() + 1);
                } else {
                    if (crate.getRows() > 1) crate.setRows(crate.getRows() - 1);
                }
                module.getCrateManager().saveCrate(crate);
                new CrateEditorGUI(module, crate).open(player);
            }
            case SLOT_FILLER -> {
                if (!isRightClick) {
                    crate.setFillerEnabled(!crate.isFillerEnabled());
                    module.getCrateManager().saveCrate(crate);
                    new CrateEditorGUI(module, crate).open(player);
                } else {
                    ItemStack hand = player.getInventory().getItemInMainHand();
                    if (hand != null && hand.getType() != org.bukkit.Material.AIR) {
                        crate.setFillerMaterial(hand.getType());
                        module.getCrateManager().saveCrate(crate);
                        new CrateEditorGUI(module, crate).open(player);
                    }
                }
            }
            case SLOT_KEEP_OPEN -> {
                crate.setKeepOpenAfterClaim(!crate.isKeepOpenAfterClaim());
                module.getCrateManager().saveCrate(crate);
                new CrateEditorGUI(module, crate).open(player);
            }
            case SLOT_PREVIEW -> new CratePreviewGUI(module, crate).open(player);
            case SLOT_HOLOGRAM -> new HologramEditorGUI(module, crate).open(player);
            case SLOT_KEY_EDITOR -> new KeyEditorGUI(module, crate).open(player);
            case SLOT_COPY -> {
                player.closeInventory();
                module.startChatInput(player, "Enter name for the copied crate:", input -> {
                    if (module.getCrateManager().exists(input)) {
                        player.sendMessage(ColorUtil.colorize("&cA crate with that name already exists!"));
                    } else {
                        module.getCrateManager().copyCrate(crate.getName(), input);
                        player.sendMessage(ColorUtil.colorize("&aCrate copied as &e" + input + "&a!"));
                    }
                    new CrateListGUI(module).open(player);
                });
            }
            case SLOT_DELETE -> new DeleteConfirmGUI(module, crate).open(player);
            case SLOT_BACK -> new CrateListGUI(module).open(player);
            default -> {
                int itemAreaStart = 9;
                int index = slot - itemAreaStart;
                if (index >= 0 && index < itemList.size()) {
                    CrateItem crateItem = itemList.get(index);
                    if (!isRightClick) {
                        new ItemEditorGUI(module, crate, crateItem).open(player);
                    } else {
                        new ItemDeleteConfirmGUI(module, crate, crateItem).open(player);
                    }
                }
            }
        }
    }
}
