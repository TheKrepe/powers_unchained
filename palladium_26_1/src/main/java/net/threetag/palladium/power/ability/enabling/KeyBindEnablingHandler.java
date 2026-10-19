package net.threetag.palladium.power.ability.enabling;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.holder.IntValueHolder;
import net.threetag.palladium.network.AbilityKeyChangePacket;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.keybind.AbilityKeyBind;
import net.threetag.palladium.power.ability.keybind.KeyBindType;
import net.threetag.palladium.util.PalladiumCodecs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.IntFunction;

public class KeyBindEnablingHandler extends EnablingHandler {

    public static final MapCodec<KeyBindEnablingHandler> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            KeyBindType.CODEC.optionalFieldOf("key_bind", AbilityKeyBind.INSTANCE).forGetter(KeyBindEnablingHandler::getKeyBindType),
            Behaviour.CODEC.optionalFieldOf("behaviour", Behaviour.ACTIVATION).forGetter(KeyBindEnablingHandler::getBehaviour),
            PalladiumCodecs.TIME_DYNAMIC.optionalFieldOf("cooldown", IntValueHolder.ZERO).forGetter(h -> h.cooldown),
            PalladiumCodecs.TIME_DYNAMIC.optionalFieldOf("time", IntValueHolder.ZERO).forGetter(h -> h.activationTime)
    ).apply(instance, KeyBindEnablingHandler::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, KeyBindEnablingHandler> STREAM_CODEC = StreamCodec.composite(
            KeyBindType.STREAM_CODEC, KeyBindEnablingHandler::getKeyBindType,
            Behaviour.STREAM_CODEC, KeyBindEnablingHandler::getBehaviour,
            IntValueHolder.STREAM_CODEC, h -> h.cooldown,
            IntValueHolder.STREAM_CODEC, h -> h.activationTime,
            KeyBindEnablingHandler::new
    );

    private final KeyBindType keyBindType;
    private final Behaviour behaviour;
    private final IntValueHolder cooldown;
    private final IntValueHolder activationTime;

    public KeyBindEnablingHandler(KeyBindType keyBindType, Behaviour behaviour, IntValueHolder cooldown, IntValueHolder activationTime) {
        this.keyBindType = keyBindType;
        this.behaviour = behaviour;
        this.cooldown = cooldown;
        this.activationTime = activationTime;
    }

    private @Nullable DataContext createContext(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        return this.cooldown.isDynamic() || this.activationTime.isDynamic() ? DataContext.forAbility(entity, abilityInstance) : null;
    }

    public int getCooldown(@Nullable DataContext context) {
        return Math.max(0, this.cooldown.get(context));
    }

    public int getActivationTime(@Nullable DataContext context) {
        int time = Math.max(0, this.activationTime.get(context));
        return this.behaviour == Behaviour.ACTIVATION && time == 0 ? 1 : time;
    }

    public int getCooldown(AbilityInstance<?> abilityInstance, @Nullable DataContext context) {
        int stored = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.COOLDOWN_MAX.get(), 0);
        return stored > 0 ? stored : this.getCooldown(context);
    }

    public int getActivationTime(AbilityInstance<?> abilityInstance, @Nullable DataContext context) {
        int stored = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.ACTIVATION_TIME_MAX.get(), 0);
        return stored > 0 ? stored : this.getActivationTime(context);
    }

    private void startCooldown(AbilityInstance<?> abilityInstance, int cooldownTime) {
        abilityInstance.set(PalladiumDataComponents.Abilities.COOLDOWN_MAX.get(), cooldownTime);
        abilityInstance.set(PalladiumDataComponents.Abilities.COOLDOWN.get(), cooldownTime);
    }

    @Override
    public boolean check(LivingEntity entity, AbilityInstance<?> abilityInstance, @Nullable DataContext context) {
        if (this.behaviour == Behaviour.ACTIVATION) {
            return abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.ACTIVATION_TIME.get(), 0) > 0;
        }
        return abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.KEY_PRESSED.get(), false);
    }

    @Override
    public void tick(LivingEntity entity, AbilityInstance<?> abilityInstance, boolean enabled) {
        var context = this.createContext(entity, abilityInstance);
        int cooldownTime = this.getCooldown(context);
        int activationTime = this.getActivationTime(abilityInstance, context);
        boolean justSetCooldown = false;

        if (this.behaviour == Behaviour.ACTIVATION) {
            int activated = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.ACTIVATION_TIME.get(), 0);
            if (activated > 0) {
                if (activated == 1) {
                    abilityInstance.set(PalladiumDataComponents.Abilities.ACTIVATION_TIME.get(), 0);
                } else {
                    abilityInstance.setSilently(PalladiumDataComponents.Abilities.ACTIVATION_TIME.get(), activated - 1);
                }
            }
        } else if (this.behaviour == Behaviour.HELD && activationTime > 0) {
            if (abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.KEY_PRESSED.get(), false)) {
                int heldTicks = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.ACTIVATION_TIME.get(), 0);
                if (heldTicks >= activationTime) {
                    abilityInstance.set(PalladiumDataComponents.Abilities.KEY_PRESSED.get(), false);
                    abilityInstance.setSilently(PalladiumDataComponents.Abilities.ACTIVATION_TIME.get(), 0);
                    if (cooldownTime > 0) {
                        this.startCooldown(abilityInstance, cooldownTime);
                        justSetCooldown = true;
                    }
                } else {
                    abilityInstance.setSilently(PalladiumDataComponents.Abilities.ACTIVATION_TIME.get(), heldTicks + 1);
                }
            }
        } else if (this.behaviour == Behaviour.TOGGLE && activationTime > 0) {
            if (abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.KEY_PRESSED.get(), false)) {
                int timeLeft = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.ACTIVATION_TIME.get(), 0);
                if (timeLeft == 1) {
                    abilityInstance.set(PalladiumDataComponents.Abilities.KEY_PRESSED.get(), false);
                    abilityInstance.setSilently(PalladiumDataComponents.Abilities.ACTIVATION_TIME.get(), 0);
                    if (cooldownTime > 0) {
                        this.startCooldown(abilityInstance, cooldownTime);
                        justSetCooldown = true;
                    }
                } else if (timeLeft > 1) {
                    abilityInstance.setSilently(PalladiumDataComponents.Abilities.ACTIVATION_TIME.get(), timeLeft - 1);
                }
            }
        }

        if (!justSetCooldown) {
            int cooldown = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.COOLDOWN.get(), 0);
            if (cooldown > 0) {
                if (cooldown == 1) {
                    abilityInstance.set(PalladiumDataComponents.Abilities.COOLDOWN.get(), 0);
                } else {
                    abilityInstance.setSilently(PalladiumDataComponents.Abilities.COOLDOWN.get(), cooldown - 1);
                }
            }
        }
    }

    @Override
    public void registerDataComponents(DataComponentMap.Builder builder) {
        builder.set(PalladiumDataComponents.Abilities.KEY_PRESSED.get(), false);

        if (this.cooldown.isDynamic() || this.cooldown.getRaw() > 0) {
            builder.set(PalladiumDataComponents.Abilities.COOLDOWN.get(), 0);
            builder.set(PalladiumDataComponents.Abilities.COOLDOWN_MAX.get(), 0);
        }

        boolean mayHaveActivationTime = this.activationTime.isDynamic() || this.activationTime.getRaw() > 0;
        if (this.behaviour == Behaviour.ACTIVATION || (mayHaveActivationTime && (this.behaviour == Behaviour.HELD || this.behaviour == Behaviour.TOGGLE))) {
            builder.set(PalladiumDataComponents.Abilities.ACTIVATION_TIME.get(), 0);
            builder.set(PalladiumDataComponents.Abilities.ACTIVATION_TIME_MAX.get(), 0);
        }
    }

    @Override
    public void onUnlocked(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.KEY_PRESSED.get(), false)) {
            abilityInstance.set(PalladiumDataComponents.Abilities.KEY_PRESSED.get(), false);
        }
        if (this.getActivationTime(abilityInstance, this.createContext(entity, abilityInstance)) > 0) {
            abilityInstance.setSilently(PalladiumDataComponents.Abilities.ACTIVATION_TIME.get(), 0);
        }
    }

    @Override
    public boolean isKeyBound() {
        return true;
    }

    public KeyBindType getKeyBindType() {
        return this.keyBindType;
    }

    public Behaviour getBehaviour() {
        return this.behaviour;
    }

    public int getRemainingCooldown(AbilityInstance<?> abilityInstance) {
        return abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.COOLDOWN.get(), 0);
    }

    public boolean displayCooldown(AbilityInstance<?> abilityInstance) {
        return this.getRemainingCooldown(abilityInstance) > 0;
    }

    public float getCooldownPercentage(AbilityInstance<?> abilityInstance, @Nullable DataContext context) {
        int cooldownTime = this.getCooldown(abilityInstance, context);
        if (cooldownTime > 0) {
            return Math.min(1F, (float) this.getRemainingCooldown(abilityInstance) / cooldownTime);
        }
        return 0;
    }

    public float getActivationPercentage(AbilityInstance<?> abilityInstance, @Nullable DataContext context) {
        int activationTime = this.getActivationTime(abilityInstance, context);
        if (activationTime > 0 && (this.behaviour == Behaviour.ACTIVATION || this.behaviour == Behaviour.HELD || this.behaviour == Behaviour.TOGGLE)) {
            return Math.min(1F, (float) abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.ACTIVATION_TIME.get(), 0) / activationTime);
        }
        return 0;
    }

    public void onKeyPressed(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (entity.level().isClientSide()) {
            Palladium.PROXY.sendPacketToServer(new AbilityKeyChangePacket(abilityInstance.getReference(), true));
        } else {
            var context = this.createContext(entity, abilityInstance);
            int cooldownTime = this.getCooldown(context);
            int activationTime = this.getActivationTime(context);

            if (this.behaviour == Behaviour.TOGGLE) {
                boolean active = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.KEY_PRESSED.get(), false);
                if (active) {
                    abilityInstance.set(PalladiumDataComponents.Abilities.KEY_PRESSED.get(), false);
                    if (activationTime > 0) {
                        abilityInstance.setSilently(PalladiumDataComponents.Abilities.ACTIVATION_TIME.get(), 0);
                    }
                    if (cooldownTime > 0) {
                        this.startCooldown(abilityInstance, cooldownTime);
                    }
                } else if (this.getRemainingCooldown(abilityInstance) == 0) {
                    abilityInstance.set(PalladiumDataComponents.Abilities.KEY_PRESSED.get(), true);
                    if (activationTime > 0) {
                        abilityInstance.set(PalladiumDataComponents.Abilities.ACTIVATION_TIME_MAX.get(), activationTime);
                        abilityInstance.set(PalladiumDataComponents.Abilities.ACTIVATION_TIME.get(), activationTime);
                    }
                }
            } else if (this.behaviour == Behaviour.ACTIVATION) {
                if (this.getRemainingCooldown(abilityInstance) == 0 &&
                        abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.ACTIVATION_TIME.get(), 0) == 0) {
                    if (cooldownTime > 0) {
                        this.startCooldown(abilityInstance, cooldownTime);
                    }
                    abilityInstance.set(PalladiumDataComponents.Abilities.ACTIVATION_TIME_MAX.get(), activationTime);
                    abilityInstance.set(PalladiumDataComponents.Abilities.ACTIVATION_TIME.get(), activationTime);
                }
            } else if (this.behaviour == Behaviour.HELD) {
                if (this.getRemainingCooldown(abilityInstance) == 0) {
                    if (activationTime > 0) {
                        abilityInstance.set(PalladiumDataComponents.Abilities.ACTIVATION_TIME_MAX.get(), activationTime);
                    }
                    abilityInstance.set(PalladiumDataComponents.Abilities.KEY_PRESSED.get(), true);
                }
            } else {
                abilityInstance.set(PalladiumDataComponents.Abilities.KEY_PRESSED.get(), true);
            }
        }
    }

    public void onKeyReleased(LivingEntity entity, AbilityInstance<?> abilityInstance) {
        if (this.behaviour == Behaviour.HELD) {
            if (entity.level().isClientSide()) {
                ClientPacketDistributor.sendToServer(new AbilityKeyChangePacket(abilityInstance.getReference(), false));
            } else {
                var context = this.createContext(entity, abilityInstance);
                boolean wasPressed = abilityInstance.getOrDefault(PalladiumDataComponents.Abilities.KEY_PRESSED.get(), false);
                abilityInstance.set(PalladiumDataComponents.Abilities.KEY_PRESSED.get(), false);
                if (this.getActivationTime(abilityInstance, context) > 0) {
                    abilityInstance.setSilently(PalladiumDataComponents.Abilities.ACTIVATION_TIME.get(), 0);
                }

                // only start the cooldown if the ability was actually running, otherwise releasing the key
                // while on cooldown would restart it
                if (wasPressed) {
                    int cooldownTime = this.getCooldown(context);
                    if (cooldownTime > 0) {
                        this.startCooldown(abilityInstance, cooldownTime);
                    }
                }
            }
        }
    }

    @Override
    public MapCodec<? extends EnablingHandler> codec() {
        return CODEC;
    }

    public enum Behaviour implements StringRepresentable {

        ACTIVATION(0, "activation"),
        TOGGLE(1, "toggle"),
        HELD(2, "held");

        public static final IntFunction<Behaviour> BY_ID = ByIdMap.continuous(Behaviour::getId, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final Codec<Behaviour> CODEC = StringRepresentable.fromEnum(Behaviour::values);
        public static final StreamCodec<ByteBuf, Behaviour> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Behaviour::getId);

        private final int id;
        private final String name;

        Behaviour(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name;
        }
    }
}
