package lucraft.mods.heroesexpansion.items;

import lucraft.mods.heroesexpansion.entities.EntityTesseract;
import lucraft.mods.lucraftcore.infinity.EnumInfinityStone;
import lucraft.mods.lucraftcore.infinity.ModuleInfinity;
import lucraft.mods.lucraftcore.infinity.items.ItemInfinityStone;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nullable;

import static lucraft.mods.heroesexpansion.items.ItemSpaceStone.randomTeleport;

public class ItemTesseract extends ItemInfinityStone {

    public ItemTesseract(String name) {
        this.setTranslationKey(name);
        this.setRegistryName(StringHelper.unlocalizedToResourceName(name));
        this.setMaxStackSize(1);
        this.setCreativeTab(ModuleInfinity.TAB);
    }

    @Override
    public EnumInfinityStone getType() {
        return EnumInfinityStone.SPACE;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Nullable
    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        EntityTesseract item = new EntityTesseract(world, location.posX, location.posY, location.posZ, itemstack);
        item.motionX = location.motionX;
        item.motionY = location.motionY;
        item.motionZ = location.motionZ;
        return item;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        return randomTeleport(playerIn, handIn);
    }

}
