package net.threetag.pymtech.client.renderer.item;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.lighting.WorldLightManager;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.item.ShrunkenStructureItem;
import net.threetag.pymtech.network.RequestShrunkenStructureMessage;
import net.threetag.pymtech.storage.ShrunkenStructureData;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ShrunkenStructureItemRenderer extends ItemStackTileEntityRenderer {

    public static List<Integer> requested = Lists.newLinkedList();

    @Override
    public void func_239207_a_(ItemStack stack, ItemCameraTransforms.TransformType transformType, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        ShrunkenStructureData shrunkenStructure = ShrunkenStructureItem.get(Minecraft.getInstance().world, stack);
        matrixStackIn.push();
        matrixStackIn.translate(0.5F, 0, 0.5F);
        float scale = 1F;
        if (shrunkenStructure != null) {
            scale = 1F / Math.max(shrunkenStructure.getSize().getX(), Math.max(shrunkenStructure.getSize().getY(), shrunkenStructure.getSize().getZ()));
        }
        render(shrunkenStructure, matrixStackIn, stack.getOrCreateTag().getInt("ShrunkenStructure"), scale);
        matrixStackIn.pop();
    }

    public static void render(ShrunkenStructureData shrunkenStructure, MatrixStack matrixStack, int id, float scale) {
        if (shrunkenStructure != null) {
            if (!renderStructure(shrunkenStructure, matrixStack, shrunkenStructure.getSize(), scale, Minecraft.getInstance().world)) {
                storeAndRender(matrixStack, Minecraft.getInstance().world, Minecraft.getInstance().player.getPosition(), shrunkenStructure, scale);
            }
        } else if (!requested.contains(id)) {
            PymTech.NETWORK_CHANNEL.sendToServer(new RequestShrunkenStructureMessage(id));
            requested.add(id);
        } else {
            // render placeholder
        }
    }

    public static boolean renderStructure(ShrunkenStructureData shrunkenStructureData, MatrixStack matrixStack, BlockPos size, float scale, World world) {
        if (shrunkenStructureData.renderer == null)
            return false;
        matrixStack.push();
        matrixStack.scale(scale, scale, scale);
        matrixStack.translate(-size.getX() / 2D, 0, -size.getZ() / 2D);
        shrunkenStructureData.renderer.render(matrixStack, world);
        matrixStack.pop();
        return true;
    }

    public static void storeAndRender(MatrixStack matrixStack, World world, BlockPos pos, ShrunkenStructureData shrunkenStructure, float scale) {
        if (shrunkenStructure.renderer == null) {
            shrunkenStructure.renderer = new CachedRender((MatrixStack ms, BufferBuilder buffer, World w, RenderType type) ->
            {
                BlockPos size = shrunkenStructure.getSize();

                for (int x = 0; x < size.getX(); x++) {
                    for (int y = 0; y < size.getY(); y++) {
                        for (int z = 0; z < size.getZ(); z++) {
                            ShrunkenStructureData.BlockData data = shrunkenStructure.getBlockData()[x][y][z];
                            if (data != null && data.getBlockState() != null && data.getBlockState().getRenderType() != BlockRenderType.INVISIBLE) {
                                BlockState state = data.getBlockState();
                                if (state.getRenderType() == BlockRenderType.MODEL && RenderTypeLookup.canRenderInLayer(state, type)) {
                                    final BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
                                    ms.push();
                                    ms.translate(x, y, z);
                                    BlockPos p = new BlockPos(x, y, z);
                                    blockrendererdispatcher.getBlockModelRenderer().renderModel(
                                            new ShrunkenStructureAccess(w, pos, shrunkenStructure),
                                            blockrendererdispatcher.getModelForState(state),
                                            state,
                                            p,
                                            ms,
                                            buffer,
                                            true,
                                            new Random(),
                                            0,
                                            OverlayTexture.NO_OVERLAY,
                                            EmptyModelData.INSTANCE);
                                    ms.pop();
                                }
                            }
                        }
                    }
                }
            });
        } else {
            renderStructure(shrunkenStructure, matrixStack, shrunkenStructure.getSize(), scale, world);
        }
    }


    public static class ShrunkenStructureAccess implements IBlockDisplayReader {

        protected World world;
        protected BlockPos pos;
        protected ShrunkenStructureData shrunkenStructure;
        protected int volume;

        public ShrunkenStructureAccess(World world, BlockPos pos, ShrunkenStructureData shrunkenStructure) {
            this.world = world;
            this.pos = pos;
            this.shrunkenStructure = shrunkenStructure;
            this.volume = shrunkenStructure.getSize().getX() * shrunkenStructure.getSize().getY() * shrunkenStructure.getSize().getZ();
        }

        @Override
        public float func_230487_a_(Direction p_230487_1_, boolean p_230487_2_) {
            return 1F;
        }

        @Override
        public WorldLightManager getLightManager() {
            return this.world.getLightManager();
        }

        @Override
        public int getBlockColor(BlockPos blockPosIn, ColorResolver colorResolverIn) {
            return Minecraft.getInstance().world.getBlockColor(Minecraft.getInstance().player.getPosition(), colorResolverIn);
        }

        @Override
        public int getLightFor(LightType lightType, BlockPos blockPos) {
            return this.world.getLightFor(lightType, this.pos);
        }

        @Nullable
        @Override
        public TileEntity getTileEntity(BlockPos blockPos) {
            return null;
        }

        @Override
        public BlockState getBlockState(BlockPos blockPos) {
            if (pos.getX() < 0 || pos.getY() < 0 || pos.getZ() < 0 || pos.getX() > shrunkenStructure.getSize().getX() - 1 || pos.getY() > shrunkenStructure.getSize().getY() - 1 || pos.getZ() > shrunkenStructure.getSize().getZ() - 1)
                return Blocks.AIR.getDefaultState();
            ShrunkenStructureData.BlockData data = this.shrunkenStructure.getBlockData()[pos.getX()][pos.getY()][pos.getZ()];
            return data == null ? Blocks.AIR.getDefaultState() : data.getBlockState();
        }

        @Override
        public FluidState getFluidState(BlockPos pos) {
            return Fluids.EMPTY.getDefaultState();
        }

    }

    public static class CachedRender {

        private final RenderInner render;

        private Map<RenderType, VertexBuffer> buffers = Maps.newHashMap();

        public CachedRender(RenderInner render) {
            this.render = render;
            this.buffers = Maps.newHashMap();
        }

        private void cache(MatrixStack matrixStack, World world) {
            this.reset();


            for (RenderType type : this.buffers.keySet()) {
                BufferBuilder builder = Tessellator.getInstance().getBuffer();
                builder.begin(type.getDrawMode(), type.getVertexFormat());
                this.render.render(new MatrixStack(), builder, world, type);
                builder.finishDrawing();
                this.buffers.get(type).upload(builder);
            }
        }

        public void render(MatrixStack matrixStack, World world) {
            if (this.buffers.isEmpty()) {
                this.cache(matrixStack, world);
            }

            Minecraft.getInstance().getTextureManager().bindTexture(PlayerContainer.LOCATION_BLOCKS_TEXTURE);
            for (RenderType type : this.buffers.keySet()) {
                type.setupRenderState();
                VertexBuffer buffer = this.buffers.get(type);
                buffer.bindBuffer();
                type.getVertexFormat().setupBufferState(0L);
                buffer.draw(matrixStack.getLast().getMatrix(), type.getDrawMode());
                VertexBuffer.unbindBuffer();
                type.getVertexFormat().clearBufferState();
                type.clearRenderState();
            }
        }

        public void reset() {
            this.buffers.forEach(((renderType, vertexBuffer) -> vertexBuffer.close()));
            this.buffers.clear();
            RenderType.getBlockRenderTypes().forEach(type -> buffers.put(type, new VertexBuffer(type.getVertexFormat())));
        }

    }


    public interface RenderInner {
        void render(MatrixStack matrixStack, BufferBuilder buffer, World world, RenderType type);
    }


}
