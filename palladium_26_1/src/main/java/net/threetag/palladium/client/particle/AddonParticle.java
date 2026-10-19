package net.threetag.palladium.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.threetag.palladium.addonpack.particle.ParticleTypeConfiguration;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class AddonParticle extends SingleQuadParticle {

    private static final int FULL_BRIGHT = 0xF000F0;

    private final ParticleTypeConfiguration config;
    private final SpriteSet spriteSet;

    protected AddonParticle(ClientLevel level, SpriteSet spriteSet, ParticleTypeConfiguration config, double x, double y, double z, double xd, double yd, double zd, RandomSource random) {
        super(level, x, y, z, xd, yd, zd, config.textureType() == ParticleTypeConfiguration.TextureType.AGING ? spriteSet.first() : spriteSet.get(random));
        this.spriteSet = spriteSet;
        this.config = config;

        this.setParticleSpeed(xd, yd, zd);
        this.lifetime = config.lifetime();
        this.hasPhysics = config.hasPhysics();
        this.gravity = config.gravity();
        this.friction = config.friction();
        this.quadSize = config.quadSize();

        Color color = config.color();
        if (color != null) {
            this.setColor(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.config.textureType() == ParticleTypeConfiguration.TextureType.AGING) {
            this.setSpriteFromAge(this.spriteSet);
        }
    }

    @Override
    protected Layer getLayer() {
        return this.config.renderType() == ParticleTypeConfiguration.RenderType.TRANSLUCENT ? Layer.TRANSLUCENT : Layer.OPAQUE;
    }

    @Override
    public int getLightCoords(float partialTick) {
        return this.config.fullbright() ? FULL_BRIGHT : super.getLightCoords(partialTick);
    }

    public static class Provider implements ParticleProvider<SimpleParticleType> {

        private final ParticleTypeConfiguration config;
        private final SpriteSet spriteSet;

        public Provider(ParticleTypeConfiguration config, SpriteSet spriteSet) {
            this.config = config;
            this.spriteSet = spriteSet;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
            return new AddonParticle(level, this.spriteSet, this.config, x, y, z, xSpeed, ySpeed, zSpeed, random);
        }
    }
}
