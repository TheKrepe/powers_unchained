package net.threetag.pymtech.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.client.model.data.EmptyModelData;
import net.minecraftforge.fml.client.gui.widget.Slider;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.ability.RegulatorTier2Ability;
import net.threetag.pymtech.container.AbstractAdvancedRegulatorContainer;
import net.threetag.pymtech.network.SetRegulatedSizeMessage;
import net.threetag.threecore.capability.CapabilitySizeChanging;
import net.threetag.threecore.util.MathUtil;
import org.lwjgl.opengl.GL11;

public abstract class AbstractAdvancedRegulatorScreen<T extends AbstractAdvancedRegulatorContainer> extends ContainerScreen<T> {

    private static final ResourceLocation RED_TEXTURES = new ResourceLocation(PymTech.MODID, "textures/gui/container/regulator_background_red.png");
    private static final ResourceLocation BLUE_TEXTURES = new ResourceLocation(PymTech.MODID, "textures/gui/container/regulator_background_blue.png");

    private Slider slider;

    public AbstractAdvancedRegulatorScreen(T screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
        this.xSize = 256;
        this.ySize = 237;
    }

    public abstract ResourceLocation getGuiTextures();

    @Override
    protected void init() {
        super.init();
        float size = this.container.ability.get(RegulatorTier2Ability.REGULATED_SIZE);
        this.addButton(this.slider = new SizeSlider(this.guiLeft + 37, this.guiTop + 118, 182, 20, StringTextComponent.EMPTY, StringTextComponent.EMPTY, this.container.ability.get(RegulatorTier2Ability.MIN), this.container.ability.get(RegulatorTier2Ability.MAX), size, true, true, this));
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStack, float partialTicks, int mouseX, int mouseY) {
        int left = this.guiLeft;
        int top = this.guiTop;

        RenderSystem.pushMatrix();
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        float f = (float) (this.slider.getValue() / this.slider.maxValue);
        RenderSystem.color4f(1F, 1F, 1F, f);
        this.minecraft.getTextureManager().bindTexture(BLUE_TEXTURES);
        this.blit(matrixStack, left, top, 0, 0, this.xSize, this.ySize);
        RenderSystem.color4f(1F, 1F, 1F, 1F - f);
        this.minecraft.getTextureManager().bindTexture(RED_TEXTURES);
        this.blit(matrixStack, left, top, 0, 0, this.xSize, this.ySize);
        RenderSystem.disableBlend();

        RenderSystem.color4f(1F, 1F, 1F, 1F);
        this.minecraft.getTextureManager().bindTexture(getGuiTextures());
        this.blit(matrixStack, left, top, 0, 0, this.xSize, this.ySize);

        // Player
        float cachedSize = this.minecraft.player.getCapability(CapabilitySizeChanging.SIZE_CHANGING).orElse(null).getScale();
        this.minecraft.player.getCapability(CapabilitySizeChanging.SIZE_CHANGING).orElse(null).setSizeDirectly(null, 1F);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(guiLeft + this.xSize / 2F + 30, guiTop + 110, 0);
        float playerScale = (float) (MathHelper.clamp(this.slider.getValue(), 0F, 1F) * 10);
        RenderSystem.scalef(playerScale, playerScale, playerScale);
        InventoryScreen.drawEntityOnScreen(0, 0, 4, 1, 1, this.minecraft.player);
        RenderSystem.popMatrix();
        this.minecraft.player.getCapability(CapabilitySizeChanging.SIZE_CHANGING).orElse(null).setSizeDirectly(null, cachedSize);

        // Block
        RenderSystem.pushMatrix();
        RenderSystem.translatef(guiLeft + this.xSize / 2F - 30, guiTop + 112, 100.0F + Minecraft.getInstance().getItemRenderer().zLevel);
        float blockScale = (float) ((this.slider.getValue() <= 1F ? 1F : (-0.125F) * this.slider.getValue() + 1.125F) * 40);
        RenderSystem.scalef(blockScale, blockScale, blockScale);
        RenderHelper.enableStandardItemLighting();
        RenderSystem.rotatef(-10, 0, 1, 0);
        RenderSystem.rotatef(180, 1, 0, 0);
        RenderSystem.translatef(-0.5F, 0, 0);
        BlockRendererDispatcher blockRendererDispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
        IRenderTypeBuffer.Impl renderTypeBuffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
        blockRendererDispatcher.renderBlock(Blocks.GRASS_BLOCK.getDefaultState(), new MatrixStack(), renderTypeBuffer, 15728880, OverlayTexture.NO_OVERLAY, EmptyModelData.INSTANCE);
        renderTypeBuffer.finish();
        RenderHelper.disableStandardItemLighting();
        RenderSystem.popMatrix();


        GlStateManager.popMatrix();
    }

    public static class SizeSlider extends Slider {

        protected AbstractAdvancedRegulatorScreen<? extends AbstractAdvancedRegulatorContainer> parent;

        public SizeSlider(int xPos, int yPos, int width, int height, ITextComponent prefix, ITextComponent suf, double minVal, double maxVal, double currentVal, boolean showDec, boolean drawStr, AbstractAdvancedRegulatorScreen<? extends AbstractAdvancedRegulatorContainer> parent) {
            super(xPos, yPos, width, height, prefix, suf, minVal, maxVal, currentVal, showDec, drawStr, null, slider -> {
                slider.setMessage(new TranslationTextComponent("ability.pymtech.regulator.size_slider", MathUtil.round(slider.getValue(), 2)));
                ((SizeSlider) slider).updateText();
            });
            this.parent = parent;
            this.updateText();
        }

        public void updateText() {
            this.setMessage(new TranslationTextComponent("ability.pymtech.regulator.size_slider", MathUtil.round(this.getValue(), 2)));
        }

        @Override
        public void onRelease(double mouseX, double mouseY) {
            super.onRelease(mouseX, mouseY);
            PymTech.NETWORK_CHANNEL.sendToServer(new SetRegulatedSizeMessage(this.parent.container.ability.getExtendedId(), (float) MathUtil.round(this.getValue(), 2)));
        }
    }

}
