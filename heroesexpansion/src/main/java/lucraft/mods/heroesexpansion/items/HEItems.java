package lucraft.mods.heroesexpansion.items;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.abilities.AbilityForceField;
import lucraft.mods.heroesexpansion.abilities.AbilityGrabEntity;
import lucraft.mods.heroesexpansion.abilities.AbilityBlackHole;
import lucraft.mods.heroesexpansion.client.render.item.*;
import lucraft.mods.heroesexpansion.items.ItemHEArrow.ArrowType;
import lucraft.mods.heroesexpansion.suitsets.HESuitSet;
import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.render.ExtendedInventoryItemRendererRegistry;
import lucraft.mods.lucraftcore.infinity.items.ItemInfinityGauntlet;
import lucraft.mods.lucraftcore.infinity.items.ItemLCCast;
import lucraft.mods.lucraftcore.infinity.render.ItemRendererInfinityStone;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.events.AbilityKeyEvent;
import lucraft.mods.lucraftcore.util.helper.ItemHelper;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import lucraft.mods.lucraftcore.util.items.ItemBase;
import lucraft.mods.lucraftcore.util.items.ModelPerspective;
import lucraft.mods.lucraftcore.util.sounds.LCSoundEvents;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.Pair;

import java.awt.*;

@EventBusSubscriber(modid = HeroesExpansion.MODID)
public class HEItems {

    public static Item CAPE = new ItemCape("cape");
    public static Item SUPPORTER_CLOAK = new ItemSupporterCloak("supporter_cloak");
    public static Item MJOLNIR = new ItemThorWeapon("mjolnir", 9).setEntitySize(1F, 0.5F);
    public static Item ULTIMATE_MJOLNIR = new ItemThorWeapon("ultimate_mjolnir", 11).setEntitySize(0.3F, 0.75F);
    public static Item STORMBREAKER = new ItemThorWeapon("stormbreaker", 11).setEntitySize(0.3F, 0.75F);
    public static Item MJOLNIR_HEAD = new ItemBase("mjolnir_head").setCreativeTab(HeroesExpansion.CREATIVE_TAB).setMaxStackSize(1);
    public static Item ULTIMATE_MJOLNIR_HEAD = new ItemBase("ultimate_mjolnir_head").setCreativeTab(HeroesExpansion.CREATIVE_TAB).setMaxStackSize(1);
    public static Item STORMBREAKER_HEAD = new ItemBase("stormbreaker_head").setCreativeTab(HeroesExpansion.CREATIVE_TAB).setMaxStackSize(1);
    public static Item MJOLNIR_HEAD_CAST = new ItemLCCast("mjolnir_head_cast").setCreativeTab(HeroesExpansion.CREATIVE_TAB);
    public static Item ULTIMATE_MJOLNIR_HEAD_CAST = new ItemLCCast("ultimate_mjolnir_head_cast").setCreativeTab(HeroesExpansion.CREATIVE_TAB);
    public static Item STORMBREAKER_HEAD_CAST = new ItemLCCast("stormbreaker_head_cast").setCreativeTab(HeroesExpansion.CREATIVE_TAB);
    public static Item COMPOUND_BOW = new ItemCompoundBow("compound_bow");
    public static Item QUIVER = new ItemQuiver("quiver");
    public static Item SHARPENED_ARROW = new ItemHEArrow(ArrowType.SHARPENED);
    public static Item EXPLOSIVE_ARROW = new ItemHEArrow(ArrowType.EXPLOSIVE);
    public static Item SMOKE_ARROW = new ItemHEArrow(ArrowType.SMOKE);
    public static Item GAS_ARROW = new ItemHEArrow(ArrowType.GAS);
    public static Item GRAPPLING_HOOK_ARROW = new ItemHEArrow(ArrowType.GRAPPLING_HOOK);
    public static Item KRYPTONITE_ARROW = new ItemHEArrow(ArrowType.KRYPTONITE);
    public static Item VIBRANIUM_ARROW = new ItemHEArrow(ArrowType.VIBRANIUM);
    public static Item KRYPTONITE = new ItemKryptonite("kryptonite");
    public static Item KRYPTONIAN_FOSSIL = new ItemBase("kryptonian_fossil").setCreativeTab(HeroesExpansion.CREATIVE_TAB);
    public static Item HEART_SHAPED_HERB_POTION = new ItemHeartShapedHerbPotion("heart_shaped_herb_potion");
    public static Item VIBRANIUM_SHIELD = new ItemCaptainAmericaShield("vibranium_shield");
    public static Item CAPTAIN_AMERICA_SHIELD = new ItemCaptainAmericaShield("captain_america_shield");
    public static Item WAKANDAN_SHIELD = new ItemWakandanShield("wakandan_shield");
    public static Item CHITAURI_GUN = new ItemChitauriGun("chitauri_gun");
    public static Item CHITAURI_METAL = new ItemBase("chitauri_metal").setCreativeTab(HeroesExpansion.CREATIVE_TAB);
    public static Item CHITAURI_ENERGY_CORE = new ItemBase("chitauri_energy_core").setCreativeTab(HeroesExpansion.CREATIVE_TAB);
    public static Item KREE_BATTLE_AXE = new ItemKreeBattleAxe("kree_battle_axe");
    public static Item KREE_FLESH = new ItemKreeFlesh("kree_flesh");
    public static Item WEB_SHOOTER_1 = new ItemWebShooter("web_shooter_1", 1, 2500, 1.5F);
    public static Item WEB_SHOOTER_2 = new ItemWebShooter("web_shooter_2", 2, 5000, 3F);
    public static Item BILLY_CLUB = new ItemBillyClub("billy_club", false);
    public static Item BILLY_CLUB_SEPARATE = new ItemBillyClub("billy_club_separate", true);

