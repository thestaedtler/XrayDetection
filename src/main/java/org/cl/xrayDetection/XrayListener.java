package org.cl.xrayDetection;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashSet;
import java.util.Set;

public final class XrayListener implements Listener {
    private static final String METADATA_KEY = "accounted";
    private final XrayDetection plugin;

    public XrayListener(XrayDetection plugin) {
        this.plugin = plugin;
    }

    // Direct
    @EventHandler
    public void onTargetBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission(XrayDetection.BYPASS_PERMISSION)) {
            return;
        }

        Block block = event.getBlock();

        if (!plugin.canDetect(block.getType())) {
            return;
        }

        VeinInfo info = VeinInfo.collect(block.getLocation(), plugin.getMaxVeinIterations(), member -> {
            member.setMetadata(METADATA_KEY, new FixedMetadataValue(plugin, null));
        }, VeinInfo.Condition.material(block.getType()), VeinInfo.Condition.excludeMetadata(METADATA_KEY));

        if (info.amount() == 0) {
            return;
        }

        plugin.alert(ChatColor.RED + "[!]" + ChatColor.YELLOW + " " + player.getName() + ChatColor.GRAY + " found exposed " + ChatColor.YELLOW + block.getType() + ChatColor.GRAY + " (vein of " + info.amount() + (info.achievedMaxIterations() ? "+" : "") + ")");
    }

    // Indirect
    @EventHandler
    public void onTargetFind(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission(XrayDetection.BYPASS_PERMISSION)) {
            return;
        }

        Block block = event.getBlock();

        if (plugin.canDetect(block.getType())) {
            return;
        }

        Set<Location> locations = new HashSet<>();
        for (BlockFace face : XrayDetection.RELEVANT_FACES) {
            Block relative = block.getRelative(face);

            if (!plugin.canDetect(relative.getType())) {
                continue;
            }

            VeinInfo info = VeinInfo.collect(relative.getLocation(), plugin.getMaxVeinIterations(), member -> {
                member.setMetadata(METADATA_KEY, new FixedMetadataValue(plugin, null));
                locations.add(member.getLocation());
            },
                    VeinInfo.Condition.material(relative.getType()),
                    VeinInfo.Condition.excludeMetadata(METADATA_KEY),
                    VeinInfo.Condition.exclude(locations)
            );


            if (info.amount() == 0) {
                continue;
            }

            plugin.alert(ChatColor.RED + "[!]" + ChatColor.YELLOW + " " + player.getName() + ChatColor.GRAY + " found " + ChatColor.YELLOW + relative.getType() + ChatColor.GRAY + " (vein of " + info.amount() + (info.achievedMaxIterations() ? "+" : "") + ")");
        }
    }
}
