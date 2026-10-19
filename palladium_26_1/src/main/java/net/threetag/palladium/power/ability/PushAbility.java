package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.power.energybar.EnergyBarUsage;

import java.util.List;

public class PushAbility extends Ability {

    private static final SettingType TYPE_MODE = SettingType.enumList(List.of("vertical", "horizontal"));

    public static final MapCodec<PushAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("range").forGetter(a -> a.range),
                    Mode.CODEC.fieldOf("mode").forGetter(a -> a.mode),
                    Codec.DOUBLE.fieldOf("amount").forGetter(a -> a.amount),
                    propertiesCodec(), stateCodec(), energyBarUsagesCodec()
            ).apply(instance, PushAbility::new));

    private final double range;
    private final Mode mode;
    private final double amount;

    public PushAbility(double range, Mode mode, double amount, AbilityProperties properties, AbilityStateManager stateManager, List<EnergyBarUsage> energyBarUsages) {
        super(properties, stateManager, energyBarUsages);
        this.range = range;
        this.mode = mode;
        this.amount = amount;
    }

    @Override
    public boolean tick(LivingEntity entity, AbilityInstance<?> abilityInstance, boolean enabled) {
        if (!enabled || entity.level().isClientSide()) {
            return false;
        }

        AABB searchBox = entity.getBoundingBox().inflate(this.range);
        var result = false;

        for (LivingEntity target : entity.level().getEntitiesOfClass(LivingEntity.class, searchBox, e -> e != entity && e.isAlive())) {
            Vec3 push = this.mode.getVector(entity, target, this.amount);
            target.setDeltaMovement(target.getDeltaMovement().add(push));
            target.hurtMarked = true;
            result = true;
        }

        return result;
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return AbilitySerializers.PUSH.get();
    }

    public enum Mode implements StringRepresentable {

        VERTICAL("vertical"),
        HORIZONTAL("horizontal");

        public static final Codec<Mode> CODEC = StringRepresentable.fromEnum(Mode::values);

        private final String name;

        Mode(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public Vec3 getVector(LivingEntity user, LivingEntity target, double amount) {
            if (this == VERTICAL) {
                return new Vec3(0.0, amount, 0.0);
            }

            Vec3 diff = target.position().subtract(user.position());
            Vec3 horizontal = new Vec3(diff.x, 0.0, diff.z);

            return horizontal.lengthSqr() > 1.0E-4 ? horizontal.normalize().scale(amount) : Vec3.ZERO;
        }
    }

    public static class Serializer extends AbilitySerializer<PushAbility> {

        @Override
        public MapCodec<PushAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, PushAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("When activated, applies a one-time velocity impulse to every living entity within range - either vertically (positive amount pushes up, negative pulls down) or horizontally, away from or toward the user's location (positive pushes away, negative pulls toward).")
                    .add("range", TYPE_DOUBLE, "How far from the entity other living entities can be to be affected.")
                    .add("mode", TYPE_MODE, "Whether to push vertically (up/down) or horizontally (away from/toward the user).")
                    .add("amount", TYPE_DOUBLE, "The velocity applied. For vertical: positive is up, negative is down. For horizontal: positive pushes targets away from the user, negative pulls them toward the user.")
                    .addExampleObject(new PushAbility(5.0, Mode.HORIZONTAL, 1.2, AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}
