package net.threetag.palladium.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.Weapon;
import org.jetbrains.annotations.Nullable;

public class PalladiumMaceItem extends MaceItem {

    public static final MapCodec<PalladiumMaceItem> MACE_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.FLOAT.optionalFieldOf("smash_fall_threshold", 1.5F).forGetter(PalladiumMaceItem::smashFallThreshold),
            Codec.FLOAT.optionalFieldOf("heavy_smash_threshold", 5.0F).forGetter(PalladiumMaceItem::heavySmashThreshold),
            Codec.FLOAT.optionalFieldOf("smash_damage_multiplier", 1.0F).forGetter(PalladiumMaceItem::smashDamageMultiplier),
            Codec.FLOAT.optionalFieldOf("smash_knockback_radius", 3.5F).forGetter(PalladiumMaceItem::smashKnockbackRadius),
            Codec.FLOAT.optionalFieldOf("smash_knockback_multiplier", 1.0F).forGetter(PalladiumMaceItem::smashKnockbackMultiplier),
            Codec.FLOAT.optionalFieldOf("heavy_knockback_amount", 2.0F).forGetter(PalladiumMaceItem::heavyKnockbackAmount),
            Codec.BOOL.optionalFieldOf("reset_fall_distance", true).forGetter(PalladiumMaceItem::resetFallDistance),
            Codec.BOOL.optionalFieldOf("allow_smash_while_fall_flying", false).forGetter(PalladiumMaceItem::allowSmashWhileFallFlying),
            Codec.INT.optionalFieldOf("density_level", 0).forGetter(PalladiumMaceItem::densityLevel),
            Codec.INT.optionalFieldOf("wind_burst_level", 0).forGetter(PalladiumMaceItem::windBurstLevel),
            ItemTypes.propertiesCodec()
    ).apply(instance, PalladiumMaceItem::new));

    private final float smashFallThreshold;
    private final float heavySmashThreshold;
    private final float smashDamageMultiplier;
    private final float smashKnockbackRadius;
    private final float smashKnockbackMultiplier;
    private final float heavyKnockbackAmount;
    private final boolean resetFallDistance;
    private final boolean allowSmashWhileFallFlying;
    private final int densityLevel;
    private final int windBurstLevel;

    public PalladiumMaceItem(float smashFallThreshold, float heavySmashThreshold, float smashDamageMultiplier,
                             float smashKnockbackRadius, float smashKnockbackMultiplier,
                             float heavyKnockbackAmount, boolean resetFallDistance,
                             boolean allowSmashWhileFallFlying, int densityLevel, int windBurstLevel,
                             Properties properties) {
        super(properties
                .rarity(Rarity.EPIC)
                .durability(500)
                .attributes(MaceItem.createAttributes())
                .component(DataComponents.TOOL, MaceItem.createToolProperties())
                .enchantable(15)
                .component(DataComponents.WEAPON, new Weapon(1)));
        this.smashFallThreshold = smashFallThreshold;
        this.heavySmashThreshold = heavySmashThreshold;
        this.smashDamageMultiplier = smashDamageMultiplier;
        this.smashKnockbackRadius = smashKnockbackRadius;
        this.smashKnockbackMultiplier = smashKnockbackMultiplier;
        this.heavyKnockbackAmount = heavyKnockbackAmount;
        this.resetFallDistance = resetFallDistance;
        this.allowSmashWhileFallFlying = allowSmashWhileFallFlying;
        this.densityLevel = densityLevel;
        this.windBurstLevel = windBurstLevel;
    }

    public float smashFallThreshold() {
        return this.smashFallThreshold;
    }

    public float heavySmashThreshold() {
        return this.heavySmashThreshold;
    }

    public float smashDamageMultiplier() {
        return this.smashDamageMultiplier;
    }

    public float smashKnockbackRadius() {
        return this.smashKnockbackRadius;
    }

    public float smashKnockbackMultiplier() {
        return this.smashKnockbackMultiplier;
    }

    public float heavyKnockbackAmount() {
        return this.heavyKnockbackAmount;
    }

    public boolean resetFallDistance() {
        return this.resetFallDistance;
    }

    public boolean allowSmashWhileFallFlying() {
        return this.allowSmashWhileFallFlying;
    }

    public int densityLevel() {
        return this.densityLevel;
    }

    public int windBurstLevel() {
        return this.windBurstLevel;
    }

    @Nullable
    public static PalladiumMaceItem of(@Nullable Entity entity) {
        return entity instanceof LivingEntity living ? of(living) : null;
    }

    @Nullable
    public static PalladiumMaceItem of(@Nullable LivingEntity entity) {
        return entity != null && entity.getWeaponItem().getItem() instanceof PalladiumMaceItem mace ? mace : null;
    }

    public static double smashThreshold(@Nullable PalladiumMaceItem mace, double fallback) {
        return mace == null ? fallback : mace.smashFallThreshold;
    }

    public static double heavyThreshold(@Nullable PalladiumMaceItem mace, double fallback) {
        return mace == null ? fallback : mace.heavySmashThreshold;
    }

    public static float damageMultiplier(@Nullable PalladiumMaceItem mace) {
        return mace == null ? 1.0F : mace.smashDamageMultiplier;
    }

    public static float densityBonus(@Nullable PalladiumMaceItem mace, DamageSource source) {
        if (mace == null || mace.densityLevel <= 0 || !(source.getEntity() instanceof LivingEntity attacker)) {
            return 0.0F;
        }
        return (float) (attacker.fallDistance * mace.densityLevel);
    }

    public static boolean overridesDensity(@Nullable PalladiumMaceItem mace) {
        return mace != null && mace.densityLevel > 0;
    }

    public static boolean overridesWindBurst(@Nullable ItemStack stack) {
        return stack != null && stack.getItem() instanceof PalladiumMaceItem mace && mace.windBurstLevel > 0;
    }

    public static double knockbackRadius(@Nullable PalladiumMaceItem mace, double fallback) {
        return mace == null ? fallback : mace.smashKnockbackRadius;
    }

    public static double knockbackFactor(@Nullable PalladiumMaceItem mace, double fallback) {
        return mace == null ? fallback : fallback * mace.smashKnockbackMultiplier;
    }

    public static int heavyKnockbackFactor(@Nullable PalladiumMaceItem mace, int fallback) {
        return mace == null ? fallback : Math.max(0, Math.round(mace.heavyKnockbackAmount));
    }

    public static boolean resetFallDistanceEnabled(@Nullable PalladiumMaceItem mace) {
        return mace == null || mace.resetFallDistance;
    }

    public static boolean allowFallFlyingSmash(@Nullable PalladiumMaceItem mace) {
        return mace != null && mace.allowSmashWhileFallFlying;
    }
}
