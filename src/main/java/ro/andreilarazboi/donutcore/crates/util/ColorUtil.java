package ro.andreilarazboi.donutcore.crates.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public final class ColorUtil {

    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private ColorUtil() {}

    public static Component parse(String text) {
        if (text == null) return Component.empty();
        return LEGACY.deserialize(text);
    }

    public static String colorize(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static List<String> colorizeList(List<String> list) {
        List<String> result = new ArrayList<>();
        if (list == null) return result;
        for (String line : list) result.add(colorize(line));
        return result;
    }

    public static List<Component> parseList(List<String> list) {
        List<Component> result = new ArrayList<>();
        if (list == null) return result;
        for (String line : list) result.add(parse(line));
        return result;
    }

    public static String strip(String text) {
        if (text == null) return "";
        return ChatColor.stripColor(colorize(text));
    }
}
