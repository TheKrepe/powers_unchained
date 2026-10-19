package net.threetag.palladium.compat.jei.tailoring;

import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.transfer.IRecipeTransferContext;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.gui.screen.menu.ClientTailoringRecipeBook;
import net.threetag.palladium.item.recipe.TailoringRecipe;
import net.threetag.palladium.menu.PalladiumMenuTypes;
import net.threetag.palladium.menu.TailoringMenu;
import net.threetag.palladium.network.TailoringCraftPacket;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TailoringTransferHandler implements IRecipeTransferHandler<TailoringMenu, TailoringRecipe> {

    private final IRecipeTransferHandlerHelper transferHelper;

    public TailoringTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
        this.transferHelper = transferHelper;
    }

    @Override
    public Class<? extends TailoringMenu> getContainerClass() {
        return TailoringMenu.class;
    }

    @Override
    public Optional<MenuType<TailoringMenu>> getMenuType() {
        return Optional.of(PalladiumMenuTypes.TAILORING.get());
    }

    @Override
    public IRecipeType<TailoringRecipe> getRecipeType() {
        return TailoringCategory.RECIPE_TYPE;
    }

    @SuppressWarnings("removal")
    @Override
    public @Nullable IRecipeTransferError transferRecipe(TailoringMenu container, TailoringRecipe recipe, IRecipeSlotsView recipeSlots, Player player, boolean maxTransfer, boolean doTransfer) {
        return null;
    }

    @Override
    public @org.jspecify.annotations.Nullable IRecipeTransferError transferRecipe(IRecipeTransferContext<TailoringRecipe, TailoringMenu> context, boolean doTransfer) {
        var recipe = context.getRecipe();
        var recipeSlots = context.getRecipeSlots();
        var container = context.getContainer();

        if (!transferHelper.recipeTransferHasServerSupport()) {
            return transferHelper.createUserErrorWithTooltip(Component.translatable("jei.tooltip.error.recipe.transfer.no.server"));
        }

        var recipeKey = ClientTailoringRecipeBook.getKeyForRecipe(recipe);
        if (recipeKey == null) {
            return transferHelper.createInternalError();
        }

        var inputSlotViews = recipeSlots.getSlotViews(RecipeIngredientRole.INPUT);

        if (inputSlotViews.isEmpty()) {
            Palladium.LOGGER.error("Tailoring recipe transfer: Expected at least one input slot view");
            return transferHelper.createInternalError();
        }
        var toolSlotView = inputSlotViews.get(0);

        var availableItemStacks = getAvailableItemStacks(container);

        Slot toolFoundSlot = null;
        if (!toolSlotView.isEmpty()) {
            for (var availableSlotStack : availableItemStacks.entrySet()) {
                ItemStack availableStack = availableSlotStack.getValue();
                if (recipe.toolIngredient().test(availableStack)) {
                    toolFoundSlot = availableSlotStack.getKey();
                }
            }
        }

        if (toolFoundSlot == null) {
            return transferHelper.createUserErrorWithTooltip(Component.translatable("jei.tooltip.error.recipe.transfer.missing"));
        }

        if (doTransfer) {
            ClientPacketDistributor.sendToServer(new TailoringCraftPacket(recipeKey));
        }

        return null;
    }

    private Map<Slot, ItemStack> getAvailableItemStacks(TailoringMenu container) {
        Map<Slot, ItemStack> availableItemStacks = new HashMap<>();
        Slot toolSlot = container.getSlot(0);
        ItemStack toolStack = toolSlot.getItem();
        if (!toolStack.isEmpty()) {
            availableItemStacks.put(toolSlot, toolStack);
        }

        int playerInvStart = 1 + 4;
        int playerInvSize = 36;
        for (int i = playerInvStart; i < playerInvStart + playerInvSize; i++) {
            Slot invSlot = container.getSlot(i);
            ItemStack playerStack = invSlot.getItem();
            if (!playerStack.isEmpty()) {
                availableItemStacks.put(invSlot, playerStack);
            }
        }

        return availableItemStacks;
    }
}
