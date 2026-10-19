package net.threetag.palladium.datagen.internal;

import net.minecraft.data.PackOutput;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.block.PalladiumBlocks;
import net.threetag.palladium.client.gui.screen.customization.CustomizationsGrid;
import net.threetag.palladium.client.gui.screen.hub.AddonHubScreen;
import net.threetag.palladium.client.gui.screen.hub.AddonInfoPanel;
import net.threetag.palladium.client.hub.AddonSort;
import net.threetag.palladium.client.gui.screen.customization.EyeSelectionScreen;
import net.threetag.palladium.client.gui.screen.customization.PlayerCustomizationScreen;
import net.threetag.palladium.client.gui.toast.CustomizationToast;
import net.threetag.palladium.client.gui.widget.PowerTreeWidget;
import net.threetag.palladium.command.*;
import net.threetag.palladium.compat.geckolib.GeckoLibCompat;
import net.threetag.palladium.config.PalladiumClientConfig;
import net.threetag.palladium.config.PalladiumServerConfig;
import net.threetag.palladium.customization.BuiltinCustomization;
import net.threetag.palladium.customization.CustomizationCategories;
import net.threetag.palladium.datagen.ExtendedLangProvider;
import net.threetag.palladium.entity.PalladiumEntityTypes;
import net.threetag.palladium.item.PalladiumCreativeTabs;
import net.threetag.palladium.item.PalladiumItems;
import net.threetag.palladium.item.multiverse.Universe;
import net.threetag.palladium.power.ability.AbilitySerializers;
import net.threetag.palladium.sound.PalladiumSoundEvents;
import net.threetag.palladium.tag.PalladiumItemTags;

public abstract class PalladiumLangProvider extends ExtendedLangProvider {

    public PalladiumLangProvider(PackOutput output, String modid, String locale) {
        super(output, modid, locale);
    }

    public static class English extends PalladiumLangProvider {

        public English(PackOutput output) {
            super(output, Palladium.MOD_ID, "en_us");
        }

