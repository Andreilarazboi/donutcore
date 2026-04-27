package ro.andreilarazboi.donutcore.crates.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import ro.andreilarazboi.donutcore.DonutCore;

import java.util.Map;

public final class MessageUtil {

    private MessageUtil() {}

    public static String get(String key) {
        FileConfiguration config = DonutCore.getInstance().getConfig();
        String prefix = config.getString("prefix", "&8[&6DonutCrate&8] &r");
        String msg = config.getString("messages." + key, "&c[Missing message: " + key + "]");
        return ColorUtil.colorize(prefix + msg);
    }

    public static String get(String key, Map<String, String> placeholders) {
        String msg = get(key);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            msg = msg.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return msg;
    }

    public static void send(Player player, String key) {
        player.sendMessage(get(key));
    }

    public static void send(Player player, String key, Map<String, String> placeholders) {
        player.sendMessage(get(key, placeholders));
    }

    public static void sendRaw(Player player, String message) {
        player.sendMessage(ColorUtil.colorize(message));
    }

    public static void sendActionBar(Player player, String message) {
        FileConfiguration config = DonutCore.getInstance().getConfig();
        if (!config.getBoolean("actionbar-enabled", true)) {
            player.sendMessage(ColorUtil.colorize(message));
            return;
        }
        Component component = ColorUtil.parse(message).decoration(TextDecoration.ITALIC, false);
        player.sendActionBar(component);
    }

    public static void broadcast(String message) {
        org.bukkit.Bukkit.broadcastMessage(ColorUtil.colorize(message));
    }
}
