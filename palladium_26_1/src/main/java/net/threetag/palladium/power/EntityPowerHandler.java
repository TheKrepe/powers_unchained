package net.threetag.palladium.power;

import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.logic.trigger.PalladiumCriteriaTriggers;
import net.threetag.palladium.network.SyncEntityPowersPacket;
import net.threetag.palladium.power.provider.PowerProvider;
import net.threetag.palladium.registry.PalladiumRegistries;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.*;
import java.util.stream.Collectors;

@EventBusSubscriber(modid = Palladium.MOD_ID)
public class EntityPowerHandler extends PalladiumEntityData<LivingEntity, EntityPowerHandler> {

    public static final Codec<EntityPowerHandler> CODEC = CompoundTag.CODEC.xmap(EntityPowerHandler::new, EntityPowerHandler::savePowerDataTag);

    private final Map<Identifier, PowerInstance> powers = new LinkedHashMap<>();
    private CompoundTag powerData;
    private boolean dirty = true;

    public EntityPowerHandler(CompoundTag powerData) {
        this.powerData = powerData;
    }

    public void markDirty() {
        this.dirty = true;
    }

    public Collection<PowerInstance> getPowers() {
        return this.powers.values().stream().sorted((o1, o2) -> o2.getPriority() - o1.getPriority()).collect(Collectors.toList());
    }

    @Override
    public void copyFrom(PalladiumEntityData<LivingEntity, EntityPowerHandler> source) {
        if (source instanceof EntityPowerHandler old) {
            this.powerData = old.savePowerDataTag().copy();
        }
    }

    @Override
    public Codec<EntityPowerHandler> codec() {
        return CODEC;
    }

    @Override
    public void tick() {
        if (!this.getEntity().level().isClientSide() && this.dirty) {
            this.dirty = false;
            Map<Identifier, PowerInstance> invalidPowers = new LinkedHashMap<>();
            PowerCollector collector = new PowerCollector(this.getEntity(), this, invalidPowers);

            // Find invalid
            for (PowerInstance instance : this.powers.values()) {
                if (instance.isInvalid()) {
                    invalidPowers.put(instance.getPowerId(), instance);
                }
            }

            // Get new ones
            for (PowerProvider provider : PalladiumRegistries.POWER_PROVIDER) {
                provider.providePowers(this.getEntity(), this, collector);
            }

            // Remove old ones
            for (PowerInstance instance : invalidPowers.values()) {
                this.removePowerInstance(instance.getPower());
            }

            // Add new ones
            List<PowerInstance> added = new ArrayList<>();
            for (PowerCollector.PowerInstanceCache instanceCache : collector.getAdded()) {
                var instance = instanceCache.make(this.getEntity(), this.powerData);
                this.addPowerInstance(instance);
                added.add(instance);
            }

            // Sync
            if (!invalidPowers.isEmpty() || !collector.getAdded().isEmpty()) {
                var msg = SyncEntityPowersPacket.create(this.getEntity(), new ArrayList<>(invalidPowers.values()), added);
                PacketDistributor.sendToPlayersTrackingEntityAndSelf(this.getEntity(), msg);
            }
        }

        // Tick
        for (PowerInstance instance : this.powers.values()) {
            instance.tick();
        }
    }

    public void addPowerInstance(PowerInstance instance) {
        if (!this.hasPower(instance.getPowerId())) {
            this.powers.put(instance.getPowerId(), instance);
            instance.firstTick();

            if (this.getEntity() instanceof ServerPlayer serverPlayer) {
                PalladiumCriteriaTriggers.POWER_GAINED.get().trigger(serverPlayer, instance.getPowerId());
            }
        }
    }

    public void removePowerInstance(Holder<Power> power) {
        var powerId = power.unwrapKey().orElseThrow().identifier();
        if (this.powers.containsKey(powerId)) {
            var instance = this.powers.get(powerId);
            boolean hasPersistentData = instance.getPower().value().hasPersistentData();
            instance.lastTick();

            if (this.getEntity() instanceof ServerPlayer serverPlayer) {
                PalladiumCriteriaTriggers.POWER_LOST.get().trigger(serverPlayer, instance.getPowerId());
            }

            if (hasPersistentData) {
                this.powerData.put(instance.getPowerId().toString(), instance.save());
            }

            this.powers.remove(powerId);

            if (!hasPersistentData) {
                this.powerData.remove(powerId.toString());
            }
        }
    }

    public PowerInstance getPowerInstance(Identifier powerId) {
        return this.powers.get(powerId);
    }

    public boolean hasPower(Identifier powerId) {
        return this.powers.containsKey(powerId);
    }

    public boolean hasPower(Holder<Power> powerHolder) {
        for (PowerInstance instance : this.powers.values()) {
            if (instance.getPower().value() == powerHolder.value()) {
                return true;
            }
        }

        return false;
    }

    public CompoundTag savePowerDataTag() {
        for (PowerInstance instance : this.powers.values()) {
            this.powerData.put(instance.getPowerId().toString(), instance.save());
        }
        this.cleanPowerData();
        return this.powerData;
    }

    private void lastTickAll() {
        for (PowerInstance instance : this.powers.values()) {
            instance.lastTick();
        }
    }

    @SubscribeEvent
    static void playerLoggedOut(PlayerEvent.PlayerLoggedOutEvent e) {
        var handler = PalladiumEntityData.get(e.getEntity(), PalladiumEntityDataTypes.POWER_HANDLER.get());
        if (handler != null) {
            handler.lastTickAll();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    static void playerCloneCleanup(PlayerEvent.Clone e) {
        var handler = PalladiumEntityData.get(e.getOriginal(), PalladiumEntityDataTypes.POWER_HANDLER.get());
        if (handler != null) {
            handler.lastTickAll();
        }
    }

    public void cleanPowerData() {
        List<String> toRemove = new ArrayList<>();
        for (String key : this.powerData.keySet()) {
            if (!this.getEntity().registryAccess().lookupOrThrow(PalladiumRegistryKeys.POWER).containsKey(Identifier.parse(key))) {
                toRemove.add(key);
            }
        }
        for (String key : toRemove) {
            this.powerData.remove(key);
        }
    }

}
