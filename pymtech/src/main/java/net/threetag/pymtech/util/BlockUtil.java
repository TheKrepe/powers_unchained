package net.threetag.pymtech.util;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockUtil {

    public static int countBlocks(World world, AxisAlignedBB box) {
        int count = 0;

        for (double x = box.minX; x <= box.maxX; x++) {
            for (double y = box.minY; y <= box.maxY; y++) {
                for (double z = box.minZ; z <= box.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!world.isAirBlock(pos)) {
                        count++;
                    }
                }
            }
        }

        return count;
    }

    public static boolean isEmpty(World world, AxisAlignedBB box) {
        for (double x = box.minX; x <= box.maxX; x++) {
            for (double y = box.minY; y <= box.maxY; y++) {
                for (double z = box.minZ; z <= box.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    if (!world.isAirBlock(pos)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public static AxisAlignedBB eraseEmptyPlanes(World world, AxisAlignedBB box) {
        // Y
        while (isEmpty(world, new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.minY + 0.9D, box.maxZ))) {
            box = new AxisAlignedBB(box.minX, box.minY + 1, box.minZ, box.maxX, box.maxY, box.maxZ);
        }

        while (isEmpty(world, new AxisAlignedBB(box.minX, box.maxY, box.minZ, box.maxX, box.maxY - 0.9D, box.maxZ))) {
            box = new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY - 1, box.maxZ);
        }

        // X
        while (isEmpty(world, new AxisAlignedBB(box.minX, box.minY, box.minZ, box.minX + 0.9D, box.maxY, box.maxZ))) {
            box = new AxisAlignedBB(box.minX + 1, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ);
        }

        while (isEmpty(world, new AxisAlignedBB(box.maxX, box.minY, box.minZ, box.maxX - 0.9D, box.maxY, box.maxZ))) {
            box = new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX - 1, box.maxY, box.maxZ);
        }

        // Z
        while (isEmpty(world, new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ + 0.9D))) {
            box = new AxisAlignedBB(box.minX, box.minY, box.minZ + 1, box.maxX, box.maxY, box.maxZ);
        }

        while (isEmpty(world, new AxisAlignedBB(box.minX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ - 0.9D))) {
            box = new AxisAlignedBB(box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ - 1);
        }

        return box;
    }

}
