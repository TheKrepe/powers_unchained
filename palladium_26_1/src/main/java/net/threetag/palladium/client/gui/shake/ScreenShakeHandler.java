package net.threetag.palladium.client.gui.shake;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.entity.flight.EntityFlightHandler;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilityInstance;
import net.threetag.palladium.power.ability.AbilitySerializers;
import net.threetag.palladium.power.ability.AbilityUtil;
import net.threetag.palladium.power.ability.ScreenShakeAbility;

import java.util.*;

@EventBusSubscriber(modid = Palladium.MOD_ID, value = Dist.CLIENT)
public class ScreenShakeHandler {

    private static final List<ScreenShakeProvider> PROVIDERS = new ArrayList<>();
    private static final Map<Object, ActiveShake> ACTIVE = new IdentityHashMap<>();

    static {
        registerProvider(ScreenShakeProvider.timed(player -> {
            var handler = EntityFlightHandler.get(player);
            return handler.isFlying() ? handler.getFlightType().getScreenShake(player) : null;
        }));

        registerProvider((player, consumer) -> {
            for (AbilityInstance<ScreenShakeAbility> instance : AbilityUtil.getInstances(player, AbilitySerializers.SCREEN_SHAKE.get())) {
                if (instance.getAnimationTimerProgressEased(1F) > 0F) {
                    var ability = instance.getAbility();
                    float frequency = ability.frequency.get(DataContext.forAbility(player, instance));
                    consumer.accept(instance, ScreenShakeSource.of(ability.intensity, frequency, instance::getAnimationTimerProgressEased));
                }
            }
        });
    }

    public static void registerProvider(ScreenShakeProvider provider) {
        PROVIDERS.add(provider);
    }

    @SubscribeEvent
    static void tick(ClientTickEvent.Post e) {
        var player = Minecraft.getInstance().player;

        if (player == null) {
            ACTIVE.clear();
            PROVIDERS.forEach(ScreenShakeProvider::reset);
            return;
        }

        Set<Object> provided = Collections.newSetFromMap(new IdentityHashMap<>());

        for (ScreenShakeProvider provider : PROVIDERS) {
            provider.provide(player, (key, source) -> {
                provided.add(key);
                ACTIVE.computeIfAbsent(key, k -> new ActiveShake()).update(source);
            });
        }

        ACTIVE.keySet().retainAll(provided);
    }

    @SubscribeEvent
    static void camera(ViewportEvent.ComputeCameraAngles e) {
        float partialTick = (float) e.getPartialTick();

        for (ActiveShake shake : ACTIVE.values()) {
            float strength = shake.source.strength(partialTick);

            if (strength > 0F) {
                applyShake(e, shake.source.intensity(), shake.getCycles(partialTick), strength);
            }
        }
    }

    private static void applyShake(ViewportEvent.ComputeCameraAngles event, ScreenShake.ShakeIntensity intensity, double cycles, float strength) {
        double cycle = cycles * Math.PI * 2;

        event.setYaw(event.getYaw() + (float) (Math.sin(cycle) * intensity.yaw() * strength));
        event.setPitch(event.getPitch() + (float) (Math.sin(cycle + Math.PI / 2.5) * intensity.pitch() * strength));
        event.setRoll(event.getRoll() + (float) (Math.sin(cycle + Math.PI / 1.5) * intensity.roll() * strength));
    }

    private static class ActiveShake {

        ScreenShakeSource source;
        double cycles = 0D;

        void update(ScreenShakeSource source) {
            if (this.source != null) {
                this.cycles += this.source.frequency();
            }

            this.source = source;
        }

        double getCycles(float partialTick) {
            return this.cycles + this.source.frequency() * partialTick;
        }
    }
}
