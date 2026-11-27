package com.star_1mp.powersunleashed.client.power;

import com.star_1mp.powersunleashed.Constants;
import com.star_1mp.powersunleashed.client.ability.AbilitySlots;
import com.star_1mp.powersunleashed.network.PowerTogglePayload;
import com.star_1mp.powersunleashed.platform.Services;
import com.star_1mp.powersunleashed.power.PowerAbility;
import com.star_1mp.powersunleashed.power.PowerData;
import com.star_1mp.powersunleashed.power.Superpowers;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

/**
 * The ability wheel, drawn over the game while its ability key is held (no screen: the player keeps moving): a ring
 * split into one segment per switchable ability, with its icon — lit blue while on, grey while off. While it is open
 * the mouse no longer turns the camera but moves a pointer over the ring; left click switches the pointed ability.
 * Opens and closes with a smooth grow / fade.
 */
public final class AbilityWheel {

    private static final float INNER = 21.0F;
    private static final float OUTER = 58.0F;
    /** Gap between two segments (GUI pixels). */
    private static final float GAP = 2.0F;
    private static final float BORDER = 1.0F;
    /** Ticks to open / close. */
    private static final float OPEN_TICKS = 5.0F;
    /** How far the pointer goes from the centre (GUI pixels). */
    private static final float POINTER_RANGE = 48.0F;

    private static final int OFF_FILL = 0xC8383D46;
    private static final int OFF_HOVER = 0xD8575E6A;
    private static final int OFF_BORDER = 0xFF7D8694;
    private static final int ON_FILL = 0xC81E74C8;
    private static final int ON_HOVER = 0xD83A93EA;
    private static final int ON_BORDER = 0xFF8CD2FF;
    private static final int CENTER_FILL = 0xB0101418;

    private static boolean open;
    private static boolean debugOpen;
    private static float anim;
    private static float prevAnim;
    /** Pointer offset from the centre (GUI pixels). */
    private static float px;
    private static float py;
    private static int hovered = -1;
    private static int debugHovered = -1;
    /**
     * The ring is rasterised once per state (hover / on-off / size) into a texture and drawn as one quad: thousands of
     * fill quads every frame made the game lag.
     */
    private static final Identifier RING = Constants.id("dynamic/ability_wheel");
    private static net.minecraft.client.renderer.texture.@org.jspecify.annotations.Nullable DynamicTexture ringTexture;
    private static int ringSize;
    private static String ringKey = "";

    private AbilityWheel() {
    }

    /** The switchable abilities the local player has, in wheel order. */
    public static List<PowerAbility> powers(LocalPlayer player) {
        List<PowerAbility> list = new ArrayList<>();
        for (PowerAbility p : PowerAbility.all()) {
            if (p.toggleable() && Superpowers.has(player, p)) {
                list.add(p);
            }
        }
        return list;
    }

    public static boolean isOpen() {
        return open;
    }

    /** Dev/CI only: keeps the wheel open / points at a segment. */
    public static void debugOpen(boolean on, int segment) {
        debugOpen = on;
        debugHovered = segment;
    }

