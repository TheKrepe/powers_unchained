package net.threetag.palladium.util;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.entity.sizechange.EntitySizeChangeHandler;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;

public class EntityScaleUtil {

    private static EntityScaleUtil INSTANCE = new EntityScaleUtil();

    public static void setInstance(EntityScaleUtil entityScaleUtil) {
        INSTANCE = entityScaleUtil;
    }

    public static EntityScaleUtil getInstance() {
        return INSTANCE;
    }

    public boolean startScaleChange(Entity entity, Identifier scaleTypeId, float targetScale, int tickDelay) {
        if (!(entity instanceof LivingEntity living) || living.level().isClientSide()) {
            return false;
        }

        var type = living.registryAccess().lookup(PalladiumRegistryKeys.SIZE_CHANGE_TYPE)
                .flatMap(lookup -> lookup.get(ResourceKey.create(PalladiumRegistryKeys.SIZE_CHANGE_TYPE, scaleTypeId)))
                .orElse(null);
        var handler = getSizeChangeHandler(living);

        if (type == null || handler == null) {
            return false;
        }

        handler.applyModifier(type, targetScale, tickDelay);
        return true;
    }

    public float getWidthScale(Entity entity) {
        return this.getWidthScale(entity, currentPartialTick(entity));
    }

    public float getWidthScale(Entity entity, float delta) {
        return getScale(entity, delta);
    }

    public float getHeightScale(Entity entity) {
        return this.getHeightScale(entity, currentPartialTick(entity));
    }

    public float getHeightScale(Entity entity, float delta) {
        return getScale(entity, delta);
    }

    public float getModelWidthScale(Entity entity) {
        return this.getWidthScale(entity);
    }

    public float getModelWidthScale(Entity entity, float delta) {
        return this.getWidthScale(entity, delta);
    }

    public float getModelHeightScale(Entity entity) {
        return this.getHeightScale(entity);
    }

    public float getModelHeightScale(Entity entity, float delta) {
        return this.getHeightScale(entity, delta);
    }

    public float getEyeHeightScale(Entity entity) {
        return this.getHeightScale(entity);
    }

    public float getEyeHeightScale(Entity entity, float delta) {
        return this.getHeightScale(entity, delta);
    }

    public Collection<Identifier> getScaleTypeIds() {
        RegistryAccess registryAccess = Palladium.PROXY.getRegistryAccess();

        if (registryAccess == null) {
            return Collections.emptyList();
        }

        return registryAccess.lookup(PalladiumRegistryKeys.SIZE_CHANGE_TYPE)
                .map(lookup -> lookup.listElementIds().map(ResourceKey::identifier).toList())
                .orElseGet(Collections::emptyList);
    }

    private static float getScale(Entity entity, float delta) {
        if (!(entity instanceof LivingEntity living)) {
            return 1F;
        }

        float scale = living.getScale();
        var handler = getSizeChangeHandler(living);

        if (handler == null) {
            return scale;
        }

        float applied = handler.getSize(currentPartialTick(entity));
        return applied == 0F ? scale : scale / applied * handler.getSize(delta);
    }

    private static float currentPartialTick(Entity entity) {
        return entity.level().isClientSide() ? EntitySizeChangeHandler.RENDER_PARTIAL_TICK : 1F;
    }

    @Nullable
    private static EntitySizeChangeHandler getSizeChangeHandler(LivingEntity entity) {
        return PalladiumEntityData.get(entity, PalladiumEntityDataTypes.SIZE_CHANGE.get());
    }

}
