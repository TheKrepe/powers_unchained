package net.threetag.pymtech.data;

import net.minecraft.data.DataGenerator;
import net.threetag.pymtech.block.PTBlocks;
import net.threetag.threecore.data.BaseLootTableProvider;

public class PTLootTableProvider extends BaseLootTableProvider {

    public PTLootTableProvider(DataGenerator dataGeneratorIn) {
        super(dataGeneratorIn);
    }

    @Override
    protected void addTables() {
        standardBlockTable(PTBlocks.STRUCTURE_SHRINKER.get());
    }

    @Override
    public String getName() {
        return "PymTech " + super.getName();
    }
}
