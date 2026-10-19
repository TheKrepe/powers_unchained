package net.threetag.palladium.client.model.item;

import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.dispatch.BlockModelRotation;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvedModel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.geometry.QuadCollection;
import net.minecraft.client.resources.model.sprite.TextureSlots;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.item.multiverse.MultiversalItemVariant;
import org.joml.Matrix4fc;
import org.jspecify.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

public class MultiversalVariantItemModel implements ItemModel {

    private final Matrix4fc transformation;
    private final List<ItemTintSource> tints;
    private final BakingContext bakingContext;

    public MultiversalVariantItemModel(Matrix4fc transformation, List<ItemTintSource> tints, BakingContext bakingContext) {
        this.transformation = transformation;
        this.tints = tints;
        this.bakingContext = bakingContext;
    }

    @Override
    public void update(ItemStackRenderState output, ItemStack item, ItemModelResolver resolver, ItemDisplayContext displayContext, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed) {
        output.appendModelIdentityElement(this);
        Identifier modelId = resolveModelId(item, level);
        if (modelId != null) {
            output.appendModelIdentityElement(modelId);
            this.createModel(modelId).update(output, item, resolver, displayContext, level, owner, seed);
        } else {
            this.bakingContext.missingItemModel().update(output, item, resolver, displayContext, level, owner, seed);
        }
    }

    @Nullable
    private static Identifier resolveModelId(ItemStack item, @Nullable ClientLevel level) {
        if (level == null) {
            return null;
        }
        MultiversalItemVariant.Appearance appearance = MultiversalItemVariant.resolve(level.registryAccess(), item);
        return appearance == null ? null : appearance.itemModel().map(id -> id.withPrefix(MultiversalVariantModels.MODEL_PREFIX)).orElse(null);
    }

    private ItemModel createModel(Identifier variantId) {
        ModelBaker baker = this.bakingContext.blockModelBaker();
        ResolvedModel resolvedModel = baker.getModel(variantId);
        TextureSlots textureSlots = resolvedModel.getTopTextureSlots();
        QuadCollection quads = resolvedModel.bakeTopGeometry(textureSlots, baker, BlockModelRotation.IDENTITY);
        ModelRenderProperties properties = ModelRenderProperties.fromResolvedModel(baker, resolvedModel, textureSlots);
        validateAtlasUsage(quads.getAll());
        return new CuboidItemModelWrapper(this.tints, quads, properties, this.transformation);
    }

    private static void validateAtlasUsage(List<BakedQuad> quads) {
        Iterator<BakedQuad> quadIterator = quads.iterator();
        if (quadIterator.hasNext()) {
            Identifier expectedAtlas = quadIterator.next().materialInfo().sprite().atlasLocation();

            while (quadIterator.hasNext()) {
                BakedQuad quad = quadIterator.next();
                Identifier quadAtlas = quad.materialInfo().sprite().atlasLocation();
                if (!quadAtlas.equals(expectedAtlas)) {
                    throw new IllegalStateException("Multiple atlases used in model, expected " + expectedAtlas + ", but also got " + quadAtlas);
                }
            }

            if (!expectedAtlas.equals(TextureAtlas.LOCATION_ITEMS) && !expectedAtlas.equals(TextureAtlas.LOCATION_BLOCKS)) {
                throw new IllegalArgumentException("Atlas " + expectedAtlas + " can't be usef for item models");
            }
        }
    }

    public record Unbaked(Optional<Transformation> transformation,
                          List<ItemTintSource> tints) implements ItemModel.Unbaked {

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Transformation.EXTENDED_CODEC.optionalFieldOf("transformation").forGetter(Unbaked::transformation),
                ItemTintSources.CODEC.listOf().optionalFieldOf("tints", List.of()).forGetter(Unbaked::tints)
        ).apply(i, Unbaked::new));

        @Override
        public MapCodec<? extends ItemModel.Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public ItemModel bake(BakingContext context, Matrix4fc transformation) {
            Matrix4fc modelTransform = Transformation.compose(transformation, this.transformation);
            return new MultiversalVariantItemModel(modelTransform, this.tints, context);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {

        }
    }

}
