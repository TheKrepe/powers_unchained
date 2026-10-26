package com.star_1mp.powersunchained.web;

import com.star_1mp.powersunchained.Constants;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * Sticky webs on an entity's legs slow it down: every web stuck to it adds to this tick's count, at the end of the
 * server tick the movement speed modifier is set from the count (like Fisk's {@code applyWebSlowness}):
 * 1 web -> -40%, 2 -> -53%, 3 -> -60% ... and removed once no web is left. An entity wrapped in a web cocoon
 * can't move or jump at all.
 */
public final class WebSlowness {

    private static final Identifier MODIFIER = Constants.id("web_slowness");
    private static final Map<LivingEntity, Float> COUNTS = new WeakHashMap<>();
    private static final Identifier COCOON = Constants.id("web_cocoon");
    /** Entities wrapped in a web cocoon this tick (true) / last tick (false, modifiers still to remove). */
    private static final Map<LivingEntity, Boolean> HELD = new WeakHashMap<>();

    private WebSlowness() {
    }

    /** Server side, called by every sticky web on the entity's legs, every tick. */
    public static void add(LivingEntity entity) {
        COUNTS.merge(entity, 1.0F, Float::sum);
    }

    /** Server side, called every tick by a web cocoon: the entity can't move or jump this tick. */
    public static void hold(LivingEntity entity) {
        HELD.put(entity, true);
    }

    /** Wrapped mobs' looks to keep this tick (players turn on their own client, so only mobs). */
    private static final Map<LivingEntity, float[]> LOOKS = new WeakHashMap<>();

    /** Server side, every tick by a web cocoon: the mob can't turn its head or body (yaw, head yaw, pitch, body yaw). */
    public static void lockLook(LivingEntity entity, float[] look) {
        if (!(entity instanceof net.minecraft.world.entity.player.Player)) {
            LOOKS.put(entity, look);
        }
    }

    /** Server side, at the end of every server tick. */
    public static void endTick() {
        // after the mobs' own AI turned them: back to the wrapped-up look
        LOOKS.forEach((entity, look) -> {
            entity.setYRot(look[0]);
            entity.yRotO = look[0];
            entity.setYHeadRot(look[1]);
            entity.yHeadRotO = look[1];
            entity.setXRot(look[2]);
            entity.xRotO = look[2];
            entity.yBodyRot = look[3];
            entity.yBodyRotO = look[3];
        });
        LOOKS.clear();
        HELD.replaceAll((entity, held) -> {
            setHeld(entity, held);
            return false;
        });
        HELD.entrySet().removeIf(e -> !isHeld(e.getKey()));
        COUNTS.replaceAll((entity, count) -> {
            apply(entity, count);
            return 0.0F;
        });
        // entities without webs got their modifier removed above -> stop tracking them
        COUNTS.entrySet().removeIf(e -> !hasModifier(e.getKey()));
    }

    private static boolean isHeld(LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        return speed != null && speed.getModifier(COCOON) != null;
    }

    private static void setHeld(LivingEntity entity, boolean held) {
        for (var attribute : List.of(Attributes.MOVEMENT_SPEED, Attributes.JUMP_STRENGTH, Attributes.FLYING_SPEED)) {
            AttributeInstance instance = entity.getAttribute(attribute);
            if (instance == null) {
                continue;
            }
            boolean has = instance.getModifier(COCOON) != null;
            if (held && !has) {
                instance.addTransientModifier(new AttributeModifier(COCOON, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            } else if (!held && has) {
                instance.removeModifier(COCOON);
            }
        }
        if (held) {
            entity.setDeltaMovement(0.0, Math.min(entity.getDeltaMovement().y, 0.0), 0.0);
        }
    }

    private static boolean hasModifier(LivingEntity entity) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        return speed != null && speed.getModifier(MODIFIER) != null;
    }

    private static void apply(LivingEntity entity, float count) {
        AttributeInstance speed = entity.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed == null) {
            return;
        }
        double amount = count > 0 ? (1.0 / (1.0 + count) - 1.0) / 1.25 : 0.0;
        AttributeModifier current = speed.getModifier(MODIFIER);
        if (amount == 0.0) {
            if (current != null) {
                speed.removeModifier(MODIFIER);
            }
        } else if (current == null || current.amount() != amount) {
            if (current != null) {
                speed.removeModifier(MODIFIER);
            }
            speed.addTransientModifier(new AttributeModifier(MODIFIER, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        }
    }
}
