package lucraft.mods.heroesexpansion.client.gui;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.container.ContainerQuiver;
import lucraft.mods.heroesexpansion.items.InventoryQuiver;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class GuiQuiver extends GuiContainer {

    private static final ResourceLocation QUIVER_GUI_TEXTURES = new ResourceLocation(HeroesExpansion.MODID, "textures/gui/quiver.png");

    public final EntityPlayer player;
    public final InventoryQuiver inv;

    public GuiQuiver(EntityPlayer player, ItemStack quiver) {
        super(new ContainerQuiver(player, quiver));
        this.player = player;
        this.inv = new InventoryQuiver(quiver);
    }

    @Override
    public void initGui() {
        this.xSize = 176;
        this.ySize = 132;
        super.initGui();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        this.renderHoveredToolTip(mouseX, mouseY);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        String s = this.inv.getDisplayName().getUnformattedText();
        this.fontRenderer.drawString(s, 8, 6, 4210752);
        this.fontRenderer.drawString(this.player.inventory.getDisplayName().getUnformattedText(), 8, this.ySize - 96 + 2, 4210752);
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(QUIVER_GUI_TEXTURES);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        this.itemRender.renderItemAndEffectIntoGUI(player.getHeldItemMainhand(), i + 26, j + 18);
    }

}
