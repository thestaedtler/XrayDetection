package org.cl.xrayDetection.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.bukkit.Bukkit;
import org.cl.xrayDetection.util.VariableString;

import java.lang.reflect.Type;

public final class VariableStringDeserializer implements JsonDeserializer<VariableString> {
    private static final char OPEN = '[';
    private static final char CLOSE = ']';

    @Override
    public VariableString deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        try {
            return VariableString.from(jsonElement.getAsString(), OPEN, CLOSE);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().severe(e.getMessage() + " - Please ensure all '" + OPEN + "' are closed with '" + CLOSE + "'");
            return null;
        }
    }
}
