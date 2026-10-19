package net.threetag.palladium.menu;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.threetag.palladium.block.PalladiumBlocks;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.item.MultiversalExtrapolatorItem;
import net.threetag.palladium.item.recipe.MultiversalIteratorRecipe;
import net.threetag.palladium.item.recipe.MultiversalIteratorRecipeInput;
import net.threetag.palladium.item.recipe.MultiversalIteratorRecipes;
import net.threetag.palladium.sound.PalladiumSoundEvents;

import java.util.List;

public class MultiversalIteratorMenu extends ItemCombinerMenu {

    public static final int CYCLE_BUTTON_ID = 0;
    public static final int EXTRAPOLATOR_SLOT = 0;
    public static final int INPUT_SLOT = 1;
    public static final int RESULT_SLOT = 2;

    private final DataSlot resultCount = DataSlot.standalone();
    private List<ItemStack> results = List.of();
    private int resultIndex = 0;

    public MultiversalIteratorMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, ContainerLevelAccess.NULL);
    }

    public MultiversalIteratorMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(PalladiumMenuTypes.MULTIVERSAL_ITERATOR.get(), containerId, playerInventory, access, createInputSlotDefinitions());
        this.addDataSlot(this.resultCount);
    }

    private static ItemCombinerMenuSlotDefinition createInputSlotDefinitions() {
        return ItemCombinerMenuSlotDefinition.create()
                .withSlot(EXTRAPOLATOR_SLOT, 38, 20, stack -> stack.has(PalladiumDataComponents.Items.EXTRAPOLATOR_UNIVERSE))
                .withSlot(INPUT_SLOT, 48, 49, stack -> true)
                .withResultSlot(RESULT_SLOT, 108, 49)
                .build();
    }

    @Override
    protected void onTake(Player player, ItemStack stack) {
        stack.onCraftedBy(player, stack.getCount());
        this.damageExtrapolator();
        this.shrinkInput();
        this.access.execute((level, pos) -> level.playSound(null, pos, PalladiumSoundEvents.MULTIVERSAL_VARIANT_CRAFTED.get(), SoundSource.PLAYERS, 1F, 1F));
    }

    private void shrinkInput() {
        ItemStack stack = this.inputSlots.getItem(INPUT_SLOT);
        if (!stack.isEmpty()) {
            stack.shrink(1);
            this.inputSlots.setItem(INPUT_SLOT, stack);
        }
    }

    private void damageExtrapolator() {
        ItemStack stack = this.inputSlots.getItem(EXTRAPOLATOR_SLOT);
        if (!stack.isEmpty() && this.player.level() instanceof ServerLevel serverLevel) {
            stack.hurtAndBreak(1, serverLevel, this.player, item -> this.inputSlots.setItem(EXTRAPOLATOR_SLOT, ItemStack.EMPTY));
            this.inputSlots.setItem(EXTRAPOLATOR_SLOT, stack);
        }
    }

    @Override
    protected boolean isValidBlock(BlockState state) {
        return state.is(PalladiumBlocks.MULTIVERSAL_ITERATOR.get());
    }

    @Override
    public void createResult() {
        var level = this.player.level();
        var registries = level.registryAccess();
        var input = new MultiversalIteratorRecipeInput(this.inputSlots.getItem(EXTRAPOLATOR_SLOT), this.inputSlots.getItem(INPUT_SLOT));

        this.results = MultiversalIteratorRecipes.getRecipes(level).stream()
                .flatMap(holder -> holder.value().getConversions(input, registries).stream())
                .map(MultiversalIteratorRecipe.Conversion::result)
                .toList();
        this.resultIndex = 0;

        if (this.results.isEmpty()) {
            this.resultCount.set(0);
            this.resultSlots.setItem(0, ItemStack.EMPTY);
        } else {
            this.resultCount.set(this.results.size());
            this.updateResultSlot();
        }
    }

    private void updateResultSlot() {
        this.resultSlots.setItem(0, this.results.get(this.resultIndex).copy());
    }

    @Override
    public boolean clickMenuButton(Player player, int buttonId) {
        if (buttonId == CYCLE_BUTTON_ID) {
            this.cycleResult();
            return true;
        }

        return false;
    }

    public void cycleResult() {
        if (this.results.size() > 1) {
            this.resultIndex = this.resultIndex >= this.results.size() - 1 ? 0 : this.resultIndex + 1;
            this.updateResultSlot();
        }
    }

    public int getResultCount() {
        return this.resultCount.get();
    }
}
