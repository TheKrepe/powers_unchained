package net.threetag.pymtech.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.container.RegulatorTier1Container;

public class RegulatorTier1Screen extends ContainerScreen<RegulatorTier1Container> {

    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(PymTech.MODID, "textures/gui/container/regulator_tier1.png");

    public RegulatorTier1Screen(RegulatorTier1Container screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.xSize = 200;
        this.ySize = 156;
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack stack, int mouseX, int mouseY) {
        this.font.func_243248_b(stack, this.playerInventory.getDisplayName(), 19.0F, (float) (this.ySize - 96 + 2), 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(GUI_TEXTURES);
        int left = this.guiLeft;
        int top = this.guiTop;
        this.blit(stack, left, top, 0, 0, this.xSize, this.ySize);
    }
}
