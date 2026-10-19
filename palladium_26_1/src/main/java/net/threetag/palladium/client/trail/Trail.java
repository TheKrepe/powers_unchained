package net.threetag.palladium.client.trail;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.client.renderer.power.PowerRenderer;
import net.threetag.palladium.client.renderer.power.PowerRendererManager;
import net.threetag.palladium.customization.CustomizationCategory;
import net.threetag.palladium.customization.EntityCustomizationHandler;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.EntityPowerHandler;
import net.threetag.palladium.power.PowerInstance;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import net.threetag.palladium.util.PalladiumCodecs;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record Trail(float spacing, int lifetime, boolean requiresMovement,
                    Optional<Integer> tickDelay, Optional<String> anchor, Vector3f offset, TrailRenderer<?> trailRenderer) {

    public static final Codec<Trail> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("spacing", 1F).forGetter(Trail::spacing),
            ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("lifetime", 20).forGetter(Trail::lifetime),
            Codec.BOOL.optionalFieldOf("requires_movement", true).forGetter(Trail::requiresMovement),
            PalladiumCodecs.TIME.optionalFieldOf("tick_delay").forGetter(Trail::tickDelay),
            Codec.STRING.optionalFieldOf("anchor").forGetter(Trail::anchor),
            PalladiumCodecs.VOXEL_VECTOR_3F.optionalFieldOf("offset", new Vector3f()).forGetter(Trail::offset),
            TrailRenderer.CODEC.fieldOf("renderer").forGetter(Trail::trailRenderer)
    ).apply(instance, Trail::new));

    public static List<Trail> getAvailableTrailsForEntity(Entity entity) {
        List<Trail> trails = new ArrayList<>();
        if (entity instanceof LivingEntity living
                && PalladiumEntityData.get(living, PalladiumEntityDataTypes.POWER_HANDLER.get()) instanceof EntityPowerHandler handler) {
            for (PowerInstance power : handler.getPowers()) {
                PowerRenderer renderer = PowerRendererManager.INSTANCE.get(power.getPowerId());

                if (renderer == null) {
                    continue;
                }

                var context = DataContext.forPower(living, power);
                renderer.trails().forEach((trailId, condition) -> {
                    if (condition.test(context)) {
                        var trailConfig = TrailManager.INSTANCE.get(trailId);

                        if (trailConfig != null) {
                            trails.addAll(trailConfig.trails());
                        }
                    }
                });
            }

            var context = DataContext.forEntity(living);
            var categories = living.registryAccess().lookupOrThrow(PalladiumRegistryKeys.CUSTOMIZATION_CATEGORY);
            var customizationHandler = EntityCustomizationHandler.get(living);

            for (CustomizationCategory category : categories) {
                if (!category.isVisible(context) || (category.hiddenByEquipment() != null && !living.getItemBySlot(category.hiddenByEquipment()).isEmpty())) {
                    continue;
                }

                var customization = customizationHandler.get(categories.wrapAsHolder(category));

                if (customization != null) {
                    for (var trailId : customization.value().getTrails()) {
                        var trailConfig = TrailManager.INSTANCE.get(trailId);

                        if (trailConfig != null) {
                            trails.addAll(trailConfig.trails());
                        }
                    }
                }
            }

            var sizeHandler = PalladiumEntityData.get(living, PalladiumEntityDataTypes.SIZE_CHANGE.get());

            if (sizeHandler != null) {
                for (var trailId : sizeHandler.getActiveTrailIds()) {
                    var trailConfig = TrailManager.INSTANCE.get(trailId);

                    if (trailConfig != null) {
                        trails.addAll(trailConfig.trails());
                    }
                }
            }
        }
        return trails;
    }

}
