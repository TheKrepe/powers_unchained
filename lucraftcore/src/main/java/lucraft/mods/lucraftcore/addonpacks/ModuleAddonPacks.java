package lucraft.mods.lucraftcore.addonpacks;

import lucraft.mods.lucraftcore.module.Module;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ModuleAddonPacks extends Module {

    public static final ModuleAddonPacks INSTANCE = new ModuleAddonPacks();

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        AddonPackHandler.load();
    }

    @Override
    public void init(FMLInitializationEvent event) {

    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {

    }

    @SideOnly(Side.CLIENT)
    @Override
    public void initClient(FMLInitializationEvent event) {
        AddonPackHandler.loadImages();
    }

    @Override
    public String getName() {
        return "AddonPacks";
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
