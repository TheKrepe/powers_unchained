package lucraft.mods.lucraftcore.materials;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.materials.blocks.MaterialsBlocks;
import lucraft.mods.lucraftcore.materials.integration.MaterialsTiConIntegration;
import lucraft.mods.lucraftcore.materials.items.MaterialsItems;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import slimeknights.tconstruct.library.client.MaterialRenderInfo;
import slimeknights.tconstruct.library.materials.*;
import slimeknights.tconstruct.library.traits.ITrait;
import slimeknights.tconstruct.tools.TinkerTraits;

import java.util.ArrayList;
import java.util.List;

public class Material {

    public static final RegistryNamespaced<String, Material> REGISTRY = new RegistryNamespaced<String, Material>();

    public static Material IRON = new Material(MaterialComponent.DUST, MaterialComponent.PLATE, MaterialComponent.FLUID).setColor(0xd8d8d8).setTemperature(769);
    public static Material GOLD = new Material(MaterialComponent.DUST, MaterialComponent.PLATE, MaterialComponent.FLUID).setColor(0xffff8b).setTemperature(532);
    public static Material COPPER = new Material(MaterialComponent.ALL).setHarvestLevel(1).setResistance(8F).setOreSettings(3, 8, 11, 0, 75).setColor(0xd0952f).setTemperature(542);
    public static Material TIN = new Material(MaterialComponent.ORE, MaterialComponent.BLOCK, MaterialComponent.INGOT, MaterialComponent.PLATE, MaterialComponent.DUST, MaterialComponent.NUGGET, MaterialComponent.FLUID).setHarvestLevel(1).setResistance(8F).setOreSettings(2, 7, 9, 20, 55).setColor(0xd8bfaa).setTemperature(350);
    public static Material NICKEL = new Material(MaterialComponent.ORE, MaterialComponent.BLOCK, MaterialComponent.INGOT, MaterialComponent.PLATE, MaterialComponent.DUST, MaterialComponent.NUGGET, MaterialComponent.FLUID).setHarvestLevel(1).setResistance(9F).setOreSettings(1, 4, 3, 5, 20).setColor(0xedede8).setTemperature(727);
    public static Material LEAD = new Material(MaterialComponent.ORE, MaterialComponent.BLOCK, MaterialComponent.INGOT, MaterialComponent.PLATE, MaterialComponent.DUST, MaterialComponent.NUGGET, MaterialComponent.FLUID).setHarvestLevel(2).setResistance(9F).setOreSettings(2, 8, 6, 10, 35).setColor(0x668595).setTemperature(400);
    public static Material SILVER = new Material(MaterialComponent.ORE, MaterialComponent.BLOCK, MaterialComponent.INGOT, MaterialComponent.PLATE, MaterialComponent.DUST, MaterialComponent.NUGGET, MaterialComponent.FLUID).setHarvestLevel(2).setResistance(10F).setOreSettings(3, 8, 6, 5, 30).setColor(0xe2e8e9).setTemperature(480);
    public static Material PALLADIUM = new Material(MaterialComponent.ORE, MaterialComponent.BLOCK, MaterialComponent.INGOT, MaterialComponent.PLATE, MaterialComponent.DUST, MaterialComponent.NUGGET, MaterialComponent.FLUID).setHarvestLevel(2).setResistance(9F).setOreSettings(2, 6, 4, 2, 18).setColor(0xe7e7e7).setTemperature(500);
    public static Material TITANIUM = new Material(MaterialComponent.ORE, MaterialComponent.BLOCK, MaterialComponent.INGOT, MaterialComponent.PLATE, MaterialComponent.DUST, MaterialComponent.NUGGET, MaterialComponent.FLUID).setHarvestLevel(2).setResistance(17F).setOreSettings(2, 5, 2, 4, 14).setColor(0x474747).setTemperature(800);
    public static Material VIBRANIUM = new Material(MaterialComponent.ORE, MaterialComponent.BLOCK, MaterialComponent.INGOT, MaterialComponent.PLATE, MaterialComponent.DUST, MaterialComponent.NUGGET, MaterialComponent.FLUID).setHarvestLevel(3).setResistance(23F).setOreSettings(1, 4, 2, 3, 12).setColor(0x91c1cc).setTemperature(950);
    public static Material OSMIUM = new Material(MaterialComponent.ORE, MaterialComponent.BLOCK, MaterialComponent.INGOT, MaterialComponent.PLATE, MaterialComponent.DUST, MaterialComponent.NUGGET, MaterialComponent.FLUID).setHarvestLevel(1).setResistance(9F).setOreSettings(1, 5, 5, 10, 32).setColor(0x8aa9a9).setTemperature(500);
    public static Material IRIDIUM = new Material(MaterialComponent.ORE, MaterialComponent.BLOCK, MaterialComponent.INGOT, MaterialComponent.PLATE, MaterialComponent.DUST, MaterialComponent.NUGGET, MaterialComponent.FLUID).setHarvestLevel(3).setResistance(13F).setOreSettings(1, 4, 2, 3, 12).setColor(0xd8dedf).setTemperature(900);
    public static Material DWARF_STAR_ALLOY = new Material(MaterialComponent.ORE, MaterialComponent.BLOCK, MaterialComponent.INGOT, MaterialComponent.PLATE, MaterialComponent.DUST, MaterialComponent.NUGGET, MaterialComponent.FLUID).setHarvestLevel(2).setResistance(15F).setOreSettings(2, 6, 4, 2, 18).setColor(0x7a7c80).setTemperature(760);
    public static Material BRONZE = new Material(MaterialComponent.INGOT, MaterialComponent.BLOCK, MaterialComponent.NUGGET, MaterialComponent.PLATE, MaterialComponent.DUST, MaterialComponent.FLUID).setHarvestLevel(1).setResistance(11F).setColor(0xb35f36).setTemperature(475);
    public static Material GOLD_TITANIUM_ALLOY = new Material(MaterialComponent.INGOT, MaterialComponent.BLOCK, MaterialComponent.NUGGET, MaterialComponent.PLATE, MaterialComponent.DUST, MaterialComponent.FLUID).setHarvestLevel(2).setResistance(20F).setColor(0x80731f).setTemperature(850);
    public static Material INTERTIUM = new Material(MaterialComponent.INGOT, MaterialComponent.BLOCK, MaterialComponent.NUGGET, MaterialComponent.PLATE, MaterialComponent.DUST, MaterialComponent.FLUID).setHarvestLevel(1).setResistance(11F).setColor(0xa15e5e).setTemperature(752);
    public static Material URANIUM = new Material(MaterialComponent.ORE, MaterialComponent.BLOCK, MaterialComponent.INGOT, MaterialComponent.PLATE, MaterialComponent.DUST, MaterialComponent.NUGGET, MaterialComponent.FLUID).setHarvestLevel(1).setResistance(9F).setOreSettings(3, 6, 4, 8, 24).setRadioactive(true).setColor(0x88d160);
    public static Material STEEL = new Material(MaterialComponent.INGOT, MaterialComponent.BLOCK, MaterialComponent.NUGGET, MaterialComponent.PLATE, MaterialComponent.DUST, MaterialComponent.FLUID).setHarvestLevel(2).setResistance(13F).setColor(0x656565).setTemperature(681);
    public static Material ADAMANTIUM = new Material(MaterialComponent.INGOT, MaterialComponent.BLOCK, MaterialComponent.NUGGET, MaterialComponent.PLATE, MaterialComponent.DUST, MaterialComponent.FLUID).setHarvestLevel(3).setResistance(30F).setColor(0xf7ffff).setTemperature(1000);
    public static Material URU = new Material(MaterialComponent.INGOT, MaterialComponent.BLOCK, MaterialComponent.ORE, MaterialComponent.NUGGET, MaterialComponent.PLATE, MaterialComponent.DUST, MaterialComponent.FLUID).setHarvestLevel(3).setResistance(60F).setColor(0xd8d8d8).setTemperature(3000);
    public static Material COAL = new Material(MaterialComponent.DUST).setColor(0x2a2a2a);
    public static Material CHARCOAL = new Material(MaterialComponent.DUST).setColor(0x2f2618);

