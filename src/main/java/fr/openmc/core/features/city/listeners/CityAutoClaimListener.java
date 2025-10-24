package fr.openmc.core.features.city.listeners;

import fr.openmc.core.features.city.City;
import fr.openmc.core.features.city.CityManager;
import fr.openmc.core.features.city.actions.CityClaimAction;
import fr.openmc.core.features.city.conditions.CityClaimCondition;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.bukkit.Chunk;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class CityAutoClaimListener implements Listener {
    private static final ObjectSet<UUID> autoClaimingPlayers = new ObjectOpenHashSet<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        Chunk newChunk = event.getTo().getChunk();
        if (event.getFrom().getChunk().equals(newChunk))
            return;

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (!autoClaimingPlayers.contains(uuid))
            return;

        City city = CityManager.getPlayerCity(uuid);
        if (city == null) {
            autoClaimingPlayers.remove(uuid);
            return;
        }

        // Sends mesage if condition fails
        if (!CityClaimCondition.canCityClaim(city, player))
            return;

        // Sends message if claim is successful
        CityClaimAction.startClaim(player, newChunk.getX(), newChunk.getZ());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        autoClaimingPlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        autoClaimingPlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerLeave(PlayerQuitEvent event) {
        autoClaimingPlayers.remove(event.getPlayer().getUniqueId());
    }

    public static void addAutoClaimingPlayer(UUID uuid) {
        autoClaimingPlayers.add(uuid);
    }

    public static void removeAutoClaimingPlayer(UUID uuid) {
        autoClaimingPlayers.remove(uuid);
    }

    public static boolean isAutoClaiming(UUID uuid) {
        return autoClaimingPlayers.contains(uuid);
    }
}
