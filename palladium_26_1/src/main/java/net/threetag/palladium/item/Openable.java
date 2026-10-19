package net.threetag.palladium.item;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.network.PlayOpenableAnimationPacket;
import net.threetag.palladium.util.PlayerUtil;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

@EventBusSubscriber(modid = Palladium.MOD_ID)
public class Openable {

    public static final Identifier TOGGLE_HELMET_ANIMATION = Palladium.id("toggle_helmet");

    private static final Codec<Openable> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("enabled", true).forGetter(o -> o.enabled),
            Codec.INT.optionalFieldOf("time", 0).forGetter(o -> o.time),
            Identifier.CODEC.optionalFieldOf("opening_sound").forGetter(o -> Optional.ofNullable(o.openingSound)),
            Identifier.CODEC.optionalFieldOf("closing_sound").forGetter(o -> Optional.ofNullable(o.closingSound)),
            Animation.CODEC.optionalFieldOf("animation").forGetter(o -> Optional.ofNullable(o.animation))
    ).apply(instance, (enabled, time, openingSound, closingSound, animation) ->
            new Openable(enabled, time, openingSound.orElse(null), closingSound.orElse(null), animation.orElse(null))
    ));

    private static final Codec<Openable> SIMPLE_CODEC = Codec.either(Codec.BOOL, Codec.INT).xmap(
            either -> either.map(
                    enabled -> new Openable(enabled, 0, null, null, null),
                    time -> new Openable(true, time, null, null, null)
            ),
            openable -> openable.time > 0
                    ? Either.right(openable.time)
                    : Either.left(openable.enabled)
    );

    public static final Codec<Openable> CODEC = Codec.either(SIMPLE_CODEC, DIRECT_CODEC).xmap(either -> either.map(
            Function.identity(), Function.identity()
    ), openable -> openable.openingSound != null || openable.closingSound != null || openable.animation != null || (!openable.enabled && openable.time > 0) ? Either.right(openable) : Either.left(openable));

    public static final StreamCodec<FriendlyByteBuf, Openable> STREAM_CODEC = StreamCodec.of(
            (buf, openable) -> {
                buf.writeNullable(openable.openingSound, FriendlyByteBuf::writeIdentifier);
                buf.writeNullable(openable.closingSound, FriendlyByteBuf::writeIdentifier);
                buf.writeNullable(openable.animation, Animation::write);
                buf.writeInt(openable.time);
                buf.writeBoolean(openable.enabled);
            },
            buf -> {
                var openingSound = buf.readNullable(FriendlyByteBuf::readIdentifier);
                var closingSound = buf.readNullable(FriendlyByteBuf::readIdentifier);
                var animation = buf.readNullable(Animation::read);
                var time = buf.readInt();
                var enabled = buf.readBoolean();
                return new Openable(enabled, time, openingSound, closingSound, animation);
            }
    );

    private final boolean enabled;
    private final int time;
    @Nullable
    private final Identifier openingSound;
    @Nullable
    private final Identifier closingSound;
    @Nullable
    private final Animation animation;

    public Openable(boolean enabled, int time, @Nullable Identifier openingSound, @Nullable Identifier closingSound, @Nullable Animation animation) {
        this.enabled = enabled;
        this.time = time;
        this.openingSound = openingSound;
        this.closingSound = closingSound;
        this.animation = animation;
    }

    public void setOpened(LivingEntity livingEntity, ItemStack stack, boolean opened) {
        if (!this.enabled) {
            return;
        }

        var current = stack.getOrDefault(PalladiumDataComponents.Items.OPENED, false);
        if (opened != current) {
            stack.set(PalladiumDataComponents.Items.OPENED, opened);

            if (opened && this.openingSound != null) {
                PlayerUtil.playSoundToAll(livingEntity.level(), livingEntity.getX(), livingEntity.getEyeY(), livingEntity.getZ(), 100, this.openingSound, livingEntity.getSoundSource(), 1F, 1F);
            } else if (!opened && this.closingSound != null) {
                PlayerUtil.playSoundToAll(livingEntity.level(), livingEntity.getX(), livingEntity.getEyeY(), livingEntity.getZ(), 100, this.closingSound, livingEntity.getSoundSource(), 1F, 1F);
            }

            var animation = this.animation != null ? this.animation.get(opened) : null;

            if (animation != null && !livingEntity.level().isClientSide()) {
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(livingEntity, new PlayOpenableAnimationPacket(livingEntity.getId(), animation));
            }
        }
    }

    public boolean isOpened(ItemStack stack) {
        if (!this.enabled) {
            return false;
        }
        return stack.getOrDefault(PalladiumDataComponents.Items.OPENED, false);
    }

    public int getOpenTimer(ItemStack stack) {
        if (this.time > 0) {
            return stack.getOrDefault(PalladiumDataComponents.Items.OPEN_TIMER, 0);
        }
        return this.isOpened(stack) ? 1 : 0;
    }

    public void tick(ItemStack stack) {
        if (this.time <= 0) {
            return;
        }

        int timer = getOpenTimer(stack);
        boolean opened = this.isOpened(stack);

        if (opened && timer < this.time) {
            stack.set(PalladiumDataComponents.Items.OPEN_TIMER, Math.clamp(timer + 1, 0, this.time));
        } else if (!opened && timer > 0) {
            stack.set(PalladiumDataComponents.Items.OPEN_TIMER, Math.clamp(timer - 1, 0, this.time));
        }
    }

    @SubscribeEvent
    static void tick(EntityTickEvent.Post e) {
        if (e.getEntity() instanceof LivingEntity living) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                var stack = living.getItemBySlot(slot);

                if (!stack.isEmpty() && stack.has(PalladiumDataComponents.Items.OPENABLE)) {
                    Objects.requireNonNull(stack.get(PalladiumDataComponents.Items.OPENABLE)).tick(stack);
                }
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Openable openable = (Openable) o;
        return enabled == openable.enabled && time == openable.time && Objects.equals(openingSound, openable.openingSound) && Objects.equals(closingSound, openable.closingSound) && Objects.equals(animation, openable.animation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabled, time, openingSound, closingSound, animation);
    }

    public Openable withSounds(@Nullable Identifier openingSound, @Nullable Identifier closingSound) {
        return new Openable(this.enabled, this.time, openingSound, closingSound, this.animation);
    }

    public Openable withAnimation(@Nullable Animation animation) {
        return new Openable(this.enabled, this.time, this.openingSound, this.closingSound, animation);
    }

    public Openable withAnimation(Identifier animation) {
        return this.withAnimation(Animation.of(animation));
    }

    public Openable withAnimation(@Nullable Identifier openingAnimation, @Nullable Identifier closingAnimation) {
        return this.withAnimation(new Animation(openingAnimation, closingAnimation));
    }

    public static Openable simple() {
        return new Openable(true, 0, null, null, null);
    }

    public static Openable simple(Identifier openingSound, Identifier closingSound) {
        return new Openable(true, 0, openingSound, closingSound, null);
    }

    public static Openable simple(Animation animation) {
        return new Openable(true, 0, null, null, animation);
    }

    public static Openable simple(Identifier openingSound, Identifier closingSound, Animation animation) {
        return new Openable(true, 0, openingSound, closingSound, animation);
    }

    public static Openable timed(int time) {
        return new Openable(true, time, null, null, null);
    }

    public static Openable timed(int time, Identifier openingSound, Identifier closingSound) {
        return new Openable(true, time, openingSound, closingSound, null);
    }

    public static Openable timed(int time, Animation animation) {
        return new Openable(true, time, null, null, animation);
    }

    public static Openable timed(int time, Identifier openingSound, Identifier closingSound, Animation animation) {
        return new Openable(true, time, openingSound, closingSound, animation);
    }

    public record Animation(@Nullable Identifier opening, @Nullable Identifier closing) {

        public static final Animation TOGGLE_HELMET = of(TOGGLE_HELMET_ANIMATION);

        private static final Codec<Animation> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Identifier.CODEC.optionalFieldOf("opening").forGetter(a -> Optional.ofNullable(a.opening)),
                Identifier.CODEC.optionalFieldOf("closing").forGetter(a -> Optional.ofNullable(a.closing))
        ).apply(instance, (opening, closing) -> new Animation(opening.orElse(null), closing.orElse(null))));

        public static final Codec<Animation> CODEC = Codec.either(Identifier.CODEC, DIRECT_CODEC).xmap(
                either -> either.map(Animation::of, Function.identity()),
                animation -> animation.opening != null && animation.opening.equals(animation.closing) ? Either.left(animation.opening) : Either.right(animation)
        );

        public static Animation of(Identifier animation) {
            return new Animation(animation, animation);
        }

        @Nullable
        public Identifier get(boolean opened) {
            return opened ? this.opening : this.closing;
        }

        private static void write(FriendlyByteBuf buf, Animation animation) {
            buf.writeNullable(animation.opening, FriendlyByteBuf::writeIdentifier);
            buf.writeNullable(animation.closing, FriendlyByteBuf::writeIdentifier);
        }

        private static Animation read(FriendlyByteBuf buf) {
            return new Animation(buf.readNullable(FriendlyByteBuf::readIdentifier), buf.readNullable(FriendlyByteBuf::readIdentifier));
        }
    }
}
