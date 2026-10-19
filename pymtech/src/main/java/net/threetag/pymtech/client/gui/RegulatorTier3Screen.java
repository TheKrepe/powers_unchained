package net.threetag.pymtech.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.container.RegulatorTier3Container;

public class RegulatorTier3Screen extends AbstractAdvancedRegulatorScreen<RegulatorTier3Container> {

    private static final ResourceLocation GUI_TEXTURES = new ResourceLocation(PymTech.MODID, "textures/gui/container/regulator_tier3.png");

    public RegulatorTier3Screen(RegulatorTier3Container screenContainer, PlayerInventory inv, ITextComponent titleIn) {
        super(screenContainer, inv, titleIn);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(MatrixStack matrixStack, int x, int y) {

    }

    @Override
    public ResourceLocation getGuiTextures() {
        return GUI_TEXTURES;
    }

}
