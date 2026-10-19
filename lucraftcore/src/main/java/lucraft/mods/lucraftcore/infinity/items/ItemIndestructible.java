package lucraft.mods.lucraftcore.infinity.items;

import lucraft.mods.lucraftcore.infinity.EntityItemIndestructible;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class ItemIndestructible extends Item {

    public float entityHeight = 0.25F;
    public float entityWidth = 0.25F;

    public Item setEntitySize(float height, float width) {
        this.entityHeight = height;
        this.entityWidth = width;
        return this;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        EntityItemIndestructible item = new EntityItemIndestructible(world, location.posX, location.posY, location.posZ, itemstack);
        item.setEntitySize(entityHeight, entityWidth);
        item.motionX = location.motionX;
        item.motionY = location.motionY;
        item.motionZ = location.motionZ;
        return item;
    }

    /**
     * Gets called when the entity is picked up. Return true to disable the pick up
     *
     * @param world
     * @param player
     * @param stack
     * @param entity
     * @return
     */
    public boolean onItemPickUp(World world, EntityPlayer player, ItemStack stack, EntityItemIndestructible entity) {
        return false;
    }

}