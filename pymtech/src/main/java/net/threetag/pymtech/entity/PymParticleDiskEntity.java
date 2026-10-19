package net.threetag.pymtech.entity;

import com.google.common.collect.Lists;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.projectile.ProjectileItemEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ItemParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.Hand;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;
import net.threetag.pymtech.item.PymParticleDiskItem;
import net.threetag.pymtech.sizechangetype.PTSizeChangeTypes;
import net.threetag.threecore.capability.CapabilitySizeChanging;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Random;

public class PymParticleDiskEntity extends ProjectileItemEntity implements IEntityAdditionalSpawnData {

    public ItemStack stack;

    public PymParticleDiskEntity(EntityType<? extends PymParticleDiskEntity> entityType, World world) {
        super(entityType, world);
        this.stack = ItemStack.EMPTY;
    }

    public PymParticleDiskEntity(World world, LivingEntity thrower, ItemStack stack) {
        super(PTEntityTypes.PYM_PARTICLE_DISK, thrower, world);
        this.stack = stack;
    }

    public PymParticleDiskEntity(World world, ItemStack stack, double posX, double posY, double posZ) {
        super(PTEntityTypes.PYM_PARTICLE_DISK, posX, posY, posZ, world);
        this.stack = stack;
    }

    @Override
    protected Item getDefaultItem() {
        return this.stack.getItem();
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public ItemStack getItem() {
        return this.stack;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void handleStatusUpdate(byte type) {
        if (type == 3) {
            IParticleData particleData = new ItemParticleData(ParticleTypes.ITEM, this.stack);

            for (int i = 0; i < 8; ++i) {
                this.world.addParticle(particleData, this.getPosX(), this.getPosY(), this.getPosZ(), 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    protected void onImpact(RayTraceResult result) {
        if (!this.world.isRemote && this.isAlive()) {
            if (result.getType() == RayTraceResult.Type.ENTITY && this.stack.getItem() instanceof PymParticleDiskItem) {
                Entity entity = ((EntityRayTraceResult) result).getEntity();
                EquipmentSlotType slot = getRandomSlotWithItem(entity);

                if (slot != null) {
                    entity.entityDropItem(((LivingEntity) entity).getItemStackFromSlot(slot));
                    entity.setItemStackToSlot(slot, ItemStack.EMPTY);
                } else {
                    float diskSize = ((PymParticleDiskItem) this.stack.getItem()).size;
                    entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING).ifPresent(sizeChanging -> {
                        float newSize = (sizeChanging.getScale() < 1F && diskSize > 1F) || (sizeChanging.getScale() > 1F && diskSize < 1F) ? 1F : diskSize;
                        sizeChanging.startSizeChange(PTSizeChangeTypes.PYM_PARTICLES, newSize);
                    });
                }
            }
            this.world.setEntityState(this, (byte) 3);
            this.remove();
        }
    }

    public EquipmentSlotType getRandomSlotWithItem(Entity entity) {
        if(entity instanceof ArmorStandEntity) {
            return null;
        } else if (entity instanceof LivingEntity) {

            if(((LivingEntity) entity).isActiveItemStackBlocking()) {
                return ((LivingEntity) entity).getActiveHand() == Hand.MAIN_HAND ? EquipmentSlotType.MAINHAND : EquipmentSlotType.OFFHAND;
            }

            List<EquipmentSlotType> list = Lists.newArrayList();

            for (EquipmentSlotType slot : EquipmentSlotType.values()) {
                if (slot.getSlotType() == EquipmentSlotType.Group.ARMOR && !((LivingEntity) entity).getItemStackFromSlot(slot).isEmpty()) {
                    list.add(slot);
                }
            }

            return list.size() <= 0 ? null : list.get(new Random().nextInt(list.size()));
        }

        return null;
    }

    @Override
    public void writeSpawnData(PacketBuffer buffer) {
        buffer.writeItemStack(this.stack);
    }

    @Override
    public void readSpawnData(PacketBuffer additionalData) {
        this.stack = additionalData.readItemStack();
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRender3d(double x, double y, double z) {
        return true;
    }

    @Nonnull
    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

}
