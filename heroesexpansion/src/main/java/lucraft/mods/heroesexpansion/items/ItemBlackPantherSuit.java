package lucraft.mods.heroesexpansion.items;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import lucraft.mods.heroesexpansion.HEConfig;
import lucraft.mods.heroesexpansion.suitsets.SuitSetBlackPanther;
import lucraft.mods.lucraftcore.superpowers.suitsets.ItemSuitSetArmor;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

import java.util.UUID;

public class ItemBlackPantherSuit extends ItemSuitSetArmor {

    private static final UUID[] ARMOR_MODIFIERS = new UUID[]{UUID.fromString("845DB27C-C624-495F-8C9F-6020A9A58B6B"), UUID.fromString("D8499B04-0E66-4726-AB29-64469D734E0D"), UUID.fromString("9F3D476D-C118-4544-8365-64846904B48E"), UUID.fromString("2AD3F246-FEE1-4E67-B886-69FD380BB150")};

    public ItemBlackPantherSuit(SuitSet suitSet, EntityEquipmentSlot armorSlot) {
        super(suitSet, armorSlot);
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        Multimap<String, AttributeModifier> multimap = HashMultimap.<String, AttributeModifier>create();

        if (equipmentSlot == this.armorType) {
            multimap.put(SharedMonsterAttributes.ARMOR.getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Armor modifier", (double) this.damageReduceAmount * HEConfig.BLACK_PANTHER_ARMOR_MULTIPLIER, 0));
            multimap.put(SharedMonsterAttributes.ARMOR_TOUGHNESS.getName(), new AttributeModifier(ARMOR_MODIFIERS[equipmentSlot.getIndex()], "Armor toughness", (double) this.toughness * HEConfig.BLACK_PANTHER_ARMOR_MULTIPLIER, 0));
        }

        return multimap;
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        if (!isArmorOpen(null, stack))
            return HashMultimap.<String, AttributeModifier>create();
        return super.getAttributeModifiers(slot, stack);
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
        if (isArmorOpen(player, itemStack)) {
            for (EntityEquipmentSlot slots : EntityEquipmentSlot.values()) {
                if (slots != EntityEquipmentSlot.CHEST && slots.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                    player.dropItem(player.getItemStackFromSlot(slots), false);
                    player.setItemStackToSlot(slots, ItemStack.EMPTY);
                }
            }
        }

        if (itemStack.hasTagCompound()) {
            NBTTagCompound nbt = itemStack.getTagCompound();
            int timer = nbt.getInteger("OpenTimer");
            boolean b = false;
            if (timer > 0 && isArmorOpen(player, itemStack)) {
                timer--;
                b = true;
            } else if (timer < SuitSetBlackPanther.MAX_TIMER && !isArmorOpen(player, itemStack)) {
                timer++;
                b = true;
            }

            if (b) {
                nbt.setInteger("OpenTimer", timer);
                itemStack.setTagCompound(nbt);
            }
        }

        super.onArmorTick(world, player, itemStack);
    }
}
