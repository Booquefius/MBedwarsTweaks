package me.metallicgoat.MBedwarsTweaks.tweaks.misc;

import de.marcely.bedwars.api.BedwarsAPI;
import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.arena.ArenaStatus;
import de.marcely.bedwars.api.event.arena.RoundEndEvent;
import de.marcely.bedwars.api.event.player.PlayerJoinArenaEvent;
import me.clip.placeholderapi.PlaceholderAPI;
import me.metallicgoat.MBedwarsTweaks.Main;
import me.metallicgoat.MBedwarsTweaks.utils.ServerManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;


public class ActionBar implements Listener {

    private BukkitTask actionBarTask = null;

    @EventHandler
    public void onGameStart(PlayerJoinArenaEvent e){
        boolean enabled = ServerManager.getConfig().getBoolean("Action-Bar-Enabled");
        if(enabled && Main.papiEnabled) {
            //Start updating ActionBar
            if(actionBarTask == null){
                actionBarTask = startUpdatingTime();
            }
        }
    }

    @EventHandler
    public void onGameStop(RoundEndEvent event){
        boolean enabled = ServerManager.getConfig().getBoolean("Action-Bar-Enabled");
        if(enabled && Main.papiEnabled) {
            //Dont kill task if
            for (Arena arena : BedwarsAPI.getGameAPI().getArenas()) {
                if (arena.getPlayers().isEmpty()) {
                    return;
                }
            }
            //Kill task
            if (actionBarTask != null) {
                actionBarTask.cancel();
                actionBarTask = null;
            }
        }
    }

    private static BukkitTask startUpdatingTime(){
        BukkitScheduler scheduler = plugin().getServer().getScheduler();
        String actionBarText = ServerManager.getConfig().getString("Action-Bar-Message");
        boolean enabledInLobby = ServerManager.getConfig().getBoolean("Action-Bar-Enabled-In-Lobby");

        return scheduler.runTaskTimer(plugin(),() -> {

            for(Arena arena:BedwarsAPI.getGameAPI().getArenas()){
                if((arena.getStatus() == ArenaStatus.RUNNING) || (arena.getStatus() == ArenaStatus.LOBBY && enabledInLobby)){
                    for(Player player:arena.getPlayers()){
                        if(actionBarText != null) {
                            BedwarsAPI.getNMSHelper().showActionbar(player, PlaceholderAPI.setPlaceholders(player, actionBarText));
                        }
                    }
                }
            }
        }, 0L, 20L);
    }

    private static Main plugin(){
        return Main.getInstance();
    }
}
