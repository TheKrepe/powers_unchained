package lucraft.mods.lucraftcore.superpowers;

import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import net.minecraft.util.math.Vec2f;

public interface ISuperpowerAbilityCoordinates {
    Vec2f getDisplayCoordinatesForAbility(Ability ability);
}