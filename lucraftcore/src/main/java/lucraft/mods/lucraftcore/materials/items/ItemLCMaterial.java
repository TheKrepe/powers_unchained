package lucraft.mods.lucraftcore.materials.items;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.materials.Material;
import lucraft.mods.lucraftcore.materials.Material.MaterialComponent;
import lucraft.mods.lucraftcore.materials.ModuleMaterials;
import lucraft.mods.lucraftcore.util.items.ItemBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class ItemLCMaterial extends ItemBase {

    public Material material;
    public MaterialComponent component;

    public ItemLCMaterial(Material material, MaterialComponent component) {
        super(component.getName() + "_" + material.getResourceName());
        this.setCreativeTab(ModuleMaterials.TAB_MATERIALS);
        this.material = material;
        this.component = component;
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
        if (material.isRadioactive() && LCConfig.materials.radiation_) {
            float radius = 2F;
            int amplifier = 0;

            for (EntityLivingBase entity : entityItem.getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(entityItem.getPosition().add(-radius, -radius, -radius), entityItem.getPosition().add(radius, radius, radius)))) {
                entity.addPotionEffect(new PotionEffect(ModuleMaterials.INSTANCE.POTIONS.RADIATION, 5 * 20, amplifier));
            }
        }
        return super.onEntityItemUpdate(entityItem);
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (entityIn instanceof EntityLivingBase && material.isRadioactive() && LCConfig.materials.radiation_) {
            ((EntityLivingBase) entityIn).addPotionEffect(new PotionEffect(ModuleMaterials.INSTANCE.POTIONS.RADIATION, 5 * 20, 1));
        }
        super.onUpdate(stack, worldIn, entityIn, itemSlot, isSelected);
    }

}
