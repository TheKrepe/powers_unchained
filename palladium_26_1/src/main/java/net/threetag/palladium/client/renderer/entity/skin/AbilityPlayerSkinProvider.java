package net.threetag.palladium.client.renderer.entity.skin;

import net.minecraft.world.entity.Avatar;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.power.ability.AbilitySerializers;
import net.threetag.palladium.power.ability.AbilityUtil;

public class AbilityPlayerSkinProvider implements PlayerSkinChangeManager.SkinProvider {

    @Override
    public PlayerSkinInfo getSkin(Avatar avatar, PlayerSkinInfo previousSkin, PlayerSkinInfo defaultSkin) {
        var abilities = AbilityUtil.getEnabledInstances(avatar, AbilitySerializers.PLAYER_SKIN_CHANGE.get()).stream().sorted((a1, a2) -> a2.getAbility().priority - a1.getAbility().priority).toList();

        if (!abilities.isEmpty()) {
            var ability = abilities.getFirst();
            return PlayerSkinFetcher.getLoadedPlayerSkinInfo(ability.getAbility().username.get(DataContext.forAbility(avatar, ability)));
        }

        return previousSkin;
    }
}
