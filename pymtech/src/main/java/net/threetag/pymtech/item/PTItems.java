package net.threetag.pymtech.item;

import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.client.renderer.item.ShrunkenStructureItemRenderer;
import net.threetag.pymtech.fluid.PTFluids;
import net.threetag.threecore.item.ItemGroupRegistry;


public class PTItems {

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PymTech.MODID);

    public static final RegistryObject<Item> SHRINK_DISK = ITEMS.register("shrink_disk", () -> new PymParticleDiskItem(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(16), 0.1F));
    public static final RegistryObject<Item> ENLARGE_DISK = ITEMS.register("enlarge_disk", () -> new PymParticleDiskItem(new Item.Properties().group(ItemGroup.TOOLS).maxStackSize(16), 5F));
    public static final RegistryObject<Item> ANT_ANTENNA = ITEMS.register("ant_antenna", () -> new Item(new Item.Properties().group(ItemGroupRegistry.getItemGroup(ItemGroupRegistry.TECHNOLOGY))));
    public static final RegistryObject<Item> EMP_COMMUNICATION_DEVICE = ITEMS.register("emp_communication_device", () -> new Item(new Item.Properties().group(ItemGroupRegistry.getItemGroup(ItemGroupRegistry.TECHNOLOGY))));
    public static final RegistryObject<Item> AIR_FILTER = ITEMS.register("air_filter", () -> new Item(new Item.Properties().group(ItemGroupRegistry.getItemGroup(ItemGroupRegistry.TECHNOLOGY))));
    public static final RegistryObject<Item> SHRINKING_SIZE_COIL = ITEMS.register("shrinking_size_coil", () -> new Item(new Item.Properties().group(ItemGroupRegistry.getItemGroup(ItemGroupRegistry.TECHNOLOGY))));
    public static final RegistryObject<Item> ENLARGEMENT_SIZE_COIL = ITEMS.register("enlargement_size_coil", () -> new Item(new Item.Properties().group(ItemGroupRegistry.getItemGroup(ItemGroupRegistry.TECHNOLOGY))));
    public static final RegistryObject<Item> FIBER_COMPOSITE_WING = ITEMS.register("fiber_composite_wing", () -> new Item(new Item.Properties().group(ItemGroupRegistry.getItemGroup(ItemGroupRegistry.TECHNOLOGY))));
    public static final RegistryObject<Item> REGULATOR = ITEMS.register("regulator", () -> new Item(new Item.Properties().maxStackSize(1).group(ItemGroupRegistry.getItemGroup(ItemGroupRegistry.TECHNOLOGY))));
    public static final RegistryObject<Item> STRUCTURE_SHRINKER_REMOTE = ITEMS.register("structure_shrinker_remote", () -> new StructureShrinkerRemoteItem(new Item.Properties().group(ItemGroupRegistry.getItemGroup(ItemGroupRegistry.TECHNOLOGY)).maxStackSize(1)));
    public static final RegistryObject<Item> SHRINK_PYM_PARTICLES_BUCKET = ITEMS.register("shrink_pym_particles_bucket", () -> new BucketItem(() -> PTFluids.SHRINK_PYM_PARTICLES, new Item.Properties().containerItem(Items.BUCKET).maxStackSize(1).group(ItemGroup.MISC)));
    public static final RegistryObject<Item> ENLARGE_PYM_PARTICLES_BUCKET = ITEMS.register("enlarge_pym_particles_bucket", () -> new BucketItem(() -> PTFluids.ENLARGE_PYM_PARTICLES, new Item.Properties().containerItem(Items.BUCKET).maxStackSize(1).group(ItemGroup.MISC)));
    public static final RegistryObject<Item> SHRUNKEN_STRUCTURE = ITEMS.register("shrunken_structure", () -> new ShrunkenStructureItem(new Item.Properties().maxStackSize(1).setISTER(() -> ShrunkenStructureItemRenderer::new)));

    @ObjectHolder(PymTech.MODID)
    public static class Suits {

        @ObjectHolder("ant_man_helmet")
        public static final Item ANT_MAN_HELMET = null;

        @ObjectHolder("ant_man_chestplate")
        public static final Item ANT_MAN_CHESTPLATE = null;

        @ObjectHolder("ant_man_leggings")
        public static final Item ANT_MAN_LEGGINGS = null;

        @ObjectHolder("ant_man_boots")
        public static final Item ANT_MAN_BOOTS = null;

        @ObjectHolder("ant_man_tier2_helmet")
        public static final Item ANT_MAN_TIER2_HELMET = null;

        @ObjectHolder("ant_man_tier2_chestplate")
        public static final Item ANT_MAN_TIER2_CHESTPLATE = null;

        @ObjectHolder("ant_man_tier2_leggings")
        public static final Item ANT_MAN_TIER2_LEGGINGS = null;

        @ObjectHolder("ant_man_tier2_boots")
        public static final Item ANT_MAN_TIER2_BOOTS = null;

        @ObjectHolder("ant_man_tier3_helmet")
        public static final Item ANT_MAN_TIER3_HELMET = null;

        @ObjectHolder("ant_man_tier3_chestplate")
        public static final Item ANT_MAN_TIER3_CHESTPLATE = null;

        @ObjectHolder("ant_man_tier3_leggings")
        public static final Item ANT_MAN_TIER3_LEGGINGS = null;

        @ObjectHolder("ant_man_tier3_boots")
        public static final Item ANT_MAN_TIER3_BOOTS = null;

        @ObjectHolder("wasp_helmet")
        public static final Item WASP_HELMET = null;

        @ObjectHolder("wasp_chestplate")
        public static final Item WASP_CHESTPLATE = null;

        @ObjectHolder("wasp_leggings")
        public static final Item WASP_LEGGINGS = null;

        @ObjectHolder("wasp_boots")
        public static final Item WASP_BOOTS = null;

        @ObjectHolder("wasp_tier2_helmet")
        public static final Item WASP_TIER2_HELMET = null;

        @ObjectHolder("wasp_tier2_chestplate")
        public static final Item WASP_TIER2_CHESTPLATE = null;

        @ObjectHolder("wasp_tier2_leggings")
        public static final Item WASP_TIER2_LEGGINGS = null;

        @ObjectHolder("wasp_tier2_boots")
        public static final Item WASP_TIER2_BOOTS = null;

        @ObjectHolder("giant_man_helmet")
        public static final Item GIANT_MAN_HELMET = null;

        @ObjectHolder("giant_man_chestplate")
        public static final Item GIANT_MAN_CHESTPLATE = null;

        @ObjectHolder("giant_man_leggings")
        public static final Item GIANT_MAN_LEGGINGS = null;

        @ObjectHolder("giant_man_boots")
        public static final Item GIANT_MAN_BOOTS = null;

        @ObjectHolder("zombie_giant_man_helmet")
        public static final Item ZOMBIE_GIANT_MAN_HELMET = null;

        @ObjectHolder("zombie_giant_man_chestplate")
        public static final Item ZOMBIE_GIANT_MAN_CHESTPLATE = null;

        @ObjectHolder("zombie_giant_man_leggings")
        public static final Item ZOMBIE_GIANT_MAN_LEGGINGS = null;

        @ObjectHolder("zombie_giant_man_boots")
        public static final Item ZOMBIE_GIANT_MAN_BOOTS = null;
        
    }

}
