package lucraft.mods.lucraftcore.util.items;

import io.netty.buffer.ByteBuf;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.network.AbstractServerMessageHandler;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.util.abilitybar.AbilityBarHandler;
import lucraft.mods.lucraftcore.util.abilitybar.EnumAbilityBarColor;
import lucraft.mods.lucraftcore.util.abilitybar.IAbilityBarEntry;
import lucraft.mods.lucraftcore.util.abilitybar.IAbilityBarProvider;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.EntityEquipmentSlot.Type;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class OpenableArmor {

    @SideOnly(Side.CLIENT)
    public static void init() {
        AbilityBarHandler.registerProvider(new OpenableArmorBarProvider());
    }

    public interface IOpenableArmor {

        boolean canBeOpened(Entity entity, ItemStack stack);

        void setArmorOpen(Entity entity, ItemStack stack, boolean open);

        boolean isArmorOpen(Entity entity, ItemStack stack);

        void onArmorToggled(Entity entity, ItemStack stack, boolean open);

        default EnumAbilityBarColor getAbilityBarColor(Entity entity, ItemStack stack, boolean open) {
            return null;
        }

    }

    public static class MessageToggleArmor implements IMessage {

        public EntityEquipmentSlot slot;

        public MessageToggleArmor() {
        }

        public MessageToggleArmor(EntityEquipmentSlot slot) {
            this.slot = slot;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            this.slot = EntityEquipmentSlot.values()[buf.readInt()];
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeInt(this.slot.ordinal());
        }

        public static class Handler extends AbstractServerMessageHandler<MessageToggleArmor> {

            @Override
            public IMessage handleServerMessage(EntityPlayer player, MessageToggleArmor message, MessageContext ctx) {

                LucraftCore.proxy.getThreadFromContext(ctx).addScheduledTask(new Runnable() {

                    @Override
                    public void run() {
                        ItemStack stack = player.getItemStackFromSlot(message.slot);

                        if (!stack.isEmpty() && stack.getItem() instanceof IOpenableArmor) {
                            IOpenableArmor armor = (IOpenableArmor) stack.getItem();
                            boolean isOpen = armor.isArmorOpen(player, stack);
                            armor.setArmorOpen(player, stack, !isOpen);
                            armor.onArmorToggled(player, stack, !isOpen);
                        }
                    }

                });

                return null;
            }

        }

    }

    @SideOnly(Side.CLIENT)
    public static class OpenableArmorBarProvider implements IAbilityBarProvider {

        @Override
        public List<IAbilityBarEntry> getEntries() {
            List<IAbilityBarEntry> entries = new ArrayList<>();
            EntityPlayer player = Minecraft.getMinecraft().player;
            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                if (slot.getSlotType() == Type.ARMOR) {
                    ItemStack stack = player.getItemStackFromSlot(slot);

                    if (!stack.isEmpty() && stack.getItem() instanceof IOpenableArmor && ((IOpenableArmor) stack.getItem()).canBeOpened(player, stack)) {
                        entries.add(new OpenableArmorBarEntry(stack, slot));
                    }
                }
            }
            return entries;
        }
    }

    @SideOnly(Side.CLIENT)
    public static class OpenableArmorBarEntry implements IAbilityBarEntry {

        public ItemStack item;
        public EntityEquipmentSlot slot;

        public OpenableArmorBarEntry(ItemStack item, EntityEquipmentSlot slot) {
            this.item = item;
            this.slot = slot;
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public void onButtonPress() {
            LCPacketDispatcher.sendToServer(new MessageToggleArmor(this.slot));
        }

        @Override
        public void onButtonRelease() {

        }

        @Override
        public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
            float zLevel = Minecraft.getMinecraft().getRenderItem().zLevel;
            mc.getRenderItem().zLevel = -100.5F;
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, 0);
            mc.getRenderItem().renderItemIntoGUI(this.item, 0, 0);

            if (((IOpenableArmor) this.item.getItem()).isArmorOpen(mc.player, this.item)) {
                mc.renderEngine.bindTexture(AbilityBarHandler.Renderer.HUD_TEX);
                mc.ingameGUI.drawTexturedModalRect(12, 12, 24, 0, 6, 6);
            }

            GlStateManager.popMatrix();
            mc.getRenderItem().zLevel = zLevel;
        }

        @Override
        public String getDescription() {
            return StringHelper.translateToLocal("lucraftcore.info.openarmor").replace("%s", this.item.getDisplayName());
        }

        @Override
        public boolean renderCooldown() {
            return false;
        }

        @Override
        public float getCooldownPercentage() {
            return 0;
        }

        @Override
        public EnumAbilityBarColor getColor() {
            IOpenableArmor armor = (IOpenableArmor) this.item.getItem();
            return armor.getAbilityBarColor(Minecraft.getMinecraft().player, this.item, armor.isArmorOpen(Minecraft.getMinecraft().player, this.item));
        }
    }
}
