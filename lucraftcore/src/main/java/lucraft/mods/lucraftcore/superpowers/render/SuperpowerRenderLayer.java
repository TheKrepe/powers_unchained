package lucraft.mods.lucraftcore.superpowers.render;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.effects.*;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import lucraft.mods.lucraftcore.util.render.ModelCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;

import java.awt.*;
import java.util.List;
import java.util.Random;

public class SuperpowerRenderLayer implements LayerRenderer<EntityPlayer> {

    public RenderPlayer renderer;
    public static Minecraft mc = Minecraft.getMinecraft();
    public static final ResourceLocation WHITE_TEX = new ResourceLocation(LucraftCore.MODID, "textures/models/white.png");

    public SuperpowerRenderLayer(RenderPlayer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(EntityPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        if (player.getActivePotionEffect(MobEffects.INVISIBILITY) != null)
            return;

        Superpower superpower = SuperpowerHandler.getSuperpower(player);
        if (superpower != null && superpower.getPlayerRenderer() != null)
            superpower.getPlayerRenderer()
                    .onRenderPlayer(renderer, mc, player, superpower, Ability.getAbilityContainer(Ability.EnumAbilityContext.SUPERPOWER, player), limbSwing, limbSwingAmount,
                            partialTicks, ageInTicks, netHeadYaw, headPitch, scale);

        // Effect: Skin Overlay
        for (EffectSkinOverlay effects : EffectHandler.getEffectsByClass(player, EffectSkinOverlay.class)) {
            if (EffectHandler.canEffectBeDisplayed(effects, player)) {
                GlStateManager.pushMatrix();
                GlStateManager.color(1, 1, 1, 1);
                ModelPlayer model = (ModelPlayer) ModelCache.getOrStoreModel("model_player_" + effects.size + "_" + PlayerHelper.hasSmallArms(player), () -> new ModelPlayer(effects.size, PlayerHelper.hasSmallArms(player)));
                model.setModelAttributes(renderer.getMainModel());
                Minecraft.getMinecraft().renderEngine.bindTexture(effects.texture);
                if (effects.glow) {
                    GlStateManager.disableLighting();
                    GlStateManager.enableBlend();
                    LCRenderHelper.setLightmapTextureCoords(240, 240);
                }
                SuperpowerRenderer.overrideSkin = false;
                model.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                SuperpowerRenderer.overrideSkin = true;
                if (effects.glow) {
                    GlStateManager.enableLighting();
                    GlStateManager.disableBlend();
                    LCRenderHelper.restoreLightmapTextureCoords();
                }
                GlStateManager.popMatrix();
            }
        }

        // Effect: Glow
        mc.renderEngine.bindTexture(WHITE_TEX);
        for (EffectGlow glow : EffectHandler.getEffectsByClass(player, EffectGlow.class)) {
            if (EffectHandler.canEffectBeDisplayed(glow, player)) {
                GlStateManager.pushMatrix();
                ModelPlayer model = (ModelPlayer) ModelCache.getOrStoreModel("model_player_" + glow.size + "_" + PlayerHelper.hasSmallArms(player), () -> new ModelPlayer(glow.size, PlayerHelper.hasSmallArms(player)));
                model.isChild = false;
                model.setModelAttributes(renderer.getMainModel());
                LCRenderHelper.setLightmapTextureCoords(240, 240);
                GlStateManager.disableLighting();
                GlStateManager.enableBlend();
                GlStateManager.color((float) glow.color.getRed() / 255F, (float) glow.color.getGreen() / 255F, (float) glow.color.getBlue() / 255F, glow.opacity);
                SuperpowerRenderer.overrideSkin = false;
                model.render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                SuperpowerRenderer.overrideSkin = true;
                LCRenderHelper.restoreLightmapTextureCoords();
                GlStateManager.enableLighting();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }
        }

        // Effect: Vibrating
        if (EffectVibrating.isVibrating(player) && !SuitSet.hasSuitSetOn(player)) {
            mc.renderEngine.bindTexture(renderer.getEntityTexture((AbstractClientPlayer) player));
            for (int i = 0; i < 10; i++) {
                GlStateManager.pushMatrix();
                Random rand = new Random();
                GlStateManager.translate((rand.nextFloat() - 0.5F) / 15, 0, (rand.nextFloat() - 0.5F) / 15);
                GlStateManager.color(1, 1, 1, 0.3F);
                this.renderer.getMainModel().render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
                GlStateManager.popMatrix();
            }
        }

        // Effect: Flickering
        List<EffectFlickering> flickerings = EffectHandler.getEffectsByClass(player, EffectFlickering.class);
        for (int i = 0; i < flickerings.size(); i++) {
            EffectFlickering flicker = flickerings.get(i);
            LCRenderHelper.setupRenderLightning();
            GlStateManager.translate(0, -0.2F, 0);
            if (EffectHandler.canEffectBeDisplayed(flicker, player)) {
                AxisAlignedBB box = new AxisAlignedBB(-player.width / 2D, 0, -player.width / 2D, player.width / 2D, player.height, player.width / 2D);
                float thickness = 0.001F;
                LCRenderHelper.drawRandomLightningCoordsInAABB(thickness, flickerings.get(i).getColor(player), box, new Random(i + player.ticksExisted / 2));
            }
            LCRenderHelper.finishRenderLightning();
        }

        // Effect: Glowing Hands
        for (EffectGlowingHand effects : EffectHandler.getEffectsByClass(player, EffectGlowingHand.class)) {
            if (EffectHandler.canEffectBeDisplayed(effects, player)) {
                Color color = effects.color;
                Minecraft mc = Minecraft.getMinecraft();
                Random rand = new Random(2);
                float f = 0.2F;

                for (int j = 0; j < 2; j++) {
                    LCRenderHelper.setupRenderLightning();
                    if (j == 0)
                        this.renderer.getMainModel().bipedRightArm.postRender(scale);
                    else
                        this.renderer.getMainModel().bipedLeftArm.postRender(scale);
                    GlStateManager.translate(0, 0.5F, 0);
                    GlStateManager.scale(effects.size, effects.size, effects.size);
                    GlStateManager.rotate((mc.player.ticksExisted + LCRenderHelper.renderTick) / 2F, 0, 1, 0);

                    for (int i = 0; i < 30; i++) {
                        GlStateManager.rotate((mc.player.ticksExisted + LCRenderHelper.renderTick) * i / 70F, 1, 1, 0);
                        LCRenderHelper.drawGlowingLine(new Vec3d((-f / 2F) + rand.nextFloat() * f, (-f / 2F) + rand.nextFloat() * f, (-f / 2F) + rand.nextFloat() * f), new Vec3d((-f / 2F) + rand.nextFloat() * f, (-f / 2F) + rand.nextFloat() * f, (-f / 2F) + rand.nextFloat() * f), 0.1F, color, 0);
                    }

                    LCRenderHelper.finishRenderLightning();
                }
            }
        }

        MinecraftForge.EVENT_BUS.post(new RenderSuperpowerLayerEvent(player, this.renderer, limbSwing, limbSwingAmount, partialTicks, ageInTicks, netHeadYaw, headPitch, scale));
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }

}
