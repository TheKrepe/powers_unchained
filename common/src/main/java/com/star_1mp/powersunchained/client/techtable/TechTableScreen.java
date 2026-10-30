package com.star_1mp.powersunchained.client.techtable;

import com.mojang.blaze3d.platform.InputConstants;
import com.star_1mp.powersunchained.Constants;
import com.star_1mp.powersunchained.entity.web.WebZipLine;
import com.star_1mp.powersunchained.item.WebShootersItem;
import com.star_1mp.powersunchained.menu.TechTableMenu;
import com.star_1mp.powersunchained.tech.GadgetSkins;
import com.star_1mp.powersunchained.tech.TechCost;
import com.star_1mp.powersunchained.tech.TechIngredient;
import com.star_1mp.powersunchained.tech.TechProgress;
import com.star_1mp.powersunchained.tech.TechRecipe;
import com.star_1mp.powersunchained.tech.TechRecipes;
import com.star_1mp.powersunchained.tech.WebModule;
import com.star_1mp.powersunchained.tech.WebModules;
import com.star_1mp.powersunchained.web.WebType;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Tech Table screen (416x236, drawn procedurally like the Suit Creator): tabs Creation (recipe list, a 3x3 grid on a
 * blueprint, the result), Upgrade (modules for the gadget: unlock on the right, drag from the top row down into the
 * 5 module slots to install, back up to remove; "before -> after" stats) and Customization (looks of the gadget on a
 * hologram, the gadget slot under it). The player's inventory is always at the bottom.
 */
public class TechTableScreen extends AbstractContainerScreen<TechTableMenu> {

    private static final int W = 416;
    private static final int H = 236;
    private static final int LIST_X = 8;
    private static final int LIST_Y = 21;
    private static final int LIST_W = 128;
    private static final int LIST_H = 207;
    private static final int CENTER_X = 140;
    private static final int CENTER_W = 162;
    private static final int BP_Y = 20;
    private static final int BP_H = 129;
    private static final int RIGHT_X = 306;
    private static final int RIGHT_W = 102;
    private static final int CARD_H = 22;

    private static final int LABEL = 0xFF404040;
    private static final int COPPER = 0xFFFF9A52;
    private static final int COPPER_DARK = 0xFF9C5426;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREY = 0xFF9AA3AD;
    private static final int DIM = 0xFF5C646E;
    private static final int GREEN = 0xFF7CFC6A;
    private static final int RED = 0xFFFF5A4A;
    private static final int GLASS = 0xFF12161B;
    private static final int CYAN = 0xFF59E4FF;
    private static final int CYAN_DIM = 0xFF2F8FA8;
    private static final int PAPER = 0xFF1D3F73;
    private static final int PAPER_L = 0xFF2B5592;
    private static final int PAPER_LL = 0xFF3D6DB0;
    private static final int INK = 0xFFE9F3FF;
    private static final int INK_D = 0xFF9FC2EE;

    private static final int[] TABS = {TechTableMenu.TAB_CREATE, TechTableMenu.TAB_UPGRADE, TechTableMenu.TAB_CUSTOMIZE};
    private static final String[] TAB_KEYS = {"tab.create", "tab.upgrade", "tab.customize"};

    private static int lastTab = TechTableMenu.TAB_CREATE;

    private @Nullable TechRecipe selectedRecipe;
    private @Nullable WebModule selectedModule;
    private int selectedSkin;
    private int listScroll;
    private float time;
    private float yaw = -25.0F;
    private boolean rotating;
    /** Module being dragged with the mouse, and whether it comes from the installed row. */
    private @Nullable WebModule dragging;
    private boolean dragFromInstalled;
    private int failFlash;
    /** Customization hologram: model (null = the player's own skin model) and running pose. */
    private @Nullable Boolean holoSlim;
    private boolean holoRun;

