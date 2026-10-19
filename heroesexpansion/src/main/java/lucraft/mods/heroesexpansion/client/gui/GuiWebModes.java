package lucraft.mods.heroesexpansion.client.gui;

import lucraft.mods.heroesexpansion.items.ItemWebShooter;
import lucraft.mods.heroesexpansion.network.HEPacketDispatcher;
import lucraft.mods.heroesexpansion.network.MessageChangeWebMode;
import lucraft.mods.lucraftcore.util.gui.buttons.GuiButtonTranslucent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiWebModes extends GuiScreen {

    public List<ResourceLocation> modes = new ArrayList<>();
    public int tier;

    public GuiWebModes(int tier) {
        this.tier = tier;
    }

    @Override
    public void initGui() {
        super.initGui();
        int i = 0;
        List<ItemWebShooter.WebMode> list = ItemWebShooter.getWebModesForTier(this.tier);
        for (ItemWebShooter.WebMode webMode : list) {
            modes.add(ItemWebShooter.WEB_MODES.getNameForObject(webMode));
            this.addButton(new GuiButtonTranslucent(i, this.width / 2 - 40, this.height / 2 - list.size() * 10 + i * 20, 80, 18, webMode.name.getFormattedText()));
            i++;
        }
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        HEPacketDispatcher.sendToServer(new MessageChangeWebMode(modes.get(button.id)));
        Minecraft.getMinecraft().player.closeScreen();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
