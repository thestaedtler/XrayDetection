package org.cl.xrayDetection;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.bukkit.Bukkit;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Set;

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
    private Set<Material> targetMaterials;
    private int maxVeinIterations;

    private void cacheConfig() throws IOException {
        getDataFolder().mkdirs();
        File file = new File(getDataFolder(), "xray-detection.json");

        if (!file.exists()) {
            try (InputStream stream = getResource("xray-detection.json")) {
                if (stream == null) {
                    throw new IOException("Empty resource stream");
                }

                Files.copy(stream, file.toPath());
            }
        }

        JsonObject obj;
        try (JsonReader reader = new JsonReader(new FileReader(file))) {
            obj = JsonParser.parseReader(reader).getAsJsonObject();
        }

        JsonArray array = obj.getAsJsonArray("targets");
        ImmutableSet.Builder<Material> targetSetBuilder = new ImmutableSet.Builder<>();
        for (JsonElement rawTarget : array) {
            Material mat = Material.getMaterial(rawTarget.getAsString());

            if (mat == null) {
                Bukkit.getLogger().warning("Could not recognize material " + rawTarget.getAsString() + " specified in xray-detection.json! Skipping...");
                continue;
            }

            targetSetBuilder.add(mat);
        }

        this.targetMaterials = targetSetBuilder.build();
        this.maxVeinIterations = Math.min(obj.get("max-vein-iterations").getAsInt(), 30);
    }

    @Override
    public void onEnable() {
        try {
            cacheConfig();
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

    boolean canDetect(Material material) {
        return targetMaterials.contains(material);
    }

    int getMaxVeinIterations() {
        return maxVeinIterations;
    }

    void alert(String message) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!player.hasPermission(ALERT_PERMISSION)) {
                continue;
            }

            player.sendMessage(message);
        }

        Bukkit.getLogger().info(message);
    }
}
