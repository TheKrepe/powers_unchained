package lucraft.mods.lucraftcore.utilities.render;

import com.mojang.authlib.GameProfile;
import lucraft.mods.lucraftcore.extendedinventory.InventoryExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.capabilities.CapabilityExtendedInventory;
import lucraft.mods.lucraftcore.util.entity.FakePlayerClient;
import lucraft.mods.lucraftcore.util.events.RenderModelEvent;
import lucraft.mods.lucraftcore.util.render.RenderArmor;
import lucraft.mods.lucraftcore.utilities.blocks.BlockSuitStand;
import lucraft.mods.lucraftcore.utilities.blocks.TileEntitySuitStand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TESRSuitStand extends TileEntitySpecialRenderer<TileEntitySuitStand> {

    public static FakePlayerClient fakePlayer = null;
    public static RenderArmor renderArmor = null;

    public TESRSuitStand() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void render(TileEntitySuitStand te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        if (!te.isEmpty() && !te.getWorld().isAirBlock(te.getPos())) {
            if (fakePlayer == null || fakePlayer.getEntityWorld() != te.getWorld()) {
                fakePlayer = new FakePlayerClient(te.getWorld(), new GameProfile(null, "SUIT STAND"));
                fakePlayer.info = "suit_stand";
            }
            if (renderArmor == null)
                renderArmor = new RenderArmor(Minecraft.getMinecraft().getRenderManager());

            float rotation = 180F;
            EnumFacing facing = te.getWorld().getBlockState(te.getPos()).getValue(BlockSuitStand.FACING);
            switch (facing) {
                case SOUTH:
                    rotation = 0F;
                    break;
                case WEST:
                    rotation = 90F;
                    break;
                case EAST:
                    rotation = 270F;
                    break;
                default:
                    break;
            }

            fakePlayer.inventory.armorInventory.set(3, te.getStackInSlot(0));
            fakePlayer.inventory.armorInventory.set(2, new ItemStack(Items.IRON_CHESTPLATE));
            fakePlayer.inventory.armorInventory.set(1, te.getStackInSlot(2));
            fakePlayer.inventory.armorInventory.set(0, te.getStackInSlot(3));
            fakePlayer.setItemStackToSlot(EntityEquipmentSlot.CHEST, te.getStackInSlot(1));

            if (fakePlayer.hasCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null)) {
                InventoryExtendedInventory inv = fakePlayer.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).getInventory();
                inv.setInventorySlotContents(InventoryExtendedInventory.SLOT_NECKLACE, te.getStackInSlot(4));
                inv.setInventorySlotContents(InventoryExtendedInventory.SLOT_MANTLE, te.getStackInSlot(5));
                inv.setInventorySlotContents(InventoryExtendedInventory.SLOT_WRIST, te.getStackInSlot(6));
            }

            fakePlayer.renderYawOffset = fakePlayer.prevRenderYawOffset = rotation;
            fakePlayer.rotationYawHead = fakePlayer.prevRotationYawHead = rotation;
            GlStateManager.pushMatrix();
            fakePlayer.setInvisible(true);
            new RenderArmor(Minecraft.getMinecraft().getRenderManager()).doRender(fakePlayer, x + 0.5F, y + 0.1F, z + 0.5F, rotation, partialTicks);
            GlStateManager.popMatrix();
        }
    }

    @SubscribeEvent
    public void renderModel(RenderModelEvent e) {
        if (e.getEntity() instanceof FakePlayerClient && ((FakePlayerClient) e.getEntity()).info.equalsIgnoreCase("suit_stand")) {
            float scale = 0.9F;
            GlStateManager.color(1, 1, 1, 0.5F);
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.translate(0, 0, -0.25F);
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void setupModel(RenderModelEvent.SetRotationAngels e) {
        if (e.getEntity() instanceof FakePlayerClient && ((FakePlayerClient) e.getEntity()).info.equalsIgnoreCase("suit_stand")) {
            e.setCanceled(true);
            e.model.bipedRightArm.rotateAngleX = 0F;
            e.model.bipedRightArm.rotateAngleY = 0F;
            e.model.bipedRightArm.rotateAngleZ = 0F;

            e.model.bipedLeftArm.rotateAngleX = 0F;
            e.model.bipedLeftArm.rotateAngleY = 0F;
            e.model.bipedLeftArm.rotateAngleZ = 0F;

            if (e.model instanceof ModelPlayer) {
                ModelPlayer model = (ModelPlayer) e.model;

                model.bipedRightArmwear.rotateAngleX = 0F;
                model.bipedRightArmwear.rotateAngleY = 0F;
                model.bipedRightArmwear.rotateAngleZ = 0F;

                model.bipedLeftArmwear.rotateAngleX = 0F;
                model.bipedLeftArmwear.rotateAngleY = 0F;
                model.bipedLeftArmwear.rotateAngleZ = 0F;
            }
        }
    }

}
