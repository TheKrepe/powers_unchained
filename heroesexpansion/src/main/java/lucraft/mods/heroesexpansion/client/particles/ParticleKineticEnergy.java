package lucraft.mods.heroesexpansion.client.particles;

import net.minecraft.client.particle.IParticleFactory;
import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

@SideOnly(Side.CLIENT)
public class ParticleKineticEnergy extends Particle {

    public static final int ID = 2510;

    @SideOnly(Side.CLIENT)
    public static class Factory implements IParticleFactory {

        @Nullable
        @Override
        public Particle createParticle(int particleID, World worldIn, double xCoordIn, double yCoordIn, double zCoordIn, double xSpeedIn, double ySpeedIn, double zSpeedIn, int... args) {
            return new ParticleKineticEnergy(worldIn, xCoordIn, yCoordIn, zCoordIn, xSpeedIn, ySpeedIn, zSpeedIn, args[0], args[1], args[2]);
        }

    }

    protected ParticleKineticEnergy(World worldIn, double posXIn, double posYIn, double posZIn, double xSpeed, double ySpeed, double zSpeed, double red, double green, double blue) {
        super(worldIn, posXIn, posYIn, posZIn);
        this.motionX = xSpeed;
        this.motionY = ySpeed;
        this.motionZ = zSpeed;
        this.particleRed = (float) red / 255F;
        this.particleGreen = (float) green / 255F;
        this.particleBlue = (float) blue / 255F;
        this.particleMaxAge = 48 + this.rand.nextInt(12);
        this.canCollide = false;
        this.nextTextureIndexX();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        this.particleAlpha = 1F - ((float) this.particleAge / (float) this.particleMaxAge);
    }
}
