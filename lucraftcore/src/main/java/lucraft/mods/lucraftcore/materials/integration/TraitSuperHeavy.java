package lucraft.mods.lucraftcore.materials.integration;

import lucraft.mods.lucraftcore.util.helper.LCEntityHelper;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.traits.AbstractTrait;

public class TraitSuperHeavy extends AbstractTrait {

    public TraitSuperHeavy(String identifier, TextFormatting color) {
        super(identifier, color);
    }

    @Override
    public void onUpdate(ItemStack tool, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (!world.isRemote && ((entity instanceof EntityPlayer && !LCEntityHelper.isStrongEnough((EntityPlayer) entity, 10)) || (entity instanceof EntityLivingBase && !(entity instanceof EntityPlayer)))) {
            LCEntityHelper.entityDropItem((EntityLivingBase) entity, tool, -3F + entity.getEyeHeight(), true);
            if (entity instanceof EntityPlayer)
                ((EntityPlayer) entity).sendStatusMessage(new TextComponentTranslation("lucraftcore.info.too_heavy"), true);
        }
    }

    @Override
    public String getLocalizedName() {
        return StringHelper.translateToLocal("lucraftcore.trait.super_heavy.name");
    }

    @Override
    public String getLocalizedDesc() {
        return StringHelper.translateToLocal("lucraftcore.trait.super_heavy.desc");
    }
}
