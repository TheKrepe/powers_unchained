package net.threetag.palladium.item.tab;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.threetag.palladium.item.multiverse.MultiversalItemVariant;

import java.util.ArrayList;
import java.util.List;

public final class SectionItemLayout {

    private static final int ROW = 9;

    public static List<ItemStack> resolve(List<TabContentEntry> entries, HolderLookup.Provider registries) {
        List<Unit> units = new ArrayList<>();
        boolean layout = false;
        for (TabContentEntry entry : entries) {
            if (entry instanceof TabContentEntry.GroupEntry(VariantGroup group1)) {
                for (List<ItemStack> group : MultiversalItemVariant.collectVariantGroups(registries, group1)) {
                    if (group.isEmpty()) {
                        continue;
                    }
                    List<ItemStack> aligned = alignToArmorSlots(group);
                    units.add(new Unit(aligned != null ? aligned : group, true));
                    if (group.size() >= 4) {
                        layout = true;
                    }
                }
            } else {
                for (ItemStack stack : entry.resolve(registries)) {
                    units.add(new Unit(List.of(stack), false));
                }
            }
        }

        List<ItemStack> out = new ArrayList<>();
        if (!layout) {
            for (Unit unit : units) {
                for (ItemStack stack : unit.stacks()) {
                    if (!stack.isEmpty()) {
                        out.add(stack);
                    }
                }
            }
            return out;
        }

        boolean firstAlignedGroup = true;
        for (Unit unit : units) {
            if (!unit.group()) {
                out.addAll(unit.stacks());
                continue;
            }

            int size = unit.stacks().size();
            if (size == 4) {
                if (firstAlignedGroup || out.size() % ROW != 4) {
                    padToRowStart(out);
                } else {
                    out.add(ItemStack.EMPTY);
                }
                out.addAll(unit.stacks());
                firstAlignedGroup = false;
            } else if (size <= 9) {
                padToRowStart(out);
                out.addAll(unit.stacks());
                padToRowStart(out);
                firstAlignedGroup = false;
            } else {
                out.addAll(unit.stacks());
            }
        }
        return out;
    }

    private static List<ItemStack> alignToArmorSlots(List<ItemStack> group) {
        if (group.size() > 4) {
            return null;
        }
        ItemStack[] slots = new ItemStack[4];
        for (ItemStack stack : group) {
            Equippable equippable = stack.getItem().components().get(DataComponents.EQUIPPABLE);
            if (equippable == null) {
                return null;
            }
            int index = switch (equippable.slot()) {
                case HEAD -> 0;
                case CHEST -> 1;
                case LEGS -> 2;
                case FEET -> 3;
                default -> -1;
            };
            if (index < 0 || slots[index] != null) {
                return null;
            }
            slots[index] = stack;
        }
        List<ItemStack> aligned = new ArrayList<>(slots.length);
        for (ItemStack slot : slots) {
            aligned.add(slot == null ? ItemStack.EMPTY : slot);
        }
        return aligned;
    }

    private static void padToRowStart(List<ItemStack> out) {
        while (out.size() % ROW != 0) {
            out.add(ItemStack.EMPTY);
        }
    }

    private record Unit(List<ItemStack> stacks, boolean group) {
    }
}