    public static Item TESSERACT = new ItemTesseract("tesseract");
    public static Item SPACE_STONE = new ItemSpaceStone("space_stone");

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> e) {
        e.getRegistry().register(CAPE);
        e.getRegistry().register(SUPPORTER_CLOAK);
        e.getRegistry().register(MJOLNIR);
        e.getRegistry().register(ULTIMATE_MJOLNIR);
        e.getRegistry().register(STORMBREAKER);
        e.getRegistry().register(MJOLNIR_HEAD);
        e.getRegistry().register(ULTIMATE_MJOLNIR_HEAD);
        e.getRegistry().register(STORMBREAKER_HEAD);
        e.getRegistry().register(MJOLNIR_HEAD_CAST);
        e.getRegistry().register(ULTIMATE_MJOLNIR_HEAD_CAST);
        e.getRegistry().register(STORMBREAKER_HEAD_CAST);
        e.getRegistry().register(COMPOUND_BOW);
        e.getRegistry().register(QUIVER);
        e.getRegistry().register(SHARPENED_ARROW);
        e.getRegistry().register(EXPLOSIVE_ARROW);
        e.getRegistry().register(SMOKE_ARROW);
        e.getRegistry().register(GAS_ARROW);
        e.getRegistry().register(GRAPPLING_HOOK_ARROW);
        e.getRegistry().register(KRYPTONITE_ARROW);
        e.getRegistry().register(VIBRANIUM_ARROW);
        e.getRegistry().register(KRYPTONITE);
        e.getRegistry().register(KRYPTONIAN_FOSSIL);
        e.getRegistry().register(HEART_SHAPED_HERB_POTION);
        e.getRegistry().register(VIBRANIUM_SHIELD);
        e.getRegistry().register(CAPTAIN_AMERICA_SHIELD);
        e.getRegistry().register(WAKANDAN_SHIELD);
        e.getRegistry().register(CHITAURI_GUN);
        e.getRegistry().register(CHITAURI_METAL);
        e.getRegistry().register(CHITAURI_ENERGY_CORE);
        e.getRegistry().register(KREE_BATTLE_AXE);
        e.getRegistry().register(KREE_FLESH);
        e.getRegistry().register(WEB_SHOOTER_1);
        e.getRegistry().register(WEB_SHOOTER_2);
        e.getRegistry().register(BILLY_CLUB);
        e.getRegistry().register(BILLY_CLUB_SEPARATE);

        e.getRegistry().register(TESSERACT);
        e.getRegistry().register(SPACE_STONE);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onRegisterModels(ModelRegistryEvent e) {
        ItemRendererCaptainAmericaShield shieldRenderer = new ItemRendererCaptainAmericaShield();
        ExtendedInventoryItemRendererRegistry.registerRenderer((IItemExtendedInventory) CAPE, new ItemRendererCape());
        ExtendedInventoryItemRendererRegistry.registerRenderer((IItemExtendedInventory) SUPPORTER_CLOAK, new ItemRendererCape());
        ExtendedInventoryItemRendererRegistry.registerRenderer((IItemExtendedInventory) QUIVER, new ItemRendererQuiver());
        ExtendedInventoryItemRendererRegistry.registerRenderer((IItemExtendedInventory) VIBRANIUM_SHIELD, shieldRenderer);
        ExtendedInventoryItemRendererRegistry.registerRenderer((IItemExtendedInventory) CAPTAIN_AMERICA_SHIELD, shieldRenderer);

        MJOLNIR.setTileEntityItemStackRenderer(new ItemRendererMjolnir());
        ULTIMATE_MJOLNIR.setTileEntityItemStackRenderer(new ItemRendererUltimateMjolnir());
        STORMBREAKER.setTileEntityItemStackRenderer(new ItemRendererStormbreaker());
        COMPOUND_BOW.setTileEntityItemStackRenderer(new ItemRendererCompoundBow());
        TESSERACT.setTileEntityItemStackRenderer(new ItemRendererTesseract());
        CHITAURI_GUN.setTileEntityItemStackRenderer(new ItemRendererChitauriGun());
        KREE_BATTLE_AXE.setTileEntityItemStackRenderer(new ItemRendererKreeBattleAxe());
        SPACE_STONE.setTileEntityItemStackRenderer(new ItemRendererInfinityStone(new Color(2, 85, 255), new Color(106, 197, 255)));
        WAKANDAN_SHIELD.setTileEntityItemStackRenderer(new ItemRendererWakandanShield());

        ModelBakery.registerItemVariants(COMPOUND_BOW, new ResourceLocation(HeroesExpansion.MODID + ":" + "compound_bow"), new ResourceLocation(HeroesExpansion.MODID + ":" + "compound_bow_3d"));
        ModelBakery.registerItemVariants(MJOLNIR, new ResourceLocation(HeroesExpansion.MODID + ":" + "mjolnir"), new ResourceLocation(HeroesExpansion.MODID + ":" + "mjolnir_3d"));
        ModelBakery.registerItemVariants(ULTIMATE_MJOLNIR, new ResourceLocation(HeroesExpansion.MODID + ":" + "ultimate_mjolnir"), new ResourceLocation(HeroesExpansion.MODID + ":" + "ultimate_mjolnir_3d"));
        ModelBakery.registerItemVariants(STORMBREAKER, new ResourceLocation(HeroesExpansion.MODID + ":" + "stormbreaker"), new ResourceLocation(HeroesExpansion.MODID + ":" + "stormbreaker_3d"));
        ModelBakery.registerItemVariants(KREE_BATTLE_AXE, new ResourceLocation(HeroesExpansion.MODID + ":" + "kree_battle_axe"), new ResourceLocation(HeroesExpansion.MODID + ":" + "kree_battle_axe_3d"));

        ModelLoader.setCustomModelResourceLocation(COMPOUND_BOW, 0, new ModelResourceLocation(HeroesExpansion.MODID + ":" + "compound_bow", "inventory"));
        ModelLoader.setCustomModelResourceLocation(MJOLNIR, 0, new ModelResourceLocation(HeroesExpansion.MODID + ":" + "mjolnir", "inventory"));
        ModelLoader.setCustomModelResourceLocation(ULTIMATE_MJOLNIR, 0, new ModelResourceLocation(HeroesExpansion.MODID + ":" + "ultimate_mjolnir", "inventory"));
        ModelLoader.setCustomModelResourceLocation(STORMBREAKER, 0, new ModelResourceLocation(HeroesExpansion.MODID + ":" + "stormbreaker", "inventory"));
        ModelLoader.setCustomModelResourceLocation(KREE_BATTLE_AXE, 0, new ModelResourceLocation(HeroesExpansion.MODID + ":" + "kree_battle_axe", "inventory"));
        ModelLoader.setCustomModelResourceLocation(VIBRANIUM_SHIELD, 0, new ModelResourceLocation(HeroesExpansion.MODID + ":" + "vibranium_shield", "inventory"));
        ModelLoader.setCustomModelResourceLocation(CAPTAIN_AMERICA_SHIELD, 0, new ModelResourceLocation(HeroesExpansion.MODID + ":" + "captain_america_shield", "inventory"));

        ItemHelper.registerItemModel(CAPE, HeroesExpansion.MODID, "cape");
        ItemHelper.registerItemModel(SUPPORTER_CLOAK, HeroesExpansion.MODID, "supporter_cloak");
        ItemHelper.registerItemModel(MJOLNIR, HeroesExpansion.MODID, "mjolnir");
        ItemHelper.registerItemModel(ULTIMATE_MJOLNIR, HeroesExpansion.MODID, "ultimate_mjolnir");
        ItemHelper.registerItemModel(STORMBREAKER, HeroesExpansion.MODID, "stormbreaker");
        ItemHelper.registerItemModel(MJOLNIR_HEAD, HeroesExpansion.MODID, "mjolnir_head");
        ItemHelper.registerItemModel(ULTIMATE_MJOLNIR_HEAD, HeroesExpansion.MODID, "ultimate_mjolnir_head");
        ItemHelper.registerItemModel(STORMBREAKER_HEAD, HeroesExpansion.MODID, "stormbreaker_head");
        ItemHelper.registerItemModel(MJOLNIR_HEAD_CAST, HeroesExpansion.MODID, "mjolnir_head_cast");
        ItemHelper.registerItemModel(ULTIMATE_MJOLNIR_HEAD_CAST, HeroesExpansion.MODID, "ultimate_mjolnir_head_cast");
        ItemHelper.registerItemModel(STORMBREAKER_HEAD_CAST, HeroesExpansion.MODID, "stormbreaker_head_cast");
        ItemHelper.registerItemModel(COMPOUND_BOW, HeroesExpansion.MODID, "compound_bow");
        ItemHelper.registerItemModel(QUIVER, HeroesExpansion.MODID, "quiver");
        ItemHelper.registerItemModel(SHARPENED_ARROW, HeroesExpansion.MODID, "sharpened_arrow");
        ItemHelper.registerItemModel(EXPLOSIVE_ARROW, HeroesExpansion.MODID, "explosive_arrow");
        ItemHelper.registerItemModel(SMOKE_ARROW, HeroesExpansion.MODID, "smoke_arrow");
        ItemHelper.registerItemModel(GAS_ARROW, HeroesExpansion.MODID, "gas_arrow");
        ItemHelper.registerItemModel(GRAPPLING_HOOK_ARROW, HeroesExpansion.MODID, "grappling_hook_arrow");
        ItemHelper.registerItemModel(KRYPTONITE_ARROW, HeroesExpansion.MODID, "kryptonite_arrow");
        ItemHelper.registerItemModel(VIBRANIUM_ARROW, HeroesExpansion.MODID, "vibranium_arrow");
        ItemHelper.registerItemModel(KRYPTONITE, HeroesExpansion.MODID, "kryptonite");
        ItemHelper.registerItemModel(KRYPTONIAN_FOSSIL, HeroesExpansion.MODID, "kryptonian_fossil");
        ItemHelper.registerItemModel(HEART_SHAPED_HERB_POTION, HeroesExpansion.MODID, "heart_shaped_herb_potion");
        ItemHelper.registerItemModel(CHITAURI_GUN, HeroesExpansion.MODID, "chitauri_gun");
        ItemHelper.registerItemModel(WAKANDAN_SHIELD, HeroesExpansion.MODID, "wakandan_shield");
        ItemHelper.registerItemModel(CHITAURI_METAL, HeroesExpansion.MODID, "chitauri_metal");
        ItemHelper.registerItemModel(CHITAURI_ENERGY_CORE, HeroesExpansion.MODID, "chitauri_energy_core");
        ItemHelper.registerItemModel(KREE_BATTLE_AXE, HeroesExpansion.MODID, "kree_battle_axe");
        ItemHelper.registerItemModel(KREE_FLESH, HeroesExpansion.MODID, "kree_flesh");
        ItemHelper.registerItemModel(WEB_SHOOTER_1, HeroesExpansion.MODID, "web_shooter_1");
        ItemHelper.registerItemModel(WEB_SHOOTER_2, HeroesExpansion.MODID, "web_shooter_2");
        ItemHelper.registerItemModel(BILLY_CLUB, HeroesExpansion.MODID, "billy_club");
        ItemHelper.registerItemModel(BILLY_CLUB_SEPARATE, HeroesExpansion.MODID, "billy_club_separate");

        ItemHelper.registerItemModel(TESSERACT, HeroesExpansion.MODID, "tesseract");
        ItemHelper.registerItemModel(SPACE_STONE, HeroesExpansion.MODID, "space_stone");
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onModelBake(ModelBakeEvent e) {
        processModel(e.getModelRegistry(), new ModelResourceLocation(new ResourceLocation(HeroesExpansion.MODID, "compound_bow"), "inventory"), new ModelResourceLocation(new ResourceLocation(HeroesExpansion.MODID, "compound_bow_3d"), "inventory"));
        processModel(e.getModelRegistry(), new ModelResourceLocation(new ResourceLocation(HeroesExpansion.MODID, "mjolnir"), "inventory"), new ModelResourceLocation(new ResourceLocation(HeroesExpansion.MODID, "mjolnir_3d"), "inventory"));
        processModel(e.getModelRegistry(), new ModelResourceLocation(new ResourceLocation(HeroesExpansion.MODID, "ultimate_mjolnir"), "inventory"), new ModelResourceLocation(new ResourceLocation(HeroesExpansion.MODID, "ultimate_mjolnir_3d"), "inventory"));
        processModel(e.getModelRegistry(), new ModelResourceLocation(new ResourceLocation(HeroesExpansion.MODID, "stormbreaker"), "inventory"), new ModelResourceLocation(new ResourceLocation(HeroesExpansion.MODID, "stormbreaker_3d"), "inventory"));
        processModel(e.getModelRegistry(), new ModelResourceLocation(new ResourceLocation(HeroesExpansion.MODID, "kree_battle_axe"), "inventory"), new ModelResourceLocation(new ResourceLocation(HeroesExpansion.MODID, "kree_battle_axe_3d"), "inventory"));
    }

    @SideOnly(Side.CLIENT)
    public static void processModel(IRegistry<ModelResourceLocation, IBakedModel> registry, ModelResourceLocation normal, ModelResourceLocation model3D) {
        IBakedModel normalModel = registry.getObject(normal);
        IBakedModel model3dBaked = registry.getObject(model3D);
        registry.putObject(normal, new ModelPerspective(normalModel, Pair.of(ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND, model3dBaked), Pair.of(ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND, model3dBaked), Pair.of(ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, model3dBaked), Pair.of(ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, model3dBaked), Pair.of(ItemCameraTransforms.TransformType.GROUND, model3dBaked)));
    }

    @SubscribeEvent
    public static void onMissingRegistries(RegistryEvent.MissingMappings<Item> e) {
        for (RegistryEvent.MissingMappings.Mapping<Item> missing : e.getMappings()) {
            if (missing.key.getNamespace().equals(HeroesExpansion.MODID)) {
                if (missing.key.getPath().equals("caster_set"))
                    missing.remap(TESSERACT);
                else if (missing.key.getPath().equals("neat_scopes"))
                    missing.remap(SPACE_STONE);
                else if (missing.key.getPath().equals("black_panther_chest") || missing.key.getPath().equals("black_panther_necklace"))
                    missing.remap(HESuitSet.BLACK_PANTHER.getChestplate());
                else if (missing.key.getPath().startsWith("black_panther"))
                    missing.ignore();
            }
        }
    }

    @SubscribeEvent
    public static void onAbility(AbilityKeyEvent e) {
        ItemStack stack = e.ability.context == Ability.EnumAbilityContext.MAIN_HAND ? e.ability.getEntity().getHeldItemMainhand() : (e.ability.context == Ability.EnumAbilityContext.OFF_HAND ? e.ability.getEntity().getHeldItemOffhand() : ItemStack.EMPTY);
        if (e.pressed && stack.getItem() instanceof ItemInfinityGauntlet && (e.ability instanceof AbilityGrabEntity || e.ability instanceof AbilityForceField || e.ability instanceof AbilityBlackHole)) {
            EntityLivingBase entity = e.ability.getEntity();
            PlayerHelper.playSoundToAll(entity.world, entity.posX, entity.posY + entity.height / 2D, entity.posZ, 50, LCSoundEvents.USE_INFINITY_GAUNTLET, SoundCategory.PLAYERS);
        }
    }

}
