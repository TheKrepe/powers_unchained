package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.entity.GrappleHook;
import net.threetag.palladium.power.energybar.EnergyBarUsage;
import net.threetag.palladium.util.PlayerUtil;

import java.util.List;
import java.util.Optional;

public class GrappleAbility extends Ability {

    private static final SettingType TYPE_MODE = SettingType.enumList(List.of("held", "one_shot"));

    public static final MapCodec<GrappleAbility> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    Codec.DOUBLE.fieldOf("range").forGetter(a -> a.range),
                    Codec.BOOL.optionalFieldOf("attach_to_entity", true).forGetter(a -> a.attachToEntity),
                    Codec.BOOL.optionalFieldOf("attach_to_block", true).forGetter(a -> a.attachToBlock),
                    Codec.DOUBLE.fieldOf("speed").forGetter(a -> a.speed),
                    Codec.DOUBLE.optionalFieldOf("hook_speed", 2.0).forGetter(a -> a.hookSpeed),
                    Mode.CODEC.optionalFieldOf("mode", Mode.HELD).forGetter(a -> a.mode),
                    Identifier.CODEC.fieldOf("beam_renderer").forGetter(a -> a.beamRenderer),
                    Identifier.CODEC.optionalFieldOf("shot_sound").forGetter(a -> a.shotSound),
                    Identifier.CODEC.optionalFieldOf("pulling_sound").forGetter(a -> a.pullingSound),
                    Identifier.CODEC.optionalFieldOf("detached_sound").forGetter(a -> a.detachedSound),
                    propertiesCodec(), stateCodec(), energyBarUsagesCodec()
            ).apply(instance, GrappleAbility::new));

    private final double range;
    private final boolean attachToEntity;
    private final boolean attachToBlock;
    private final double speed;
    private final double hookSpeed;
    private final Mode mode;
    public final Identifier beamRenderer;
    private final Optional<Identifier> shotSound;
    private final Optional<Identifier> pullingSound;
    private final Optional<Identifier> detachedSound;

    public GrappleAbility(double range, boolean attachToEntity, boolean attachToBlock, double speed, double hookSpeed, Mode mode, Identifier beamRenderer,
                          Optional<Identifier> shotSound, Optional<Identifier> pullingSound, Optional<Identifier> detachedSound,
                          AbilityProperties properties, AbilityStateManager stateManager, List<EnergyBarUsage> energyBarUsages) {
        super(properties, stateManager, energyBarUsages);
        this.range = range;
        this.attachToEntity = attachToEntity;
        this.attachToBlock = attachToBlock;
        this.speed = speed;
        this.hookSpeed = hookSpeed;
        this.mode = mode;
        this.beamRenderer = beamRenderer;
        this.shotSound = shotSound;
        this.pullingSound = pullingSound;
        this.detachedSound = detachedSound;
    }

    @Override
    public void registerDataComponents(DataComponentMap.Builder components) {
        super.registerDataComponents(components);
        components.set(PalladiumDataComponents.Abilities.GRAPPLE_HOOK_ID.get(), -1);
    }

    @Override
    public void firstTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (entity.level().isClientSide()) {
            this.pullingSound.ifPresent(sound -> Palladium.PROXY.playGrappleSound(abilityInstance, entity, sound, 1F, 1F));
            return;
        }

        // Don't fire again while a hook from this ability is still in the air or attached.
        if (hasActiveHook(entity, abilityInstance)) {
            return;
        }

        this.shotSound.ifPresent(sound -> PlayerUtil.playSoundToAll(entity.level(), entity.getX(), entity.getEyeY(), entity.getZ(), 100, sound, entity.getSoundSource(), 1F, 1F));

        GrappleHook hook = new GrappleHook(entity, entity.getLookAngle(), this.range, this.hookSpeed, this.speed,
                this.attachToEntity, this.attachToBlock, this.beamRenderer, this.detachedSound.orElse(null));
        entity.level().addFreshEntity(hook);

        abilityInstance.set(PalladiumDataComponents.Abilities.GRAPPLE_HOOK_ID.get(), hook.getId());
    }

    @Override
    public void lastTick(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (entity.level().isClientSide()) {
            return;
        }

        // Held mode: the grapple lives only as long as the ability is active, so end it on release.
        // One-shot mode: leave the hook running - it finishes its flight/reel on its own.
        if (this.mode == Mode.HELD) {
            if (getHook(entity, abilityInstance) instanceof GrappleHook hook) {
                hook.endGrapple();
            }

            abilityInstance.set(PalladiumDataComponents.Abilities.GRAPPLE_HOOK_ID.get(), -1);
        }
    }

    public static boolean isPulling(LivingEntity entity, AbilityInstance<?> instance) {
        return getHook(entity, instance) instanceof GrappleHook hook && hook.isAttached();
    }

    public static boolean hasActiveHook(LivingEntity entity, AbilityInstance<?> instance) {
        return getHook(entity, instance) instanceof GrappleHook;
    }

    private static Entity getHook(LivingEntity entity, AbilityInstance<?> instance) {
        int hookId = instance.getOrDefault(PalladiumDataComponents.Abilities.GRAPPLE_HOOK_ID.get(), -1);
        return hookId == -1 ? null : entity.level().getEntity(hookId);
    }

    @Override
    public AbilitySerializer<?> getSerializer() {
        return AbilitySerializers.GRAPPLE.get();
    }

    public enum Mode implements StringRepresentable {

        HELD("held"),
        ONE_SHOT("one_shot");

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

    public static class Serializer extends AbilitySerializer<GrappleAbility> {

        @Override
        public MapCodec<GrappleAbility> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Ability, GrappleAbility> builder, HolderLookup.Provider provider) {
            builder.setDescription("Fires a grappling hook that flies out to a range and latches on contact: reels a hit living entity toward the caster, or reels the caster toward a hit block.")
                    .add("range", TYPE_DOUBLE, "The maximum distance the grapple can reach.")
                    .addOptional("attach_to_entity", TYPE_BOOLEAN, "If true, the hook latches onto a living entity it flies into and reels it toward the caster.", true)
                    .addOptional("attach_to_block", TYPE_BOOLEAN, "If true, the hook latches onto a block it flies into and reels the caster toward it.", true)
                    .add("speed", TYPE_DOUBLE, "The velocity, in blocks/tick, that the pulled entity (or the caster) is reeled at.")
                    .addOptional("hook_speed", TYPE_DOUBLE, "The velocity, in blocks/tick, that the hook itself flies out at.", 2.0)
                    .addOptional("mode", TYPE_MODE, "\"held\" keeps the grapple active only while the ability is enabled (released on disable); \"one_shot\" fires once and runs to completion on its own.", "held")
                    .add("beam_renderer", TYPE_IDENTIFIER, "The id of the beam renderer used to draw the rope, e.g. one used by the swinging flight type or your own under assets/<namespace>/palladium/beams/.")
                    .addOptional("shot_sound", TYPE_IDENTIFIER, "Sound played once when the grapple is fired.")
                    .addOptional("pulling_sound", TYPE_IDENTIFIER, "Looping sound played while the grapple is actively reeling.")
                    .addOptional("detached_sound", TYPE_IDENTIFIER, "Sound played once when the grapple detaches (arrives, times out, or is released mid-pull).")
                    .addExampleObject(new GrappleAbility(
                            20.0, true, true, 1.2, 2.0, Mode.HELD,
                            Identifier.fromNamespaceAndPath("example", "beam_renderer_id"),
                            Optional.empty(), Optional.empty(), Optional.empty(),
                            AbilityProperties.BASIC, AbilityStateManager.EMPTY, List.of()));
        }
    }

}