    public TechTableScreen(TechTableMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, W, H);
    }

    // =====================================================================================================
    // setup
    // =====================================================================================================

    @Override
    protected void init() {
        super.init();
        this.setTab(lastTab);
    }

    private void setTab(int tab) {
        lastTab = tab;
        this.menu.tab = tab;
        this.listScroll = 0;
        this.dragging = null;
        if (this.minecraft != null && this.minecraft.gameMode != null) {
            this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, tab);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.failFlash > 0) {
            this.failFlash--;
        }
    }

    private TechProgress progress() {
        return this.menu.progress();
    }

    private void click() {
        this.playSound(SoundEvents.UI_BUTTON_CLICK.value(), 1.0F);
    }

    private void playSound(SoundEvent sound, float pitch) {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
    }

    private void button(int id) {
        this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, id);
    }

    private static Component tr(String key, Object... args) {
        return Component.translatable("screen.powers_unchained.tech_table." + key, args);
    }

    private static Identifier sprite(String name) {
        return Constants.id(name);
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private String trim(String text, int width) {
        if (this.font.width(text) <= width) {
            return text;
        }
        while (!text.isEmpty() && this.font.width(text + "…") > width) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "…";
    }

    // =====================================================================================================
    // drawing helpers (same look as the Suit Creator)
    // =====================================================================================================

    private static void panel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x + 2, y + 2, x + w - 2, y + h - 2, 0xFFC6C6C6);
        g.fill(x + 2, y, x + w - 2, y + 1, 0xFF000000);
        g.fill(x + 2, y + h - 1, x + w - 2, y + h, 0xFF000000);
        g.fill(x, y + 2, x + 1, y + h - 2, 0xFF000000);
        g.fill(x + w - 1, y + 2, x + w, y + h - 2, 0xFF000000);
        g.fill(x + 1, y + 1, x + 2, y + 2, 0xFF000000);
        g.fill(x + w - 2, y + 1, x + w - 1, y + 2, 0xFF000000);
        g.fill(x + 1, y + h - 2, x + 2, y + h - 1, 0xFF000000);
        g.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, 0xFF000000);
        g.fill(x + 2, y + 1, x + w - 3, y + 3, 0xFFFFFFFF);
        g.fill(x + 1, y + 2, x + 3, y + h - 3, 0xFFFFFFFF);
        g.fill(x + 3, y + h - 3, x + w - 2, y + h - 1, 0xFF555555);
        g.fill(x + w - 3, y + 3, x + w - 1, y + h - 2, 0xFF555555);
    }

    private static void inset(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + h, color);
        g.fill(x, y, x + w - 1, y + 1, 0xFF373737);
        g.fill(x, y, x + 1, y + h - 1, 0xFF373737);
        g.fill(x + 1, y + h - 1, x + w, y + h, 0xFFFFFFFF);
        g.fill(x + w - 1, y + 1, x + w, y + h, 0xFFFFFFFF);
    }

    private static void monitor(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        inset(g, x, y, w, h, GLASS);
        for (int yy = y + 2; yy < y + h - 1; yy += 2) {
            g.fill(x + 1, yy, x + w - 1, yy + 1, 0x163C5A6E);
        }
        g.fill(x + 1, y + 1, x + w - 1, y + 2, 0x28FF9A52);
    }

    private static void outline(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y + 1, x + 1, y + h - 1, color);
        g.fill(x + w - 1, y + 1, x + w, y + h - 1, color);
    }

    private static void brackets(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        int l = 5;
        g.fill(x, y, x + l, y + 1, color);
        g.fill(x, y, x + 1, y + l, color);
        g.fill(x + w - l, y, x + w, y + 1, color);
        g.fill(x + w - 1, y, x + w, y + l, color);
        g.fill(x, y + h - 1, x + l, y + h, color);
        g.fill(x, y + h - l, x + 1, y + h, color);
        g.fill(x + w - l, y + h - 1, x + w, y + h, color);
        g.fill(x + w - 1, y + h - l, x + w, y + h, color);
    }

    /** Blueprint paper: deep blue, fine grid (every 4 px), stronger lines every 8, a white title block. */
    private void blueprint(GuiGraphicsExtractor g, int x, int y, int w, int h, Component title) {
        inset(g, x, y, w, h, PAPER);
        for (int gx = x + 4; gx < x + w - 1; gx += 4) {
            g.fill(gx, y + 1, gx + 1, y + h - 1, (gx - x) % 8 == 0 ? PAPER_LL : PAPER_L);
        }
        for (int gy = y + 4; gy < y + h - 1; gy += 4) {
            g.fill(x + 1, gy, x + w - 1, gy + 1, (gy - y) % 8 == 0 ? PAPER_LL : PAPER_L);
        }
        outline(g, x + 3, y + 3, w - 6, h - 6, INK_D);
        String t = this.trim(title.getString(), w - 16);
        int tw = this.font.width(t) + 8;
        g.fill(x + 3, y + 3, x + 3 + tw, y + 15, PAPER);
        outline(g, x + 3, y + 3, tw, 12, INK_D);
        g.text(this.font, t, x + 7, y + 5, INK, false);
    }

    /** A socket drawn on the blueprint (dark, white frame); {@code size} 18 for normal slots. */
    private static void socket(GuiGraphicsExtractor g, int x, int y, int size, boolean bright) {
        g.fill(x, y, x + size, y + size, 0xC80A1932);
        outline(g, x, y, size, size, bright ? INK : INK_D);
    }

    private static void dashed(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int color) {
        int n = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
        for (int i = 0; i <= n; i++) {
            if ((i / 2) % 2 == 0) {
                int px = x0 + Math.round((x1 - x0) * i / (float) Math.max(1, n));
                int py = y0 + Math.round((y1 - y0) * i / (float) Math.max(1, n));
                g.fill(px, py, px + 1, py + 1, color);
            }
        }
    }

    private void vanillaButton(GuiGraphicsExtractor g, int x, int y, int w, int h, Component text, boolean active, boolean hovered, boolean selected) {
        Identifier id = Identifier.withDefaultNamespace(!active ? "widget/button_disabled"
                : hovered || selected ? "widget/button_highlighted" : "widget/button");
        g.blitSprite(RenderPipelines.GUI_TEXTURED, id, x, y, w, h);
        int color = !active ? 0xFFA0A0A0 : selected ? 0xFFFFD9B8 : WHITE;
        g.centeredText(this.font, text, x + w / 2, y + (h - 8) / 2 + 1, color);
        if (selected) {
            g.fill(x + 3, y + h - 3, x + w - 3, y + h - 2, COPPER);
        }
    }

    private void groupHeader(GuiGraphicsExtractor g, int x, int y, int w, Component text, int count) {
        g.fill(x, y, x + w, y + 11, 0x12FFFFFF);
        g.text(this.font, text, x + 3, y + 2, COPPER, false);
        String c = String.valueOf(count);
        g.text(this.font, c, x + w - 3 - this.font.width(c), y + 2, GREY, false);
    }

    /** List card: icon (item or sprite), name, sub line; selected = copper frame. */
    private void card(GuiGraphicsExtractor g, int x, int y, int w, @Nullable ItemStack item, @Nullable Identifier icon, Component name,
            @Nullable Component sub, boolean selected, boolean hover, boolean locked) {
        g.fill(x, y, x + w, y + CARD_H - 2, selected ? 0x40FF9A52 : hover ? 0x20FFFFFF : 0x0CFFFFFF);
        if (selected) {
            outline(g, x, y, w, CARD_H - 2, COPPER);
        }
        inset(g, x + 2, y + 1, 18, 18, 0xFF2A3038);
        if (item != null) {
            g.item(item, x + 3, y + 2);
        } else if (icon != null) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, icon, x + 3, y + 2, 16, 16, locked ? 0xFF505860 : 0xFFFFFFFF);
        }
        int tw = w - 26 - (locked ? 10 : 0);
        g.text(this.font, this.trim(name.getString(), tw), x + 23, y + (sub == null ? 6 : 2), locked ? 0xFF8A929A : selected ? 0xFFFFD9B8 : WHITE, true);
        if (sub != null) {
            g.text(this.font, this.trim(sub.getString(), tw), x + 23, y + 11, locked ? DIM : GREY, false);
        }
        if (locked) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("suit_creator/lock"), x + w - 11, y + 5, 8, 9);
        }
    }

    /** Material rows: icon, have / need (green / red). @return the height used */
    private int costRows(GuiGraphicsExtractor g, int x, int y, int w, TechCost cost) {
        int yy = y;
        for (TechCost.Entry e : cost.entries()) {
            ItemStack stack = e.stack();
            g.item(stack, x, yy);
            int have = TechCost.count(this.minecraft.player.getInventory(), e.item().get());
            boolean ok = have >= e.count() || this.minecraft.player.hasInfiniteMaterials();
            String t = Math.min(have, 999) + "/" + e.count();
            g.text(this.font, t, x + w - this.font.width(t), yy + 4, ok ? GREEN : RED, true);
            g.text(this.font, this.trim(stack.getHoverName().getString(), w - 20 - this.font.width(t) - 4), x + 18, yy + 4, 0xFFDFE6EC, false);
            yy += 17;
        }
        return yy - y;
    }

    private int wrapped(GuiGraphicsExtractor g, Component text, int x, int y, int w, int color, int maxLines) {
        List<FormattedCharSequence> lines = this.font.split(text, w);
        int n = 0;
        for (FormattedCharSequence line : lines) {
            if (n >= maxLines) {
                break;
            }
            g.text(this.font, line, x, y + n * 10, color, false);
            n++;
        }
        return n * 10;
    }

    // =====================================================================================================
    // rendering
    // =====================================================================================================

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        this.time = (this.minecraft.player != null ? this.minecraft.player.tickCount : 0) + partialTick;
        int x = this.leftPos;
        int y = this.topPos;
        panel(g, x, y, W, H);
        this.drawTopBar(g, x, y, mouseX, mouseY);
        monitor(g, x + LIST_X, y + LIST_Y, LIST_W, LIST_H);
        switch (this.menu.tab) {
            case TechTableMenu.TAB_UPGRADE -> this.drawUpgrade(g, x, y, mouseX, mouseY);
            case TechTableMenu.TAB_CUSTOMIZE -> this.drawCustomize(g, x, y, mouseX, mouseY);
            default -> this.drawCreate(g, x, y, mouseX, mouseY);
        }
        // the player's inventory
        for (int i = 0; i < 36; i++) {
            Slot slot = this.menu.slots.get(i);
            g.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("container/slot"), x + slot.x - 1, y + slot.y - 1, 18, 18);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        // drawn in the background
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        if (this.dragging != null) {
            g.nextStratum();
            g.blitSprite(RenderPipelines.GUI_TEXTURED, this.dragging.sprite(), mouseX - 8, mouseY - 8, 16, 16);
        } else if (this.hoveredSlot == null && this.menu.getCarried().isEmpty()) {
            this.tooltips(g, mouseX, mouseY);
        }
    }

    private int[] tabBounds(int i) {
        int titleEnd = this.leftPos + 8 + this.font.width(this.title) + 8;
        int total = -2;
        for (String key : TAB_KEYS) {
            total += this.font.width(tr(key)) + 14;
        }
        int x = Math.max(this.leftPos + (W - total) / 2, titleEnd);
        for (int t = 0; t < i; t++) {
            x += this.font.width(tr(TAB_KEYS[t])) + 14;
        }
        return new int[]{x, this.topPos + 4, this.font.width(tr(TAB_KEYS[i])) + 12, 14};
    }

    private void drawTopBar(GuiGraphicsExtractor g, int x, int y, int mx, int my) {
        g.text(this.font, this.title, x + 8, y + 7, LABEL, false);
        for (int i = 0; i < TABS.length; i++) {
            int[] b = this.tabBounds(i);
            this.vanillaButton(g, b[0], b[1], b[2], b[3], tr(TAB_KEYS[i]), true, in(mx, my, b[0], b[1], b[2], b[3]), this.menu.tab == TABS[i]);
        }
        // unlocked modules
        int n = (int) WebModule.all().stream().filter(this.progress()::hasModule).count();
        String count = n + "/" + WebModule.values().length;
        int bw = this.font.width(count) + 18;
        int bx = x + W - 8 - bw;
        inset(g, bx, y + 4, bw, 14, 0xFF1D2128);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("tech_table/module/tight_spring"), bx + 3, y + 5, 12, 12);
        g.text(this.font, count, bx + 16, y + 7, COPPER, false);
    }

    // ---- Creation ----

    private record ListRow(@Nullable String group, @Nullable TechRecipe recipe) {
    }

    private List<ListRow> createRows() {
        List<ListRow> rows = new ArrayList<>();
        String group = null;
        for (TechRecipe r : TechRecipes.all()) {
            if (!r.group().equals(group)) {
                group = r.group();
                rows.add(new ListRow(group, null));
            }
            rows.add(new ListRow(null, r));
        }
        return rows;
    }

    private static @Nullable Component recipeSub(TechRecipe r) {
        String key = "tech_recipe.powers_unchained." + r.id() + ".sub";
        return Language.getInstance().has(key) ? Component.translatable(key) : null;
    }

    private static Component recipeName(TechRecipe r) {
        return r.resultStack().getHoverName();
    }

    private void drawCreate(GuiGraphicsExtractor g, int x, int y, int mx, int my) {
        // list
        int lx = x + LIST_X + 2;
        int ly = y + LIST_Y + 3 - this.listScroll;
        g.enableScissor(x + LIST_X + 1, y + LIST_Y + 1, x + LIST_X + LIST_W - 1, y + LIST_Y + LIST_H - 1);
        for (ListRow row : this.createRows()) {
            if (row.group() != null) {
                long n = TechRecipes.all().stream().filter(r -> r.group().equals(row.group())).count();
                this.groupHeader(g, lx, ly, LIST_W - 4, Component.translatable("tech_group.powers_unchained." + row.group()), (int) n);
                ly += 13;
            } else {
                TechRecipe r = row.recipe();
                boolean hover = in(mx, my, lx, ly, LIST_W - 4, CARD_H - 2) && in(mx, my, x + LIST_X, y + LIST_Y, LIST_W, LIST_H);
                this.card(g, lx, ly, LIST_W - 4, r.resultStack(), null, recipeName(r), recipeSub(r), r == this.selectedRecipe, hover, false);
                ly += CARD_H;
            }
        }
        g.disableScissor();

        // blueprint with the grid
        int bx = x + CENTER_X;
        int by = y + BP_Y;
        TechRecipe shown = this.currentRecipe();
        this.blueprint(g, bx, by, CENTER_W, BP_H, tr("blueprint", shown != null ? recipeName(shown) : tr("workbench")));
        int gx = x + TechTableMenu.GRID_X;
        int gy = y + TechTableMenu.GRID_Y;
        for (int i = 0; i < 9; i++) {
            socket(g, gx + (i % 3) * 22 - 1, gy + (i / 3) * 22 - 1, 18, !this.menu.gridItems().get(i).isEmpty());
        }
        // ghost of the picked recipe in the empty cells
        if (this.selectedRecipe != null) {
            List<@Nullable TechIngredient> layout = this.selectedRecipe.layout();
            for (int i = 0; i < 9; i++) {
                TechIngredient ing = layout.get(i);
                if (ing != null && this.menu.gridItems().get(i).isEmpty()) {
                    int sx = gx + (i % 3) * 22;
                    int sy = gy + (i / 3) * 22;
                    g.item(this.cycle(ing), sx, sy);
                    g.fill(sx, sy, sx + 16, sy + 16, this.has(ing) ? 0x901D3F73 : 0x90731D1D);
                }
            }
        }
        // the arrow over to the result
        int ay = gy + 30;
        dashed(g, gx + 70, ay, bx + CENTER_W - 8, ay, INK_D);
        for (int i = 0; i < 4; i++) {
            g.fill(bx + CENTER_W - 8 - i, ay - i, bx + CENTER_W - 7 - i, ay + i + 1, INK);
        }
        if (shown == null) {
            g.text(this.font, tr("grid_hint"), bx + 8, by + BP_H - 14, INK_D, false);
        }

        // result
        int rx = x + RIGHT_X;
        monitor(g, rx, y + BP_Y, RIGHT_W, BP_H);
        g.centeredText(this.font, tr("result"), rx + RIGHT_W / 2, y + BP_Y + 5, COPPER);
        int fx = x + TechTableMenu.RESULT_X - 5;
        int fy = y + TechTableMenu.RESULT_Y - 5;
        boolean ready = !this.menu.slots.get(TechTableMenu.RESULT_SLOT).getItem().isEmpty();
        inset(g, fx, fy, 26, 26, 0xFF1A2027);
        float pulse = 0.6F + 0.4F * Mth.sin(this.time * 0.2F);
        outline(g, fx - 1, fy - 1, 28, 28, ready ? ((int) (pulse * 255) << 24) | 0xFF9A52 : 0x40FF9A52);
        if (!ready && shown != null) {
            g.item(shown.resultStack(), x + TechTableMenu.RESULT_X, y + TechTableMenu.RESULT_Y);
            g.fill(fx + 1, fy + 1, fx + 25, fy + 25, 0xA012161B);
        }
        if (shown != null) {
            int ty = fy + 32;
            ty += this.wrapped(g, recipeName(shown), rx + 5, ty, RIGHT_W - 10, WHITE, 2);
            String key = "tech_recipe.powers_unchained." + shown.id() + ".desc";
            if (Language.getInstance().has(key)) {
                this.wrapped(g, Component.translatable(key), rx + 5, ty + 2, RIGHT_W - 10, GREY, 5);
            }
        } else {
            this.wrapped(g, tr("pick_recipe"), rx + 5, fy + 32, RIGHT_W - 10, GREY, 6);
        }
        // what the picked recipe needs (have / need in the inventory + grid), two columns
        if (this.selectedRecipe != null) {
            int ny = y + 153;
            g.text(this.font, tr("needs"), rx, ny, LABEL, false);
            List<ItemStack> needs = this.needs(this.selectedRecipe);
            for (int i = 0; i < Math.min(6, needs.size()); i++) {
                ItemStack need = needs.get(i);
                int nx = rx + (i % 2) * 52;
                int nyy = ny + 10 + (i / 2) * 18;
                g.item(need, nx, nyy);
                int have = this.countEverywhere(need);
                String t = Math.min(have, 99) + "/" + need.getCount();
                g.text(this.font, t, nx + 18, nyy + 4, have >= need.getCount() ? 0xFF2E8B2E : 0xFFB03A2E, false);
            }
        }
    }

    /** The recipe shown on the blueprint: the one the grid makes, else the picked one. */
    private @Nullable TechRecipe currentRecipe() {
        TechRecipe made = TechRecipes.find(this.menu.gridItems());
        return made != null ? made : this.selectedRecipe;
    }

    /** Display stack of an ingredient (cycles through its options). */
    private ItemStack cycle(TechIngredient ing) {
        List<ItemStack> list = ing.display().get();
        return list.isEmpty() ? ItemStack.EMPTY : list.get((int) (this.time / 20) % list.size());
    }

    private boolean has(TechIngredient ing) {
        if (this.minecraft.player.hasInfiniteMaterials()) {
            return true;
        }
        for (ItemStack s : this.minecraft.player.getInventory().getNonEquipmentItems()) {
            if (ing.matches(s)) {
                return true;
            }
        }
        return false;
    }

    /** Ingredients of a recipe merged by item (count = how many). */
    private List<ItemStack> needs(TechRecipe recipe) {
        List<ItemStack> out = new ArrayList<>();
        for (TechIngredient ing : recipe.layout()) {
            if (ing == null) {
                continue;
            }
            ItemStack ex = ing.example();
            ItemStack found = null;
            for (ItemStack o : out) {
                if (ItemStack.isSameItemSameComponents(o, ex)) {
                    found = o;
                }
            }
            if (found != null) {
                found.grow(1);
            } else {
                out.add(ex.copyWithCount(1));
            }
        }
        return out;
    }

    private int countEverywhere(ItemStack what) {
        int n = 0;
        for (ItemStack s : this.minecraft.player.getInventory().getNonEquipmentItems()) {
            if (ItemStack.isSameItemSameComponents(s, what)) {
                n += s.getCount();
            }
        }
        for (ItemStack s : this.menu.gridItems()) {
            if (ItemStack.isSameItemSameComponents(s, what)) {
                n += s.getCount();
            }
        }
        return n;
    }

    // ---- Upgrade ----

    private List<WebModule> available(ItemStack gadget) {
        List<WebModule> list = new ArrayList<>();
        for (WebModule m : WebModule.values()) {
            if (this.progress().hasModule(m) && !WebModules.has(gadget, m)) {
                list.add(m);
            }
        }
        return list;
    }

    /** Item positions of the module sockets: top row (available modules) and the 5 install slots. */
    private int[] topSocket(int i, int n) {
        int cx = this.leftPos + TechTableMenu.DEVICE_X + 8;
        return new int[]{cx - (n * 24 - 6) / 2 + i * 24, this.topPos + 42};
    }

    private int installSlots() {
        return Math.max(WebModules.MAX_INSTALLED, WebModules.installed(this.menu.device()).size());
    }

    private int[] installSocket(int i) {
        int cx = this.leftPos + TechTableMenu.DEVICE_X + 8;
        return new int[]{cx - (this.installSlots() * 22 - 4) / 2 + i * 22, this.topPos + 118};
    }

    private List<WebModule> moduleRows(int section) {
        ItemStack gadget = this.menu.device();
        List<WebModule> list = new ArrayList<>();
        for (WebModule m : WebModule.values()) {
            boolean unlocked = this.progress().hasModule(m);
            boolean installed = WebModules.has(gadget, m);
            if ((section == 0 && installed) || (section == 1 && unlocked && !installed) || (section == 2 && !unlocked)) {
                list.add(m);
            }
        }
        return list;
    }

    private WebModule shownModule() {
        return this.selectedModule != null ? this.selectedModule : WebModule.values()[0];
    }

    private void drawUpgrade(GuiGraphicsExtractor g, int x, int y, int mx, int my) {
        ItemStack gadget = this.menu.device();
        boolean hasGadget = TechTableMenu.isGadget(gadget);
        // list: installed / unlocked / locked
        int lx = x + LIST_X + 2;
        int ly = y + LIST_Y + 3 - this.listScroll;
        g.enableScissor(x + LIST_X + 1, y + LIST_Y + 1, x + LIST_X + LIST_W - 1, y + LIST_Y + LIST_H - 1);
        String[] sections = {"installed", "unlocked", "locked"};
        for (int s = 0; s < (hasGadget ? 3 : 0); s++) {  // owner: no device = empty list and right panel
            List<WebModule> rows = this.moduleRows(s);
            if (rows.isEmpty()) {
                continue;
            }
            this.groupHeader(g, lx, ly, LIST_W - 4, tr("modules." + sections[s]), rows.size());
            ly += 13;
            for (WebModule m : rows) {
                boolean hover = in(mx, my, lx, ly, LIST_W - 4, CARD_H - 2);
                this.card(g, lx, ly, LIST_W - 4, null, m.sprite(), m.displayName(),
                        Component.translatable("module.powers_unchained." + m.id() + ".short"), m == this.shownModule(), hover, s == 2);
                ly += CARD_H;
            }
        }
        g.disableScissor();

        // blueprint: modules on top, the gadget in the middle, 5 module slots below
        int bx = x + CENTER_X;
        int by = y + BP_Y;
        this.blueprint(g, bx, by, CENTER_W, BP_H, tr("upgrade_blueprint"));
        int dx = x + TechTableMenu.DEVICE_X;
        int dy = y + TechTableMenu.DEVICE_Y;
        List<WebModule> available = this.available(gadget);
        List<WebModule> installed = WebModules.installed(gadget);
        int n = Math.max(1, available.size());
        for (int i = 0; i < (hasGadget ? available.size() : 0); i++) {
            int[] p = this.topSocket(i, n);
            dashed(g, p[0] + 8, p[1] + 17, dx + 8, dy - 5, INK_D);
        }
        boolean creative = WebShootersItem.isCreative(gadget);
        for (int i = 0; i < this.installSlots(); i++) {
            int[] p = this.installSocket(i);
            dashed(g, dx + 8, dy + 21, p[0] + 8, p[1] - 1, i < installed.size() ? INK : INK_D);
        }
        for (int i = 0; i < (hasGadget ? available.size() : 0); i++) {
            int[] p = this.topSocket(i, n);
            WebModule m = available.get(i);
            socket(g, p[0] - 1, p[1] - 1, 18, hasGadget);
            if (m != this.dragging || this.dragFromInstalled) {
                g.blitSprite(RenderPipelines.GUI_TEXTURED, m.sprite(), p[0], p[1], 16, 16, hasGadget ? 0xFFFFFFFF : 0x80FFFFFF);
            }
            if (m == this.shownModule()) {
                outline(g, p[0] - 2, p[1] - 2, 20, 20, COPPER);
            }
        }
        if (!hasGadget) {
            g.centeredText(this.font, tr("put_gadget"), dx + 8, y + 46, INK);
        } else if (creative) {
            g.centeredText(this.font, tr("modules.creative"), dx + 8, y + 46, INK);
        } else if (available.isEmpty()) {
            g.centeredText(this.font, tr("modules.none_open"), dx + 8, y + 46, INK_D);
        }
        // the gadget socket (the real slot is drawn by the menu on top of this)
        g.fill(dx - 5, dy - 5, dx + 21, dy + 21, 0xC80A1932);
        outline(g, dx - 5, dy - 5, 26, 26, INK);
        for (int i = 0; i < this.installSlots(); i++) {
            int[] p = this.installSocket(i);
            boolean target = this.dragging != null && !this.dragFromInstalled;
            socket(g, p[0] - 1, p[1] - 1, 18, target);
            if (target) {
                outline(g, p[0] - 2, p[1] - 2, 20, 20, ((int) ((0.5F + 0.5F * Mth.sin(this.time * 0.4F)) * 255) << 24) | 0xFF9A52);
            }
            if (i < installed.size() && (installed.get(i) != this.dragging || !this.dragFromInstalled)) {
                g.blitSprite(RenderPipelines.GUI_TEXTURED, installed.get(i).sprite(), p[0], p[1], 16, 16);
            }
        }
        g.text(this.font, tr("modules.slots", installed.size(), this.installSlots()), bx + 7, by + BP_H - 13, INK_D, false);
        if (this.failFlash > 0) {
            g.centeredText(this.font, tr("not_enough"), bx + CENTER_W / 2, by + BP_H - 24, 0xFFFF9B8F);
        }

        // right: the picked module
        int rx = x + RIGHT_X;
        int ry = y + BP_Y;
        monitor(g, rx, ry, RIGHT_W, H - BP_Y - 8);
        if (!hasGadget) {
            return;
        }
        WebModule m = this.shownModule();
        boolean unlocked = this.progress().hasModule(m);
        boolean isInstalled = WebModules.has(gadget, m);
        int ty = ry + 4;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, m.sprite(), rx + 4, ty, 16, 16, unlocked ? 0xFFFFFFFF : 0xFF707880);
        this.wrapped(g, m.displayName(), rx + 23, ty + (this.font.width(m.displayName()) > RIGHT_W - 28 ? 0 : 4), RIGHT_W - 27, COPPER, 2);
        ty += 20;
        ty += this.wrapped(g, m.description(), rx + 5, ty, RIGHT_W - 10, GREY, 5) + 4;
        // before -> after
        if (hasGadget) {
            g.text(this.font, tr(isInstalled ? "stats.installed" : "stats.title"), rx + 5, ty, CYAN_DIM, false);
            ty += 11;
            ItemStack after = gadget.copy();
            if (!isInstalled) {
                List<WebModule> list = new ArrayList<>(WebModules.installed(after));
                list.add(m);
                WebModules.set(after, list);
            }
            ty = this.statRow(g, rx, ty, "stats.range", Math.round(WebZipLine.RANGE * WebModules.range(gadget)),
                    Math.round(WebZipLine.RANGE * WebModules.range(after)), true);
            ty = this.statRow(g, rx, ty, "stats.recharge", Math.round(WebType.IMPACT.recharge() * WebModules.recharge(gadget) / 2.0F) / 10.0F,
                    Math.round(WebType.IMPACT.recharge() * WebModules.recharge(after) / 2.0F) / 10.0F, false);
            ty = this.statRow(g, rx, ty, "stats.cartridges", WebModules.cartridgeSlots(gadget), WebModules.cartridgeSlots(after), true);
            ty = this.statRow(g, rx, ty, "stats.web_types", WebModules.has(gadget, WebModule.ELECTRO_WEB) ? 3 : 2,
                    WebModules.has(after, WebModule.ELECTRO_WEB) ? 3 : 2, true);
            ty += 4;
        }
        // unlock / install cost
        if (!unlocked) {
            g.text(this.font, tr("unlock_cost"), rx + 5, ty, COPPER_DARK, false);
            ty += 11;
            ty += this.costRows(g, rx + 4, ty, RIGHT_W - 8, m.unlockCost());
            int bty = y + H - 8 - 20;
            boolean can = m.unlockCost().canPay(this.minecraft.player);
            this.vanillaButton(g, rx + 3, bty, RIGHT_W - 6, 16, tr("unlock"), can, in(mx, my, rx + 3, bty, RIGHT_W - 6, 16), false);
        } else if (!isInstalled) {
            g.text(this.font, tr("install_cost"), rx + 5, ty, COPPER_DARK, false);
            ty += 11;
            ty += this.costRows(g, rx + 4, ty, RIGHT_W - 8, m.installCost());
            this.wrapped(g, tr(hasGadget ? "drag_hint" : "put_gadget_hint"), rx + 5, ty + 2, RIGHT_W - 10, DIM, Math.max(0, (this.topPos + H - 10 - ty - 2) / 10));
        } else {
            this.wrapped(g, tr(creative ? "creative_hint" : "remove_hint"), rx + 5, ty, RIGHT_W - 10, DIM, 3);
        }
    }

    private int statRow(GuiGraphicsExtractor g, int rx, int ty, String key, float before, float after, boolean higherBetter) {
        String b = fmt(before);
        String a = fmt(after);
        String t = before == after ? b : b + "→" + a;
        g.text(this.font, this.trim(tr(key).getString(), RIGHT_W - 13 - this.font.width(t)), rx + 5, ty, 0xFFDFE6EC, false);
        boolean better = higherBetter ? after > before : after < before;
        g.text(this.font, t, rx + RIGHT_W - 5 - this.font.width(t), ty, before == after ? GREY : better ? GREEN : RED, false);
        return ty + 10;
    }

    private static String fmt(float v) {
        return v == Math.round(v) ? String.valueOf(Math.round(v)) : String.format(Locale.ROOT, "%.1f", v);
    }

    // ---- Customization ----

    private int[] holoButton(int i) {
        int w = 42;
        return new int[]{this.leftPos + CENTER_X + CENTER_W - 4 - (2 - i) * (w + 2), this.topPos + BP_Y + 3, w, 12};
    }

    private boolean holoSlim() {
        return this.holoSlim != null ? this.holoSlim : this.minecraft.player.getSkin().model() == PlayerModelType.SLIM;
    }

    private void drawCustomize(GuiGraphicsExtractor g, int x, int y, int mx, int my) {
        ItemStack gadget = this.menu.device();
        boolean linked = this.menu.isLinked();
        boolean hasGadget = linked && TechTableMenu.isGadget(gadget);
        int lx = x + LIST_X + 2;
        int ly = y + LIST_Y + 3;
        if (hasGadget) {
            this.groupHeader(g, lx, ly, LIST_W - 4, tr("looks"), GadgetSkins.WEB_SHOOTERS.size());
            ly += 13;
            String current = GadgetSkins.of(gadget);
            for (int i = 0; i < GadgetSkins.WEB_SHOOTERS.size(); i++) {
                GadgetSkins skin = GadgetSkins.WEB_SHOOTERS.get(i);
                boolean unlocked = this.progress().hasSkin(skin.id());
                boolean hover = in(mx, my, lx, ly, LIST_W - 4, CARD_H - 2);
                this.card(g, lx, ly, LIST_W - 4, gadget, null, skin.displayName(),
                        skin.id().equals(current) ? tr("look.current") : unlocked ? tr("look.unlocked") : tr("look.locked"),
                        i == this.selectedSkin, hover, !unlocked);
                ly += CARD_H;
            }
            g.centeredText(this.font, tr("looks.more"), x + LIST_X + LIST_W / 2, ly + 6, DIM);
        }

        // hologram of the gadget on a player
        int hx = x + CENTER_X;
        int hy = y + BP_Y;
        int hw = CENTER_W;
        int hh = 106;
        monitor(g, hx, hy, hw, hh);
        brackets(g, hx + 2, hy + 2, hw - 4, hh - 4, linked ? CYAN_DIM : 0xFF8A3A30);
        g.text(this.font, tr("hologram"), hx + 6, hy + 5, linked ? CYAN_DIM : 0xFF8A3A30, false);
        int cx = hx + hw / 2;
        int feet = hy + hh - 6;
        if (!linked) {
            // owner: Customization only works when the table touches a Suit Creator or is wired to one with redstone
            g.blitSprite(RenderPipelines.GUI_TEXTURED, Constants.id("suit_creator/lock"), cx - 8, hy + 20, 16, 18);
            int ty = hy + 40;
            ty += this.wrapped(g, tr("link.title"), hx + 8, ty, hw - 16, 0xFFFF9B8F, 2) + 3;
            this.wrapped(g, tr("link.how"), hx + 8, ty, hw - 16, GREY, 5);
            int bx = x + RIGHT_X;
            monitor(g, bx, y + BP_Y, RIGHT_W, H - BP_Y - 8);
            return;
        }
        g.fillGradient(cx - 26, hy + 8, cx + 26, feet, 0x0059E4FF, 0x2259E4FF);
        g.fill(cx - 30, feet, cx + 30, feet + 1, 0xFF59E4FF);
        g.fill(cx - 22, feet + 1, cx + 22, feet + 2, 0x8859E4FF);
        if (hasGadget) {
            float spin = this.rotating ? 0.0F : this.time * 0.6F;
            GadgetHologram.draw(g, hx + 2, hy + 14, hx + hw - 2, feet, 40.0F, this.holoSlim(), this.yaw + spin, this.time,
                    this.holoRun ? com.star_1mp.powersunchained.client.suitcreator.SuitHologram.Pose.RUN
                            : com.star_1mp.powersunchained.client.suitcreator.SuitHologram.Pose.STAND);
            // model + pose buttons (like the Suit Creator's hologram)
            Component[] labels = {tr(this.holoSlim() ? "holo.alex" : "holo.steve"), tr(this.holoRun ? "holo.run" : "holo.stand")};
            for (int i = 0; i < 2; i++) {
                int[] b = this.holoButton(i);
                this.vanillaButton(g, b[0], b[1], b[2], b[3], labels[i], true, in(mx, my, b[0], b[1], b[2], b[3]), false);
            }
        } else {
            g.centeredText(this.font, tr("looks.put_short"), cx, hy + hh / 2 - 4, CYAN);
        }
        // the gadget slot under it (the empty-slot icon is the slot's own background)
        int sx = x + TechTableMenu.SKIN_DEVICE_X - 1;
        int sy = y + TechTableMenu.SKIN_DEVICE_Y - 1;
        g.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("container/slot"), sx, sy, 18, 18);
        g.text(this.font, tr("gadget"), sx - 4 - this.font.width(tr("gadget")), sy + 5, LABEL, false);

        // right: the picked look
        int rx = x + RIGHT_X;
        int ry = y + BP_Y;
        monitor(g, rx, ry, RIGHT_W, H - BP_Y - 8);
        if (!hasGadget) {
            return;
        }
        GadgetSkins skin = GadgetSkins.WEB_SHOOTERS.get(Mth.clamp(this.selectedSkin, 0, GadgetSkins.WEB_SHOOTERS.size() - 1));
        boolean unlocked = this.progress().hasSkin(skin.id());
        boolean current = skin.id().equals(GadgetSkins.of(gadget));
        int ty = ry + 5;
        ty += this.wrapped(g, skin.displayName(), rx + 5, ty, RIGHT_W - 10, COPPER, 2) + 4;
        if (GadgetSkins.DEFAULT.equals(skin.id())) {
            ty += this.wrapped(g, tr("look.default_desc"), rx + 5, ty, RIGHT_W - 10, GREY, 4) + 4;
        } else if (!unlocked) {
            g.text(this.font, tr("look.how"), rx + 5, ty, COPPER_DARK, false);
            ty += 11;
            if (skin.cost() != null) {
                ty += this.costRows(g, rx + 4, ty, RIGHT_W - 8, skin.cost());
            } else if (skin.hint() != null) {
                ty += this.wrapped(g, Component.translatable(skin.hint()), rx + 5, ty, RIGHT_W - 10, GREY, 5);
            }
        }
        int bty = y + H - 8 - 20;
        if (!unlocked) {
            boolean can = skin.cost() != null && skin.cost().canPay(this.minecraft.player);
            this.vanillaButton(g, rx + 3, bty, RIGHT_W - 6, 16, tr("unlock"), can, in(mx, my, rx + 3, bty, RIGHT_W - 6, 16), false);
        } else {
            this.vanillaButton(g, rx + 3, bty, RIGHT_W - 6, 16, tr(current ? "look.selected" : "look.select"), !current,
                    in(mx, my, rx + 3, bty, RIGHT_W - 6, 16), false);
        }
    }

    // ---- tooltips ----

    private void tooltips(GuiGraphicsExtractor g, int mx, int my) {
        int x = this.leftPos;
        int y = this.topPos;
        if (this.menu.tab == TechTableMenu.TAB_UPGRADE) {
            ItemStack gadget = this.menu.device();
            List<WebModule> available = TechTableMenu.isGadget(gadget) ? this.available(gadget) : List.of();
            for (int i = 0; i < available.size(); i++) {
                int[] p = this.topSocket(i, Math.max(1, available.size()));
                if (in(mx, my, p[0], p[1], 16, 16)) {
                    g.setTooltipForNextFrame(this.font, available.get(i).displayName().copy().append(" — ").append(tr("drag_down")), mx, my);
                }
            }
            List<WebModule> installed = WebModules.installed(gadget);
            for (int i = 0; i < installed.size(); i++) {
                int[] p = this.installSocket(i);
                if (in(mx, my, p[0], p[1], 16, 16)) {
                    g.setTooltipForNextFrame(this.font, installed.get(i).displayName().copy().append(" — ").append(tr("drag_up")), mx, my);
                }
            }
        } else if (this.menu.tab == TechTableMenu.TAB_CREATE && this.selectedRecipe != null) {
            int gx = x + TechTableMenu.GRID_X;
            int gy = y + TechTableMenu.GRID_Y;
            List<@Nullable TechIngredient> layout = this.selectedRecipe.layout();
            for (int i = 0; i < 9; i++) {
                if (layout.get(i) != null && this.menu.gridItems().get(i).isEmpty()
                        && in(mx, my, gx + (i % 3) * 22, gy + (i / 3) * 22, 16, 16)) {
                    g.setTooltipForNextFrame(this.font, this.cycle(layout.get(i)), mx, my);
                }
            }
            List<ItemStack> needs = this.needs(this.selectedRecipe);
            for (int i = 0; i < Math.min(6, needs.size()); i++) {
                if (in(mx, my, x + RIGHT_X + (i % 2) * 52, y + 163 + (i / 2) * 18, 48, 16)) {
                    g.setTooltipForNextFrame(this.font, needs.get(i), mx, my);
                }
            }
        }
    }

    // =====================================================================================================
    // input
    // =====================================================================================================

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && this.clickUi(mx, my)) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean clickUi(double mx, double my) {
        int x = this.leftPos;
        int y = this.topPos;
        for (int i = 0; i < TABS.length; i++) {
            int[] b = this.tabBounds(i);
            if (in(mx, my, b[0], b[1], b[2], b[3])) {
                if (this.menu.tab != TABS[i]) {
                    this.setTab(TABS[i]);
                    this.click();
                }
                return true;
            }
        }
        boolean inList = in(mx, my, x + LIST_X, y + LIST_Y, LIST_W, LIST_H);
        int lx = x + LIST_X + 2;
        switch (this.menu.tab) {
            case TechTableMenu.TAB_CREATE -> {
                if (inList) {
                    int ly = y + LIST_Y + 3 - this.listScroll;
                    for (ListRow row : this.createRows()) {
                        if (row.group() != null) {
                            ly += 13;
                            continue;
                        }
                        if (in(mx, my, lx, ly, LIST_W - 4, CARD_H - 2)) {
                            this.selectedRecipe = row.recipe();
                            this.button(TechTableMenu.PLACE_RECIPE + TechRecipes.all().indexOf(row.recipe()));
                            this.click();
                            return true;
                        }
                        ly += CARD_H;
                    }
                }
            }
            case TechTableMenu.TAB_UPGRADE -> {
                ItemStack gadget = this.menu.device();
                if (!TechTableMenu.isGadget(gadget)) {
                    break;  // no device: nothing to click
                }
                if (inList) {
                    int ly = y + LIST_Y + 3 - this.listScroll;
                    for (int s = 0; s < 3; s++) {
                        List<WebModule> rows = this.moduleRows(s);
                        if (rows.isEmpty()) {
                            continue;
                        }
                        ly += 13;
                        for (WebModule m : rows) {
                            if (in(mx, my, lx, ly, LIST_W - 4, CARD_H - 2)) {
                                this.selectedModule = m;
                                this.click();
                                return true;
                            }
                            ly += CARD_H;
                        }
                    }
                }
                // start dragging a module
                if (TechTableMenu.isGadget(gadget)) {
                    List<WebModule> available = this.available(gadget);
                    for (int i = 0; i < available.size(); i++) {
                        int[] p = this.topSocket(i, Math.max(1, available.size()));
                        if (in(mx, my, p[0] - 1, p[1] - 1, 18, 18)) {
                            this.selectedModule = available.get(i);
                            this.dragging = available.get(i);
                            this.dragFromInstalled = false;
                            this.playSound(SoundEvents.ITEM_PICKUP, 1.6F);
                            return true;
                        }
                    }
                    List<WebModule> installed = WebModules.installed(gadget);
                    for (int i = 0; i < installed.size(); i++) {
                        int[] p = this.installSocket(i);
                        if (in(mx, my, p[0] - 1, p[1] - 1, 18, 18)) {
                            this.selectedModule = installed.get(i);
                            if (WebShootersItem.isCreative(gadget)) {
                                return true;  // creative web-shooters keep every module
                            }
                            this.dragging = installed.get(i);
                            this.dragFromInstalled = true;
                            this.playSound(SoundEvents.ITEM_PICKUP, 1.6F);
                            return true;
                        }
                    }
                }
                // unlock button
                WebModule m = this.shownModule();
                int bty = y + H - 8 - 20;
                if (!this.progress().hasModule(m) && in(mx, my, x + RIGHT_X + 3, bty, RIGHT_W - 6, 16)) {
                    if (m.unlockCost().canPay(this.minecraft.player)) {
                        this.button(TechTableMenu.UNLOCK_MODULE + m.ordinal());
                        this.click();
                    } else {
                        this.fail();
                    }
                    return true;
                }
            }
            case TechTableMenu.TAB_CUSTOMIZE -> {
                ItemStack gadget = this.menu.device();
                if (!this.menu.isLinked() || !TechTableMenu.isGadget(gadget)) {
                    break;
                }
                for (int i = 0; i < 2; i++) {
                    int[] b = this.holoButton(i);
                    if (in(mx, my, b[0], b[1], b[2], b[3])) {
                        if (i == 0) {
                            this.holoSlim = !this.holoSlim();
                        } else {
                            this.holoRun = !this.holoRun;
                        }
                        this.click();
                        return true;
                    }
                }
                if (inList) {
                    int ly = y + LIST_Y + 3 + 13;
                    for (int i = 0; i < GadgetSkins.WEB_SHOOTERS.size(); i++) {
                        if (in(mx, my, lx, ly, LIST_W - 4, CARD_H - 2)) {
                            this.selectedSkin = i;
                            this.click();
                            return true;
                        }
                        ly += CARD_H;
                    }
                }
                int bty = y + H - 8 - 20;
                if (in(mx, my, x + RIGHT_X + 3, bty, RIGHT_W - 6, 16)) {
                    GadgetSkins skin = GadgetSkins.WEB_SHOOTERS.get(Mth.clamp(this.selectedSkin, 0, GadgetSkins.WEB_SHOOTERS.size() - 1));
                    if (!this.progress().hasSkin(skin.id())) {
                        if (skin.cost() != null && skin.cost().canPay(this.minecraft.player)) {
                            this.button(TechTableMenu.UNLOCK_SKIN + this.selectedSkin);
                            this.click();
                        } else {
                            this.fail();
                        }
                    } else if (!skin.id().equals(GadgetSkins.of(gadget))) {
                        this.button(TechTableMenu.SELECT_SKIN + this.selectedSkin);
                        this.click();
                    }
                    return true;
                }
                if (in(mx, my, x + CENTER_X, y + BP_Y, CENTER_W, 106)) {
                    this.rotating = true;
                    return true;
                }
            }
            default -> {
            }
        }
        return false;
    }

    private void fail() {
        this.failFlash = 40;
        this.playSound(SoundEvents.VILLAGER_NO, 1.2F);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.rotating) {
            this.yaw += (float) dx * 2.0F;
            return true;
        }
        if (this.dragging != null) {
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.rotating) {
            this.rotating = false;
            return true;
        }
        if (this.dragging != null) {
            WebModule m = this.dragging;
            this.dragging = null;
            double my = event.y();
            double mx = event.x();
            int y = this.topPos;
            boolean overBlueprint = in(mx, my, this.leftPos + CENTER_X, y + BP_Y, CENTER_W, BP_H);
            if (!this.dragFromInstalled && overBlueprint && my >= y + TechTableMenu.DEVICE_Y + 22) {
                // dropped on the module slots: install
                if (WebModules.installed(this.menu.device()).size() >= WebModules.MAX_INSTALLED || !m.installCost().canPay(this.minecraft.player)) {
                    this.fail();
                } else {
                    this.button(TechTableMenu.INSTALL_MODULE + m.ordinal());
                    this.playSound(SoundEvents.SMITHING_TABLE_USE, 1.3F);
                }
            } else if (this.dragFromInstalled && (!overBlueprint || my < y + TechTableMenu.DEVICE_Y + 22)) {
                // dragged back up (or out): remove
                this.button(TechTableMenu.REMOVE_MODULE + m.ordinal());
                this.playSound(SoundEvents.ITEM_FRAME_REMOVE_ITEM, 1.2F);
            }
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (in(mx, my, this.leftPos + LIST_X, this.topPos + LIST_Y, LIST_W, LIST_H)) {
            this.listScroll = Math.max(0, this.listScroll - (int) (scrollY * 12));
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    // =====================================================================================================
    // dev / CI
    // =====================================================================================================

    public void debugTab(int tab) {
        this.setTab(tab);
    }

    public void debugSelectRecipe(String id) {
        TechRecipe r = TechRecipes.byId(id);
        if (r != null) {
            this.selectedRecipe = r;
            this.button(TechTableMenu.PLACE_RECIPE + TechRecipes.all().indexOf(r));
        }
    }

    public void debugSelectModule(WebModule module) {
        this.selectedModule = module;
    }

    public void debugButton(int id) {
        this.button(id);
    }

    public void debugHologram(boolean slim, boolean run) {
        this.holoSlim = slim;
        this.holoRun = run;
    }

    public void debugDrag(@Nullable WebModule module, boolean fromInstalled) {
        this.dragging = module;
        this.dragFromInstalled = fromInstalled;
    }
}
