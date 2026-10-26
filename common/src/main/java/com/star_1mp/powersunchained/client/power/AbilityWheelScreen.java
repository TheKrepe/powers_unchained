package com.star_1mp.powersunchained.client.power;

import com.mojang.blaze3d.platform.InputConstants;
import com.star_1mp.powersunchained.Constants;
import com.star_1mp.powersunchained.client.input.PUKeyMappings;
import com.star_1mp.powersunchained.network.PowerTogglePayload;
import com.star_1mp.powersunchained.platform.Services;
import com.star_1mp.powersunchained.power.PowerData;
import com.star_1mp.powersunchained.power.PowerAbility;
import com.star_1mp.powersunchained.power.Superpowers;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;

/**
 * The ability wheel (hold its ability key; let go to close): a ring split into one segment per
 * switchable superpower, with its icon. A segment is lit blue while the power is on and grey while it is off; click
 * one to switch it. The hovered power's name and state show under the wheel.
 */
public class AbilityWheelScreen extends Screen {

    private static final float INNER = 30.0F;
    private static final float OUTER = 84.0F;
    /** Gap between two segments (GUI pixels). */
    private static final float GAP = 3.0F;
    private static final float BORDER = 1.0F;

    private static final int OFF_FILL = 0xC8383D46;
    private static final int OFF_HOVER = 0xD8575E6A;
    private static final int OFF_BORDER = 0xFF7D8694;
    private static final int ON_FILL = 0xC81E74C8;
    private static final int ON_HOVER = 0xD83A93EA;
    private static final int ON_BORDER = 0xFF8CD2FF;
    private static final int CENTER_FILL = 0xB0101418;

    /** The key held to keep the wheel open (null: stays open until closed). */
    private final net.minecraft.client.@org.jspecify.annotations.Nullable KeyMapping heldKey;
    private int hovered = -1;
    /** Cached wheel rectangles (real pixels): x0, y0, x1, y1, colour. */
    private List<int[]> rects = List.of();
    private String rectsKey = "";
    private int openTicks;
    private int debugHovered = -1;

    public AbilityWheelScreen(net.minecraft.client.@org.jspecify.annotations.Nullable KeyMapping heldKey) {
        super(Component.translatable("screen.powers_unchained.ability_wheel"));
        this.heldKey = heldKey;
    }

