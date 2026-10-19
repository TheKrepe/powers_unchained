package net.threetag.pymtech.client.renderer.tileentity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.threetag.pymtech.tileentity.StructureShrinkerTileEntity;
import net.threetag.threecore.client.renderer.tileentity.FastFluidTESR;
import net.threetag.threecore.util.RenderUtil;

import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class StructureShrinkerTileEntityRenderer extends FastFluidTESR<StructureShrinkerTileEntity> {

    public StructureShrinkerTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcher) {
        super(rendererDispatcher);
    }

    @Override
    public void render(StructureShrinkerTileEntity te, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, int combinedLightIn, int combinedOverlayIn) {
        super.render(te, partialTicks, matrixStack, renderTypeBuffer, combinedLightIn, combinedOverlayIn);

        AxisAlignedBB box = te.getSelectionRelative();
        float a = 0.15F + (MathHelper.sin((Minecraft.getInstance().player.ticksExisted + partialTicks) / 10F) + 1F) / 8F;
        RenderUtil.renderFilledBox(matrixStack.getLast().getMatrix(), renderTypeBuffer.getBuffer(RenderUtil.RenderTypes.HYDRAULIC_PRESS_PISTONS), (float)box.minX, (float)box.minY,(float)box.minZ, (float)box.maxX + 1, (float)box.maxY + 1, (float)box.maxZ + 1, 1F, 0F, 0F, a, combinedLightIn);
        WorldRenderer.drawBoundingBox(matrixStack, renderTypeBuffer.getBuffer(RenderType.getLines()), box.minX, box.minY, box.minZ, box.maxX + 1, box.maxY + 1, box.maxZ + 1, 0F, 0F, 0F, 1F);
    }

    @Override
    public List<TankRenderInfo> getTanksToRender(StructureShrinkerTileEntity te) {
        return Collections.singletonList(new TankRenderInfo(te.fluidTank,
                new AxisAlignedBB(2F / 16F, 8F / 16F, 2F / 16F, 14F / 16F, 15F / 16F, 14F / 16F),
                Direction.UP));
    }
}
