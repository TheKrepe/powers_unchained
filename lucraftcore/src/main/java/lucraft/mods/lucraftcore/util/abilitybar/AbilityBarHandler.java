package lucraft.mods.lucraftcore.util.abilitybar;

import com.google.common.collect.ImmutableList;
import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.superpowers.render.SuperpowerRenderLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class AbilityBarHandler {

    private static final List<IAbilityBarProvider> ENTRIES = new ArrayList<>();
    public static int INDEX = 0;
    public static final int ENTRY_SHOW_AMOUNT = 5;

    public static void registerProvider(IAbilityBarProvider provider) {
        if (!ENTRIES.contains(provider))
            ENTRIES.add(provider);
    }

    public List<IAbilityBarProvider> getProviders() {
        return ImmutableList.copyOf(ENTRIES);
    }

    public static List<IAbilityBarEntry> getActiveEntries() {
        List<IAbilityBarEntry> list = new ArrayList<>();
        for (IAbilityBarProvider providers : ENTRIES) {
            for (IAbilityBarEntry entry : providers.getEntries()) {
                if (entry.isActive()) {
                    list.add(entry);
                }
            }
        }
        return list;
    }

    public static List<IAbilityBarEntry> getCurrentDisplayedEntries(List<IAbilityBarEntry> active) {
        List<IAbilityBarEntry> list = new ArrayList<>();

        if (active.isEmpty())
            return list;

        if (INDEX >= active.size())
            INDEX = 0;
        else if (INDEX < 0)
            INDEX = active.size() - 1;

        list.add(active.get(INDEX));

        int i = INDEX + 1;
        int added = 1;
        while (list.size() < ENTRY_SHOW_AMOUNT && added < active.size()) {
            if (i >= active.size())
                i = 0;
            list.add(active.get(i));
            i++;
            added++;
        }

        return list;
    }

    public static IAbilityBarEntry getEntryFromKey(int key) {
        if (key > ENTRY_SHOW_AMOUNT)
            return null;

        List<IAbilityBarEntry> list = getCurrentDisplayedEntries(getActiveEntries());

        if (key < 0 || key >= list.size())
            return null;

        return list.get(key);
    }

    public static void scroll(boolean up) {
        if (up)
            INDEX++;
        else
            INDEX--;
    }

    @Mod.EventBusSubscriber(modid = LucraftCore.MODID, value = Side.CLIENT)
    public static class Renderer {

        public static ResourceLocation HUD_TEX = new ResourceLocation(LucraftCore.MODID, "textures/gui/ability_bar.png");
        public static Minecraft mc = Minecraft.getMinecraft();

        @SubscribeEvent
        public static void onRenderGameOverlay(RenderGameOverlayEvent e) {
            if (e.isCancelable() || LCConfig.abilityBar == AbilityBarPos.DISABLED || e.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE || mc.gameSettings.showDebugInfo || ENTRIES.isEmpty()) {
                return;
            }

            int y = 12;
            List<IAbilityBarEntry> all = getActiveEntries();
            List<IAbilityBarEntry> current = getCurrentDisplayedEntries(all);
            Tessellator tes = Tessellator.getInstance();
            BufferBuilder bb = tes.getBuffer();
            boolean names = mc.ingameGUI.getChatGUI().getChatOpen();
            ScaledResolution res = e.getResolution();
            boolean right = LCConfig.abilityBar == AbilityBarPos.RIGHT;

            GlStateManager.pushMatrix();
            if (right)
                GlStateManager.translate(res.getScaledWidth() - 22, 0, 0);

            GlStateManager.enableRescaleNormal();

            int k = 0;
            for (IAbilityBarEntry a : current) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                mc.renderEngine.bindTexture(HUD_TEX);
                EnumAbilityBarColor barColor = a.getColor();
                mc.ingameGUI.drawTexturedModalRect(0, y, barColor == null ? 0 : barColor.getX(), barColor == null ? 0 : barColor.getY(), 22, 22);
                RenderHelper.enableGUIStandardItemLighting();
                GlStateManager.disableTexture2D();
                GlStateManager.disableCull();
                mc.renderEngine.bindTexture(SuperpowerRenderLayer.WHITE_TEX);
                GlStateManager.color(0.2F, 0.2F, 0.2F, 0.5F);
                boolean showKey = names || (a.showKey() && !names);

                String infoString = names ? a.getDescription() : GameSettings.getKeyDisplayString(AbilityBarKeys.KEYS.get(k).getKeyCode());
                k++;
                int infoLength = mc.fontRenderer.getStringWidth(infoString);

                if (showKey) {
                    if (right) {
                        bb.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
                        bb.pos(0, y + 6, 0).endVertex();
                        bb.pos(-2 - infoLength - 6, y + 6, 0).endVertex();
                        bb.pos(-2 - infoLength - 6, y + 17, 0).endVertex();
                        bb.pos(0, y + 17, 0).endVertex();
                        tes.draw();
                    } else {
                        bb.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
                        bb.pos(22, y + 6, 0).endVertex();
                        bb.pos(24 + infoLength + 6, y + 6, 0).endVertex();
                        bb.pos(24 + infoLength + 6, y + 17, 0).endVertex();
                        bb.pos(22, y + 17, 0).endVertex();
                        tes.draw();
                    }
                }

                if (a.renderCooldown()) {
                    Vec3d color = a.getCooldownColor();
                    float percentage = a.getCooldownPercentage();

                    GlStateManager.color(0, 0, 0);
                    bb.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
                    bb.pos(4, y + 16, 50).endVertex();
                    bb.pos(4 + 14, y + 16, 50).endVertex();
                    bb.pos(4 + 14, y + 18, 50).endVertex();
                    bb.pos(4, y + 18, 50).endVertex();
                    tes.draw();

                    GlStateManager.color((float) color.x, (float) color.y, (float) color.z, 1);
                    bb.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
                    bb.pos(4, y + 16, 50).endVertex();
                    bb.pos(4 + percentage * 14, y + 16, 50).endVertex();
                    bb.pos(4 + percentage * 14, y + 17, 50).endVertex();
                    bb.pos(4, y + 17, 50).endVertex();
                    tes.draw();
                }

                GlStateManager.enableTexture2D();
                GlStateManager.popMatrix();

                GlStateManager.translate(0, 0, 50);
                GlStateManager.color(1, 1, 1, 1);
                a.drawIcon(mc, mc.ingameGUI, 3, y + 3);
                GlStateManager.translate(0, 0, -50);

                if (showKey)
                    mc.ingameGUI.drawString(mc.fontRenderer, infoString, right ? -5 - infoLength : 27, y + 8, 0xfefefe);

                y += 21;
            }

            if (all.size() > ENTRY_SHOW_AMOUNT) {
                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableTexture2D();
                GlStateManager.disableCull();
                mc.renderEngine.bindTexture(SuperpowerRenderLayer.WHITE_TEX);
                GlStateManager.color(0.2F, 0.2F, 0.2F, 0.5F);
                boolean showUp = AbilityBarKeys.UP.getKeyCode() != 0 || names;
                boolean showDown = AbilityBarKeys.DOWN.getKeyCode() != 0 || names;
                String key = AbilityBarKeys.UP.getKeyCode() == 0 && LCConfig.abilityBarScrolling ? "SHIFT + SCROLL" : GameSettings.getKeyDisplayString(AbilityBarKeys.UP.getKeyCode());
                String key2 = AbilityBarKeys.DOWN.getKeyCode() == 0 && LCConfig.abilityBarScrolling ? "SHIFT + SCROLL" : GameSettings.getKeyDisplayString(AbilityBarKeys.DOWN.getKeyCode());
                int infoLength = mc.fontRenderer.getStringWidth(key);
                int infoLength2 = mc.fontRenderer.getStringWidth(key2);

                if (right) {
                    if (showUp) {
                        bb.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
                        bb.pos(11, 2, -100).endVertex();
                        bb.pos(2 - infoLength - 6, 2, -100).endVertex();
                        bb.pos(2 - infoLength - 6, 11, -100).endVertex();
                        bb.pos(4, 11, -100).endVertex();
                        tes.draw();
                    }

                    if (showDown) {
                        bb.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
                        bb.pos(4, y + 2, -100).endVertex();
                        bb.pos(2 - infoLength2 - 6, y + 2, -100).endVertex();
                        bb.pos(2 - infoLength2 - 6, y + 11, -100).endVertex();
                        bb.pos(11, y + 11, -100).endVertex();
                        tes.draw();
                    }
                } else {
                    if (showUp) {
                        bb.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
                        bb.pos(11, 2, -100).endVertex();
                        bb.pos(20 + infoLength + 6, 2, -100).endVertex();
                        bb.pos(20 + infoLength + 6, 11, -100).endVertex();
                        bb.pos(18, 11, -100).endVertex();
                        tes.draw();
                    }

                    if (showDown) {
                        bb.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
                        bb.pos(18, y + 2, -100).endVertex();
                        bb.pos(20 + infoLength2 + 6, y + 2, -100).endVertex();
                        bb.pos(20 + infoLength2 + 6, y + 11, -100).endVertex();
                        bb.pos(11, y + 11, -100).endVertex();
                        tes.draw();
                    }
                }

                GlStateManager.enableTexture2D();

                if (showUp)
                    mc.ingameGUI.drawString(mc.fontRenderer, key, right ? -1 - infoLength : 23, 3, 0xfefefe);
                if (showDown)
                    mc.ingameGUI.drawString(mc.fontRenderer, key2, right ? -1 - infoLength2 : 23, y + 3, 0xfefefe);
                mc.renderEngine.bindTexture(HUD_TEX);
                GlStateManager.color(1, 1, 1, 1);
                mc.ingameGUI.drawTexturedModalRect(4, 2, 40, 0, 14, 9);
                mc.ingameGUI.drawTexturedModalRect(4, y + 2, 54, 0, 14, 9);
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();
            }

            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }

    }

}
