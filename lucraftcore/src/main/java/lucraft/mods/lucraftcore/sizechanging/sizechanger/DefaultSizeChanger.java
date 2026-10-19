package lucraft.mods.lucraftcore.sizechanging.sizechanger;

import lucraft.mods.lucraftcore.sizechanging.capabilities.ISizeChanging;
import lucraft.mods.lucraftcore.util.attributes.LCAttributes;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;

import java.util.UUID;

public class DefaultSizeChanger extends SizeChanger {

    @Override
    public void onSizeChanged(EntityLivingBase entity, ISizeChanging data, float size) {
        AbstractAttributeMap map = entity.getAttributeMap();
        setAttribute(map, SharedMonsterAttributes.MOVEMENT_SPEED, (size - 1F) * 0.5D, 2, SizeChanger.ATTRIBUTE_UUID);
        setAttribute(map, LCAttributes.JUMP_HEIGHT, (size - 1F) * 1D, 0, SizeChanger.ATTRIBUTE_UUID);
        setAttribute(map, LCAttributes.FALL_RESISTANCE, size > 1F ? 1F / size : size, 1, SizeChanger.ATTRIBUTE_UUID);
        setAttribute(map, LCAttributes.STEP_HEIGHT, size, 1, SizeChanger.ATTRIBUTE_UUID);
        setAttribute(map, SharedMonsterAttributes.ATTACK_DAMAGE, (size - 1F) * 1D, 0, SizeChanger.ATTRIBUTE_UUID);
        setAttribute(map, EntityPlayer.REACH_DISTANCE, (size - 1F) * 1D, 0, SizeChanger.ATTRIBUTE_UUID);
        setAttribute(map, SharedMonsterAttributes.KNOCKBACK_RESISTANCE, (size - 1F) * 0.5D, 0, SizeChanger.ATTRIBUTE_UUID);
    }

    @Override
    public int getSizeChangingTime(EntityLivingBase entity, ISizeChanging data, float estimatedSize) {
        return 60;
    }

    @Override
    public void onUpdate(EntityLivingBase entity, ISizeChanging data, float size) {

    }

    @Override
    public boolean start(EntityLivingBase entity, ISizeChanging data, float size, float estimatedSize) {
        return true;
    }

    @Override
    public void end(EntityLivingBase entity, ISizeChanging data, float size) {

    }

    public void setAttribute(AbstractAttributeMap map, IAttribute attribute, double value, int operation, UUID uuid) {
        if (map.getAttributeInstance(attribute) != null) {
            IAttributeInstance instance = map.getAttributeInstance(attribute);
            if (instance.getModifier(uuid) != null)
                instance.removeModifier(uuid);
            instance.applyModifier(new AttributeModifier(uuid, "default_size_changer", value, operation));
        }
    }

}
