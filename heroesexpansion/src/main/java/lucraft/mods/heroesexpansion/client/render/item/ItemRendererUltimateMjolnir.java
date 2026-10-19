package lucraft.mods.heroesexpansion.client.render.item;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.abilities.AbilityGodMode;
import lucraft.mods.heroesexpansion.client.models.ModelUltimateMjolnir;
import lucraft.mods.heroesexpansion.enchantments.HEEnchantments;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.UUID;

public class ItemRendererUltimateMjolnir extends TileEntityItemStackRenderer {

    public static final ResourceLocation ULTIMATE_MJOLNIR_TEX = new ResourceLocation(HeroesExpansion.MODID, "textures/models/ultimate_mjolnir.png");
    public static final ResourceLocation ENCHANTED_ITEM_GLINT_RES = new ResourceLocation("textures/misc/enchanted_item_glint.png");
    public static final ModelUltimateMjolnir ULTIMATE_MJOLNIR_MODEL = new ModelUltimateMjolnir();
    public static final ModelUltimateMjolnir ULTIMATE_MJOLNIR_HEAD_MODEL = new ModelUltimateMjolnir(1F);

    static {
        ULTIMATE_MJOLNIR_HEAD_MODEL.disableHandle();
    }

    @Override
    public void renderByItem(ItemStack stack, float partialTicks) {
        GlStateManager.translate(0.53F, -0.5F, 0.5F);
        renderUltimateMjolnir(stack, partialTicks);
    }

    public static void renderUltimateMjolnir(ItemStack stack, float partialTicks) {
        Minecraft.getMinecraft().renderEngine.bindTexture(ULTIMATE_MJOLNIR_TEX);
        ULTIMATE_MJOLNIR_MODEL.renderModel(0.0625F);

        for (Enchantment enchantment : EnchantmentHelper.getEnchantments(stack).keySet()) {
            if (enchantment != HEEnchantments.WORTHINESS) {
                renderEnchantedGlint(Minecraft.getMinecraft().player, ULTIMATE_MJOLNIR_MODEL);
                break;
            }
        }

        renderLightning(stack, partialTicks, ULTIMATE_MJOLNIR_HEAD_MODEL);
    }

    public static void renderEnchantedGlint(EntityLivingBase entity, ModelUltimateMjolnir model) {
        float f = (float) entity.ticksExisted + LCRenderHelper.renderTick;
        Minecraft.getMinecraft().renderEngine.bindTexture(ENCHANTED_ITEM_GLINT_RES);
        GlStateManager.enableBlend();
        GlStateManager.depthFunc(514);
        GlStateManager.depthMask(false);
        GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);

        for (int i = 0; i < 2; ++i) {
            GlStateManager.disableLighting();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE);
            GlStateManager.color(0.38F, 0.19F, 0.608F, 1.0F);
            GlStateManager.matrixMode(5890);
            GlStateManager.loadIdentity();
            GlStateManager.scale(0.33333334F, 0.33333334F, 0.33333334F);
            GlStateManager.rotate(30.0F - (float) i * 60.0F, 0.0F, 0.0F, 1.0F);
            GlStateManager.translate(0.0F, f * (0.001F + (float) i * 0.003F) * 20.0F, 0.0F);
            GlStateManager.matrixMode(5888);
            model.renderModel(0.0625F);
        }

        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        GlStateManager.enableLighting();
        GlStateManager.depthMask(true);
        GlStateManager.depthFunc(515);
        GlStateManager.disableBlend();
    }

    public static void renderLightning(ItemStack stack, float partialTicks, ModelUltimateMjolnir model) {
        EntityPlayer entity = stack.hasTagCompound() && stack.getTagCompound().hasKey("Owner") ? Minecraft.getMinecraft().world.getPlayerEntityByUUID(UUID.fromString(stack.getTagCompound().getString("Owner"))) : null;

        if (entity == null)
            return;

        for (AbilityGodMode ab : Ability.getAbilitiesFromClass(Ability.getAbilities((EntityPlayer) entity), AbilityGodMode.class)) {
            if (ab != null && ab.isUnlocked() && ab.isEnabled()) {
                GlStateManager.disableLighting();
                LCRenderHelper.setLightmapTextureCoords(240, 240);
                boolean flag = false;
                GlStateManager.depthMask(!flag);
                Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation("textures/entity/creeper/creeper_armor.png"));
                GlStateManager.matrixMode(5890);
                GlStateManager.loadIdentity();
                float f = (Minecraft.getMinecraft().player.ticksExisted + partialTicks) / 2F;
                GlStateManager.translate(f * 0.01F, f * 0.01F, 0.0F);
                GlStateManager.matrixMode(5888);
                GlStateManager.enableBlend();
                GlStateManager.color(0.5F, 0.5F, 0.5F, 1.0F);
                GlStateManager.disableLighting();
                GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                Minecraft.getMinecraft().entityRenderer.setupFogColor(true);
                model.renderModel(0.0625F);
                Minecraft.getMinecraft().entityRenderer.setupFogColor(false);
                GlStateManager.matrixMode(5890);
                GlStateManager.loadIdentity();
                GlStateManager.matrixMode(5888);
                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.depthMask(flag);
                GlStateManager.enableLighting();
                LCRenderHelper.restoreLightmapTextureCoords();
                return;
            }
        }
    }

}
