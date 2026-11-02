package com.star_1mp.powersunchained.client.power;

import com.star_1mp.powersunchained.entity.web.WebSwingLine;
import com.star_1mp.powersunchained.entity.web.WebZipLine;
import com.star_1mp.powersunchained.mixin.client.ClientInputAccessor;
import com.star_1mp.powersunchained.network.ClimbPayload;
import com.star_1mp.powersunchained.platform.Services;
import com.star_1mp.powersunchained.power.Superpowers;
import com.star_1mp.powersunchained.power.WallClimbing;
import java.util.ArrayDeque;
import java.util.Deque;
import net.minecraft.client.player.ClientInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Wall climbing of the local player (client-authoritative, like web swinging): crawling over walls and ceilings in
 * the spirit of Marvel's Spider-Man. Runs in the movement input event (inside the player's tick, right before vanilla
 * applies the input), takes the movement keys over and sets the motion itself; the standing hitbox stays.
 *
 * <p>Controls follow the camera: on a wall the part of the input that pushes into the wall (or away from it) climbs
 * up (down), the rest moves where you look along the wall; on a ceiling you walk where you look. Pushing into a
 * wall clings to it, holding jump under a ceiling clings to the ceiling. Jump leaps off a wall (drops from a ceiling),
 * sneak lets go. Edges and corners are crossed by short scripted moves (around outer corners, over the top of a wall,
 * from a wall's bottom edge onto the underside, from a ceiling's edge onto the outer face), turning the camera with
 * the surface so the held keys keep going the same way.
 */
public final class WallClimbClient {

    private static final double SPEED = 0.13;
    private static final double SPRINT = 1.7;
    private static final double PROBE = 0.08;
    /** How far the box moves to get around an outer edge. */
    private static final double WRAP = 0.68;

    private static int mode = WallClimbing.MODE_NONE;
    private static Direction wall = Direction.NORTH;
    private static int attachTicks;
    private static int cooldown;
    private static int pushTicks;
    private static boolean wasJump;
    /** Looking away from the wall (with hysteresis, like the pose): hanging there, back to it, no crawling. */
    private static boolean facingOut;
    private static int sentMode = WallClimbing.MODE_NONE;
    private static int sentWall = -1;
    /** Scripted moves of an edge crossing (one per tick) and what follows them. */
    private static final Deque<Step> SCRIPT = new ArrayDeque<>();
    private static int scriptMode;
    private static Direction scriptWall = Direction.NORTH;
    private static Vec3 scriptExit = Vec3.ZERO;

    private record Step(Vec3 move, float yaw) {
    }

    private WallClimbClient() {
    }

    public static boolean isClimbing() {
        return mode != WallClimbing.MODE_NONE || !SCRIPT.isEmpty();
    }

    public static int mode() {
        return mode;
    }

    public static Direction wall() {
        return wall;
    }

    /** Dev/CI only: clings the local player to a wall / ceiling right away. */
    public static void debugAttach(LocalPlayer player, int newMode, Direction newWall) {
        SCRIPT.clear();
        attach(player, newMode, newWall);
    }

    /**
     * Movement input event of the local player: climbs, crosses edges, attaches.
     *
     * @return whether climbing took the movement over this tick
     */
    public static boolean onMovementInput(LocalPlayer player, ClientInput input) {
        if (cooldown > 0) {
            cooldown--;
        }
        Input keys = input.keyPresses;
        boolean jumpPressed = keys.jump() && !wasJump;
        wasJump = keys.jump();
        if (!canClimb(player)) {
            if (isClimbing()) {
                detach(player, Vec3.ZERO, 0);
            }
            attachTicks = 0;
            return false;
        }
        Vec2 move = input.getMoveVector();
        if (!SCRIPT.isEmpty()) {
            runScript(player);
            neutralize(input);
            return true;
        }
        if (mode == WallClimbing.MODE_NONE) {
            tryAttach(player, move, keys);
            if (mode == WallClimbing.MODE_NONE) {
                return false;
            }
        }
        if (keys.shift()) {
            // let go (no clinging again while sneak is held)
            detach(player, Vec3.ZERO, 4);
        } else if (jumpPressed) {
            leap(player);
        } else if (mode == WallClimbing.MODE_WALL) {
            wallTick(player, move, keys);
        } else {
            ceilingTick(player, move, keys);
        }
        if (isClimbing()) {
            player.resetFallDistance();
        }
        neutralize(input);
        return true;
    }

