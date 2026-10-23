package com.thekrepe.powersunchained.web;

import com.thekrepe.powersunchained.entity.web.AbstractWebProjectile;
import com.thekrepe.powersunchained.entity.web.WebSwingLine;
import com.thekrepe.powersunchained.entity.web.WebZipLine;
import com.thekrepe.powersunchained.network.WebShotAnimPayload;
import com.thekrepe.powersunchained.platform.Services;
import com.thekrepe.powersunchained.registry.PUItems;
import com.thekrepe.powersunchained.registry.PUSounds;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/** Server side of the "Web Shot" ability. */
public final class WebShooting {

    /** A wrist that fired less than this many ticks ago makes the next web come from the other wrist. */
    private static final int ALTERNATE_TICKS = 8;

    private static final Map<UUID, State> STATES = new ConcurrentHashMap<>();

    private WebShooting() {
    }

    /** Whether the player can use the web-shooters at all (worn in the accessory slot). */
    public static boolean hasWebShooters(Player player) {
        return Services.GAMEPLAY.isAccessoryEquipped(player, PUItems.WEB_SHOOTERS.get());
    }

    public static void shoot(ServerPlayer player, WebType type) {
        if (player.isSpectator() || !hasWebShooters(player) || !player.getMainHandItem().isEmpty()) {
            return;
        }
        State state = STATES.computeIfAbsent(player.getUUID(), id -> new State());
        if (!state.ammo.canUse(type)) {
            return;
        }
        if (!player.getAbilities().instabuild) {
            if (!WebCartridges.consumeWorn(player)) {
                // out of web fluid: dry click
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.DISPENSER_FAIL,
                        SoundSource.PLAYERS, 0.5F, 1.6F);
                return;
            }
        }
        state.ammo.use(type);

        // right wrist, or the left one when the right fired just before (fast shooting alternates)
        long now = player.level().getGameTime();
        int side = now - state.lastRight < ALTERNATE_TICKS && state.lastRight >= state.lastLeft
                ? AbstractWebProjectile.LEFT : AbstractWebProjectile.RIGHT;
        if (side == AbstractWebProjectile.RIGHT) {
            state.lastRight = now;
        } else {
            state.lastLeft = now;
        }

