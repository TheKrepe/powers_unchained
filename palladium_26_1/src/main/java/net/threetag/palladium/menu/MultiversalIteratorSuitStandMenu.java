package net.threetag.palladium.menu;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.threetag.palladium.block.PalladiumBlocks;
import net.threetag.palladium.component.PalladiumDataComponents;
import net.threetag.palladium.item.MultiversalExtrapolatorItem;
import net.threetag.palladium.item.recipe.MultiversalIteratorRecipe;
import net.threetag.palladium.item.recipe.MultiversalIteratorRecipeInput;
import net.threetag.palladium.item.recipe.MultiversalIteratorRecipes;
import net.threetag.palladium.sound.PalladiumSoundEvents;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;

public class MultiversalIteratorSuitStandMenu extends AbstractContainerMenu {

    public static final int EXTRAPOLATOR_SLOT = 0;
    private static final int INVENTORY_SLOT_START = 1;
    private static final int INVENTORY_SLOT_END = 28;
    private static final int HOTBAR_SLOT_START = 28;
    private static final int HOTBAR_SLOT_END = 37;

    private final SimpleContainer extrapolatorSlotContainer = this.createContainer();
    protected final ContainerLevelAccess access;
    protected final Player player;
    public final ArmorStand suitStand;
    protected List<IterationPage> pages = List.of();
    protected int selectedIndex = 0;
    private List<ItemStack> equipmentSnapshot = List.of();

    public MultiversalIteratorSuitStandMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, (ArmorStand) playerInventory.player.level().getEntity(buf.readVarInt()), ContainerLevelAccess.NULL);
    }

    public MultiversalIteratorSuitStandMenu(int containerId, Inventory playerInventory, ArmorStand suitStand, ContainerLevelAccess access) {
        super(PalladiumMenuTypes.MULTIVERSAL_ITERATOR_SUIT_STAND.get(), containerId);
        this.player = playerInventory.player;
        this.access = access;
        this.suitStand = suitStand;

        this.addSlot(new Slot(this.extrapolatorSlotContainer, 0, 38, 20) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.has(PalladiumDataComponents.Items.EXTRAPOLATOR_UNIVERSE);
            }
        });

        this.addStandardInventorySlots(playerInventory, 8, 132);
        this.updatePages();
    }

    protected void updatePages() {
        this.pages = createPages(this.suitStand, this.extrapolatorSlotContainer.getItem(0));
        this.selectedIndex = 0;
        this.equipmentSnapshot = snapshotEquipment(this.suitStand);
    }

    public void updatePagesIfEquipmentChanged() {
        var current = snapshotEquipment(this.suitStand);

        for (int i = 0; i < current.size(); i++) {
            if (!ItemStack.matches(current.get(i), this.equipmentSnapshot.get(i))) {
                this.updatePages();
                return;
            }
        }
    }

    private static List<ItemStack> snapshotEquipment(ArmorStand suitStand) {
        List<ItemStack> snapshot = new ArrayList<>();

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            snapshot.add(suitStand.getItemBySlot(slot).copy());
        }

        return snapshot;
    }

    public List<IterationPage> getPages() {
        return this.pages;
    }

    public IterationPage getSelectedPage() {
        return this.pages.get(this.selectedIndex);
    }

    public void switchPage(boolean forward) {
        if (forward) {
            this.selectedIndex = this.selectedIndex >= this.pages.size() - 1 ? 0 : this.selectedIndex + 1;
        } else {
            this.selectedIndex = this.selectedIndex <= 0 ? this.pages.size() - 1 : this.selectedIndex - 1;
        }
    }

    @Override
    public boolean clickMenuButton(Player player, int pageId) {
        if (pageId <= 0) {
            return false;
        }

        var page = this.pages.stream().filter(p -> p.id() == pageId).findFirst().orElse(null);

        if (page == null) {
            return false;
        }

        this.damageExtrapolator(page.apply(this.suitStand));
        this.access.execute((level, pos) -> level.playSound(null, pos, PalladiumSoundEvents.MULTIVERSAL_VARIANT_CRAFTED.get(), SoundSource.PLAYERS, 1F, 1F));
        this.updatePages();
        return true;
    }

    private void damageExtrapolator(int amount) {
        ItemStack stack = this.extrapolatorSlotContainer.getItem(0);
        if (!stack.isEmpty() && this.player.level() instanceof ServerLevel serverLevel) {
            stack.hurtAndBreak(amount, serverLevel, this.player, item -> this.extrapolatorSlotContainer.setItem(0, ItemStack.EMPTY));
            this.extrapolatorSlotContainer.setItem(0, stack);
        }
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);

        if (container == this.extrapolatorSlotContainer) {
            this.updatePages();
        }
    }

    @Override
    public @NotNull ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            itemStack = slotStack.copy();
            if (index != EXTRAPOLATOR_SLOT) {
                if (slotStack.getItem() instanceof MultiversalExtrapolatorItem) {
                    if (!this.moveItemStackTo(slotStack, EXTRAPOLATOR_SLOT, EXTRAPOLATOR_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index < INVENTORY_SLOT_END) {
                    if (!this.moveItemStackTo(slotStack, HOTBAR_SLOT_START, HOTBAR_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(slotStack, INVENTORY_SLOT_START, INVENTORY_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, INVENTORY_SLOT_START, HOTBAR_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == itemStack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, slotStack);
        }

        return itemStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.suitStand != null && this.suitStand.isAlive() && this.access.evaluate(
                (level, pos) -> level.getBlockState(pos).is(PalladiumBlocks.MULTIVERSAL_ITERATOR.get()) && player.isWithinBlockInteractionRange(pos, 4.0),
                true
        );
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.access.execute((level, pos) -> this.clearContainer(player, this.extrapolatorSlotContainer));
    }

    private SimpleContainer createContainer() {
        return new SimpleContainer(1) {
            @Override
            public void setChanged() {
                super.setChanged();
                MultiversalIteratorSuitStandMenu.this.slotsChanged(this);
            }
        };
    }

    /**
     * Builds the pages deterministically from the suit stand's equipment, the extrapolator and the synced
     * multiversal iterator recipes, so server and client always agree on page ids. Conversions of one recipe
     * that share the same {@link MultiversalIteratorRecipe.Conversion#key() key} across equipment slots are
     * grouped into a single page.
     */
    public static List<IterationPage> createPages(ArmorStand suitStand, ItemStack extrapolator) {
        List<IterationPage> pages = new ArrayList<>();
        Map<EquipmentSlot, ItemStack> equipment = new EnumMap<>(EquipmentSlot.class);

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            equipment.put(slot, suitStand.getItemBySlot(slot));
        }

        pages.add(new IterationPage(0, equipment));

        var level = suitStand.level();
        var registries = level.registryAccess();
        int id = 1;

        for (RecipeHolder<MultiversalIteratorRecipe> holder : MultiversalIteratorRecipes.getRecipes(level)) {
            Map<Identifier, Map<EquipmentSlot, ItemStack>> grouped = new TreeMap<>(Comparator.comparing(Identifier::toString));

            for (Map.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
                var stack = entry.getValue();

                if (stack.isEmpty()) {
                    continue;
                }

                var input = new MultiversalIteratorRecipeInput(extrapolator, stack);

                for (MultiversalIteratorRecipe.Conversion conversion : holder.value().getConversions(input, registries)) {
                    grouped.computeIfAbsent(conversion.key(), key -> new EnumMap<>(EquipmentSlot.class)).put(entry.getKey(), conversion.result());
                }
            }

            for (Map<EquipmentSlot, ItemStack> converted : grouped.values()) {
                pages.add(new IterationPage(id++, converted));
            }
        }

        return pages;
    }

    public record IterationPage(int id, Map<EquipmentSlot, ItemStack> items) {

        public int apply(ArmorStand suitStand) {
            int changed = 0;

            for (Map.Entry<EquipmentSlot, ItemStack> entry : this.items.entrySet()) {
                suitStand.setItemSlot(entry.getKey(), entry.getValue().copy());
                changed++;
            }

            return changed;
        }

        public void pretend(ArmorStand suitStand, Consumer<ArmorStand> consumer) {
            Map<EquipmentSlot, ItemStack> cached = new EnumMap<>(EquipmentSlot.class);

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                cached.put(slot, suitStand.getItemBySlot(slot));

                if (this.items.containsKey(slot)) {
                    suitStand.setItemSlot(slot, this.items.get(slot));
                }
            }

            consumer.accept(suitStand);

            for (EquipmentSlot slot : EquipmentSlot.values()) {
                suitStand.setItemSlot(slot, cached.getOrDefault(slot, ItemStack.EMPTY));
            }
        }
    }
}
