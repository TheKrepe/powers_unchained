package com.star_1mp.powersunleashed.power;

import com.star_1mp.powersunleashed.Constants;
import com.star_1mp.powersunleashed.platform.Services;
import com.star_1mp.powersunleashed.power.skill.SkillNode;
import com.star_1mp.powersunleashed.power.skill.SkillTrees;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

/**
 * Skill trees in play: experience for using a power, buying nodes, and what the nodes do. Spider-Man's effects:
 * attributes here (strength, health, knockback, armour, jump, speed, attack speed), damage rules from the loader's
 * damage event ({@link #incomingDamage}), soft landing ({@link Superpowers#fallImmune}), and the client-side gates
 * (roll, flips, ceilings, crawl speed, Spider-Sense) via {@link #has}.
 */
public final class PowerSkills {

    /** Experience per action. */
    public static final int XP_CLIMB = 1;       // owner: VERY slow — 1 xp per 30 seconds spent on walls / ceilings
    public static final int CLIMB_TICKS_PER_XP = 600;
    /** Ticks spent climbing since the last climb xp (kept across climbs, so short climbs add up). */
    private static final java.util.Map<java.util.UUID, Integer> CLIMB_TICKS = new java.util.HashMap<>();
    public static final int XP_ROLL = 2;
    public static final int XP_WEB_SHOT = 1;    // owner: web-shooters train the SPIDER power (only it)
    public static final int XP_WEB_ZIP = 2;
    public static final int XP_WEB_SWING = 2;
    public static final int XP_SENSE = 2;       // a Spider-Sense alert, at most every 5 seconds
    public static final int XP_KILL = 4;        // a monster killed

    private static final Map<UUID, Long> LAST_SENSE = new HashMap<>();
    /**
     * Optimisation: experience is stored on the server at once but sent to the player at most once a second (every web
     * shot used to send the whole progress). Weak keys: a player entity that is gone drops out by itself.
     */
    private static final Map<ServerPlayer, Boolean> UNSYNCED = new java.util.WeakHashMap<>();
    /**
     * Optimisation: the skill attribute modifiers are put on once per player ENTITY (login, respawn = a new entity) and
     * again only when the skills / powers change ({@link #refreshAttributes}), instead of every second for everyone.
     */
    private static final Map<ServerPlayer, Boolean> ATTRIBUTES_APPLIED = new java.util.WeakHashMap<>();

    private PowerSkills() {
    }

    public static PowerProgress progress(Player player) {
        return Services.GAMEPLAY.powerProgress(player);
    }

    /** The player owns the power AND bought the node. */
    public static boolean has(Player player, Superpower power, String node) {
        return Superpowers.data(player).has(power) && progress(player).has(power, node);
    }

    public static boolean spider(Player player, String node) {
        return has(player, Superpower.SPIDERMAN, node);
    }

    /** Abilities a node must unlock first (the rest come with the power). */
    public static boolean unlocks(Player player, PowerAbility ability) {
        return switch (ability) {
            case ROLL -> spider(player, SkillTrees.ROLL);
            case NIGHT_VISION -> spider(player, SkillTrees.NIGHT_VISION);
            default -> true;
        };
    }

    // ---- experience ----

    public static void addXp(ServerPlayer player, Superpower power, int amount) {
        if (amount <= 0 || !Superpowers.data(player).has(power)) {
            return;
        }
        PowerProgress progress = progress(player);
        int max = 0;
        for (int l = 0; l < PowerLevels.maxLevel(power); l++) {
            max += PowerLevels.xpForNext(l);
        }
        int xp = progress.get(power).xp();
        if (xp >= max) {
            return;
        }
        Services.GAMEPLAY.setPowerProgressQuiet(player, progress.withXp(power, Math.min(max, xp + amount)));
        UNSYNCED.put(player, Boolean.TRUE);
    }

    /** Sends experience gained since the last sync (also before anything that sends the whole progress anyway). */
    public static void flushXp(ServerPlayer player) {
        if (UNSYNCED.remove(player) != null) {
            Services.GAMEPLAY.syncPowerProgress(player);
        }
    }

    /** The skills or powers changed: put the attribute modifiers on again (next tick). */
    public static void refreshAttributes(ServerPlayer player) {
        ATTRIBUTES_APPLIED.remove(player);
    }

    /** Spider-Man experience (also from the web-shooters, owner). */
    public static void spiderXp(ServerPlayer player, int amount) {
        addXp(player, Superpower.SPIDERMAN, amount);
    }

    public static void onSenseAlert(ServerPlayer player) {
        long now = player.level().getGameTime();
        Long last = LAST_SENSE.get(player.getUUID());
        if (last == null || now - last >= 100) {
            LAST_SENSE.put(player.getUUID(), now);
            spiderXp(player, XP_SENSE);
        }
    }

    public static void onKill(ServerPlayer player, LivingEntity victim) {
        if (victim instanceof Enemy) {
            for (Superpower power : Superpower.values()) {
                addXp(player, power, XP_KILL);
            }
        }
    }

    // ---- buying ----

