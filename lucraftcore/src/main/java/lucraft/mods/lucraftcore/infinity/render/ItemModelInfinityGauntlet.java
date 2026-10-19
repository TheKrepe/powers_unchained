package lucraft.mods.lucraftcore.infinity.render;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.infinity.EnumInfinityStone;
import lucraft.mods.lucraftcore.infinity.items.InventoryInfinityGauntlet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.*;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class ItemModelInfinityGauntlet implements IModel {

    public static final ModelResourceLocation LOCATION = new ModelResourceLocation(new ResourceLocation(LucraftCore.MODID, "infinity_gauntlet_state"), "inventory");
    public static final ModelResourceLocation LOCATION_3D = new ModelResourceLocation(new ResourceLocation(LucraftCore.MODID, "infinity_gauntlet_3d"), "inventory");

    public static final IModel MODEL = new ItemModelInfinityGauntlet();

    private final ResourceLocation baseLocation;
    private final ResourceLocation spaceLocation;
    private final ResourceLocation realityLocation;
    private final ResourceLocation powerLocation;
    private final ResourceLocation soulLocation;
    private final ResourceLocation mindLocation;
    private final ResourceLocation timeLocation;
    private final boolean[] stones;

    public ItemModelInfinityGauntlet() {
        this(null, null, null, null, null, null, null);
    }

    public ItemModelInfinityGauntlet(ResourceLocation baseLocation, ResourceLocation spaceLocation, ResourceLocation realityLocation, ResourceLocation powerLocation, ResourceLocation soulLocation, ResourceLocation mindLocation, ResourceLocation timeLocation, boolean[] stones) {
        this.baseLocation = baseLocation;
        this.spaceLocation = spaceLocation;
        this.realityLocation = realityLocation;
        this.powerLocation = powerLocation;
        this.soulLocation = soulLocation;
        this.mindLocation = mindLocation;
        this.timeLocation = timeLocation;
        this.stones = stones;
    }

    public ItemModelInfinityGauntlet(ResourceLocation baseLocation, ResourceLocation spaceLocation, ResourceLocation realityLocation, ResourceLocation powerLocation, ResourceLocation soulLocation, ResourceLocation mindLocation, ResourceLocation timeLocation, EnumInfinityStone... stones) {
        this.baseLocation = baseLocation;
        this.spaceLocation = spaceLocation;
        this.realityLocation = realityLocation;
        this.powerLocation = powerLocation;
        this.soulLocation = soulLocation;
        this.mindLocation = mindLocation;
        this.timeLocation = timeLocation;
        this.stones = new boolean[EnumInfinityStone.values().length];
        for (EnumInfinityStone types : stones)
            this.stones[types.ordinal()] = true;
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();

        if (baseLocation != null)
            builder.add(baseLocation);
        if (spaceLocation != null)
            builder.add(spaceLocation);
        if (realityLocation != null)
            builder.add(realityLocation);
        if (powerLocation != null)
            builder.add(powerLocation);
        if (soulLocation != null)
            builder.add(soulLocation);
        if (mindLocation != null)
            builder.add(mindLocation);
        if (timeLocation != null)
            builder.add(timeLocation);

        return builder.build();
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transformMap = PerspectiveMapWrapper.getTransforms(state);

        TRSRTransformation transform = state.apply(Optional.empty()).orElse(TRSRTransformation.identity());
        TextureAtlasSprite particleSprite = null;
        ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();

        if (baseLocation != null) {
            IBakedModel model = (new ItemLayerModel(ImmutableList.of(baseLocation))).bake(state, format, bakedTextureGetter);
            builder.addAll(model.getQuads(null, null, 0));
        }

        if (spaceLocation != null && stones[EnumInfinityStone.SPACE.ordinal()]) {
            IBakedModel model = (new ItemLayerModel(ImmutableList.of(spaceLocation))).bake(state, format, bakedTextureGetter);
            builder.addAll(model.getQuads(null, null, 0));
        }

        if (realityLocation != null && stones[EnumInfinityStone.REALITY.ordinal()]) {
            IBakedModel model = (new ItemLayerModel(ImmutableList.of(realityLocation))).bake(state, format, bakedTextureGetter);
            builder.addAll(model.getQuads(null, null, 0));
        }

        if (powerLocation != null && stones[EnumInfinityStone.POWER.ordinal()]) {
            IBakedModel model = (new ItemLayerModel(ImmutableList.of(powerLocation))).bake(state, format, bakedTextureGetter);
            builder.addAll(model.getQuads(null, null, 0));
        }

        if (soulLocation != null && stones[EnumInfinityStone.SOUL.ordinal()]) {
            IBakedModel model = (new ItemLayerModel(ImmutableList.of(soulLocation))).bake(state, format, bakedTextureGetter);
            builder.addAll(model.getQuads(null, null, 0));
        }

        if (mindLocation != null && stones[EnumInfinityStone.MIND.ordinal()]) {
            IBakedModel model = (new ItemLayerModel(ImmutableList.of(mindLocation))).bake(state, format, bakedTextureGetter);
            builder.addAll(model.getQuads(null, null, 0));
        }

        if (timeLocation != null && stones[EnumInfinityStone.TIME.ordinal()]) {
            IBakedModel model = (new ItemLayerModel(ImmutableList.of(timeLocation))).bake(state, format, bakedTextureGetter);
            builder.addAll(model.getQuads(null, null, 0));
        }

        return new BakedInfinityGauntletModel(this, builder.build(), particleSprite, format, Maps.immutableEnumMap(transformMap), Maps.newHashMap(), transform.isIdentity());
    }

    @Override
    public ItemModelInfinityGauntlet process(ImmutableMap<String, String> customData) {
        boolean[] stones = new boolean[EnumInfinityStone.values().length];
        for (EnumInfinityStone types : EnumInfinityStone.values()) {
            if (customData.containsKey(types.toString().toLowerCase())) {
                stones[types.ordinal()] = customData.get(types.toString().toLowerCase()).equals("true");
            }
        }
        return new ItemModelInfinityGauntlet(baseLocation, spaceLocation, realityLocation, powerLocation, soulLocation, mindLocation, timeLocation, stones);
    }

    @Override
    public ItemModelInfinityGauntlet retexture(ImmutableMap<String, String> textures) {
        ResourceLocation base = baseLocation;
        ResourceLocation space = spaceLocation;
        ResourceLocation reality = realityLocation;
        ResourceLocation power = powerLocation;
        ResourceLocation soul = soulLocation;
        ResourceLocation mind = mindLocation;
        ResourceLocation time = timeLocation;

        if (textures.containsKey("base"))
            base = new ResourceLocation(textures.get("base"));
        if (textures.containsKey("space"))
            space = new ResourceLocation(textures.get("space"));
        if (textures.containsKey("reality"))
            reality = new ResourceLocation(textures.get("reality"));
        if (textures.containsKey("power"))
            power = new ResourceLocation(textures.get("power"));
        if (textures.containsKey("soul"))
            soul = new ResourceLocation(textures.get("soul"));
        if (textures.containsKey("mind"))
            mind = new ResourceLocation(textures.get("mind"));
        if (textures.containsKey("time"))
            time = new ResourceLocation(textures.get("time"));

        return new ItemModelInfinityGauntlet(base, space, reality, power, soul, mind, time, this.stones);
    }

    public enum LoaderInfinityGauntlet implements ICustomModelLoader {

        INSTANCE;

        @Override
        public boolean accepts(ResourceLocation modelLocation) {
            return modelLocation.getNamespace().equals(LucraftCore.MODID) && modelLocation.getPath().contains("infinity_gauntlet_model");
        }

        @Override
        public IModel loadModel(ResourceLocation modelLocation) {
            return MODEL;
        }

        @Override
        public void onResourceManagerReload(IResourceManager resourceManager) {

        }
    }

    private static final class BakedInfinityGauntletOverrideHandler extends ItemOverrideList {

        public static final BakedInfinityGauntletOverrideHandler INSTANCE = new BakedInfinityGauntletOverrideHandler();

        private BakedInfinityGauntletOverrideHandler() {
            super(ImmutableList.of());
        }

        @Override
        public IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, @Nullable World world, @Nullable EntityLivingBase entity) {
            if (!stack.hasTagCompound())
                return originalModel;
            InventoryInfinityGauntlet inv = new InventoryInfinityGauntlet(stack);
            Map<String, String> stones = Maps.newHashMap();
            StringBuilder key = new StringBuilder();

            for (int i = 0; i < InventoryInfinityGauntlet.SLOTS.length; i++) {
                EnumInfinityStone type = InventoryInfinityGauntlet.SLOTS[i];
                if (!inv.getStackInSlot(i).isEmpty()) {
                    stones.put(type.toString().toLowerCase(), "true");
                    key.append(type.toString()).append(',');
                }
            }

            BakedInfinityGauntletModel model = (BakedInfinityGauntletModel) originalModel;

            if (!model.cache.containsKey(key.toString())) {
                IModel parent = model.parent.process(ImmutableMap.copyOf(stones));
                Function<ResourceLocation, TextureAtlasSprite> textureGetter;
                textureGetter = location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());

                IBakedModel bakedModel = parent.bake(new SimpleModelState(model.transforms_), model.format, textureGetter);
                model.cache.put(key.toString(), bakedModel);
                return bakedModel;
            }

            return model.cache.get(key.toString());
        }
    }

    public static final class BakedInfinityGauntletModel extends BakedItemModel {

        private final ItemModelInfinityGauntlet parent;
        private final Map<String, IBakedModel> cache;
        private final VertexFormat format;
        public final ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms_;
        public static IBakedModel model3d;

        BakedInfinityGauntletModel(ItemModelInfinityGauntlet parent,
                                   ImmutableList<BakedQuad> quads,
                                   TextureAtlasSprite particle,
                                   VertexFormat format,
                                   ImmutableMap<ItemCameraTransforms.TransformType, TRSRTransformation> transforms,
                                   Map<String, IBakedModel> cache,
                                   boolean untransformed) {
            super(quads, particle, transforms, BakedInfinityGauntletOverrideHandler.INSTANCE, untransformed);
            this.format = format;
            this.parent = parent;
            this.cache = cache;
            this.transforms_ = transforms;
        }

        @Override
        public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType type) {
            if (model3d == null || type == ItemCameraTransforms.TransformType.GUI || type == ItemCameraTransforms.TransformType.FIXED)
                return super.handlePerspective(type);
            return model3d.handlePerspective(type);
        }
    }

}
