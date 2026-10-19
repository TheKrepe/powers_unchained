package lucraft.mods.heroesexpansion.worldgen;

import lucraft.mods.heroesexpansion.enchantments.HEEnchantments;
import lucraft.mods.heroesexpansion.entities.EntityThorWeapon;
import lucraft.mods.heroesexpansion.items.HEItems;
import lucraft.mods.lucraftcore.materials.Material;
import lucraft.mods.lucraftcore.materials.worldgen.WorldGeneratorMeteorite;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class WorldGenMjolnir extends WorldGeneratorMeteorite {

    public WorldGenMjolnir(int size) {
        super(size);
    }

    @Override
    public void generateMeteorBlocks(World world, Random random, BlockPos position) {
        EntityThorWeapon entity = new EntityThorWeapon(world);
        entity.mode = EntityThorWeapon.Mode.GRANT_POWER;
        int posY = position.getY();
        while (world.getBlockState(new BlockPos(position.getX(), posY, position.getZ())).isNormalCube())
            posY++;
        entity.setPosition(position.getX() + 0.5F, position.getY() + 2, position.getZ() + 0.5F);
        ItemStack mjolnir = new ItemStack(HEItems.MJOLNIR);
        mjolnir.addEnchantment(HEEnchantments.WORTHINESS, 1);
        entity.setItem(mjolnir);
        world.spawnEntity(entity);
        generateSphere(world, position.getX(), position.getY() - 1, position.getZ(), 3, Arrays.asList(Blocks.COBBLESTONE.getDefaultState(), Blocks.GRAVEL.getDefaultState()), random);
    }

    @Override
    public List<IBlockState> getBlocksForCrater(World world, Random random, BlockPos position) {
        return Arrays.asList(Blocks.NETHERRACK.getDefaultState(), Blocks.COBBLESTONE.getDefaultState(), Blocks.COBBLESTONE.getDefaultState(), Blocks.COBBLESTONE.getDefaultState(), Blocks.MAGMA.getDefaultState(), Material.URU.getBlock(Material.MaterialComponent.ORE));
    }
}
