package me.metallicgoat.MBedwarsTweaks;

import de.marcely.bedwars.api.BedwarsAPI;
import de.marcely.bedwars.api.GameAPI;
import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.arena.ArenaStatus;
import de.marcely.bedwars.api.message.Message;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.metallicgoat.MBedwarsTweaks.tweaks.spawners.GenTiers;
import me.metallicgoat.MBedwarsTweaks.tweaks.spawners.GenTiersV2;
import me.metallicgoat.MBedwarsTweaks.utils.ServerManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Placeholders extends PlaceholderExpansion {

    @Override
    public @NotNull String getIdentifier() {
        return "tweaks";
    }

    @Override
    public @NotNull String getAuthor() {
        return "MetallicGoat";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {

        Player player1 = Bukkit.getPlayer(player.getUniqueId());

        switch (params.toLowerCase()){
            case "next-tier":
                //Gen Tiers
                if(ServerManager.getConfig().getBoolean("Gen-Tiers-Enabled")) {

                    Arena arena = BedwarsAPI.getGameAPI().getArenaByPlayer(player1);
                    if (arena != null) {

                        String nextTierName = GenTiersV2.nextTierMap.get(arena);
                        String nextTierTime = GenTiersV2.timeLeft(arena);

                        switch (arena.getStatus()) {
                            case LOBBY:
                                return "Game is still in Lobby";
                            case END_LOBBY:
                                return "Game Ended";
                            case STOPPED:
                                return "Game Stopped";
                            case RESETTING:
                                return "Game Resetting";
                            case RUNNING:
                                return Message.build(ServerManager.getConfig().getString("Next-Tier-Placeholder"))
                                        .placeholder("next-tier", nextTierName)
                                        .placeholder("time", nextTierTime)
                                        .done();
                        }
                    }
                }
                return "---";

            //Arena mode placeholder (eg. Solo, Duos)
            case "arena-mode":
                Arena arena = BedwarsAPI.getGameAPI().getArenaByPlayer(player1);
                if (arena != null) {
                    int teamsAmount = arena.getEnabledTeams().size();
                    int playersPerTeam = arena.getPlayersPerTeam();
                    for(String modeGroup : ServerManager.getConfig().getStringList("PAPI-Mode-Placeholder")){
                        String[] splitModeGroup = modeGroup.split(":");
                        try{
                            int groupTeams = Integer.parseInt(splitModeGroup[0]);
                            int groupPlayerPerTeam = Integer.parseInt(splitModeGroup[1]);
                            if(teamsAmount == groupTeams && playersPerTeam == groupPlayerPerTeam){
                                return splitModeGroup[2];
                            }
                        }catch (NumberFormatException ignored){

                        }
                    }
                }
                return "---";

            //Player count placeholders
            case "allplayers": return getPlayerAmount();
            case "players-ingame": return getPlayerAmount(ArenaStatus.RUNNING);
            case "players-lobby": return getPlayerAmount(ArenaStatus.LOBBY);
            case "players-endlobby": return getPlayerAmount(ArenaStatus.END_LOBBY);

            default: return null;
        }
    }

    private String getPlayerAmount(ArenaStatus status) {
        int count = 0;

        //Iterate through every arena and check arena status
        for (Arena arena : GameAPI.get().getArenas()) {
            if(arena.getStatus() == status) {
                count += (ServerManager.getConfig().getBoolean("Player-Count-Placeholder-Count-Spectators") ? arena.getPlayers().size() + arena.getSpectators().size() : arena.getPlayers().size());
            }
        }

        return Integer.toString(count);
    }

    private String getPlayerAmount() {
        int count = 0;

        //Iterate through every arena
        for (Arena arena : GameAPI.get().getArenas()) {
            count += (ServerManager.getConfig().getBoolean("Player-Count-Placeholder-Count-Spectators") ? arena.getPlayers().size() + arena.getSpectators().size() : arena.getPlayers().size());
        }

        return Integer.toString(count);
    }
}
