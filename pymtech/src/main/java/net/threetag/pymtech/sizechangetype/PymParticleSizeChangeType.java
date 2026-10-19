package net.threetag.pymtech.sizechangetype;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fml.DistExecutor;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.config.PTServerConfig;
import net.threetag.pymtech.entity.DiscoTrailEntity;
import net.threetag.pymtech.entity.attributes.PTAttributes;
import net.threetag.pymtech.sound.PTSoundEvents;
import net.threetag.pymtech.util.PTDamageSources;
import net.threetag.threecore.capability.CapabilitySizeChanging;
import net.threetag.threecore.capability.ISizeChanging;
import net.threetag.threecore.entity.attributes.TCAttributes;
import net.threetag.threecore.potion.TCEffects;
import net.threetag.threecore.sizechanging.DefaultSizeChangeType;
import net.threetag.threecore.sizechanging.SizeChangeType;
import net.threetag.threecore.util.PlayerUtil;


public class PymParticleSizeChangeType extends DefaultSizeChangeType {

    @Override
    public int getSizeChangingTime(Entity entity, ISizeChanging iSizeChanging, float size) {
        return 10;
    }

    @Override
    public void onSizeChanged(Entity entity, ISizeChanging iSizeChanging, float size) {
        if (entity instanceof LivingEntity) {

            setAttribute((LivingEntity) entity, Attributes.MOVEMENT_SPEED, size > 1F ? 0 : (size - 1F) * 0.5D, size > 1F ? AttributeModifier.Operation.ADDITION : AttributeModifier.Operation.MULTIPLY_TOTAL, SizeChangeType.ATTRIBUTE_UUID);

            setAttribute((LivingEntity) entity, TCAttributes.SPRINT_SPEED.get(), size == 1F ? 0 : (size >= 1F ? -0.2F * size : (size - 1F) * 0.5D), size >= 1F ? AttributeModifier.Operation.ADDITION : AttributeModifier.Operation.MULTIPLY_TOTAL, SizeChangeType.ATTRIBUTE_UUID);
            if (((LivingEntity) entity).getAttribute(TCAttributes.SPRINT_SPEED.get()) != null && size == 1F) {
                ((LivingEntity) entity).getAttribute(TCAttributes.SPRINT_SPEED.get()).removeModifier(SizeChangeType.ATTRIBUTE_UUID);
            }

            setAttribute((LivingEntity) entity, TCAttributes.JUMP_HEIGHT.get(), size > 1F ? (size - 1F) * 1D : (size == 1F ? 0 : size), AttributeModifier.Operation.ADDITION, ATTRIBUTE_UUID);

            setAttribute((LivingEntity) entity, TCAttributes.FALL_RESISTANCE.get(), size > 1F ? 1F / size : size / 2F, AttributeModifier.Operation.MULTIPLY_TOTAL, ATTRIBUTE_UUID);

            setAttribute((LivingEntity) entity, Attributes.ATTACK_DAMAGE, size > 1F ? (size - 1F) * 3D : (size == 1F ? 0 : (1F - size) * 2), AttributeModifier.Operation.ADDITION, SizeChangeType.ATTRIBUTE_UUID);

            setAttribute((LivingEntity) entity, Attributes.ATTACK_SPEED, size > 1F ? size / 2F + 1F : 0, AttributeModifier.Operation.ADDITION, SizeChangeType.ATTRIBUTE_UUID);
            if (((LivingEntity) entity).getAttribute(Attributes.ATTACK_SPEED) != null && size <= 1F) {
                ((LivingEntity) entity).getAttribute(Attributes.ATTACK_SPEED).removeModifier(SizeChangeType.ATTRIBUTE_UUID);
            }

            setAttribute((LivingEntity) entity, ForgeMod.REACH_DISTANCE.get(), (size - 1F) * 1D, AttributeModifier.Operation.ADDITION, SizeChangeType.ATTRIBUTE_UUID);

            setAttribute((LivingEntity) entity, Attributes.KNOCKBACK_RESISTANCE, size < 0.5F ? (size - 1F) * 20D : (size - 1F) * 0.5D, AttributeModifier.Operation.ADDITION, SizeChangeType.ATTRIBUTE_UUID);

            if (!entity.world.isRemote && entity.ticksExisted % 2 == 0) {
                Entity discoTrail = new DiscoTrailEntity(entity.world, (LivingEntity) entity, 10);
                float scale = entity.getCapability(CapabilitySizeChanging.SIZE_CHANGING).orElse(new CapabilitySizeChanging(entity)).getScale();
                discoTrail.getCapability(CapabilitySizeChanging.SIZE_CHANGING).orElse(new CapabilitySizeChanging(discoTrail)).setSizeDirectly(SizeChangeType.DEFAULT_TYPE, scale);
                entity.world.addEntity(discoTrail);
            }

            this.changeCreeperExplosionRadius(entity, size);
            this.spawnWaterParticles(entity);

            if (size < 5F)
                DistExecutor.runWhenOn(Dist.CLIENT, () -> new Runnable() {
                    @Override
                    public void run() {
                        if (Minecraft.getInstance().player == entity) {
                            Minecraft.getInstance().gameSettings.smoothCamera = false;
                        }
                    }
                });
        }
    }

