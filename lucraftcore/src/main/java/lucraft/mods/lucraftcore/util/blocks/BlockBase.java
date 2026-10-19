package lucraft.mods.lucraftcore.util.blocks;

import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;

public class BlockBase extends Block {

    public String name;

    public BlockBase(String name, Material materialIn) {
        super(materialIn);

        this.setTranslationKey(name);
        this.setRegistryName(StringHelper.unlocalizedToResourceName(name));
        this.name = name;
        this.setCreativeTab(CreativeTabs.MISC);
    }

}