    public static void init() {

        REGISTRY.register(0, "iron", IRON);

        REGISTRY.register(1, "gold", GOLD);

        REGISTRY.register(2, "copper", COPPER);

        REGISTRY.register(3, "tin", TIN);

        REGISTRY.register(4, "nickel", NICKEL);

        REGISTRY.register(5, "lead", LEAD);

        REGISTRY.register(6, "silver", SILVER);

        REGISTRY.register(7, "palladium", PALLADIUM);

        REGISTRY.register(8, "titanium", TITANIUM);

        REGISTRY.register(9, "vibranium", VIBRANIUM);

        REGISTRY.register(10, "osmium", OSMIUM);

        REGISTRY.register(11, "iridium", IRIDIUM);

        REGISTRY.register(12, "dwarfStarAlloy", DWARF_STAR_ALLOY);

        REGISTRY.register(13, "bronze", BRONZE);

        REGISTRY.register(14, "goldTitaniumAlloy", GOLD_TITANIUM_ALLOY);

        REGISTRY.register(15, "intertium", INTERTIUM);

        REGISTRY.register(16, "uranium", URANIUM);

        REGISTRY.register(17, "steel", STEEL);

        REGISTRY.register(18, "adamantium", ADAMANTIUM);

        REGISTRY.register(19, "coal", COAL);

        REGISTRY.register(20, "charcoal", CHARCOAL);

        REGISTRY.register(21, "uru", URU);

        for (String s : REGISTRY.getKeys()) {
            Material m = REGISTRY.getObject(s);
            int id = REGISTRY.getIDForObject(m);

            EnumHelper.addEnum(EnumMaterialWrapper.class, m.getResourceName().toUpperCase(), new Class[]{Material.class}, m);
        }

    }

