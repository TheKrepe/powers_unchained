package lucraft.mods.heroesexpansion.client.gui;

import com.google.common.base.Predicate;
import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.network.HEPacketDispatcher;
import lucraft.mods.heroesexpansion.network.MessageOpenPortal;
import lucraft.mods.heroesexpansion.tileentities.TileEntityPortalDevice;
import lucraft.mods.lucraftcore.util.gui.buttons.GuiButtonTranslucent;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.io.IOException;

public class GuiOpenPortal extends GuiScreen {

    public static final ResourceLocation GUI_TEXTURES = new ResourceLocation(HeroesExpansion.MODID, "textures/gui/portal.png");

    public TileEntityPortalDevice tileEntity;

    protected int xSize = 172;
    protected int ySize = 172;

    public GuiTextField dimensionText;
    public GuiTextField xText;
    public GuiTextField yText;
    public GuiTextField zText;

    public GuiOpenPortal() {

    }

    public GuiOpenPortal(TileEntityPortalDevice tileEntity) {
        this.tileEntity = tileEntity;
    }

    @Override
    public void initGui() {
        super.initGui();
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;

        Predicate<String> validator = new Predicate<String>() {
            @Override
            public boolean apply(@Nullable String input) {
                return input.isEmpty() || input.equalsIgnoreCase("-") || StringHelper.isNumeric(input);
            }
        };

        dimensionText = new GuiTextField(0, mc.fontRenderer, i + (this.xSize / 2) + 8, j + 50, 30, 18);
        dimensionText.setValidator((str) -> {
            return str.isEmpty() || str.equalsIgnoreCase("-") || StringHelper.isInteger(str);
        });
        xText = new GuiTextField(1, mc.fontRenderer, i + (this.xSize / 2) - 60, j + (this.ySize / 2) - 9, 30, 18);
        xText.setValidator(validator);
        yText = new GuiTextField(2, mc.fontRenderer, i + (this.xSize / 2) - 12, j + (this.ySize / 2) - 9, 30, 18);
        yText.setValidator(validator);
        zText = new GuiTextField(3, mc.fontRenderer, i + (this.xSize / 2) + 37, j + (this.ySize / 2) - 9, 30, 18);
        zText.setValidator(validator);

        this.addButton(new GuiButtonTranslucent(4, i + (this.xSize / 2) - 40, j + 105, 80, 18, StringHelper.translateToLocal("heroesexpansion.info.open_portal")));

        if (this.tileEntity != null) {
            this.addButton(new GuiButtonTranslucent(5, i + (this.xSize / 2) - 40, j + 125, 80, 18, StringHelper.translateToLocal("heroesexpansion.info.invasion")));
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.dimensionText.updateCursorCounter();
        this.xText.updateCursorCounter();
        this.yText.updateCursorCounter();
        this.zText.updateCursorCounter();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        super.keyTyped(typedChar, keyCode);

        if (this.dimensionText.isFocused())
            this.dimensionText.textboxKeyTyped(typedChar, keyCode);
        if (this.xText.isFocused())
            this.xText.textboxKeyTyped(typedChar, keyCode);
        if (this.yText.isFocused())
            this.yText.textboxKeyTyped(typedChar, keyCode);
        if (this.zText.isFocused())
            this.zText.textboxKeyTyped(typedChar, keyCode);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        super.actionPerformed(button);

        if (button.id == 4) {
            try {
                int dim = Integer.parseInt(this.dimensionText.getText());
                double x = Double.parseDouble(this.xText.getText());
                double y = Double.parseDouble(this.yText.getText());
                double z = Double.parseDouble(this.zText.getText());

                if (this.tileEntity == null)
                    HEPacketDispatcher.sendToServer(new MessageOpenPortal(dim, x, y, z));
                else
                    HEPacketDispatcher.sendToServer(new MessageOpenPortal(dim, x, y, z, this.tileEntity.getPos().getX(), this.tileEntity.getPos().getY(), this.tileEntity.getPos().getZ()));
                mc.player.closeScreen();
            } catch (Exception e) {

            }
        } else if (button.id == 5) {
            HEPacketDispatcher.sendToServer(new MessageOpenPortal(0, 0, 0, 0, this.tileEntity.getPos().getX(), this.tileEntity.getPos().getY(), this.tileEntity.getPos().getZ(), true));
            mc.player.closeScreen();
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        this.dimensionText.mouseClicked(mouseX, mouseY, mouseButton);
        this.xText.mouseClicked(mouseX, mouseY, mouseButton);
        this.yText.mouseClicked(mouseX, mouseY, mouseButton);
        this.zText.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(GUI_TEXTURES);
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

        this.dimensionText.drawTextBox();
        this.xText.drawTextBox();
        this.yText.drawTextBox();
        this.zText.drawTextBox();

        this.fontRenderer.drawString("X:", this.xText.x - 10, this.xText.y + 5, 0xfefefe);
        this.fontRenderer.drawString("Y:", this.yText.x - 10, this.yText.y + 5, 0xfefefe);
        this.fontRenderer.drawString("Z:", this.zText.x - 10, this.zText.y + 5, 0xfefefe);
        this.fontRenderer.drawString("Dimension:", this.dimensionText.x - 50, this.dimensionText.y + 5, 0xfefefe);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
