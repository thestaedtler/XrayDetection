package org.cl.xrayDetection.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class VariableString {
    private interface Component {}

    private record FixedComponent(String string) implements Component {
        @Override
        public String toString() {
            return string;
        }
    }

    private record VariableComponent(String variable) implements Component {
        public String build(Map<String, String> mapper) {
            return mapper.get(variable);
        }
    }

    private final List<Component> components;

    public static VariableString from(String raw, char start, char end) {
        List<Component> components = new ArrayList<>();

        boolean in = false;
        StringBuilder builder = new StringBuilder();
        for (char c : raw.toCharArray()) {
            if (in) {
                if (c == end) {
                    if (!builder.isEmpty()) {
                        components.add(new VariableComponent(builder.toString()));
                        builder = new StringBuilder();
                    }

                    in = false;
                    continue;
                }

                builder.append(c);
                continue;
            }

            if (c == start) {
                if (!builder.isEmpty()) {
                    components.add(new FixedComponent(builder.toString()));
                    builder = new StringBuilder();
                }

                in = true;
                continue;
            }

            builder.append(c);
        }

        if (in) {
            throw new IllegalArgumentException(raw + " contains an unclosed variable");
        }

        if (!builder.isEmpty()) {
            components.add(new FixedComponent(builder.toString()));
        }

        return new VariableString(components);
    }

    private VariableString(List<Component> components) {
        this.components = components;
    }

    public String build(Map<String, String> mapper) {
        StringBuilder builder = new StringBuilder();

        for (Component component : components) {
            if (component instanceof VariableComponent var) {
                String str = var.build(mapper);

                if (str == null) {
                    throw new IllegalStateException("Specified mapper does not contain value for " + var.variable());
                }

                builder.append(str);
                continue;
            }

            builder.append(component.toString());
        }

        return builder.toString();
    }
}
