package com.thekrepe.powersunchained.web;

import com.thekrepe.powersunchained.entity.WebProjectile;
import com.thekrepe.powersunchained.platform.Services;
import com.thekrepe.powersunchained.registry.PUEntities;
import com.thekrepe.powersunchained.registry.PUItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** Server side of the "Web Shot" ability. */
public final class WebShooting {

    private WebShooting() {
    }

    /** Whether the player can use the web-shooters at all (worn in the accessory slot). */
    public static boolean hasWebShooters(Player player) {
        return Services.GAMEPLAY.isAccessoryEquipped(player, PUItems.WEB_SHOOTERS.get());
    }

    /** Cooldown is tracked on the web-shooters item, so it is synced to the client and shared by all web types. */
    public static ItemStack cooldownStack() {
        return PUItems.WEB_SHOOTERS.get().getDefaultInstance();
    }

    public static void shoot(ServerPlayer player, WebType type) {
        ItemStack cooldown = cooldownStack();
        if (player.isSpectator() || !hasWebShooters(player) || player.getCooldowns().isOnCooldown(cooldown)) {
            return;
        }
        ServerLevel level = player.level();
        boolean sticky = type == WebType.STICKY;
        WebProjectile web = new WebProjectile(sticky ? PUEntities.STICKY_WEB.get() : PUEntities.IMPACT_WEB.get(), level, player,
                (sticky ? PUItems.STICKY_WEB : PUItems.IMPACT_WEB).get().getDefaultInstance());
        web.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 2.5F, 0.5F);
        level.addFreshEntity(web);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS,
                0.6F, 1.6F + level.getRandom().nextFloat() * 0.3F);
        player.getCooldowns().addCooldown(cooldown, type.cooldown());
    }
}