    /** Clears the movement keys so vanilla adds no walking push or jump of its own. */
    private static void neutralize(ClientInput input) {
        ((ClientInputAccessor) input).powers_unchained$setMoveVector(Vec2.ZERO);
        Input k = input.keyPresses;
        input.keyPresses = new Input(false, false, false, false, false, false, k.sprint());
    }

    private static boolean canClimb(LocalPlayer player) {
        return Superpowers.isActive(player, com.star_1mp.powersunchained.power.PowerAbility.WALL_CLIMBING)
                && !player.isSpectator()
                && !player.getAbilities().flying
                && !player.isPassenger()
                && !player.isFallFlying()
                && !player.isInWater()
                && !player.isInLava()
                && !player.isSleeping()
                && player.isAlive()
                && !player.onClimbable()
                && WebSwingLine.of(player) == null
                && !zipping(player)
                && !RollClient.isRolling();
    }

    private static boolean zipping(LocalPlayer player) {
        // optimisation: the line reports itself (used to be a search of a 128-block box every tick)
        return WebZipLine.isZipping(player);
    }

    // ---- attaching / leaving ----

    private static void tryAttach(LocalPlayer player, Vec2 move, Input keys) {
        if (cooldown > 0 || keys.shift()) {
            attachTicks = 0;
            return;
        }
        Vec3 wish = horizontalWish(player, move);
        if (wish.lengthSqr() > 0.1) {
            Direction d = horizontalDirection(wish);
            Vec3 n = normal(d);
            AABB box = player.getBoundingBox();
            if (wish.normalize().dot(n) > 0.6 && touches(player, box, d) && touches(player, upperHalf(box), d)) {
                attachTicks++;
                if (attachTicks >= (player.onGround() ? 3 : 1)) {
                    attach(player, WallClimbing.MODE_WALL, d);
                    return;
                }
            } else {
                attachTicks = 0;
            }
        } else {
            attachTicks = 0;
        }
        if (!player.onGround() && keys.jump() && touches(player, player.getBoundingBox(), Direction.UP)) {
            attach(player, WallClimbing.MODE_CEILING, wall);
        }
    }

    private static void attach(LocalPlayer player, int newMode, Direction newWall) {
        if (newMode == WallClimbing.MODE_CEILING && !com.star_1mp.powersunchained.power.PowerSkills.spider(player, com.star_1mp.powersunchained.power.skill.SkillTrees.CEILINGS)) {
            return;  // ceilings are a skill (Agility)
        }
        boolean fresh = mode == WallClimbing.MODE_NONE;
        mode = newMode;
        wall = newWall;
        attachTicks = 0;
        pushTicks = 0;
        player.resetFallDistance();
        if (fresh) {
            player.playSound(SoundEvents.WOOL_STEP, 0.4F, 1.3F);
        }
        sync();
    }

    /** Lets go with {@code motion}; no clinging again for {@code cooldownTicks}. */
    private static void detach(LocalPlayer player, Vec3 motion, int cooldownTicks) {
        SCRIPT.clear();
        mode = WallClimbing.MODE_NONE;
        cooldown = Math.max(cooldown, cooldownTicks);
        attachTicks = 0;
        player.setDeltaMovement(motion);
        sync();
    }

    /**
     * Roll key while clinging: a flip off the surface — off a wall a back flip when facing it (a front flip when facing
     * away), away from it with a lift; off a ceiling a front flip down.
     *
     * @return {roll kind, body yaw} for the animation, or {@code null}
     */
    public static float @Nullable [] flipOff(LocalPlayer player) {
        if (!SCRIPT.isEmpty() || (mode != WallClimbing.MODE_WALL && mode != WallClimbing.MODE_CEILING)) {
            return null;
        }
        Vec3 look = player.getLookAngle();
        Vec3 lookH = new Vec3(look.x, 0.0, look.z);
        if (mode == WallClimbing.MODE_CEILING) {
            Vec3 dir = lookH.lengthSqr() > 1.0E-4 ? lookH.normalize() : Vec3.ZERO;
            detach(player, dir.scale(0.35).add(0.0, -0.15, 0.0), 12);
            return new float[] {com.star_1mp.powersunchained.power.Rolling.KIND_FLIP, player.getYRot()};
        }
        Vec3 n = normal(wall);
        Vec3 out = n.scale(-1.0);
        boolean facingWall = lookH.lengthSqr() < 1.0E-4 || lookH.normalize().dot(n) > -0.2;
        Vec3 dir = facingWall || lookH.normalize().dot(out) < 0.3 ? out : lookH.normalize();
        detach(player, dir.scale(0.75).add(0.0, 0.55, 0.0), 12);
        float yawOut = (float) (Mth.atan2(dir.z, dir.x) * Mth.RAD_TO_DEG) - 90.0F;
        return facingWall
                ? new float[] {com.star_1mp.powersunchained.power.Rolling.KIND_BACKFLIP, yawOut + 180.0F}
                : new float[] {com.star_1mp.powersunchained.power.Rolling.KIND_FLIP, yawOut};
    }

