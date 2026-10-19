package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.entities.EntityThorWeapon;
import lucraft.mods.heroesexpansion.entities.EntityThrownThorWeapon;
import lucraft.mods.heroesexpansion.items.ItemThorWeapon;
import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityAction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.LinkedList;

public class AbilityCallWeapon extends AbilityAction {

    public AbilityCallWeapon(EntityLivingBase entity) {
        super(entity);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        HEIconHelper.drawIcon(mc, gui, x, y, 1, 6);
    }

    @Override
    public boolean action() {
        WeaponLocation loc = getWeapon(entity);
        if (loc != null) {
            loc.sendToOwner();
            return true;
        } else {
            if (entity instanceof EntityPlayer)
                ((EntityPlayer) entity).sendStatusMessage(new TextComponentTranslation("heroesexpansion.into.weapon_not_found"), true);
            return false;
        }
    }

    public static WeaponLocation getWeapon(EntityLivingBase en) {
        LinkedList<WorldServer> list = getWorlds(en);
        for (WorldServer worlds : list) {
            if (worlds == null)
                continue;
            
            for (Entity entity : worlds.loadedEntityList) {
                if (entity instanceof EntityThorWeapon && isCallableWeapon(en, ((EntityThorWeapon) entity).getItem())) {
                    return new WeaponLocation(en, (EntityThorWeapon) entity, ((EntityThorWeapon) entity).getItem());
                }

                if (entity instanceof EntityLivingBase && entity != en) {
                    for (EntityEquipmentSlot slots : EntityEquipmentSlot.values()) {
                        ItemStack stack = ((EntityLivingBase) entity).getItemStackFromSlot(slots);
                        if (isCallableWeapon(en, stack))
                            return new WeaponLocation(en, (EntityLivingBase) entity, slots, stack);
                    }

                    if (entity instanceof EntityPlayer) {
                        EntityPlayer player2 = (EntityPlayer) entity;

                        for (int i = 0; i < player2.inventory.getSizeInventory(); i++) {
                            ItemStack stack = player2.inventory.getStackInSlot(i);
                            if (isCallableWeapon(en, stack))
                                return new WeaponLocation(en, player2, i, stack);
                        }
                    }
                }
            }

            for (TileEntity tileEntity : worlds.loadedTileEntityList) {
                if (tileEntity.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null)) {
                    IItemHandler itemHandler = tileEntity.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                    for (int i = 0; i < itemHandler.getSlots(); i++) {
                        ItemStack stack = itemHandler.getStackInSlot(i);
                        if (isCallableWeapon(en, stack))
                            return new WeaponLocation(en, tileEntity, i, stack);
                    }
                }
            }
        }

        return null;
    }

    public static LinkedList<WorldServer> getWorlds(EntityLivingBase entity) {
        LinkedList<WorldServer> list = new LinkedList();

        if (entity.world instanceof WorldServer)
            list.add((WorldServer) entity.world);

        for (WorldServer worlds : entity.getServer().worlds) {
            if (!list.contains(worlds))
                list.add(worlds);
        }

        return list;
    }

    public static boolean isCallableWeapon(EntityLivingBase entity, ItemStack stack) {
        boolean b = !stack.isEmpty() && stack.getItem() instanceof ItemThorWeapon && stack.hasTagCompound() && stack.getTagCompound().hasKey("Owner") && stack.getTagCompound().getString("Owner").equals(entity.getPersistentID().toString());
        try {
            ItemThorWeapon.canLift(entity, stack);
        } catch (Exception e) {
            b = false;
        }
        return b;
    }

    public static class WeaponLocation {

        public EntityLivingBase owner = null;
        public EntityThorWeapon entity = null;
        public EntityLivingBase entityWithItem = null;
        public EntityEquipmentSlot slot = null;
        public TileEntity tileEntityWithItem = null;
        public int invSlot = -1;
        public ItemStack weapon;

        public WeaponLocation(EntityLivingBase owner, EntityThorWeapon entity, ItemStack weapon) {
            this.owner = owner;
            this.entity = entity;
            this.weapon = weapon;
        }

        public WeaponLocation(EntityLivingBase owner, EntityLivingBase entityWithItem, EntityEquipmentSlot slot, ItemStack weapon) {
            this.owner = owner;
            this.entityWithItem = entityWithItem;
            this.slot = slot;
            this.weapon = weapon;
        }

        public WeaponLocation(EntityLivingBase owner, EntityPlayer entityWithItem, int invSlot, ItemStack weapon) {
            this.owner = owner;
            this.entityWithItem = entityWithItem;
            this.invSlot = invSlot;
            this.weapon = weapon;
        }

        public WeaponLocation(EntityLivingBase owner, TileEntity tileEntityWithItem, int invSlot, ItemStack weapon) {
            this.owner = owner;
            this.tileEntityWithItem = tileEntityWithItem;
            this.invSlot = invSlot;
            this.weapon = weapon;
        }

        public void sendToOwner() {
            if (entity != null) {
                EntityThrownThorWeapon newEntity = new EntityThrownThorWeapon(owner.world, owner);
                if (entity.dimension != owner.dimension || entity.getDistance(owner) > 50)
                    newEntity.setPositionAndUpdate(owner.posX, 256, owner.posZ);
                else
                    newEntity.setPositionAndUpdate(entity.posX, entity.posY, entity.posZ);
                newEntity.item = entity.getItem();
                newEntity.backToUser = true;
                owner.world.spawnEntity(newEntity);
                entity.setDead();
            } else if (entityWithItem != null) {
                EntityThrownThorWeapon newEntity = new EntityThrownThorWeapon(owner.world, owner);
                if (entityWithItem.dimension != owner.dimension || entityWithItem.getDistance(owner) > 50)
                    newEntity.setPositionAndUpdate(owner.posX, 256, owner.posZ);
                else
                    newEntity.setPositionAndUpdate(entityWithItem.posX, entityWithItem.posY, entityWithItem.posZ);
                newEntity.item = this.slot == null ? ((EntityPlayer) entityWithItem).inventory.getStackInSlot(invSlot) : entityWithItem.getItemStackFromSlot(slot);
                newEntity.backToUser = true;
                owner.world.spawnEntity(newEntity);
                if (slot == null)
                    ((EntityPlayer) entityWithItem).inventory.setInventorySlotContents(invSlot, ItemStack.EMPTY);
                else
                    entityWithItem.setItemStackToSlot(slot, ItemStack.EMPTY);
            } else if (tileEntityWithItem != null) {
                EntityThrownThorWeapon newEntity = new EntityThrownThorWeapon(owner.world, owner);
                IItemHandler itemHandler = tileEntityWithItem.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
                ItemStack itemStack = itemHandler.extractItem(invSlot, 1, false);
                if (!itemStack.isEmpty()) {
                    if (tileEntityWithItem.getWorld() != owner.world || tileEntityWithItem.getPos().getDistance((int) owner.posX, (int) owner.posY, (int) owner.posZ) > 50)
                        newEntity.setPositionAndUpdate(owner.posX, 256, owner.posZ);
                    else
                        newEntity.setPositionAndUpdate(tileEntityWithItem.getPos().getX(), tileEntityWithItem.getPos().getY(), tileEntityWithItem.getPos().getZ());
                    newEntity.item = itemStack;
                    newEntity.backToUser = true;
                    owner.world.spawnEntity(newEntity);
                }
            }
        }

    }

}
