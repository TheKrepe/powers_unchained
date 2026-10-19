package lucraft.mods.lucraftcore.addonpacks.gui;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.addonpacks.AddonPackHandler;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiOptionButton;
import net.minecraft.client.gui.GuiOptions;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.io.IOException;

@EventBusSubscriber(modid = LucraftCore.MODID, value = Side.CLIENT)
public class GuiAddonPacks extends GuiScreen {

    private final GuiScreen parentScreen;
    private GuiListAddonPacks list;

    public GuiAddonPacks(GuiScreen parentScreenIn) {
        this.parentScreen = parentScreenIn;
    }

    @Override
    public void initGui() {
        super.initGui();

        this.buttonList.add(new GuiOptionButton(2, this.width / 2 - 154, this.height - 48, I18n.format("lucraftcore.info.openaddonpacksfolder")));
        this.buttonList.add(new GuiOptionButton(1, this.width / 2 + 4, this.height - 48, I18n.format("gui.done")));

        this.list = new GuiListAddonPacks(this, this.mc, this.width, this.height, 32, this.height - 64, 36);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 2) {
            OpenGlHelper.openFile(AddonPackHandler.getAddonPacksDir());
        } else if (button.id == 1) {
            this.mc.displayGuiScreen(this.parentScreen);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        this.list.handleMouseInput();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.list.drawScreen(mouseX, mouseY, partialTicks);
        // this.drawCenteredString(this.fontRenderer, this.title, this.width /
        // 2, 20, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.list.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        super.mouseReleased(mouseX, mouseY, state);
        this.list.mouseReleased(mouseX, mouseY, state);
    }

    @SubscribeEvent
    public static void guiEvent(GuiScreenEvent.InitGuiEvent.Post e) {
        if (e.getGui() instanceof GuiOptions) {
            e.getButtonList().add(new GuiButtonExt(666, e.getGui().width / 2 - 155, e.getGui().height / 6 + 134 - 6, 150, 20, StringHelper.translateToLocal("lucraftcore.info.addonpackbutton")));

            for (GuiButton button : e.getButtonList()) {
                if (button.id >= 100 && button.id <= 110) {
                    button.y -= 10;
                }
            }
        }
    }

    @SubscribeEvent
    public static void guiButtonEvent(GuiScreenEvent.ActionPerformedEvent.Pre e) {
        if (e.getGui() instanceof GuiOptions && e.getButton().id == 666) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiAddonPacks(Minecraft.getMinecraft().currentScreen));
        }
    }

}
