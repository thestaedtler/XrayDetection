package org.cl.xrayDetection;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;
import org.cl.xrayDetection.util.VariableString;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

public record ConfigCache(
        Set<Material> materials,
        int maxVeinIterations,
        boolean consoleLogging,
        String hoverMessage,
        List<VariableString> clickCommands
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

        JsonArray materialArray = obj.getAsJsonArray("targets");
        Set<Material> materialSet = XrayDetection.GSON.fromJson(materialArray, new TypeToken<Set<Material>>() {}.getType());
        materialSet.remove(null);

        JsonElement rawHover = obj.get("alert-hover-message");
        String hover = rawHover == null ? null : rawHover.getAsString();

        JsonArray commandArray = obj.getAsJsonArray("alert-click-commands");
        List<VariableString> commands = XrayDetection.GSON.fromJson(commandArray, new TypeToken<List<VariableString>>() {}.getType());

        return new ConfigCache(
                materialSet,
                Math.min(obj.get("max-vein-iterations").getAsInt(), 30),
                obj.get("console-alerts").getAsBoolean(),
                hover,
                commands
        );
    }
}
