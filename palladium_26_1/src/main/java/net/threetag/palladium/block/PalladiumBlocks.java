package net.threetag.palladium.block;

import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.flag.PalladiumFeatureFlags;

import java.util.function.Supplier;

public class PalladiumBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Palladium.MOD_ID);

    // Utilities
    public static final DeferredBlock<Block> TAILORING_BENCH = BLOCKS.registerBlock("tailoring_bench", TailoringBenchBlock::new, p -> p.requiredFeatures(PalladiumFeatureFlags.MECHANIC_TAILORING));
    public static final DeferredBlock<Block> MULTIVERSAL_ITERATOR = BLOCKS.registerBlock("multiversal_iterator", MultiversalIteratorBlock::new, p -> p
            .mapColor(MapColor.COLOR_CYAN)
            .requiresCorrectToolForDrops()
            .strength(5.0F, 6.0F)
            .sound(SoundType.METAL)
            .lightLevel(_ -> 5)
            .requiredFeatures(PalladiumFeatureFlags.MECHANIC_MULTIVERSAL_VARIANTS));

    // Plants
    public static final DeferredBlock<Block> HEART_SHAPED_HERB = BLOCKS.registerBlock("heart_shaped_herb", p -> new FlowerBlock(MobEffects.RESISTANCE, 4.0F, p), p -> p
            .mapColor(MapColor.PLANT)
            .noCollision()
            .instabreak()
            .sound(SoundType.GRASS)
            .offsetType(BlockBehaviour.OffsetType.XZ)
            .pushReaction(PushReaction.DESTROY)
            .requiredFeatures(PalladiumFeatureFlags.BLOCK_HEART_SHAPED_HERB));
    public static final DeferredBlock<Block> POTTED_HEART_SHAPED_HERB = BLOCKS.registerBlock("potted_heart_shaped_herb", p -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, HEART_SHAPED_HERB, p), p -> p
            .instabreak()
            .noOcclusion()
            .pushReaction(PushReaction.DESTROY)
            .requiredFeatures(PalladiumFeatureFlags.BLOCK_HEART_SHAPED_HERB));

    // Resources
    public static final DeferredBlock<Block> METEORITE_STONE = BLOCKS.registerSimpleBlock("meteorite_stone", p -> p
            .mapColor(MapColor.COLOR_BLACK)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(4.0F, 8.0F)
            .sound(SoundType.DEEPSLATE)
            .requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredBlock<Block> METEORITE_STONE_STAIRS = registerStairs("meteorite_stone_stairs", METEORITE_STONE);
    public static final DeferredBlock<Block> METEORITE_STONE_WALL = BLOCKS.registerBlock("meteorite_stone_wall", WallBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(METEORITE_STONE.get()).forceSolidOn());
    public static final DeferredBlock<Block> METEORITE_STONE_SLAB = BLOCKS.registerBlock("meteorite_stone_slab", SlabBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(METEORITE_STONE.get()));
    public static final DeferredBlock<Block> METEORITE_BRICKS = BLOCKS.registerSimpleBlock("meteorite_bricks", p -> p
            .mapColor(MapColor.COLOR_BLACK)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(4.0F, 8.0F)
            .sound(SoundType.DEEPSLATE)
            .requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredBlock<Block> METEORITE_BRICK_STAIRS = registerStairs("meteorite_brick_stairs", METEORITE_BRICKS);
    public static final DeferredBlock<Block> METEORITE_BRICK_WALL = BLOCKS.registerBlock("meteorite_brick_wall", WallBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(METEORITE_BRICKS.get()).forceSolidOn());
    public static final DeferredBlock<Block> METEORITE_BRICK_SLAB = BLOCKS.registerBlock("meteorite_brick_slab", SlabBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(METEORITE_BRICKS.get()));

    public static final DeferredBlock<Block> LEAD_ORE = BLOCKS.registerBlock("lead_ore", p -> new DropExperienceBlock(ConstantInt.of(0), p), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE).requiredFeatures(PalladiumFeatureFlags.MATERIAL_LEAD));
    public static final DeferredBlock<Block> DEEPSLATE_LEAD_ORE = BLOCKS.registerBlock("deepslate_lead_ore", p -> new DropExperienceBlock(ConstantInt.of(0), p), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE).requiredFeatures(PalladiumFeatureFlags.MATERIAL_LEAD));
    public static final DeferredBlock<Block> METEORITE_LEAD_ORE = BLOCKS.registerBlock("meteorite_lead_ore", p -> new DropExperienceBlock(ConstantInt.of(0), p), () -> BlockBehaviour.Properties.ofFullCopy(LEAD_ORE.get())
            .mapColor(MapColor.COLOR_BLACK).strength(5.5F, 4F).sound(SoundType.DEEPSLATE).requiredFeatures(PalladiumFeatureFlags.MATERIAL_LEAD, PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredBlock<Block> LEAD_BLOCK = BLOCKS.registerSimpleBlock("lead_block", p -> p
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(5F, 6F)
            .sound(SoundType.METAL)
            .requiredFeatures(PalladiumFeatureFlags.MATERIAL_LEAD));
    public static final DeferredBlock<Block> RAW_LEAD_BLOCK = BLOCKS.registerSimpleBlock("raw_lead_block", p -> p
            .mapColor(MapColor.COLOR_BLUE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(5F, 6.0F)
            .requiredFeatures(PalladiumFeatureFlags.MATERIAL_LEAD));

    public static final DeferredBlock<Block> TITANIUM_ORE = BLOCKS.registerBlock("titanium_ore", p -> new DropExperienceBlock(ConstantInt.of(0), p), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE).requiredFeatures(PalladiumFeatureFlags.MATERIAL_TITANIUM));
    public static final DeferredBlock<Block> DEEPSLATE_TITANIUM_ORE = BLOCKS.registerBlock("deepslate_titanium_ore", p -> new DropExperienceBlock(ConstantInt.of(0), p), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE).requiredFeatures(PalladiumFeatureFlags.MATERIAL_TITANIUM));
    public static final DeferredBlock<Block> METEORITE_TITANIUM_ORE = BLOCKS.registerBlock("meteorite_titanium_ore", p -> new DropExperienceBlock(ConstantInt.of(0), p), () -> BlockBehaviour.Properties.ofFullCopy(TITANIUM_ORE.get())
            .mapColor(MapColor.COLOR_BLACK).strength(5.5F, 4F).sound(SoundType.DEEPSLATE).requiredFeatures(PalladiumFeatureFlags.MATERIAL_TITANIUM, PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredBlock<Block> TITANIUM_BLOCK = BLOCKS.registerSimpleBlock("titanium_block", p -> p
            .mapColor(MapColor.DEEPSLATE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(10F, 600F)
            .sound(SoundType.METAL)
            .requiredFeatures(PalladiumFeatureFlags.MATERIAL_TITANIUM));
    public static final DeferredBlock<Block> RAW_TITANIUM_BLOCK = BLOCKS.registerSimpleBlock("raw_titanium_block", p -> p
            .mapColor(MapColor.DEEPSLATE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(5F, 100.0F)
            .requiredFeatures(PalladiumFeatureFlags.MATERIAL_TITANIUM));

    public static final DeferredBlock<Block> VIBRANIUM_ORE = BLOCKS.registerBlock("vibranium_ore", p -> new DropExperienceBlock(ConstantInt.of(0), p), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE).strength(3, 20F).requiredFeatures(PalladiumFeatureFlags.MATERIAL_VIBRANIUM));
    public static final DeferredBlock<Block> DEEPSLATE_VIBRANIUM_ORE = BLOCKS.registerBlock("deepslate_vibranium_ore", p -> new DropExperienceBlock(ConstantInt.of(0), p), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DEEPSLATE_IRON_ORE).strength(4.5F, 20F).requiredFeatures(PalladiumFeatureFlags.MATERIAL_VIBRANIUM));
    public static final DeferredBlock<Block> METEORITE_VIBRANIUM_ORE = BLOCKS.registerBlock("meteorite_vibranium_ore", p -> new DropExperienceBlock(UniformInt.of(5, 9), p), p -> p
            .mapColor(MapColor.COLOR_BLACK)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(5.5F, 20F)
            .sound(SoundType.DEEPSLATE)
            .lightLevel(_ -> 4)
            .requiredFeatures(PalladiumFeatureFlags.MATERIAL_VIBRANIUM, PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredBlock<Block> METEORITE_VIBRANIUM_VEIN = BLOCKS.registerSimpleBlock("meteorite_vibranium_vein", (p) -> BlockBehaviour.Properties.ofFullCopy(METEORITE_STONE.get()).requiredFeatures(PalladiumFeatureFlags.MATERIAL_VIBRANIUM, PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredBlock<Block> VIBRANIUM_BLOCK = BLOCKS.registerSimpleBlock("vibranium_block", p -> p
            .mapColor(MapColor.COLOR_LIGHT_BLUE)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(50F, 1200.0F)
            .sound(SoundType.NETHERITE_BLOCK)
            .requiredFeatures(PalladiumFeatureFlags.MATERIAL_VIBRANIUM));
    public static final DeferredBlock<Block> RAW_VIBRANIUM_BLOCK = BLOCKS.registerSimpleBlock("raw_vibranium_block", p -> p
            .mapColor(MapColor.COLOR_BLACK)
            .instrument(NoteBlockInstrument.BASEDRUM)
            .requiresCorrectToolForDrops()
            .strength(30F, 1200.0F)
            .requiredFeatures(PalladiumFeatureFlags.MATERIAL_VIBRANIUM));

    public static final DeferredBlock<Block> METEORITE_COAL_ORE = BLOCKS.registerBlock("meteorite_coal_ore", p -> new DropExperienceBlock(UniformInt.of(0, 2), p), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.COAL_ORE)
            .mapColor(MapColor.COLOR_BLACK).strength(5.5F, 4F).sound(SoundType.DEEPSLATE).requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredBlock<Block> METEORITE_IRON_ORE = BLOCKS.registerBlock("meteorite_iron_ore", p -> new DropExperienceBlock(ConstantInt.of(0), p), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_ORE)
            .mapColor(MapColor.COLOR_BLACK).strength(5.5F, 4F).sound(SoundType.DEEPSLATE).requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredBlock<Block> METEORITE_COPPER_ORE = BLOCKS.registerBlock("meteorite_copper_ore", p -> new DropExperienceBlock(ConstantInt.of(0), p), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.COPPER_ORE)
            .mapColor(MapColor.COLOR_BLACK).strength(5.5F, 4F).sound(SoundType.DEEPSLATE).requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredBlock<Block> METEORITE_GOLD_ORE = BLOCKS.registerBlock("meteorite_gold_ore", p -> new DropExperienceBlock(ConstantInt.of(0), p), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.GOLD_ORE)
            .mapColor(MapColor.COLOR_BLACK).strength(5.5F, 4F).sound(SoundType.DEEPSLATE).requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredBlock<Block> METEORITE_REDSTONE_ORE = BLOCKS.registerBlock("meteorite_redstone_ore", RedStoneOreBlock::new, () -> BlockBehaviour.Properties.ofFullCopy(Blocks.REDSTONE_ORE)
            .mapColor(MapColor.COLOR_BLACK).strength(5.5F, 4F).sound(SoundType.DEEPSLATE).requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredBlock<Block> METEORITE_EMERALD_ORE = BLOCKS.registerBlock("meteorite_emerald_ore", p -> new DropExperienceBlock(UniformInt.of(3, 7), p), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.EMERALD_ORE)
            .mapColor(MapColor.COLOR_BLACK).strength(5.5F, 4F).sound(SoundType.DEEPSLATE).requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredBlock<Block> METEORITE_LAPIS_ORE = BLOCKS.registerBlock("meteorite_lapis_ore", p -> new DropExperienceBlock(UniformInt.of(2, 5), p), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.LAPIS_ORE)
            .mapColor(MapColor.COLOR_BLACK).strength(5.5F, 4F).sound(SoundType.DEEPSLATE).requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));
    public static final DeferredBlock<Block> METEORITE_DIAMOND_ORE = BLOCKS.registerBlock("meteorite_diamond_ore", p -> new DropExperienceBlock(UniformInt.of(3, 7), p), () -> BlockBehaviour.Properties.ofFullCopy(Blocks.DIAMOND_ORE)
            .mapColor(MapColor.COLOR_BLACK).strength(5.5F, 4F).sound(SoundType.DEEPSLATE).requiredFeatures(PalladiumFeatureFlags.WORLDGEN_METEORITE));

    private static DeferredBlock<Block> registerStairs(String name, Supplier<Block> baseBlock) {
        return BLOCKS.registerBlock(name, props -> new StairBlock(baseBlock.get().defaultBlockState(), props), () -> BlockBehaviour.Properties.ofFullCopy(baseBlock.get()));
    }

}
