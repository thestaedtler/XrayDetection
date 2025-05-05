package org.cl.xrayDetection;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
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

    XrayListener(XrayDetection plugin) {
        this.plugin = plugin;
    }

    private void alert(Player target, Material material, boolean exposed, VeinInfo info) {
        ComponentBuilder builder = new ComponentBuilder()
                .append("[!] ").color(ChatColor.RED)
                .append(target.getName()).color(ChatColor.YELLOW)
                .append(" found " + (exposed ? "exposed " : "")).color(ChatColor.GRAY)
                .append(material.toString()).color(ChatColor.YELLOW)
                .append(" (vein of " + info.amount() + (info.achievedMaxIterations() ? "+" : "") + ")").color(ChatColor.GRAY);

        if (plugin.getConfigCache().consoleLogging()) {
            Bukkit.getLogger().info(builder.build().toPlainText());
        }

        BaseComponent finalComponent = builder.build();

        if (plugin.getConfigCache().hoverMessage() != null) {
            finalComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(TextComponent.fromLegacy(ChatColor.translateAlternateColorCodes('&', plugin.getConfigCache().hoverMessage())))));
        }

        finalComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/xdet " + target.getName()));

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission(XrayDetection.ALERT_PERMISSION)) {
                continue;
            }

            player.spigot().sendMessage(finalComponent);
        }
    }

    private boolean flags(Material material) {
        return plugin.getConfigCache().materials().contains(material);
    }

    // Direct
    @EventHandler
    public void onTargetBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission(XrayDetection.BYPASS_PERMISSION)) {
            return;
        }

        Block block = event.getBlock();

        if (!flags(block.getType())) {
            return;
        }

        VeinInfo info = VeinInfo.collect(block.getLocation(), plugin.getConfigCache().maxVeinIterations(), member -> {
            member.setMetadata(METADATA_KEY, new FixedMetadataValue(plugin, null));
        }, VeinInfo.Condition.material(block.getType()), VeinInfo.Condition.excludeMetadata(METADATA_KEY));

        if (info.amount() == 0) {
            return;
        }

        alert(player, block.getType(), true, info);
    }

    // Indirect
    @EventHandler
    public void onTargetFind(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (player.hasPermission(XrayDetection.BYPASS_PERMISSION)) {
            return;
        }

        Block block = event.getBlock();

        if (flags(block.getType())) {
            return;
        }

        Set<Location> locations = new HashSet<>();
        for (BlockFace face : XrayDetection.RELEVANT_FACES) {
            Block relative = block.getRelative(face);

            if (!flags(relative.getType())) {
                continue;
            }

            VeinInfo info = VeinInfo.collect(relative.getLocation(), plugin.getConfigCache().maxVeinIterations(), member -> {
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

            alert(player, relative.getType(), false, info);
        }
    }
}
