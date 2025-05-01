package org.cl.xrayDetection;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Set;

public record ConfigCache(
        Set<Material> materials,
        int maxVeinIterations,
        boolean consoleLogging
) {
    public static ConfigCache cache() throws IOException {
        XrayDetection plugin = JavaPlugin.getPlugin(XrayDetection.class);

        plugin.getDataFolder().mkdirs();
        File file = new File(plugin.getDataFolder(), "xray-detection.json");

        if (!file.exists()) {
            try (InputStream stream = plugin.getResource("xray-detection.json")) {
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

        return new ConfigCache(
                targetSetBuilder.build(),
                Math.min(obj.get("max-vein-iterations").getAsInt(), 30),
                obj.get("console-alerts").getAsBoolean()
        );
    }
}
