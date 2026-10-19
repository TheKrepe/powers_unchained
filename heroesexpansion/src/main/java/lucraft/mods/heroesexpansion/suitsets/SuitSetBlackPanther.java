package lucraft.mods.heroesexpansion.suitsets;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.abilities.AbilityKineticEnergyAbsorption;
import lucraft.mods.heroesexpansion.items.ItemBlackPantherSuit;
import lucraft.mods.lucraftcore.superpowers.abilities.*;
import lucraft.mods.lucraftcore.superpowers.abilities.predicates.AbilityConditionOpenArmor;
import lucraft.mods.lucraftcore.superpowers.capabilities.CapabilitySuperpower;
import lucraft.mods.lucraftcore.superpowers.effects.Effect;
import lucraft.mods.lucraftcore.superpowers.effects.EffectConditionOpenArmor;
import lucraft.mods.lucraftcore.superpowers.effects.EffectNameChange;
import lucraft.mods.lucraftcore.superpowers.models.ModelBipedSuitSet;
import lucraft.mods.lucraftcore.superpowers.suitsets.ItemSuitSetArmor;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class SuitSetBlackPanther extends HESuitSet {

    public static final int MAX_TIMER = 20;
    public NBTTagCompound data = new NBTTagCompound();
    public List<Effect> effects = new ArrayList<>();

    public SuitSetBlackPanther(String name) {
        super(name);

        NBTTagCompound color1 = new NBTTagCompound();
        color1.setInteger("red", 191);
        color1.setInteger("green", 0);
        color1.setInteger("blue", 153);
        this.data.setTag("transition_color_1", color1);

        NBTTagCompound color2 = new NBTTagCompound();
        color2.setInteger("red", 255);
        color2.setInteger("green", 34);
        color2.setInteger("blue", 210);
        this.data.setTag("transition_color_2", color2);

        EffectNameChange nameChange = new EffectNameChange();
        EffectConditionOpenArmor openArmor = new EffectConditionOpenArmor();
        openArmor.slot = EntityEquipmentSlot.CHEST;
        nameChange.conditions.add(openArmor);
        nameChange.name = new TextComponentString("???");
        this.effects.add(nameChange);
    }

    @Override
    public NBTTagCompound getData() {
        return data;
    }

    @Override
    public List<Effect> getEffects() {
        return effects;
    }

    @Override
    public boolean canOpenArmor(EntityEquipmentSlot slot) {
        return true;
    }

    @Override
    public ItemArmor.ArmorMaterial getArmorMaterial(EntityEquipmentSlot slot) {
        return ItemArmor.ArmorMaterial.DIAMOND;
    }

    @Override
    public String getArmorTexturePath(ItemStack stack, Entity entity, EntityEquipmentSlot slot, boolean light, boolean smallArms, boolean open) {
        String tex = slot == EntityEquipmentSlot.HEAD ? "helmet" : slot == EntityEquipmentSlot.CHEST ? "chestplate" : slot == EntityEquipmentSlot.LEGS ? "legs" : "boots";

        if (slot == EntityEquipmentSlot.CHEST && smallArms)
            tex = tex + "_smallarms";
        if (light)
            tex = tex + "_lights";

        return getModId() + ":textures/models/armor/" + this.getRegistryName().getPath() + "/" + tex + ".png";
    }

    @Override
    public boolean hasGlowyThings(EntityLivingBase entity, EntityEquipmentSlot slot) {
        return true;
    }

    @Override
    public void onEquip(SuitSet suitSet, EntityLivingBase player) {
        ((ItemBlackPantherSuit) player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem()).setArmorOpen(player, player.getItemStackFromSlot(EntityEquipmentSlot.CHEST), false);
        if (!player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).hasTagCompound())
            player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).setTagCompound(new NBTTagCompound());
        player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getTagCompound().setInteger("OpenTimer", MAX_TIMER);
    }

    @Override
    public void registerItems(RegistryEvent.Register<Item> e) {
        e.getRegistry().register(chestplate = createItem(this, EntityEquipmentSlot.CHEST));
    }

    @Override
    public ItemSuitSetArmor createItem(SuitSet suitSet, EntityEquipmentSlot slot) {
        if (slot == EntityEquipmentSlot.CHEST)
            return (ItemSuitSetArmor) new ItemBlackPantherSuit(suitSet, EntityEquipmentSlot.CHEST).setRegistryName(suitSet.getRegistryName().getNamespace(), suitSet.getRegistryName().getPath() + "_suit");
        return super.createItem(suitSet, slot);
    }

    @Override
    public float getGlowOpacity(SuitSet suitSet, EntityLivingBase entity, EntityEquipmentSlot slot) {
        if (!(entity instanceof EntityPlayer) || !entity.hasCapability(CapabilitySuperpower.SUPERPOWER_CAP, null)) {
            return 0F;
        }
        List<Ability> abilities = new ArrayList<>(Ability.getAbilityContainer(Ability.EnumAbilityContext.SUIT, entity).getAbilities());
        if (abilities.size() == 0)
            return 0F;
        AbilityKineticEnergyAbsorption ab = Ability.getAbilityFromClass(abilities, AbilityKineticEnergyAbsorption.class);
        if (ab == null)
            return 0F;
        return ab.getPercentage();
    }

    public UUID uuid = UUID.fromString("c7c0fca6-8065-4120-8532-294d478678df");

    @Override
    public Ability.AbilityMap addDefaultAbilities(EntityLivingBase entity, Ability.AbilityMap abilities, Ability.EnumAbilityContext context) {
        abilities.put("kinetic_absorption", new AbilityKineticEnergyAbsorption(entity).setDataValue(AbilityKineticEnergyAbsorption.MAX_ENERGY, 100).setDataValue(AbilityKineticEnergyAbsorption.COLOR, new Color(172f / 255f, 104f / 255f, 218f / 255f)).addCondition(new AbilityConditionOpenArmor(EntityEquipmentSlot.CHEST)));
        abilities.put("knockback_resistance", new AbilityKnockbackResistance(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 3f).addCondition(new AbilityConditionOpenArmor(EntityEquipmentSlot.CHEST)));
        abilities.put("fall_resistance", new AbilityFallResistance(entity).setDataValue(AbilityAttributeModifier.OPERATION, 1).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 0.05f).addCondition(new AbilityConditionOpenArmor(EntityEquipmentSlot.CHEST)));
        abilities.put("punch", new AbilityPunch(entity).setDataValue(AbilityAttributeModifier.UUID, uuid).setDataValue(AbilityAttributeModifier.AMOUNT, 2f).addCondition(new AbilityConditionOpenArmor(EntityEquipmentSlot.CHEST)));
        return super.addDefaultAbilities(entity, abilities, context);
    }

    @Override
    public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
        for (int i : OreDictionary.getOreIDs(repair)) {
            String ore = OreDictionary.getOreName(i);
            if (ore.equalsIgnoreCase("plateVibranium")) {
                return true;
            }
        }

        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ModelBiped getArmorModel(SuitSet suitSet, ItemStack stack, Entity entity, EntityEquipmentSlot slot, boolean light, boolean smallArms, boolean open) {
        float headScale = 0F;
        try {
            headScale = suitSet.getArmorModelScale(EntityEquipmentSlot.HEAD);
        } catch (Exception e) {
            headScale = this.getArmorModelScale(slot);
        }
        ModelBipedSuitSet model = (ModelBipedSuitSet) super.getArmorModel(suitSet, stack, entity, slot, light, smallArms, open);
        model.bipedHead = new ModelRenderer(model, 0, 0);
        model.bipedHead.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, headScale);
        model.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
        model.bipedHeadwear = new ModelRenderer(model, 32, 0);
        model.bipedHeadwear.addBox(-4.0F, -8.0F, -4.0F, 8, 8, 8, headScale + 0.5F);
        model.bipedHeadwear.setRotationPoint(0.0F, 0.0F, 0.0F);
        return model;
    }

    public static Map<String, Object> SUIT_TEXTURES = new HashMap<>();

    @SideOnly(Side.CLIENT)
    @Override
    public void bindArmorTexture(SuitSet suitSet, Entity entity, float f, float f1, float f2, float f3, float f4, float f5, ResourceLocation normalTex, ResourceLocation glowTex, boolean glow, EntityEquipmentSlot slot, boolean smallArms) {
        if (!(entity instanceof EntityLivingBase)) {
            super.bindArmorTexture(suitSet, entity, f, f1, f2, f3, f4, f5, normalTex, glowTex, glow, slot, smallArms);
            return;
        }

        ItemStack stack = ((EntityLivingBase) entity).getItemStackFromSlot(slot);
        int timer = stack.hasTagCompound() ? MAX_TIMER - stack.getTagCompound().getInteger("OpenTimer") : MAX_TIMER;
        String key = suitSet.getRegistryName().toString() + (glow ? "_glow" : "_normal") + (smallArms ? "_smallarms" : "") + "_" + timer;
        ResourceLocation necklaceOverlay = suitSet.getData() != null && suitSet.getData().hasKey("necklace_texture") ? new ResourceLocation(suitSet.getData().getString("necklace_texture")) : new ResourceLocation(HeroesExpansion.MODID, "textures/models/armor/black_panther/necklace.png");

        if (timer == 0) {
            Minecraft.getMinecraft().renderEngine.bindTexture(necklaceOverlay);
            return;
        } else if (timer == MAX_TIMER) {
            super.bindArmorTexture(suitSet, entity, f, f1, f2, f3, f4, f5, normalTex, glowTex, glow, slot, smallArms);
            return;
        }

        try {
            DynamicTexture t = null;
            if (SUIT_TEXTURES.containsKey(key))
                t = (DynamicTexture) SUIT_TEXTURES.get(key);
            else {
                BufferedImage image = TextureUtil.readBufferedImage(Minecraft.getMinecraft().getResourceManager().getResource(glow ? glowTex : normalTex).getInputStream());
                BufferedImage overlay = TextureUtil.readBufferedImage(Minecraft.getMinecraft().getResourceManager().getResource(necklaceOverlay).getInputStream());

                // Helmet
                float helmetProgress = (float) MathHelper.clamp(timer, 0, MAX_TIMER / 2) / (MAX_TIMER / 2F);
                animY(image, 0, 17, 64, 8, helmetProgress, helmetProgress != 1F, suitSet);
                animY(image, 8, 0, 16, 8, helmetProgress == 1F ? 1F : 0F, false, null);
                animY(image, 40, 0, 48, 8, helmetProgress == 1F ? 1F : 0F, false, null);
                animY(image, 16, 0, 24, 8, helmetProgress > 0F ? 1F : 0F, false, null);
                animY(image, 48, 0, 56, 8, helmetProgress > 0F ? 1F : 0F, false, null);

                // Chest
                float chestProgress = (float) MathHelper.clamp(timer, 0, MAX_TIMER / 2) / (MAX_TIMER / 2F);
                animY(image, 16, 20, 40, 32, chestProgress, suitSet);
                animY(image, 16, 36, 40, 48, chestProgress, suitSet);
                animY(image, 28, 16, 36, 20, chestProgress == 1F ? 1F : 0F, false, null);
                animY(image, 28, 32, 36, 36, chestProgress == 1F ? 1F : 0F, false, null);
                animY(image, 20, 16, 28, 20, chestProgress > 0F ? 1F : 0F, false, null);
                animY(image, 20, 32, 28, 36, chestProgress > 0F ? 1F : 0F, false, null);

                // Arms
                animY(image, 40, 20, 56, 32, chestProgress, suitSet);
                animY(image, 40, 36, 56, 48, chestProgress, suitSet);
                animY(image, 32, 52, 48, 64, chestProgress, suitSet);
                animY(image, 48, 52, 64, 64, chestProgress, suitSet);
                animY(image, 44, 16, smallArms ? 47 : 48, 20, chestProgress > 0F ? 1F : 0F, false, null);
                animY(image, 44, 32, smallArms ? 47 : 48, 36, chestProgress > 0F ? 1F : 0F, false, null);
                animY(image, smallArms ? 47 : 48, 16, 52, 20, chestProgress == 1F ? 1F : 0F, false, null);
                animY(image, smallArms ? 47 : 48, 32, 52, 36, chestProgress == 1F ? 1F : 0F, false, null);

                animY(image, 36, 48, smallArms ? 39 : 40, 52, chestProgress > 0F ? 1F : 0F, false, null);
                animY(image, 52, 48, smallArms ? 55 : 56, 52, chestProgress > 0F ? 1F : 0F, false, null);
                animY(image, smallArms ? 39 : 40, 48, 44, 52, chestProgress == 1F ? 1F : 0F, false, null);
                animY(image, smallArms ? 55 : 56, 48, 60, 52, chestProgress == 1F ? 1F : 0F, false, null);

                // Legs
                float legsProgress = (float) MathHelper.clamp(timer - (MAX_TIMER / 2), 0, MAX_TIMER / 2) / (MAX_TIMER / 2F);
                animY(image, 0, 20, 16, 32, legsProgress, legsProgress != 0F, suitSet);
                animY(image, 0, 36, 16, 48, legsProgress, legsProgress != 0F, suitSet);
                animY(image, 0, 52, 16, 64, legsProgress, legsProgress != 0F, suitSet);
                animY(image, 16, 52, 32, 64, legsProgress, legsProgress != 0F, suitSet);
                animY(image, 4, 16, 8, 20, legsProgress > 0F ? 1F : 0F, false, null);
                animY(image, 4, 32, 8, 36, legsProgress > 0F ? 1F : 0F, false, null);
                animY(image, 8, 16, 12, 20, legsProgress == 1F ? 1F : 0F, false, null);
                animY(image, 8, 32, 12, 36, legsProgress == 1F ? 1F : 0F, false, null);

                animY(image, 20, 48, 24, 52, legsProgress > 0F ? 1F : 0F, false, null);
                animY(image, 4, 48, 8, 52, legsProgress > 0F ? 1F : 0F, false, null);
                animY(image, 24, 48, 28, 52, legsProgress == 1F ? 1F : 0F, false, null);
                animY(image, 8, 48, 16, 52, legsProgress == 1F ? 1F : 0F, false, null);

                for (int x = 0; x < 64; x++) {
                    for (int y = 0; y < 64; y++) {
                        int rgb = overlay.getRGB(x, y);
                        if (rgb != 0) {
                            image.setRGB(x, y, rgb);
                        }
                    }
                }

                t = new DynamicTexture(image);
                SUIT_TEXTURES.put(key, t);
            }
            GlStateManager.bindTexture(t.getGlTextureId());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SideOnly(Side.CLIENT)
    public void animX(BufferedImage image, int startX, int startY, int endX, int endY, float progress) {
        for (int x = Math.min(startX, endX); x < Math.max(startX, endX); x++) {
            for (int y = Math.min(startY, endY); y < Math.max(startY, endY); y++) {
                int i = image.getRGB(x, y);
                if (i != 0) {
                    boolean v = startX < endX ? (y < startX + progress * (endX - startX)) : (y >= startX + progress * (endX - startX));
                    String rgb = Integer.toHexString(i).substring(2);
                    String newRgb = (v ? "ff" : "00") + rgb;
                    image.setRGB(x, y, (int) Long.parseLong(newRgb, 16));
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public void animY(BufferedImage image, int startX, int startY, int endX, int endY, float progress, SuitSet suitSet) {
        animY(image, startX, startY, endX, endY, progress, true, suitSet);
    }

    @SideOnly(Side.CLIENT)
    public void animY(BufferedImage image, int startX, int startY, int endX, int endY, float progress, boolean trans, SuitSet suitSet) {
        for (int x = Math.min(startX, endX); x < Math.max(startX, endX); x++) {
            for (int y = Math.min(startY, endY); y < Math.max(startY, endY); y++) {
                int i = image.getRGB(x, y);
                if (i != 0 && Integer.toHexString(i).length() > 2) {
                    boolean v = startY < endY ? (y < startY + progress * (endY - startY)) : (y >= startY + progress * (endY - startY));
                    String rgb = Integer.toHexString(i).substring(2);
                    String newRgb = (v ? "ff" : "00") + rgb;
                    image.setRGB(x, y, (int) Long.parseLong(newRgb, 16));
                }

                if (trans && y == (int) (startY + progress * (endY - startY)) && suitSet.getData() != null && suitSet.getData().hasKey("transition_color_1") && suitSet.getData().hasKey("transition_color_2")) {
                    Random rand = new Random();
                    Vec3i color1 = new Vec3i(suitSet.getData().getCompoundTag("transition_color_1").getInteger("red"), suitSet.getData().getCompoundTag("transition_color_1").getInteger("green"), suitSet.getData().getCompoundTag("transition_color_1").getInteger("blue"));
                    Vec3i color2 = new Vec3i(suitSet.getData().getCompoundTag("transition_color_2").getInteger("red"), suitSet.getData().getCompoundTag("transition_color_2").getInteger("green"), suitSet.getData().getCompoundTag("transition_color_2").getInteger("blue"));
                    String red = Integer.toHexString(Math.min(color1.getX(), color2.getX()) + rand.nextInt(Math.max(color1.getX(), color2.getX()) - Math.min(color1.getX(), color2.getX())));
                    String green = Integer.toHexString(Math.min(color1.getY(), color2.getY()) + rand.nextInt(Math.max(color1.getY(), color2.getY()) - Math.min(color1.getY(), color2.getY())));
                    String blue = Integer.toHexString(Math.min(color1.getZ(), color2.getZ()) + rand.nextInt(Math.max(color1.getZ(), color2.getZ()) - Math.min(color1.getZ(), color2.getZ())));
                    image.setRGB(x, y, (int) Long.parseLong("ff" + red + green + blue, 16));
                }
            }
        }
    }

}
