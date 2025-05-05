package org.cl.xrayDetection.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    private final Set<String> variables;
    private final List<Component> components;

    public static VariableString from(String raw, char start, char end) {
        Set<String> variables = new HashSet<>();
        List<Component> components = new ArrayList<>();

        boolean in = false;
        StringBuilder builder = new StringBuilder();
        for (char c : raw.toCharArray()) {
            if (in) {
                if (c == end) {
                    if (!builder.isEmpty()) {
                        String var = builder.toString();
                        variables.add(var);
                        components.add(new VariableComponent(var));
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

        return new VariableString(components, variables);
    }

    private VariableString(List<Component> components, Set<String> variables) {
        this.components = components;
        this.variables = variables;
    }

    public boolean ensureVariables(Collection<String> variables) {
        if (variables.size() != this.variables.size()) {
            return false;
        }

        for (String var : variables) {
            if (!this.variables.contains(var)) {
                return false;
            }
        }

        return true;
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
