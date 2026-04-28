
package ro.andreilarazboi.donutcore.crates;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class HologramListener
implements Listener {
    private final DonutCrates plugin;

    public HologramListener(DonutCrates plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        this.plugin.holoMgr.handleJoin(e.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        this.plugin.holoMgr.handleQuit(e.getPlayer());
    }
}

