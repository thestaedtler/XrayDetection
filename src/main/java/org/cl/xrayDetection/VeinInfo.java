package org.cl.xrayDetection;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;

public record VeinInfo(int amount, boolean achievedMaxIterations, long time) {
    public interface Condition {
        static Condition material(Material material) {
            return block -> block.getType() == material;
        }

        static Condition excludeMetadata(String metadata) {
            return block -> !block.hasMetadata(metadata);
        }

        static Condition exclude(Set<Location> locations) {
            return block -> !locations.contains(block.getLocation());
        }

        boolean counts(Block block);
    }

    public static VeinInfo collect(Location start, int maxIterations, Consumer<Block> veinConsumer, Condition... conditions) {
        Set<Location> locations = new HashSet<>();
        Queue<Block> queue = new LinkedList<>();
        queue.add(start.getBlock());

        int i = 1;
        while (!queue.isEmpty() && i <= maxIterations) {
            Block block = queue.poll();
            Location location = block.getLocation();

            if (locations.contains(location)) {
                continue;
            }

            boolean passed = true;
            for (Condition countingCondition : conditions) {
                if (!countingCondition.counts(block)) {
                    passed = false;
                    break;
                }
            }

            if (!passed) {
                continue;
            }

            veinConsumer.accept(block);

            locations.add(location);
            for (BlockFace face : XrayDetection.RELEVANT_FACES) {
                queue.add(block.getRelative(face));
            }

            i++;
        }

        return new VeinInfo(locations.size(), i >= maxIterations, System.currentTimeMillis());
    }
}
