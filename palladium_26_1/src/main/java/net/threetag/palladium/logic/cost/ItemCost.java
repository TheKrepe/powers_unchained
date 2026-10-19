package net.threetag.palladium.logic.cost;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.icon.IngredientIcon;

import java.util.function.Predicate;

public class ItemCost extends Cost {

    public static final MapCodec<ItemCost> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(c -> c.ingredient),
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("amount", 1).forGetter(c -> c.amount)
    ).apply(instance, ItemCost::new));

    private final Ingredient ingredient;
    private final int amount;

    public ItemCost(Ingredient ingredient, int amount) {
        this.ingredient = ingredient;
        this.amount = amount;
    }

    @Override
    public boolean hasEnoughCurrency(LivingEntity entity) {
        return entity instanceof Player player && countItems(player, this.ingredient) >= this.amount;
    }

    @Override
    public void consumeCurrency(LivingEntity entity) {
        if (entity instanceof Player player) {
            removeItems(player, this.ingredient, this.amount);
        }
    }

    static int countItems(Player player, Predicate<ItemStack> predicate) {
        int found = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && predicate.test(stack)) {
                found += stack.getCount();
            }
        }
        return found;
    }

    static void removeItems(Player player, Predicate<ItemStack> predicate, int amount) {
        int remove = amount;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && predicate.test(stack)) {
                int willRemove = Math.min(remove, stack.getCount());
                player.getInventory().removeItem(i, willRemove);
                remove -= willRemove;
                if (remove <= 0) break;
            }
        }
    }

    @Override
    public CostDisplay createDisplay() {
        var stacks = this.ingredient.items().toList();
        var component = Component.empty();
        for (int i = 0; i < stacks.size(); i++) {
            component.append(stacks.get(i).value().getName(stacks.get(i).value().getDefaultInstance()));
            if (i < stacks.size() - 1) {
                if (i == stacks.size() - 2) {
                    component.append(" ").append(Component.translatable("gui.palladium.powers.buy_ability.or")).append(" ");
                } else {
                    component.append(", ");
                }
            }
        }
        return new CostDisplay(new IngredientIcon(this.ingredient), this.amount, component);
    }

    @Override
    public CostSerializer<?> getSerializer() {
        return CostSerializers.ITEM.value();
    }

    public static class Serializer extends CostSerializer<ItemCost> {

        @Override
        public MapCodec<ItemCost> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Cost, ItemCost> builder, HolderLookup.Provider provider) {
            builder.setName("Item Cost").setDescription("Requires the entity to have a certain item in their inventory.")
                    .add("ingredient", TYPE_INGREDIENT, "The item(s) accepted as payment.")
                    .add("amount", TYPE_POSITIVE_INT, "How many items to consume. Defaults to 1.")
                    .addExampleObject(new ItemCost(Ingredient.of(Items.DIAMOND), 5));
        }
    }
}