    @Override
    public void onUpdate(Entity entity, ISizeChanging data, float size) {
        if (size < 0.4F && entity.ticksExisted % 50 == 0) {
            entity.attackEntityFrom(PTDamageSources.PYM_PARTICLE_SUFFOCATE, 6F);
        }

        // Smooth Camera
        if (size >= 5F)
            DistExecutor.runWhenOn(Dist.CLIENT, () -> new Runnable() {
                @Override
                public void run() {
                    if (Minecraft.getInstance().player == entity) {
                        Minecraft.getInstance().gameSettings.smoothCamera = true;
                    }
                }
            });

        // Size resistance
        if (!(entity instanceof PlayerEntity) || !((PlayerEntity) entity).isCreative()) {
            String nbtKey = PymTech.MODID + ":size_resistance_timer";
            if (entity instanceof LivingEntity && !entity.world.isRemote) {
                if (size > 2F && PTServerConfig.PASSING_OUT_WHEN_GIANT.get()) {
                    int timer = (int) (entity.getPersistentData().getInt(nbtKey) + size);
                    entity.getPersistentData().putInt(nbtKey, timer);
                    int max = (int) (1000 + ((LivingEntity) entity).getAttribute(PTAttributes.SIZE_RESISTANCE.get()).getValue() * 10D);

                    if (PTServerConfig.HUNGER.get())
                        ((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.HUNGER, 55, 1, false, false));

                    if (timer >= max * 0.7 && entity.ticksExisted % 100 == 0) {
                        ((LivingEntity) entity).addPotionEffect(new EffectInstance(TCEffects.UNCONSCIOUS.get(), 25, 0, false, false));
                    }

                    if (timer >= max) {
                        ((LivingEntity) entity).addPotionEffect(new EffectInstance(TCEffects.UNCONSCIOUS.get(), 60, 0, false, false));

                        if (timer >= max + 500) {
                            data.startSizeChange(this, 1F);
                        }
                    }
                } else if (size < 0.5F && PTServerConfig.NAUSEA_WHEN_SMALL.get()) {
                    int timer = (int) (entity.getPersistentData().getInt(nbtKey) + 1F / size);
                    entity.getPersistentData().putInt(nbtKey, timer);
                    int max = (int) (1000 + ((LivingEntity) entity).getAttribute(PTAttributes.SIZE_RESISTANCE.get()).getValue() * 30D);

                    if (PTServerConfig.HUNGER.get())
                        ((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.HUNGER, 55, 1, false, false));

                    if (timer >= max * 0.7 && entity.ticksExisted % 300 == 0) {
                        ((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.NAUSEA, 200, 0, false, false));
                        ((LivingEntity) entity).addPotionEffect(new EffectInstance(Effects.HUNGER, 10, 0, false, false));
                    }
                } else if (entity.getPersistentData().getInt(nbtKey) > 0) {
                    entity.getPersistentData().putInt(nbtKey, 0);
                }
            } else if (entity.getPersistentData().getInt(nbtKey) > 0) {
                entity.getPersistentData().putInt(nbtKey, 0);
            }
        }
    }

    @Override
    public boolean start(Entity entity, ISizeChanging data, float size, float estimatedSize) {
        // TODO sound doesnt get quiet when distancing from entity
        SoundEvent soundEvent = estimatedSize < size ? (entity.isInWater() ? PTSoundEvents.SHRINK_UNDERWATER.get() : PTSoundEvents.SHRINK.get()) : (entity.isInWater() ? PTSoundEvents.ENLARGE_UNDERWATER.get() : PTSoundEvents.ENLARGE.get());
        PlayerUtil.playSoundToAll(entity.world, entity.getPosX(), entity.getPosY() + entity.size.height / 2D, entity.getPosZ(), 50, soundEvent, SoundCategory.PLAYERS);
        return true;
    }

    @Override
    public void end(Entity entity, ISizeChanging iSizeChanging, float size) {

    }
}
