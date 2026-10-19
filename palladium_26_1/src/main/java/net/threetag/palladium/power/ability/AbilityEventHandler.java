package net.threetag.palladium.power.ability;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.ICancellableEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ServerChatEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.attachment.PalladiumAttachments;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.logic.action.Action;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.PlayerUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@EventBusSubscriber(modid = Palladium.MOD_ID)
public class AbilityEventHandler {

    public static final List<String> CHECK_FOR_CHAT_MESSAGES = new ArrayList<>();

    @SubscribeEvent
    static void nameFormat(PlayerEvent.NameFormat e) {
        AbilityUtil.getEnabledInstances(e.getEntity(), AbilitySerializers.NAME_CHANGE.get())
                .stream()
                .filter(ab -> ab.get(PalladiumDataComponents.Abilities.NAME_CHANGE_CACHED.get()) != null)
                .findFirst()
                .ifPresent(ability -> {
                    e.setDisplayname(Objects.requireNonNull(ability.get(PalladiumDataComponents.Abilities.NAME_CHANGE_CACHED.get())));
                });
    }

    @SubscribeEvent
    static void onLivingVisibility(LivingEvent.LivingVisibilityEvent e) {
        e.modifyVisibility(OpacityChanging.getMobVisibilityMultiplier(e.getEntity(), 1F));
    }

    @SubscribeEvent
    static void chatReceived(ServerChatEvent e) {
        var msg = e.getMessage().getString().trim();

        if (CHECK_FOR_CHAT_MESSAGES.contains(msg.toLowerCase(Locale.ROOT))) {
            for (AbilityInstance<?> entry : AbilityUtil.getInstances(e.getPlayer())) {
                // TODO
//                for (Condition condition : entry.getAbility().getStateManager().getUnlockingHandler()) {
//                    if (condition instanceof ChatMessageCondition chat && chat.chatMessage.trim().equalsIgnoreCase(msg)) {
//                        chat.onChat(player, entry);
//                    }
//                }
//                for (Condition condition : entry.getAbility().getStateManager().getActivationHandler()) {
//                    if (condition instanceof ChatMessageCondition chat && chat.chatMessage.trim().equalsIgnoreCase(msg)) {
//                        chat.onChat(player, entry);
//                    }
//                }
            }
        }
    }

    @SubscribeEvent
    static void onHurt(LivingIncomingDamageEvent e) {
        // Fire Aspect Ability
        if (e.getSource().getEntity() instanceof LivingEntity sourceEntity && AbilityUtil.isTypeEnabled(sourceEntity, AbilitySerializers.FIRE_ASPECT.get())) {
            boolean hasAddedExistingFire = false;
            int fireSeconds = 0;
            for (AbilityInstance<FireAspectAbility> instance : AbilityUtil.getEnabledInstances(sourceEntity, AbilitySerializers.FIRE_ASPECT.get())) {
                int time = Math.max(instance.getAbility().time, 0);
                if (!hasAddedExistingFire && instance.getAbility().shouldStackTime) {
                    fireSeconds = Math.min(fireSeconds + (e.getEntity().getRemainingFireTicks() / 20), instance.getAbility().maxTime);
                    hasAddedExistingFire = true;
                }
                fireSeconds += time;
            }
            e.getEntity().setRemainingFireTicks(fireSeconds);
        }

        // Intangibility Ability
        if (e.getSource().is(DamageTypes.IN_WALL) && AbilityUtil.isTypeEnabled(e.getEntity(), AbilitySerializers.INTANGIBILITY.value())) {
            e.setAmount(0);
            e.setCanceled(true);
        }

        // Damage Immunity Ability
        for (AbilityInstance<DamageImmunityAbility> instance : AbilityUtil.getEnabledInstances(e.getEntity(), AbilitySerializers.DAMAGE_IMMUNITY.get())) {
            if (DamageImmunityAbility.isImmuneAgainst(instance, e.getSource())) {
                e.setAmount(0);
                e.setCanceled(true);
            }
        }

        // Guard Ability
        for (AbilityInstance<GuardAbility> instance : AbilityUtil.getEnabledInstances(e.getEntity(), AbilitySerializers.GUARD.get())) {
            if (!GuardAbility.canBlock(instance, e.getEntity(), e.getSource())) {
                continue;
            }

            if (!GuardAbility.absorb(instance, e.getAmount())) {
                instance.getAbility().playBlockSound(e.getEntity());
            }

            e.setAmount(0);
            e.setCanceled(true);

            if (e.getSource().getDirectEntity() instanceof LivingEntity attacker) {
                instance.getAbility().knockbackAttacker(e.getEntity(), attacker);
            }

            break;
        }

        // Combo Attack Ability
        if (e.getSource().getEntity() instanceof LivingEntity attacker) {
            var instance = ComboAttackAbility.getTopPriorityInstance(attacker);
            if (instance != null) {
                float bonus = ComboAttackAbility.consumeFinalBonus(instance, e.getEntity().getId());
                if (bonus > 0F) {
                    e.setAmount(e.getAmount() + bonus);
                }
            }
        }
    }

