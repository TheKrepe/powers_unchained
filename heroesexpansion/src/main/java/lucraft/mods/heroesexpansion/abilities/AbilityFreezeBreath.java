package lucraft.mods.heroesexpansion.abilities;

import com.google.common.collect.Maps;
import lucraft.mods.heroesexpansion.potions.PotionFrozen;
import lucraft.mods.heroesexpansion.sounds.HESoundEvents;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityHeld;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityData;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataFloat;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Map;
import java.util.Random;

public class AbilityFreezeBreath extends AbilityHeld {

    public static AbilityData<Float> DISTANCE = new AbilityDataFloat("distance").setSyncType(EnumSync.EVERYONE).enableSetting("distance", "Sets the maximum distance entities and blocks can be reached with");
    public static Map<IBlockState, IBlockState> REPLACED_BLOCKS = Maps.newHashMap();

    static {
        REPLACED_BLOCKS.put(Blocks.WATER.getDefaultState(), Blocks.ICE.getDefaultState());
        REPLACED_BLOCKS.put(Blocks.FLOWING_WATER.getDefaultState(), Blocks.ICE.getDefaultState());
        REPLACED_BLOCKS.put(Blocks.LAVA.getDefaultState(), Blocks.OBSIDIAN.getDefaultState());
        REPLACED_BLOCKS.put(Blocks.MAGMA.getDefaultState(), Blocks.NETHERRACK.getDefaultState());
        REPLACED_BLOCKS.put(Blocks.FIRE.getDefaultState(), Blocks.AIR.getDefaultState());

        REPLACED_BLOCKS.put(Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.OAK), Blocks.ICE.getDefaultState());
        REPLACED_BLOCKS.put(Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.SPRUCE), Blocks.ICE.getDefaultState());
        REPLACED_BLOCKS.put(Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.BIRCH), Blocks.ICE.getDefaultState());
        REPLACED_BLOCKS.put(Blocks.LEAVES.getDefaultState().withProperty(BlockOldLeaf.VARIANT, BlockPlanks.EnumType.JUNGLE), Blocks.ICE.getDefaultState());
        REPLACED_BLOCKS.put(Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.ACACIA), Blocks.ICE.getDefaultState());
        REPLACED_BLOCKS.put(Blocks.LEAVES2.getDefaultState().withProperty(BlockNewLeaf.VARIANT, BlockPlanks.EnumType.DARK_OAK), Blocks.ICE.getDefaultState());

        for (int i = 1; i <= 7; i++) {
            REPLACED_BLOCKS.put(Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, i), Blocks.SNOW_LAYER.getDefaultState().withProperty(BlockSnow.LAYERS, i + 1));
        }
    }

    public AbilityFreezeBreath(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(DISTANCE, 5F);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        float zLevel = Minecraft.getMinecraft().getRenderItem().zLevel;
        Minecraft.getMinecraft().getRenderItem().zLevel = -100.5F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(new ItemStack(Blocks.ICE), 0, 0);
        GlStateManager.popMatrix();
        Minecraft.getMinecraft().getRenderItem().zLevel = zLevel;
    }

    @Override
    public void updateTick() {
        RayTraceResult rtr = getPosLookingAt();

        if (rtr != null && !entity.world.isRemote) {

            if (entity.ticksExisted % 2 == 0) {
                Random rand = new Random();
                Vec3d endPos = rtr.hitVec.add(0, 0.5F, 0);
                Vec3d startPos = entity.getPositionVector().add(0, entity.getEyeHeight(), 0);
                double d = this.dataManager.get(DISTANCE);
                double brightness = rand.nextFloat();
                PlayerHelper.spawnParticleForAll(entity.world, 50, EnumParticleTypes.SNOW_SHOVEL.getParticleID(), startPos.x, startPos.y, startPos.z, (endPos.x - startPos.x) / d, (endPos.y - startPos.y) / d, (endPos.z - startPos.z) / d, (int) (brightness * 176), 134 + (int) (brightness * 96), 198 + (int) (brightness * 57));
            }

            if (rtr.entityHit != null && rtr.entityHit != entity) {
                if (rtr.entityHit instanceof EntityLivingBase)
                    PotionFrozen.freeze((EntityLivingBase) rtr.entityHit, 100);
            } else if (rtr.hitVec != null) {
                BlockPos pos = new BlockPos(rtr.hitVec);
                IBlockState state = entity.world.getBlockState(pos);
                IBlockState replace = REPLACED_BLOCKS.get(state);

                if (replace != null) {
                    entity.world.setBlockState(pos, replace);
                } else {
                    for (EnumFacing dir : EnumFacing.values()) {
                        if (entity.world.isAirBlock(pos.add(dir.getDirectionVec()))) {
                            BlockPos p = pos.add(dir.getDirectionVec());

                            if (!Blocks.SNOW_LAYER.canPlaceBlockAt(entity.world, p) || entity instanceof EntityPlayer && MinecraftForge.EVENT_BUS.post(new BlockEvent.PlaceEvent(new BlockSnapshot(entity.world, p, Blocks.FIRE.getDefaultState()), entity.world.getBlockState(pos),
                                    (EntityPlayer) entity, EnumHand.MAIN_HAND)))
                                return;

                            entity.world.setBlockState(pos.add(dir.getDirectionVec()), Blocks.SNOW_LAYER.getDefaultState());
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        if(isEnabled()) {
            PlayerHelper.playSoundToAll(this.entity.world, this.entity.posX, this.entity.posY + this.entity.getEyeHeight(), this.entity.posZ, 50, HESoundEvents.FREEZE_BREATH, SoundCategory.PLAYERS);
        }
    }

    public RayTraceResult getPosLookingAt() {
        Vec3d lookVec = entity.getLookVec();
        double distance = this.dataManager.get(DISTANCE);
        for (int i = 0; i < distance * 2; i++) {
            float scale = i / 2F;
            Vec3d pos = entity.getPositionVector().add(0, entity.getEyeHeight(), 0).add(lookVec.scale(scale));

            if (!entity.world.isAirBlock(new BlockPos(pos))) {
                return new RayTraceResult(pos, null);
            } else {
                Vec3d min = pos.add(0.25F, 0.25F, 0.25F);
                Vec3d max = pos.add(-0.25F, -0.25F, -0.25F);
                for (Entity entity : this.entity.world.getEntitiesWithinAABBExcludingEntity(this.entity, new AxisAlignedBB(min.x, min.y, min.z, max.x, max.y, max.z))) {
                    return new RayTraceResult(entity);
                }
            }
        }
        return new RayTraceResult(entity.getPositionVector().add(0, entity.getEyeHeight(), 0).add(lookVec.scale(distance)), null);
    }
}
