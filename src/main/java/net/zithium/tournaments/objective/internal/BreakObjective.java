/*
 * XLTournaments Plugin
 * Copyright (c) 2020 - 2023 Zithium Studios. All rights reserved.
 */

package net.zithium.tournaments.objective.internal;

import net.zithium.tournaments.XLTournamentsPlugin;
import net.zithium.tournaments.objective.XLObjective;
import net.zithium.tournaments.objective.hook.TEBlockExplode;
import net.zithium.tournaments.tournament.Tournament;
import net.zithium.tournaments.utility.universal.XBlock;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@SuppressWarnings("unchecked") // Suppressing unchecked warning for "List<String> whitelist"
public class BreakObjective extends XLObjective {

    private final XLTournamentsPlugin plugin;
    private boolean excludePlaced;

    public BreakObjective(@NotNull XLTournamentsPlugin plugin) {
        super("BLOCK_BREAK");

        // Exception handling for "TokenEnchant" plugin
        if (plugin.getServer().getPluginManager().isPluginEnabled("TokenEnchant")) {
            try {
                Bukkit.getServer().getPluginManager().registerEvents(new TEBlockExplode(this, excludePlaced), plugin);
            } catch (Exception e) {
                // Handle the exception
                plugin.getLogger().warning("Failed to register TokenEnchant event.");
            }
        }

        this.plugin = plugin;
    }

    @Override
    public boolean loadTournament(Tournament tournament, @NotNull FileConfiguration config) {
        if (config.contains("exclude_placed_blocks")) {
            excludePlaced = config.getBoolean("exclude_placed_blocks");
        }

        if (config.contains("block_whitelist")) {
            tournament.setMeta("BLOCK_WHITELIST_" + tournament.getIdentifier(), config.getStringList("block_whitelist"));
        }
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(@NotNull BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (XBlock.isCrop(block) && !XBlock.isCropFullyGrown(block)) {
            return;
        }

        for (Tournament tournament : getTournaments()) {
            if (!canExecute(tournament, player) || block.hasMetadata("XLTPlacedBlock")) {
                continue;
            }

            String tournamentIdentifier = tournament.getIdentifier();

            if (tournament.hasMeta("BLOCK_WHITELIST_" + tournamentIdentifier)) {
                List<String> whitelist = (List<String>) tournament.getMeta("BLOCK_WHITELIST_" + tournamentIdentifier);
                String blockType = block.getType().toString();


                if (whitelist.contains(blockType)) {
                    tournament.addScore(player.getUniqueId(), 1);
                }
            } else {
                // Ignore the whitelist if not present.
                tournament.addScore(player.getUniqueId(), 1);
            }
        }

    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if (excludePlaced) {
            if ((block.getType().equals(Material.SUGAR_CANE) || block.getType().equals(Material.CACTUS)) && !XBlock.isCrop(block)) {
                block.setMetadata("XLTPlacedBlock", new FixedMetadataValue(plugin, event.getPlayer().getName()));
            }

            block.setMetadata("XLTPlacedBlock", new FixedMetadataValue(plugin, event.getPlayer().getName()));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            Location to = block.getLocation().add(event.getDirection().getDirection());
            trackMovedBlock(to);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            Location to = block.getLocation().add(event.getDirection().getOppositeFace().getDirection());
            trackMovedBlock(to);
        }
    }

    private void trackMovedBlock(Location movedTo) {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Block newBlock = movedTo.getBlock();
            newBlock.setMetadata("XLTPlacedBlock", new FixedMetadataValue(plugin, true));
        }, 20L); // Delay by 20 tick to wait for the move to complete
    }

}