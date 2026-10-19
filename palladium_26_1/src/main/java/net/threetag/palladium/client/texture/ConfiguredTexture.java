package net.threetag.palladium.client.texture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.threetag.palladium.client.texture.transformer.TextureTransformer;
import net.threetag.palladium.client.texture.transformer.TransformedTexture;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.logic.value.Value;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class ConfiguredTexture extends DynamicTexture {

    public static final MapCodec<ConfiguredTexture> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("base").forGetter(t -> t.base),
            Codec.unboundedMap(Codec.STRING, Value.CODEC).optionalFieldOf("variables", Collections.emptyMap()).forGetter(t -> t.variables),
            ExtraCodecs.compactListCodec(TextureTransformer.CODEC).optionalFieldOf("transformers",Collections.emptyList()).forGetter(t -> t.transformers),
            Codec.STRING.optionalFieldOf("output").forGetter(t -> Optional.ofNullable(t.output))
    ).apply(instance, (base, variables, transformers, output) -> new ConfiguredTexture(base, variables, transformers, output.orElse(null))));

    private final String base;
    protected final Map<String, Value> variables;
    protected final List<TextureTransformer> transformers;
    @Nullable
    private final String output;
    public final String rawOutputPath;

    public ConfiguredTexture(String base, Map<String, Value> variables, List<TextureTransformer> transformers) {
        this(base, variables, transformers, null);
    }

    public ConfiguredTexture(String base, Map<String, Value> variables, List<TextureTransformer> transformers, @Nullable String output) {
        this.base = base;
        this.variables = variables;
        this.transformers = transformers;
        this.output = output != null && !output.isBlank() ? output : null;

        this.rawOutputPath = this.output != null ? this.output : defaultOutputPath(this.base, this.variables);
    }

    @Override
    public Identifier getTexture(DataContext context) {
        if (this.variables.isEmpty() && this.transformers.isEmpty()) {
            return Identifier.parse(this.base);
        }

        if (this.transformers.isEmpty()) {
            return Identifier.parse(replaceVariables(this.base, context, this.variables));
        }

        return registerTransformed(this.rawOutputPath, () -> Identifier.parse(replaceVariables(this.base, context, this.variables)), this.transformers, context, this.variables);
    }

    public static String defaultOutputPath(String base, Map<String, Value> variables) {
        if (variables.isEmpty()) {
            return base + "_modified";
        }

        StringBuilder path = new StringBuilder(base);

        for (String var : variables.keySet()) {
            if (!path.toString().contains("#" + var)) {
                path.append("_#").append(var);
            }
        }

        return path.toString();
    }

    public static Identifier registerTransformed(String rawOutputPath, Supplier<Identifier> texture, List<TextureTransformer> transformers, DataContext context, Map<String, Value> variables) {
        Identifier output = Identifier.parse(replaceVariables(rawOutputPath, context, variables));

        if (!Minecraft.getInstance().getTextureManager().byPath.containsKey(output)) {
            Minecraft.getInstance().getTextureManager().registerAndLoad(output, new TransformedTexture(output, texture.get(), transformers, transformerPath -> replaceVariables(transformerPath, context, variables)));
        }

        return output;
    }

    public static String replaceVariables(String base, DataContext context, Map<String, Value> variables) {
        for (Map.Entry<String, Value> entry : variables.entrySet()) {
            Value variable = entry.getValue();
            base = variable.replace(base, entry.getKey(), context);
        }

        return base;
    }

    @Override
    public DynamicTextureSerializer<?> getSerializer() {
        return DynamicTextureSerializers.CONFIGURED;
    }

    public static class Serializer extends DynamicTextureSerializer<ConfiguredTexture> {

        @Override
        public MapCodec<ConfiguredTexture> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<DynamicTexture, ConfiguredTexture> builder, HolderLookup.Provider provider) {
            builder.setName("Configured Texture").setDescription("The texture type used when no type is given. With only variables it swaps between existing texture files, with transformers it builds a new texture in memory from the base image.")
                    .add("base", TYPE_IDENTIFIER, "The base texture, as a full texture path including \"textures/\" and \".png\". Can use variables.")
                    .addOptional("variables", TYPE_MAP_VARIABLES, "A map of variables that can be used in the texture path.")
                    .addOptional("transformers", SettingType.listOrPrimitive("Texture Transformer"), "Texture transformers applied to the base image, in the order they are listed.")
                    .addOptional("output", TYPE_IDENTIFIER, "Path the generated texture is registered under. Supports variables. If not specified, it is derived from the base path and the used variables.");
        }
    }
}
