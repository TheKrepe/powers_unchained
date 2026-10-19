package net.threetag.pymtech.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.container.StructureShrinkerContainer;
import net.threetag.pymtech.network.StructureShrinkActivateMessage;
import net.threetag.threecore.client.gui.widget.TranslucentButton;
import net.threetag.threecore.util.RenderUtil;
import net.threetag.threecore.util.energy.EnergyUtil;

import static net.threetag.threecore.util.TCFluidUtil.drawTooltip;

public class StructureShrinkerScreen extends ContainerScreen<StructureShrinkerContainer> {

    private static final ResourceLocation STRUCTURE_SHRINKER_GUI_TEXTURES = new ResourceLocation(PymTech.MODID, "textures/gui/container/structure_shrinker.png");

    public final PlayerInventory playerInventory;
    public final StructureShrinkerContainer container;

    public StructureShrinkerScreen(StructureShrinkerContainer container, PlayerInventory inventory, ITextComponent title) {
        super(container, inventory, title);
        this.playerInventory = inventory;
        this.container = container;
        this.ySize = 174;
    }

    @Override
    protected void init() {
        super.init();
        int left = this.guiLeft;
        int top = this.guiTop;
        this.playerInventoryTitleY = this.ySize - 94;
        this.addButton(new TranslucentButton(left + 77 - 40, top + 55, 80, 20, new TranslationTextComponent("pymtech.structure_shrinker.shrink"), b -> {
            PymTech.NETWORK_CHANNEL.sendToServer(new StructureShrinkActivateMessage(this.container.tileEntity.getPos()));
        }));
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
        this.renderHoveredTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int mouseX, int mouseY) {
        super.drawGuiContainerForegroundLayer(matrixStack, mouseX, mouseY);

        {
            BlockPos start = this.container.getSelectionStart();
            String s = I18n.format("pymtech.structure_shrinker.from", start.getX(), start.getY(), start.getZ());
            this.font.drawString(matrixStack, s, 77 - this.font.getStringWidth(s) / 2F, 25, 4210752);
        }

        {
            BlockPos end = this.container.getSelectionEnd();
            String s = I18n.format("pymtech.structure_shrinker.to", end.getX(), end.getY(), end.getZ());
            this.font.drawString(matrixStack, s, 77 - this.font.getStringWidth(s) / 2F, 37, 4210752);
        }

        EnergyUtil.drawTooltip(matrixStack, this.container.getEnergyStored(), this.container.getMaxEnergyStored(), this, 10, 17, 12, 40, mouseX - this.guiLeft, mouseY - this.guiTop);
        drawTooltip(this.container.tileEntity.fluidTank, matrixStack, this, 152, 17, 16, 60, mouseX - this.guiLeft, mouseY - this.guiTop);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(MatrixStack stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.minecraft.getTextureManager().bindTexture(STRUCTURE_SHRINKER_GUI_TEXTURES);
        int left = this.guiLeft;
        int top = this.guiTop;
        this.blit(stack, left, top, 0, 0, this.xSize, this.ySize);
        int energy = (int) (this.container.getEnergyPercentage() * 40);
        this.blit(stack, left + 10, top + 17 + 40 - energy, 194, 40 - energy, 12, energy);
        RenderUtil.renderGuiTank(this.container.tileEntity.fluidTank, 0, left + 152, top + 17, 0, 16, 60);
        this.minecraft.getTextureManager().bindTexture(STRUCTURE_SHRINKER_GUI_TEXTURES);
        this.blit(stack, left + 151, top + 16, 176, 0, 18, 62);
    }
}