    /** @return whether the node was bought */
    public static boolean buy(ServerPlayer player, Superpower power, String id) {
        SkillNode node = SkillTrees.node(power, id);
        if (node == null || !Superpowers.data(player).has(power)) {
            return false;
        }
        PowerProgress progress = progress(player);
        PowerProgress.Entry entry = progress.get(power);
        if (entry.nodes().contains(id) || PowerLevels.points(power, entry) < node.cost()) {
            return false;
        }
        for (String req : node.requires()) {
            if (!entry.nodes().contains(req)) {
                return false;
            }
        }
        UNSYNCED.remove(player);  // the full progress (with the new xp) is sent right now
        Services.GAMEPLAY.setPowerProgress(player, progress.withNode(power, id, true));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.6F, 1.5F);
        applyAttributes(player);
        return true;
    }

    /** Server, every player tick. */
    public static void serverTick(ServerPlayer player) {
        if (ATTRIBUTES_APPLIED.put(player, Boolean.TRUE) == null) {
            applyAttributes(player);
        }
        if (player.tickCount % 20 == 0) {
            flushXp(player);
        }
        if (WallClimbing.isClimbing(player)) {
            int t = CLIMB_TICKS.merge(player.getUUID(), 1, Integer::sum);
            if (t >= CLIMB_TICKS_PER_XP) {
                CLIMB_TICKS.put(player.getUUID(), 0);
                spiderXp(player, XP_CLIMB);
            }
        }
        if (player.tickCount % 80 == 0 && spider(player, SkillTrees.REGENERATION) && player.getHealth() < player.getMaxHealth()
                && player.tickCount - player.getLastHurtByMobTimestamp() > 100) {
            player.heal(1.0F);
        }
    }

    // ---- effects ----

    private static void modifier(ServerPlayer player, Holder<Attribute> attribute, String name, double amount,
            AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        Identifier id = Constants.id("skill/" + name);
        if (amount == 0.0) {
            instance.removeModifier(id);
        } else {
            AttributeModifier old = instance.getModifier(id);
            if (old == null || old.amount() != amount) {
                instance.addOrUpdateTransientModifier(new AttributeModifier(id, amount, operation));
            }
        }
    }

    /** Puts the attribute nodes' modifiers on the player (and takes them off without the power / node). */
    public static void applyAttributes(ServerPlayer player) {
        double attack = (spider(player, SkillTrees.STRENGTH) ? 1.0 : 0.0) + (spider(player, SkillTrees.STRENGTH_2) ? 2.0 : 0.0);
        double health = (spider(player, SkillTrees.TOUGH_BODY) ? 2.0 : 0.0) + (spider(player, SkillTrees.HERO_BODY) ? 6.0 : 0.0);
        modifier(player, Attributes.ATTACK_DAMAGE, "attack", attack, AttributeModifier.Operation.ADD_VALUE);
        modifier(player, Attributes.MAX_HEALTH, "health", health, AttributeModifier.Operation.ADD_VALUE);
        modifier(player, Attributes.ATTACK_KNOCKBACK, "knockback", spider(player, SkillTrees.HEAVY_PUNCH) ? 0.6 : 0.0,
                AttributeModifier.Operation.ADD_VALUE);
        modifier(player, Attributes.ARMOR, "armor", spider(player, SkillTrees.SPIDER_ARMOR) ? 3.0 : 0.0, AttributeModifier.Operation.ADD_VALUE);
        modifier(player, Attributes.JUMP_STRENGTH, "jump", spider(player, SkillTrees.SPIDER_JUMP) ? 0.12 : 0.0,
                AttributeModifier.Operation.ADD_VALUE);
        modifier(player, Attributes.MOVEMENT_SPEED, "speed", spider(player, SkillTrees.ACROBAT) ? 0.15 : 0.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        modifier(player, Attributes.ATTACK_SPEED, "attack_speed", spider(player, SkillTrees.REFLEXES) ? 0.15 : 0.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    /**
     * Loader hook (damage about to hit a player): Dodge = 15 % of melee hits miss while Spider-Sense is on; Sixth Sense =
     * projectiles and explosions hurt 30 % less. @return the new amount, 0 = cancel
     */
    public static float incomingDamage(ServerPlayer player, DamageSource source, float amount) {
        if (spider(player, SkillTrees.DODGE) && Superpowers.isActive(player, PowerAbility.SPIDER_SENSE)
                && source.getDirectEntity() instanceof LivingEntity && source.getDirectEntity() == source.getEntity()
                && player.getRandom().nextFloat() < 0.15F) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_ATTACK_NODAMAGE,
                    SoundSource.PLAYERS, 0.8F, 1.4F);
            return 0.0F;
        }
        if (spider(player, SkillTrees.SIXTH_SENSE) && (source.is(DamageTypeTags.IS_PROJECTILE) || source.is(DamageTypeTags.IS_EXPLOSION))) {
            return amount * 0.7F;
        }
        return amount;
    }

    public static void forget(ServerPlayer player) {
        flushXp(player);
        LAST_SENSE.remove(player.getUUID());
        CLIMB_TICKS.remove(player.getUUID());
        UNSYNCED.remove(player);
        ATTRIBUTES_APPLIED.remove(player);
    }
}
