package net.threetag.pymtech.item;

import net.minecraft.block.DispenserBlock;
import net.minecraft.dispenser.IPosition;
import net.minecraft.dispenser.ProjectileDispenseBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.Stats;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.threetag.pymtech.entity.PymParticleDiskEntity;

public class PymParticleDiskItem extends Item {

    public final float size;

    public PymParticleDiskItem(Properties properties, float size) {
        super(properties);
        this.size = size;
        DispenserBlock.registerDispenseBehavior(this, new ProjectileDispenseBehavior() {
            protected ProjectileEntity getProjectileEntity(World worldIn, IPosition position, ItemStack stackIn) {
                return new PymParticleDiskEntity(worldIn, stackIn, position.getX(), position.getY(), position.getZ());
            }
        });
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
        ItemStack stack = player.getHeldItem(hand);

        player.getCooldownTracker().setCooldown(this, 10);

        world.playSound(null, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
        if (!world.isRemote) {
            PymParticleDiskEntity disk = new PymParticleDiskEntity(world, player, stack.split(1));
            disk.func_234612_a_(player, player.rotationPitch, player.rotationYaw, 0.0F, 1.5F, 1.0F);
            world.addEntity(disk);
        }

        player.addStat(Stats.ITEM_USED.get(this));
        return new ActionResult(ActionResultType.SUCCESS, stack);
    }
}