    /** Jump: a leap off the wall (where you look, if that's away from it) / a drop from the ceiling. */
    private static void leap(LocalPlayer player) {
        if (mode == WallClimbing.MODE_CEILING) {
            detach(player, new Vec3(0.0, -0.1, 0.0), 8);
            return;
        }
        Vec3 out = normal(wall).scale(-1.0);
        Vec3 look = player.getLookAngle();
        Vec3 dir = new Vec3(look.x, 0.0, look.z);
        dir = dir.lengthSqr() < 1.0E-4 || dir.normalize().dot(out) < 0.3 ? out : dir.normalize();
        detach(player, dir.scale(0.55).add(0.0, 0.45, 0.0), 10);
        player.playSound(SoundEvents.WOOL_STEP, 0.5F, 0.9F);
    }

    private static void sync() {
        int w = wall.get3DDataValue();
        if (sentMode != mode || (mode == WallClimbing.MODE_WALL && sentWall != w)) {
            sentMode = mode;
            sentWall = w;
            Services.GAMEPLAY.sendToServer(new ClimbPayload(mode, w));
        }
    }

    /** The world changed (respawn, new level): forget everything. */
    public static void reset() {
        SCRIPT.clear();
        mode = WallClimbing.MODE_NONE;
        sentMode = WallClimbing.MODE_NONE;
        sentWall = -1;
        cooldown = 0;
    }

    // ---- on a wall ----

    private static void wallTick(LocalPlayer player, Vec2 move, Input keys) {
        AABB box = player.getBoundingBox();
        if (!touches(player, box, wall) && !touches(player, box.move(normal(wall).scale(0.2)), wall)) {
            detach(player, player.getDeltaMovement(), 4);
            return;
        }
        Vec3 n = normal(wall);
        Vec3 lookH = new Vec3(player.getLookAngle().x, 0.0, player.getLookAngle().z);
        double away = lookH.lengthSqr() > 1.0E-4 ? -lookH.normalize().dot(n) : 0.0;
        facingOut = facingOut ? away > 0.1 : away > 0.35;
        if (facingOut) {
            // back to the wall: just hold on (jump / sneak / the roll key still work)
            pushTicks = 0;
            setMotion(player, n.scale(0.1));
            return;
        }
        Vec3 wish = lookWish(player, move);
        double into = wish.dot(n);
        Vec3 w = wish.subtract(n.scale(into)).add(0.0, into, 0.0);
        double amount = Math.min(1.0, Math.sqrt(move.x * move.x + move.y * move.y));
        Vec3 v = w.lengthSqr() > 1.0E-6 ? w.normalize().scale(amount * speed(keys)) : Vec3.ZERO;

        // feet on the ground and climbing down: stand
        if (v.y < 0.0 && touches(player, box, Direction.DOWN)) {
            if (w.normalize().y < -0.5) {
                detach(player, Vec3.ZERO, 6);
                return;
            }
            v = new Vec3(v.x, 0.0, v.z);
        }
        // a ceiling over the wall: looking up onto it while climbing up moves onto it
        if (v.y > 0.0 && touches(player, box, Direction.UP)) {
            if (player.getXRot() < -25.0F && w.normalize().y > 0.3) {
                if (++pushTicks >= 3) {
                    attach(player, WallClimbing.MODE_CEILING, wall);
                    setMotion(player, new Vec3(0.0, 0.08, 0.0));
                    return;
                }
            } else {
                pushTicks = 0;
            }
            v = new Vec3(v.x, 0.0, v.z);
        }
        Vec3 side = new Vec3(v.x, 0.0, v.z).subtract(n.scale(v.dot(n)));
        // inner corner: another wall across the way -> climb onto it, the camera turning along
        if (side.lengthSqr() > 1.0E-4) {
            Direction sd = horizontalDirection(side);
            if (touches(player, box, sd)) {
                if (++pushTicks >= 2) {
                    turnTo(player, sd, wall, Vec3.ZERO, WallClimbing.MODE_WALL, 5);
                    return;
                }
                v = v.subtract(side);
                side = Vec3.ZERO;
            }
        }
        AABB next = box.move(v);
        if (v.y > 0.0 && !touches(player, upperPart(next, 1.0), wall) && tryMantle(player, next)) {
            return;
        }
        if (!grips(player, next, wall)) {
            // the wall ends this way (under the body's middle): round the edge if there is room, else stop at it
            if (side.lengthSqr() > 1.0E-4 && tryOuterCorner(player, next, v, horizontalDirection(side))) {
                return;
            }
            if (v.y < 0.0 && tryUnderneath(player, next, v)) {
                return;
            }
            v = keepContact(player, box, v, wall);
        }
        setMotion(player, v.add(n.scale(0.1)));
    }

