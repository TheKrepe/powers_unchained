package lucraft.mods.lucraftcore.superpowers.suitsets;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityGenerator;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataBarColor;
import lucraft.mods.lucraftcore.superpowers.effects.Effect;
import lucraft.mods.lucraftcore.superpowers.effects.EffectHandler;
import lucraft.mods.lucraftcore.util.abilitybar.EnumAbilityBarColor;
import lucraft.mods.lucraftcore.util.creativetabs.CreativeTabRegistry;
import lucraft.mods.lucraftcore.util.helper.ItemHelper;
import lucraft.mods.lucraftcore.util.items.OpenableArmor;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.EntityEquipmentSlot.Type;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class JsonSuitSet extends SuitSet {

    protected ResourceLocation parent;
    public JsonObject jsonOriginal;
    public ResourceLocation loc;
    public ArmorMaterial material;
    public ITextComponent name;
    public ITextComponent desc;
    public Map<EntityEquipmentSlot, JsonSuitSetSlotInfo> slotInfos;
    public List<AbilityGenerator> abilityGenerators;
    public boolean parentAbilities;
    public List<Effect> effects;
    public NBTTagCompound data;
    public String tab;

    public JsonSuitSet(String name) {
        super(name);
    }

    public SuitSet getParent() {
        if (this.parent == null)
            return null;
        return SuitSet.REGISTRY.getObject(this.parent);
    }

    @Override
    public void registerItems(RegistryEvent.Register<Item> e) {
        if (slotInfos.containsKey(EntityEquipmentSlot.HEAD))
            e.getRegistry().register(this.helmet = createItem(this, EntityEquipmentSlot.HEAD));

        e.getRegistry().register(this.chestplate = createItem(this, EntityEquipmentSlot.CHEST));

        if (slotInfos.containsKey(EntityEquipmentSlot.LEGS))
            e.getRegistry().register(this.legs = createItem(this, EntityEquipmentSlot.LEGS));

        if (slotInfos.containsKey(EntityEquipmentSlot.FEET))
            e.getRegistry().register(this.boots = createItem(this, EntityEquipmentSlot.FEET));

        CreativeTabRegistry.getOrCreateCreativeTab(this.tab, new ItemStack(getChestplate()));
    }

    @Override
    public ItemSuitSetArmor createItem(SuitSet suitSet, EntityEquipmentSlot slot) {
        if (getParent() != null)
            return getParent().createItem(suitSet, slot);
        return super.createItem(suitSet, slot);
    }

    @Override
    public String getUnlocalizedName() {
        return loc.toString();
    }

    @SideOnly(Side.CLIENT)
    public void registerModel(Item item, EntityEquipmentSlot slot) {
        if (item != null && slotInfos.get(slot).itemModel != null && !slotInfos.get(slot).itemModelInfo.isEmpty()) {
            JsonSuitSetItemModelType type = slotInfos.get(slot).itemModel;
            String s = slotInfos.get(slot).itemModelInfo;
            if (type == JsonSuitSetItemModelType.MODEL) {
                ResourceLocation loc = new ResourceLocation(s);
                ItemHelper.registerItemModel(item, loc.getNamespace(), loc.getPath());
            }
            if (type == JsonSuitSetItemModelType.BLOCKSTATE)
                ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(new ResourceLocation(s), slot.toString().toLowerCase()));
        }
    }

    @Override
    public String getDisplayName() {
        return name.getFormattedText();
    }

    @Override
    public String getDisplayNameForItem(Item item, ItemStack stack, EntityEquipmentSlot armorType, String origName) {
        return slotInfos.get(armorType).name.getFormattedText();
    }

    @Override
    public List<String> getExtraDescription(ItemStack stack) {
        return desc == null ? null : Arrays.asList(desc.getFormattedText());
    }

    @Override
    public String getModId() {
        return loc.getNamespace();
    }

    @Override
    public ArmorMaterial getArmorMaterial(EntityEquipmentSlot slot) {
        return material;
    }

    @Override
    public boolean canOpenArmor(EntityEquipmentSlot slot) {
        return slotInfos.get(slot).openable;
    }

    @Override
    public boolean showInCreativeTab() {
        return !tab.equalsIgnoreCase("none");
    }

    @Override
    public CreativeTabs getCreativeTab() {
        return CreativeTabRegistry.getCreativeTab(tab);
    }

    @Override
    public boolean hasGlowyThings(EntityLivingBase entity, EntityEquipmentSlot slot) {
        if (this.slotInfos.containsKey(slot)) {
            return this.slotInfos.get(slot).glow;
        }
        return super.hasGlowyThings(entity, slot);
    }

    @Override
    public float getGlowOpacity(SuitSet suitSet, EntityLivingBase entity, EntityEquipmentSlot slot) {
        if (getParent() != null)
            return getParent().getGlowOpacity(suitSet, entity, slot);
        return super.getGlowOpacity(suitSet, entity, slot);
    }

    @Override
    public ModelBiped getArmorModel(SuitSet suitSet, ItemStack stack, Entity entity, EntityEquipmentSlot slot, boolean light, boolean smallArms, boolean open) {
        if (getParent() != null)
            return getParent().getArmorModel(suitSet, stack, entity, slot, light, smallArms, open);
        return super.getArmorModel(suitSet, stack, entity, slot, light, smallArms, open);
    }

    @Override
    public float getArmorModelScale(EntityEquipmentSlot armorSlot) {
        if (slotInfos.containsKey(armorSlot) && slotInfos.get(armorSlot).armor_model_scale > 0F)
            return slotInfos.get(armorSlot).armor_model_scale;
        else
            return super.getArmorModelScale(armorSlot);
    }

    @Override
    public String getArmorTexturePath(ItemStack stack, Entity entity, EntityEquipmentSlot slot, boolean light, boolean smallArms, boolean open) {
        JsonSuitSetSlotInfo info = slotInfos.get(slot);
        String key = light ? "glow" : "normal";
        if (smallArms && info.textures.containsKey(key + "_smallarms"))
            key = key + "_smallarms";
        if (open && info.textures.containsKey(key + "_open"))
            key = key + "_open";

        return info.textures.containsKey(key) ? info.textures.get(key) : "";
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void bindArmorTexture(SuitSet suitSet, Entity entity, float f, float f1, float f2, float f3, float f4, float f5, ResourceLocation normalTex, ResourceLocation glowTex, boolean glow, EntityEquipmentSlot slot, boolean smallArms) {
        if (getParent() != null)
            getParent().bindArmorTexture(suitSet, entity, f, f1, f2, f3, f4, f5, normalTex, glowTex, glow, slot, smallArms);
        else
            super.bindArmorTexture(suitSet, entity, f, f1, f2, f3, f4, f5, normalTex, glowTex, glow, slot, smallArms);
    }

    @Override
    public Ability.AbilityMap addDefaultAbilities(EntityLivingBase entity, Ability.AbilityMap abilities, Ability.EnumAbilityContext context) {
        if (parentAbilities && getParent() != null)
            getParent().addDefaultAbilities(entity, abilities, context);
        for (AbilityGenerator ab : abilityGenerators) {
            Ability ability = ab.create(entity, abilities);
            if (ability != null)
                abilities.put(ab.key, ability);
        }
        return super.addDefaultAbilities(entity, abilities, context);
    }

    @Override
    public void onEquip(SuitSet suitSet, EntityLivingBase player) {
        if (getParent() != null)
            getParent().onEquip(suitSet, player);
        else
            super.onEquip(suitSet, player);
    }

    @Override
    public void onUnequip(SuitSet suitSet, EntityLivingBase player) {
        if (getParent() != null)
            getParent().onUnequip(suitSet, player);
        else
            super.onUnequip(suitSet, player);
    }

    @Override
    public void onUpdate(SuitSet suitSet, EntityLivingBase player) {
        if (getParent() != null)
            getParent().onUpdate(suitSet, player);
        else
            super.onUpdate(suitSet, player);
    }

    @Override
    public List<Effect> getEffects() {
        return this.effects;
    }

    @Override
    public NBTTagCompound getData() {
        return data;
    }

    public JsonSuitSet deserialize(JsonObject jsonobject, ResourceLocation loc) throws Exception {
        JsonObject armor_material = JsonUtils.getJsonObject(jsonobject, "armor_material");
        JsonArray damage_reduction = JsonUtils.getJsonArray(armor_material, "damage_reduction_amounts");
        ArmorMaterial material = EnumHelper.addArmorMaterial(loc.toString(), "", JsonUtils.getInt(armor_material, "durability"), new int[]{damage_reduction.get(0).getAsInt(), damage_reduction.get(1).getAsInt(), damage_reduction.get(2).getAsInt(), damage_reduction.get(3).getAsInt()}, JsonUtils.getInt(armor_material, "enchantibility"), SoundEvent.REGISTRY.getObject(new ResourceLocation(JsonUtils.getString(armor_material, "sound"))), JsonUtils.getFloat(armor_material, "toughness"));

        ITextComponent name = ITextComponent.Serializer.jsonToComponent(JsonUtils.getJsonObject(jsonobject, "name").toString());
        ITextComponent desc = null;
        if (JsonUtils.hasField(jsonobject, "description"))
            desc = ITextComponent.Serializer.jsonToComponent(JsonUtils.getJsonObject(jsonobject, "description").toString());

        if (JsonUtils.hasField(jsonobject, "parent"))
            this.parent = new ResourceLocation(JsonUtils.getString(jsonobject, "parent"));
        this.name = name;
        this.loc = loc;
        this.desc = desc;
        this.material = material;
        this.slotInfos = new HashMap<>();
        this.abilityGenerators = new ArrayList<>();
        this.effects = new ArrayList<>();
        this.tab = JsonUtils.getString(jsonobject, "creative_tab", "addon_packs");
        this.parentAbilities = JsonUtils.getBoolean(jsonobject, "parent_abilities", false);

        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot.getSlotType() == Type.ARMOR) {
                if (slot == EntityEquipmentSlot.CHEST && !JsonUtils.hasField(JsonUtils.getJsonObject(jsonobject, "armor_parts"), slot.toString().toLowerCase()))
                    throw new Exception("Suit sets need a chestplate!");

                if (JsonUtils.hasField(JsonUtils.getJsonObject(jsonobject, "armor_parts"), slot.toString().toLowerCase())) {
                    JsonSuitSetSlotInfo info = new JsonSuitSetSlotInfo();
                    JsonObject armor = JsonUtils.getJsonObject(JsonUtils.getJsonObject(jsonobject, "armor_parts"), slot.toString().toLowerCase());

                    info.openable = JsonUtils.getBoolean(armor, "openable", false);
                    info.glow = JsonUtils.getBoolean(armor, "glow", false);
                    info.armor_model_scale = JsonUtils.getFloat(armor, "armor_model_scale", 0F);
                    info.name = ITextComponent.Serializer.jsonToComponent(JsonUtils.getJsonObject(armor, "name").toString());
                    info.barColor = EnumAbilityBarColor.fromName(JsonUtils.getString(armor, "ability_bar_color", ""));

                    info.textures = new HashMap<>();
                    for (Map.Entry<String, JsonElement> entry : JsonUtils.getJsonObject(armor, "textures").entrySet()) {
                        info.textures.put(entry.getKey(), entry.getValue().getAsString());
                    }

                    info.itemModel = null;

                    if (JsonUtils.hasField(armor, "item_texture")) {
                        JsonObject item_texture = JsonUtils.getJsonObject(armor, "item_texture");
                        if (JsonUtils.hasField(item_texture, "model")) {
                            info.itemModel = JsonSuitSetItemModelType.MODEL;
                            info.itemModelInfo = JsonUtils.getString(item_texture, "model");
                        } else if (JsonUtils.hasField(item_texture, "blockstate")) {
                            info.itemModel = JsonSuitSetItemModelType.BLOCKSTATE;
                            info.itemModelInfo = JsonUtils.getString(item_texture, "blockstate");
                        }
                    }

                    this.slotInfos.put(slot, info);
                }
            }
        }

        this.abilityGenerators = JsonUtils.hasField(jsonobject, "abilities") ? Ability.parseAbilityGenerators(jsonobject.get("abilities")) : new ArrayList<>();

        if (JsonUtils.hasField(jsonobject, "effects")) {
            JsonArray array = JsonUtils.getJsonArray(jsonobject, "effects");
            for (int i = 0; i < array.size(); i++) {
                JsonObject obj = array.get(i).getAsJsonObject();
                this.effects.add(EffectHandler.makeEffect(obj));
            }
        }

        if (JsonUtils.hasField(jsonobject, "data")) {
            String s = JsonUtils.getJsonObject(jsonobject, "data").toString();
            this.data = JsonToNBT.getTagFromJson(s);
        }

        return this;
    }

    public static class JsonSuitSetSlotInfo {

        public boolean openable;
        public EnumAbilityBarColor barColor;
        public boolean glow;
        public float armor_model_scale;
        public ITextComponent name;
        public Map<String, String> textures;
        public JsonSuitSetItemModelType itemModel;
        public String itemModelInfo;

    }

    public enum JsonSuitSetItemModelType {

        MODEL, BLOCKSTATE;

    }

}
