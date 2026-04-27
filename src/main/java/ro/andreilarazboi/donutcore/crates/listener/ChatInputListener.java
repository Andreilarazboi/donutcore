package ro.andreilarazboi.donutcore.crates.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ro.andreilarazboi.donutcore.crates.CratesModule;

public class ChatInputListener implements Listener {

    private final CratesModule module;

    public ChatInputListener(CratesModule module) {
        this.module = module;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!module.hasChatInput(player)) return;

        event.setCancelled(true);
        String input = event.getMessage();

        if (input.equalsIgnoreCase("cancel")) {
            module.removeChatInput(player);
            player.sendMessage("§cInput cancelled.");
            return;
        }

        java.util.function.Consumer<String> callback = module.getChatInputCallback(player);
        module.removeChatInput(player);

        org.bukkit.Bukkit.getScheduler().runTask(module.getPlugin(), () -> callback.accept(input));
    }
}