    /** Client tick: open while the key is held (and no screen is open). */
    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        boolean want = player != null && mc.gui.screen() == null && !powers(player).isEmpty()
                && (debugOpen || AbilitySlots.isDown(AbilitySlots.ABILITY_WHEEL));
        if (want && !open) {
            px = 0.0F;
            py = 0.0F;
        }
        open = want;
        prevAnim = anim;
        anim = Mth.approach(anim, open ? 1.0F : 0.0F, 1.0F / OPEN_TICKS);
    }

    /**
     * Mouse moved while the camera would turn (NeoForge turn event, before the turn): while open, the movement moves
     * the pointer instead.
     *
     * @return whether the camera must not turn
     */
    public static boolean onMouseTurn(double dx, double dy) {
        if (!open) {
            return false;
        }
        double scale = Minecraft.getInstance().getWindow().getGuiScale();
        px += (float) (dx / scale);
        py += (float) (dy / scale);
        float r = Mth.sqrt(px * px + py * py);
        if (r > POINTER_RANGE) {
            px *= POINTER_RANGE / r;
            py *= POINTER_RANGE / r;
        }
        return true;
    }

    /** Left click while open: switch the pointed ability. @return whether the click was used */
    public static boolean onClick() {
        LocalPlayer player = Minecraft.getInstance().player;
        if (!open || player == null) {
            return false;
        }
        List<PowerAbility> powers = powers(player);
        int seg = pointed(powers.size());
        if (seg >= 0) {
            toggle(powers.get(seg));
        }
        return true;
    }

    private static int pointed(int n) {
        if (debugHovered >= 0) {
            return debugHovered;
        }
        return Mth.sqrt(px * px + py * py) < INNER * 0.5F ? -1 : segmentAt(px, py, n);
    }

    private static float ease(float t) {
        t = Mth.clamp(t, 0.0F, 1.0F);
        return t * t * (3.0F - 2.0F * t);
    }

    private static int fade(int argb, float alpha) {
        int a = Math.round((argb >>> 24) * alpha);
        return a << 24 | argb & 0xFFFFFF;
    }

    public static void render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        float a = ease(Mth.lerp(delta.getGameTimeDeltaPartialTick(false), prevAnim, anim));
        if (player == null || a <= 0.0F) {
            return;
        }
        List<PowerAbility> powers = powers(player);
        int n = powers.size();
        if (n == 0) {
            return;
        }
        PowerData data = Superpowers.data(player);
        float cx = g.guiWidth() / 2.0F;
        float cy = g.guiHeight() / 2.0F;
        hovered = open ? pointed(n) : -1;
        float grow = 0.7F + 0.3F * a;
        // a soft dim behind it
        g.fill(0, 0, g.guiWidth(), g.guiHeight(), fade(0x38000000, a));

        float guiScale = (float) mc.getWindow().getGuiScale();
        updateRing(powers, data, guiScale);
        g.pose().pushMatrix();
        g.pose().translate(cx, cy);
        g.pose().scale(grow);
        g.pose().translate(-cx, -cy);
        if (ringTexture != null) {
            // one quad in real pixels, faded with the open animation
            g.pose().pushMatrix();
            g.pose().translate(cx, cy);
            g.pose().scale(1.0F / guiScale);
            int half = ringSize / 2;
            g.blit(RenderPipelines.GUI_TEXTURED, RING, -half, -half, 0.0F, 0.0F, ringSize, ringSize, ringSize, ringSize, ringSize, ringSize,
                    fade(0xFFFFFFFF, a));
            g.pose().popMatrix();
        }

        // icons
        for (int i = 0; i < n; i++) {
            float ang = segmentAngle(i, n) * Mth.DEG_TO_RAD;
            float rad = (INNER + OUTER) / 2.0F;
            int ix = Math.round(cx + Mth.cos(ang) * rad) - 9;
            int iy = Math.round(cy + Mth.sin(ang) * rad) - 9;
            int color = fade(data.isEnabled(powers.get(i)) ? 0xFFFFFFFF : 0xFFB4BAC4, a);
            g.blitSprite(RenderPipelines.GUI_TEXTURED, icon(powers.get(i)), ix, iy, 18, 18, color);
        }

        // texts: the pointed ability's name and state
        int textColor = fade(0xFFFFFFFF, a);
        if (hovered >= 0 && hovered < n) {
            PowerAbility p = powers.get(hovered);
            boolean on = data.isEnabled(p);
            Component state = Component.translatable(on ? "screen.powers_unleashed.ability_wheel.on" : "screen.powers_unleashed.ability_wheel.off");
            g.centeredText(mc.font, p.title(), (int) cx, (int) (cy + OUTER + 8), textColor);
            g.centeredText(mc.font, state, (int) cx, (int) (cy + OUTER + 20), fade(on ? 0xFF8CD2FF : 0xFFA0A7B2, a));
            g.centeredText(mc.font, Component.literal(on ? "ON" : "OFF"), (int) cx, (int) cy - 4, fade(on ? 0xFF8CD2FF : 0xFFA0A7B2, a));
        } else {
            g.centeredText(mc.font, Component.translatable("screen.powers_unleashed.ability_wheel"), (int) cx, (int) (cy + OUTER + 8),
                    fade(0xFFE0E0E0, a));
        }
        g.pose().popMatrix();

        // the pointer
        if (open && debugHovered < 0) {
            int x = Math.round(cx + px);
            int y = Math.round(cy + py);
            g.fill(x - 2, y - 2, x + 3, y + 3, fade(0xC0000000, a));
            g.fill(x - 1, y - 1, x + 2, y + 2, fade(0xFFFFFFFF, a));
        }
    }

    /** Re-rasterises the ring texture when the hovered segment, a state or the GUI scale changed. */
    private static void updateRing(List<PowerAbility> powers, PowerData data, float guiScale) {
        StringBuilder key = new StringBuilder().append(guiScale).append('h').append(hovered).append('n').append(powers.size());
        for (PowerAbility p : powers) {
            key.append(data.isEnabled(p) ? '1' : '0');
        }
        if (key.toString().equals(ringKey) && ringTexture != null) {
            return;
        }
        ringKey = key.toString();
        int size = Mth.ceil(OUTER * 2.0F * guiScale) + 4;
        size += size & 1;
        Minecraft mc = Minecraft.getInstance();
        if (ringTexture == null || ringSize != size) {
            if (ringTexture != null) {
                mc.getTextureManager().release(RING);
            }
            ringTexture = new net.minecraft.client.renderer.texture.DynamicTexture(() -> "powers_unleashed ability wheel", size, size, true);
            mc.getTextureManager().register(RING, ringTexture);
            ringSize = size;
        }
        com.mojang.blaze3d.platform.NativeImage image = ringTexture.getPixels();
        image.fillRect(0, 0, size, size, 0);
        float c = size / 2.0F / guiScale;
        for (int[] r : buildRects(powers, data, hovered, c, c, guiScale, 1.0F, 1.0F)) {
            int x0 = Math.max(0, r[0]);
            int x1 = Math.min(size, r[2]);
            if (r[1] >= 0 && r[1] < size && x1 > x0) {
                image.fillRect(x0, r[1], x1 - x0, 1, r[4]);
            }
        }
        ringTexture.upload();
    }

    private static Identifier icon(PowerAbility p) {
        return Constants.id("power/" + p.id());
    }

    /** Centre angle of segment {@code i} of {@code n} (degrees, 0 = right, clockwise on screen; the first at the top). */
    private static float segmentAngle(int i, int n) {
        return -90.0F + i * 360.0F / n;
    }

    private static int segmentAt(double dx, double dy, int n) {
        double r = Math.sqrt(dx * dx + dy * dy);
        if (r < INNER * 0.6 || r > OUTER + 24.0) {
            return -1;
        }
        return segmentOf((float) Math.toDegrees(Math.atan2(dy, dx)), n);
    }

    private static int segmentOf(float angleDeg, int n) {
        float step = 360.0F / n;
        float rel = angleDeg + 90.0F + step / 2.0F;
        rel = ((rel % 360.0F) + 360.0F) % 360.0F;
        return Math.min(n - 1, (int) (rel / step));
    }

    /** The ring rasterised in real pixels, as runs of equal colour per row. */
    private static List<int[]> buildRects(List<PowerAbility> powers, PowerData data, int hovered, float cx, float cy, float s, float grow, float alpha) {
        int n = powers.size();
        float step = 360.0F / n;
        float inner = INNER * grow * s;
        float outer = OUTER * grow * s;
        float gap = GAP * 0.5F * s;
        float border = BORDER * s;
        float centerR = (INNER - 3.0F) * grow * s;
        float pcx = cx * s;
        float pcy = cy * s;
        int[] fill = new int[n];
        int[] edge = new int[n];
        for (int i = 0; i < n; i++) {
            boolean on = data.isEnabled(powers.get(i));
            boolean hover = i == hovered;
            fill[i] = fade(on ? (hover ? ON_HOVER : ON_FILL) : (hover ? OFF_HOVER : OFF_FILL), alpha);
            edge[i] = fade(on ? ON_BORDER : OFF_BORDER, alpha);
        }
        List<int[]> out = new ArrayList<>();
        int y0 = (int) Math.floor(pcy - outer - 1);
        int y1 = (int) Math.ceil(pcy + outer + 1);
        int x0 = (int) Math.floor(pcx - outer - 1);
        int x1 = (int) Math.ceil(pcx + outer + 1);
        for (int y = y0; y < y1; y++) {
            int runColor = 0;
            int runStart = x0;
            for (int x = x0; x <= x1; x++) {
                int color = 0;
                if (x < x1) {
                    float dx = x + 0.5F - pcx;
                    float dy = y + 0.5F - pcy;
                    float r = (float) Math.sqrt(dx * dx + dy * dy);
                    if (r <= centerR) {
                        color = fade(CENTER_FILL, alpha);
                    } else if (r >= inner && r <= outer) {
                        float ang = (float) Math.toDegrees(Math.atan2(dy, dx));
                        int seg = n == 1 ? 0 : segmentOf(ang, n);
                        float boundary;
                        if (n == 1) {
                            boundary = Float.MAX_VALUE;
                        } else {
                            float rel = ang - (segmentAngle(seg, n) - step / 2.0F);
                            rel = ((rel % 360.0F) + 360.0F) % 360.0F;
                            float toEdge = Math.min(rel, step - rel);
                            boundary = r * Mth.sin(Math.min(toEdge, 90.0F) * Mth.DEG_TO_RAD);
                        }
                        if (boundary >= gap) {
                            boolean isBorder = r < inner + border || r > outer - border || boundary < gap + border;
                            color = isBorder ? edge[seg] : fill[seg];
                        }
                    }
                }
                if (color != runColor) {
                    if (runColor != 0) {
                        out.add(new int[] {runStart, y, x, y + 1, runColor});
                    }
                    runColor = color;
                    runStart = x;
                }
            }
        }
        return out;
    }

    /** Switches an ability on / off (also used by the CI driver). */
    public static void toggle(PowerAbility power) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        boolean on = Superpowers.data(player).isEnabled(power);
        player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4F, on ? 0.8F : 1.2F);
        Services.GAMEPLAY.sendToServer(new PowerTogglePayload(power.id()));
    }
}
