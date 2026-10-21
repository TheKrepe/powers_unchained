package com.thekrepe.powersunchained.web;

import com.thekrepe.powersunchained.entity.web.AbstractWebProjectile;
import com.thekrepe.powersunchained.entity.web.WebZipLine;
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
import net.minecraft.world.item.ItemStack;
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
            ItemStack shooters = WebCartridges.wornShooters(player);
            if (!WebCartridges.consume(shooters)) {
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
        level.playSound(null, player.getX(), player.getY(), player.getZ(), PUSounds.WEB_SHOOT.get(), SoundSource.PLAYERS,
                1.0F, 0.95F + level.getRandom().nextFloat() * 0.1F);
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
        if (!player.getAbilities().instabuild && !WebCartridges.consume(WebCartridges.wornShooters(player))) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.DISPENSER_FAIL,
                    SoundSource.PLAYERS, 0.5F, 1.6F);
            return;
        }
        state.lastZip = now;
        ServerLevel level = player.level();
        // one line at a time
        level.getEntitiesOfClass(WebZipLine.class, player.getBoundingBox().inflate(64.0), line -> line.getShooter() == player)
                .forEach(WebZipLine::discard);
        level.addFreshEntity(new WebZipLine(level, player));
        level.playSound(null, player.getX(), player.getY(), player.getZ(), PUSounds.WEB_SHOOT.get(), SoundSource.PLAYERS,
                1.0F, 0.75F + level.getRandom().nextFloat() * 0.1F);
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
    }
}
