package ro.andreilarazboi.donutcore.crates.command;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import ro.andreilarazboi.donutcore.crates.CratesModule;
import ro.andreilarazboi.donutcore.crates.gui.CrateEditorGUI;
import ro.andreilarazboi.donutcore.crates.gui.CrateListGUI;
import ro.andreilarazboi.donutcore.crates.model.Crate;
import ro.andreilarazboi.donutcore.crates.util.ColorUtil;
import ro.andreilarazboi.donutcore.crates.util.MessageUtil;

import java.util.*;
import java.util.stream.Collectors;

public class CrateCommand implements CommandExecutor, TabCompleter {

    private final CratesModule module;

    public CrateCommand(CratesModule module) {
        this.module = module;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sendHelp(sender);
                return true;
            }
            if (player.hasPermission("donutcore.crate.admin")) {
                new CrateListGUI(module).open(player);
            } else {
                sendHelp(sender);
            }
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "help" -> sendHelp(sender);
            case "reload" -> {
                if (!sender.hasPermission("donutcore.crate.admin")) {
                    MessageUtil.sendRaw(sender instanceof Player p ? p : null, "&cNo permission.");
                    return true;
                }
                module.reload();
                if (sender instanceof Player p) MessageUtil.send(p, "config-reloaded");
                else sender.sendMessage("Config reloaded.");
            }
            case "create" -> {
                if (!sender.hasPermission("donutcore.crate.admin")) { noPermission(sender); return true; }
                if (args.length < 2) { usage(sender, "/" + label + " create <Name>"); return true; }
                String name = args[1];
                if (module.getCrateManager().exists(name)) {
                    if (sender instanceof Player p)
                        MessageUtil.send(p, "crate-already-exists", Map.of("crate", name));
                    else sender.sendMessage("Crate already exists!");
                    return true;
                }
                Crate crate = module.getCrateManager().createCrate(name);
                if (sender instanceof Player p) {
                    MessageUtil.send(p, "crate-created", Map.of("crate", name));
                    new CrateEditorGUI(module, crate).open(p);
                } else {
                    sender.sendMessage("Crate " + name + " created.");
                }
            }
            case "delete" -> {
                if (!sender.hasPermission("donutcore.crate.admin")) { noPermission(sender); return true; }
                if (args.length < 2) { usage(sender, "/" + label + " delete <Name>"); return true; }
                String name = args[1];
                if (!module.getCrateManager().exists(name)) {
                    cratNotFound(sender, name); return true;
                }
                module.getHologramManager().removeHologram(name);
                module.getCrateManager().deleteCrate(name);
                if (sender instanceof Player p) MessageUtil.send(p, "crate-deleted", Map.of("crate", name));
                else sender.sendMessage("Crate deleted.");
            }
            case "keygive", "key", "give" -> {
                if (!sender.hasPermission("donutcore.crate.key.give")) { noPermission(sender); return true; }
                if (args.length < 4) { usage(sender, "/" + label + " keygive <Player> <Crate> <Amount>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    if (sender instanceof Player p) MessageUtil.send(p, "player-not-found", Map.of("player", args[1]));
                    else sender.sendMessage("Player not found.");
                    return true;
                }
                Crate crate = module.getCrateManager().getCrate(args[2]);
                if (crate == null) { cratNotFound(sender, args[2]); return true; }
                int amount;
                try { amount = Integer.parseInt(args[3]); } catch (NumberFormatException e) { usage(sender, "/" + label + " keygive <Player> <Crate> <Amount>"); return true; }
                module.getKeyManager().addKeys(target, crate, amount);
                if (sender instanceof Player p) MessageUtil.send(p, "key-given", Map.of("amount", String.valueOf(amount), "crate", crate.getName(), "player", target.getName()));
                else sender.sendMessage("Given " + amount + " key(s) to " + target.getName());
                MessageUtil.send(target, "key-received", Map.of("amount", String.valueOf(amount), "crate", crate.getName()));
            }
            case "keytake", "take" -> {
                if (!sender.hasPermission("donutcore.crate.key.give")) { noPermission(sender); return true; }
                if (args.length < 4) { usage(sender, "/" + label + " keytake <Player> <Crate> <Amount>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { if (sender instanceof Player p) MessageUtil.send(p, "player-not-found", Map.of("player", args[1])); return true; }
                Crate crate = module.getCrateManager().getCrate(args[2]);
                if (crate == null) { cratNotFound(sender, args[2]); return true; }
                int amount;
                try { amount = Integer.parseInt(args[3]); } catch (NumberFormatException e) { return true; }
                module.getKeyManager().removeKeys(target, crate, amount);
                if (sender instanceof Player p) MessageUtil.send(p, "key-taken", Map.of("amount", String.valueOf(amount), "crate", crate.getName(), "player", target.getName()));
                else sender.sendMessage("Taken " + amount + " key(s) from " + target.getName());
            }
            case "keyall" -> {
                if (!sender.hasPermission("donutcore.crate.key.give")) { noPermission(sender); return true; }
                if (args.length < 3) { usage(sender, "/" + label + " keyall <Crate> <Amount>"); return true; }
                Crate crate = module.getCrateManager().getCrate(args[1]);
                if (crate == null) { cratNotFound(sender, args[1]); return true; }
                int amount;
                try { amount = Integer.parseInt(args[2]); } catch (NumberFormatException e) { return true; }
                int finalAmount = amount;
                for (Player online : Bukkit.getOnlinePlayers()) {
                    module.getKeyManager().addKeys(online, crate, finalAmount);
                    MessageUtil.send(online, "key-received", Map.of("amount", String.valueOf(finalAmount), "crate", crate.getName()));
                }
                sender.sendMessage(ColorUtil.colorize("&aGiven &e" + finalAmount + " &akey(s) for &e" + crate.getName() + " &ato all players."));
            }
            case "keypay", "pay" -> {
                if (!sender.hasPermission("donutcore.crate.key.pay")) { noPermission(sender); return true; }
                if (!(sender instanceof Player from)) { sender.sendMessage("Players only."); return true; }
                if (!module.getPlugin().getConfig().getBoolean("crate-key-payment-enabled", true)) {
                    from.sendMessage(ColorUtil.colorize("&cKey payments are disabled."));
                    return true;
                }
                if (args.length < 4) { usage(sender, "/" + label + " keypay <Player> <Crate> <Amount>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { MessageUtil.send(from, "player-not-found", Map.of("player", args[1])); return true; }
                Crate crate = module.getCrateManager().getCrate(args[2]);
                if (crate == null) { cratNotFound(sender, args[2]); return true; }
                int amount;
                try { amount = Integer.parseInt(args[3]); } catch (NumberFormatException e) { return true; }
                if (!module.getKeyManager().payKeys(from, target, crate, amount)) {
                    MessageUtil.send(from, "insufficient-keys-pay");
                    return true;
                }
                MessageUtil.send(from, "key-paid", Map.of("player", target.getName(), "crate", crate.getName(), "amount", String.valueOf(amount)));
                MessageUtil.send(target, "key-paid-received", Map.of("player", from.getName(), "crate", crate.getName(), "amount", String.valueOf(amount)));
            }
            case "resetkey" -> {
                if (!sender.hasPermission("donutcore.crate.admin")) { noPermission(sender); return true; }
                if (args.length < 3) { usage(sender, "/" + label + " resetkey <Crate> <Player/all>"); return true; }
                Crate crate = module.getCrateManager().getCrate(args[1]);
                if (crate == null) { cratNotFound(sender, args[1]); return true; }
                if (args[2].equalsIgnoreCase("all")) {
                    module.getKeyManager().resetAllKeys(crate);
                    if (sender instanceof Player p) MessageUtil.send(p, "key-reset-all", Map.of("crate", crate.getName()));
                    else sender.sendMessage("Reset all keys for " + crate.getName());
                } else {
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) { if (sender instanceof Player p) MessageUtil.send(p, "player-not-found", Map.of("player", args[2])); return true; }
                    module.getKeyManager().resetKeys(target, crate);
                    if (sender instanceof Player p) MessageUtil.send(p, "key-reset", Map.of("player", target.getName(), "crate", crate.getName()));
                    else sender.sendMessage("Reset keys for " + target.getName());
                }
            }
            case "addhanditem" -> {
                if (!sender.hasPermission("donutcore.crate.admin")) { noPermission(sender); return true; }
                if (!(sender instanceof Player player)) { sender.sendMessage("Players only."); return true; }
                if (args.length < 3) { usage(sender, "/" + label + " addhanditem <Crate> <ItemName>"); return true; }
                Crate crate = module.getCrateManager().getCrate(args[1]);
                if (crate == null) { cratNotFound(sender, args[1]); return true; }
                org.bukkit.inventory.ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand.getType() == org.bukkit.Material.AIR) {
                    MessageUtil.send(player, "hand-empty"); return true;
                }
                String itemId = args[2];
                ro.andreilarazboi.donutcore.crates.model.CrateItem item = new ro.andreilarazboi.donutcore.crates.model.CrateItem(itemId);
                item.setMaterial(hand.getType());
                org.bukkit.inventory.meta.ItemMeta meta = hand.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    item.setDisplayName(meta.getDisplayName());
                } else {
                    item.setDisplayName("&f" + itemId);
                }
                if (meta != null && meta.hasLore()) {
                    item.setLore(new ArrayList<>(meta.getLore().stream().map(s -> ColorUtil.colorize(s)).toList()));
                }
                item.setSlot(crate.getItems().size());
                item.setChance(50.0);
                crate.addItem(item);
                module.getCrateManager().saveCrate(crate);
                MessageUtil.send(player, "item-added", Map.of("item", itemId, "crate", crate.getName()));
            }
            case "removeitem" -> {
                if (!sender.hasPermission("donutcore.crate.admin")) { noPermission(sender); return true; }
                if (args.length < 3) { usage(sender, "/" + label + " removeitem <Crate> <ItemName>"); return true; }
                Crate crate = module.getCrateManager().getCrate(args[1]);
                if (crate == null) { cratNotFound(sender, args[1]); return true; }
                String itemId = args[2];
                if (!crate.getItems().containsKey(itemId)) {
                    if (sender instanceof Player p) MessageUtil.send(p, "item-not-found", Map.of("item", itemId, "crate", crate.getName()));
                    return true;
                }
                crate.removeItem(itemId);
                module.getCrateManager().saveCrate(crate);
                if (sender instanceof Player p) MessageUtil.send(p, "item-removed", Map.of("item", itemId, "crate", crate.getName()));
                else sender.sendMessage("Item removed.");
            }
            case "moveblock" -> {
                if (!sender.hasPermission("donutcore.crate.admin")) { noPermission(sender); return true; }
                if (!(sender instanceof Player player)) { sender.sendMessage("Players only."); return true; }
                if (args.length < 2) { usage(sender, "/" + label + " moveblock <Crate>"); return true; }
                Crate crate = module.getCrateManager().getCrate(args[1]);
                if (crate == null) { cratNotFound(sender, args[1]); return true; }
                module.startMoveBlockMode(player, crate.getName());
                MessageUtil.send(player, "moveblock-mode", Map.of("crate", crate.getName()));
            }
            case "stats" -> {
                if (!(sender instanceof Player player)) { sender.sendMessage("Players only."); return true; }
                if (!player.hasPermission("donutcore.crate.stats")) { noPermission(sender); return true; }
                showStats(player);
            }
            case "givekey" -> {
                // alias for keygive but with physical key item
                if (!sender.hasPermission("donutcore.crate.key.give")) { noPermission(sender); return true; }
                if (args.length < 4) { usage(sender, "/" + label + " givekey <Player> <Crate> <Amount>"); return true; }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) { if (sender instanceof Player p) MessageUtil.send(p, "player-not-found", Map.of("player", args[1])); return true; }
                Crate crate = module.getCrateManager().getCrate(args[2]);
                if (crate == null) { cratNotFound(sender, args[2]); return true; }
                int amount;
                try { amount = Integer.parseInt(args[3]); } catch (NumberFormatException e) { return true; }
                org.bukkit.inventory.ItemStack keyItem = module.getCrateManager().createKeyItem(crate);
                keyItem.setAmount(Math.min(amount, 64));
                target.getInventory().addItem(keyItem);
                module.getKeyManager().addKeys(target, crate, amount);
                if (sender instanceof Player p) MessageUtil.send(p, "key-given", Map.of("amount", String.valueOf(amount), "crate", crate.getName(), "player", target.getName()));
                MessageUtil.send(target, "key-received", Map.of("amount", String.valueOf(amount), "crate", crate.getName()));
            }
            default -> sendHelp(sender);
        }

