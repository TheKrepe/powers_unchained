package lucraft.mods.lucraftcore.materials.items;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.materials.Material;
import lucraft.mods.lucraftcore.materials.Material.MaterialComponent;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashMap;
import java.util.Map;

public class MaterialsItems {

    public static Map<Material, ItemLCMaterial> INGOTS = new HashMap<>();
    public static Map<Material, ItemLCMaterial> DUSTS = new HashMap<>();
    public static Map<Material, ItemLCMaterial> NUGGETS = new HashMap<>();
    public static Map<Material, ItemLCMaterial> PLATES = new HashMap<>();
    public static Map<Material, ItemLCMaterial> WIRINGS = new HashMap<>();

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> e) {
        for (Material m : Material.getMaterials()) {

            if (m.autoGenerateComponent(MaterialComponent.INGOT)) {
                ItemLCMaterial item = new ItemLCMaterial(m, MaterialComponent.INGOT);
                INGOTS.put(m, item);
                e.getRegistry().register(item);
            }

            if (m.autoGenerateComponent(MaterialComponent.DUST)) {
                ItemLCMaterial item = new ItemLCMaterial(m, MaterialComponent.DUST);
                DUSTS.put(m, item);
                e.getRegistry().register(item);
            }

            if (m.autoGenerateComponent(MaterialComponent.NUGGET)) {
                ItemLCMaterial item = new ItemLCMaterial(m, MaterialComponent.NUGGET);
                NUGGETS.put(m, item);
                e.getRegistry().register(item);
            }

            if (m.autoGenerateComponent(MaterialComponent.PLATE)) {
                ItemLCMaterial item = new ItemLCMaterial(m, MaterialComponent.PLATE);
                PLATES.put(m, item);
                e.getRegistry().register(item);
            }

            if (m.autoGenerateComponent(MaterialComponent.WIRING)) {
                ItemLCMaterial item = new ItemLCMaterial(m, MaterialComponent.WIRING);
                WIRINGS.put(m, item);
                e.getRegistry().register(item);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRegisterModels(ModelRegistryEvent e) {
        for (Material m : Material.getMaterials()) {
            if (m.autoGenerateComponent(MaterialComponent.INGOT))
                ModelLoader.setCustomModelResourceLocation(INGOTS.get(m), 0, new ModelResourceLocation(new ResourceLocation(LucraftCore.MODID, "ingot"), m.getResourceName()));
            if (m.autoGenerateComponent(MaterialComponent.DUST))
                ModelLoader.setCustomModelResourceLocation(DUSTS.get(m), 0, new ModelResourceLocation(new ResourceLocation(LucraftCore.MODID, "dust"), m.getResourceName()));
            if (m.autoGenerateComponent(MaterialComponent.NUGGET))
                ModelLoader.setCustomModelResourceLocation(NUGGETS.get(m), 0, new ModelResourceLocation(new ResourceLocation(LucraftCore.MODID, "nugget"), m.getResourceName()));
            if (m.autoGenerateComponent(MaterialComponent.PLATE))
                ModelLoader.setCustomModelResourceLocation(PLATES.get(m), 0, new ModelResourceLocation(new ResourceLocation(LucraftCore.MODID, "plate"), m.getResourceName()));
            if (m.autoGenerateComponent(MaterialComponent.WIRING))
                ModelLoader.setCustomModelResourceLocation(WIRINGS.get(m), 0, new ModelResourceLocation(new ResourceLocation(LucraftCore.MODID, "wiring"), m.getResourceName()));
        }
    }

}
