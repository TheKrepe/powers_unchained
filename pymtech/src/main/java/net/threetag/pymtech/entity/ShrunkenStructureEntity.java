package net.threetag.pymtech.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.threetag.pymtech.item.ShrunkenStructureItem;
import net.threetag.pymtech.storage.ShrunkenStructureData;
import net.threetag.threecore.capability.CapabilitySizeChanging;
import net.threetag.threecore.capability.ISizeChanging;
import net.threetag.threecore.entity.SolidItemEntity;

public class ShrunkenStructureEntity extends SolidItemEntity {

    public ShrunkenStructureEntity(EntityType<? extends ShrunkenStructureEntity> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    public ShrunkenStructureEntity(World worldIn, double x, double y, double z) {
        super(PTEntityTypes.SHRUNKEN_STRUCTURE, worldIn);
        this.setPosition(x, y, z);
        this.rotationYaw = this.rand.nextFloat() * 360.0F;
    }

    public ShrunkenStructureEntity(World worldIn, double x, double y, double z, ItemStack stack) {
        this(worldIn, x, y, z);
        this.setItem(stack);
    }

    @Override
    public ActionResultType applyPlayerInteraction(PlayerEntity player, Vector3d vec, Hand hand) {
        if (this.getCapability(CapabilitySizeChanging.SIZE_CHANGING).isPresent() && this.getCapability(CapabilitySizeChanging.SIZE_CHANGING).orElse(null).getScale() <= 0.1F) {
            if (player.isCrouching()) {
                ShrunkenStructureData data = ShrunkenStructureItem.get(this.world, this.getItem());
                if (data != null) {
                    data.rotateCounterclockwise();
                    return ActionResultType.SUCCESS;
                } else {
                    this.remove();
                    return ActionResultType.PASS;
                }
            } else {
                return super.applyPlayerInteraction(player, vec, hand);
            }
        }
        return ActionResultType.PASS;
    }

    @Override
    public void tick() {
        super.tick();
        this.getCapability(CapabilitySizeChanging.SIZE_CHANGING).ifPresent(ISizeChanging::updateBoundingBox);
        if (this.world != null && !this.world.isRemote)
            this.getCapability(CapabilitySizeChanging.SIZE_CHANGING).ifPresent(sizeChanging -> {
                if (!sizeChanging.isSizeChanging() && sizeChanging.getScale() == 1F && this.isAlive()) {
                    this.remove();
                    PlayerEntity player = this.getThrowerId() != null ? this.world.getPlayerByUuid(this.getThrowerId()) : null;
                    ShrunkenStructureData data = ShrunkenStructureItem.get(this.world, this.getItem());
                    if (data != null)
                        data.placeInWorld(this.world, this.getPosition(), player);
                }
            });
    }

}
