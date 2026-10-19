package lucraft.mods.heroes.speedsterheroes.speedstertypes;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Multimap;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.abilities.AbilitySuitRing;
import lucraft.mods.heroes.speedsterheroes.client.models.ModelJayGarrickHelm;
import lucraft.mods.heroes.speedsterheroes.client.models.ModelSpeedsterAdvancedBiped;
import lucraft.mods.heroes.speedsterheroes.client.models.ModelTheRivalEars;
import lucraft.mods.heroes.speedsterheroes.entity.EntityTimeRemnant;
import lucraft.mods.heroes.speedsterheroes.items.ItemSpeedsterArmor;
import lucraft.mods.heroes.speedsterheroes.items.ItemSymbol;
import lucraft.mods.heroes.speedsterheroes.proxies.SpeedsterHeroesProxy;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.abilities.Ability;
import lucraft.mods.lucraftcore.client.model.ModelAdvancedBiped;
import lucraft.mods.lucraftcore.recipes.ISuitMakerRecipe.SuitMakerRecipeType;
import lucraft.mods.lucraftcore.recipes.LucraftCoreRecipes;
import lucraft.mods.lucraftcore.recipes.SuitMakerAdvRecipe;
import lucraft.mods.lucraftcore.suitset.SuitSet;
import lucraft.mods.lucraftcore.util.LucraftCoreClientUtil;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import lucraft.mods.lucraftcore.util.LucraftKeys;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpeedsterType extends SuitSet {

	public static SpeedsterType flashS1 = new SpeedsterTypeFlashS1();
	public static SpeedsterType flashS2 = new SpeedsterTypeFlash();
	public static SpeedsterType futureFlash = new SpeedsterTypeFutureFlash();
	public static SpeedsterType reverseFlash = new SpeedsterTypeReverseFlash();
	public static SpeedsterType zoom = new SpeedsterTypeZoom();
	public static SpeedsterType jayGarrick = new SpeedsterTypeJayGarrick();
	public static SpeedsterType jayGarrick2 = new SpeedsterTypeJayGarrick2();
	public static SpeedsterType kidFlash = new SpeedsterTypeKidFlash();
	public static SpeedsterType theRival = new SpeedsterTypeTheRival();
	public static SpeedsterType trajectory = new SpeedsterTypeTrajectory();
	public static SpeedsterType jesseQuick = new SpeedsterTypeJesseQuick();
	public static SpeedsterType savitar = new SpeedsterTypeSavitar();
	public static SpeedsterType blackFlash = new SpeedsterTypeBlackFlash();
	public static SpeedsterType acceleratedMan = new SpeedsterTypeAcceleratedMan();
	public static SpeedsterType dceuFlash = new SpeedsterTypeDCEUFlash();
	public static SpeedsterType comicFlash = new SpeedsterTypeComicFlash();
	public static SpeedsterType comicReverseFlash = new SpeedsterTypeComicReverseFlash();
	public static SpeedsterType professorZoom = new SpeedsterTypeProfessorZoom();
	public static SpeedsterType new52Flash = new SpeedsterTypeNew52Flash();
	public static SpeedsterType wallyWestComicFlash = new SpeedsterTypeWallyWestComicFlash();
	public static SpeedsterType wallyWestComicKidFlash = new SpeedsterTypeWallyWestComicKidFlash();
	public static SpeedsterType rebirthWallyWest = new SpeedsterTypeRebirthWallyWest();
	public static SpeedsterType comicJesseQuick = new SpeedsterTypeComicJesseQuick();
	public static SpeedsterType godspeed = new SpeedsterTypeGodspeed();
	public static SpeedsterType quicksilver = new SpeedsterTypeQuicksilver();
	public static SpeedsterType xmenQuicksilver = new SpeedsterTypeXmenQuicksilver();
	public static SpeedsterType comicQuicksilver = new SpeedsterTypeComicQuicksilver();
	public static SpeedsterType speed = new SpeedsterTypeSpeed();
	public static SpeedsterType flay = new SpeedsterTypeFlay();
	public static SpeedsterType starLabsTraining = new SpeedsterTypeSTARLabsTraining();

	public static ArrayList<SpeedsterType> speedsterTypes = new ArrayList<SpeedsterType>();

	public static void preInit() {
		registerSpeedsterType(flashS1);
		registerSpeedsterType(flashS2);
		registerSpeedsterType(futureFlash);
		registerSpeedsterType(reverseFlash);
		registerSpeedsterType(zoom);
		registerSpeedsterType(jayGarrick);
		registerSpeedsterType(jayGarrick2);
		registerSpeedsterType(kidFlash);
		registerSpeedsterType(theRival);
		registerSpeedsterType(trajectory);
		registerSpeedsterType(jesseQuick);
		registerSpeedsterType(savitar);
		registerSpeedsterType(blackFlash);
		registerSpeedsterType(acceleratedMan);
		registerSpeedsterType(dceuFlash);
		registerSpeedsterType(comicFlash);
		registerSpeedsterType(comicReverseFlash);
		registerSpeedsterType(professorZoom);
		registerSpeedsterType(new52Flash);
		registerSpeedsterType(wallyWestComicFlash);
		registerSpeedsterType(wallyWestComicKidFlash);
		registerSpeedsterType(rebirthWallyWest);
		registerSpeedsterType(comicJesseQuick);
		registerSpeedsterType(godspeed);
		registerSpeedsterType(quicksilver);
		registerSpeedsterType(xmenQuicksilver);
		registerSpeedsterType(comicQuicksilver);
		registerSpeedsterType(speed);
		registerSpeedsterType(flay);
		registerSpeedsterType(starLabsTraining);
		
		registerSpeedsterTypeForSymbol(flashS2);
		registerSpeedsterTypeForSymbol(reverseFlash);
		registerSpeedsterTypeForSymbol(zoom);
		registerSpeedsterTypeForSymbol(jayGarrick);
		registerSpeedsterTypeForSymbol(jayGarrick2);
		registerSpeedsterTypeForSymbol(kidFlash);
		registerSpeedsterTypeForSymbol(theRival);
		registerSpeedsterTypeForSymbol(trajectory);
		registerSpeedsterTypeForSymbol(dceuFlash);
		registerSpeedsterTypeForSymbol(comicFlash);
		registerSpeedsterTypeForSymbol(comicReverseFlash);
		registerSpeedsterTypeForSymbol(professorZoom);
		registerSpeedsterTypeForSymbol(new52Flash);
		registerSpeedsterTypeForSymbol(godspeed);
		registerSpeedsterTypeForSymbol(flay);
		registerSpeedsterTypeForSymbol(jesseQuick);
		registerSpeedsterTypeForSymbol(rebirthWallyWest);
		registerSpeedsterTypeForSymbol(wallyWestComicFlash);
		registerSpeedsterTypeForSymbol(wallyWestComicKidFlash);
		registerSpeedsterTypeForSymbol(comicJesseQuick);
		registerSpeedsterTypeForSymbol(starLabsTraining);
		registerSpeedsterTypeForSymbol(flashS1);
	}

	public static void init() {
		for (ItemSpeedsterArmor armor : ItemSpeedsterArmor.armorToCraft) {
			if (armor.getSpeedsterType() instanceof IAutoSpeedsterRecipe) {
				IAutoSpeedsterRecipe recipe = (IAutoSpeedsterRecipe) armor.getSpeedsterType();
				EntityEquipmentSlot armorSlot = armor.armorType;

				if (armorSlot == EntityEquipmentSlot.HEAD)
					LucraftCoreRecipes.addSuitMakerHelmetRecipe(recipe.getFirstItemStack(new ItemStack(armor), armorSlot), recipe.getSecondItemStack(new ItemStack(armor), armorSlot), recipe.getThirdItemStack(new ItemStack(armor), armorSlot), new ItemStack(armor));
				else if (armorSlot == EntityEquipmentSlot.CHEST)
					LucraftCoreRecipes.addSuitMakerChestplateRecipe(recipe.getFirstItemStack(new ItemStack(armor), armorSlot), recipe.getSecondItemStack(new ItemStack(armor), armorSlot), recipe.getThirdItemStack(new ItemStack(armor), armorSlot), new ItemStack(armor));
				else if (armorSlot == EntityEquipmentSlot.LEGS)
					LucraftCoreRecipes.addSuitMakerLegsRecipe(recipe.getFirstItemStack(new ItemStack(armor), armorSlot), recipe.getSecondItemStack(new ItemStack(armor), armorSlot), recipe.getThirdItemStack(new ItemStack(armor), armorSlot), new ItemStack(armor));
				else if (armorSlot == EntityEquipmentSlot.FEET)
					LucraftCoreRecipes.addSuitMakerBootsRecipe(recipe.getFirstItemStack(new ItemStack(armor), armorSlot), recipe.getSecondItemStack(new ItemStack(armor), armorSlot), recipe.getThirdItemStack(new ItemStack(armor), armorSlot), new ItemStack(armor));
			} else if (armor.getSpeedsterType() instanceof IAutoSpeedsterRecipeAdvanced) {
				IAutoSpeedsterRecipeAdvanced recipe = (IAutoSpeedsterRecipeAdvanced) armor.getSpeedsterType();
				EntityEquipmentSlot armorSlot = armor.armorType;

				if (armorSlot == EntityEquipmentSlot.HEAD)
					LucraftCoreRecipes.suitMakerHelmetRecipes.add(new SuitMakerAdvRecipe(recipe.getFirstItemStack(new ItemStack(armor), armorSlot), recipe.getSecondItemStack(new ItemStack(armor), armorSlot), recipe.getThirdItemStack(new ItemStack(armor), armorSlot), new ItemStack(armor), SuitMakerRecipeType.HELMET));
				else if (armorSlot == EntityEquipmentSlot.CHEST)
					LucraftCoreRecipes.suitMakerChestplateRecipes.add(new SuitMakerAdvRecipe(recipe.getFirstItemStack(new ItemStack(armor), armorSlot), recipe.getSecondItemStack(new ItemStack(armor), armorSlot), recipe.getThirdItemStack(new ItemStack(armor), armorSlot), new ItemStack(armor), SuitMakerRecipeType.CHESTPLATE));
				else if (armorSlot == EntityEquipmentSlot.LEGS)
					LucraftCoreRecipes.suitMakerLegsRecipes.add(new SuitMakerAdvRecipe(recipe.getFirstItemStack(new ItemStack(armor), armorSlot), recipe.getSecondItemStack(new ItemStack(armor), armorSlot), recipe.getThirdItemStack(new ItemStack(armor), armorSlot), new ItemStack(armor), SuitMakerRecipeType.LEGS));
				else
					LucraftCoreRecipes.suitMakerBootsRecipes.add(new SuitMakerAdvRecipe(recipe.getFirstItemStack(new ItemStack(armor), armorSlot), recipe.getSecondItemStack(new ItemStack(armor), armorSlot), recipe.getThirdItemStack(new ItemStack(armor), armorSlot), new ItemStack(armor), SuitMakerRecipeType.BOOTS));
			}
		}
	}

	// ---------------------------------------------------------------------------------------------------------------------------------

	public static void registerSpeedsterType(SpeedsterType type) {
		if (!speedsterTypes.contains(type))
			speedsterTypes.add(type);
	}
	
	public static void registerSpeedsterTypeForSymbol(SpeedsterType type) {
		if (!ItemSymbol.list.contains(type))
			ItemSymbol.list.add(type);
	}

	public static SpeedsterType getSpeedsterTypeFromName(String name) {
		for (SpeedsterType s : speedsterTypes) {
			if (s.getUnlocalizedName().equalsIgnoreCase(name)) {
				return s;
			}
		}
		return null;
	}

	public static int getSpeedsterTypeId(SpeedsterType type) {
		return speedsterTypes.indexOf(type);
	}

	public static SpeedsterType getSpeedsterTypeFromId(int id) {
		return speedsterTypes.get(id);
	}

	// ---------------------------------------------------------------------------------------------------------------------------------

	public ItemStack helmet = ItemStack.EMPTY;
	public ItemStack chestplate = ItemStack.EMPTY;
	public ItemStack legs = ItemStack.EMPTY;
	public ItemStack boots = ItemStack.EMPTY;
	
	private TrailType trail;
	private boolean doesFlicker;
	private boolean doesVibrate;
	private int extraSpeedLevel;
	private float[] speedLevelRenderData;

	protected SpeedsterType(String name, TrailType trail) {
		super(name);
		this.trail = trail;
		this.doesFlicker = false;
		this.doesVibrate = false;
		this.extraSpeedLevel = 3;
		this.speedLevelRenderData = new float[] { 0, 21 };
	}

	public SpeedsterType enableFlicker() {
		this.doesFlicker = true;
		return this;
	}

	public SpeedsterType enableVibrate() {
		this.doesVibrate = true;
		return this;
	}
	
	@Override
	public boolean canOpenHelmet() {
		return true;
	}

	public SpeedsterType setExtraSpeedLevel(int i) {
		this.extraSpeedLevel = i;
		return this;
	}

	public SpeedsterType setSpeedLevelRenderData(int texturePosX, int texturePosY) {
		this.speedLevelRenderData = new float[] { texturePosX, texturePosY };
		return this;
	}

	public String getDisplayName() {
		return LucraftCoreUtil.translateToLocal("speedsterheroes.speedstertypes." + getUnlocalizedName() + ".name");
	}
	
	@Override
	public String getModId() {
		return SpeedsterHeroes.MODID;
	}
	
	public TrailType getTrailType() {
		return trail;
	}

	public boolean doesFlicker() {
		return doesFlicker;
	}

	public boolean doesVibrate() {
		return doesVibrate;
	}

	@Override
	public Item getHelmet() {
		if(helmet.isEmpty())
			return null;
		return helmet.getItem();
	}
	
	@Override
	public Item getChestplate() {
		if(chestplate.isEmpty())
			return null;
		return chestplate.getItem();
	}
	
	@Override
	public Item getLegs() {
		if(legs.isEmpty())
			return null;
		return legs.getItem();
	}
	
	@Override
	public Item getBoots() {
		if(boots.isEmpty())
			return null;
		return boots.getItem();
	}
	
	@Override
	public CreativeTabs getCreativeTab() {
		return SpeedsterHeroesProxy.tabSpeedsterArmor;
	}

	public int getExtraSpeedLevel(SpeedsterType type, EntityPlayer player) {
		return extraSpeedLevel;
	}

	public float[] getSpeedLevelRenderData() {
		return speedLevelRenderData;
	}

	public String getSpeedLevelTextureLocation() {
		return SpeedsterHeroes.ASSETDIR + "textures/gui/speedHud.png";
	}

	public float getTachyonDeviceModelTranslation(Entity entity, float f, float f1, float f2, float f3, float f4, float f5, ModelAdvancedBiped model) {
		return -0.025F;
	}

	public boolean shouldHideNameTag(EntityLivingBase entity, boolean hasMaskOpen) {
		return SpeedsterHeroesUtil.hasArmorOn(entity) && !hasMaskOpen;
	}

	public boolean hasSymbol() {
		return true;
	}

	public boolean canBeCraftedDescription(ItemStack stack) {
		return true;
	}

	public ItemStack getRepairItem(ItemStack toRepair) {
		return ItemStack.EMPTY;
	}
	
	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		if(!getRepairItem(toRepair).isEmpty())
			return getRepairItem(toRepair).getItem() == repair.getItem() && getRepairItem(toRepair).getItemDamage() == repair.getItemDamage();

		return false;
	}
	
	public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack, Multimap<String, AttributeModifier> origMap) {
		return origMap;
	}

	@Override
	public ArmorMaterial getArmorMaterial(EntityEquipmentSlot slot) {
		return ArmorMaterial.CHAIN;
	}
	
	public String getArmorTexturePath(ItemStack stack, Entity entity, EntityEquipmentSlot slot, boolean light, boolean smallArms, boolean helmetOpen) {
		smallArms = entity instanceof EntityTimeRemnant && ((EntityTimeRemnant)entity).getOwner() != null ? LucraftCoreClientUtil.hasSmallArms((EntityPlayer)((EntityTimeRemnant)entity).getOwner()) : smallArms;
		return super.getArmorTexturePath(stack, entity, slot, light, smallArms, helmetOpen);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(ItemStack stack, Entity entity, EntityEquipmentSlot slot, boolean light, boolean smallArms, boolean helmetOpen) {
		smallArms = entity instanceof EntityTimeRemnant && ((EntityTimeRemnant)entity).getOwner() != null ? LucraftCoreClientUtil.hasSmallArms((EntityPlayer)((EntityTimeRemnant)entity).getOwner()) : smallArms;
		
		if(this == SpeedsterType.jayGarrick && slot == EntityEquipmentSlot.HEAD)
			return new ModelJayGarrickHelm(0.55F, getArmorTexturePath(stack, entity, slot, false, false, false), "", this, slot);
		else if(this == SpeedsterType.theRival && slot == EntityEquipmentSlot.HEAD)
			return new ModelTheRivalEars(0.55F, getArmorTexturePath(stack, entity, slot, false, false, false), "", this, slot);
		
		return new ModelSpeedsterAdvancedBiped(getArmorModelScale(slot), getArmorTexturePath(stack, entity, slot, false, smallArms, helmetOpen), getArmorTexturePath(stack, entity, slot, true, smallArms, helmetOpen), this, slot, smallArms);
	}
	
	@Override
	public List<Ability> addDefaultAbilities(EntityPlayer player, List<Ability> list) {
		list.add(new AbilitySuitRing(player).setUnlocked(true));
		return super.addDefaultAbilities(player, list);
	}
	
	@Override
	public Ability getSuitAbilityForKey(LucraftKeys key, List<Ability> list) {
		if(key == LucraftKeys.ARMOR_5)
			return Ability.getAbilityFromClass(list, AbilitySuitRing.class);
		return super.getSuitAbilityForKey(key, list);
	}
	
}

