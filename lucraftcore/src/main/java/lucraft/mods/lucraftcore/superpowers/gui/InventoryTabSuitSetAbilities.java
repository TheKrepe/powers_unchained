package lucraft.mods.lucraftcore.superpowers.gui;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.proxies.LCClientProxy;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import lucraft.mods.lucraftcore.tabs.AbstractTab;
import lucraft.mods.lucraftcore.tabs.TabRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class InventoryTabSuitSetAbilities extends AbstractTab {

    public ResourceLocation texture = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

    public InventoryTabSuitSetAbilities() {
        super(0, 0, 0, new ItemStack(Blocks.BARRIER));
    }

    @Override
    public void onTabClicked() {
        if (shouldAddToList() && LCClientProxy.canClickTab())
            Minecraft.getMinecraft().displayGuiScreen(new GuiAbilities(Minecraft.getMinecraft().player, Ability.EnumAbilityContext.SUIT));
    }

    @Override
    public boolean shouldAddToList() {
        return LCConfig.modules.superpowers && SuitSet.hasSuitSetOn(Minecraft.getMinecraft().player);
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        int newPotionOffset = TabRegistry.getPotionOffsetNEI();
        if (newPotionOffset != this.potionOffsetLast) {
            this.x += newPotionOffset - this.potionOffsetLast;
            this.potionOffsetLast = newPotionOffset;
        }
        if (this.visible) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

            int yTexPos = this.enabled ? 3 : 32;
            int ySize = this.enabled ? 25 : 32;
            int xOffset = this.id == 2 ? 0 : 1;
            int yPos = this.y + (this.enabled ? 3 : 0);

            mc.renderEngine.bindTexture(this.texture);
            this.drawTexturedModalRect(this.x, yPos, xOffset * 28, yTexPos, 28, ySize);

            RenderHelper.enableGUIStandardItemLighting();
            this.zLevel = 100.0F;
            this.itemRender.zLevel = 100.0F;
            GlStateManager.enableLighting();
            GlStateManager.enableRescaleNormal();
            this.itemRender.renderItemAndEffectIntoGUI(this.getItemStack(), this.x + 6, this.y + 8);
            this.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, this.getItemStack(), this.x + 6, this.y + 8, null);
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            this.itemRender.zLevel = 0.0F;
            this.zLevel = 0.0F;
            RenderHelper.disableStandardItemLighting();
        }
    }

    public ItemStack getItemStack() {
        if (SuitSet.hasSuitSetOn(Minecraft.getMinecraft().player))
            return SuitSet.getSuitSet(Minecraft.getMinecraft().player).getRepresentativeItem();

        return new ItemStack(Blocks.BARRIER);
    }

}
