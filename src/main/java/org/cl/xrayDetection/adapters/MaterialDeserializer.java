package org.cl.xrayDetection.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.lang.reflect.Type;

public final class MaterialDeserializer implements JsonDeserializer<Material> {

    @Override
    public Material deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        String raw = jsonElement.getAsString();
        Material material = Material.getMaterial(raw);

        if (material == null) {
            Bukkit.getLogger().warning("Could not deserialize material " + raw + "!");
        }

        return material;
    }
}
