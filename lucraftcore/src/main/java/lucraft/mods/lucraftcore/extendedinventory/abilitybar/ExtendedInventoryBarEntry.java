package lucraft.mods.lucraftcore.extendedinventory.abilitybar;

import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.events.ExtendedInventoryKeyEvent;
import lucraft.mods.lucraftcore.extendedinventory.network.MessageExtendedInventoryKey;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.util.abilitybar.IAbilityBarEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

public class ExtendedInventoryBarEntry implements IAbilityBarEntry {

    public ItemStack stack;
    public IItemExtendedInventory.ExtendedInventoryItemType type;

    public ExtendedInventoryBarEntry(ItemStack stack, IItemExtendedInventory.ExtendedInventoryItemType type) {
        this.stack = stack;
        this.type = type;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void onButtonPress() {
        if (MinecraftForge.EVENT_BUS.post(new ExtendedInventoryKeyEvent.Client(type, true)))
            return;
        LCPacketDispatcher.sendToServer(new MessageExtendedInventoryKey(true, this.type));
    }

    @Override
    public void onButtonRelease() {
        if (MinecraftForge.EVENT_BUS.post(new ExtendedInventoryKeyEvent.Client(type, false)))
            return;
        LCPacketDispatcher.sendToServer(new MessageExtendedInventoryKey(false, this.type));
    }

    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        float zLevel = Minecraft.getMinecraft().getRenderItem().zLevel;
        mc.getRenderItem().zLevel = -100.5F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        mc.getRenderItem().renderItemAndEffectIntoGUI(this.stack, 0, 0);
        mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRenderer, this.stack, 0, 0, null);
        GlStateManager.popMatrix();
        mc.getRenderItem().zLevel = zLevel;
    }

    @Override
    public String getDescription() {
        return ((IItemExtendedInventory) this.stack.getItem()).getAbilityBarDescription(this.stack, Minecraft.getMinecraft().player);
    }

    @Override
    public boolean renderCooldown() {
        return false;
    }

    @Override
    public float getCooldownPercentage() {
        return 0;
    }
}
