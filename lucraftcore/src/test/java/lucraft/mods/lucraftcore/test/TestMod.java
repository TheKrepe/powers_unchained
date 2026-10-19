package lucraft.mods.lucraftcore.test;

import lucraft.mods.lucraftcore.materials.Material;
import lucraft.mods.lucraftcore.materials.Material.MaterialComponent;
import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityEntry;
import lucraft.mods.lucraftcore.utilities.items.ItemInjection;
import lucraft.mods.lucraftcore.utilities.recipes.InstructionRecipe;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@EventBusSubscriber(modid = TestMod.MODID)
@Mod(modid = TestMod.MODID, version = TestMod.VERSION, name = TestMod.NAME)
public class TestMod {

    public static final String NAME = "Test Mod";
    public static final String MODID = "testmod";
    public static final String VERSION = "1.12-2.0.0";

    public static final ResourceLocation SUPERPOWER_ICON_TEX = new ResourceLocation(TestMod.MODID, "textures/gui/superpower_icons.png");
    public static Superpower TEST = new SuperpowerTest("test").setRegistryName(TestMod.MODID, "test");
    public static TestSuitSet TEST_SUIT_SET = new TestSuitSet("test");

    public static Item testItemExtendedInventory = new ItemTestExtendedInventory();

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {

    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        ItemInjection.registerInjection(new InjectionTest("test"), new ResourceLocation(MODID, "testinjection"));

        InstructionRecipe.registerInstructionRecipe(new InstructionRecipe(new ItemStack(Items.BAKED_POTATO), new ItemStack(Items.APPLE), new ItemStack(Blocks.FURNACE), Material.ADAMANTIUM.getItemStack(MaterialComponent.PLATE, 3)).setRegistryName(TestMod.MODID, "test_recipe"));
        InstructionRecipe.registerInstructionRecipe(new InstructionRecipe(new ItemStack(Items.BREAD), new ItemStack(Items.DIAMOND_CHESTPLATE), new ItemStack(Blocks.DROPPER), Material.ADAMANTIUM.getItemStack(MaterialComponent.PLATE, 3)).setRegistryName(TestMod.MODID, "test_recipe2"));
        InstructionRecipe.registerInstructionRecipe(new InstructionRecipe(new ItemStack(Items.EXPERIENCE_BOTTLE), new ItemStack(Items.BEETROOT_SOUP), new ItemStack(Blocks.ANVIL), Material.ADAMANTIUM.getItemStack(MaterialComponent.PLATE, 3)).setRegistryName(TestMod.MODID, "test_recipe3"));
        InstructionRecipe.registerInstructionRecipe(new InstructionRecipe(new ItemStack(Items.NAME_TAG), new ItemStack(Items.APPLE), new ItemStack(Blocks.FURNACE), Material.ADAMANTIUM.getItemStack(MaterialComponent.PLATE, 3)).setRegistryName(TestMod.MODID, "test_recipe4"));
        InstructionRecipe.registerInstructionRecipe(new InstructionRecipe(new ItemStack(Items.COOKED_RABBIT), new ItemStack(Items.SHEARS), new ItemStack(Blocks.FURNACE), Material.ADAMANTIUM.getItemStack(MaterialComponent.PLATE, 3)).setRegistryName(TestMod.MODID, "test_recipe5"));
        InstructionRecipe.registerInstructionRecipe(new InstructionRecipe(new ItemStack(Items.LAVA_BUCKET), new ItemStack(Items.ARROW), new ItemStack(Blocks.FURNACE), Material.ADAMANTIUM.getItemStack(MaterialComponent.PLATE, 3)).setRegistryName(TestMod.MODID, "test_recipe6"));
        // InstructionRecipe.registerInstructionRecipe(new InstructionRecipe(new
        // ItemStack(Blocks.ANVIL), new ItemStack(Items.STONE_SWORD), new
        // ItemStack(Items.DYE, 2, 15),
        // Material.GOLD_TITANIUM_ALLOY.getItemStack(MaterialComponent.BLOCK,
        // 3)).setRegistryName(TestMod.MODID, "test_recipe2"));
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {

    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> e) {
        TEST_SUIT_SET.registerItems(e);
        e.getRegistry().register(testItemExtendedInventory);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent e) {
        TEST_SUIT_SET.registerModels();
        ModelLoader.setCustomModelResourceLocation(testItemExtendedInventory, 0, new ModelResourceLocation(new ResourceLocation(MODID, "test_item"), "inventory"));
    }

    @SubscribeEvent
    public static void registerSuperpowers(RegistryEvent.Register<Superpower> e) {
        e.getRegistry().register(TEST);
    }

    @SubscribeEvent
    public static void registerAbilities(RegistryEvent.Register<AbilityEntry> e) {
        e.getRegistry().register(new AbilityEntry(AbilityTest.class, new ResourceLocation(TestMod.MODID, "testability")));
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickItem e) {

    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void renderEntityPre(RenderLivingEvent.Pre event) {

    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void renderEntityPost(RenderLivingEvent.Post event) {
    }

}
