package net.threetag.palladium.client.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.client.texture.transformer.TextureTransformer;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.Value;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class EntityTexture extends DynamicTexture {

    public static final MapCodec<EntityTexture> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, Value.CODEC).optionalFieldOf("variables", Collections.emptyMap()).forGetter(t -> t.variables),
            ExtraCodecs.compactListCodec(TextureTransformer.CODEC).optionalFieldOf("transformers", Collections.emptyList()).forGetter(t -> t.transformers),
            Codec.STRING.optionalFieldOf("output").forGetter(t -> Optional.ofNullable(t.output))
    ).apply(instance, (variables, transformers, output) -> new EntityTexture(variables, transformers, output.orElse(null))));

    private static boolean RESOLVING = false;

    private final Map<String, Value> variables;
    private final List<TextureTransformer> transformers;
    @Nullable
    private final String output;

    public EntityTexture(Map<String, Value> variables, List<TextureTransformer> transformers, @Nullable String output) {
        this.variables = variables;
        this.transformers = transformers;
        this.output = output != null && !output.isBlank() ? output : null;
    }

    @Override
    public Identifier getTexture(DataContext context) {
        if (!(context.getEntity() instanceof LivingEntity living)) {
            return null;
        }

        Identifier texture = getEntityTexture(living);

        if (texture == null || this.transformers.isEmpty()) {
            return texture;
        }

        String rawOutputPath = this.output != null ? this.output : ConfiguredTexture.defaultOutputPath(texture.toString(), this.variables);
        return ConfiguredTexture.registerTransformed(rawOutputPath, () -> texture, this.transformers, context, this.variables);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Nullable
    private static Identifier getEntityTexture(LivingEntity entity) {
        if (entity instanceof ClientAvatarEntity avatar) {
            return avatar.getSkin().body().id();
        }

        if (RESOLVING) {
            return null;
        }

        var renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(entity);

        if (!(renderer instanceof LivingEntityRenderer livingEntityRenderer)) {
            return null;
        }

        RESOLVING = true;

        try {
            return livingEntityRenderer.getTextureLocation((LivingEntityRenderState) livingEntityRenderer.createRenderState(entity, 1F));
        } finally {
            RESOLVING = false;
        }
    }

    @Override
    public DynamicTextureSerializer<?> getSerializer() {
        return DynamicTextureSerializers.ENTITY;
    }

    public static class Serializer extends DynamicTextureSerializer<EntityTexture> {

        @Override
        public MapCodec<EntityTexture> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DynamicTexture, EntityTexture> builder, HolderLookup.Provider provider) {
            builder.setName("Entity Texture").setDescription("Starts from the texture the entity is currently rendered with. For players that is their skin, including skins set by abilities or customizations. On its own it is rarely useful; it exists to run transformers on the skin, for example to paint a suit pattern onto it.")
                    .addOptional("variables", TYPE_MAP_VARIABLES, "A map of variables that can be used in the output path and transformer paths.")
                    .addOptional("transformers", SettingType.listOrPrimitive("Texture Transformer"), "Texture transformers applied to the entity texture, in the order they are listed.")
                    .addOptional("output", TYPE_IDENTIFIER, "Path the generated texture is registered under. Supports variables. If not specified, it is derived from the entity texture path and the used variables.")
                    .addExampleObject(new EntityTexture(Collections.emptyMap(), Collections.emptyList(), null));
        }
    }
}
