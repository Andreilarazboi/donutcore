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
import ro.andreilarazboi.donutcore.crates.model.CrateHologramSettings;
import ro.andreilarazboi.donutcore.crates.util.ColorUtil;
import ro.andreilarazboi.donutcore.crates.util.ItemBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HologramEditorGUI extends AbstractGUI {

    private final CratesModule module;
    private final Crate crate;

    private static final int SLOT_TOGGLE         = 10;
    private static final int SLOT_LINES          = 12;
    private static final int SLOT_BG_COLOR       = 14;
    private static final int SLOT_TEXT_SHADOW    = 16;
    private static final int SLOT_UPDATE_TIMER   = 19;
    private static final int SLOT_TEMPLATE       = 21;
    private static final int SLOT_RESPAWN        = 23;
    private static final int SLOT_BACK           = 49;

    public HologramEditorGUI(CratesModule module, Crate crate) {
        this.module = module;
        this.crate = crate;
    }

    @Override
    public void open(Player player) {
        Component title = ColorUtil.parse("&8Hologram: &r" + crate.getDisplayName())
                .decoration(TextDecoration.ITALIC, false);
        inventory = Bukkit.createInventory(this, 54, title);

        ItemStack border = ItemBuilder.filler(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inventory.setItem(i, border);

        CrateHologramSettings h = crate.getHologramSettings();

        inventory.setItem(SLOT_TOGGLE, new ItemBuilder(h.isEnabled() ? Material.LIME_DYE : Material.GRAY_DYE)
                .name("&eToggle Hologram")
                .lore("&7Enabled: &f" + h.isEnabled(),
                      "",
                      "&eClick &7to toggle")
                .build());

        List<String> linesInfo = new ArrayList<>();
        linesInfo.add("&7Current lines:");
        if (h.getLines().isEmpty()) {
            linesInfo.add("  &8None");
        } else {
            for (String line : h.getLines()) linesInfo.add("  &f" + line);
        }
        linesInfo.add("");
        linesInfo.add("&eLeft-click &7to set (lines separated by |)");
        linesInfo.add("&7Supports &colors and %donutcrate_key_" + crate.getName() + "%");
        inventory.setItem(SLOT_LINES, new ItemBuilder(Material.BOOK)
                .name("&eHologram Lines")
                .lore(linesInfo)
                .build());

        inventory.setItem(SLOT_BG_COLOR, new ItemBuilder(Material.PURPLE_DYE)
                .name("&eBackground Color")
                .lore("&7R: &f" + h.getBackgroundColorR(),
                      "&7G: &f" + h.getBackgroundColorG(),
                      "&7B: &f" + h.getBackgroundColorB(),
                      "&7Alpha: &f" + h.getBackgroundAlpha() + " &8(0 = transparent)",
                      "",
                      "&eClick &7to change (format: R,G,B,Alpha)")
                .build());

        inventory.setItem(SLOT_TEXT_SHADOW, new ItemBuilder(
                h.isTextShadow() ? Material.LIME_DYE : Material.GRAY_DYE)
                .name("&eText Shadow")
                .lore("&7Enabled: &f" + h.isTextShadow(),
                      "",
                      "&eClick &7to toggle")
                .build());

        inventory.setItem(SLOT_UPDATE_TIMER, new ItemBuilder(Material.CLOCK)
                .name("&eUpdate Timer")
                .lore("&7Interval: &f" + h.getUpdateInterval() + " ticks",
                      "&8(" + (h.getUpdateInterval() / 20.0) + "s)",
                      "",
                      "&aLeft-click &7+20 ticks",
                      "&cRight-click &7-20 ticks")
                .build());

        inventory.setItem(SLOT_TEMPLATE, new ItemBuilder(Material.PAPER)
                .name("&eTemplate")
                .lore("&7Current: &f" + (h.getTemplateName().isEmpty() ? "None" : h.getTemplateName()),
                      "",
                      "&eLeft-click &7to set template name",
                      "&cRight-click &7to clear template")
                .build());

        inventory.setItem(SLOT_RESPAWN, new ItemBuilder(Material.RESPAWN_ANCHOR)
                .name("&eRespawn Hologram")
                .lore("&7Force respawn hologram now")
                .build());

        inventory.setItem(SLOT_BACK, new ItemBuilder(Material.ARROW).name("&7Back").build());

        player.openInventory(inventory);
    }

    @Override
    public void handleClick(InventoryClickEvent event) {
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        int slot = event.getSlot();
        CrateHologramSettings h = crate.getHologramSettings();

        switch (slot) {
            case SLOT_TOGGLE -> {
                h.setEnabled(!h.isEnabled());
                module.getCrateManager().saveCrate(crate);
                if (h.isEnabled()) module.getHologramManager().spawnHologram(crate);
                else module.getHologramManager().removeHologram(crate.getName());
                new HologramEditorGUI(module, crate).open(player);
            }
            case SLOT_LINES -> {
                player.closeInventory();
                module.startChatInput(player, "Enter hologram lines separated by | (supports &colors):", input -> {
                    String[] parts = input.split("\\|");
                    List<String> lines = new ArrayList<>();
                    for (String part : parts) lines.add(part.trim());
                    h.setLines(lines);
                    module.getCrateManager().saveCrate(crate);
                    module.getHologramManager().respawnHologram(crate);
                    new HologramEditorGUI(module, crate).open(player);
                });
            }
            case SLOT_BG_COLOR -> {
                player.closeInventory();
                module.startChatInput(player, "Enter background color as R,G,B,Alpha (e.g. 0,0,0,200):", input -> {
                    String[] parts = input.split(",");
                    if (parts.length == 4) {
                        try {
                            h.setBackgroundColorR(Integer.parseInt(parts[0].trim()));
                            h.setBackgroundColorG(Integer.parseInt(parts[1].trim()));
                            h.setBackgroundColorB(Integer.parseInt(parts[2].trim()));
                            h.setBackgroundAlpha(Integer.parseInt(parts[3].trim()));
                            module.getCrateManager().saveCrate(crate);
                            module.getHologramManager().respawnHologram(crate);
                        } catch (NumberFormatException e) {
                            player.sendMessage(ColorUtil.colorize("&cInvalid color format!"));
                        }
                    }
                    new HologramEditorGUI(module, crate).open(player);
                });
            }
            case SLOT_TEXT_SHADOW -> {
                h.setTextShadow(!h.isTextShadow());
                module.getCrateManager().saveCrate(crate);
                module.getHologramManager().respawnHologram(crate);
                new HologramEditorGUI(module, crate).open(player);
            }
            case SLOT_UPDATE_TIMER -> {
                if (!event.isRightClick()) {
                    h.setUpdateInterval(h.getUpdateInterval() + 20);
                } else {
                    h.setUpdateInterval(Math.max(1, h.getUpdateInterval() - 20));
                }
                module.getCrateManager().saveCrate(crate);
                new HologramEditorGUI(module, crate).open(player);
            }
            case SLOT_TEMPLATE -> {
                if (!event.isRightClick()) {
                    player.closeInventory();
                    module.startChatInput(player, "Enter template name (must be registered):", input -> {
                        h.setTemplateName(input.trim());
                        module.getCrateManager().saveCrate(crate);
                        module.getHologramManager().respawnHologram(crate);
                        new HologramEditorGUI(module, crate).open(player);
                    });
                } else {
                    h.setTemplateName("");
                    module.getCrateManager().saveCrate(crate);
                    new HologramEditorGUI(module, crate).open(player);
                }
            }
            case SLOT_RESPAWN -> {
                module.getHologramManager().respawnHologram(crate);
                player.sendMessage(ColorUtil.colorize("&aHologram respawned!"));
            }
            case SLOT_BACK -> new CrateEditorGUI(module, crate).open(player);
        }
    }
}
