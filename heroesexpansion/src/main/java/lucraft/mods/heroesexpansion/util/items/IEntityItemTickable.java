package lucraft.mods.heroesexpansion.util.items;

import net.minecraft.entity.Entity;
import net.minecraft.world.World;

public interface IEntityItemTickable {

    boolean onEntityItemTick(World world, Entity entity, double x, double y, double z);

}
