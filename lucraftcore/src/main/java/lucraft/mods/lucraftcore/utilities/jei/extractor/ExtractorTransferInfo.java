package lucraft.mods.lucraftcore.utilities.jei.extractor;

import lucraft.mods.lucraftcore.utilities.container.ContainerExtractor;
import lucraft.mods.lucraftcore.utilities.jei.LCJEIPlugin;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import net.minecraft.inventory.Slot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExtractorTransferInfo implements IRecipeTransferInfo<ContainerExtractor> {

    @Override
    public Class<ContainerExtractor> getContainerClass() {
        return ContainerExtractor.class;
    }

    @Override
    public String getRecipeCategoryUid() {
        return LCJEIPlugin.EXTRACTOR;
    }

    @Override
    public boolean canHandle(ContainerExtractor container) {
        return true;
    }

    @Override
    public List<Slot> getRecipeSlots(ContainerExtractor container) {
        return Arrays.asList(container.getSlot(2), container.getSlot(3));
    }

    @Override
    public List<Slot> getInventorySlots(ContainerExtractor container) {
        List<Slot> slots = new ArrayList<>();
        for (int i = 6; i < 41; i++) {
            slots.add(container.getSlot(i));
        }
        return slots;
    }

}
