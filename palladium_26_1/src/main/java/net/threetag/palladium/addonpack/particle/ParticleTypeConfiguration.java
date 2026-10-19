package net.threetag.palladium.addonpack.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import net.threetag.palladium.util.PalladiumCodecs;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ParticleTypeConfiguration {

    public static final Map<Identifier, ParticleTypeConfiguration> REGISTERED = new ConcurrentHashMap<>();

    public static final Codec<ParticleTypeConfiguration> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("override_limiter", false).forGetter(ParticleTypeConfiguration::overrideLimiter),
            RenderType.CODEC.optionalFieldOf("render_type", RenderType.OPAQUE).forGetter(ParticleTypeConfiguration::renderType),
            TextureType.CODEC.optionalFieldOf("texture_type", TextureType.RANDOM).forGetter(ParticleTypeConfiguration::textureType),
            PalladiumCodecs.TIME.optionalFieldOf("lifetime", 100).forGetter(ParticleTypeConfiguration::lifetime),
            Codec.BOOL.optionalFieldOf("has_physics", true).forGetter(ParticleTypeConfiguration::hasPhysics),
            Codec.FLOAT.optionalFieldOf("gravity", 0F).forGetter(ParticleTypeConfiguration::gravity),
            Codec.FLOAT.optionalFieldOf("friction", 0.98F).forGetter(ParticleTypeConfiguration::friction),
            Codec.FLOAT.optionalFieldOf("quad_size", 0.2F).forGetter(ParticleTypeConfiguration::quadSize),
            Codec.BOOL.optionalFieldOf("fullbright", false).forGetter(ParticleTypeConfiguration::fullbright),
            PalladiumCodecs.COLOR_CODEC.optionalFieldOf("color").forGetter(config -> Optional.ofNullable(config.color()))
    ).apply(instance, ParticleTypeConfiguration::new));

    private final boolean overrideLimiter;
    private final RenderType renderType;
    private final TextureType textureType;
    private final int lifetime;
    private final boolean hasPhysics;
    private final float gravity;
    private final float friction;
    private final float quadSize;
    private final boolean fullbright;
    private final @Nullable Color color;

    private SimpleParticleType particleType;

    public ParticleTypeConfiguration(boolean overrideLimiter, RenderType renderType, TextureType textureType, int lifetime, boolean hasPhysics, float gravity, float friction, float quadSize, boolean fullbright, Optional<Color> color) {
        this.overrideLimiter = overrideLimiter;
        this.renderType = renderType;
        this.textureType = textureType;
        this.lifetime = lifetime;
        this.hasPhysics = hasPhysics;
        this.gravity = gravity;
        this.friction = friction;
        this.quadSize = quadSize;
        this.fullbright = fullbright;
        this.color = color.orElse(null);
    }

    public SimpleParticleType getOrCreateParticleType() {
        if (this.particleType == null) {
            this.particleType = new SimpleParticleType(this.overrideLimiter);
        }
        return this.particleType;
    }

    public boolean overrideLimiter() {
        return this.overrideLimiter;
    }

    public RenderType renderType() {
        return this.renderType;
    }

    public TextureType textureType() {
        return this.textureType;
    }

    public int lifetime() {
        return this.lifetime;
    }

    public boolean hasPhysics() {
        return this.hasPhysics;
    }

    public float gravity() {
        return this.gravity;
    }

    public float friction() {
        return this.friction;
    }

    public float quadSize() {
        return this.quadSize;
    }

    public boolean fullbright() {
        return this.fullbright;
    }

    public @Nullable Color color() {
        return this.color;
    }

    public enum RenderType implements StringRepresentable {

        OPAQUE("opaque"),
        TRANSLUCENT("translucent");

        public static final Codec<RenderType> CODEC = StringRepresentable.fromEnum(RenderType::values);

        private final String name;

        RenderType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }

    public enum TextureType implements StringRepresentable {

        RANDOM("random"),
        AGING("aging");

        public static final Codec<TextureType> CODEC = StringRepresentable.fromEnum(TextureType::values);

        private final String name;

        TextureType(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