        return true;
    }

    private void showStats(Player player) {
        MessageUtil.sendRaw(player, module.getPlugin().getConfig().getString("messages.stats-header", "&8&m--------- &6Your Crate Stats &8&m---------"));
        var stats = module.getDatabase().getStats(player.getUniqueId());
        if (stats.isEmpty()) {
            MessageUtil.sendRaw(player, module.getPlugin().getConfig().getString("messages.stats-empty", "&7No crate opens yet."));
        } else {
            stats.forEach((crate, data) -> {
                String lastReward = module.getDatabase().getLastReward(player.getUniqueId(), crate);
                String line = module.getPlugin().getConfig()
                        .getString("messages.stats-crate-line", "  &7{crate}: &e{opens} &7opens - Last: &e{last_reward}")
                        .replace("{crate}", crate)
                        .replace("{opens}", String.valueOf(data[0]))
                        .replace("{last_reward}", lastReward);
                MessageUtil.sendRaw(player, line);
            });
        }
        MessageUtil.sendRaw(player, module.getPlugin().getConfig().getString("messages.stats-footer", "&8&m------------------------------------"));
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.colorize("&8&m--------- &6DonutCrate Help &8&m---------"));
        sender.sendMessage(ColorUtil.colorize("&e/donutcrate &7- Open crate manager"));
        sender.sendMessage(ColorUtil.colorize("&e/donutcrate create <Name> &7- Create a crate"));
        sender.sendMessage(ColorUtil.colorize("&e/donutcrate delete <Name> &7- Delete a crate"));
        sender.sendMessage(ColorUtil.colorize("&e/donutcrate keygive <Player> <Crate> <Amount> &7- Give keys"));
        sender.sendMessage(ColorUtil.colorize("&e/donutcrate keytake <Player> <Crate> <Amount> &7- Take keys"));
        sender.sendMessage(ColorUtil.colorize("&e/donutcrate keyall <Crate> <Amount> &7- Give keys to all"));
        sender.sendMessage(ColorUtil.colorize("&e/donutcrate keypay <Player> <Crate> <Amount> &7- Pay keys"));
        sender.sendMessage(ColorUtil.colorize("&e/donutcrate resetkey <Crate> <Player/all> &7- Reset keys"));
        sender.sendMessage(ColorUtil.colorize("&e/donutcrate addhanditem <Crate> <ItemName> &7- Add held item"));
        sender.sendMessage(ColorUtil.colorize("&e/donutcrate removeitem <Crate> <ItemName> &7- Remove item"));
        sender.sendMessage(ColorUtil.colorize("&e/donutcrate moveblock <Crate> &7- Place crate block"));
        sender.sendMessage(ColorUtil.colorize("&e/donutcrate stats &7- View your crate stats"));
        sender.sendMessage(ColorUtil.colorize("&e/donutcrate reload &7- Reload configuration"));
        sender.sendMessage(ColorUtil.colorize("&8&m---------------------------------------"));
    }

    private void noPermission(CommandSender sender) {
        sender.sendMessage(ColorUtil.colorize("&cYou do not have permission."));
    }

    private void usage(CommandSender sender, String usage) {
        sender.sendMessage(ColorUtil.colorize("&cUsage: &f" + usage));
    }

    private void cratNotFound(CommandSender sender, String name) {
        sender.sendMessage(ColorUtil.colorize("&cCrate &e" + name + " &cdoes not exist."));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return filterStartsWith(List.of("help", "reload", "create", "delete", "keygive", "keytake",
                    "keyall", "keypay", "resetkey", "addhanditem", "removeitem", "moveblock", "stats", "givekey"), args[0]);
        }
        if (args.length == 2) {
            return switch (args[0].toLowerCase()) {
                case "delete", "moveblock", "keyall", "addhanditem", "removeitem" ->
                        filterStartsWith(new ArrayList<>(module.getCrateManager().getCratesMap().keySet()), args[1]);
                case "keygive", "keytake", "keypay", "resetkey", "givekey" ->
                        filterStartsWith(Bukkit.getOnlinePlayers().stream()
                                .map(Player::getName).collect(Collectors.toList()), args[1]);
                default -> List.of();
            };
        }
        if (args.length == 3) {
            return switch (args[0].toLowerCase()) {
                case "keygive", "keytake", "keypay", "givekey" ->
                        filterStartsWith(new ArrayList<>(module.getCrateManager().getCratesMap().keySet()), args[2]);
                case "resetkey" -> {
                    List<String> suggestions = new ArrayList<>();
                    suggestions.add("all");
                    Bukkit.getOnlinePlayers().stream().map(Player::getName).forEach(suggestions::add);
                    yield filterStartsWith(suggestions, args[2]);
                }
                case "removeitem" -> {
                    Crate crate = module.getCrateManager().getCrate(args[1]);
                    if (crate == null) yield List.of();
                    yield filterStartsWith(new ArrayList<>(crate.getItems().keySet()), args[2]);
                }
                default -> List.of();
            };
        }
        return List.of();
    }

    private List<String> filterStartsWith(List<String> list, String prefix) {
        return list.stream()
                .filter(s -> s.toLowerCase().startsWith(prefix.toLowerCase()))
                .collect(Collectors.toList());
    }
}
