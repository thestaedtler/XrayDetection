package org.cl.xrayDetection;

import org.bukkit.Bukkit;

import org.bukkit.block.BlockFace;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

public final class XrayDetection extends JavaPlugin {
    public static final String BASE_PERMISSION = "xray";
    public static final String ALERT_PERMISSION = BASE_PERMISSION + ".alerts";
    public static final String BYPASS_PERMISSION = BASE_PERMISSION + ".bypass";
    public static final BlockFace[] RELEVANT_FACES = {
            BlockFace.DOWN,
            BlockFace.EAST,
            BlockFace.NORTH,
            BlockFace.SOUTH,
            BlockFace.WEST,
            BlockFace.UP
    };
    private ConfigCache cacheConfig;

    @Override
    public void onEnable() {
        try {
            this.cacheConfig = ConfigCache.cache();
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to cache xray detection configuration! Please contact the plugin developer.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        Bukkit.getPluginManager().registerEvents(new XrayListener(this), this);
    }

    @Override
    public void onDisable() {
    }

    ConfigCache getConfigCache() {
        return cacheConfig;
    }
}
