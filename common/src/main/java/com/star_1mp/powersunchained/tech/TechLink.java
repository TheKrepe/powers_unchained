package com.star_1mp.powersunchained.tech;

import com.star_1mp.powersunchained.registry.PUBlocks;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Whether a Tech Table is linked to a Suit Creator (owner: the Customization tab only works then): the two blocks touch,
 * or a line of redstone components (dust — also up / down a block like dust climbs —, repeaters, comparators, torches,
 * redstone blocks) runs from one to the other. The line does not need to be powered.
 */
public final class TechLink {

    /** How many redstone blocks the search follows at most. */
    public static final int MAX_NODES = 512;
    public static final int MAX_DISTANCE = 64;

    private TechLink() {
    }

    public static boolean isLinked(Level level, BlockPos table) {
        if (touchesCreator(level, table)) {
            return true;
        }
        Set<BlockPos> seen = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        for (BlockPos n : neighbours(table)) {
            if (isRedstone(level.getBlockState(n)) && seen.add(n)) {
                queue.add(n);
            }
        }
        while (!queue.isEmpty() && seen.size() <= MAX_NODES) {
            BlockPos p = queue.poll();
            if (touchesCreator(level, p)) {
                return true;
            }
            for (BlockPos n : neighbours(p)) {
                if (n.distManhattan(table) <= MAX_DISTANCE && !seen.contains(n) && level.isLoaded(n)
                        && isRedstone(level.getBlockState(n))) {
                    seen.add(n);
                    queue.add(n);
                }
            }
        }
        return false;
    }

    private static boolean touchesCreator(Level level, BlockPos pos) {
        for (Direction d : Direction.values()) {
            if (level.getBlockState(pos.relative(d)).is(PUBlocks.SUIT_CREATOR.get())) {
                return true;
            }
        }
        return false;
    }

    /** The 6 faces plus the 8 up / down diagonals (dust running up and down a block). */
    private static Iterable<BlockPos> neighbours(BlockPos p) {
        java.util.List<BlockPos> list = new java.util.ArrayList<>(14);
        for (Direction d : Direction.values()) {
            list.add(p.relative(d));
        }
        for (Direction d : Direction.Plane.HORIZONTAL) {
            list.add(p.relative(d).above());
            list.add(p.relative(d).below());
        }
        return list;
    }

    public static boolean isRedstone(BlockState state) {
        return state.is(Blocks.REDSTONE_WIRE) || state.is(Blocks.REPEATER) || state.is(Blocks.COMPARATOR)
                || state.is(Blocks.REDSTONE_TORCH) || state.is(Blocks.REDSTONE_WALL_TORCH) || state.is(Blocks.REDSTONE_BLOCK);
    }
}
