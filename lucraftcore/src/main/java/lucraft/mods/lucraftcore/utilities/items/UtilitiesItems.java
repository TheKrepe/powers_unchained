package lucraft.mods.lucraftcore.utilities.items;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.helper.ItemHelper;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import lucraft.mods.lucraftcore.util.items.ItemBase;
import lucraft.mods.lucraftcore.util.items.ItemBaseEnergyStorage;
import lucraft.mods.lucraftcore.utilities.recipes.RecipeInstructionCloning;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.Map;

public class UtilitiesItems {

    public static Item HAMMER = new ItemHammer("hammer");
    public static Item WIRE_CUTTER = new ItemHammer("wire_cutter");
    public static Item INJECTION = new ItemInjection("injection");
    public static Item INSTRUCTION = new ItemInstruction("instruction");
    public static Item LV_CAPACITOR = new ItemBaseEnergyStorage("lv_capacitor", 100000).setCreativeTab(LucraftCore.CREATIVE_TAB);
    public static Item MV_CAPACITOR = new ItemBaseEnergyStorage("mv_capacitor", 1000000).setCreativeTab(LucraftCore.CREATIVE_TAB);
    public static Item HV_CAPACITOR = new ItemBaseEnergyStorage("hv_capacitor", 4000000).setCreativeTab(LucraftCore.CREATIVE_TAB);
    public static Item SERVO_MOTOR = new ItemBase("servo_motor").setCreativeTab(LucraftCore.CREATIVE_TAB);
    public static Item BASIC_CIRCUIT = new ItemBase("basic_circuit").setCreativeTab(LucraftCore.CREATIVE_TAB);
    public static Item ADVANCED_CIRCUIT = new ItemBase("advanced_circuit").setCreativeTab(LucraftCore.CREATIVE_TAB);
    public static Map<EnumDyeColor, ItemTriPolymer> TRI_POLYMER = new HashMap<>();

    @SubscribeEvent
    public void onRegisterItems(RegistryEvent.Register<Item> e) {
        e.getRegistry().register(HAMMER);
        e.getRegistry().register(WIRE_CUTTER);
        e.getRegistry().register(INJECTION);
        e.getRegistry().register(INSTRUCTION);
        e.getRegistry().register(LV_CAPACITOR);
        e.getRegistry().register(MV_CAPACITOR);
        e.getRegistry().register(HV_CAPACITOR);
        e.getRegistry().register(SERVO_MOTOR);
        e.getRegistry().register(BASIC_CIRCUIT);
        e.getRegistry().register(ADVANCED_CIRCUIT);

        for (EnumDyeColor color : EnumDyeColor.values()) {
            ItemTriPolymer item = new ItemTriPolymer(color);
            TRI_POLYMER.put(color, item);
            e.getRegistry().register(item);
            OreDictionary.registerOre("triPolymer" + color.getName().substring(0, 1).toLowerCase() + color.getName().substring(1), SERVO_MOTOR);
        }

        OreDictionary.registerOre("servoMotor", SERVO_MOTOR);
        OreDictionary.registerOre("circuitBasic", BASIC_CIRCUIT);
        OreDictionary.registerOre("circuitAdvanced", ADVANCED_CIRCUIT);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void onRegisterModels(ModelRegistryEvent e) {
        ItemHelper.registerItemModel(HAMMER, LucraftCore.MODID, "hammer");
        ItemHelper.registerItemModel(WIRE_CUTTER, LucraftCore.MODID, "wire_cutter");
        ItemHelper.registerItemModel(INJECTION, LucraftCore.MODID, "injection");
        ItemHelper.registerItemModel(INSTRUCTION, LucraftCore.MODID, "instruction");
        ItemHelper.registerItemModel(LV_CAPACITOR, LucraftCore.MODID, "lv_capacitor");
        ItemHelper.registerItemModel(MV_CAPACITOR, LucraftCore.MODID, "mv_capacitor");
        ItemHelper.registerItemModel(HV_CAPACITOR, LucraftCore.MODID, "hv_capacitor");
        ItemHelper.registerItemModel(SERVO_MOTOR, LucraftCore.MODID, "servo_motor");
        ItemHelper.registerItemModel(BASIC_CIRCUIT, LucraftCore.MODID, "basic_circuit");
        ItemHelper.registerItemModel(ADVANCED_CIRCUIT, LucraftCore.MODID, "advanced_circuit");

        for (EnumDyeColor color : EnumDyeColor.values()) {
            ModelLoader.setCustomModelResourceLocation(TRI_POLYMER.get(color), 0, new ModelResourceLocation(new ResourceLocation(LucraftCore.MODID, "tri_polymer"), StringHelper.unlocalizedToResourceName(color.getTranslationKey())));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRegisterRecipes(RegistryEvent.Register<IRecipe> e) {
        e.getRegistry().register(new RecipeInstructionCloning().setRegistryName("clone_instruction"));
    }

}
