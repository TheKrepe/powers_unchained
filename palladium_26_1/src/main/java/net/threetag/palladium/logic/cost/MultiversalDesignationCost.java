package net.threetag.palladium.logic.cost;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.icon.ItemIcon;
import net.threetag.palladium.item.PalladiumItems;
import net.threetag.palladium.item.multiverse.Universe;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class MultiversalDesignationCost extends Cost {

    public static final MapCodec<MultiversalDesignationCost> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceKey.codec(PalladiumRegistryKeys.MULTIVERSE).fieldOf("universe").forGetter(c -> c.universe),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("amount", 1).forGetter(c -> c.amount)
    ).apply(instance, MultiversalDesignationCost::new));

    private final ResourceKey<Universe> universe;
    private final int amount;

    public MultiversalDesignationCost(ResourceKey<Universe> universe, int amount) {
        this.universe = universe;
        this.amount = amount;
    }

    private boolean isDesignation(ItemStack stack) {
        return this.universe.equals(stack.get(PalladiumDataComponents.Items.EXTRAPOLATOR_UNIVERSE));
    }

    @Override
    public boolean hasEnoughCurrency(LivingEntity entity) {
        return entity instanceof Player player && ItemCost.countItems(player, this::isDesignation) >= this.amount;
    }

    @Override
    public void consumeCurrency(LivingEntity entity) {
        if (entity instanceof Player player) {
            ItemCost.removeItems(player, this::isDesignation, this.amount);
        }
    }

    @Override
    public CostDisplay createDisplay() {
        return new CostDisplay(
                new ItemIcon(PalladiumItems.MULTIVERSAL_EXTRAPOLATOR.get()),
                this.amount,
                Component.translatable("gui.palladium.powers.buy_ability.multiversal_designation" + (this.amount > 1 ? "_plural" : ""), this.amount, Universe.getTitle(this.universe.identifier()))
        );
    }

    @Override
    public CostSerializer<?> getSerializer() {
        return CostSerializers.MULTIVERSAL_DESIGNATION.value();
    }

    public static class Serializer extends CostSerializer<MultiversalDesignationCost> {

        @Override
        public MapCodec<MultiversalDesignationCost> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Cost, MultiversalDesignationCost> builder, HolderLookup.Provider provider) {
            builder.setName("Multiversal Designation Cost").setDescription("Requires items that carry a designation of a specific universe, like a Multiversal Extrapolator that has found that universe. The items are consumed.")
                    .add("universe", TYPE_UNIVERSE, "The universe the items must be designated to.")
                    .addOptional("amount", TYPE_POSITIVE_INT, "How many designated items to consume.", 1)
                    .addExampleObject(new MultiversalDesignationCost(ResourceKey.create(PalladiumRegistryKeys.MULTIVERSE, Identifier.fromNamespaceAndPath("example", "earth_616")), 1));
        }
    }
}
