package lucraft.mods.lucraftcore.extendedinventory.capabilities;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.InventoryExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.network.MessageOpenExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.network.MessageSyncExtendedInventory;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.util.gui.buttons.GuiButton10x;
import lucraft.mods.lucraftcore.tabs.TabRegistry;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerDropsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CapabilityExtendedInventory implements IExtendedInventoryCapability {

    @CapabilityInject(IExtendedInventoryCapability.class)
    public static final Capability<IExtendedInventoryCapability> EXTENDED_INVENTORY_CAP = null;

    public EntityPlayer player;
    private InventoryExtendedInventory inventory;

    public CapabilityExtendedInventory(EntityPlayer player) {
        this.player = player;
        this.inventory = new InventoryExtendedInventory(player);
    }

    @Override
    public InventoryExtendedInventory getInventory() {
        return this.inventory;
    }

    @Override
    public NBTTagCompound writeNBT() {
        return inventory.writeToNBT(new NBTTagCompound());
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        this.inventory.readFromNBT(nbt);
    }

    @Override
    public void syncToPlayer() {
        this.syncToPlayer(this.player);
    }

    @Override
    public void syncToPlayer(EntityPlayer receiver) {
        if (receiver instanceof EntityPlayerMP)
            LCPacketDispatcher.sendTo(new MessageSyncExtendedInventory(this.player), (EntityPlayerMP) receiver);
    }

    @Override
    public void syncToAll() {
        this.syncToPlayer();
        if (player.world instanceof WorldServer) {
            for (EntityPlayer players : ((WorldServer) player.world).getEntityTracker().getTrackingPlayers(player)) {
                if (players instanceof EntityPlayerMP) {
                    LCPacketDispatcher.sendTo(new MessageSyncExtendedInventory(player), (EntityPlayerMP) players);
                }
            }
        }
    }

    public static class CapabilityExtendedInventoryEventHandler {

        @SubscribeEvent
        public void onAttachCapabilities(AttachCapabilitiesEvent<Entity> evt) {
            if (!(evt.getObject() instanceof EntityPlayer) || evt.getObject().hasCapability(EXTENDED_INVENTORY_CAP, null))
                return;

            evt.addCapability(new ResourceLocation(LucraftCore.MODID, "extended_inventory"), new CapabilityExtendedInventoryProvider(new CapabilityExtendedInventory((EntityPlayer) evt.getObject())));
        }

        @SubscribeEvent
        public void onPlayerStartTracking(PlayerEvent.StartTracking e) {
            if (e.getTarget().hasCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null)) {
                e.getTarget().getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).syncToPlayer(e.getEntityPlayer());
            }
        }

        @SubscribeEvent
        public void onEntityJoinWorld(EntityJoinWorldEvent e) {
            if (e.getEntity() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) e.getEntity();
                IExtendedInventoryCapability data = player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null);
                data.syncToAll();
            }
        }

        @SubscribeEvent
        public void onPlayerClone(PlayerEvent.Clone e) {
            NBTTagCompound compound = new NBTTagCompound();
            compound = (NBTTagCompound) CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP.getStorage().writeNBT(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, e.getOriginal().getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null), null);
            CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP.getStorage().readNBT(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, e.getEntityPlayer().getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null), null, compound);
        }

        @SubscribeEvent
        public void onPlayerDrops(PlayerDropsEvent e) {
            if (!e.getEntityPlayer().world.isRemote && !e.getEntityPlayer().world.getGameRules().getBoolean("keepInventory")) {
                if (e.getEntityPlayer().hasCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null)) {
                    InventoryExtendedInventory inv = e.getEntityPlayer().getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).getInventory();

                    for (int i = 0; i < InventoryExtendedInventory.INV_SIZE; i++) {
                        if (!inv.getStackInSlot(i).isEmpty()) {
                            EntityItem en = new EntityItem(e.getEntityPlayer().world, e.getEntityPlayer().posX, e.getEntityPlayer().posY + e.getEntityPlayer().eyeHeight, e.getEntityPlayer().posZ, inv.getStackInSlot(i).copy());
                            en.setPickupDelay(40);
                            float f1 = e.getEntityPlayer().world.rand.nextFloat() * 0.5F;
                            float f2 = e.getEntityPlayer().world.rand.nextFloat() * (float) Math.PI * 2.0F;
                            en.motionX = (double) (-MathHelper.sin(f2) * f1);
                            en.motionZ = (double) (MathHelper.cos(f2) * f1);
                            en.motionY = 0.20000000298023224D;
                            e.getDrops().add(en);
                            inv.setInventorySlotContents(i, ItemStack.EMPTY);
                        }
                    }
                }
            }
        }

        @SubscribeEvent
        public void playerTick(LivingUpdateEvent e) {
            if (e.getEntity() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) e.getEntity();

                if (player.hasCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null)) {
                    InventoryExtendedInventory inv = player.getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).getInventory();

                    for (int i = 0; i < inv.getSizeInventory(); i++) {
                        ItemStack stack = inv.getStackInSlot(i);

                        if (!stack.isEmpty() && stack.getItem() instanceof IItemExtendedInventory) {
                            ((IItemExtendedInventory) stack.getItem()).onWornTick(stack, player);
                        }
                    }
                }
            }
        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public void onGuiOpen(GuiScreenEvent.InitGuiEvent.Post e) {
            if (e.getGui() instanceof GuiContainerCreative) {
                int k = (e.getGui().width - 195) / 2;
                int l = (e.getGui().height - 136) / 2;

                e.getButtonList().add(new GuiButton10x(52, k + 151, l + 142, "+"));
            }
        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public void onButtonPressPre(GuiScreenEvent.ActionPerformedEvent.Post e) {
            if (e.getGui() instanceof GuiContainerCreative && e.getButton().id == 52 && TabRegistry.getTabList().size() > 1) {
                LCPacketDispatcher.sendToServer(new MessageOpenExtendedInventory());
            }
        }

    }

    public static class CapabilityExtendedInventoryStorage implements IStorage<IExtendedInventoryCapability> {

        @Override
        public NBTBase writeNBT(Capability<IExtendedInventoryCapability> capability, IExtendedInventoryCapability instance, EnumFacing side) {
            return instance.writeNBT();
        }

        @Override
        public void readNBT(Capability<IExtendedInventoryCapability> capability, IExtendedInventoryCapability instance, EnumFacing side, NBTBase nbt) {
            instance.readNBT((NBTTagCompound) nbt);
        }

    }

}
