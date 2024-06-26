package com.gmail.nossr50.util.player;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import com.gmail.nossr50.mcMMO;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;

public final class UserManager {

    private UserManager() {}

    /**
     * Add a new user.
     *
     * @param player The player to create a user record for
     * @return the player's {@link McMMOPlayer} object
     */
    public static McMMOPlayer addUser(Player player) {
        McMMOPlayer mcMMOPlayer = new McMMOPlayer(player);
        player.setMetadata(mcMMO.playerDataKey, new FixedMetadataValue(mcMMO.p, mcMMOPlayer));

        return mcMMOPlayer;
    }

    /**
     * Remove a user.
     *
     * @param player The Player object
     */
    public static void remove(Player player) {
        player.removeMetadata(mcMMO.playerDataKey, mcMMO.p);
    }

    /**
     * Clear all users.
     */
    public static void clearAll() {
        for (Player player : mcMMO.p.getServer().getOnlinePlayers()) {
            remove(player);
        }
    }

    /**
     * Save all users.
     */
    public static void saveAll() {
        Player[] onlinePlayers = mcMMO.p.getServer().getOnlinePlayers().toArray(new Player[0]);
        mcMMO.p.debug("Saving mcMMOPlayers... (" + onlinePlayers.length + ")");

        for (Player player : onlinePlayers) {
            getPlayer(player).getProfile().save();
        }
    }

    public static Collection<McMMOPlayer> getPlayers() {
        Collection<McMMOPlayer> playerCollection  = new ArrayList<McMMOPlayer>();

        for (Player player : mcMMO.p.getServer().getOnlinePlayers()) {
            playerCollection.add(getPlayer(player));
        }

        return playerCollection;
    }

    /**
     * Get the McMMOPlayer of a player by name.
     *
     * @param playerName The name of the player whose McMMOPlayer to retrieve
     * @return the player's McMMOPlayer object
     */
    public static McMMOPlayer getPlayer(String playerName) {
        return retrieveMcMMOPlayer(playerName, false);
    }

    public static McMMOPlayer getOfflinePlayer(OfflinePlayer player) {
        if (player instanceof Player) {
            return getPlayer((Player) player);
        }

        return retrieveMcMMOPlayer(player.getName(), true);
    }

    public static McMMOPlayer getOfflinePlayer(String playerName) {
        return retrieveMcMMOPlayer(playerName, true);
    }

    public static McMMOPlayer getPlayer(Player player) {
        return (McMMOPlayer) player.getMetadata(mcMMO.playerDataKey).get(0).value();
    }

    private static McMMOPlayer retrieveMcMMOPlayer(String playerName, boolean offlineValid) {
        Player player = mcMMO.p.getServer().getPlayerExact(playerName);

        if (player == null) {
            if (!offlineValid) {
                mcMMO.p.getLogger().warning("A valid mcMMOPlayer object could not be found for " + playerName + ".");
            }

            return null;
        }

        return getPlayer(player);
    }

    public static boolean hasPlayerDataKey(Entity entity) {
        return entity != null && entity.hasMetadata(mcMMO.playerDataKey);
    }
}