        @Override
        protected void addTranslations() {
            // Blocks
            this.addBlock(PalladiumBlocks.TAILORING_BENCH, "Tailoring Bench");
            this.addBlock(PalladiumBlocks.MULTIVERSAL_ITERATOR, "Multiversal Iterator");
            this.addBlock(PalladiumBlocks.METEORITE_STONE, "Meteorite Stone");
            this.addBlock(PalladiumBlocks.METEORITE_STONE_STAIRS, "Meteorite Stone Stairs");
            this.addBlock(PalladiumBlocks.METEORITE_STONE_WALL, "Meteorite Stone Wall");
            this.addBlock(PalladiumBlocks.METEORITE_STONE_SLAB, "Meteorite Stone Slab");
            this.addBlock(PalladiumBlocks.METEORITE_BRICKS, "Meteorite Bricks");
            this.addBlock(PalladiumBlocks.METEORITE_BRICK_STAIRS, "Meteorite Brick Stairs");
            this.addBlock(PalladiumBlocks.METEORITE_BRICK_WALL, "Meteorite Brick Wall");
            this.addBlock(PalladiumBlocks.HEART_SHAPED_HERB, "Heart-Shaped Herb");
            this.addBlock(PalladiumBlocks.POTTED_HEART_SHAPED_HERB, "Potted Heart-Shaped Herb");
            this.addBlock(PalladiumBlocks.METEORITE_BRICK_SLAB, "Meteorite Brick Slab");
            this.addBlock(PalladiumBlocks.LEAD_ORE, "Lead Ore");
            this.addBlock(PalladiumBlocks.DEEPSLATE_LEAD_ORE, "Deepslate Lead Ore");
            this.addBlock(PalladiumBlocks.METEORITE_LEAD_ORE, "Meteorite Lead Ore");
            this.addBlock(PalladiumBlocks.LEAD_BLOCK, "Block of Lead");
            this.addBlock(PalladiumBlocks.RAW_LEAD_BLOCK, "Block of Raw Lead");
            this.addBlock(PalladiumBlocks.TITANIUM_ORE, "Titanium Ore");
            this.addBlock(PalladiumBlocks.DEEPSLATE_TITANIUM_ORE, "Deepslate Titanium Ore");
            this.addBlock(PalladiumBlocks.METEORITE_TITANIUM_ORE, "Meteorite Titanium Ore");
            this.addBlock(PalladiumBlocks.TITANIUM_BLOCK, "Block of Titanium");
            this.addBlock(PalladiumBlocks.RAW_TITANIUM_BLOCK, "Block of Raw Titanium");
            this.addBlock(PalladiumBlocks.VIBRANIUM_ORE, "Vibranium Ore");
            this.addBlock(PalladiumBlocks.DEEPSLATE_VIBRANIUM_ORE, "Deepslate Vibranium Ore");
            this.addBlock(PalladiumBlocks.METEORITE_VIBRANIUM_ORE, "Meteorite Vibranium Ore");
            this.addBlock(PalladiumBlocks.METEORITE_VIBRANIUM_VEIN, "Meteorite Vibranium Vein");
            this.addBlock(PalladiumBlocks.VIBRANIUM_BLOCK, "Block of Vibranium");
            this.addBlock(PalladiumBlocks.RAW_VIBRANIUM_BLOCK, "Block of Raw Vibranium");
            this.addBlock(PalladiumBlocks.METEORITE_COAL_ORE, "Meteorite Coal Ore");
            this.addBlock(PalladiumBlocks.METEORITE_IRON_ORE, "Meteorite Iron Ore");
            this.addBlock(PalladiumBlocks.METEORITE_COPPER_ORE, "Meteorite Copper Ore");
            this.addBlock(PalladiumBlocks.METEORITE_GOLD_ORE, "Meteorite Gold Ore");
            this.addBlock(PalladiumBlocks.METEORITE_REDSTONE_ORE, "Meteorite Redstone Ore");
            this.addBlock(PalladiumBlocks.METEORITE_EMERALD_ORE, "Meteorite Emerald Ore");
            this.addBlock(PalladiumBlocks.METEORITE_LAPIS_ORE, "Meteorite Lapis Lazuli Ore");
            this.addBlock(PalladiumBlocks.METEORITE_DIAMOND_ORE, "Meteorite Diamond Ore");

            // Items
            this.addCreativeModeTab(PalladiumCreativeTabs.MAIN, "Palladium");
            this.addItem(PalladiumItems.SUIT_STAND, "Suit Stand");
            this.addItem(PalladiumItems.MULTIVERSAL_EXTRAPOLATOR, "Multiversal Extrapolator");
            this.add("item.palladium.multiversal_extrapolator.desc.error", "None found");
            this.addItem(PalladiumItems.LEAD_CIRCUIT, "Lead Circuit");
            this.addItem(PalladiumItems.QUARTZ_CIRCUIT, "Quartz Circuit");
            this.addItem(PalladiumItems.VIBRANIUM_CIRCUIT, "Vibranium Circuit");
            this.addItem(PalladiumItems.WHITE_FABRIC, "White Fabric");
            this.addItem(PalladiumItems.ORANGE_FABRIC, "Orange Fabric");
            this.addItem(PalladiumItems.MAGENTA_FABRIC, "Magenta Fabric");
            this.addItem(PalladiumItems.LIGHT_BLUE_FABRIC, "Light Blue Fabric");
            this.addItem(PalladiumItems.YELLOW_FABRIC, "Yellow Fabric");
            this.addItem(PalladiumItems.LIME_FABRIC, "Lime Fabric");
            this.addItem(PalladiumItems.PINK_FABRIC, "Pink Fabric");
            this.addItem(PalladiumItems.GRAY_FABRIC, "Gray Fabric");
            this.addItem(PalladiumItems.LIGHT_GRAY_FABRIC, "Light Gray Fabric");
            this.addItem(PalladiumItems.CYAN_FABRIC, "Cyan Fabric");
            this.addItem(PalladiumItems.PURPLE_FABRIC, "Purple Fabric");
            this.addItem(PalladiumItems.BLUE_FABRIC, "Blue Fabric");
            this.addItem(PalladiumItems.BROWN_FABRIC, "Brown Fabric");
            this.addItem(PalladiumItems.GREEN_FABRIC, "Green Fabric");
            this.addItem(PalladiumItems.RED_FABRIC, "Red Fabric");
            this.addItem(PalladiumItems.BLACK_FABRIC, "Black Fabric");
            this.addItem(PalladiumItems.RAW_LEAD, "Raw Lead");
            this.addItem(PalladiumItems.VIAL, "Vial");
            this.addItem(PalladiumItems.LEAD_INGOT, "Lead Ingot");
            this.addItem(PalladiumItems.LEAD_NUGGET, "Lead Nugget");
            this.addItem(PalladiumItems.RAW_TITANIUM, "Raw Titanium");
            this.addItem(PalladiumItems.TITANIUM_INGOT, "Titanium Ingot");
            this.addItem(PalladiumItems.TITANIUM_NUGGET, "Titanium Nugget");
            this.addItem(PalladiumItems.RAW_VIBRANIUM, "Raw Vibranium");
            this.addItem(PalladiumItems.VIBRANIUM_INGOT, "Vibranium Ingot");
            this.addItem(PalladiumItems.VIBRANIUM_NUGGET, "Vibranium Nugget");

            // Item Tags
            this.add(PalladiumItemTags.LEAD_ORES, "Lead Ores");
            this.add(PalladiumItemTags.LEAD_STORAGE_BLOCKS, "Lead Storage Blocks");
            this.add(PalladiumItemTags.RAW_LEAD_STORAGE_BLOCKS, "Raw Lead Storage Blocks");
            this.add(PalladiumItemTags.RAW_LEAD, "Raw Lead");
            this.add(PalladiumItemTags.LEAD_INGOTS, "Lead Ingots");
            this.add(PalladiumItemTags.LEAD_NUGGETS, "Lead Nuggets");
            this.add(PalladiumItemTags.TITANIUM_ORES, "Titanium Ores");
            this.add(PalladiumItemTags.TITANIUM_STORAGE_BLOCKS, "Titanium Storage Blocks");
            this.add(PalladiumItemTags.RAW_TITANIUM_STORAGE_BLOCKS, "Raw Titanium Storage Blocks");
            this.add(PalladiumItemTags.RAW_TITANIUM, "Raw Titanium");
            this.add(PalladiumItemTags.TITANIUM_INGOTS, "Titanium Ingots");
            this.add(PalladiumItemTags.TITANIUM_NUGGETS, "Titanium Nuggets");
            this.add(PalladiumItemTags.VIBRANIUM_ORES, "Vibranium Ores");
            this.add(PalladiumItemTags.VIBRANIUM_STORAGE_BLOCKS, "Vibranium Storage Blocks");
            this.add(PalladiumItemTags.RAW_VIBRANIUM_STORAGE_BLOCKS, "Raw Vibranium Storage Blocks");
            this.add(PalladiumItemTags.RAW_VIBRANIUM, "Raw Vibranium");
            this.add(PalladiumItemTags.VIBRANIUM_INGOTS, "Vibranium Ingots");
            this.add(PalladiumItemTags.VIBRANIUM_NUGGETS, "Vibranium Nuggets");
            this.add(PalladiumItemTags.VIBRATION_ABSORPTION_BOOTS, "Vibration Absorption Boots");
            this.add(PalladiumItemTags.FABRICS, "Fabrics");
            this.add(PalladiumItemTags.WHITE_FABRICS, "White Fabrics");
            this.add(PalladiumItemTags.ORANGE_FABRICS, "Orange Fabrics");
            this.add(PalladiumItemTags.MAGENTA_FABRICS, "Magenta Fabrics");
            this.add(PalladiumItemTags.LIGHT_BLUE_FABRICS, "Light Blue Fabrics");
            this.add(PalladiumItemTags.YELLOW_FABRICS, "Yellow Fabrics");
            this.add(PalladiumItemTags.LIME_FABRICS, "Lime Fabrics");
            this.add(PalladiumItemTags.PINK_FABRICS, "Pink Fabrics");
            this.add(PalladiumItemTags.GRAY_FABRICS, "Gray Fabrics");
            this.add(PalladiumItemTags.LIGHT_GRAY_FABRICS, "Light Gray Fabrics");
            this.add(PalladiumItemTags.CYAN_FABRICS, "Cyan Fabrics");
            this.add(PalladiumItemTags.PURPLE_FABRICS, "Purple Fabrics");
            this.add(PalladiumItemTags.BLUE_FABRICS, "Blue Fabrics");
            this.add(PalladiumItemTags.BROWN_FABRICS, "Brown Fabrics");
            this.add(PalladiumItemTags.GREEN_FABRICS, "Green Fabrics");
            this.add(PalladiumItemTags.RED_FABRICS, "Red Fabrics");
            this.add(PalladiumItemTags.BLACK_FABRICS, "Black Fabrics");

            // Item Upgrades
            this.add("item.palladium.smithing_template.item_upgrade.applies_to", "Compatible Items");
            this.add("item.palladium.smithing_template.item_upgrade.ingredients", "/");
            this.add("item.palladium.smithing_template.item_upgrade.base_slot_description", "Add item");
            this.add("item.palladium.smithing_template.item_upgrade.incompatible_with", "Incompatible with:");
            this.add("item.palladium.upgrades", "Upgrades:");

            // Entities
            this.addEntityType(PalladiumEntityTypes.SUIT_STAND, "Suit Stand");
            this.addEntityType(PalladiumEntityTypes.EFFECT, "Effect");
            this.addEntityType(PalladiumEntityTypes.SWING_ANCHOR, "Swing Anchor");
            this.addEntityType(PalladiumEntityTypes.CUSTOM_PROJECTILE, "Projectile");
            this.addEntityType(PalladiumEntityTypes.GRAPPLE_HOOK, "Grapple Hook");

            // Abilities
            this.addAbility(AbilitySerializers.DUMMY, "Dummy");
            this.addAbility(AbilitySerializers.ACTION, "Action");
            this.addAbility(AbilitySerializers.COMMAND, "Command");
            this.addAbility(AbilitySerializers.SHRINK_PLAYER_OVERLAY, "Shrink Body Overlay");
            this.addAbility(AbilitySerializers.ATTRIBUTE_MODIFIER, "Attribute Modifier");
            this.addAbility(AbilitySerializers.HEALING, "Healing");
            this.addAbility(AbilitySerializers.SCREEN_SHAKE, "Screen Shake");
            this.addAbility(AbilitySerializers.SLOWFALL, "Slowfall");
            this.addAbility(AbilitySerializers.DAMAGE_IMMUNITY, "Damage Immunity");
            this.addAbility(AbilitySerializers.INVISIBILITY, "Invisibility");
            this.addAbility(AbilitySerializers.BEAM, "Beam");
            this.addAbility(AbilitySerializers.BLAST, "Blast");
            this.addAbility(AbilitySerializers.SIZE, "Size");
            this.addAbility(AbilitySerializers.PROJECTILE, "Projectile");
            this.addAbility(AbilitySerializers.SKIN_CHANGE, "Skin Change");
            this.addAbility(AbilitySerializers.PLAYER_SKIN_CHANGE, "Player Skin Change");
            this.addAbility(AbilitySerializers.AIM, "Aim");
            this.addAbility(AbilitySerializers.HIDE_MODEL_PART, "Hide Body Part");
            this.addAbility(AbilitySerializers.SHADER_EFFECT, "Shader Effect");
            this.addAbility(AbilitySerializers.GUI_OVERLAY, "Gui Overlay");
            this.addAbility(AbilitySerializers.SHOW_BOTH_ARMS, "Show Both Arms");
            this.addAbility(AbilitySerializers.FLUID_WALKING, "Fluid Walking");
            this.addAbility(AbilitySerializers.RESTRICT_SLOTS, "Restrict Slots");
            this.addAbility(AbilitySerializers.PLAY_SOUND, "Sound");
            this.addAbility(AbilitySerializers.INTANGIBILITY, "Intangibility");
            this.addAbility(AbilitySerializers.NAME_CHANGE, "Name Change");
            this.addAbility(AbilitySerializers.SCULK_IMMUNITY, "Sculk Immunity");
            this.addAbility(AbilitySerializers.FIRE_ASPECT, "Fire Aspect");
            this.addAbility(AbilitySerializers.PARTICLES, "Particles");
            this.addAbility(AbilitySerializers.FLIGHT, "Flight");
            this.addAbility(AbilitySerializers.GLIDING, "Gliding");
            this.addAbility(AbilitySerializers.PREVENT_GLIDING, "Prevent Gliding");
            this.addAbility(AbilitySerializers.PREVENT_SPRINTING, "Prevent Sprinting");
            this.addAbility(AbilitySerializers.PREVENT_EFFECTS, "Prevent Effects");
            this.addAbility(AbilitySerializers.PREVENT_HEALING, "Prevent Healing");
            this.addAbility(AbilitySerializers.WALL_CLIMBING, "Wall Climbing");
            this.addAbility(AbilitySerializers.GUARD, "Guard");
            this.addAbility(AbilitySerializers.MULTI_JUMP, "Multi Jump");
            this.addAbility(AbilitySerializers.SLIDE, "Slide");
            this.addAbility(AbilitySerializers.CARRY, "Carry");
            this.addAbility(AbilitySerializers.PUSH, "Push");
            this.addAbility(AbilitySerializers.GRAPPLE, "Grapple");
            this.addAbility(AbilitySerializers.MODE_SWITCH, "Mode Switch");
            this.addAbility(AbilitySerializers.COMBO_ATTACK, "Combo Attack");
            this.addAbility(AbilitySerializers.ENTITY_GLOW, "Entity Glow");
            this.addAbility(AbilitySerializers.ACTION_ON_HIT, "Action On Hit");
            this.addAbility(AbilitySerializers.AREA_ACTION, "Area Action");
            this.addAbility(AbilitySerializers.STEP_DOWN, "Step Down");
            this.addAbility(GeckoLibCompat.TRIGGER_LAYER_ANIMATION, "Geo Animation");

            // Config
            this.addConfigEntry(PalladiumClientConfig.DEV_MODE, "Addon Developer Mode");
            this.addConfigEntry(PalladiumClientConfig.ABILITY_BAR_ALIGNMENT, "Ability Bar - Alignment");
            this.addConfigEntry(PalladiumClientConfig.ABILITY_BAR_KEY_BIND_DISPLAY, "Ability Bar - Key Bind Display");
            this.addConfigEntry(PalladiumClientConfig.HIDE_EXPERIMENTAL_WARNING, "Singleplayer - Hide Experimental Settings Warning");
            this.addConfigEntry(PalladiumClientConfig.SCALE_CAMERA_FIX, "Visuals - Fix Camera Scale");
            this.addConfigEntry(PalladiumServerConfig.MAX_SUPERPOWER_SETS, "Max. amount of superpower sets");

            // Key Mappings
            this.add("key.category.palladium.powers", "Powers");
            this.addKeyMapping("open_equipment", "Open/Close Equipment");
            this.addKeyMapping("show_powers", "Powers");
            this.addKeyMapping("rotate_ability_list", "Rotate through ability bar");
            for (int i = 1; i <= 5; i++) {
                this.addKeyMapping("ability_" + i, "Ability #" + i);
            }

            // Commands
            this.add(SuperpowerCommand.ERROR_NO_LIVING_ENTITY, "%s is not a living entity");
            this.add(SuperpowerCommand.QUERY_SUCCESS, "%s has the following superpowers: %s");
            this.add(SuperpowerCommand.QUERY_NO_POWERS, "%s has no superpowers");
            this.add(SuperpowerCommand.SET_SUCCESS_SINGLE, "%s has gained the superpower %s");
            this.add(SuperpowerCommand.SET_SUCCESS_MULTIPLE, "%s entities have gained the superpower %s");
            this.add(SuperpowerCommand.ADD_ERROR_ALREADY_HAS, "%s already has that superpower");
            this.add(SuperpowerCommand.ADD_ERROR_NOT_POSSIBLE, "This superpower can not be added to %s anymore");
            this.add(SuperpowerCommand.ADD_ERROR_NONE_ADDED, "No entity was able to gain the superpower %s");
            this.add(SuperpowerCommand.ADD_SUCCESS_SINGLE, "%s has gained the superpower %s");
            this.add(SuperpowerCommand.ADD_SUCCESS_MULTIPLE, "%s entities have gained the superpower %s");
            this.add(SuperpowerCommand.REMOVE_ERROR_NO_MATCH, "No matching superpower was found");
            this.add(SuperpowerCommand.REMOVE_SUCCESS_SINGLE, "The superpower(s) of %s were removed");
            this.add(SuperpowerCommand.REMOVE_SUCCESS_MULTIPLE, "The superpower(s) of %s entities were removed");
            this.add(SuperpowerCommand.REPLACE_SUCCESS_SINGLE, "Some of %s's superpowers have been replaced with %s");
            this.add(SuperpowerCommand.REPLACE_SUCCESS_MULTIPLE, "Some of %s entities' superpowers have been replaced with %s");
            this.add(SuperpowerCommand.REPLACE_ERROR_NOT_POSSIBLE, "This superpower can not be added to %s anymore");
            this.add(SuperpowerCommand.REPLACE_ERROR_NONE_ADDED, "No entity was able to gain the superpower %s");

            this.add(DataAttachmentCommand.TRANS_UNKNOWN_TYPE, "Unknown data attachment type: %s");
            this.add(DataAttachmentCommand.TRANS_NO_DATA, "No data found for data attachment type %s");
            this.add(DataAttachmentCommand.TRANS_GET_DATA, "Data attachment value %s for %s is %s");
            this.add(DataAttachmentCommand.TRANS_PARSE_ERROR, "Error while parsing data attachment value");
            this.add(DataAttachmentCommand.TRANS_SET_DATA_SINGLE, "Data attachment value %s for %s is now set to %s");
            this.add(DataAttachmentCommand.TRANS_SET_DATA_MULTIPLE, "Data attachment value %s for %s entities is now set to %s");
            this.add(DataAttachmentCommand.TRANS_ADD_DATA_SINGLE, "Added %3$s to data attachment %1$s for %2$s");
            this.add(DataAttachmentCommand.TRANS_ADD_DATA_MULTIPLE, "Added %3$s to data attachment %1$s for %2$s entities");
            this.add(DataAttachmentCommand.TRANS_SUBTRACT_DATA_SINGLE, "Subtracted %3$s from data attachment %1$s for %2$s");
            this.add(DataAttachmentCommand.TRANS_SUBTRACT_DATA_MULTIPLE, "Subtracted %3$s from data attachment %1$s for %2$s entities");
            this.add(DataAttachmentCommand.TRANS_NOT_A_NUMBER, "Data attachment %s does not hold a numeric value");

            this.add(CustomizationCommand.UNLOCK_SUCCESS, "Successfully unlocked %s for %s");
            this.add(CustomizationCommand.LOCK_SUCCESS, "Successfully locked %s for %s");
            this.add(CustomizationCommand.ERROR_NOT_UNLOCKABLE, "%s can not be unlocked by commands");
            this.add(CustomizationCommand.ERROR_ALREADY_UNLOCKED, "%s is already unlocked");
            this.add(CustomizationCommand.ERROR_ALREADY_LOCKED, "%s is already locked");
            this.add(CustomizationCommand.ERROR_CANT_HAVE_CUSTOMIZATIONS, "The given entity can not have customizations");

            this.add(ScreenCommand.TRANS_SHOWED_SCREEN_SINGLE, "Opened screen %s for %s");
            this.add(ScreenCommand.TRANS_SHOWED_SCREEN_MULTIPLE, "Opened screen %s for %s players");

            this.add(SizeChangeCommand.ERROR_NO_LIVING_ENTITY, "%s is not a living entity");
            this.add(SizeChangeCommand.SET_SUCCESS_SINGLE, "Set %s's size to %s using %s");
            this.add(SizeChangeCommand.SET_SUCCESS_MULTIPLE, "Set the size of %s entities to %s using %s");
            this.add(SizeChangeCommand.CHANGE_SUCCESS_SINGLE, "Started changing %s's size to %s using %s");
            this.add(SizeChangeCommand.CHANGE_SUCCESS_MULTIPLE, "Started changing the size of %s entities to %s using %s");
            this.add(SizeChangeCommand.RESET_SUCCESS_SINGLE, "Reset %s's size");
            this.add(SizeChangeCommand.RESET_SUCCESS_MULTIPLE, "Reset the size of %s entities");
            this.add(SizeChangeCommand.RESET_TYPE_SUCCESS_SINGLE, "Reverted %s's size modifier for %s");
            this.add(SizeChangeCommand.RESET_TYPE_SUCCESS_MULTIPLE, "Reverted the size modifier for %s of %s entities");
            this.add(SizeChangeCommand.QUERY_SUCCESS, "%s has a current size of %s");
            this.add(SizeChangeCommand.QUERY_TYPE_SUCCESS, "%s's size modifier for %s is currently %s");

            // Container
            this.add("container.palladium.tailoring", "Tailoring");
            this.add("container.palladium.tailoring.craft", "Create");
            this.add("container.palladium.multiversal_iterator", "Multiversal Iterator");

            // GUI
            this.add(PowerTreeWidget.TRANS_TITLE, "Powers");
            this.add(PowerTreeWidget.TRANS_CUSTOMIZE, "Customize");
            this.add(PowerTreeWidget.TRANS_KEY_UNLOCK, "Unlock");
            this.add(PowerTreeWidget.TRANS_KEY_NO_ABILITIES_LABEL, "There doesn't seem to be anything here...");
            this.add(PowerTreeWidget.TRANS_KEY_VERY_SAD_LABEL, ":(");
            this.add("gui.palladium.powers.buy_ability.experience_level", "%sx experience level");
            this.add("gui.palladium.powers.buy_ability.experience_level_plural", "%sx experience levels");
            this.add("gui.palladium.powers.buy_ability.or", "or");
            this.add("gui.palladium.powers.buy_ability.multiversal_designation", "%sx Multiversal Designation (%s)");
            this.add("gui.palladium.powers.buy_ability.multiversal_designation_plural", "%sx Multiversal Designations (%s)");
            this.add(PlayerCustomizationScreen.TITLE_TRANSLATION_KEY, "Customization");
            this.add(EyeSelectionScreen.TRANS_TITLE, "Select your eyes");
            this.add(EyeSelectionScreen.TRANS_RESET, "Reset");
            this.add(EyeSelectionScreen.TRANS_SAVE, "Save");
            this.add(CustomizationsGrid.NO_CUSTOMIZATIONS_LABEL, "There doesn't seem to be anything here...");
            this.add(CustomizationsGrid.VERY_SAD_LABEL, ":(");
            this.add(CustomizationsGrid.UNLOCK, "Unlock");
            this.add(CustomizationsGrid.UNLOCK_DESC, "Do you want to unlock this customization?");
            this.add("gui.palladium.category.tailoring", "Tailoring");
            this.add("gui.palladium.category.multiversal_variants", "Multiversal Variants");

            // Addon Hub
            this.add(AddonHubScreen.TITLE_KEY, "Palladium Addons");
            this.add(AddonHubScreen.LOADING_KEY, "Loading addons...");
            this.add(AddonHubScreen.EMPTY_KEY, "No addons found.");
            this.add(AddonHubScreen.ERROR_KEY, "Failed to load addons.");
            this.add(AddonHubScreen.SEARCH_HINT_KEY, "Search addons...");
            this.add(AddonSort.DOWNLOADS.getTranslationKey(), "Popular");
            this.add(AddonSort.DOWNLOADS.getDescriptionKey(), "Sort by total downloads");
            this.add(AddonSort.LATEST.getTranslationKey(), "Newest");
            this.add(AddonSort.LATEST.getDescriptionKey(), "Sort by most recently added");
            this.add(AddonSort.NAME.getTranslationKey(), "A-Z");
            this.add(AddonSort.NAME.getDescriptionKey(), "Sort alphabetically by name");
            this.add(AddonInfoPanel.BY_KEY, "by %s");
            this.add(AddonInfoPanel.VERSIONS_KEY, "Versions: %s");
            this.add(AddonInfoPanel.DOWNLOADS_KEY, "Downloads: %s");
            this.add(AddonHubScreen.BUTTON_KEY, "Palladium Addons");
            this.add(AddonHubScreen.SUPPORT_KEY, "Support Palladium!");

            // Toasts
            this.add(CustomizationToast.TITLE_TEXT, "New Customization Unlocked!");

            // Customizations
            this.addCustomization(BuiltinCustomization.Type.LUCRAFT_ARC_REACTOR.getResourceKey(), "Lucraft Arc Reactor");
            this.addCustomizationCategory(CustomizationCategories.HAT, "Hat");
            this.addCustomizationCategory(CustomizationCategories.HEAD, "Head");
            this.addCustomizationCategory(CustomizationCategories.CHEST, "Chest");
            this.addCustomizationCategory(CustomizationCategories.BACK, "Back");
            this.addCustomizationCategory(CustomizationCategories.ARMS, "Arms");
            this.addCustomizationCategory(CustomizationCategories.LEGS, "Legs");

            // Sounds
            this.addSubtitle(PalladiumSoundEvents.PLAYER_FLIGHT_WEB_SHOOT, "Web shot");
            this.addSubtitle(PalladiumSoundEvents.PLAYER_FLIGHT_PROPELLED, "Flying");
            this.addSubtitle(PalladiumSoundEvents.PLAYER_FLIGHT_SWING, "Swinging");
            this.addSubtitle(PalladiumSoundEvents.MULTIVERSE_SEARCH, "Multiverse Search");
            this.addSubtitle(PalladiumSoundEvents.MULTIVERSAL_VARIANT_CRAFTED, "Multiversal Variant Crafted");

            // Misc
            this.add(Universe.makeTranslationKey(Palladium.id("generic")), "Earth-%s");
        }
    }

