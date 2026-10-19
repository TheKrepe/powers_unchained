package lucraft.mods.lucraftcore.addonpacks.gui;

import com.google.common.collect.Lists;
import lucraft.mods.lucraftcore.addonpacks.AddonPackHandler;
import lucraft.mods.lucraftcore.addonpacks.AddonPackInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiListExtended;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;

public class GuiListAddonPacks extends GuiListExtended {

    public GuiAddonPacks parent;
    private final List<GuiListAddonPacksEntry> entries = Lists.<GuiListAddonPacksEntry>newArrayList();

    public GuiListAddonPacks(GuiAddonPacks parent, Minecraft mcIn, int widthIn, int heightIn, int topIn, int bottomIn, int slotHeightIn) {
        super(mcIn, widthIn, heightIn, topIn, bottomIn, slotHeightIn);
        this.parent = parent;

        for (AddonPackInfo infos : AddonPackHandler.ADDON_PACKS) {
            entries.add(new GuiListAddonPacksEntry(infos));
        }
    }

    @Override
    public IGuiListEntry getListEntry(int index) {
        return entries.get(index);
    }

    @Override
    protected int getSize() {
        return entries.size();
    }

    public static class GuiListAddonPacksEntry implements IGuiListEntry {

        private static final ResourceLocation ICON_MISSING = new ResourceLocation("textures/misc/unknown_server.png");

        private final AddonPackInfo packInfo;

        public GuiListAddonPacksEntry(AddonPackInfo packInfo) {
            this.packInfo = packInfo;
        }

        @Override
        public void updatePosition(int slotIndex, int x, int y, float partialTicks) {

        }

        @Override
        public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected, float partialTicks) {
            Minecraft mc = Minecraft.getMinecraft();

            List<String> list = new ArrayList<>();

            list.add(TextFormatting.UNDERLINE + "" + TextFormatting.BOLD + packInfo.getName() + (packInfo.getAuthor().isEmpty() ? "" : TextFormatting.RESET + " by " + packInfo.getAuthor()));

            if (!packInfo.getDescription().isEmpty()) {
                for (String s : packInfo.getDescription().split("\n")) {
                    if (list.size() < 3) {
                        list.add(s);
                    }
                }
            }

            for (int i = 0; i < MathHelper.clamp(list.size(), 1, 3); i++) {
                mc.fontRenderer.drawString(list.get(i), x + 38, y + 14 - list.size() * 5 + i * 12, i == 0 ? 0xfefefe : 8421504);
            }

            // if(!packInfo.getDescription().isEmpty())
            // mc.fontRenderer.drawString(packInfo.getDescription(), x + 38, y + 16,
            // 8421504);

            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(packInfo.getIcon() != null ? packInfo.getIcon() : ICON_MISSING);
            GlStateManager.enableBlend();
            Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
            GlStateManager.disableBlend();
        }

        @Override
        public boolean mousePressed(int slotIndex, int mouseX, int mouseY, int mouseEvent, int relativeX, int relativeY) {
            return false;
        }

        @Override
        public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {

        }

    }

}
