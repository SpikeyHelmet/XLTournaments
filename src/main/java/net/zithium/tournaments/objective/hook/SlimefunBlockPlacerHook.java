/*
 * XLTournaments Plugin
 * Copyright (c) 2020 - 2023 Zithium Studios. All rights reserved.
 */

package net.zithium.tournaments.objective.hook;

import io.github.thebusybiscuit.slimefun4.api.events.BlockPlacerPlaceEvent;
import net.zithium.tournaments.XLTournamentsPlugin;
import net.zithium.tournaments.utility.universal.XBlock;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;

public class SlimefunBlockPlacerHook implements Listener {

    private final XLTournamentsPlugin plugin;
    private boolean excludePlaced;

    public SlimefunBlockPlacerHook(XLTournamentsPlugin plugin, boolean excludePlaced) {
        this.plugin = plugin;
        this.excludePlaced = excludePlaced;
    }
    
    public void setExcludePlaced(boolean excludePlaced) {
        this.excludePlaced = excludePlaced;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlacerPlace(BlockPlacerPlaceEvent event) {
        Block block = event.getBlock();
        if (excludePlaced) {
            if ((block.getType().equals(Material.SUGAR_CANE) || block.getType().equals(Material.CACTUS)) && !XBlock.isCrop(block)) {
                block.setMetadata("XLTPlacedBlock", new FixedMetadataValue(plugin, "SlimefunBlockPlacer"));
            }

            block.setMetadata("XLTPlacedBlock", new FixedMetadataValue(plugin, "SlimefunBlockPlacer"));
        }
    }
}