    public static class German extends PalladiumLangProvider {

        public German(PackOutput output) {
            super(output, Palladium.MOD_ID, "de_de");
        }

        @Override
        protected void addTranslations() {
            // Blocks
            this.addBlock(PalladiumBlocks.TAILORING_BENCH, "Schneidertisch");
            this.addBlock(PalladiumBlocks.MULTIVERSAL_ITERATOR, "Multiversaler Iterator");
            this.addBlock(PalladiumBlocks.METEORITE_STONE, "Meteoritengestein");
            this.addBlock(PalladiumBlocks.METEORITE_STONE_STAIRS, "Meteoritengesteintreppe");
            this.addBlock(PalladiumBlocks.METEORITE_STONE_WALL, "Meteoritengesteinmauer");
            this.addBlock(PalladiumBlocks.METEORITE_STONE_SLAB, "Meteoritengesteinstufe");
            this.addBlock(PalladiumBlocks.METEORITE_BRICKS, "Meteoritenziegel");
            this.addBlock(PalladiumBlocks.METEORITE_BRICK_STAIRS, "Meteoritenziegeltreppe");
            this.addBlock(PalladiumBlocks.METEORITE_BRICK_WALL, "Meteoritenziegelmauer");
            this.addBlock(PalladiumBlocks.HEART_SHAPED_HERB, "Herzförmiges Kraut");
            this.addBlock(PalladiumBlocks.POTTED_HEART_SHAPED_HERB, "Eingetopftes herzförmiges Kraut");
            this.addBlock(PalladiumBlocks.METEORITE_BRICK_SLAB, "Meteoritenziegelstufe");
            this.addBlock(PalladiumBlocks.LEAD_ORE, "Bleierz");
            this.addBlock(PalladiumBlocks.DEEPSLATE_LEAD_ORE, "Tiefenschiefer-Bleierz");
            this.addBlock(PalladiumBlocks.METEORITE_LEAD_ORE, "Meteoriten-Bleierz");
            this.addBlock(PalladiumBlocks.LEAD_BLOCK, "Bleiblock");
            this.addBlock(PalladiumBlocks.RAW_LEAD_BLOCK, "Rohbleiblock");
            this.addBlock(PalladiumBlocks.TITANIUM_ORE, "Titaniumerz");
            this.addBlock(PalladiumBlocks.DEEPSLATE_TITANIUM_ORE, "Tiefenschiefer-Titaniumerz");
            this.addBlock(PalladiumBlocks.METEORITE_TITANIUM_ORE, "Meteoriten-Titaniumerz");
            this.addBlock(PalladiumBlocks.TITANIUM_BLOCK, "Titaniumblock");
            this.addBlock(PalladiumBlocks.RAW_TITANIUM_BLOCK, "Rohtitaniumblock");
            this.addBlock(PalladiumBlocks.VIBRANIUM_ORE, "Vibraniumerz");
            this.addBlock(PalladiumBlocks.DEEPSLATE_VIBRANIUM_ORE, "Tiefenschiefer-Vibraniumerz");
            this.addBlock(PalladiumBlocks.METEORITE_VIBRANIUM_ORE, "Meteoriten-Vibraniumerz");
            this.addBlock(PalladiumBlocks.METEORITE_VIBRANIUM_VEIN, "Meteoriten-Vibraniumvene");
            this.addBlock(PalladiumBlocks.VIBRANIUM_BLOCK, "Vibraniumblock");
            this.addBlock(PalladiumBlocks.RAW_VIBRANIUM_BLOCK, "Rohvibraniumblock");
            this.addBlock(PalladiumBlocks.METEORITE_COAL_ORE, "Meteoriten-Steinkohle");
            this.addBlock(PalladiumBlocks.METEORITE_IRON_ORE, "Meteoriten-Eisenerz");
            this.addBlock(PalladiumBlocks.METEORITE_COPPER_ORE, "Meteoriten-Kupferz");
            this.addBlock(PalladiumBlocks.METEORITE_GOLD_ORE, "Meteoriten-Golderz");
            this.addBlock(PalladiumBlocks.METEORITE_REDSTONE_ORE, "Meteoriten-Redstone-Erz");
            this.addBlock(PalladiumBlocks.METEORITE_EMERALD_ORE, "Meteoriten-Smaragderz");
            this.addBlock(PalladiumBlocks.METEORITE_LAPIS_ORE, "Meteoriten-Lapislazulierz");
            this.addBlock(PalladiumBlocks.METEORITE_DIAMOND_ORE, "Meteoriten-Diamanterz");

            // Items
            this.addCreativeModeTab(PalladiumCreativeTabs.MAIN, "Palladium");
            this.addItem(PalladiumItems.SUIT_STAND, "Anzugsständer");
            this.addItem(PalladiumItems.MULTIVERSAL_EXTRAPOLATOR, "Multiversaler Extrapolator");
            this.add("item.palladium.multiversal_extrapolator.desc.error", "Nichts gefunden");
            this.addItem(PalladiumItems.LEAD_CIRCUIT, "Bleischaltkreis");
            this.addItem(PalladiumItems.QUARTZ_CIRCUIT, "Quarzschaltkreis");
            this.addItem(PalladiumItems.VIBRANIUM_CIRCUIT, "Vibraniumschaltkreis");
            this.addItem(PalladiumItems.WHITE_FABRIC, "Weißer Stoff");
            this.addItem(PalladiumItems.ORANGE_FABRIC, "Oranger Stoff");
            this.addItem(PalladiumItems.MAGENTA_FABRIC, "Magenta Stoff");
            this.addItem(PalladiumItems.LIGHT_BLUE_FABRIC, "Hellblauer Stoff");
            this.addItem(PalladiumItems.YELLOW_FABRIC, "Gelber Stoff");
            this.addItem(PalladiumItems.LIME_FABRIC, "Hellgrüner Stoff");
            this.addItem(PalladiumItems.PINK_FABRIC, "Rosa Stoff");
            this.addItem(PalladiumItems.GRAY_FABRIC, "Grauer Stoff");
            this.addItem(PalladiumItems.LIGHT_GRAY_FABRIC, "Hellgrauer Stoff");
            this.addItem(PalladiumItems.CYAN_FABRIC, "Türkiser Stoff");
            this.addItem(PalladiumItems.PURPLE_FABRIC, "Violetter Stoff");
            this.addItem(PalladiumItems.BLUE_FABRIC, "Blauer Stoff");
            this.addItem(PalladiumItems.BROWN_FABRIC, "Brauner Stoff");
            this.addItem(PalladiumItems.GREEN_FABRIC, "Grüner Stoff");
            this.addItem(PalladiumItems.RED_FABRIC, "Roter Stoff");
            this.addItem(PalladiumItems.BLACK_FABRIC, "Schwarzer Stoff");
            this.addItem(PalladiumItems.RAW_LEAD, "Rohblei");
            this.addItem(PalladiumItems.VIAL, "Phiole");
            this.addItem(PalladiumItems.LEAD_INGOT, "Bleibarren");
            this.addItem(PalladiumItems.LEAD_NUGGET, "Bleiklumpen");
            this.addItem(PalladiumItems.RAW_TITANIUM, "Rohtitanium");
            this.addItem(PalladiumItems.TITANIUM_INGOT, "Titaniumbarren");
            this.addItem(PalladiumItems.TITANIUM_NUGGET, "Titaniumklumpen");
            this.addItem(PalladiumItems.RAW_VIBRANIUM, "Rohvibranium");
            this.addItem(PalladiumItems.VIBRANIUM_INGOT, "Vibraniumbarren");
            this.addItem(PalladiumItems.VIBRANIUM_NUGGET, "Vibraniumklumpen");

            // Item Tags
            this.add(PalladiumItemTags.LEAD_ORES, "Bleierze");
            this.add(PalladiumItemTags.LEAD_STORAGE_BLOCKS, "Bleiblöcke");
            this.add(PalladiumItemTags.RAW_LEAD_STORAGE_BLOCKS, "Rohbleiblöcke");
            this.add(PalladiumItemTags.RAW_LEAD, "Rohblei");
            this.add(PalladiumItemTags.LEAD_INGOTS, "Bleibarren");
            this.add(PalladiumItemTags.LEAD_NUGGETS, "Bleiklumpen");
            this.add(PalladiumItemTags.TITANIUM_ORES, "Titaniumerze");
            this.add(PalladiumItemTags.TITANIUM_STORAGE_BLOCKS, "Titaniumblöcke");
            this.add(PalladiumItemTags.RAW_TITANIUM_STORAGE_BLOCKS, "Rohtitaniumblöcke");
            this.add(PalladiumItemTags.RAW_TITANIUM, "Rohtitanium");
            this.add(PalladiumItemTags.TITANIUM_INGOTS, "Titaniumbarren");
            this.add(PalladiumItemTags.TITANIUM_NUGGETS, "Titaniumklumpen");
            this.add(PalladiumItemTags.VIBRANIUM_ORES, "Vibraniumerze");
            this.add(PalladiumItemTags.VIBRANIUM_STORAGE_BLOCKS, "Vibraniumblöcke");
            this.add(PalladiumItemTags.RAW_VIBRANIUM_STORAGE_BLOCKS, "Rohvibraniumblöcke");
            this.add(PalladiumItemTags.RAW_VIBRANIUM, "Rohvibranium");
            this.add(PalladiumItemTags.VIBRANIUM_INGOTS, "Vibraniumbarren");
            this.add(PalladiumItemTags.VIBRANIUM_NUGGETS, "Vibraniumklumpen");
            this.add(PalladiumItemTags.VIBRATION_ABSORPTION_BOOTS, "Vibrationsabsorbierende Stiefel");
            this.add(PalladiumItemTags.FABRICS, "Stoffe");
            this.add(PalladiumItemTags.WHITE_FABRICS, "Weiße Stoffe");
            this.add(PalladiumItemTags.ORANGE_FABRICS, "Orange Stoffe");
            this.add(PalladiumItemTags.MAGENTA_FABRICS, "Magenta Stoffe");
            this.add(PalladiumItemTags.LIGHT_BLUE_FABRICS, "Hellblaue Stoffe");
            this.add(PalladiumItemTags.YELLOW_FABRICS, "Gelbe Stoffe");
            this.add(PalladiumItemTags.LIME_FABRICS, "Hellgrüne Stoffe");
            this.add(PalladiumItemTags.PINK_FABRICS, "Rosa Stoffe");
            this.add(PalladiumItemTags.GRAY_FABRICS, "Graue Stoffe");
            this.add(PalladiumItemTags.LIGHT_GRAY_FABRICS, "Hellgraue Stoffe");
            this.add(PalladiumItemTags.CYAN_FABRICS, "Türkise Stoffe");
            this.add(PalladiumItemTags.PURPLE_FABRICS, "Violette Stoffe");
            this.add(PalladiumItemTags.BLUE_FABRICS, "Blaue Stoffe");
            this.add(PalladiumItemTags.BROWN_FABRICS, "Braune Stoffe");
            this.add(PalladiumItemTags.GREEN_FABRICS, "Grüne Stoffe");
            this.add(PalladiumItemTags.RED_FABRICS, "Rote Stoffe");
            this.add(PalladiumItemTags.BLACK_FABRICS, "Schwarze Stoffe");

            // Item Upgrades
            this.add("item.palladium.smithing_template.item_upgrade.applies_to", "Kompatible Gegenstände");
            this.add("item.palladium.smithing_template.item_upgrade.ingredients", "/");
            this.add("item.palladium.smithing_template.item_upgrade.base_slot_description", "Gegenstand hinzufügen");
            this.add("item.palladium.smithing_template.item_upgrade.incompatible_with", "Inkompatibel mit:");
            this.add("item.palladium.upgrades", "Upgrades:");

            // Entities
            this.addEntityType(PalladiumEntityTypes.SUIT_STAND, "Anzugsständer");
            this.addEntityType(PalladiumEntityTypes.EFFECT, "Effekt");
            this.addEntityType(PalladiumEntityTypes.SWING_ANCHOR, "Schwunganker");
            this.addEntityType(PalladiumEntityTypes.CUSTOM_PROJECTILE, "Projektil");
            this.addEntityType(PalladiumEntityTypes.GRAPPLE_HOOK, "Enterhaken");

            // Abilities
            this.addAbility(AbilitySerializers.DUMMY, "Dummy");
            this.addAbility(AbilitySerializers.ACTION, "Aktion");
            this.addAbility(AbilitySerializers.COMMAND, "Befehl");
            this.addAbility(AbilitySerializers.SHRINK_PLAYER_OVERLAY, "Körperoverlay schrumpfen");
            this.addAbility(AbilitySerializers.ATTRIBUTE_MODIFIER, "Attributmodifikator");
            this.addAbility(AbilitySerializers.HEALING, "Heilung");
            this.addAbility(AbilitySerializers.SCREEN_SHAKE, "Bildschirmwackeln");
            this.addAbility(AbilitySerializers.SLOWFALL, "Langsamer Fall");
            this.addAbility(AbilitySerializers.DAMAGE_IMMUNITY, "Schadensimmunität");
            this.addAbility(AbilitySerializers.INVISIBILITY, "Unsichtbarkeit");
            this.addAbility(AbilitySerializers.BEAM, "Energiestrahl");
            this.addAbility(AbilitySerializers.BLAST, "Energieschuss");
            this.addAbility(AbilitySerializers.SIZE, "Größe");
            this.addAbility(AbilitySerializers.PROJECTILE, "Projektil");
            this.addAbility(AbilitySerializers.SKIN_CHANGE, "Skin Änderung");
            this.addAbility(AbilitySerializers.PLAYER_SKIN_CHANGE, "Spieler-Skin Änderung");
            this.addAbility(AbilitySerializers.AIM, "Zielen");
            this.addAbility(AbilitySerializers.HIDE_MODEL_PART, "Körperteile verstecken");
            this.addAbility(AbilitySerializers.SHADER_EFFECT, "Shader Effekt");
            this.addAbility(AbilitySerializers.GUI_OVERLAY, "GUI-Overlay");
            this.addAbility(AbilitySerializers.SHOW_BOTH_ARMS, "Beide Arme zeigen");
            this.addAbility(AbilitySerializers.FLUID_WALKING, "Auf Flüssigkeit Laufen");
            this.addAbility(AbilitySerializers.RESTRICT_SLOTS, "Slots beschränken");
            this.addAbility(AbilitySerializers.PLAY_SOUND, "Sound");
            this.addAbility(AbilitySerializers.INTANGIBILITY, "Ungreifbarkeit");
            this.addAbility(AbilitySerializers.NAME_CHANGE, "Namesänderung");
            this.addAbility(AbilitySerializers.SCULK_IMMUNITY, "Sculk-Immunität");
            this.addAbility(AbilitySerializers.FIRE_ASPECT, "Verbrennung");
            this.addAbility(AbilitySerializers.PARTICLES, "Partikel");
            this.addAbility(AbilitySerializers.FLIGHT, "Flug");
            this.addAbility(AbilitySerializers.GLIDING, "Gleiten");
            this.addAbility(AbilitySerializers.PREVENT_GLIDING, "Gleiten verhindern");
            this.addAbility(AbilitySerializers.PREVENT_SPRINTING, "Sprinten verhindern");
            this.addAbility(AbilitySerializers.PREVENT_EFFECTS, "Effekte verhindern");
            this.addAbility(AbilitySerializers.PREVENT_HEALING, "Heilung verhindern");
            this.addAbility(AbilitySerializers.WALL_CLIMBING, "Wandklettern");
            this.addAbility(AbilitySerializers.GUARD, "Verteidigung");
            this.addAbility(AbilitySerializers.MULTI_JUMP, "Mehrfachsprung");
            this.addAbility(AbilitySerializers.SLIDE, "Rutschen");
            this.addAbility(AbilitySerializers.CARRY, "Tragen");
            this.addAbility(AbilitySerializers.PUSH, "Stoß");
            this.addAbility(AbilitySerializers.GRAPPLE, "Enterhaken");
            this.addAbility(AbilitySerializers.MODE_SWITCH, "Moduswechsel");
            this.addAbility(AbilitySerializers.COMBO_ATTACK, "Kombo-Angriff");
            this.addAbility(AbilitySerializers.ENTITY_GLOW, "Entitäten-Leuchten");
            this.addAbility(AbilitySerializers.ACTION_ON_HIT, "Aktion bei Treffer");
            this.addAbility(AbilitySerializers.AREA_ACTION, "Flächenaktion");
            this.addAbility(AbilitySerializers.STEP_DOWN, "Herabsteigen");
            this.addAbility(GeckoLibCompat.TRIGGER_LAYER_ANIMATION, "Geo Animation");

            // Config
            this.addConfigEntry(PalladiumClientConfig.ABILITY_BAR_ALIGNMENT, "Addon Entwickler Modus");
            this.addConfigEntry(PalladiumClientConfig.ABILITY_BAR_KEY_BIND_DISPLAY, "Ability Bar - Anzeige der Tastenbelegung");
            this.addConfigEntry(PalladiumClientConfig.HIDE_EXPERIMENTAL_WARNING, "Singleplayer - Experimental Settings Warnung ausblenden");
            this.addConfigEntry(PalladiumClientConfig.SCALE_CAMERA_FIX, "Visuell - Kameraskalierung fixen");
            this.addConfigEntry(PalladiumServerConfig.MAX_SUPERPOWER_SETS, "Max. Anzahl an Superkraft-Sets");

            // Key Mappings
            this.add("key.category.palladium.powers", "Kräfte");
            this.addKeyMapping("open_equipment", "Ausrüstung öffnen/schließen");
            this.addKeyMapping("show_powers", "Kräfte");
            this.addKeyMapping("rotate_ability_list", "Durch Fähigkeitenleiste rotieren");
            for (int i = 1; i <= 5; i++) {
                this.addKeyMapping("ability_" + i, "Fähigkeit #" + i);
            }

            // Commands
            this.add(SuperpowerCommand.ERROR_NO_LIVING_ENTITY, "%s ist kein lebendes Wesen");
            this.add(SuperpowerCommand.QUERY_SUCCESS, "%s hat folgende Superkräfte: %s");
            this.add(SuperpowerCommand.QUERY_NO_POWERS, "%s hat keine Superkräfte");
            this.add(SuperpowerCommand.SET_SUCCESS_SINGLE, "%s hat die Superkraft %s erhalten");
            this.add(SuperpowerCommand.SET_SUCCESS_MULTIPLE, "%s Wesen haben die Superkraft %s erhalten");
            this.add(SuperpowerCommand.ADD_ERROR_ALREADY_HAS, "%s besitzt diese Superkraft bereits");
            this.add(SuperpowerCommand.ADD_ERROR_NOT_POSSIBLE, "Diese Superkraft kann %s nicht mehr hinzugefügt werden");
            this.add(SuperpowerCommand.ADD_ERROR_NONE_ADDED, "Keinem Wesen konnte die Superkraft %s verliehen werden");
            this.add(SuperpowerCommand.ADD_SUCCESS_SINGLE, "%s hat die Superkraft %s erhalten");
            this.add(SuperpowerCommand.ADD_SUCCESS_MULTIPLE, "%s Wesen haben die Superkraft %s erhalten");
            this.add(SuperpowerCommand.REMOVE_ERROR_NO_MATCH, "Keine passende Superkraft wurde gefunden");
            this.add(SuperpowerCommand.REMOVE_SUCCESS_SINGLE, "Die Superkraft(e) von %s wurde(n) entfernt");
            this.add(SuperpowerCommand.REMOVE_SUCCESS_MULTIPLE, "Die Superkraft(e) von %s Wesen wurden entfernt");
            this.add(SuperpowerCommand.REPLACE_SUCCESS_SINGLE, "Einige von %ss Superkräften wurden durch %s ersetzt");
            this.add(SuperpowerCommand.REPLACE_SUCCESS_MULTIPLE, "Einige der Superkräfte von %s Wesen wurden durch %s ersetzt");
            this.add(SuperpowerCommand.REPLACE_ERROR_NOT_POSSIBLE, "Diese Superkraft kann %s nicht mehr hinzugefügt werden");
            this.add(SuperpowerCommand.REPLACE_ERROR_NONE_ADDED, "Keinem Wesen konnte die Superkraft %s verliehen werden");

            this.add(DataAttachmentCommand.TRANS_UNKNOWN_TYPE, "Unbekannter Datenanhang: %s");
            this.add(DataAttachmentCommand.TRANS_NO_DATA, "Keine Daten für Datenanlagentyp %s gefunden");
            this.add(DataAttachmentCommand.TRANS_GET_DATA, "Der Wert der Datenanlage %s für %s ist %s");
            this.add(DataAttachmentCommand.TRANS_PARSE_ERROR, "Fehler beim Parsen von Datenanhangswerten");
            this.add(DataAttachmentCommand.TRANS_SET_DATA_SINGLE, "Datenanhangswert %s für %s ist jetzt auf %s gesetzt");
            this.add(DataAttachmentCommand.TRANS_SET_DATA_MULTIPLE, "Datenanhangswert %s für %s Entitäten ist jetzt auf %s gesetzt");
            this.add(DataAttachmentCommand.TRANS_ADD_DATA_SINGLE, "%3$s zu Datenanhang %1$s für %2$s hinzugefügt");
            this.add(DataAttachmentCommand.TRANS_ADD_DATA_MULTIPLE, "%3$s zu Datenanhang %1$s für %2$s Entitäten hinzugefügt");
            this.add(DataAttachmentCommand.TRANS_SUBTRACT_DATA_SINGLE, "%3$s von Datenanhang %1$s für %2$s abgezogen");
            this.add(DataAttachmentCommand.TRANS_SUBTRACT_DATA_MULTIPLE, "%3$s von Datenanhang %1$s für %2$s Entitäten abgezogen");
            this.add(DataAttachmentCommand.TRANS_NOT_A_NUMBER, "Datenanhang %s enthält keinen numerischen Wert");

            this.add(CustomizationCommand.UNLOCK_SUCCESS, "%s wurde erfolgreich für %s freigeschaltet");
            this.add(CustomizationCommand.LOCK_SUCCESS, "%s wurde erfolgreich für %s gesperrt");
            this.add(CustomizationCommand.ERROR_NOT_UNLOCKABLE, "%s kann nicht per Befehl freigeschaltet werden");
            this.add(CustomizationCommand.ERROR_ALREADY_UNLOCKED, "%s ist bereits freigeschaltet");
            this.add(CustomizationCommand.ERROR_ALREADY_LOCKED, "%s ist bereits gesperrt");
            this.add(CustomizationCommand.ERROR_CANT_HAVE_CUSTOMIZATIONS, "Die angegebene Entität kann keine Anpassungen besitzen");

            this.add(ScreenCommand.TRANS_SHOWED_SCREEN_SINGLE, "Fenster %s für %s geöffnet");
            this.add(ScreenCommand.TRANS_SHOWED_SCREEN_MULTIPLE, "Fenster %s für %s Spieler geöffnet");

            this.add(SizeChangeCommand.ERROR_NO_LIVING_ENTITY, "%s ist kein lebendes Wesen");
            this.add(SizeChangeCommand.SET_SUCCESS_SINGLE, "Größe von %s auf %s mit %s gesetzt");
            this.add(SizeChangeCommand.SET_SUCCESS_MULTIPLE, "Größe von %s Wesen auf %s mit %s gesetzt");
            this.add(SizeChangeCommand.CHANGE_SUCCESS_SINGLE, "Größenänderung von %s auf %s mit %s gestartet");
            this.add(SizeChangeCommand.CHANGE_SUCCESS_MULTIPLE, "Größenänderung von %s Wesen auf %s mit %s gestartet");
            this.add(SizeChangeCommand.RESET_SUCCESS_SINGLE, "Größe von %s zurückgesetzt");
            this.add(SizeChangeCommand.RESET_SUCCESS_MULTIPLE, "Größe von %s Wesen zurückgesetzt");
            this.add(SizeChangeCommand.RESET_TYPE_SUCCESS_SINGLE, "Größenmodifikator von %s für %s zurückgesetzt");
            this.add(SizeChangeCommand.RESET_TYPE_SUCCESS_MULTIPLE, "Größenmodifikator für %s von %s Wesen zurückgesetzt");
            this.add(SizeChangeCommand.QUERY_SUCCESS, "%s hat eine aktuelle Größe von %s");
            this.add(SizeChangeCommand.QUERY_TYPE_SUCCESS, "Der Größenmodifikator von %s für %s beträgt aktuell %s");

            // Container
            this.add("container.palladium.tailoring", "Zuschneidern");
            this.add("container.palladium.tailoring.craft", "Herstellen");
            this.add("container.palladium.multiversal_iterator", "Multiversaler Iterator");

            // GUI
            this.add(PowerTreeWidget.TRANS_TITLE, "Kräfte");
            this.add(PowerTreeWidget.TRANS_CUSTOMIZE, "Anpassen");
            this.add(PowerTreeWidget.TRANS_KEY_UNLOCK, "Freischalten");
            this.add(PowerTreeWidget.TRANS_KEY_NO_ABILITIES_LABEL, "Hier scheint es nichts zu geben...");
            this.add(PowerTreeWidget.TRANS_KEY_VERY_SAD_LABEL, ":(");
            this.add("gui.palladium.powers.buy_ability.experience_level", "%sx Erfahrungsstufe");
            this.add("gui.palladium.powers.buy_ability.experience_level_plural", "%sx Erfahrungsstufen");
            this.add("gui.palladium.powers.buy_ability.or", "oder");
            this.add("gui.palladium.powers.buy_ability.multiversal_designation", "%sx Multiversale Designation (%s)");
            this.add("gui.palladium.powers.buy_ability.multiversal_designation_plural", "%sx Multiversale Designationen (%s)");
            this.add(PlayerCustomizationScreen.TITLE_TRANSLATION_KEY, "Anpassungen");
            this.add(EyeSelectionScreen.TRANS_TITLE, "Wähle deine Augen aus");
            this.add(EyeSelectionScreen.TRANS_RESET, "Zurücksetzen");
            this.add(EyeSelectionScreen.TRANS_SAVE, "Speichern");
            this.add(CustomizationsGrid.NO_CUSTOMIZATIONS_LABEL, "Hier scheint es nichts zu geben...");
            this.add(CustomizationsGrid.VERY_SAD_LABEL, ":(");
            this.add(CustomizationsGrid.UNLOCK, "Freischalten");
            this.add(CustomizationsGrid.UNLOCK_DESC, "Willst du diese Anpassung freischalten?");
            this.add("gui.palladium.category.tailoring", "Zuschneidern");
            this.add("gui.palladium.category.multiversal_variants", "Multiversale Varianten");

            // Addon Hub
            this.add(AddonHubScreen.TITLE_KEY, "Palladium Addons");
            this.add(AddonHubScreen.LOADING_KEY, "Lade Addons...");
            this.add(AddonHubScreen.EMPTY_KEY, "Keine Addons gefunden.");
            this.add(AddonHubScreen.ERROR_KEY, "Addons konnten nicht geladen werden.");
            this.add(AddonHubScreen.SEARCH_HINT_KEY, "Addons suchen...");
            this.add(AddonSort.DOWNLOADS.getTranslationKey(), "Beliebt");
            this.add(AddonSort.DOWNLOADS.getDescriptionKey(), "Nach Downloads sortieren");
            this.add(AddonSort.LATEST.getTranslationKey(), "Neueste");
            this.add(AddonSort.LATEST.getDescriptionKey(), "Nach zuletzt hinzugefügt sortieren");
            this.add(AddonSort.NAME.getTranslationKey(), "A-Z");
            this.add(AddonSort.NAME.getDescriptionKey(), "Alphabetisch nach Name sortieren");
            this.add(AddonInfoPanel.BY_KEY, "von %s");
            this.add(AddonInfoPanel.VERSIONS_KEY, "Versionen: %s");
            this.add(AddonInfoPanel.DOWNLOADS_KEY, "Downloads: %s");
            this.add(AddonHubScreen.BUTTON_KEY, "Palladium Addons");
            this.add(AddonHubScreen.SUPPORT_KEY, "Unterstütze Palladium!");

            // Toasts
            this.add(CustomizationToast.TITLE_TEXT, "Neue Anpassung freigeschaltet!");

            // Customizations
            this.addCustomization(BuiltinCustomization.Type.LUCRAFT_ARC_REACTOR.getResourceKey(), "Lucraft Arc Reaktor");
            this.addCustomizationCategory(CustomizationCategories.HAT, "Hut");
            this.addCustomizationCategory(CustomizationCategories.HEAD, "Kopf");
            this.addCustomizationCategory(CustomizationCategories.CHEST, "Brust");
            this.addCustomizationCategory(CustomizationCategories.BACK, "Rücken");
            this.addCustomizationCategory(CustomizationCategories.ARMS, "Arme");
            this.addCustomizationCategory(CustomizationCategories.LEGS, "Beine");

            // Sounds
            this.addSubtitle(PalladiumSoundEvents.PLAYER_FLIGHT_WEB_SHOOT, "Netz geschossen");
            this.addSubtitle(PalladiumSoundEvents.PLAYER_FLIGHT_PROPELLED, "Fliegen");
            this.addSubtitle(PalladiumSoundEvents.PLAYER_FLIGHT_SWING, "Schwingen");
            this.addSubtitle(PalladiumSoundEvents.MULTIVERSE_SEARCH, "Multiversumssuche");
            this.addSubtitle(PalladiumSoundEvents.MULTIVERSAL_VARIANT_CRAFTED, "Multiversale Variante hergestellt");

            // Misc
            this.add(Universe.makeTranslationKey(Palladium.id("generic")), "Erde-%s");
        }
    }

