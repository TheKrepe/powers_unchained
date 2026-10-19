package net.threetag.palladium.client.renderer.entity.skin;

import com.mojang.datafixers.util.Pair;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.ArrayList;
import java.util.List;

public class PlayerSkinChangeManager {

    public static final List<Pair<Integer, SkinProvider>> PROVIDER = new ArrayList<>();

    public static PlayerSkin getSkinOverride(Avatar avatar, PlayerSkin original) {
        if (PROVIDER.isEmpty()) {
            return null;
        }

        var override = PlayerSkinHandler.get(avatar).getOverriddenSkin(original);
        return override != null ? override.getSkin() : null;
    }

    public static void registerSkinProvider(int priority, SkinProvider provider) {
        PROVIDER.add(Pair.of(priority, provider));
        PROVIDER.sort((p1, p2) -> p2.getFirst() - p1.getFirst());
    }

    public interface SkinProvider {

        PlayerSkinInfo getSkin(Avatar avatar, PlayerSkinInfo previousSkin, PlayerSkinInfo defaultSkin);

    }

    static {
        // Abilities
        registerSkinProvider(30, new AbilitySkinProvider());
        registerSkinProvider(40, new AbilityPlayerSkinProvider());
    }

}