    /** Over the top of a wall: up until the feet clear it, then just onto its edge (no step further). */
    private static boolean tryMantle(LocalPlayer player, AABB next) {
        AABB box = player.getBoundingBox();
        Vec3 n = normal(wall);
        for (double dy = 0.0; dy <= 1.3; dy += 0.05) {
            AABB up = box.move(0.0, dy + 0.02, 0.0);
            if (!player.level().noCollision(player, up)) {
                continue;
            }
            // the shortest move onto the top that stands safely on it
            for (double f = 0.1; f <= 0.75; f += 0.05) {
                AABB on = up.move(n.scale(f));
                if (player.level().noCollision(player, on) && !player.level().noCollision(player, on.move(0.0, -0.12, 0.0))) {
                    double forward = Math.min(f + 0.12, 0.75);
                    if (!player.level().noCollision(player, up.move(n.scale(forward)))) {
                        forward = f;
                    }
                    SCRIPT.clear();
                    // the climbing-over pose while it lasts
                    mode = WallClimbing.MODE_MANTLE;
                    sync();
                    int upTicks = Math.max(3, Mth.ceil((dy + 0.02) / 0.16));
                    for (int i = 0; i < upTicks; i++) {
                        SCRIPT.add(new Step(new Vec3(0.0, (dy + 0.02) / upTicks, 0.0).add(n.scale(0.02)), 0.0F));
                    }
                    for (int i = 0; i < 3; i++) {
                        SCRIPT.add(new Step(n.scale(forward / 3.0), 0.0F));
                    }
                    scriptMode = WallClimbing.MODE_NONE;
                    scriptWall = wall;
                    scriptExit = Vec3.ZERO;
                    runScript(player);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Round an outer corner: the body's middle reached the wall's end, so it wraps round: on past the edge until the
     * box clears it, then beside the wall's side face, clinging to it (the camera turning along).
     */
    private static boolean tryOuterCorner(LocalPlayer player, AABB next, Vec3 v, Direction sd) {
        Vec3 n = normal(wall);
        Vec3 t = normal(sd);
        Direction newWall = sd.getOpposite();
        if (!player.level().noCollision(player, next)) {
            return false;
        }
        double m = clearance(player, next, t, n.scale(WRAP));
        if (m < 0.0) {
            return false;
        }
        AABB beside = next.move(t.scale(m)).move(n.scale(WRAP));
        if (!touches(player, beside.move(normal(newWall).scale(0.2)), newWall)) {
            return false;
        }
        SCRIPT.clear();
        SCRIPT.add(new Step(v, 0.0F));
        float yaw = yawBetween(wall, newWall);
        int sideTicks = Math.max(1, Mth.ceil(m / 0.13));
        for (int i = 0; i < sideTicks; i++) {
            SCRIPT.add(new Step(t.scale(m / sideTicks), yaw * 0.25F / sideTicks));
        }
        turnSteps(n.scale(WRAP), yaw * 0.75F, 4);
        scriptMode = WallClimbing.MODE_WALL;
        scriptWall = newWall;
        scriptExit = Vec3.ZERO;
        runScript(player);
        return true;
    }

    /**
     * How far the box has to go along {@code dir} (0..1) so that it can then make the move {@code then} without
     * hitting anything, or -1.
     */
    private static double clearance(LocalPlayer player, AABB box, Vec3 dir, Vec3 then) {
        for (double m = 0.0; m <= 1.0; m += 0.02) {
            AABB a = box.move(dir.scale(m));
            if (!player.level().noCollision(player, a)) {
                return -1.0;
            }
            if (player.level().noCollision(player, a.move(then))) {
                return m + 0.01;
            }
        }
        return -1.0;
    }

    /** From a wall's bottom edge onto its underside (a ceiling). */
    private static boolean tryUnderneath(LocalPlayer player, AABB next, Vec3 v) {
        if (!com.star_1mp.powersunchained.power.PowerSkills.spider(player, com.star_1mp.powersunchained.power.skill.SkillTrees.CEILINGS)) {
            return false;
        }
        Vec3 n = normal(wall);
        Vec3 look = player.getLookAngle();
        if (new Vec3(look.x, 0.0, look.z).dot(n) < -0.2 && player.getXRot() < 60.0F) {
            return false; // looking away from the wall: not heading underneath
        }
        if (!player.level().noCollision(player, next)) {
            return false;
        }
        // down until the box clears the bottom edge, then in under the block
        double m = clearance(player, next, new Vec3(0.0, -1.0, 0.0), n.scale(WRAP));
        if (m < 0.0) {
            return false;
        }
        AABB under = next.move(0.0, -m, 0.0).move(n.scale(WRAP));
        if (!touches(player, under.move(0.0, 0.25 + m, 0.0), Direction.UP)) {
            return false;
        }
        SCRIPT.clear();
        SCRIPT.add(new Step(v, 0.0F));
        int downTicks = Math.max(1, Mth.ceil(m / 0.15));
        for (int i = 0; i < downTicks; i++) {
            SCRIPT.add(new Step(new Vec3(0.0, -m / downTicks, 0.0), 0.0F));
        }
        // keep the held keys going the same way: they now walk on under the block
        Vec3 wish = horizontalWish(player, currentMove(player));
        float yaw = wish.lengthSqr() > 0.09 ? signedYaw(wish, n) : 0.0F;
        turnSteps(n.scale(WRAP).add(0.0, 0.12 + m, 0.0), yaw, 5);
        scriptMode = WallClimbing.MODE_CEILING;
        scriptWall = wall;
        scriptExit = new Vec3(0.0, 0.1, 0.0);
        runScript(player);
        return true;
    }

    // ---- on a ceiling ----

    private static void ceilingTick(LocalPlayer player, Vec2 move, Input keys) {
        AABB box = player.getBoundingBox();
        if (player.onGround() || !touches(player, box.move(0.0, 0.2, 0.0), Direction.UP)) {
            detach(player, player.getDeltaMovement(), 4);
            return;
        }
        Vec3 wish = horizontalWish(player, move);
        Vec3 v = wish.scale(speed(keys));
        if (v.lengthSqr() > 1.0E-4) {
            Direction sd = horizontalDirection(v);
            // a wall ahead: onto it (unless looking up into the corner)
            if (touches(player, box, sd)) {
                if (player.getXRot() > -10.0F && ++pushTicks >= 3) {
                    attach(player, WallClimbing.MODE_WALL, sd);
                    setMotion(player, normal(sd).scale(0.1));
                    return;
                }
                v = v.subtract(normal(sd).scale(v.dot(normal(sd))));
            } else {
                pushTicks = 0;
            }
            AABB next = box.move(v);
            if (!grips(player, next, Direction.UP)) {
                if (v.lengthSqr() > 1.0E-4 && tryCeilingEdge(player, next, v, horizontalDirection(v))) {
                    return;
                }
                v = keepContact(player, box, v, Direction.UP);
            }
        }
        setMotion(player, v.add(0.0, 0.1, 0.0));
    }

    /** Over a ceiling's edge onto the outer face above it. */
    private static boolean tryCeilingEdge(LocalPlayer player, AABB next, Vec3 v, Direction sd) {
        Direction newWall = sd.getOpposite();
        Vec3 t = normal(sd);
        if (!player.level().noCollision(player, next)) {
            return false;
        }
        // on past the edge until the box clears it, then up beside the outer face
        double m = clearance(player, next, t, new Vec3(0.0, 0.75, 0.0));
        if (m < 0.0) {
            return false;
        }
        AABB up = next.move(t.scale(m)).move(0.0, 0.75, 0.0);
        if (!touches(player, up.move(normal(newWall).scale(0.2)), newWall)) {
            return false;
        }
        SCRIPT.clear();
        SCRIPT.add(new Step(v, 0.0F));
        int sideTicks = Math.max(1, Mth.ceil(m / 0.13));
        for (int i = 0; i < sideTicks; i++) {
            SCRIPT.add(new Step(t.scale(m / sideTicks), 0.0F));
        }
        // face the wall, so the held keys climb on up
        Vec3 wish = horizontalWish(player, currentMove(player));
        float yaw = wish.lengthSqr() > 0.09 ? signedYaw(wish, normal(newWall)) : 0.0F;
        turnSteps(new Vec3(0.0, 0.75, 0.0), yaw, 5);
        scriptMode = WallClimbing.MODE_WALL;
        scriptWall = newWall;
        scriptExit = Vec3.ZERO;
        runScript(player);
        return true;
    }

    // ---- scripted edge crossings ----

    private static void turnTo(LocalPlayer player, Direction newWall, Direction oldWall, Vec3 offset, int newMode, int ticks) {
        float yaw = yawBetween(oldWall, newWall);
        attach(player, newMode, newWall);
        SCRIPT.clear();
        turnSteps(offset, yaw, ticks);
        scriptMode = newMode;
        scriptWall = newWall;
        scriptExit = Vec3.ZERO;
        runScript(player);
    }

    private static void turnSteps(Vec3 move, float yaw, int ticks) {
        for (int i = 0; i < ticks; i++) {
            float a = smooth((i + 1) / (float) ticks) - smooth(i / (float) ticks);
            SCRIPT.add(new Step(move.scale(1.0 / ticks), yaw * a));
        }
    }

    private static void runScript(LocalPlayer player) {
        Step step = SCRIPT.poll();
        if (step == null) {
            return;
        }
        if (step.yaw() != 0.0F) {
            player.setYRot(player.getYRot() + step.yaw());
            player.setYHeadRot(player.getYHeadRot() + step.yaw());
        }
        setMotion(player, step.move());
        if (mode != scriptMode || wall != scriptWall) {
            // the pose turns towards the next surface while moving
            if (scriptMode != WallClimbing.MODE_NONE) {
                attach(player, scriptMode, scriptWall);
            }
        }
        if (SCRIPT.isEmpty()) {
            if (scriptMode == WallClimbing.MODE_NONE) {
                SCRIPT.clear();
                mode = WallClimbing.MODE_NONE;
                cooldown = 6;
                sync();
                // the last step still moves this tick; the exit motion carries on after it
                player.setDeltaMovement(step.move().add(scriptExit));
            }
        }
        player.resetFallDistance();
    }

    // ---- helpers ----

    private static void setMotion(LocalPlayer player, Vec3 v) {
        player.setDeltaMovement(v);
    }

    private static double speed(Input keys) {
        LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
        double skill = player != null && com.star_1mp.powersunchained.power.PowerSkills.spider(player, com.star_1mp.powersunchained.power.skill.SkillTrees.QUICK_CRAWL) ? 1.3 : 1.0;  // Quick Crawl skill
        return SPEED * skill * (keys.sprint() ? SPRINT : 1.0);
    }

    private static Vec2 currentMove(LocalPlayer player) {
        return player.input.getMoveVector();
    }

    /** The move keys as a world direction on the horizontal plane (length = input strength). */
    private static Vec3 horizontalWish(LocalPlayer player, Vec2 move) {
        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        double sin = Mth.sin(yaw);
        double cos = Mth.cos(yaw);
        Vec3 forward = new Vec3(-sin, 0.0, cos);
        Vec3 right = new Vec3(-cos, 0.0, -sin);
        Vec3 w = forward.scale(move.y).add(right.scale(-move.x));
        return w.lengthSqr() > 1.0 ? w.normalize() : w;
    }

    /** The move keys along the full look direction (forward follows the pitch). */
    private static Vec3 lookWish(LocalPlayer player, Vec2 move) {
        float yaw = player.getYRot() * Mth.DEG_TO_RAD;
        Vec3 right = new Vec3(-Mth.cos(yaw), 0.0, -Mth.sin(yaw));
        Vec3 w = player.getLookAngle().scale(move.y).add(right.scale(-move.x));
        return w.lengthSqr() > 1.0 ? w.normalize() : w;
    }

    private static Vec3 normal(Direction d) {
        return new Vec3(d.getStepX(), d.getStepY(), d.getStepZ());
    }

    private static Direction horizontalDirection(Vec3 v) {
        if (Math.abs(v.x) > Math.abs(v.z)) {
            return v.x > 0.0 ? Direction.EAST : Direction.WEST;
        }
        return v.z > 0.0 ? Direction.SOUTH : Direction.NORTH;
    }

    /** Yaw change that turns {@code from} into {@code to} (degrees, -180..180). */
    private static float yawBetween(Direction from, Direction to) {
        return Mth.wrapDegrees(to.toYRot() - from.toYRot());
    }

    private static float signedYaw(Vec3 from, Vec3 to) {
        float a = (float) (Mth.atan2(from.z, from.x) * Mth.RAD_TO_DEG);
        float b = (float) (Mth.atan2(to.z, to.x) * Mth.RAD_TO_DEG);
        return Mth.wrapDegrees(b - a);
    }

    private static float smooth(float t) {
        t = Mth.clamp(t, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    /** Whether there is something solid right next to {@code box} on side {@code d}. */
    static boolean touches(LocalPlayer player, AABB box, Direction d) {
        AABB probe;
        if (d.getAxis().isHorizontal()) {
            // a little off the floor and the ceiling, so only walls count
            probe = new AABB(box.minX, box.minY + 0.03, box.minZ, box.maxX, box.maxY - 0.03, box.maxZ)
                    .move(d.getStepX() * PROBE, 0.0, d.getStepZ() * PROBE);
        } else {
            probe = box.deflate(0.03, 0.0, 0.03).move(0.0, d.getStepY() * PROBE, 0.0);
        }
        return !player.level().noCollision(player, probe);
    }

    /**
     * Whether the middle of the body is over the surface on side {@code d} (not just an edge of the box): the box
     * narrowed around its centre — along the surface to 0.24 wide, on a wall also to its middle 0.9 of height.
     */
    static boolean grips(LocalPlayer player, AABB box, Direction d) {
        Vec3 c = box.getCenter();
        double hx = d.getAxis() == Direction.Axis.X ? box.getXsize() / 2.0 : 0.12;
        double hz = d.getAxis() == Direction.Axis.Z ? box.getZsize() / 2.0 : 0.12;
        double hy = d.getAxis().isVertical() ? box.getYsize() / 2.0 : 0.45;
        return touches(player, new AABB(c.x - hx, c.y - hy, c.z - hz, c.x + hx, c.y + hy, c.z + hz), d);
    }

    private static AABB upperHalf(AABB box) {
        return new AABB(box.minX, box.minY + box.getYsize() * 0.5, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    /** The box above {@code fromFeet} blocks over the feet. */
    private static AABB upperPart(AABB box, double fromFeet) {
        return new AABB(box.minX, box.minY + fromFeet, box.minZ, box.maxX, box.maxY, box.maxZ);
    }

    /** The largest part of the motion that keeps the box on the surface (whole, one axis, or nothing). */
    private static Vec3 keepContact(LocalPlayer player, AABB box, Vec3 v, Direction surface) {
        Vec3[] tries = {new Vec3(v.x, 0.0, v.z), new Vec3(0.0, v.y, 0.0), new Vec3(v.x, 0.0, 0.0), new Vec3(0.0, 0.0, v.z)};
        Vec3 best = Vec3.ZERO;
        // normally the body's middle stays over the surface; clinging off-centre (just attached), any contact will do
        boolean centred = grips(player, box, surface);
        for (Vec3 t : tries) {
            AABB moved = box.move(t);
            if (t.lengthSqr() > best.lengthSqr() && (centred ? grips(player, moved, surface) : touches(player, moved, surface))) {
                best = t;
            }
        }
        return best;
    }

    static @Nullable Direction wallOrNull() {
        return mode == WallClimbing.MODE_WALL ? wall : null;
    }
}