    /** The switchable powers the local player owns, in wheel order. */
    public static List<PowerAbility> powers(LocalPlayer player) {
        PowerData data = Superpowers.data(player);
        List<PowerAbility> list = new ArrayList<>();
        for (PowerAbility p : PowerAbility.values()) {
            if (p.toggleable() && data.has(p)) {
                list.add(p);
            }
        }
        return list;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        this.openTicks++;
        if (this.minecraft.player == null || powers(this.minecraft.player).isEmpty()) {
            this.onClose();
            return;
        }
        if (this.heldKey != null) {
            InputConstants.Key key = InputConstants.getKey(this.heldKey.saveString());
            if (key.getType() == InputConstants.Type.KEYBOARD && !InputConstants.isKeyDown(key.getValue())) {
                this.onClose();
            }
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // the world stays visible: a soft dim only
        graphics.fill(0, 0, this.width, this.height, 0x38000000);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float a) {
        LocalPlayer player = this.minecraft.player;
        if (player == null) {
            return;
        }
        List<PowerAbility> powers = powers(player);
        int n = powers.size();
        if (n == 0) {
            return;
        }
        PowerData data = Superpowers.data(player);
        float cx = this.width / 2.0F;
        float cy = this.height / 2.0F;
        this.hovered = this.debugHovered >= 0 ? this.debugHovered : this.segmentAt(mouseX, mouseY, n);

        // ring (scale-in when it opens)
        float open = Mth.clamp((this.openTicks + a) / 3.0F, 0.0F, 1.0F);
        float grow = 0.85F + 0.15F * (1.0F - (1.0F - open) * (1.0F - open));
        float guiScale = (float) this.minecraft.getWindow().getGuiScale();
        StringBuilder key = new StringBuilder().append(this.width).append('x').append(this.height).append('@').append(guiScale)
                .append('h').append(this.hovered).append('g').append(Math.round(grow * 100));
        for (PowerAbility p : powers) {
            key.append(data.isActive(p) ? '1' : '0');
        }
        if (!key.toString().equals(this.rectsKey)) {
            this.rectsKey = key.toString();
            this.rects = this.buildRects(powers, data, cx, cy, guiScale, grow);
        }
        g.pose().pushMatrix();
        g.pose().scale(1.0F / guiScale);
        for (int[] r : this.rects) {
            g.fill(r[0], r[1], r[2], r[3], r[4]);
        }
        g.pose().popMatrix();

        // icons
        for (int i = 0; i < n; i++) {
            float ang = segmentAngle(i, n) * Mth.DEG_TO_RAD;
            float rad = (INNER + OUTER) / 2.0F * grow;
            int ix = Math.round(cx + Mth.cos(ang) * rad) - 16;
            int iy = Math.round(cy + Mth.sin(ang) * rad) - 16;
            int color = data.isActive(powers.get(i)) ? 0xFFFFFFFF : 0xFFB4BAC4;
            g.blitSprite(RenderPipelines.GUI_TEXTURED, icon(powers.get(i)), ix, iy, 32, 32, color);
        }

        // centre: the hovered power's state as a dot
        if (this.hovered >= 0) {
            PowerAbility p = powers.get(this.hovered);
            boolean on = data.isActive(p);
            Component state = Component.translatable(on ? "screen.powers_unchained.ability_wheel.on" : "screen.powers_unchained.ability_wheel.off");
            g.centeredText(this.font, p.title(), (int) cx, (int) (cy + OUTER * grow + 8), 0xFFFFFFFF);
            g.centeredText(this.font, state, (int) cx, (int) (cy + OUTER * grow + 20), on ? 0xFF8CD2FF : 0xFFA0A7B2);
            g.centeredText(this.font, Component.literal(on ? "ON" : "OFF"), (int) cx, (int) cy - 4, on ? 0xFF8CD2FF : 0xFFA0A7B2);
        } else {
            g.centeredText(this.font, this.title, (int) cx, (int) (cy + OUTER * grow + 8), 0xFFE0E0E0);
        }
        super.extractRenderState(g, mouseX, mouseY, a);
    }

    private static Identifier icon(PowerAbility p) {
        return Constants.id("power/" + p.id());
    }

    /** Centre angle of segment {@code i} of {@code n} (degrees, 0 = right, clockwise on screen; the first at the top). */
    private static float segmentAngle(int i, int n) {
        return -90.0F + i * 360.0F / n;
    }

    private int segmentAt(double mx, double my, int n) {
        double dx = mx - this.width / 2.0;
        double dy = my - this.height / 2.0;
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
    private List<int[]> buildRects(List<PowerAbility> powers, PowerData data, float cx, float cy, float s, float grow) {
        int n = powers.size();
        float step = 360.0F / n;
        float inner = INNER * grow * s;
        float outer = OUTER * grow * s;
        float gap = GAP * 0.5F * s;
        float border = BORDER * s;
        float centerR = (INNER - 5.0F) * grow * s;
        float pcx = cx * s;
        float pcy = cy * s;
        int[] fill = new int[n];
        int[] edge = new int[n];
        for (int i = 0; i < n; i++) {
            boolean on = data.isActive(powers.get(i));
            boolean hover = i == this.hovered;
            fill[i] = on ? (hover ? ON_HOVER : ON_FILL) : (hover ? OFF_HOVER : OFF_FILL);
            edge[i] = on ? ON_BORDER : OFF_BORDER;
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
                        color = CENTER_FILL;
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

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && this.minecraft.player != null) {
            List<PowerAbility> powers = powers(this.minecraft.player);
            int seg = this.segmentAt(event.x(), event.y(), powers.size());
            if (seg >= 0) {
                toggle(powers.get(seg));
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    /** Switches a power on / off (also used by the CI driver). */
    public static void toggle(PowerAbility power) {
        LocalPlayer player = net.minecraft.client.Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        boolean on = Superpowers.data(player).isActive(power);
        player.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 0.4F, on ? 0.8F : 1.2F);
        Services.GAMEPLAY.sendToServer(new PowerTogglePayload(power.id()));
    }

    /** Dev/CI only: hover a segment without a mouse. */
    public void debugHover(int segment) {
        this.debugHovered = segment;
    }
}
