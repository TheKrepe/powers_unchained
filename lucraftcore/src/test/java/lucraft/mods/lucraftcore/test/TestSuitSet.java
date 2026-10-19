package lucraft.mods.lucraftcore.test;

import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityEnergyBlast;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityFlight;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityWaterBreathing;
import lucraft.mods.lucraftcore.superpowers.abilities.predicates.AbilityConditionHeldItem;
import lucraft.mods.lucraftcore.superpowers.abilities.predicates.AbilityConditionLevel;
import lucraft.mods.lucraftcore.superpowers.abilities.predicates.AbilityConditionOr;
import lucraft.mods.lucraftcore.superpowers.suitsets.ItemSuitSetArmor;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent.Register;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

public class TestSuitSet extends SuitSet {

    public TestSuitSet(String name) {
        super(name);
    }

    protected Item helmet;
    protected Item chestplate;
    protected Item legs;
    protected Item boots;

    @Override
    public String getDisplayNameForItem(Item item, ItemStack stack, EntityEquipmentSlot armorType, String origName) {
        return "Nice.";
    }

    @Override
    public String getModId() {
        return TestMod.MODID;
    }

    @Override
    public ArmorMaterial getArmorMaterial(EntityEquipmentSlot slot) {
        return ArmorMaterial.DIAMOND;
    }

    public void registerItems(Register<Item> e) {
        e.getRegistry().register(this.helmet = new ItemSuitSetArmorTest(this, EntityEquipmentSlot.HEAD).setRegistryName("test_helmet"));
        e.getRegistry().register(this.chestplate = new ItemSuitSetArmor(this, EntityEquipmentSlot.CHEST).setRegistryName("test_chestplate"));
        e.getRegistry().register(this.legs = new ItemSuitSetArmor(this, EntityEquipmentSlot.LEGS).setRegistryName("test_legs"));
        e.getRegistry().register(this.boots = new ItemSuitSetArmor(this, EntityEquipmentSlot.FEET).setRegistryName("test_boots"));
    }

    @SideOnly(Side.CLIENT)
    public void registerModels() {
        if (helmet != null)
            ModelLoader.setCustomModelResourceLocation(getHelmet(), 0, new ModelResourceLocation(TestMod.MODID + ":" + getRegistryName() + "_suit", "helmet"));
        if (chestplate != null)
            ModelLoader.setCustomModelResourceLocation(getChestplate(), 0, new ModelResourceLocation(TestMod.MODID + ":" + getRegistryName() + "_suit", "chestplate"));
        if (legs != null)
            ModelLoader.setCustomModelResourceLocation(getLegs(), 0, new ModelResourceLocation(TestMod.MODID + ":" + getRegistryName() + "_suit", "legs"));
        if (boots != null)
            ModelLoader.setCustomModelResourceLocation(getBoots(), 0, new ModelResourceLocation(TestMod.MODID + ":" + getRegistryName() + "_suit", "boots"));
    }

    @Override
    public Item getHelmet() {
        return helmet;
    }

    @Override
    public Item getChestplate() {
        return chestplate;
    }

    @Override
    public Item getLegs() {
        return legs;
    }

    @Override
    public Item getBoots() {
        return boots;
    }

    @Override
    public boolean canOpenArmor(EntityEquipmentSlot slot) {
        return slot == EntityEquipmentSlot.HEAD || slot == EntityEquipmentSlot.CHEST;
    }

    @Override
    public Ability.AbilityMap addDefaultAbilities(EntityLivingBase entity, Ability.AbilityMap abilities, Ability.EnumAbilityContext context) {
        abilities.put("test", new AbilityTest(entity));
        abilities.put("flight", new AbilityFlight(entity).setDataValue(AbilityFlight.SPEED, 1F).setDataValue(AbilityFlight.SPRINT_SPEED, 5F));
        abilities.put("energy_blast", new AbilityEnergyBlast(entity).setDataValue(AbilityEnergyBlast.COLOR, Color.GREEN).setMaxCooldown(5 * 20).addCondition(new AbilityConditionOr(new AbilityConditionHeldItem(Items.APPLE, EnumHand.MAIN_HAND), new AbilityConditionLevel(5))));
        return super.addDefaultAbilities(entity, abilities, context);
    }
}
