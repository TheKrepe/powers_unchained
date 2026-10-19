package net.threetag.palladium.logic.condition;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.entity.PlayerSlot;
import net.threetag.palladium.item.upgrade.ItemUpgrade;
import net.threetag.palladium.item.upgrade.ItemUpgrades;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.Optional;

public record ItemUpgradeCondition(ResourceKey<ItemUpgrade> upgrade, Optional<PlayerSlot> slot) implements Condition {

    public static final MapCodec<ItemUpgradeCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance
            .group(
                    ResourceKey.codec(PalladiumRegistryKeys.ITEM_UPGRADE).fieldOf("upgrade").forGetter(ItemUpgradeCondition::upgrade),
                    PlayerSlot.CODEC.optionalFieldOf("slot").forGetter(ItemUpgradeCondition::slot)
            ).apply(instance, ItemUpgradeCondition::new)
    );

    @Override
    public boolean test(DataContext context) {
        LivingEntity entity = context.getLivingEntity();
        if (entity == null) {
            return false;
        }

        Optional<Holder<ItemUpgrade>> holder = ItemUpgrade.getHolder(entity.registryAccess(), this.upgrade);
        if (holder.isEmpty()) {
            return false;
        }

        if (this.slot.isEmpty()) {
            var contextItem = context.getItem();

            if (contextItem.isEmpty()) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    if (ItemUpgrades.get(entity.getItemBySlot(slot)).has(holder.get())) {
                        return true;
                    }
                }
            } else {
                return ItemUpgrades.get(contextItem).has(holder.get());
            }
        } else {
            for (ItemStack stack : this.slot.get().getItems(entity)) {
                if (ItemUpgrades.get(stack).has(holder.get())) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public ConditionSerializer<ItemUpgradeCondition> getSerializer() {
        return ConditionSerializers.ITEM_UPGRADE.get();
    }

    public static class Serializer extends ConditionSerializer<ItemUpgradeCondition> {

        @Override
        public MapCodec<ItemUpgradeCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, ItemUpgradeCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Item Upgrade")
                    .setDescription("Checks if the entity has an equipped item with the given item upgrade installed.")
                    .add("upgrade", TYPE_IDENTIFIER, "ID of the item upgrade to look for.")
                    .addOptional("slot", TYPE_PLAYER_SLOT, "The slot which is checked for the item upgrade. If empty it looks into the slot of the current context.")
                    .addExampleObject(new ItemUpgradeCondition(ResourceKey.create(PalladiumRegistryKeys.ITEM_UPGRADE, Identifier.fromNamespaceAndPath("mypack", "my_upgrade")), Optional.empty()))
                    .addExampleObject(new ItemUpgradeCondition(ResourceKey.create(PalladiumRegistryKeys.ITEM_UPGRADE, Identifier.fromNamespaceAndPath("mypack", "my_upgrade")), Optional.of(PlayerSlot.get(EquipmentSlot.BODY))));
        }
    }

}
