package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityHeld;
import lucraft.mods.lucraftcore.util.helper.ItemHelper;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class AbilityXray extends AbilityHeld {

    public AbilityXray(EntityLivingBase entity) {
        super(entity);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        HEIconHelper.drawIcon(mc, gui, x, y, 2, 1);
    }

    @Override
    public void updateTick() {

    }

    @SideOnly(Side.CLIENT)
    @Mod.EventBusSubscriber(modid = HeroesExpansion.MODID, value = Side.CLIENT)
    public static class Renderer {

        public static ResourceLocation SHADER = new ResourceLocation("shaders/post/desaturate.json");
        public static boolean xray = false;

        @SubscribeEvent
        public static void onTick(TickEvent.ClientTickEvent e) {
            Minecraft mc = Minecraft.getMinecraft();

            if (mc.entityRenderer == null || mc.player == null || e.phase == TickEvent.Phase.START || mc.isGamePaused())
                return;

            xray = false;

            List<AbilityXray> xrays = Ability.getAbilitiesFromClass(Ability.getAbilities(mc.player), AbilityXray.class);
            for (AbilityXray xray : xrays) {
                if (xray != null && xray.isUnlocked() && xray.isEnabled()) {
                    Renderer.xray = true;
                    break;
                }
            }

            if (xray && (mc.entityRenderer.getShaderGroup() == null || !mc.entityRenderer.getShaderGroup().getShaderGroupName().equals(SHADER.toString())))
                mc.entityRenderer.loadShader(SHADER);
            else if (!xray && mc.entityRenderer.getShaderGroup() != null && mc.entityRenderer.getShaderGroup().getShaderGroupName().equals(SHADER.toString()))
                mc.entityRenderer.stopUseShader();
        }

        @SubscribeEvent
        public static void onRenderWorldLast(RenderWorldLastEvent e) {
            if (xray && Minecraft.getMinecraft().player != null) {
                int range = 10;
                EntityPlayer player = Minecraft.getMinecraft().player;
                GlStateManager.pushMatrix();
                Vec3d translate = new Vec3d(LCRenderHelper.interpolateValue(player.prevPosX, player.posX, e.getPartialTicks()), LCRenderHelper.interpolateValue(player.prevPosY, player.posY, e.getPartialTicks()), LCRenderHelper.interpolateValue(player.prevPosZ, player.posZ, e.getPartialTicks()));

                for (int x = 0; x < range; x++) {
                    for (int y = 0; y < range; y++) {
                        for (int z = 0; z < range; z++) {
                            BlockPos pos = new BlockPos(x - range / 2, y - range / 2, z - range / 2);
                            IBlockState state = player.world.getBlockState(player.getPosition().add(pos));
                            ItemStack stack = new ItemStack(state.getBlock());

                            if (!stack.isEmpty()) {
                                List<String> list = ItemHelper.getOreDictionaryEntries(stack);
                                boolean render = false;

                                for (String s : list) {
                                    if (s.startsWith("block") || s.startsWith("ore")) {
                                        render = !s.contains("Lead");
                                        break;
                                    }
                                }

                                if (render) {
                                    GlStateManager.pushMatrix();
                                    GlStateManager.disableLighting();
                                    GlStateManager.disableTexture2D();
                                    GlStateManager.disableDepth();
                                    GlStateManager.enableBlend();
                                    GlStateManager.color(1F, 1F, 1F, 1F);
                                    GlStateManager.blendFunc(770, 771);
                                    Vec3d blockPos = new Vec3d(player.getPosition().add(pos)).subtract(translate);
                                    RenderGlobal.renderFilledBox(blockPos.x, blockPos.y, blockPos.z, blockPos.x + 1, blockPos.y + 1, blockPos.z + 1, 1F, 1F, 1F, 0.5F);
                                    GlStateManager.blendFunc(771, 770);
                                    GlStateManager.disableBlend();
                                    GlStateManager.enableDepth();
                                    GlStateManager.enableTexture2D();
                                    GlStateManager.enableLighting();
                                    GlStateManager.popMatrix();
                                }
                            }
                        }
                    }
                }

                GlStateManager.popMatrix();
            }
        }
    }
}