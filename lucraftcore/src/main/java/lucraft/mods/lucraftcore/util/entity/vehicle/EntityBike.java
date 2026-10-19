package lucraft.mods.lucraftcore.util.entity.vehicle;

import net.minecraft.world.World;

public abstract class EntityBike extends EntityLandVehicle {

    public float prevLeanAngle;
    public float leanAngle;

    public EntityBike(World worldIn) {
        super(worldIn);
    }

    public EntityBike(World worldIn, double posX, double posY, double posZ) {
        super(worldIn, posX, posY, posZ);
        this.setPosition(posX, posY, posZ);
    }

    @Override
    public void onEntityUpdate() {
        this.prevLeanAngle = this.leanAngle;
        super.onEntityUpdate();
        this.leanAngle = this.turnAngle / getMaxTurnAngle();
    }
}
