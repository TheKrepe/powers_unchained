package lucraft.mods.lucraftcore.superpowers.effects;

import com.google.gson.JsonObject;
import lucraft.mods.lucraftcore.util.items.OpenableArmor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.JsonUtils;

public class EffectConditionOpenArmor extends EffectCondition {

    public EntityEquipmentSlot slot;

    @Override
    public boolean isFulFilled(EntityLivingBase entity) {
        if (slot == null)
            return false;

        ItemStack stack = entity.getItemStackFromSlot(slot);

        if (!stack.isEmpty() && stack.getItem() instanceof OpenableArmor.IOpenableArmor) {
            return ((OpenableArmor.IOpenableArmor) stack.getItem()).isArmorOpen(entity, stack);
        }

        return false;
    }

    @Override
    public void readSettings(JsonObject json) {
        String s = JsonUtils.getString(json, "slot");
        this.slot = s.equalsIgnoreCase("helmet") ? EntityEquipmentSlot.HEAD : (s.equalsIgnoreCase("chest") ? EntityEquipmentSlot.CHEST : (s.equalsIgnoreCase("legs") ? EntityEquipmentSlot.LEGS : (s.equalsIgnoreCase("boots") ? EntityEquipmentSlot.FEET : null)));
    }
}
