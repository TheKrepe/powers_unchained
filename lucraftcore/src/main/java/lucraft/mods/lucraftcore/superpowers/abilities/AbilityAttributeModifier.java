package lucraft.mods.lucraftcore.superpowers.abilities;

import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityData;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataFloat;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataInteger;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataUUID;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.util.text.TextFormatting;

import java.util.UUID;

public abstract class AbilityAttributeModifier extends AbilityConstant {

    public static final AbilityData<Float> AMOUNT = new AbilityDataFloat("amount").disableSaving().setSyncType(EnumSync.SELF).enableSetting("amount", "The amount for the giving attribute modifier");
    public static final AbilityData<Integer> OPERATION = new AbilityDataInteger("operation").disableSaving().setSyncType(EnumSync.SELF).enableSetting("operation", "The operation for the giving attribute modifier (More: https://minecraft.gamepedia.com/Attribute#Operations)");
    public static final AbilityData<UUID> UUID = new AbilityDataUUID("uuid").disableSaving().setSyncType(EnumSync.SELF).enableSetting("uuid", "Sets the unique identifier for this attribute modifier");
    public static final AbilityData<Float> MULTIPLIER = new AbilityDataFloat("multiplier").setSyncType(EnumSync.SELF);

    public AbilityAttributeModifier(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(AMOUNT, 1F);
        this.dataManager.register(OPERATION, 0);
        this.dataManager.register(UUID, java.util.UUID.fromString("0669d99d-b34d-40fc-a4d8-c7ee963cc842"));
        this.dataManager.register(MULTIPLIER, 1f);
    }

    @Override
    public String getDisplayDescription() {
        return super.getDisplayDescription() + "\n \n" + TextFormatting.BLUE + (getOperation() == 0 ? "+" : "*") + getAmount();
    }

    public abstract IAttribute getAttribute();

    public UUID getModifierUUID() {
        return this.dataManager.get(UUID);
    }

    public float getAmount() {
        return this.dataManager.get(AMOUNT) * this.dataManager.get(MULTIPLIER);
    }

    public int getOperation() {
        return this.dataManager.get(OPERATION);
    }

    @Override
    public void updateTick() {
        if(!entity.world.isRemote) {
            if (entity.getAttributeMap().getAllAttributes().stream().noneMatch(iAttributeInstance -> iAttributeInstance.getAttribute() == getAttribute())) {
                entity.getAttributeMap().registerAttribute(getAttribute()).setBaseValue(getAttribute().getDefaultValue());
            }
            if (entity.getAttributeMap().getAttributeInstance(getAttribute()).getModifier(getModifierUUID()) != null && (
                    entity.getEntityAttribute(getAttribute()).getModifier(getModifierUUID()).getAmount() != getAmount()
                            || entity.getEntityAttribute(getAttribute()).getModifier(getModifierUUID()).getOperation() != getOperation())) {
                entity.getAttributeMap().getAttributeInstance(getAttribute()).removeModifier(getModifierUUID());
            }

            if (entity.getAttributeMap().getAttributeInstance(getAttribute()).getModifier(getModifierUUID()) == null) {
                AttributeModifier modifier = new AttributeModifier(getModifierUUID(), getUnlocalizedName(), getAmount(), getOperation()).setSaved(false);
                entity.getAttributeMap().getAttributeInstance(getAttribute()).applyModifier(modifier);
            }
        }
    }

    @Override
    public void lastTick() {
        if (!entity.world.isRemote && entity.getAttributeMap().getAttributeInstance(getAttribute()).getModifier(getModifierUUID()) != null) {
            entity.getAttributeMap().getAttributeInstance(getAttribute()).removeModifier(getModifierUUID());
        }
    }

}
