package lucraft.mods.heroesexpansion.items;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.client.gui.HEGuiHandler;
import lucraft.mods.heroesexpansion.entities.EntityHEArrow.EntityGrapplingHookArrow;
import lucraft.mods.heroesexpansion.util.items.IColorableItem;
import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.InventoryExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.capabilities.CapabilityExtendedInventory;
import lucraft.mods.lucraftcore.util.events.PlayerEmptyClickEvent;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import lucraft.mods.lucraftcore.util.items.ItemBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemQuiver extends ItemBase implements IItemExtendedInventory, IColorableItem {

    int[] dyeInts = {1973019, 11743532, 3887386, 5320730, 2437522, 8073150, 2651799, 11250603, 4408131, 14188952, 4312372, 14602026, 6719955, 12801229, 15435844};

    public ItemQuiver(String name) {
        super(name);
        this.setCreativeTab(HeroesExpansion.CREATIVE_TAB);
        this.setMaxStackSize(1);
    }

    @Override
    public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
    }

    @Override
    public ExtendedInventoryItemType getEIItemType(ItemStack stack) {
        return ExtendedInventoryItemType.MANTLE;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        if (handIn == EnumHand.MAIN_HAND) {
            if (!playerIn.getHeldItem(handIn).hasTagCompound())
                playerIn.getHeldItem(handIn).setTagCompound(new NBTTagCompound());
            playerIn.openGui(HeroesExpansion.INSTANCE, HEGuiHandler.QUIVER, worldIn, (int) playerIn.posX, (int) playerIn.posY, (int) playerIn.posZ);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(handIn));
        }
        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public boolean useButton(ItemStack stack, EntityPlayer player) {
        return true;
    }

    @Override
    public String getAbilityBarDescription(ItemStack stack, EntityPlayer player) {
        return StringHelper.translateToLocal("heroesexpansion.info.quiver_desc");
    }

    @Override
    public void onPressedButton(ItemStack itemstack, EntityPlayer player, boolean pressed) {
        if (player.world.isRemote || !pressed)
            return;

        if (!itemstack.hasTagCompound())
            itemstack.setTagCompound(new NBTTagCompound());

        int selected = itemstack.getTagCompound().getInteger("Selected");
        selected += 1;
        if (selected >= 5)
            selected = 0;
        itemstack.getTagCompound().setInteger("Selected", selected);

        player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).syncToAll();
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        super.getSubItems(tab, items);
        if (this.isInCreativeTab(tab)) {
            for (int i : dyeInts) {
                ItemStack quiver = new ItemStack(this);
                this.setColor(quiver, i);
                items.add(quiver);
            }
        }
    }

    @Override
    public int getDefaultColor(ItemStack stack) {
        return 10511680;
    }

    @EventBusSubscriber(modid = HeroesExpansion.MODID)
    public static class EventHandler {

        @SubscribeEvent
        public static void onLeftClick(PlayerEmptyClickEvent.LeftClick e) {
            boolean sound = false;
            for (EntityGrapplingHookArrow entity : e.getEntityLiving().getEntityWorld().getEntitiesWithinAABB(EntityGrapplingHookArrow.class, new AxisAlignedBB(e.getEntityPlayer().getPosition().add(50, 50, 50), e.getEntityPlayer().getPosition().add(-50, -50, -50)))) {
                if (!entity.world.isRemote && entity.shootingEntity == e.getEntityPlayer()) {
                    entity.setDead();
                    sound = true;
                }
            }
            if (sound)
                PlayerHelper.playSoundToAll(e.getEntityPlayer().world, e.getEntity().posX, e.getEntity().posY, e.getEntity().posZ, 50, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS);
        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void onRenderHUD(RenderGameOverlayEvent e) {
            Minecraft mc = Minecraft.getMinecraft();

            if (e.isCancelable() || e.getType() != ElementType.EXPERIENCE || !mc.player.hasCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null) || mc.player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).getInventory().getStackInSlot(InventoryExtendedInventory.SLOT_MANTLE).isEmpty()
                    || !(mc.player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).getInventory().getStackInSlot(InventoryExtendedInventory.SLOT_MANTLE).getItem() instanceof ItemQuiver)) {
                return;
            }

            ItemStack stack = mc.player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).getInventory().getStackInSlot(InventoryExtendedInventory.SLOT_MANTLE);
            InventoryQuiver inv = new InventoryQuiver(stack);
            int selected = stack.hasTagCompound() ? stack.getTagCompound().getInteger("Selected") : 0;

            GlStateManager.pushMatrix();

            mc.renderEngine.bindTexture(new ResourceLocation("textures/gui/widgets.png"));
            mc.ingameGUI.drawTexturedModalRect(e.getResolution().getScaledWidth() - 102, e.getResolution().getScaledHeight() - 22, 0, 0, 81, 22);
            mc.ingameGUI.drawTexturedModalRect(e.getResolution().getScaledWidth() - 102 + 81, e.getResolution().getScaledHeight() - 22, 161, 0, 21, 22);
            mc.ingameGUI.drawTexturedModalRect(e.getResolution().getScaledWidth() - 103 + selected * 20, e.getResolution().getScaledHeight() - 23, 0, 22, 24, 24);

            for (int i = 0; i < inv.getSizeInventory(); i++) {
                mc.getRenderItem().renderItemAndEffectIntoGUI(inv.getStackInSlot(i), e.getResolution().getScaledWidth() - 99 + i * 20, e.getResolution().getScaledHeight() - 19);
                mc.getRenderItem().renderItemOverlays(mc.fontRenderer, inv.getStackInSlot(i), e.getResolution().getScaledWidth() - 99 + i * 20, e.getResolution().getScaledHeight() - 19);
            }

            float scale = e.getResolution().getScaleFactor() * 1F;
            GlStateManager.translate(e.getResolution().getScaledWidth() - 51 - (16 * scale) / 2, e.getResolution().getScaledHeight_double() - 16 * scale - 24, 0);
            GlStateManager.scale(scale, scale, 1);
            mc.getRenderItem().renderItemIntoGUI(stack, 0, 0);

            GlStateManager.popMatrix();
        }

    }

}
