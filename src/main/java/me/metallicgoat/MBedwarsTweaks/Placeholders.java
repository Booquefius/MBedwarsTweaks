package me.metallicgoat.MBedwarsTweaks;

import de.marcely.bedwars.api.BedwarsAPI;
import de.marcely.bedwars.api.GameAPI;
import de.marcely.bedwars.api.arena.Arena;
import de.marcely.bedwars.api.arena.ArenaStatus;
import de.marcely.bedwars.api.message.Message;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.metallicgoat.MBedwarsTweaks.tweaks.spawners.GenTiers;
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

        //Gen Tiers
        if(ServerManager.getConfig().getBoolean("Gen-Tiers-Enabled")) {
            if (params.equalsIgnoreCase("next-tier")) {
                Player player1 = Bukkit.getPlayer(player.getUniqueId());

                Arena arena = BedwarsAPI.getGameAPI().getArenaByPlayer(player1);
                if (arena != null) {

                    String nextTierName = GenTiers.nextTierMap.get(arena);
                    String nextTierTime = GenTiers.timeLeft(arena);

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
                return "---";
            }
        }

        if (params.equalsIgnoreCase("arena-mode")) {
            Player player1 = Bukkit.getPlayer(player.getUniqueId());

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
        }

        //Player Count of All Arenas
        if(params.equalsIgnoreCase("allplayers")) {
            return getPlayerAmount();
        }

        //Player Count of All Arenas in Lobby
        if(params.equalsIgnoreCase("players-lobby")) {
            return getPlayerAmount(ArenaStatus.LOBBY);
        }

        //Player Count of All Arenas Running
        if(params.equalsIgnoreCase("players-ingame")) {
            return getPlayerAmount(ArenaStatus.RUNNING);
        }

        //Player Count of All Arenas in EndLobby
        if(params.equalsIgnoreCase("players-endlobby")) {
            return getPlayerAmount(ArenaStatus.END_LOBBY);
        }
        return null;
    }

    private String getPlayerAmount(ArenaStatus status) {
        int count = 0;

        //Iterate through every arena
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
