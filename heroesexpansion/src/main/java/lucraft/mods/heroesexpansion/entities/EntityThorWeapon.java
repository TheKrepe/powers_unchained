package lucraft.mods.heroesexpansion.entities;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.items.ItemThorWeapon;
import lucraft.mods.heroesexpansion.superpowers.HESuperpowers;
import lucraft.mods.heroesexpansion.worldgen.WorldGenMjolnir;
import lucraft.mods.lucraftcore.infinity.EntityItemIndestructible;
import lucraft.mods.lucraftcore.karma.KarmaHandler;
import lucraft.mods.lucraftcore.karma.KarmaStat.KarmaClass;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import net.minecraft.block.Block;
import net.minecraft.block.BlockChest;
import net.minecraft.block.material.Material;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Random;
import java.util.UUID;

public class EntityThorWeapon extends EntityItemIndestructible {

    public Mode mode = Mode.NONE;

    public EntityThorWeapon(World worldIn) {
        super(worldIn);
    }

    public EntityThorWeapon(World worldIn, double x, double y, double z, ItemStack stack) {
        super(worldIn, x, y, z, stack);
    }

    public EntityThorWeapon(World worldIn, double x, double y, double z, ItemStack stack, float height, float width) {
        super(worldIn, x, y, z, stack, height, width);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        super.readEntityFromNBT(compound);
        this.mode = Mode.valueOf(compound.getString("Mode"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        super.writeEntityToNBT(compound);
        compound.setString("Mode", this.mode.toString());
    }

    public UUID getOwner() {
        if (this.getItem().isEmpty() || !this.getItem().hasTagCompound() || !this.getItem().getTagCompound().hasKey("Owner"))
            return null;

        return UUID.fromString(this.getItem().getTagCompound().getString("Owner"));
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        double d0 = this.motionX;
        double d1 = this.motionY;
        double d2 = this.motionZ;

        if (!this.hasNoGravity()) {
            this.motionY -= this.mode != Mode.FALLING ? 0.03999999910593033D : 0.03999999910593033D * 3;
        }

        if (this.world.isRemote) {
            this.noClip = false;
        } else {
            this.noClip = this.pushOutOfBlocks(this.posX, (this.getEntityBoundingBox().minY + this.getEntityBoundingBox().maxY) / 2.0D, this.posZ);
        }

        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        boolean flag = (int) this.prevPosX != (int) this.posX || (int) this.prevPosY != (int) this.posY || (int) this.prevPosZ != (int) this.posZ;

        if (flag || this.ticksExisted % 25 == 0) {
            if (this.world.getBlockState(new BlockPos(this)).getMaterial() == Material.LAVA) {
                this.motionY = 0.20000000298023224D;
                this.motionX = (double) ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
                this.motionZ = (double) ((this.rand.nextFloat() - this.rand.nextFloat()) * 0.2F);
                this.playSound(SoundEvents.ENTITY_GENERIC_BURN, 0.4F, 2.0F + this.rand.nextFloat() * 0.4F);
            }
        }

        float f = 0.98F;

        if (this.onGround) {
            BlockPos underPos = new BlockPos(MathHelper.floor(this.posX), MathHelper.floor(this.getEntityBoundingBox().minY) - 1, MathHelper.floor(this.posZ));
            net.minecraft.block.state.IBlockState underState = this.world.getBlockState(underPos);
            f = underState.getBlock().getSlipperiness(underState, this.world, underPos, this) * 0.98F;

            if (this.mode == Mode.FALLING) {
                this.setDead();
                Random random = new Random();
                this.world.newExplosion(null, this.posX, this.posY, this.posZ, 3F, false, false);
                new WorldGenMjolnir(1 + random.nextInt(2)).generate(world, random, new BlockPos(posX, posY, posZ));
                return;
            }
        }

        this.motionX *= (double) f;
        this.motionY *= 0.9800000190734863D;
        this.motionZ *= (double) f;

        if (this.onGround) {
            this.motionY *= -0.5D;
        }

        this.handleWaterMovement();

        if (!this.world.isRemote) {
            double d3 = this.motionX - d0;
            double d4 = this.motionY - d1;
            double d5 = this.motionZ - d2;
            double d6 = d3 * d3 + d4 * d4 + d5 * d5;

            if (d6 > 0.01D) {
                this.isAirBorne = true;
            }
        }

        if (mode == Mode.GRANT_POWER) {
            if (this.ticksExisted % 6000 == 0)
                this.getEntityWorld().addWeatherEffect(new EntityLightningBolt(world, posX, posY, posZ, true));

            if (this.ticksExisted % 100 == 0)
                PlayerHelper.spawnParticleForAll(world, 30, EnumParticleTypes.SPELL_INSTANT, true, (float) posX, (float) posY + height / 2.4F, (float) posZ, 0, 0, 0, 0.01F, 10);
        }
    }

    @Override
    public EnumActionResult applyPlayerInteraction(EntityPlayer player, Vec3d vec, EnumHand hand) {
        if (getEntityWorld().isRemote || this.isDead || this.mode == Mode.FALLING)
            return EnumActionResult.PASS;

        try {
            ItemThorWeapon.canLift(player, this.getItem());
        } catch (Exception e) {
            player.sendStatusMessage(new TextComponentTranslation(e.getMessage()), true);
            return EnumActionResult.FAIL;
        }

        NBTTagCompound nbt = getItem().hasTagCompound() ? getItem().getTagCompound() : new NBTTagCompound();

        if (mode == Mode.GRANT_POWER) {
            SuperpowerHandler.giveSuperpower(player, HESuperpowers.GOD_OF_THUNDER);
            player.sendStatusMessage(new TextComponentTranslation("heroesexpansion.info.get_god_of_thunder_power"), true);
            this.getEntityWorld().addWeatherEffect(new EntityLightningBolt(world, player.posX, player.posY, player.posZ, true));
            nbt.setString("Owner", player.getGameProfile().getId().toString());
            nbt.setString("OwnerName", player.getGameProfile().getName());
            getItem().setTagCompound(nbt);
        }

        if (nbt.hasKey("Owner")) {
            UUID uuid = UUID.fromString(nbt.getString("Owner"));
            if (!player.getGameProfile().getId().equals(uuid))
                player.sendStatusMessage(new TextComponentTranslation("heroesexpansion.info.hammer_belongs_to", nbt.getString("OwnerName")), true);
        }

        PlayerHelper.givePlayerItemStack(player, getItem());
        this.setDead();
        return EnumActionResult.SUCCESS;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }

    @EventBusSubscriber(modid = HeroesExpansion.MODID)
    public static class EventHandler {

        @SubscribeEvent
        public static void onInteract(PlayerInteractEvent.RightClickBlock e) {
            Block block = e.getWorld().getBlockState(e.getPos()).getBlock();
            if (block == Blocks.CHEST || block == Blocks.ENDER_CHEST || block == Blocks.TRAPPED_CHEST) {
                AxisAlignedBB box1 = block.getBoundingBox(e.getWorld().getBlockState(e.getPos()), e.getWorld(), e.getPos());
                AxisAlignedBB box = new AxisAlignedBB(e.getPos().getX() + box1.minX, e.getPos().getY() + box1.minY, e.getPos().getZ() + box1.minZ, e.getPos().getX() + box1.maxX, e.getPos().getY() + box1.maxY, e.getPos().getZ() + box1.maxZ);

                if (block instanceof BlockChest) {
                    for (EnumFacing facing : EnumFacing.VALUES) {
                        if (facing != EnumFacing.DOWN && facing != EnumFacing.UP && e.getWorld().getBlockState(e.getPos().add(facing.getDirectionVec())).getBlock() == block) {
                            box = box.expand(facing.getDirectionVec().getX(), facing.getDirectionVec().getY(), facing.getDirectionVec().getZ());
                        }
                    }
                }

                for (EntityThorWeapon entities : e.getWorld().getEntitiesWithinAABB(EntityThorWeapon.class, box.expand(0, 1, 0))) {
                    if (KarmaHandler.getKarma(e.getEntityPlayer()) < KarmaClass.SUPERHERO.getMinKarma()) {
                        e.setCanceled(true);
                    }
                }
            }
        }

    }

    public enum Mode {

        NONE, GRANT_POWER, FALLING

    }

}
