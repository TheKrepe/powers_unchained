package lucraft.mods.heroesexpansion.blocks;

import lucraft.mods.heroesexpansion.HEConfig;
import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.lucraftcore.materials.Material;
import lucraft.mods.lucraftcore.materials.events.LCMaterialBlockTickEvent;
import net.minecraft.block.BlockBush;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BlockHeartShapedHerb extends BlockBush {

    public BlockHeartShapedHerb(String name) {
        super();
        this.setTranslationKey(name);
        this.setRegistryName(name);
        this.setCreativeTab(HeroesExpansion.CREATIVE_TAB);
        this.setHardness(0F);
        this.setSoundType(SoundType.PLANT);
    }

    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return super.getBoundingBox(state, source, pos).offset(state.getOffset(source, pos));
    }

    @Mod.EventBusSubscriber(modid = HeroesExpansion.MODID)
    public static class EventHandler {

        @SubscribeEvent
        public static void onBlockTick(LCMaterialBlockTickEvent e) {
            if (HEConfig.HERB_GROWING_ON_VIBRANIUM && e.getMaterial() == Material.VIBRANIUM && e.getWorld().getBlockState(e.getPos().up()).getBlock() == Blocks.GRASS && e.getWorld().isAirBlock(e.getPos().up(2)) && e.getRand().nextInt(5) == 0) {
                e.getWorld().setBlockState(e.getPos().up(2), HEBlocks.HEART_SHAPED_HERB.getDefaultState());
            }
        }

    }

}
