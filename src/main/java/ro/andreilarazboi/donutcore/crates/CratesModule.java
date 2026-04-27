package ro.andreilarazboi.donutcore.crates;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ro.andreilarazboi.donutcore.crates.animation.CrateAnimation;
import ro.andreilarazboi.donutcore.crates.command.CrateCommand;
import ro.andreilarazboi.donutcore.crates.database.CrateDatabase;
import ro.andreilarazboi.donutcore.crates.gui.CrateChooseGUI;
import ro.andreilarazboi.donutcore.crates.listener.ChatInputListener;
import ro.andreilarazboi.donutcore.crates.listener.CrateBlockListener;
import ro.andreilarazboi.donutcore.crates.listener.CrateInventoryListener;
import ro.andreilarazboi.donutcore.crates.manager.CrateManager;
import ro.andreilarazboi.donutcore.crates.manager.HologramManager;
import ro.andreilarazboi.donutcore.crates.manager.KeyManager;
import ro.andreilarazboi.donutcore.crates.model.Crate;
import ro.andreilarazboi.donutcore.crates.model.CrateItem;
import ro.andreilarazboi.donutcore.crates.model.CrateType;
import ro.andreilarazboi.donutcore.crates.placeholder.CratePlaceholderExpansion;
import ro.andreilarazboi.donutcore.crates.util.ColorUtil;
import ro.andreilarazboi.donutcore.crates.util.MessageUtil;

import java.util.*;
import java.util.function.Consumer;

public class CratesModule {

    private final JavaPlugin plugin;
    private CrateDatabase database;
    private CrateManager crateManager;
    private KeyManager keyManager;
    private HologramManager hologramManager;

    private final Map<UUID, Consumer<String>> chatInputCallbacks = new HashMap<>();
    private final Map<UUID, String> chatInputPrompts = new HashMap<>();
    private final Map<UUID, String> moveBlockTargets = new HashMap<>();

    public CratesModule(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        plugin.saveDefaultConfig();

        database = new CrateDatabase(plugin);
        database.connect();

        crateManager = new CrateManager(plugin);
        crateManager.loadAll();

        keyManager = new KeyManager(database);
        hologramManager = new HologramManager(plugin, keyManager);

        registerListeners();
        registerCommands();
        registerPlaceholders();
        spawnHolograms();

        plugin.getLogger().info("[DonutCore] Crates module enabled.");
    }

    public void disable() {
        hologramManager.removeAll();
        if (database != null) database.disconnect();
        plugin.getLogger().info("[DonutCore] Crates module disabled.");
    }

    public void reload() {
        plugin.reloadConfig();
        hologramManager.removeAll();
        crateManager.loadAll();
        spawnHolograms();
    }

    private void registerListeners() {
        var pm = plugin.getServer().getPluginManager();
        pm.registerEvents(new CrateBlockListener(this), plugin);
        pm.registerEvents(new CrateInventoryListener(), plugin);
        pm.registerEvents(new ChatInputListener(this), plugin);
    }

    private void registerCommands() {
        CrateCommand executor = new CrateCommand(this);
        Objects.requireNonNull(plugin.getCommand("donutcrate")).setExecutor(executor);
        Objects.requireNonNull(plugin.getCommand("donutcrate")).setTabCompleter(executor);
    }

    private void registerPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new CratePlaceholderExpansion(this).register();
            plugin.getLogger().info("[DonutCore] PlaceholderAPI hooked.");
        }
    }

    private void spawnHolograms() {
        for (Crate crate : crateManager.getAllCrates()) {
            hologramManager.spawnHologram(crate);
        }
    }

    public void openCrate(Player player, Crate crate) {
        if (crate.getType() == CrateType.CHOOSE) {
            new CrateChooseGUI(this, crate).open(player);
        } else {
            CrateItem winner = rollRandom(crate);
            if (winner == null) {
                player.sendMessage(ColorUtil.colorize("&cThis crate has no items configured!"));
                return;
            }
            new CrateAnimation(this, player, crate, winner).start();
        }
    }

    private CrateItem rollRandom(Crate crate) {
        List<CrateItem> items = new ArrayList<>(crate.getItems().values());
        if (items.isEmpty()) return null;

        double totalWeight = items.stream().mapToDouble(CrateItem::getChance).sum();
        double roll = Math.random() * totalWeight;
        double cumulative = 0;
        for (CrateItem item : items) {
            cumulative += item.getChance();
            if (roll < cumulative) return item;
        }
        return items.get(items.size() - 1);
    }

    public void giveReward(Player player, Crate crate, CrateItem item) {
        keyManager.removeKeys(player, crate, 1);

        if (item.getCommand() != null && !item.getCommand().isBlank()) {
            String cmd = item.getCommand()
                    .replace("{player}", player.getName())
                    .replace("%player%", player.getName());
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        }

        database.recordOpen(player.getUniqueId(), crate.getName(),
                ColorUtil.strip(item.getDisplayName()));

        String rewardMsg = ColorUtil.colorize("&aYou received &e" +
                ColorUtil.strip(item.getDisplayName()) + " &afrom the &e" +
                ColorUtil.strip(crate.getDisplayName()) + "&a!");
        player.sendMessage(rewardMsg);

        if (item.isBroadcastOnWin() && item.getBroadcastMessage() != null && !item.getBroadcastMessage().isEmpty()) {
            String broadcast = item.getBroadcastMessage()
                    .replace("{player}", player.getName())
                    .replace("{reward}", ColorUtil.strip(item.getDisplayName()))
                    .replace("{crate}", ColorUtil.strip(crate.getDisplayName()));
            MessageUtil.broadcast(broadcast);
        }

        if (crate.isKeepOpenAfterClaim()) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> openCrate(player, crate), 1L);
        }
    }

    public void startChatInput(Player player, String prompt, Consumer<String> callback) {
        chatInputCallbacks.put(player.getUniqueId(), callback);
        chatInputPrompts.put(player.getUniqueId(), prompt);
        player.sendMessage(ColorUtil.colorize("&e" + prompt + " &8(type &ccancel &8to abort)"));
    }

    public boolean hasChatInput(Player player) {
        return chatInputCallbacks.containsKey(player.getUniqueId());
    }

    public Consumer<String> getChatInputCallback(Player player) {
        return chatInputCallbacks.get(player.getUniqueId());
    }

    public void removeChatInput(Player player) {
        chatInputCallbacks.remove(player.getUniqueId());
        chatInputPrompts.remove(player.getUniqueId());
    }

    public void startMoveBlockMode(Player player, String crateName) {
        moveBlockTargets.put(player.getUniqueId(), crateName);
    }

    public boolean isMoveBlockMode(Player player) {
        return moveBlockTargets.containsKey(player.getUniqueId());
    }

    public String getMoveBlockTarget(Player player) {
        return moveBlockTargets.get(player.getUniqueId());
    }

    public void removeMoveBlockMode(Player player) {
        moveBlockTargets.remove(player.getUniqueId());
    }

    public JavaPlugin getPlugin() { return plugin; }
    public CrateDatabase getDatabase() { return database; }
    public CrateManager getCrateManager() { return crateManager; }
    public KeyManager getKeyManager() { return keyManager; }
    public HologramManager getHologramManager() { return hologramManager; }
}
