package lucraft.mods.heroesexpansion.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class OpenSpaceStonePortalEvent extends PlayerEvent {

    protected World world;
    protected Vec3d pos;
    protected int destinationDim;
    protected Vec3d destinationPos;
    protected boolean invasion;

    public OpenSpaceStonePortalEvent(EntityPlayer player, World world, Vec3d pos, int destinationDim, Vec3d destinationPos, boolean invasion) {
        super(player);
        this.world = world;
        this.pos = pos;
        this.destinationDim = destinationDim;
        this.destinationPos = destinationPos;
        this.invasion = invasion;
    }

    public World getWorld() {
        return world;
    }

    public Vec3d getPos() {
        return pos;
    }

    public int getDestinationDim() {
        return destinationDim;
    }

    public Vec3d getDestinationPos() {
        return destinationPos;
    }

    public boolean isInvasion() {
        return invasion;
    }
}