    public static int getHighestMaterialId() {
        int i = 0;
        for (String s : REGISTRY.getKeys()) {
            Material m = REGISTRY.getObject(s);

            if (REGISTRY.getIDForObject(m) > i)
                i = REGISTRY.getIDForObject(m);
        }

        return i;
    }

    public static List<Material> getMaterials() {
        List<Material> list = new ArrayList<Material>();

        for (String s : REGISTRY.getKeys())
            list.add(REGISTRY.getObject(s));

        return list;
    }

    // ----------------------------------------------------------------------------------------------------------------

    private MaterialComponent[] components;
    private Fluid fluid;

    private int harvestLevel;
    private float resistance;
    private int maxVeinSize;
    private int minVeinSize;
    private int chance;
    private int minY;
    private int maxY;
    private boolean generateOre;
    private boolean radioactive;
    private int color;
    private int temperature;

    public Material(MaterialComponent... components) {
        this.components = components;
    }

    public String getIdentifier() {
        return REGISTRY.getNameForObject(this);
    }

    public String getUnlocalizedName() {
        return "lucraftcore.materials." + getIdentifier() + ".name";
    }

    public String getResourceName() {
        return StringHelper.unlocalizedToResourceName(getIdentifier());
    }

    public boolean autoGenerateComponent(MaterialComponent component) {
        for (MaterialComponent c : components) {
            if (c.equals(MaterialComponent.ALL) || c.equals(component)) {
                return true;
            }
        }
        return false;
    }

    public ItemStack getItemStack(MaterialComponent component) {
        return getItemStack(component, 1);
    }

