package net.threetag.palladium.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.NonNull;

public class PalladiumThrownTrident extends ThrownTrident implements ItemSupplier {

    private static final EntityDataAccessor<ItemStack> DATA_ITEM_STACK =
            SynchedEntityData.defineId(PalladiumThrownTrident.class, EntityDataSerializers.ITEM_STACK);

    public PalladiumThrownTrident(EntityType<? extends PalladiumThrownTrident> entityType, Level level) {
        super(entityType, level);
    }

    public PalladiumThrownTrident(Level level, double x, double y, double z, ItemStack stack) {
        this(PalladiumEntityTypes.THROWN_TRIDENT.get(), level);
        this.setPos(x, y, z);
        this.setPickupItemStack(stack);
        this.entityData.set(DATA_ITEM_STACK, stack.copy());
        this.pickup = AbstractArrow.Pickup.ALLOWED;
    }

    public PalladiumThrownTrident(Level level, LivingEntity owner, ItemStack stack) {
        this(level, owner.getX(), owner.getEyeY() - 0.1, owner.getZ(), stack);
        this.setOwner(owner);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ITEM_STACK, ItemStack.EMPTY);
    }

    @Override
    public @NonNull ItemStack getItem() {
        return this.entityData.get(DATA_ITEM_STACK);
    }

}
