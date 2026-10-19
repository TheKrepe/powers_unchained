package net.threetag.pymtech.data;

import net.minecraft.data.BlockTagsProvider;
import net.minecraft.data.DataGenerator;

public class PTBlockTagProvider extends BlockTagsProvider {

    public PTBlockTagProvider(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    protected void registerTags() {

    }

    @Override
    public String getName() {
        return "PymTech Block Tags";
    }
}