    public ItemStack getItemStack(MaterialComponent component, int amount) {
        if (this.equals(Material.IRON)) {
            if (component == MaterialComponent.INGOT)
                return new ItemStack(Items.IRON_INGOT, amount);
            if (component == MaterialComponent.NUGGET)
                return new ItemStack(Items.IRON_NUGGET, amount);
            if (component == MaterialComponent.ORE)
                return new ItemStack(Blocks.IRON_ORE, amount);
            if (component == MaterialComponent.BLOCK)
                return new ItemStack(Blocks.IRON_BLOCK, amount);
        }

        if (this.equals(Material.GOLD)) {
            if (component == MaterialComponent.INGOT)
                return new ItemStack(Items.GOLD_INGOT, amount);
            if (component == MaterialComponent.NUGGET)
                return new ItemStack(Items.GOLD_NUGGET, amount);
            if (component == MaterialComponent.ORE)
                return new ItemStack(Blocks.GOLD_ORE, amount);
            if (component == MaterialComponent.BLOCK)
                return new ItemStack(Blocks.GOLD_BLOCK, amount);
        }

        if (!autoGenerateComponent(component))
            return ItemStack.EMPTY;

        if (component == MaterialComponent.INGOT)
            return new ItemStack(MaterialsItems.INGOTS.get(this), amount);
        if (component == MaterialComponent.DUST)
            return new ItemStack(MaterialsItems.DUSTS.get(this), amount);
        if (component == MaterialComponent.PLATE)
            return new ItemStack(MaterialsItems.PLATES.get(this), amount);
        if (component == MaterialComponent.NUGGET)
            return new ItemStack(MaterialsItems.NUGGETS.get(this), amount);
        if (component == MaterialComponent.WIRING)
            return new ItemStack(MaterialsItems.WIRINGS.get(this), amount);
        if (component == MaterialComponent.ORE)
            return new ItemStack(getBlock(component).getBlock(), amount);
        if (component == MaterialComponent.BLOCK)
            return new ItemStack(getBlock(component).getBlock(), amount);

        return ItemStack.EMPTY;
    }

    public IBlockState getBlock(MaterialComponent component) {
        if (this.equals(Material.IRON)) {
            if (component == MaterialComponent.ORE)
                return Blocks.IRON_ORE.getDefaultState();
            if (component == MaterialComponent.BLOCK)
                return Blocks.IRON_BLOCK.getDefaultState();
        }

        if (this.equals(Material.GOLD)) {
            if (component == MaterialComponent.ORE)
                return Blocks.GOLD_ORE.getDefaultState();
            if (component == MaterialComponent.BLOCK)
                return Blocks.GOLD_BLOCK.getDefaultState();
        }

        if (component == MaterialComponent.ORE) {
            return MaterialsBlocks.ORES.containsKey(this) ? MaterialsBlocks.ORES.get(this).getDefaultState() : null;
        }

        if (component == MaterialComponent.BLOCK) {
            return MaterialsBlocks.BLOCKS.containsKey(this) ? MaterialsBlocks.BLOCKS.get(this).getDefaultState() : null;
        }

        return null;
    }

    public Fluid getFluid() {
        return getFluid(false);
    }

    public Fluid getFluid(boolean ownFluid) {
        return null; // TODO TiCon
        // return ownFluid ? (LCFluids.materialFluids.containsKey(this) ?
        // LCFluids.materialFluids.get(this) : null) :
        // FluidRegistry.getFluid(this.getIdentifier().toLowerCase());
    }

    public String getRegistryName(MaterialComponent component) {
        if (component == MaterialComponent.ORE)
            return "ore_" + getResourceName();
        if (component == MaterialComponent.BLOCK)
            return "block_" + getResourceName();

        return component.getName().toLowerCase();
    }

    public String getOreDictionaryName(MaterialComponent c) {
        String name = c == MaterialComponent.WIRING ? "wire" : c.getName();
        return name + Character.toString(getIdentifier().charAt(0)).toUpperCase() + getIdentifier().substring(1);
    }

    public int getHarvestLevel(MaterialComponent component) {
        return harvestLevel;
    }

    public Material setHarvestLevel(int harvestLevel) {
        this.harvestLevel = harvestLevel;
        return this;
    }

    public float getResistance() {
        return resistance;
    }

    public Material setResistance(float resistance) {
        this.resistance = resistance;
        return this;
    }

    public float getBlockHardness(IBlockState blockState, World world, BlockPos pos) {
        return getResistance() / 2F;
    }

