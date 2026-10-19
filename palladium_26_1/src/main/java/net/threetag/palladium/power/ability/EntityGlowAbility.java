package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.condition.Condition;
import net.threetag.palladium.logic.condition.TrueCondition;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PalladiumCodecs;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.Optional;

public class EntityGlowAbility extends Ability {

    public static final MapCodec<EntityGlowAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Mode.CODEC.optionalFieldOf("mode", Mode.SELF).forGetter(ab -> ab.mode),
                    PalladiumCodecs.COLOR_CODEC.optionalFieldOf("color").forGetter(ab -> ab.color),
                    Codec.floatRange(0F, Float.MAX_VALUE).optionalFieldOf("distance", 0F).forGetter(ab -> ab.distance),
                    Condition.CODEC.optionalFieldOf("condition", TrueCondition.INSTANCE).forGetter(ab -> ab.condition),
                    propertiesCodec(), stateCodec(), energyBarUsagesCodec()
            ).apply(instance, EntityGlowAbility::new));

    public final Mode mode;
    public final Optional<Color> color;
    public final float distance;
    public final Condition condition;

    public EntityGlowAbility(Mode mode, Optional<Color> color, float distance, Condition condition, AbilityProperties properties, AbilityStateManager state, List<EnergyBarUsage> energyBarUsages) {
        super(properties, state, energyBarUsages);
        this.mode = mode;
        this.color = color;
        this.distance = distance;
        this.condition = condition;
    }

    @Override
    public AbilitySerializer<EntityGlowAbility> getSerializer() {
        return AbilitySerializers.ENTITY_GLOW.get();
    }

    private boolean withinDistance(Entity renderedEntity, Entity cameraEntity, boolean self) {
        if (this.distance <= 0F) {
            return true;
        }

        if (self && renderedEntity == cameraEntity) {
            return true;
        }

        return renderedEntity.distanceToSqr(cameraEntity) <= (double) this.distance * this.distance;
    }

    public static boolean shouldGlow(Entity renderedEntity, Entity cameraEntity) {
        if (renderedEntity instanceof LivingEntity living) {
            for (AbilityInstance<EntityGlowAbility> instance : AbilityUtil.getEnabledInstances(living, AbilitySerializers.ENTITY_GLOW.get())) {
                var ability = instance.getAbility();

                if (ability.mode == Mode.SELF && ability.condition.test(DataContext.forAbility(living, instance)) && ability.withinDistance(renderedEntity, cameraEntity, true)) {
                    return true;
                }
            }
        }

        if (renderedEntity != cameraEntity && cameraEntity instanceof LivingEntity living) {
            for (AbilityInstance<EntityGlowAbility> instance : AbilityUtil.getEnabledInstances(living, AbilitySerializers.ENTITY_GLOW.get())) {
                var ability = instance.getAbility();

                if (ability.mode == Mode.OTHERS && ability.condition.test(DataContext.forEntity(renderedEntity)) && ability.withinDistance(renderedEntity, cameraEntity, false)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Nullable
    public static Integer getGlowColor(Entity renderedEntity, Entity cameraEntity) {
        int red = 0, green = 0, blue = 0, count = 0;

        if (renderedEntity instanceof LivingEntity living) {
            for (AbilityInstance<EntityGlowAbility> instance : AbilityUtil.getEnabledInstances(living, AbilitySerializers.ENTITY_GLOW.get())) {
                var ability = instance.getAbility();

                if (ability.mode == Mode.SELF && ability.color.isPresent() && ability.condition.test(DataContext.forAbility(living, instance))) {
                    var color = ability.color.get();
                    red += color.getRed();
                    green += color.getGreen();
                    blue += color.getBlue();
                    count++;
                }
            }
        }

        if (cameraEntity instanceof LivingEntity living) {
            for (AbilityInstance<EntityGlowAbility> instance : AbilityUtil.getEnabledInstances(living, AbilitySerializers.ENTITY_GLOW.get())) {
                var ability = instance.getAbility();

                if (ability.mode == Mode.OTHERS && ability.color.isPresent() && ability.condition.test(DataContext.forEntity(renderedEntity))) {
                    var color = ability.color.get();
                    red += color.getRed();
                    green += color.getGreen();
                    blue += color.getBlue();
                    count++;
                }
            }
        }

        return count > 0 ? ((red / count) << 16) | ((green / count) << 8) | (blue / count) : null;
    }

    public enum Mode implements StringRepresentable {

        SELF("self"),
        OTHERS("others");

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public static class Serializer extends AbilitySerializer<EntityGlowAbility> {

        @Override
        public MapCodec<EntityGlowAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, EntityGlowAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Makes the entity itself glow, or lets you see other entities with their glow effect.")
                    .addOptional("mode", TYPE_STRING, "Determines if this ability makes the entity itself glow (\"self\"), or lets you see other entities with their glow effect (\"others\").", Mode.SELF.getSerializedName())
                    .addOptional("color", TYPE_COLOR, "Determines the color of the glow effect. If left empty, it will use the default team color.", null)
                    .addOptional("distance", TYPE_NON_NEGATIVE_FLOAT, "The distance at which the glow effect is visible. If 0, it will behave normally.", 0F)
                    .addOptional("condition", TYPE_CONDITION_LIST, "A condition, evaluated on the client, that must be met for the glow to appear.", "true")
                    .addExampleObject(new EntityGlowAbility(Mode.SELF, Optional.empty(), 0F, TrueCondition.INSTANCE, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }
}