    @SubscribeEvent
    static void onAttackEntity(AttackEntityEvent e) {
        if (e.getTarget() instanceof LivingEntity target) {
            var instance = ComboAttackAbility.getTopPriorityInstance(e.getEntity());
            if (instance != null) {
                instance.getAbility().onSwing(e.getEntity(), instance, target);
            }
        }
    }

    @SubscribeEvent
    static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock e) {
        if (!e.getEntity().level().isClientSide()) {
            var instance = ComboAttackAbility.getTopPriorityInstance(e.getEntity());
            if (instance != null) {
                ComboAttackAbility.resetCombo(instance);
            }
        }
    }

    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        if (event.getSource().getEntity() instanceof LivingEntity source) {
            AbilityUtil.getEnabledInstances(source, AbilitySerializers.ACTION_ON_HIT.get()).forEach(instance -> {
                if (!instance.getAbility().directOnly || event.getSource().isDirect() && !instance.getAbility().action.isEmpty()) {
                    var targetContext = DataContext.forAbility(event.getEntity(), instance);
                    if (instance.getAbility().entityCondition.test(targetContext)) {
                        for (Action action : instance.getAbility().action) {
                            action.run(targetContext);
                        }
                    }
                }
            });
        }
    }

    @SubscribeEvent
    static void onAttackEntityGuard(AttackEntityEvent e) {
        if (GuardAbility.isBlockingActions(e.getEntity())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onRightClickItemGuard(PlayerInteractEvent.RightClickItem e) {
        blockIfGuarding(e);
    }

    @SubscribeEvent
    static void onRightClickBlockGuard(PlayerInteractEvent.RightClickBlock e) {
        blockIfGuarding(e);
    }

    @SubscribeEvent
    static void onEntityInteractGuard(PlayerInteractEvent.EntityInteract e) {
        blockIfGuarding(e);
    }

    @SubscribeEvent
    static void onEntityInteractSpecificGuard(PlayerInteractEvent.EntityInteractSpecific e) {
        blockIfGuarding(e);
    }

    private static <E extends PlayerInteractEvent & ICancellableEvent> void blockIfGuarding(E e) {
        if (GuardAbility.isBlockingActions(e.getEntity())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onTick(EntityTickEvent.Pre e) {
        if (e.getEntity() instanceof Player player && !player.level().isClientSide()) {
            var flying = PlayerUtil.isFlying(player);
            player.setData(PalladiumAttachments.IS_CLIMBING.get(),
                    !flying
                            && (!player.onGround() || player.isCrouching())
                            && AbilityUtil.isTypeEnabled(player, AbilitySerializers.WALL_CLIMBING.get())
                            && player.level().findSupportingBlock(player, player.getBoundingBox().inflate(0.2F, 0F, 0.2F)).isPresent()
                            && player.level().findSupportingBlock(player, player.getBoundingBox().move(0, -1, 0)).isEmpty()
            );
        }
    }

    @SubscribeEvent
    static void onEffect(MobEffectEvent.Applicable e) {
        for (AbilityInstance<PreventEffectsAbility> instance : AbilityUtil.getEnabledInstances(e.getEntity(), AbilitySerializers.PREVENT_EFFECTS.get())) {
            if (PreventEffectsAbility.isImmuneTo(e.getEntity(), instance, e.getEffectInstance().getEffect())) {
                e.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
                return;
            }
        }
    }

    @SubscribeEvent
    static void onHeal(LivingHealEvent e) {
        for (AbilityInstance<PreventHealingAbility> instance : AbilityUtil.getEnabledInstances(e.getEntity(), AbilitySerializers.PREVENT_HEALING.get())) {
            if (PreventHealingAbility.isFullPrevention(e.getEntity(), instance)) {
                e.setCanceled(true);
                return;
            }
        }
    }

    // Carry Ability

    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post e) {
        Player player = e.getEntity();

        if (player.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        LivingEntity target = getCarriedTarget(serverPlayer);

        if (target == null) {
            if (serverPlayer.getData(PalladiumAttachments.CARRIED_TARGET_ID) != -1) {
                releaseCarry(serverPlayer);
            }

            return;
        }

        double distance = serverPlayer.getData(PalladiumAttachments.CARRY_DISTANCE);
        double heightOffset = serverPlayer.getData(PalladiumAttachments.CARRY_HEIGHT_OFFSET);
        Vec3 holdPoint = serverPlayer.getEyePosition().add(serverPlayer.getLookAngle().scale(distance));

        target.setPos(holdPoint.x, holdPoint.y - target.getBbHeight() / 2.0 + heightOffset, holdPoint.z);
        target.setDeltaMovement(Vec3.ZERO);
        target.setYRot(serverPlayer.getYRot());
        target.setYHeadRot(serverPlayer.getYRot());
        target.hurtMarked = true;
    }

    @SubscribeEvent
    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock e) {
        if (throwCarried(e.getEntity())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onRightClickItem(PlayerInteractEvent.RightClickItem e) {
        if (throwCarried(e.getEntity())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty e) {
        throwCarried(e.getEntity());
    }

    @SubscribeEvent
    static void onEntityInteract(PlayerInteractEvent.EntityInteract e) {
        if (throwCarried(e.getEntity())) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific e) {
        if (throwCarried(e.getEntity())) {
            e.setCanceled(true);
        }
    }

    private static boolean throwCarried(Player player) {
        if (player.level().isClientSide() || !(player instanceof ServerPlayer serverPlayer) || !serverPlayer.getData(PalladiumAttachments.CARRY_THROWABLE)) {
            return false;
        }

        LivingEntity target = getCarriedTarget(serverPlayer);

        if (target == null) {
            return false;
        }

        double strength = serverPlayer.getData(PalladiumAttachments.CARRY_THROW_STRENGTH);

        if (target instanceof Mob mob) {
            mob.setNoAi(false);
        }

        target.setDeltaMovement(serverPlayer.getLookAngle().scale(strength));
        target.hurtMarked = true;
        serverPlayer.setData(PalladiumAttachments.CARRIED_TARGET_ID, -1);
        return true;
    }

    static void releaseCarry(ServerPlayer player) {
        LivingEntity target = getCarriedTarget(player);
        player.setData(PalladiumAttachments.CARRIED_TARGET_ID, -1);

        if (target instanceof Mob mob) {
            mob.setNoAi(false);
        }
    }

    private static LivingEntity getCarriedTarget(ServerPlayer player) {
        int carriedId = player.getData(PalladiumAttachments.CARRIED_TARGET_ID);

        if (carriedId == -1 || !(player.level() instanceof ServerLevel serverLevel)) {
            return null;
        }

        Entity target = serverLevel.getEntity(carriedId);
        return target instanceof LivingEntity living && living.isAlive() ? living : null;
    }

}
