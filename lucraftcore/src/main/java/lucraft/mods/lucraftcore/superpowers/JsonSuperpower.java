package lucraft.mods.lucraftcore.superpowers;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityGenerator;
import lucraft.mods.lucraftcore.superpowers.effects.Effect;
import lucraft.mods.lucraftcore.superpowers.effects.EffectHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class JsonSuperpower extends Superpower {

    public JsonObject jsonOriginal;
    public ITextComponent name;
    public SuperpowerIconType iconType = null;
    public ItemStack iconStack = null;
    public ResourceLocation iconLoc = null;
    public int maxLevel;
    public int capsuleColor;
    public List<AbilityGenerator> abilityGenerators;
    public List<Effect> effects;
    public NBTTagCompound data;

    public JsonSuperpower(String name) {
        super(name);
    }

    @Override
    public String getDisplayName() {
        return name.getFormattedText();
    }

    @Override
    public boolean canLevelUp() {
        return maxLevel > 0;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public int getCapsuleColor() {
        return capsuleColor;
    }

    @Override
    public List<Effect> getEffects() {
        return effects;
    }

    @Override
    public NBTTagCompound getData() {
        return data;
    }

    @Override
    public Ability.AbilityMap addDefaultAbilities(EntityLivingBase entity, Ability.AbilityMap abilities, Ability.EnumAbilityContext context) {
        for (AbilityGenerator ab : abilityGenerators) {
            Ability ability = ab.create(entity, abilities);
            if (ability != null)
                abilities.put(ab.key, ability);
        }
        return abilities;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderIcon(Minecraft mc, Gui gui, int x, int y) {
        if (iconType == SuperpowerIconType.ITEM) {
            float zLevel = Minecraft.getMinecraft().getRenderItem().zLevel;
            Minecraft.getMinecraft().getRenderItem().zLevel = -100.5F;
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, 0);
            GlStateManager.scale(2, 2, 2);
            Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(iconStack, 0, 0);
            GlStateManager.popMatrix();
            Minecraft.getMinecraft().getRenderItem().zLevel = zLevel;
        } else if (iconType == SuperpowerIconType.TEXTURE) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, 0);
            GlStateManager.scale(0.125F, 0.125F, 1);
            GlStateManager.color(1, 1, 1, 1);
            Minecraft.getMinecraft().renderEngine.bindTexture(iconLoc);
            gui.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
            GlStateManager.popMatrix();
        }
    }

    public JsonSuperpower deserialize(JsonObject jsonobject) throws Exception {
        this.name = ITextComponent.Serializer.jsonToComponent(JsonUtils.getJsonObject(jsonobject, "name").toString());

        this.maxLevel = JsonUtils.getInt(jsonobject, "max_level", 0);

        this.capsuleColor = JsonUtils.getInt(jsonobject, "capsule_color", 15073794);

        if (JsonUtils.hasField(jsonobject, "icon")) {
            JsonObject icon = JsonUtils.getJsonObject(jsonobject, "icon");
            String type = JsonUtils.getString(icon, "type");
            this.iconType = type.equalsIgnoreCase("item") ? SuperpowerIconType.ITEM : (type.equalsIgnoreCase("texture") ? SuperpowerIconType.TEXTURE : null);

            if (this.iconType == SuperpowerIconType.ITEM) {
                this.iconStack = deserializeIcon(JsonUtils.getJsonObject(icon, "item"));
            } else if (this.iconType == SuperpowerIconType.TEXTURE) {
                this.iconLoc = new ResourceLocation(JsonUtils.getString(icon, "texture"));
            }
        }

        this.abilityGenerators = JsonUtils.hasField(jsonobject, "abilities") ? Ability.parseAbilityGenerators(jsonobject.get("abilities")) : new ArrayList<>();

        this.effects = new ArrayList<>();
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

    private static ItemStack deserializeIcon(JsonObject object) {
        if (!object.has("item")) {
            throw new JsonSyntaxException("Unsupported icon type, currently only items are supported (add 'item' key)");
        } else {
            Item item = JsonUtils.getItem(object, "item");
            int i = JsonUtils.getInt(object, "data", 0);
            ItemStack ret = new ItemStack(item, 1, i);
            ret.setTagCompound(net.minecraftforge.common.util.JsonUtils.readNBT(object, "nbt"));
            return ret;
        }
    }

    public static enum SuperpowerIconType {

        ITEM, TEXTURE;

    }

}
