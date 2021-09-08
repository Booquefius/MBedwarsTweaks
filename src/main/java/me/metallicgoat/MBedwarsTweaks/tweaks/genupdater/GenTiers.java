package me.metallicgoat.MBedwarsTweaks.tweaks.genupdater;

import de.marcely.bedwars.api.BedwarsAPI;
import de.marcely.bedwars.api.BedwarsAddon;
import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.arena.ArenaStatus;
import de.marcely.bedwars.api.event.arena.RoundStartEvent;
import de.marcely.bedwars.api.event.player.PlayerOpenShopEvent;
import de.marcely.bedwars.api.game.shop.BuyGroup;
import de.marcely.bedwars.api.game.shop.layout.ShopLayout;
import de.marcely.bedwars.api.game.spawner.Spawner;
import de.marcely.bedwars.api.game.spawner.SpawnerDurationModifier;
import me.metallicgoat.MBedwarsTweaks.Main;
import me.metallicgoat.MBedwarsTweaks.utils.ServerManager;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.Arrays;
import java.util.HashMap;

public class GenTiers implements Listener {

    public static HashMap<Arena, String> nextTierMap = new HashMap<>();
    public static HashMap<Arena, Long> timeToNextUpdate = new HashMap<>();

    @EventHandler
    public void onGameStart(RoundStartEvent e){
        Arena arena = e.getArena();

        boolean enabled = plugin().getConfig().getBoolean("Gen-Tiers-Enabled");
        ConfigurationSection sect = ServerManager.getTiersConfig().getConfigurationSection("Gen-Tiers");
        if(enabled) {
            assert sect != null;
            if (sect.contains(Integer.toString(1))) {
                scheduleTier(arena, 1);
            }
        }
    }

    private void scheduleTier(Arena arena, int key){
        BukkitScheduler scheduler = plugin().getServer().getScheduler();

        ConfigurationSection sect = ServerManager.getTiersConfig().getConfigurationSection("Gen-Tiers");

        assert sect != null;
        if(sect.contains(Integer.toString(key))) {

            final String tierName = ServerManager.getTiersConfig().getString("Gen-Tiers." + key + ".TierName");
            final long time = ServerManager.getTiersConfig().getLong("Gen-Tiers." + key + ".Time");
            final long speed = ServerManager.getTiersConfig().getLong("Gen-Tiers." + key + ".Speed");
            final String spawnerType = ServerManager.getTiersConfig().getString("Gen-Tiers." + key + ".Type");
            final String chat = ServerManager.getTiersConfig().getString("Gen-Tiers." + key + ".Chat");

            int newKey = key + 1;

            nextTierMap.remove(arena);
            nextTierMap.put(arena, tierName);

            timeToNextUpdate.remove(arena);
            timeToNextUpdate.put(arena, time * 20 * 60);

            scheduler.scheduleSyncDelayedTask(plugin(), () -> {

                if (arena.getStatus() == ArenaStatus.RUNNING) {
                    scheduleTier(arena, newKey);
                    for (Spawner s : arena.getSpawners()) {
                        if (getItemType(s).equalsIgnoreCase(spawnerType)) {
                            s.addDropDurationModifier("GEN_TIER_UPDATE", plugin(), SpawnerDurationModifier.Operation.SET, speed);
                            //s.setOverridingHologramLines(new String[]{"{spawner}", "Tier 1", "&fspawning in &7{time} &fseconds!"});
                            //System.out.println(Arrays.toString(s.getOverridingHologramLines()));
                        }
                    }
                    arena.getPlayers().forEach(p -> {
                        assert chat != null;
                        p.sendMessage(ChatColor.translateAlternateColorCodes('&', chat));
                    });
                } else {
                    nextTierMap.remove(arena);
                }
            }, time * 20 * 60);
        }
    }

    public static void startUpdatingTime(){
        BukkitScheduler scheduler = plugin().getServer().getScheduler();
        scheduler.runTaskTimer(plugin(),() -> {
            if(!timeToNextUpdate.isEmpty()){
                timeToNextUpdate.forEach((arena, integer) -> {
                    if(arena.getStatus() == ArenaStatus.RUNNING){
                        timeToNextUpdate.replace(arena, integer, integer - 5);
                    }
                });
            }
        }, 0L, 5L);
    }

    private String getItemType(Spawner s){
        for(ItemStack i : s.getDropType().getDroppingMaterials()){
            return i.getType().name();
        }
        return "";
    }

    public static String timeLeft(Arena arena) {

        int timeoutTicks = Math.toIntExact(timeToNextUpdate.get(arena));
        int timeoutSeconds = (timeoutTicks / 20);

        int minutes = (timeoutSeconds / 60) % 60;
        int seconds = timeoutSeconds % 60;

        return minutes + ":" + seconds;
    }

    private static Main plugin(){
        return Main.getInstance();
    }

}
