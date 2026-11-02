package com.star_1mp.powersunchained.registry;

import com.star_1mp.powersunchained.Constants;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import org.jspecify.annotations.Nullable;

/** Damage types are data driven ({@code data/powers_unchained/damage_type/*.json}). */
public final class PUDamageTypes {

    public static final ResourceKey<DamageType> WEB = ResourceKey.create(Registries.DAMAGE_TYPE, Constants.id("web"));

    /** The Irradiator's radiation (and a Radioactive Spider dying of it). */
    public static final ResourceKey<DamageType> RADIATION = ResourceKey.create(Registries.DAMAGE_TYPE, Constants.id("radiation"));

    private PUDamageTypes() {
    }

    public static DamageSource web(Entity web, @Nullable Entity shooter) {
        return new DamageSource(web.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(WEB), web, shooter);
    }

    public static DamageSource radiation(net.minecraft.world.level.Level level) {
        return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(RADIATION));
    }
}