        ServerLevel level = player.level();
        level.addFreshEntity(type.create(level, player, side));
        announceShot(player, side);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), PUSounds.WEB_SHOOT.get(), SoundSource.PLAYERS,
                1.0F, 0.95F + level.getRandom().nextFloat() * 0.1F);
    }

    /** Tells everyone watching (and the shooter) to play the arm animation of a web shot from the given wrist. */
    public static void announceShot(ServerPlayer player, int side) {
        Services.GAMEPLAY.sendToTrackingAndSelf(player, new WebShotAnimPayload(player.getId(), side));
    }

    /** Ticks between two web zips. */
    public static final int ZIP_COOLDOWN = 20;

    /** Web Zip: shoots a web line that pulls the player to where it hits. */
    public static void zip(ServerPlayer player) {
        if (player.isSpectator() || !hasWebShooters(player) || !player.getMainHandItem().isEmpty()) {
            return;
        }
        State state = STATES.computeIfAbsent(player.getUUID(), id -> new State());
        long now = player.level().getGameTime();
        if (now - state.lastZip < ZIP_COOLDOWN - 2) {  // a little slack for lag
            return;
        }
        if (!player.getAbilities().instabuild && !WebCartridges.consumeWorn(player)) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.DISPENSER_FAIL,
                    SoundSource.PLAYERS, 0.5F, 1.6F);
            return;
        }
        state.lastZip = now;
        markFallSafe(player);
        ServerLevel level = player.level();
        // one line at a time
        level.getEntitiesOfClass(WebZipLine.class, player.getBoundingBox().inflate(64.0), line -> line.getShooter() == player)
                .forEach(WebZipLine::discard);
        level.addFreshEntity(new WebZipLine(level, player));
        announceShot(player, AbstractWebProjectile.RIGHT);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), PUSounds.WEB_SHOOT.get(), SoundSource.PLAYERS,
                1.0F, 0.75F + level.getRandom().nextFloat() * 0.1F);
    }

    /** Ticks between two web swinging ropes. */
    public static final int SWING_COOLDOWN = 6;

    /** Web swinging (use key while swinging is on): shoots a rope that the player swings on. */
    public static void swingShoot(ServerPlayer player) {
        if (player.isSpectator() || !hasWebShooters(player) || !player.getMainHandItem().isEmpty()) {
            return;
        }
        State state = STATES.computeIfAbsent(player.getUUID(), id -> new State());
        long now = player.level().getGameTime();
        if (now - state.lastSwing < SWING_COOLDOWN - 2) {
            return;
        }
        if (!player.getAbilities().instabuild && !WebCartridges.consumeWorn(player)) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.DISPENSER_FAIL,
                    SoundSource.PLAYERS, 0.5F, 1.6F);
            return;
        }
        state.lastSwing = now;
        ServerLevel level = player.level();
        // one hand when the head is turned away from the body, more often the further (like Fisk's)
        int kind = WebSwingLine.BOTH_HANDS;
        double diff = Mth.wrapDegrees(player.yBodyRot - player.getYRot());
        if (level.getRandom().nextDouble() < 0.25 + Math.min(Math.abs(diff) / 40.0, 0.5)) {
            kind = diff > 0.0 ? WebSwingLine.LEFT_HAND : WebSwingLine.RIGHT_HAND;
        }
        // a new rope replaces the old one
        level.getEntitiesOfClass(WebSwingLine.class, player.getBoundingBox().inflate(80.0),
                line -> line.getShooter() == player && !line.isDetached()).forEach(WebSwingLine::letGo);
        level.addFreshEntity(new WebSwingLine(level, player, kind));
        announceShot(player, kind == WebSwingLine.LEFT_HAND ? AbstractWebProjectile.LEFT : AbstractWebProjectile.RIGHT);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), PUSounds.WEB_SHOOT.get(), SoundSource.PLAYERS,
                1.0F, 0.85F + level.getRandom().nextFloat() * 0.1F);
    }

    /** The player let go of the swing rope ({@code boost}: with the attack key, which also yanks a held mob). */
    public static void swingRelease(ServerPlayer player, boolean boost) {
        WebSwingLine line = WebSwingLine.of(player);
        if (line == null) {
            return;
        }
        if (boost && line.getHooked() != null) {
            line.yankHooked(player);
        } else {
            line.letGo();
        }
        markFallSafe(player);
        if (boost && !player.onGround()) {
            ServerLevel level = player.level();
            level.playSound(player, player.getX(), player.getY(), player.getZ(), PUSounds.WEB_SWING_WHOOSH.get(),
                    SoundSource.PLAYERS, 0.8F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        }
    }

    /**
     * No fall damage until the player lands after travelling by web (zip, swing): set when zipping, while hanging
     * on a swing rope and when letting go.
     */
    public static void markFallSafe(Player player) {
        State state = STATES.computeIfAbsent(player.getUUID(), id -> new State());
        state.fallSafe = true;
        state.fallSafeSince = player.level().getGameTime();
    }

    public static boolean isFallSafe(Player player) {
        State state = STATES.get(player.getUUID());
        return state != null && state.fallSafe;
    }

    /** Server, every player tick: the web travel fall protection ends after landing. */
    public static void playerTick(ServerPlayer player) {
        State state = STATES.get(player.getUUID());
        if (state != null && state.fallSafe && (player.onGround() || player.isInWater())
                && player.level().getGameTime() - state.fallSafeSince > 3) {
            state.fallSafe = false;
        }
    }

    /** Server tick: recharges the shots of every player who used the web-shooters. */
    public static void tick() {
        STATES.values().forEach(state -> state.ammo.tick());
    }

    public static void forget(Player player) {
        STATES.remove(player.getUUID());
    }

    private static final class State {
        final WebAmmo ammo = new WebAmmo();
        long lastRight = Long.MIN_VALUE / 2;
        long lastLeft = Long.MIN_VALUE / 2;
        long lastZip = Long.MIN_VALUE / 2;
        long lastSwing = Long.MIN_VALUE / 2;
        boolean fallSafe;
        long fallSafeSince;
    }
}