    public static class Saxon extends PalladiumLangProvider {

        public Saxon(PackOutput output) {
            super(output, Palladium.MOD_ID, "sxu");
        }

        @Override
        protected void addTranslations() {
            // Blocks
            this.addBlock(PalladiumBlocks.TAILORING_BENCH, "Schneidertisch’n");
            this.addBlock(PalladiumBlocks.MULTIVERSAL_ITERATOR, "Multiversaler Itterador");
            this.addBlock(PalladiumBlocks.METEORITE_STONE, "Meteoriitngestehn");
            this.addBlock(PalladiumBlocks.METEORITE_STONE_STAIRS, "Meteoriitngestehndrebbe");
            this.addBlock(PalladiumBlocks.METEORITE_STONE_WALL, "Meteoriitngestehnmauor");
            this.addBlock(PalladiumBlocks.METEORITE_STONE_SLAB, "Meteoriitngestehnschdufe");
            this.addBlock(PalladiumBlocks.METEORITE_BRICKS, "Meteoriitnzieschl");
            this.addBlock(PalladiumBlocks.METEORITE_BRICK_STAIRS, "Meteoriitnzieschldrebbe");
            this.addBlock(PalladiumBlocks.METEORITE_BRICK_WALL, "Meteoriitnzieschlmauor");
            this.addBlock(PalladiumBlocks.HEART_SHAPED_HERB, "Herzförmijes Graud");
            this.addBlock(PalladiumBlocks.POTTED_HEART_SHAPED_HERB, "Eingedobbdes herzförmijes Graud");
            this.addBlock(PalladiumBlocks.METEORITE_BRICK_SLAB, "Meteoriitnzieschlschdufe");
            this.addBlock(PalladiumBlocks.LEAD_ORE, "Bleeärds");
            this.addBlock(PalladiumBlocks.DEEPSLATE_LEAD_ORE, "Diefnschieforr-Bleeärds");
            this.addBlock(PalladiumBlocks.METEORITE_LEAD_ORE, "Meteoriitn-Bleeärds");
            this.addBlock(PalladiumBlocks.LEAD_BLOCK, "Bleeblogg");
            this.addBlock(PalladiumBlocks.RAW_LEAD_BLOCK, "Rohbleeblogg");
            this.addBlock(PalladiumBlocks.TITANIUM_ORE, "Titaniumärds");
            this.addBlock(PalladiumBlocks.DEEPSLATE_TITANIUM_ORE, "Diefnschieforr-Titaniumärds");
            this.addBlock(PalladiumBlocks.METEORITE_TITANIUM_ORE, "Meteoriitn-Titaniumärds");
            this.addBlock(PalladiumBlocks.TITANIUM_BLOCK, "Titaniumblogg");
            this.addBlock(PalladiumBlocks.RAW_TITANIUM_BLOCK, "Rohtitaniumblogg");
            this.addBlock(PalladiumBlocks.VIBRANIUM_ORE, "Vibraniumärds");
            this.addBlock(PalladiumBlocks.DEEPSLATE_VIBRANIUM_ORE, "Diefnschieforr-Vibraniumärds");
            this.addBlock(PalladiumBlocks.METEORITE_VIBRANIUM_ORE, "Meteoriitn-Vibraniumärds");
            this.addBlock(PalladiumBlocks.METEORITE_VIBRANIUM_VEIN, "Meteoriitn-Vibraniumvene");
            this.addBlock(PalladiumBlocks.VIBRANIUM_BLOCK, "Vibraniumblogg");
            this.addBlock(PalladiumBlocks.RAW_VIBRANIUM_BLOCK, "Rohvibraniumblogg");
            this.addBlock(PalladiumBlocks.METEORITE_COAL_ORE, "Meteoriitn-Schdeengohle");
            this.addBlock(PalladiumBlocks.METEORITE_IRON_ORE, "Meteoriitn-Eisnärds");
            this.addBlock(PalladiumBlocks.METEORITE_COPPER_ORE, "Meteoriitn-Gupforrärds");
            this.addBlock(PalladiumBlocks.METEORITE_GOLD_ORE, "Meteoriitn-Goldärz");
            this.addBlock(PalladiumBlocks.METEORITE_REDSTONE_ORE, "Meteoriitn-Redstone-Ärds");
            this.addBlock(PalladiumBlocks.METEORITE_EMERALD_ORE, "Meteoriitn-Smaragdärz");
            this.addBlock(PalladiumBlocks.METEORITE_LAPIS_ORE, "Meteoriitn-Labisladsuliärds");
            this.addBlock(PalladiumBlocks.METEORITE_DIAMOND_ORE, "Meteoriitn-Diamantärds");

            // Items
            this.addCreativeModeTab(PalladiumCreativeTabs.MAIN, "Palladium");
            this.addItem(PalladiumItems.SUIT_STAND, "Anzuchsschdändorr");
            this.addItem(PalladiumItems.MULTIVERSAL_EXTRAPOLATOR, "Multiversaler Extrapolator");
            this.add("item.palladium.multiversal_extrapolator.desc.error", "Nichts jefunden");
            this.addItem(PalladiumItems.LEAD_CIRCUIT, "Bleeschaldkres");
            this.addItem(PalladiumItems.QUARTZ_CIRCUIT, "Quarzschaldkres");
            this.addItem(PalladiumItems.VIBRANIUM_CIRCUIT, "Vibraniumschaldkres");
            this.addItem(PalladiumItems.WHITE_FABRIC, "Weeßer Schdoff");
            this.addItem(PalladiumItems.ORANGE_FABRIC, "Orangscher Schdoff");
            this.addItem(PalladiumItems.MAGENTA_FABRIC, "Magenda Schdoff");
            this.addItem(PalladiumItems.LIGHT_BLUE_FABRIC, "Hellblaaer Schdoff");
            this.addItem(PalladiumItems.YELLOW_FABRIC, "Gälber Schdoff");
            this.addItem(PalladiumItems.LIME_FABRIC, "Hellgriener Schdoff");
            this.addItem(PalladiumItems.PINK_FABRIC, "Roser Schdoff");
            this.addItem(PalladiumItems.GRAY_FABRIC, "Graer Schdoff");
            this.addItem(PalladiumItems.LIGHT_GRAY_FABRIC, "Hellgraer Schdoff");
            this.addItem(PalladiumItems.CYAN_FABRIC, "Türkiser Schdoff");
            this.addItem(PalladiumItems.PURPLE_FABRIC, "Violedder Schdoff");
            this.addItem(PalladiumItems.BLUE_FABRIC, "Blaer Schdoff");
            this.addItem(PalladiumItems.BROWN_FABRIC, "Braer Schdoff");
            this.addItem(PalladiumItems.GREEN_FABRIC, "Griener Schdoff");
            this.addItem(PalladiumItems.RED_FABRIC, "Roder Schdoff");
            this.addItem(PalladiumItems.BLACK_FABRIC, "Schwarzer Schdoff");
            this.addItem(PalladiumItems.RAW_LEAD, "Rohblee");
            this.addItem(PalladiumItems.VIAL, "Fläschn");
            this.addItem(PalladiumItems.LEAD_INGOT, "Bleebarrn");
            this.addItem(PalladiumItems.LEAD_NUGGET, "Bleeglumbn");
            this.addItem(PalladiumItems.RAW_TITANIUM, "Rohtitanium");
            this.addItem(PalladiumItems.TITANIUM_INGOT, "Titaniumbarrn");
            this.addItem(PalladiumItems.TITANIUM_NUGGET, "Titaniumglumbn");
            this.addItem(PalladiumItems.RAW_VIBRANIUM, "Rohvibranium");
            this.addItem(PalladiumItems.VIBRANIUM_INGOT, "Vibraniumbarrn");
            this.addItem(PalladiumItems.VIBRANIUM_NUGGET, "Vibraniumglumbn");

            // Item Tags
            this.add(PalladiumItemTags.LEAD_ORES, "Bleiärds");
            this.add(PalladiumItemTags.LEAD_STORAGE_BLOCKS, "Bleiblöcke");
            this.add(PalladiumItemTags.RAW_LEAD_STORAGE_BLOCKS, "Rohbleiblöcke");
            this.add(PalladiumItemTags.RAW_LEAD, "Rohblei");
            this.add(PalladiumItemTags.LEAD_INGOTS, "Bleibarrn");
            this.add(PalladiumItemTags.LEAD_NUGGETS, "Bleiglumbn");
            this.add(PalladiumItemTags.TITANIUM_ORES, "Titaniumärds");
            this.add(PalladiumItemTags.TITANIUM_STORAGE_BLOCKS, "Titaniumblöcke");
            this.add(PalladiumItemTags.RAW_TITANIUM_STORAGE_BLOCKS, "Rohtitaniumblöcke");
            this.add(PalladiumItemTags.RAW_TITANIUM, "Rohtitanium");
            this.add(PalladiumItemTags.TITANIUM_INGOTS, "Titaniumbarrn");
            this.add(PalladiumItemTags.TITANIUM_NUGGETS, "Titaniumglumbn");
            this.add(PalladiumItemTags.VIBRANIUM_ORES, "Vibraniumärds");
            this.add(PalladiumItemTags.VIBRANIUM_STORAGE_BLOCKS, "Vibraniumblöcke");
            this.add(PalladiumItemTags.RAW_VIBRANIUM_STORAGE_BLOCKS, "Rohvibraniumblöcke");
            this.add(PalladiumItemTags.RAW_VIBRANIUM, "Rohvibranium");
            this.add(PalladiumItemTags.VIBRANIUM_INGOTS, "Vibraniumbarrn");
            this.add(PalladiumItemTags.VIBRANIUM_NUGGETS, "Vibraniumglumbn");
            this.add(PalladiumItemTags.VIBRATION_ABSORPTION_BOOTS, "Vibradsjonsabsorbierende Schdiefel");
            this.add(PalladiumItemTags.FABRICS, "Schdoffe");
            this.add(PalladiumItemTags.WHITE_FABRICS, "Weeße Schdoffe");
            this.add(PalladiumItemTags.ORANGE_FABRICS, "Orangschorr Schdoffe");
            this.add(PalladiumItemTags.MAGENTA_FABRICS, "Magenda Schdoffe");
            this.add(PalladiumItemTags.LIGHT_BLUE_FABRICS, "Hellblauorr Schdoffe");
            this.add(PalladiumItemTags.YELLOW_FABRICS, "Gelborr Schdoffe");
            this.add(PalladiumItemTags.LIME_FABRICS, "Hellgrienorr Schdoffe");
            this.add(PalladiumItemTags.PINK_FABRICS, "Pingorr Schdoffe");
            this.add(PalladiumItemTags.GRAY_FABRICS, "Grauorr Schdoffe");
            this.add(PalladiumItemTags.LIGHT_GRAY_FABRICS, "Hellgrauorr Schdoffe");
            this.add(PalladiumItemTags.CYAN_FABRICS, "Dürgisorr Schdoffe");
            this.add(PalladiumItemTags.PURPLE_FABRICS, "Lila Schdoffe");
            this.add(PalladiumItemTags.BLUE_FABRICS, "Blauorr Schdoffe");
            this.add(PalladiumItemTags.BROWN_FABRICS, "Braunorr Schdoffe");
            this.add(PalladiumItemTags.GREEN_FABRICS, "Grienorr Schdoffe");
            this.add(PalladiumItemTags.RED_FABRICS, "Rodorr Schdoffe");
            this.add(PalladiumItemTags.BLACK_FABRICS, "Schworzorr Schdoffe");

            // Item Upgrades
            this.add("item.palladium.smithing_template.item_upgrade.applies_to", "Gombadible Sachn");
            this.add("item.palladium.smithing_template.item_upgrade.ingredients", "/");
            this.add("item.palladium.smithing_template.item_upgrade.base_slot_description", "Sache dazu");
            this.add("item.palladium.smithing_template.item_upgrade.incompatible_with", "Bassd nedd mim:");
            this.add("item.palladium.upgrades", "Upgrades:");

            // Entities
            this.addEntityType(PalladiumEntityTypes.SUIT_STAND, "Anzuchsschdändorr");
            this.addEntityType(PalladiumEntityTypes.EFFECT, "Effägd");
            this.addEntityType(PalladiumEntityTypes.SWING_ANCHOR, "Schwungangorr");
            this.addEntityType(PalladiumEntityTypes.CUSTOM_PROJECTILE, "Brojegdil");
            this.addEntityType(PalladiumEntityTypes.GRAPPLE_HOOK, "Endorrhaggn");

            // Abilities
            this.addAbility(AbilitySerializers.DUMMY, "Dummy");
            this.addAbility(AbilitySerializers.ACTION, "Aktion");
            this.addAbility(AbilitySerializers.COMMAND, "Befehl");
            this.addAbility(AbilitySerializers.SHRINK_PLAYER_OVERLAY, "Körperoverlay schrumpfen");
            this.addAbility(AbilitySerializers.ATTRIBUTE_MODIFIER, "Ättribütmodifikator");
            this.addAbility(AbilitySerializers.HEALING, "Helung");
            this.addAbility(AbilitySerializers.SCREEN_SHAKE, "Bildschirmgewaggl");
            this.addAbility(AbilitySerializers.SLOWFALL, "Langsamer Fall");
            this.addAbility(AbilitySerializers.DAMAGE_IMMUNITY, "Schadensimmunität");
            this.addAbility(AbilitySerializers.INVISIBILITY, "Unsischtbarkeet");
            this.addAbility(AbilitySerializers.BEAM, "Energiestrahl");
            this.addAbility(AbilitySerializers.BLAST, "Energieschuss");
            this.addAbility(AbilitySerializers.SIZE, "Größe");
            this.addAbility(AbilitySerializers.PROJECTILE, "Projektil");
            this.addAbility(AbilitySerializers.SKIN_CHANGE, "Skin Änderung");
            this.addAbility(AbilitySerializers.PLAYER_SKIN_CHANGE, "Spieler-Skin Änderung");
            this.addAbility(AbilitySerializers.AIM, "Zielen");
            this.addAbility(AbilitySerializers.HIDE_MODEL_PART, "Görperdeile versteggen");
            this.addAbility(AbilitySerializers.SHADER_EFFECT, "Shader Effekt");
            this.addAbility(AbilitySerializers.GUI_OVERLAY, "GUI-Overlay");
            this.addAbility(AbilitySerializers.SHOW_BOTH_ARMS, "Beide Arme zeijen");
            this.addAbility(AbilitySerializers.FLUID_WALKING, "Uff Flüssichkeht Lofen");
            this.addAbility(AbilitySerializers.RESTRICT_SLOTS, "Slots beschränken");
            this.addAbility(AbilitySerializers.PLAY_SOUND, "Sound");
            this.addAbility(AbilitySerializers.INTANGIBILITY, "Ungreifbarkeht");
            this.addAbility(AbilitySerializers.NAME_CHANGE, "Namesänderung");
            this.addAbility(AbilitySerializers.SCULK_IMMUNITY, "Sculk-Immunität");
            this.addAbility(AbilitySerializers.FIRE_ASPECT, "Vorbrennung");
            this.addAbility(AbilitySerializers.PARTICLES, "Partikel");
            this.addAbility(AbilitySerializers.FLIGHT, "Flug");
            this.addAbility(AbilitySerializers.GLIDING, "Gleiden");
            this.addAbility(AbilitySerializers.PREVENT_GLIDING, "Gleiden vorhindorn");
            this.addAbility(AbilitySerializers.PREVENT_SPRINTING, "Schbrinden vorhindorn");
            this.addAbility(AbilitySerializers.PREVENT_EFFECTS, "Effegde vorhindorn");
            this.addAbility(AbilitySerializers.PREVENT_HEALING, "Heilung vorhindorn");
            this.addAbility(AbilitySerializers.WALL_CLIMBING, "Wandkleddern");
            this.addAbility(AbilitySerializers.GUARD, "Vordeidichung");
            this.addAbility(AbilitySerializers.MULTI_JUMP, "Mehrfachschbrung");
            this.addAbility(AbilitySerializers.SLIDE, "Rudschen");
            this.addAbility(AbilitySerializers.CARRY, "Draachen");
            this.addAbility(AbilitySerializers.PUSH, "Schdoß");
            this.addAbility(AbilitySerializers.GRAPPLE, "Enderhagen");
            this.addAbility(AbilitySerializers.MODE_SWITCH, "Moduswechsl");
            this.addAbility(AbilitySerializers.COMBO_ATTACK, "Kombo-Angriff");
            this.addAbility(AbilitySerializers.ENTITY_GLOW, "Endidäden-Leuchda");
            this.addAbility(AbilitySerializers.ACTION_ON_HIT, "Agsjon bei Dreffor");
            this.addAbility(AbilitySerializers.AREA_ACTION, "Flächnagsjon");
            this.addAbility(AbilitySerializers.STEP_DOWN, "Runnorschdeing");
            this.addAbility(GeckoLibCompat.TRIGGER_LAYER_ANIMATION, "Geo Animation");

            // Config
            this.addConfigEntry(PalladiumClientConfig.DEV_MODE, "Addon Entwickler Modus");
            this.addConfigEntry(PalladiumClientConfig.ABILITY_BAR_ALIGNMENT, "Ability Bar - Position");
            this.addConfigEntry(PalladiumClientConfig.ABILITY_BAR_KEY_BIND_DISPLAY, "Ability Bar - Anzeige der Tastenbelegung");
            this.addConfigEntry(PalladiumClientConfig.HIDE_EXPERIMENTAL_WARNING, "Singleplayer - Experimental Settings Warnung ausblenden");
            this.addConfigEntry(PalladiumClientConfig.SCALE_CAMERA_FIX, "Visuell - Kameraskalierung fixen");
            this.addConfigEntry(PalladiumServerConfig.MAX_SUPERPOWER_SETS, "Max. Anzahl an Superkraft-Sets");

            // Key Mappings
            this.add("key.category.palladium.powers", "Gräfte");
            this.addKeyMapping("open_equipment", "Ohsrüstung öffnen/schließen");
            this.addKeyMapping("show_powers", "Gräfte");
            this.addKeyMapping("rotate_ability_list", "Durch Fähischgehtlehste rotieren");
            for (int i = 1; i <= 5; i++) {
                this.addKeyMapping("ability_" + i, "Fähischgeht #" + i);
            }

            // Commands
            this.add(SuperpowerCommand.ERROR_NO_LIVING_ENTITY, "%s is keen lebdisches Viech");
            this.add(SuperpowerCommand.QUERY_SUCCESS, "%s hat de folgende Superkrächte: %s");
            this.add(SuperpowerCommand.QUERY_NO_POWERS, "%s hat keene Superkrächte");
            this.add(SuperpowerCommand.SET_SUCCESS_SINGLE, "%s hat nu die Superkraft %s bekomm");
            this.add(SuperpowerCommand.SET_SUCCESS_MULTIPLE, "%s Viecher ham nu die Superkraft %s bekomm");
            this.add(SuperpowerCommand.ADD_ERROR_ALREADY_HAS, "%s hat die Superkraft doch schon");
            this.add(SuperpowerCommand.ADD_ERROR_NOT_POSSIBLE, "Die Superkraft kann mer %s nich mehr geb'n");
            this.add(SuperpowerCommand.ADD_ERROR_NONE_ADDED, "Keins von de Viecher konnt die Superkraft %s kriegn");
            this.add(SuperpowerCommand.ADD_SUCCESS_SINGLE, "%s hat nu die Superkraft %s bekomm");
            this.add(SuperpowerCommand.ADD_SUCCESS_MULTIPLE, "%s Viecher ham nu die Superkraft %s bekomm");
            this.add(SuperpowerCommand.REMOVE_ERROR_NO_MATCH, "Keene passnde Superkraft gefundn");
            this.add(SuperpowerCommand.REMOVE_SUCCESS_SINGLE, "Die Superkraft(e) von %s sin nu wech");
            this.add(SuperpowerCommand.REMOVE_SUCCESS_MULTIPLE, "Die Superkraft(e) von %s Viechern sin nu wech");
            this.add(SuperpowerCommand.REPLACE_SUCCESS_SINGLE, "E paar von %ss Superkrächte sin nu %s");
            this.add(SuperpowerCommand.REPLACE_SUCCESS_MULTIPLE, "E paar Superkrächte von %s Viechern sin nu %s");
            this.add(SuperpowerCommand.REPLACE_ERROR_NOT_POSSIBLE, "Die Superkraft kann mer %s nich mehr geb'n");
            this.add(SuperpowerCommand.REPLACE_ERROR_NONE_ADDED, "Keins von de Viecher konnt die Superkraft %s kriegn");

            this.add(DataAttachmentCommand.TRANS_UNKNOWN_TYPE, "Unbegannter Datenanhang: %s");
            this.add(DataAttachmentCommand.TRANS_NO_DATA, "Kehne Daten für Datenanlagentyp %s jefunden");
            this.add(DataAttachmentCommand.TRANS_GET_DATA, "Der Wert dor Datenanlage %s für %s is %s");
            this.add(DataAttachmentCommand.TRANS_PARSE_ERROR, "Fehler beim Parsen von Datenanhangswerten");
            this.add(DataAttachmentCommand.TRANS_SET_DATA_SINGLE, "Datenanhangswert %s für %s jetze uff %s jesetzt");
            this.add(DataAttachmentCommand.TRANS_SET_DATA_MULTIPLE, "Datenanhangswert %s für %s Entitäten is jetze uff %s jesetzt");
            this.add(DataAttachmentCommand.TRANS_ADD_DATA_SINGLE, "%3$s zu Datenanhang %1$s für %2$s dazujetan");
            this.add(DataAttachmentCommand.TRANS_ADD_DATA_MULTIPLE, "%3$s zu Datenanhang %1$s für %2$s Viecher dazujetan");
            this.add(DataAttachmentCommand.TRANS_SUBTRACT_DATA_SINGLE, "%3$s von Datenanhang %1$s für %2$s abgezohn");
            this.add(DataAttachmentCommand.TRANS_SUBTRACT_DATA_MULTIPLE, "%3$s von Datenanhang %1$s für %2$s Viechern abgezohn");
            this.add(DataAttachmentCommand.TRANS_NOT_A_NUMBER, "Datenanhang %s hat keen Zahlenwert drin");

            this.add(CustomizationCommand.UNLOCK_SUCCESS, "%s is nu ordentlich für %s freigeschaltn");
            this.add(CustomizationCommand.LOCK_SUCCESS, "%s is nu ordentlich für %s gesperrt");
            this.add(CustomizationCommand.ERROR_NOT_UNLOCKABLE, "%s kannsch du mit Befehln nich freischaltn");
            this.add(CustomizationCommand.ERROR_ALREADY_UNLOCKED, "%s is scho freigeschaldet");
            this.add(CustomizationCommand.ERROR_ALREADY_LOCKED, "%s is scho gesperrt");
            this.add(CustomizationCommand.ERROR_CANT_HAVE_CUSTOMIZATIONS, "Die Geeschd kann kee Anpassungen habn");

            this.add(ScreenCommand.TRANS_SHOWED_SCREEN_SINGLE, "Fenster %s für %s uffgemacht");
            this.add(ScreenCommand.TRANS_SHOWED_SCREEN_MULTIPLE, "Fenster %s für %s Spielor uffgemacht");

            this.add(SizeChangeCommand.ERROR_NO_LIVING_ENTITY, "%s is keen lebdisches Viech");
            this.add(SizeChangeCommand.SET_SUCCESS_SINGLE, "Größe von %s uff %s mid %s jesetzt");
            this.add(SizeChangeCommand.SET_SUCCESS_MULTIPLE, "Größe von %s Viechern uff %s mid %s jesetzt");
            this.add(SizeChangeCommand.CHANGE_SUCCESS_SINGLE, "Größenänderung von %s uff %s mid %s anjefahrn");
            this.add(SizeChangeCommand.CHANGE_SUCCESS_MULTIPLE, "Größenänderung von %s Viechern uff %s mid %s anjefahrn");
            this.add(SizeChangeCommand.RESET_SUCCESS_SINGLE, "Größe von %s zurückjesetzt");
            this.add(SizeChangeCommand.RESET_SUCCESS_MULTIPLE, "Größe von %s Viechern zurückjesetzt");
            this.add(SizeChangeCommand.RESET_TYPE_SUCCESS_SINGLE, "Größenmodifikator von %s für %s zurückjesetzt");
            this.add(SizeChangeCommand.RESET_TYPE_SUCCESS_MULTIPLE, "Größenmodifikator für %s von %s Viechern zurückjesetzt");
            this.add(SizeChangeCommand.QUERY_SUCCESS, "%s hat ne Größe von %s");
            this.add(SizeChangeCommand.QUERY_TYPE_SUCCESS, "Größenmodifikator von %s für %s is gerade %s");

            // Container
            this.add("container.palladium.tailoring", "Zuschneidern");
            this.add("container.palladium.tailoring.craft", "Herstellen");
            this.add("container.palladium.multiversal_iterator", "Multiversaler Itterador");

            // GUI
            this.add(PowerTreeWidget.TRANS_TITLE, "Kräfte");
            this.add(PowerTreeWidget.TRANS_CUSTOMIZE, "Anpassn");
            this.add(PowerTreeWidget.TRANS_KEY_UNLOCK, "Freischaltn");
            this.add(PowerTreeWidget.TRANS_KEY_NO_ABILITIES_LABEL, "Hier scheind es nüschd zu gähm...");
            this.add(PowerTreeWidget.TRANS_KEY_VERY_SAD_LABEL, ":(");
            this.add("gui.palladium.powers.buy_ability.experience_level", "%sx Erfahrungsschdufe");
            this.add("gui.palladium.powers.buy_ability.experience_level_plural", "%sx Erfahrungsschdufen");
            this.add("gui.palladium.powers.buy_ability.or", "oder");
            this.add("gui.palladium.powers.buy_ability.multiversal_designation", "%sx Multiversale Designation (%s)");
            this.add("gui.palladium.powers.buy_ability.multiversal_designation_plural", "%sx Multiversale Designationen (%s)");
            this.add(PlayerCustomizationScreen.TITLE_TRANSLATION_KEY, "Anpassungen");
            this.add(EyeSelectionScreen.TRANS_TITLE, "Such dir dei Oogn aus");
            this.add(EyeSelectionScreen.TRANS_RESET, "Zrücksetzn");
            this.add(EyeSelectionScreen.TRANS_SAVE, "Speichern");
            this.add(CustomizationsGrid.NO_CUSTOMIZATIONS_LABEL, "Hier scheind es nüschd zu gähm...");
            this.add(CustomizationsGrid.VERY_SAD_LABEL, ":(");
            this.add(CustomizationsGrid.UNLOCK, "Freischaltn");
            this.add(CustomizationsGrid.UNLOCK_DESC, "Willste das freischaltn?");
            this.add("gui.palladium.category.tailoring", "Zuschneidern");
            this.add("gui.palladium.category.multiversal_variants", "Multiversale Varianten");

            // Addon Hub
            this.add(AddonHubScreen.TITLE_KEY, "Balladium Addons");
            this.add(AddonHubScreen.LOADING_KEY, "Lade Addons...");
            this.add(AddonHubScreen.EMPTY_KEY, "Geene Addons gefunden.");
            this.add(AddonHubScreen.ERROR_KEY, "Addons gonnden nich geladen wärn.");
            this.add(AddonHubScreen.SEARCH_HINT_KEY, "Addons suchn...");
            this.add(AddonSort.DOWNLOADS.getTranslationKey(), "Beliebd");
            this.add(AddonSort.DOWNLOADS.getDescriptionKey(), "Nach Downloads sordieren");
            this.add(AddonSort.LATEST.getTranslationKey(), "Neisde");
            this.add(AddonSort.LATEST.getDescriptionKey(), "Nach zuledzd hinzugefügd sordieren");
            this.add(AddonSort.NAME.getTranslationKey(), "A-Z");
            this.add(AddonSort.NAME.getDescriptionKey(), "Alfabedisch nach Name sordieren");
            this.add(AddonInfoPanel.BY_KEY, "von %s");
            this.add(AddonInfoPanel.VERSIONS_KEY, "Versionen: %s");
            this.add(AddonInfoPanel.DOWNLOADS_KEY, "Downloads: %s");
            this.add(AddonHubScreen.BUTTON_KEY, "Balladium Addons");
            this.add(AddonHubScreen.SUPPORT_KEY, "Unterstütze Balladium!");

            // Toasts
            this.add(CustomizationToast.TITLE_TEXT, "Neue Anpassung freigeschaldet!");

            // Customizations
            this.addCustomization(BuiltinCustomization.Type.LUCRAFT_ARC_REACTOR.getResourceKey(), "Lucraft Arc Reaktor");
            this.addCustomizationCategory(CustomizationCategories.HAT, "Hood");
            this.addCustomizationCategory(CustomizationCategories.HEAD, "Nüschl");
            this.addCustomizationCategory(CustomizationCategories.CHEST, "Bruscht");
            this.addCustomizationCategory(CustomizationCategories.BACK, "Rieggn");
            this.addCustomizationCategory(CustomizationCategories.ARMS, "Arme");
            this.addCustomizationCategory(CustomizationCategories.LEGS, "Behne");

            // Sounds
            this.addSubtitle(PalladiumSoundEvents.PLAYER_FLIGHT_WEB_SHOOT, "Netz jeschossn");
            this.addSubtitle(PalladiumSoundEvents.PLAYER_FLIGHT_PROPELLED, "Fliechn");
            this.addSubtitle(PalladiumSoundEvents.PLAYER_FLIGHT_SWING, "Schwingn");
            this.addSubtitle(PalladiumSoundEvents.MULTIVERSE_SEARCH, "Multiversumssuche");
            this.addSubtitle(PalladiumSoundEvents.MULTIVERSAL_VARIANT_CRAFTED, "Multiversale Variante hergestellt");

            // Misc
            this.add(Universe.makeTranslationKey(Palladium.id("generic")), "Erde-%s");
        }
    }

}