    public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
        return getResistance();
    }

    public int getMaxVeinSize() {
        return maxVeinSize;
    }

    public int getMinVeinSize() {
        return minVeinSize;
    }

    public int getChance() {
        return chance;
    }

    public int getMinY() {
        return minY;
    }

    public int getMaxY() {
        return maxY;
    }

    public Material setOreSettings(int minVeinSize, int maxVeinSize, int chance, int minY, int maxY) {
        this.minVeinSize = minVeinSize;
        this.maxVeinSize = maxVeinSize;
        this.chance = chance;
        this.minY = minY;
        this.maxY = maxY;
        this.generateOre = true;

        return this;
    }

    public boolean generateOre() {
        return generateOre;
    }

    public boolean isRadioactive() {
        return radioactive;
    }

    public Material setRadioactive(boolean radioactive) {
        this.radioactive = radioactive;
        return this;
    }

    @Optional.Method(modid = "tconstruct")
    public IMaterialStats[] getTiConMaterialStats() {
        int harvestLevel = getHarvestLevel(MaterialComponent.ORE) + 1;

        // HeadMaterialStats: durability, miningspeed, attack, harvestlevel
        // HandleMaterialStats: modifier, durability
        // BowMaterialStats: drawspeed, range, bonusdamage

        if (this == PALLADIUM)
            return new IMaterialStats[]{new HeadMaterialStats(250, 5.78f, 4.9f, harvestLevel), new HandleMaterialStats(1.10f, 50), new ExtraMaterialStats(120), new BowMaterialStats(0.65f, 1.55f, 6f)};

        if (this == IRIDIUM)
            return new IMaterialStats[]{new HeadMaterialStats(1910, 11.02f, 9.95f, harvestLevel), new HandleMaterialStats(0.83f, 500), new ExtraMaterialStats(200), new BowMaterialStats(0.23f, 1.44f, 5f)};

        if (this == TITANIUM)
            return new IMaterialStats[]{new HeadMaterialStats(590, 7.63f, 10.41f, harvestLevel), new HandleMaterialStats(1.52f, 180), new ExtraMaterialStats(95), new BowMaterialStats(1.0f, 3.52f, 7.6f)};

        if (this == OSMIUM)
            return new IMaterialStats[]{new HeadMaterialStats(500, 3.73f, 4.01f, harvestLevel), new HandleMaterialStats(1.19f, 98), new ExtraMaterialStats(48), new BowMaterialStats(0.98f, 1.09f, 4f)};

        if (this == DWARF_STAR_ALLOY)
            return new IMaterialStats[]{new HeadMaterialStats(782, 8.69f, 8.62f, harvestLevel), new HandleMaterialStats(1.74f, 373), new ExtraMaterialStats(150), new BowMaterialStats(1f, 4f, 8f)};

        if (this == GOLD_TITANIUM_ALLOY)
            return new IMaterialStats[]{new HeadMaterialStats(698, 8.03f, 7.92f, harvestLevel), new HandleMaterialStats(1.54f, 321), new ExtraMaterialStats(132), new BowMaterialStats(1f, 3.75f, 7.2f)};

        if (this == INTERTIUM)
            return new IMaterialStats[]{new HeadMaterialStats(559, 4.21f, 4.9f, harvestLevel), new HandleMaterialStats(1.25f, 120), new ExtraMaterialStats(60), new BowMaterialStats(1.2f, 2.4f, 6.1f)};

        if (this == VIBRANIUM)
            return new IMaterialStats[]{new HeadMaterialStats(2519, 15f, 15f, harvestLevel), new HandleMaterialStats(2f, 1000), new ExtraMaterialStats(400), new BowMaterialStats(2f, 5f, 10f)};

        if (this == ADAMANTIUM)
            return new IMaterialStats[]{new HeadMaterialStats(2519, 15f, 20f, harvestLevel), new HandleMaterialStats(4f, 2000), new ExtraMaterialStats(600), new BowMaterialStats(4f, 8f, 15f)};

        if (this == URU)
            return new IMaterialStats[]{new HeadMaterialStats(16384, 20f, 40f, harvestLevel), new HandleMaterialStats(6f, 4000), new ExtraMaterialStats(800), new BowMaterialStats(6f, 12f, 20f)};

        return new IMaterialStats[0];
    }

    @SideOnly(Side.CLIENT)
    @Optional.Method(modid = "tconstruct")
    public MaterialRenderInfo getTiConMaterialRenderInfo() {
        if (this == DWARF_STAR_ALLOY)
            return new MaterialRenderInfo.BlockTexture(new ResourceLocation(LucraftCore.MODID, "blocks/ores/dwarf_star_alloy"));
        if (this == VIBRANIUM)
            return new MaterialRenderInfo.BlockTexture(new ResourceLocation(LucraftCore.MODID, "blocks/blocks/vibranium_0"));

        return new MaterialRenderInfo.Default(getColor());
    }

    @Optional.Method(modid = "tconstruct")
    public ITrait[] getTiConTraits() {
        if (this == DWARF_STAR_ALLOY)
            return new ITrait[]{TinkerTraits.alien};

        if (this == IRIDIUM)
            return new ITrait[]{TinkerTraits.alien};

        if (this == VIBRANIUM)
            return new ITrait[]{TinkerTraits.dense, TinkerTraits.ecological, TinkerTraits.lightweight};

        if (this == NICKEL && !Loader.isModLoaded("mmdlib"))
            return new ITrait[]{TinkerTraits.magnetic};

        if (this == ADAMANTIUM)
            return new ITrait[]{TinkerTraits.sharp};

        if (this == URU)
            return new ITrait[]{TinkerTraits.alien, MaterialsTiConIntegration.TRAIT_SUPER_HEAVY};

        return new ITrait[0];
    }

    public int getColor() {
        return color;
    }

    public Material setColor(int color) {
        this.color = color;
        return this;
    }

    public int getTemperature() {
        return MathHelper.clamp(temperature, 0, 1000);
    }

    public Material setTemperature(int temperature) {
        this.temperature = temperature;
        return this;
    }

    public enum MaterialComponent {

        ALL(""), INGOT("ingot"), ORE("ore"), BLOCK("block"), NUGGET("nugget"), PLATE("plate"), DUST("dust"), WIRING("wiring"), FLUID("fluid");

        private String name;

        private MaterialComponent(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    public static enum EnumMaterialWrapper implements IStringSerializable {

        ;

        private Material material;

        EnumMaterialWrapper(Material material) {
            this.material = material;
        }

        public Material getMaterial() {
            return material;
        }

        public int getMetadataForBlock(MaterialComponent component) {
            for (int i = 0; i < 10; i++) {
                EnumMaterialWrapper[] array = getMaterialsForBlock(component, i);

                for (int j = 0; j < array.length; j++) {
                    if (array[j] == this) {
                        return j;
                    }
                }
            }

            return 0;
        }

        @Override
        public String getName() {
            return getMaterial().getIdentifier().toLowerCase();
        }

        public static EnumMaterialWrapper getWrapperFromMetadata(int i) {
            for (EnumMaterialWrapper type : EnumMaterialWrapper.values()) {
                if (Material.REGISTRY.getIDForObject(type.getMaterial()) == i) {
                    return type;
                }
            }
            return null;
        }

        public static EnumMaterialWrapper[] getMaterialsForBlock(MaterialComponent component, int index) {
            List<EnumMaterialWrapper> list = new ArrayList<EnumMaterialWrapper>();
            int ignore = index * 16;

            for (EnumMaterialWrapper emw : EnumMaterialWrapper.values()) {
                if (emw.getMaterial().autoGenerateComponent(component)) {
                    if (ignore > 0)
                        ignore--;
                    else if (list.size() < 16)
                        list.add(emw);
                }
            }

            EnumMaterialWrapper[] array = new EnumMaterialWrapper[list.size()];

            for (int i = 0; i < list.size(); i++) {
                array[i] = list.get(i);
            }

            return array;
        }

    }

}
