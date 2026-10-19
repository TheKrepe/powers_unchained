package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.entities.EntitySound;
import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityConstant;
import lucraft.mods.lucraftcore.util.helper.LCMathHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class AbilityBlindness extends AbilityConstant {

    public AbilityBlindness(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        HEIconHelper.drawIcon(mc, gui, x, y, 1, 9);
    }

    @Override
    public void updateTick() {
        if (entity.isPotionActive(MobEffects.BLINDNESS))
            entity.removeActivePotionEffect(MobEffects.BLINDNESS);
        entity.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 205));
    }

    @SideOnly(Side.CLIENT)
    @Mod.EventBusSubscriber(modid = HeroesExpansion.MODID, value = Side.CLIENT)
    public static class Renderer {

        public static ResourceLocation SHADER = new ResourceLocation(HeroesExpansion.MODID, "shaders/post/blindness.json");
        public static int transitionStage = 0;
        public static int maxTransitionStage = 60;
        public static boolean blind = false;

        @SubscribeEvent
        public static void onTick(TickEvent.ClientTickEvent e) {
            Minecraft mc = Minecraft.getMinecraft();

            if (mc == null || mc.player == null || e.phase == TickEvent.Phase.START || mc.isGamePaused())
                return;

            if (blind && transitionStage < maxTransitionStage)
                transitionStage++;
            else if (!blind && transitionStage > 0)
                transitionStage--;

            if (transitionStage == maxTransitionStage / 2) {
                if (blind)
                    mc.entityRenderer.loadShader(SHADER);
                else
                    mc.entityRenderer.stopUseShader();
            }

            if (renderBlindness() && (mc.entityRenderer.getShaderGroup() == null || !mc.entityRenderer.getShaderGroup().getShaderGroupName().equals(SHADER.toString()))) {
                mc.entityRenderer.loadShader(SHADER);
            } else if (!renderBlindness() && mc.entityRenderer.getShaderGroup() != null && mc.entityRenderer.getShaderGroup().getShaderGroupName().equals(SHADER.toString())) {
                mc.entityRenderer.stopUseShader();
            }

            List<AbilityBlindness> blindnessList = Ability.getAbilitiesFromClass(Ability.getAbilities(mc.player), AbilityBlindness.class);

            if (blindnessList.size() <= 0 && mc.entityRenderer.getShaderGroup() != null && mc.entityRenderer.getShaderGroup().getShaderGroupName().equals(SHADER.toString())) {
                blind = false;
            } else if (blindnessList.size() > 0) {
                for (AbilityBlindness blindness : blindnessList) {
                    if (blindness != null && blindness.isUnlocked()) {
                        blind = true;
                        return;
                    }
                }
            }
        }

        @SubscribeEvent
        public static void onSound(PlaySoundEvent e) {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.world != null && mc.player != null && !mc.isGamePaused() && renderBlindness()) {
                EntitySound sound = new EntitySound(Minecraft.getMinecraft().world, e.getSound());
                mc.world.spawnEntity(sound);
            }
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        public static void onRenderHUDPre(RenderGameOverlayEvent.Pre e) {
            if (e.getType() == RenderGameOverlayEvent.ElementType.HELMET && blind) {
                e.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onRenderHUD(RenderGameOverlayEvent e) {
            if (e.isCancelable() || e.getType() != RenderGameOverlayEvent.ElementType.VIGNETTE) {
                return;
            }

            double opacity = LCMathHelper.round(MathHelper.sin((float) (((float) transitionStage / (float) maxTransitionStage) * Math.PI)), 2);

            if (opacity == 0F)
                return;

            Tessellator tes = Tessellator.getInstance();
            BufferBuilder bb = tes.getBuffer();
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
            GlStateManager.disableTexture2D();
            GlStateManager.disableCull();
            GlStateManager.color(0, 0, 0, (float) opacity);

            bb.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
            bb.pos(0, 0, 0).endVertex();
            bb.pos(e.getResolution().getScaledWidth(), 0, 0).endVertex();
            bb.pos(e.getResolution().getScaledWidth(), e.getResolution().getScaledHeight(), 0).endVertex();
            bb.pos(0, e.getResolution().getScaledHeight(), 0).endVertex();
            tes.draw();

            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

        public static boolean renderBlindness() {
            return transitionStage >= maxTransitionStage / 2;
        }

    }

}
