package net.threetag.palladium.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Util;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.neoforged.neoforge.common.Tags;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.item.upgrade.ItemUpgrade;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ItemUpgradeTemplateItem extends SmithingTemplateItem {

    public static final Component APPLIES_TO = Component.translatable(
            Util.makeDescriptionId("item", Palladium.id("smithing_template.item_upgrade.applies_to"))).withStyle(ChatFormatting.BLUE);
    public static final Component INGREDIENTS = Component.translatable(
            Util.makeDescriptionId("item", Palladium.id("smithing_template.item_upgrade.ingredients"))).withStyle(ChatFormatting.BLUE);
    public static final Component BASE_SLOT_DESCRIPTION = Component.translatable(
            Util.makeDescriptionId("item", Palladium.id("smithing_template.item_upgrade.base_slot_description")));
    public static final Component INCOMPATIBLE_WITH = Component.translatable(
            Util.makeDescriptionId("item", Palladium.id("smithing_template.item_upgrade.incompatible_with"))).withStyle(ChatFormatting.GRAY);

    public static final MapCodec<ItemUpgradeTemplateItem> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(PalladiumRegistryKeys.ITEM_UPGRADE).fieldOf("upgrade").forGetter(ItemUpgradeTemplateItem::getUpgrade),
            ComponentSerialization.CODEC.optionalFieldOf("applies_to").forGetter(i -> i.appliesTo),
            ItemTypes.propertiesCodec()
    ).apply(instance, ItemUpgradeTemplateItem::new));

    private final ResourceKey<ItemUpgrade> upgrade;
    private final Optional<Component> appliesTo;

    public ItemUpgradeTemplateItem(ResourceKey<ItemUpgrade> upgrade, Properties properties) {
        this(upgrade, Optional.empty(), properties);
    }

    public ItemUpgradeTemplateItem(ResourceKey<ItemUpgrade> upgrade, TagKey<Item> appliesToTag, Properties properties) {
        this(upgrade, Optional.of(Component.translatable(Tags.getTagTranslationKey(appliesToTag)).withStyle(ChatFormatting.BLUE)), properties);
    }

    public ItemUpgradeTemplateItem(ResourceKey<ItemUpgrade> upgrade, Optional<Component> appliesTo, Properties properties) {
        super(appliesTo.orElse(APPLIES_TO), INGREDIENTS, BASE_SLOT_DESCRIPTION, Component.empty(),
                List.of(Identifier.withDefaultNamespace("container/slot/chestplate")), List.of(), properties);
        this.upgrade = upgrade;
        this.appliesTo = appliesTo;
    }

    public ResourceKey<ItemUpgrade> getUpgrade() {
        return this.upgrade;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, context, display, builder, tooltipFlag);

        HolderLookup.Provider registries = context.registries();
        if (registries == null) {
            return;
        }

        ItemUpgrade upgrade = ItemUpgrade.get(registries, this.upgrade);
        if (upgrade == null) {
            return;
        }

        if (upgrade.incompatibilities().size() > 0) {
            builder.accept(INCOMPATIBLE_WITH);
            for (Holder<ItemUpgrade> incompatible : upgrade.incompatibilities()) {
                incompatible.unwrapKey().ifPresent(key ->
                        builder.accept(CommonComponents.space().append(ItemUpgrade.getName(key)).withStyle(ChatFormatting.RED)));
            }
        }
    }

}
