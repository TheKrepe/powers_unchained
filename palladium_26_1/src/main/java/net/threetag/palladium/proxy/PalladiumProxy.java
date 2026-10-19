package net.threetag.palladium.proxy;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.threetag.palladium.customization.Customization;
import net.threetag.palladium.customization.CustomizationCategory;
import net.threetag.palladium.entity.effect.EntityEffect;
import net.threetag.palladium.entity.flight.FlightType;
import net.threetag.palladium.logic.cost.CostDisplay;
import net.threetag.palladium.network.*;
import net.threetag.palladium.power.ability.AbilityInstance;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PalladiumProxy {

    public float getCurrentPartialTick() {
        return 1F;
    }

    public boolean hasCape(Entity entity) {
        return false;
    }

    public boolean isFirstPerson(Entity entity) {
        return false;
    }

    @Nullable
    public Player getClientPlayer() {
        return null;
    }

    /**
     * Registry access of the currently running game, for lookups without level context (e.g. ingredient tests).
     */
    @Nullable
    public RegistryAccess getRegistryAccess() {
        var server = ServerLifecycleHooks.getCurrentServer();
        return server != null ? server.registryAccess() : null;
    }

    public void sendPacketToServer(CustomPacketPayload payload, CustomPacketPayload... payloads) {

    }

    public void showCustomizationToast(Player player, Customization customization) {

    }

    public void spawnEffectEntity(Entity anchor, EntityEffect entityEffect) {

    }

    public void openScreen(Identifier screenId) {

    }

    public void openCustomizationScreen(@Nullable ResourceKey<CustomizationCategory> category) {

    }

    public void openCustomizationBuyScreen(Holder<Customization> customization, CostDisplay costDisplay,
                                           boolean available, RegistryAccess registryAccess) {

    }

    public void updateCustomizationGrid() {

    }

    public Collection<Identifier> getAvailableScreenIds() {
        return Collections.emptyList();
    }

    public boolean hasMovementInput(Entity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            var input = serverPlayer.getLastClientInput();
            return input.forward() || input.backward() || input.left() || input.right() || input.jump();
        }

        return false;
    }

    public Input getMovementInput(Entity entity) {
        if (entity instanceof ServerPlayer serverPlayer) {
            return serverPlayer.getLastClientInput();
        }

        return null;
    }

    public boolean playerHasSlimModel(Avatar player) {
        return false;
    }

    public void spawnEnergyBeamParticles(Level level, Vec3 pos, Identifier beamId) {

    }

    public void spawnParticleEmitter(LivingEntity entity, List<Identifier> particleEmitterIds, Holder<ParticleType<?>> particleTypeHolder, CompoundTag options) {

    }

    public void applyShader(LivingEntity entity, Identifier shader) {

    }

    public void removeShader(LivingEntity entity, Identifier shader) {

    }

    public void playAbilitySound(AbilityInstance<?> abilityInstance, LivingEntity entity, Identifier sound, float volume, float pitch, boolean playSelf) {

    }

    public void playGrappleSound(AbilityInstance<?> abilityInstance, LivingEntity entity, Identifier sound, float volume, float pitch) {

    }

    public void displayModeSwitch(LivingEntity entity, net.threetag.palladium.power.ability.ModeSwitchAbility.Mode mode) {

    }

    public void startFlightSound(LivingEntity entity, FlightType flightType) {

    }

    public void playOpenableAnimation(LivingEntity entity, Identifier animation) {

    }


    public void packetHandleOpenAbilityBuyScreen(OpenAbilityBuyScreenPacket packet, IPayloadContext context) {

    }

    public void packetHandleSyncAbilityComponent(SyncAbilityComponentPacket packet, IPayloadContext context) {

    }

    public void packetHandleSyncEntityCustomization(SyncEntityCustomizationPacket packet, IPayloadContext context) {

    }

    public void packetHandleSyncEntityCustomizations(SyncEntityCustomizationsPacket packet, IPayloadContext context) {

    }

    public void packetHandleSyncEntityUnselectCustomization(SyncEntityUnselectCustomizationPacket packet, IPayloadContext context) {

    }

    public void packetHandleSyncEntityPowers(SyncEntityPowersPacket packet, IPayloadContext context) {

    }

}
