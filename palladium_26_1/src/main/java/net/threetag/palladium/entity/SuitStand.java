package net.threetag.palladium.entity;

import net.minecraft.core.Rotations;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.threetag.palladium.item.PalladiumItems;

public class SuitStand extends ArmorStand {

    public static final Rotations DEFAULT_LEFT_ARM_POSE = new Rotations(0.0F, 0.0F, 0.0F);
    public static final Rotations DEFAULT_RIGHT_ARM_POSE = new Rotations(0.0F, 0.0F, 0.0F);
    public static final Rotations DEFAULT_LEFT_LEG_POSE = new Rotations(0.0F, 0.0F, 0.0F);
    public static final Rotations DEFAULT_RIGHT_LEG_POSE = new Rotations(0.0F, 0.0F, 0.0F);
    private static final EntityDataAccessor<Byte> DYE_COLOR = SynchedEntityData.defineId(SuitStand.class, EntityDataSerializers.BYTE);

    public SuitStand(EntityType<? extends SuitStand> entityType, Level level) {
        super(entityType, level);
        this.setLeftArmPose(DEFAULT_LEFT_ARM_POSE);
        this.setRightArmPose(DEFAULT_RIGHT_ARM_POSE);
        this.setLeftLegPose(DEFAULT_LEFT_LEG_POSE);
        this.setRightLegPose(DEFAULT_RIGHT_LEG_POSE);
        this.setShowArms(true);
    }

    public SuitStand(Level level, double x, double y, double z) {
        this(PalladiumEntityTypes.SUIT_STAND.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DYE_COLOR, (byte) 0);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setDyeColor(DyeColor.byId(input.getByteOr("color", (byte) 0)));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("color", (byte) this.getDyeColor().getId());
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        this.applyImplicitComponentIfPresent(components, DataComponents.SHEEP_COLOR);
        super.applyImplicitComponents(components);
    }

    @Override
    protected <T> boolean applyImplicitComponent(DataComponentType<T> type, T value) {
        if (type == DataComponents.SHEEP_COLOR) {
            this.setDyeColor(castComponentValue(DataComponents.SHEEP_COLOR, value));
            return true;
        } else {
            return super.applyImplicitComponent(type, value);
        }
    }

    public DyeColor getDyeColor() {
        return DyeColor.byId(this.entityData.get(DYE_COLOR) & 15);
    }

    public void setDyeColor(DyeColor color) {
        byte b0 = this.entityData.get(DYE_COLOR);
        this.entityData.set(DYE_COLOR, (byte) (b0 & 240 | color.getId() & 15));
    }

    @Override
    public ItemStack getPickResult() {
        return PalladiumItems.SUIT_STAND.get().getDefaultInstance();
    }
}
