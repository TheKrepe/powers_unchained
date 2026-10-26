package com.star_1mp.powersunchained.client.suitcreator;

import com.star_1mp.powersunchained.Constants;
import com.star_1mp.powersunchained.menu.SuitCreatorMenu;
import com.star_1mp.powersunchained.registry.PUItems;
import com.star_1mp.powersunchained.suit.Suit;
import com.star_1mp.powersunchained.suit.SuitCatalog;
import com.star_1mp.powersunchained.suit.SuitInfo;
import com.star_1mp.powersunchained.suit.SuitProgress;
import com.star_1mp.powersunchained.suit.SuitTag;
import com.star_1mp.powersunchained.suit.SuitUnlocks;
import com.star_1mp.powersunchained.suit.Suits;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import org.jspecify.annotations.Nullable;

/**
 * Suit Creator screen (vanilla container look with dark "tech" monitors). Three tabs:
 * <ul>
 *   <li>Catalog: suit list (search, tag filter popup, collapsible hero groups, favorites, locked suits (name + tags known, lock icon instead of the look, still selectable)),
 *   hologram (Steve / Alex, poses, part toggles, drag to rotate, variant arrows), blueprint (pieces, materials have /
 *   need, armor, durability), a big Sew panel (or "unlock for XP" while the suit is locked) and the inventory.</li>
 *   <li>Scanner: scan slot (any suit piece), progress, decoding ghost of the target suit, the inventory.</li>
 * </ul>
 * The tag count and unlocked count are shown in the top bar on every tab.
 */
public class SuitCreatorScreen extends AbstractContainerScreen<SuitCreatorMenu> {

    // ---- layout ----
    private static final int W = 416;
    private static final int H = 236;
    private static final int LIST_X = 8;
    private static final int LIST_Y = 37;
    private static final int LIST_W = 128;
    private static final int LIST_H = 191;
    private static final int CENTER_X = 140;
    private static final int CENTER_W = 148;
    private static final int RIGHT_X = 292;
    private static final int RIGHT_W = 116;
    private static final int SEW_X = 306;
    private static final int SEW_Y = 152;
    private static final int SEW_W = 102;
    private static final int SEW_H = 76;
    private static final int HEADER_H = 12;
    /** Blueprint material list: monitor top (relative), row height, visible height (4 rows). */
    private static final int MAT_Y = 52;
    private static final int MAT_ROW = 16;
    private static final int MAT_VISIBLE = 64;
    private static final int CARD_H = 22;

    private static final int TAB_CATALOG = 0;
    /** Tabs in their order on screen (the dossier tab was removed, owner). */
    private static final int[] TABS = {0, 2};
    private static final int TAB_SCANNER = 2;
    private static final int SEW_TICKS = 20;

    // ---- colors ----
    private static final int LABEL = 0xFF404040;
    private static final int AMBER = 0xFFFFB13B;
    private static final int AMBER_DARK = 0xFF8A5A12;
    private static final int CYAN = 0xFF59E4FF;
    private static final int CYAN_DIM = 0xFF2F8FA8;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREY = 0xFF9AA3AD;
    private static final int DIM = 0xFF5C646E;
    private static final int GREEN = 0xFF7CFC6A;
    private static final int RED = 0xFFFF5A4A;
    private static final int GLASS = 0xFF12161B;

    private static final ArmorType[] PIECES = {ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS};
    private static final String[] PART_SPRITES = {"head", "chest", "legs", "feet"};
    private static final String LOCKED_GROUP = "???";
    /** List group of the starred suits (they stay in their hero group too). */
    private static final String FAVORITES_GROUP = "__favorites";

    private EditBox search;
    private final List<Row> rows = new ArrayList<>();
    private int listScroll;
    private @Nullable Suit selected;
    private int tab;
    private boolean tagsOpen;
    private int piece = SuitCatalog.ALL;
    private final boolean[] parts = {true, true, true, true};
    private SuitHologram.Pose pose = SuitHologram.Pose.STAND;
    private float yaw = -20.0F;
    private boolean dragging;
    private int materialScroll;
    private boolean draggingMaterialBar;
    private int sewTicks = -1;
    private int sewButton;
    private float time;

    public SuitCreatorScreen(SuitCreatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, W, H);
        SuitCreatorPrefs.load();
        this.tab = SuitCreatorPrefs.tab == TAB_SCANNER ? TAB_SCANNER : TAB_CATALOG;
        this.selected = Suits.byName(SuitCreatorPrefs.selected);
    }

    // =====================================================================================================
    // setup / state
    // =====================================================================================================

    @Override
    protected void init() {
        super.init();
        String old = this.search != null ? this.search.getValue() : "";
        this.search = new EditBox(this.font, this.leftPos + LIST_X, this.topPos + 20, 86, 14,
                Component.translatable("screen.powers_unchained.suit_creator.search"));
        this.search.setHint(Component.translatable("screen.powers_unchained.suit_creator.search").withColor(0xFF6F7780));
        this.search.setMaxLength(32);
        this.search.setValue(old);
        this.search.setResponder(value -> this.listScroll = 0);
        this.addRenderableWidget(this.search);
        this.applyTab();
    }

    private void applyTab() {
        this.menu.inventoryVisible = true;
        this.menu.scannerVisible = this.tab == TAB_SCANNER;
        SuitCreatorPrefs.tab = this.tab;
    }

    private SuitProgress progress() {
        return SuitUnlocks.progress(this.minecraft.player);
    }

    private boolean unlocked(Suit suit) {
        return this.progress().isUnlocked(suit);
    }

    private void select(Suit suit) {
        if (suit != this.selected) {
            this.selected = suit;
            this.sewTicks = -1;
            this.materialScroll = 0;
            SuitCreatorPrefs.selected = suit.id().getPath();
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.sewTicks >= 0) {
            this.sewTicks++;
            if (this.sewTicks % 5 == 1) {
                this.playSound(SoundEvents.WOOL_PLACE, 1.4F + this.sewTicks * 0.01F);
            }
            if (this.sewTicks >= SEW_TICKS) {
                this.sewTicks = -1;
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, this.sewButton);
            }
        }
    }

    private void playSound(net.minecraft.sounds.SoundEvent sound, float pitch) {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
    }

    private void click() {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private static Component variantName(Suit suit) {
        return Component.translatable(Suits.variantNameKey(suit));
    }

    private static Component groupName(String group) {
        return Component.translatable("suit_group.powers_unchained." + group);
    }

    private static ItemStack pieceStack(Suit suit, int index) {
        return PUItems.piece(suit, PIECES[index]).get().getDefaultInstance();
    }

    private static Identifier sprite(String name) {
        return Constants.id("suit_creator/" + name);
    }

    private static Component tr(String key, Object... args) {
        return Component.translatable("screen.powers_unchained.suit_creator." + key, args);
    }

    // =====================================================================================================
    // list model
    // =====================================================================================================

    /** A list row: group header, suit card, locked header / card, or a message. */
    private record Row(int kind, String group, @Nullable Suit suit, int count) {
        static final int HEADER = 0;
        static final int CARD = 1;
        static final int LOCKED_CARD = 2;
        static final int MESSAGE = 3;

        int height() {
            return this.kind == HEADER || this.kind == MESSAGE ? HEADER_H : CARD_H;
        }
    }

    private void rebuildRows() {
        this.rows.clear();
        SuitProgress progress = this.progress();
        String query = this.search.getValue().trim().toLowerCase(Locale.ROOT);
        Map<String, List<Suit>> groups = new LinkedHashMap<>();
        for (Suit suit : Suits.ALL) {
            SuitInfo info = SuitCatalog.info(suit);
            if (info.tags().stream().noneMatch(SuitCreatorPrefs.TAGS::contains)) {
                continue;
            }
            String name = variantName(suit).getString().toLowerCase(Locale.ROOT);
            String group = groupName(info.group()).getString().toLowerCase(Locale.ROOT);
            if (!query.isEmpty() && !name.contains(query) && !group.contains(query)) {
                continue;
            }
            groups.computeIfAbsent(info.group(), g -> new ArrayList<>()).add(suit);
        }
        // Favorites: starred suits, listed here AND in their own hero group
        List<Suit> favorites = new ArrayList<>();
        groups.values().forEach(list -> list.stream().filter(s -> SuitCreatorPrefs.FAVORITES.contains(s.id().getPath())
                && progress.isUnlocked(s)).forEach(favorites::add));
        if (!favorites.isEmpty()) {
            this.rows.add(new Row(Row.HEADER, FAVORITES_GROUP, null, favorites.size()));
            if (!SuitCreatorPrefs.COLLAPSED.contains(FAVORITES_GROUP)) {
                favorites.forEach(s -> this.rows.add(new Row(Row.CARD, FAVORITES_GROUP, s, 0)));
            }
        }
        groups.forEach((group, suits) -> {
            // unlocked first, then locked (locked ones show their name and tags, not their look)
            suits.sort(Comparator.comparing((Suit s) -> progress.isUnlocked(s) ? 0 : 1));
            this.rows.add(new Row(Row.HEADER, group, null, suits.size()));
            if (!SuitCreatorPrefs.COLLAPSED.contains(group)) {
                suits.forEach(s -> this.rows.add(new Row(progress.isUnlocked(s) ? Row.CARD : Row.LOCKED_CARD, group, s, 0)));
            }
        });
        if (this.rows.isEmpty()) {
            this.rows.add(new Row(Row.MESSAGE, SuitCreatorPrefs.TAGS.isEmpty() ? "empty_tags" : "empty", null, 0));
        }
        if (this.selected == null) {
            this.rows.stream().filter(r -> r.suit != null).findFirst().ifPresent(r -> this.select(r.suit));
            if (this.selected == null) {
                this.select(Suits.ALL.get(0));
            }
        }
        int content = this.rows.stream().mapToInt(Row::height).sum();
        this.listScroll = Mth.clamp(this.listScroll, 0, Math.max(0, content - (LIST_H - 4)));
    }

    private int maxScroll() {
        return Math.max(0, this.rows.stream().mapToInt(Row::height).sum() - (LIST_H - 4));
    }

    // =====================================================================================================
    // drawing helpers
    // =====================================================================================================

    /** Vanilla container panel: light grey, white / dark bevel, black rounded outline. */
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
        g.fill(x + 3, y + 3, x + 4, y + 4, 0xFFFFFFFF);
        g.fill(x + w - 4, y + h - 4, x + w - 3, y + h - 3, 0xFF555555);
    }

    /** Sunken area (like a slot). */
    private static void inset(GuiGraphicsExtractor g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + h, color);
        g.fill(x, y, x + w - 1, y + 1, 0xFF373737);
        g.fill(x, y, x + 1, y + h - 1, 0xFF373737);
        g.fill(x + 1, y + h - 1, x + w, y + h, 0xFFFFFFFF);
        g.fill(x + w - 1, y + 1, x + w, y + h, 0xFFFFFFFF);
    }

    /** Dark glass monitor with faint scanlines. */
    private static void monitor(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        inset(g, x, y, w, h, GLASS);
        for (int yy = y + 2; yy < y + h - 1; yy += 2) {
            g.fill(x + 1, yy, x + w - 1, yy + 1, 0x163C5A6E);
        }
        g.fill(x + 1, y + 1, x + w - 1, y + 2, 0x2859E4FF);
    }

    /** Corner brackets. */
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

    private void button(GuiGraphicsExtractor g, int x, int y, int w, int h, Component text, boolean active, boolean hovered, boolean selected) {
        Identifier id = Identifier.withDefaultNamespace(!active ? "widget/button_disabled"
                : hovered || selected ? "widget/button_highlighted" : "widget/button");
        g.blitSprite(RenderPipelines.GUI_TEXTURED, id, x, y, w, h);
        int color = !active ? 0xFFA0A0A0 : selected ? 0xFFFFE2A8 : WHITE;
        g.centeredText(this.font, text, x + w / 2, y + (h - 8) / 2 + 1, color);
        if (selected) {
            g.fill(x + 3, y + h - 3, x + w - 3, y + h - 2, AMBER);
        }
    }

    private int chip(GuiGraphicsExtractor g, int x, int y, SuitTag tag, boolean on) {
        Component text = tag.title();
        int w = this.font.width(text) + 6;
        int bg = on ? tag.color() : 0xFF3A3F46;
        g.fill(x, y, x + w, y + 11, bg);
        g.fill(x, y + 10, x + w, y + 11, 0x5A000000);
        g.text(this.font, text, x + 3, y + 2, on ? WHITE : 0xFF8A9099, false);
        return w;
    }

    /** Tag chips in a w-wide area starting at (x, y), at most 18 px high. */
    private void drawTagRow(GuiGraphicsExtractor g, int x, int y, int w, List<SuitTag> tags) {
        int full = -2;
        for (SuitTag tag : tags) {
            full += this.font.width(tag.title()) + 8;
        }
        float scale = full <= w ? 1.0F : 0.75F;
        g.pose().pushMatrix();
        g.pose().translate(x, y + (scale == 1.0F ? 2 : 0));
        g.pose().scale(scale, scale);
        float maxW = w / scale;
        int cx = 0;
        int cy = 0;
        for (SuitTag tag : tags) {
            int cw = this.font.width(tag.title()) + 6;
            if (cx > 0 && cx + cw > maxW) {
                cx = 0;
                cy += 12;
                if (scale == 1.0F || cy > 12) {
                    break;
                }
            }
            cx += this.chip(g, cx, cy, tag, true) + 2;
        }
        g.pose().popMatrix();
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && my >= y && mx < x + w && my < y + h;
    }

    private String trim(String text, int width) {
        if (this.font.width(text) <= width) {
            return text;
        }
        return this.font.plainSubstrByWidth(text, width - this.font.width("..")) + "..";
    }

    // =====================================================================================================
    // rendering
    // =====================================================================================================

    @Override
    public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(g, mouseX, mouseY, partialTick);
        this.time = SuitCreatorClient.ticks() + partialTick;
        this.rebuildRows();
        int x = this.leftPos;
        int y = this.topPos;
        panel(g, x, y, W, H);
        this.drawTopBar(g, x, y, mouseX, mouseY);
        this.drawList(g, x, y, mouseX, mouseY);
        switch (this.tab) {
            case TAB_SCANNER -> this.drawScanner(g, x, y, mouseX, mouseY);
            default -> this.drawCatalog(g, x, y, mouseX, mouseY);
        }
        if (this.menu.inventoryVisible) {
            for (Slot slot : this.menu.slots) {
                if (slot.isActive()) {
                    g.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("container/slot"), x + slot.x - 1, y + slot.y - 1, 18, 18);
                }
            }
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int xm, int ym) {
        // drawn in the background
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        if (this.tagsOpen) {
            g.nextStratum();
            this.drawTagsPopup(g, mouseX, mouseY);
        } else if (this.hoveredSlot == null && this.menu.getCarried().isEmpty()) {
            this.tooltips(g, mouseX, mouseY);
        }
    }

    // ---- top bar ----

    /** Bounds of the tab at screen position {@code i} (see {@link #TABS}). */
    private int[] tabBounds(int i) {
        // centred in the menu
        int total = -2;
        for (int tab : TABS) {
            total += this.font.width(tabTitle(tab)) + 14;
        }
        int x = this.leftPos + (W - total) / 2;
        for (int t = 0; t < i; t++) {
            x += this.font.width(tabTitle(TABS[t])) + 14;
        }
        return new int[]{x, this.topPos + 4, this.font.width(tabTitle(TABS[i])) + 12, 14};
    }

    private static Component tabTitle(int tab) {
        return tr(tab == TAB_SCANNER ? "tab.scanner" : "tab.catalog");
    }

    private void drawTopBar(GuiGraphicsExtractor g, int x, int y, int mx, int my) {
        g.text(this.font, this.title, x + 8, y + 7, LABEL, false);
        for (int i = 0; i < TABS.length; i++) {
            int[] b = this.tabBounds(i);
            this.button(g, b[0], b[1], b[2], b[3], tabTitle(TABS[i]), true, in(mx, my, b[0], b[1], b[2], b[3]), this.tab == TABS[i]);
        }
        // indicators: enabled tags, unlocked suits
        int bx = x + W - 8 - 78;
        inset(g, bx, y + 4, 78, 14, 0xFF1D2128);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("tag_icon"), bx + 4, y + 7, 7, 7);
        g.text(this.font, String.valueOf(SuitCreatorPrefs.TAGS.size()), bx + 13, y + 7, AMBER, false);
        String count = this.progress().unlockedCount() + "/" + Suits.ALL.size();
        int cw = this.font.width(count);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("suit_icon"), bx + 78 - 6 - cw - 9, y + 7, 7, 7);
        g.text(this.font, count, bx + 78 - 5 - cw, y + 7, CYAN, false);
    }

    // ---- list ----

    private void drawList(GuiGraphicsExtractor g, int x, int y, int mx, int my) {
        // tags button
        int tx = x + 96;
        int ty = y + 20;
        boolean hover = in(mx, my, tx, ty, 40, 14);
        this.button(g, tx, ty, 40, 14, Component.empty(), true, hover || this.tagsOpen, false);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("tag_icon"), tx + 5, ty + 4, 7, 7);
        g.text(this.font, String.valueOf(SuitCreatorPrefs.TAGS.size()), tx + 15, ty + 3, this.tagsOpen ? 0xFFFFE2A8 : WHITE, true);

        monitor(g, x + LIST_X, y + LIST_Y, LIST_W, LIST_H);
        int ix = x + LIST_X + 2;
        int iy = y + LIST_Y + 2;
        int iw = LIST_W - 10;
        int ih = LIST_H - 4;
        g.enableScissor(ix, iy, ix + iw, iy + ih);
        int ry = iy - this.listScroll;
        for (Row row : this.rows) {
            int h = row.height();
            if (ry + h > iy && ry < iy + ih) {
                boolean hov = !this.tagsOpen && in(mx, my, ix, Math.max(ry, iy), iw, Math.min(ry + h, iy + ih) - Math.max(ry, iy));
                this.drawRow(g, row, ix, ry, iw, hov, mx, my);
            }
            ry += h;
        }
        g.disableScissor();
        // scrollbar
        int max = this.maxScroll();
        int sx = x + LIST_X + LIST_W - 7;
        g.fill(sx, iy, sx + 4, iy + ih, 0xFF0A0D10);
        int content = ih + max;
        int thumb = Math.max(14, ih * ih / Math.max(1, content));
        int pos = max == 0 ? 0 : (ih - thumb) * this.listScroll / max;
        g.fill(sx, iy + pos, sx + 4, iy + pos + thumb, max == 0 ? 0xFF2A3038 : 0xFF6F7A86);
        g.fill(sx, iy + pos, sx + 3, iy + pos + thumb - 1, max == 0 ? 0xFF353C46 : 0xFFA9B4C0);
    }

    private void drawRow(GuiGraphicsExtractor g, Row row, int x, int y, int w, boolean hover, int mx, int my) {
        switch (row.kind) {
            case Row.HEADER -> {
                boolean locked = LOCKED_GROUP.equals(row.group);
                g.fill(x, y, x + w, y + HEADER_H - 1, hover ? 0xFF323B46 : 0xFF2A323C);
                boolean collapsed = SuitCreatorPrefs.COLLAPSED.contains(row.group);
                int ax = x + 3;
                int ay = y + 3;
                if (collapsed) {
                    for (int i = 0; i < 3; i++) {
                        g.fill(ax + i, ay + i, ax + i + 1, ay + 5 - i, AMBER);
                    }
                } else {
                    for (int i = 0; i < 3; i++) {
                        g.fill(ax + i, ay + 1 + i, ax + 5 - i, ay + 2 + i, AMBER);
                    }
                }
                boolean favorites = FAVORITES_GROUP.equals(row.group);
                Component name = locked ? Component.literal(LOCKED_GROUP) : favorites ? tr("favorites") : groupName(row.group);
                g.text(this.font, name, x + 11, y + 2, AMBER, false);
                if (favorites) {
                    g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("star_on"), x + 14 + this.font.width(name), y + 2, 7, 7);
                } else if (!locked) {
                    // the hero's emblem right after the name (textures/gui/sprites/suit_creator/group/<group>.png)
                    g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("group/" + row.group), x + 13 + this.font.width(name), y + 1, 9, 9);
                }
                String count = String.valueOf(row.count);
                g.text(this.font, count, x + w - 3 - this.font.width(count), y + 2, GREY, false);
            }
            case Row.MESSAGE -> g.text(this.font, tr(row.group), x + 4, y + 2, GREY, false);
            default -> {
                Suit suit = row.suit;
                boolean locked = row.kind == Row.LOCKED_CARD;
                boolean sel = suit == this.selected;
                g.fill(x, y, x + w, y + CARD_H - 1, hover ? 0xFF27303A : 0xFF1E242C);
                SuitInfo info = SuitCatalog.info(suit);
                g.fill(x, y, x + 2, y + CARD_H - 1, locked ? 0xFF3A3F46 : info.tags().get(0).color());
                inset(g, x + 4, y + 2, 18, 18, 0xFF0E1216);
                if (locked) {
                    g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("lock"), x + 9, y + 6, 8, 9);
                } else {
                    g.item(pieceStack(suit, 0), x + 5, y + 3);
                }
                boolean fav = SuitCreatorPrefs.FAVORITES.contains(suit.id().getPath());
                String name = variantName(suit).getString();
                g.text(this.font, this.trim(name, w - 26 - 12), x + 26, y + 7, sel ? 0xFFFFD27A : locked ? 0xFF8A939E : WHITE, false);
                if (!locked && (fav || hover)) {
                    boolean starHover = in(mx, my, x + w - 10, y + 6, 8, 9);
                    g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite(fav || starHover ? "star_on" : "star_off"), x + w - 10, y + 7, 7, 7);
                }
                if (sel) {
                    g.outline(x, y, w, CARD_H - 1, AMBER);
                }
            }
        }
    }

    // ---- hologram ----

    private void hologramFrame(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean label) {
        monitor(g, x, y, w, h);
        brackets(g, x + 2, y + 2, w - 4, h - 4, CYAN_DIM);
        if (label) {
            g.text(this.font, tr("hologram"), x + 6, y + 5, CYAN_DIM, false);
            g.text(this.font, "360°", x + w - 6 - this.font.width("360°"), y + 5, CYAN_DIM, false);
        }
    }

    /** Beam, platform ring, the suit (or the locked ghost), scanline overlay. */
    private void drawHologram(GuiGraphicsExtractor g, int x, int y, int w, int h, float scale, @Nullable Suit suit, boolean locked) {
        int cx = x + w / 2;
        int feet = y + h - 4;
        // beam
        int bw = (int) (scale * 0.75F);
        g.fillGradient(cx - bw, y + 6, cx + bw, feet, 0x0059E4FF, 0x2259E4FF);
        // platform rings
        this.ring(g, cx, feet, (int) (scale * 0.62F), 3, 0xFF59E4FF);
        this.ring(g, cx, feet, (int) (scale * 0.45F), 2, 0x8859E4FF);
        if (suit == null || locked) {
            float pulse = 0.65F + 0.25F * Mth.sin(this.time * 0.15F);
            int gh = (int) (scale * 1.8F);
            int gw = gh * 24 / 32;
            int color = ((int) (pulse * 255) << 24) | 0xFFFFFF;
            g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("ghost"), cx - gw / 2, feet - gh, gw, gh, color);
            g.centeredText(this.font, Component.literal("?"), cx, feet - gh * 3 / 4, CYAN);
        } else {
            SuitHologram.draw(g, x + 2, y + 2, x + w - 2, feet, scale, suit, this.parts, SuitCreatorPrefs.slim, this.pose, this.yaw, this.time);
        }
    }

    private void ring(GuiGraphicsExtractor g, int cx, int cy, int rx, int ry, int color) {
        int lastHalf = -1;
        for (int dy = -ry; dy <= ry; dy++) {
            float f = 1.0F - (float) (dy * dy) / (ry * ry + 0.5F);
            int half = (int) (rx * Math.sqrt(Math.max(0.0F, f)));
            if (dy == -ry || dy == ry) {
                g.fill(cx - half, cy + dy, cx + half, cy + dy + 1, color);
            } else {
                int step = Math.max(1, half - Math.max(lastHalf, 0));
                g.fill(cx - half, cy + dy, cx - half + step, cy + dy + 1, color);
                g.fill(cx + half - step, cy + dy, cx + half, cy + dy + 1, color);
            }
            lastHalf = half;
        }
    }

    private void drawVariantRow(GuiGraphicsExtractor g, int x, int y, int w, int mx, int my) {
        Suit suit = this.selected;
        int[] pos = suit != null ? Suits.variantPosition(suit) : null;
        if (pos == null) {
            return;
        }
        boolean lh = in(mx, my, x + 4, y, 12, 11);
        boolean rh = in(mx, my, x + w - 16, y, 12, 11);
        this.arrow(g, x + 4, y, true, lh);
        this.arrow(g, x + w - 16, y, false, rh);
        Component label = tr("variant", pos[0] + 1, pos[1]);
        g.centeredText(this.font, label, x + w / 2, y + 2, WHITE);
    }

    private void arrow(GuiGraphicsExtractor g, int x, int y, boolean left, boolean hover) {
        g.fill(x, y, x + 12, y + 11, hover ? 0xAA2F8FA8 : 0x6612161B);
        int c = hover ? WHITE : CYAN;
        for (int i = 0; i < 4; i++) {
            int xx = left ? x + 4 + i : x + 7 - i;
            g.fill(xx, y + 5 - i, xx + 1, y + 6 + i, c);
        }
    }

    // ---- catalog ----

    private void drawCatalog(GuiGraphicsExtractor g, int x, int y, int mx, int my) {
        Suit suit = this.selected;
        boolean locked = suit == null || !this.unlocked(suit);
        int hx = x + CENTER_X;
        int hy = y + 20;
        this.hologramFrame(g, hx, hy, CENTER_W, 108, true);
        this.drawHologram(g, hx, hy, CENTER_W, 96, 40.0F, suit, locked);
        this.drawVariantRow(g, hx, hy + 95, CENTER_W, mx, my);

        // controls: Steve / Alex, pose, part toggles
        int cy = y + 131;
        this.button(g, hx, cy, 30, 14, tr("steve"), true, in(mx, my, hx, cy, 30, 14), !SuitCreatorPrefs.slim);
        this.button(g, hx + 31, cy, 32, 14, tr("alex"), true, in(mx, my, hx + 31, cy, 32, 14), SuitCreatorPrefs.slim);
        this.button(g, hx + 65, cy, 16, 14, Component.empty(), true, in(mx, my, hx + 65, cy, 16, 14), false);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("pose_" + this.pose.name().toLowerCase(Locale.ROOT)), hx + 68, cy + 2, 10, 10);
        for (int i = 0; i < 4; i++) {
            int px = hx + 83 + i * 16;
            boolean hov = in(mx, my, px, cy, 15, 14);
            inset(g, px, cy, 15, 14, hov ? 0xFF323A44 : 0xFF232931);
            g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("part_" + PART_SPRITES[i] + (this.parts[i] ? "_on" : "_off")), px + 3, cy + 2, 10, 10);
        }

        // blueprint
        int rx = x + RIGHT_X;
        Component pieceName = tr(this.piece == SuitCatalog.ALL ? "part.all" : "part." + this.piece);
        g.text(this.font, tr("blueprint", pieceName), rx, y + 21, LABEL, false);
        for (int i = 0; i < 4; i++) {
            int sx = rx + i * 19;
            g.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("container/slot"), sx, y + 31, 18, 18);
            if (suit != null) {
                if (locked) {
                    g.fill(sx + 1, y + 32, sx + 17, y + 48, 0xFF5A5A5A);
                    g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("part_" + PART_SPRITES[i] + "_off"), sx + 4, y + 35, 10, 10);
                } else {
                    g.item(pieceStack(suit, i), sx + 1, y + 32);
                }
            }
            if (this.piece == i) {
                g.outline(sx - 1, y + 30, 20, 20, AMBER);
            }
        }
        int allX = rx + 77;
        this.button(g, allX, y + 31, RIGHT_W - 77, 18, tr("all"), true, in(mx, my, allX, y + 31, RIGHT_W - 77, 18), this.piece == SuitCatalog.ALL);

        // materials have / need
        monitor(g, rx, y + 52, RIGHT_W, 68);
        if (suit != null) {
            List<SuitInfo.Material> cost = SuitCatalog.cost(suit, this.piece);
            int max = this.maxMaterialScroll(cost.size());
            this.materialScroll = Mth.clamp(this.materialScroll, 0, max);
            boolean bar = max > 0;
            int listW = RIGHT_W - (bar ? 7 : 0);
            int top = y + MAT_Y + 2;
            // clipped to the monitor; more than 4 materials scroll (mouse wheel or the bar on the right)
            g.enableScissor(rx + 1, top, rx + RIGHT_W - 1, top + MAT_VISIBLE);
            int row = 0;
            for (SuitInfo.Material m : cost) {
                int ry = top + row * MAT_ROW - this.materialScroll;
                row++;
                if (ry + MAT_ROW <= top || ry >= top + MAT_VISIBLE) {
                    continue;
                }
                ItemStack icon = new ItemStack(m.item().get());
                g.item(icon, rx + 3, ry);
                int ty = ry + 4;
                int have = SuitCreatorMenu.count(this.minecraft.player.getInventory(), m.item().get());
                String count = have + "/" + m.count();
                int cw = this.font.width(count);
                g.text(this.font, this.trim(icon.getHoverName().getString(), listW - 26 - cw - 4), rx + 21, ty, 0xFFD8DEE6, false);
                g.text(this.font, count, rx + listW - 4 - cw, ty, have >= m.count() ? GREEN : RED, false);
            }
            g.disableScissor();
            if (bar) {
                int bx = rx + RIGHT_W - 6;
                g.fill(bx, top, bx + 4, top + MAT_VISIBLE, 0xFF0A0D10);
                int content = cost.size() * MAT_ROW;
                int thumb = Math.max(10, MAT_VISIBLE * MAT_VISIBLE / content);
                int pos = (MAT_VISIBLE - thumb) * this.materialScroll / max;
                boolean hot = this.draggingMaterialBar || in(mx, my, bx, top, 4, MAT_VISIBLE);
                g.fill(bx, top + pos, bx + 4, top + pos + thumb, hot ? 0xFF8A96A2 : 0xFF6F7A86);
                g.fill(bx, top + pos, bx + 3, top + pos + thumb - 1, hot ? 0xFFC8D2DC : 0xFFA9B4C0);
            }
        }

        // armor + durability
        if (suit != null) {
            int ay = y + 123;
            int armor = 0;
            for (int i = 0; i < 4; i++) {
                if (this.piece == SuitCatalog.ALL || this.piece == i) {
                    armor += suit.material().defense().getOrDefault(PIECES[i], 0);
                }
            }
            g.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("hud/armor_full"), rx, ay, 9, 9);
            g.text(this.font, "+" + armor, rx + 11, ay + 1, LABEL, false);
            if (this.piece != SuitCatalog.ALL) {
                Component dur = tr("durability", pieceStack(suit, this.piece).getMaxDamage());
                g.text(this.font, dur, rx + RIGHT_W - this.font.width(dur), ay + 1, LABEL, false);
            } else {
                Component pcs = tr("pieces", 4);
                g.text(this.font, pcs, rx + RIGHT_W - this.font.width(pcs), ay + 1, LABEL, false);
            }
            // the suit's tags (known even while it is locked): full size if they fit in one row, otherwise
            // 3/4-size chips in up to two rows, so every tag shows without crowding the menu
            this.drawTagRow(g, rx, y + 134, RIGHT_W, SuitCatalog.info(suit).tags());
        }

        this.drawSewPanel(g, x + SEW_X, y + SEW_Y, mx, my, suit, locked);
    }

    /** The big Sew panel (or "Unlock" for experience levels while the suit is locked). */
    private void drawSewPanel(GuiGraphicsExtractor g, int x, int y, int mx, int my, @Nullable Suit suit, boolean locked) {
        if (suit == null) {
            return;
        }
        boolean hover = in(mx, my, x, y, SEW_W, SEW_H) && !this.tagsOpen;
        if (locked) {
            SuitInfo info = SuitCatalog.info(suit);
            boolean creative = this.minecraft.player.hasInfiniteMaterials();
            boolean can = creative || this.minecraft.player.experienceLevel >= info.xpCost();
            int frame = can ? (hover ? 0xFFB6FF7A : 0xFF7CFC6A) : 0xFF5C646E;
            g.fill(x, y, x + SEW_W, y + SEW_H, 0xFF000000);
            g.fillGradient(x + 1, y + 1, x + SEW_W - 1, y + SEW_H - 1, can ? 0xFF1E3418 : 0xFF23272D, can ? 0xFF10200C : 0xFF181B20);
            g.outline(x + 1, y + 1, SEW_W - 2, SEW_H - 2, frame);
            this.bigText(g, tr("unlock"), x + SEW_W / 2, y + 7, can ? 0xFFB6FF7A : 0xFF8A9099, 1.5F);
            g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("xp_orb"), x + 22, y + 29, 9, 9);
            Component cost = tr("unlock.cost", info.xpCost());
            g.text(this.font, cost, x + 34, y + 30, can ? GREEN : RED, true);
            Component scan = tr("unlock.or_scan");
            g.centeredText(this.font, scan, x + SEW_W / 2, y + 46, 0xFFB8C2CC);
            g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("lock"), x + SEW_W / 2 - 4, y + 60, 8, 9, can ? 0xFFB6FF7A : 0xFF8A9099);
            return;
        }
        List<SuitInfo.Material> cost = SuitCatalog.cost(suit, this.piece);
        boolean creative = this.minecraft.player.hasInfiniteMaterials();
        boolean can = creative || SuitCreatorMenu.hasMaterials(this.minecraft.player.getInventory(), cost);
        boolean sewing = this.sewTicks >= 0;
        float glow = 0.5F + 0.5F * Mth.sin(this.time * 0.2F);
        int frame = !can ? 0xFF6A4040 : sewing || hover ? 0xFFFFD27A : lerpColor(0xFFC98A1E, AMBER, glow);
        g.fill(x, y, x + SEW_W, y + SEW_H, 0xFF000000);
        g.fillGradient(x + 1, y + 1, x + SEW_W - 1, y + SEW_H - 1, can ? (hover ? 0xFF4A3412 : 0xFF3A2810) : 0xFF2A2224,
                can ? 0xFF1E1408 : 0xFF181416);
        g.outline(x + 1, y + 1, SEW_W - 2, SEW_H - 2, frame);
        g.outline(x + 2, y + 2, SEW_W - 4, SEW_H - 4, can ? AMBER_DARK : 0xFF3A2A2A);
        Component sewTitle = tr("sew");
        float sewScale = Math.min(2.0F, (SEW_W - 10) / (float) this.font.width(sewTitle.copy().withStyle(net.minecraft.ChatFormatting.BOLD)));
        this.bigText(g, sewTitle, x + SEW_W / 2, y + 6 + (int) ((2.0F - sewScale) * 4), can ? (hover ? 0xFFFFE2A8 : AMBER) : 0xFF8A7070, sewScale);
        // result icon (2x) and what it makes
        g.pose().pushMatrix();
        g.pose().translate(x + 8, y + 26);
        g.pose().scale(2.0F, 2.0F);
        g.item(pieceStack(suit, this.piece == SuitCatalog.ALL ? 1 : this.piece), 0, 0);
        g.pose().popMatrix();
        Component what = tr(this.piece == SuitCatalog.ALL ? "part.all" : "part." + this.piece);
        List<FormattedCharSequence> lines = this.font.split(what, SEW_W - 48);
        int ty = y + 30;
        for (FormattedCharSequence line : lines) {
            g.text(this.font, line, x + 44, ty, can ? WHITE : 0xFF9A8A8A, false);
            ty += 10;
        }
        g.text(this.font, "×" + (this.piece == SuitCatalog.ALL ? 4 : 1), x + 44, ty, can ? 0xFFFFD27A : 0xFF7A6A6A, false);
        // progress bar
        int bx = x + 7;
        int by = y + SEW_H - 11;
        int bw = SEW_W - 14;
        g.fill(bx, by, bx + bw, by + 5, 0xFF0A0806);
        if (sewing) {
            int fillW = bw * Math.min(SEW_TICKS, this.sewTicks) / SEW_TICKS;
            g.fill(bx, by, bx + fillW, by + 5, AMBER);
            g.fill(bx, by, bx + fillW, by + 1, 0xFFFFE2A8);
            g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("needle"), bx + fillW - 5, by - 6, 10, 10);
        } else if (can) {
            g.fill(bx, by + 2, bx + bw, by + 3, 0x55FFB13B);
        }
    }

    /** How far the blueprint's material list can scroll (4 rows of 16 px are visible). */
    private int maxMaterialScroll(int rows) {
        return Math.max(0, rows * MAT_ROW - MAT_VISIBLE);
    }

    private void setMaterialScrollFromBar(double my, int rows) {
        int top = this.topPos + MAT_Y + 2;
        int max = this.maxMaterialScroll(rows);
        this.materialScroll = Mth.clamp((int) Math.round((my - top) / MAT_VISIBLE * (max + MAT_VISIBLE) - MAT_VISIBLE / 2.0), 0, max);
    }

    private void bigText(GuiGraphicsExtractor g, Component text, int cx, int y, int color, float scale) {
        g.pose().pushMatrix();
        g.pose().translate(cx, y);
        g.pose().scale(scale, scale);
        Component bold = text.copy().withStyle(net.minecraft.ChatFormatting.BOLD);
        g.text(this.font, bold, -this.font.width(bold) / 2, 0, color, true);
        g.pose().popMatrix();
    }

    private static int lerpColor(int a, int b, float t) {
        int r = (int) Mth.lerp(t, (a >> 16) & 0xFF, (b >> 16) & 0xFF);
        int gg = (int) Mth.lerp(t, (a >> 8) & 0xFF, (b >> 8) & 0xFF);
        int bl = (int) Mth.lerp(t, a & 0xFF, b & 0xFF);
        return 0xFF000000 | r << 16 | gg << 8 | bl;
    }

    // ---- scanner ----

    private void drawScanner(GuiGraphicsExtractor g, int x, int y, int mx, int my) {
        int sx = x + CENTER_X;
        int sy = y + 20;
        monitor(g, sx, sy, CENTER_W, 128);
        brackets(g, sx + 2, sy + 2, CENTER_W - 4, 124, AMBER_DARK);
        g.text(this.font, tr("scanner"), sx + 6, sy + 5, AMBER_DARK, false);
        int slotX = x + SuitCreatorMenu.SCAN_X;
        int slotY = y + SuitCreatorMenu.SCAN_Y;
        g.fill(slotX - 6, slotY - 6, slotX + 22, slotY + 22, 0xFF1D232A);
        g.outline(slotX - 6, slotY - 6, 28, 28, 0xFF3A4652);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, Identifier.withDefaultNamespace("container/slot"), slotX - 1, slotY - 1, 18, 18);

        ItemStack stack = this.menu.scanItem();
        Suit target = SuitUnlocks.scanTarget(stack);
        int state = this.menu.scanState();
        int progress = this.menu.scanProgress();
        boolean scanning = state == SuitCreatorMenu.STATE_SCANNING;
        if (scanning) {
            float phase = (Mth.sin(this.time * 0.25F) + 1.0F) / 2.0F;
            int ly = slotY - 4 + (int) (phase * 22);
            g.fill(slotX - 8, ly, slotX + 24, ly + 1, 0xFFFFB13B);
            g.fill(slotX - 8, ly - 1, slotX + 24, ly, 0x55FFB13B);
            g.fill(slotX - 8, ly + 1, slotX + 24, ly + 2, 0x55FFB13B);
        }
        String key = switch (state) {
            case SuitCreatorMenu.STATE_SCANNING -> "scan.scanning";
            case SuitCreatorMenu.STATE_KNOWN -> "scan.known";
            case SuitCreatorMenu.STATE_INVALID -> "scan.invalid";
            default -> "scan.empty";
        };
        int cx = sx + CENTER_W / 2;
        g.centeredText(this.font, tr(key), cx, sy + 52, scanning ? WHITE : 0xFFB8C2CC);
        int bx = sx + 12;
        int bw = CENTER_W - 24;
        g.fill(bx, sy + 66, bx + bw, sy + 72, 0xFF0A0D10);
        g.outline(bx - 1, sy + 65, bw + 2, 8, 0xFF3A4652);
        int fillW = scanning ? bw * progress / SuitCreatorMenu.SCAN_TICKS : state == SuitCreatorMenu.STATE_KNOWN ? bw : 0;
        g.fill(bx, sy + 66, bx + fillW, sy + 72, state == SuitCreatorMenu.STATE_KNOWN ? CYAN_DIM : AMBER);
        g.centeredText(this.font, Component.literal((fillW * 100 / bw) + "%"), cx, sy + 76, scanning ? AMBER : GREY);

        // the sample's suit
        g.fill(sx + 8, sy + 94, sx + CENTER_W - 8, sy + 95, 0xFF2A3A46);
        g.text(this.font, tr("sample"), sx + 8, sy + 100, 0xFF8A939E, false);
        Component sample = target == null ? Component.literal("—")
                : scanning ? Component.literal("???") : SuitCreatorClient.displayName(target);
        g.textWithWordWrap(this.font, sample, sx + 8, sy + 110, CENTER_W - 16, target == null ? DIM : scanning ? CYAN : AMBER, false);

        // decoding ghost / just unlocked suit
        int gx = x + RIGHT_X;
        int gy = y + 20;
        this.hologramFrame(g, gx, gy, RIGHT_W, 128, false);
        Suit recent = SuitCreatorClient.recentlyUnlocked(120);
        if (recent != null) {
            g.centeredText(this.font, tr("unlocked"), gx + RIGHT_W / 2, gy + 6, GREEN);
            this.drawHologram(g, gx, gy + 16, RIGHT_W, 92, 38.0F, recent, false);
            g.centeredText(this.font, Component.literal(this.trim(variantName(recent).getString(), RIGHT_W - 8)), gx + RIGHT_W / 2, gy + 114, AMBER);
        } else {
            this.drawHologram(g, gx, gy + 4, RIGHT_W, 104, 46.0F, null, true);
            Component label = scanning
                    ? tr("decoding", progress * 100 / SuitCreatorMenu.SCAN_TICKS) : tr("decoding.idle");
            g.centeredText(this.font, label, gx + RIGHT_W / 2, gy + 114, CYAN);
        }

        // hint
        monitor(g, x + SEW_X, y + SEW_Y, SEW_W, SEW_H);
        g.textWithWordWrap(this.font, tr("hint"), x + SEW_X + 4, y + SEW_Y + 4, SEW_W - 8, 0xFF9AA8B4, false);
    }

    // ---- tags popup ----

    private int[] popupBounds() {
        return new int[]{this.leftPos + 8, this.topPos + 36, 280, this.popupHeight()};
    }

    private int popupHeight() {
        int rowsUsed = this.layoutChips(null, 0, 0, -1, -1);
        return 20 + rowsUsed * 14 + 26;
    }

    /** Lays out (and draws, if {@code g != null}) the tag chips; returns the number of rows. */
    private int layoutChips(@Nullable GuiGraphicsExtractor g, int x0, int y0, int mx, int my) {
        int maxW = 268;
        int cx = 0;
        int row = 0;
        for (SuitTag tag : SuitTag.values()) {
            int w = this.font.width(tag.title()) + 6;
            if (cx + w > maxW) {
                cx = 0;
                row++;
            }
            if (g != null) {
                boolean on = SuitCreatorPrefs.TAGS.contains(tag);
                int x = x0 + cx;
                int y = y0 + row * 14;
                this.chip(g, x, y, tag, on);
                if (in(mx, my, x, y, w, 11)) {
                    g.outline(x - 1, y - 1, w + 2, 13, WHITE);
                }
            }
            cx += w + 3;
        }
        return row + 1;
    }

    private @Nullable SuitTag chipAt(int x0, int y0, double mx, double my) {
        int maxW = 268;
        int cx = 0;
        int row = 0;
        for (SuitTag tag : SuitTag.values()) {
            int w = this.font.width(tag.title()) + 6;
            if (cx + w > maxW) {
                cx = 0;
                row++;
            }
            if (in(mx, my, x0 + cx, y0 + row * 14, w, 11)) {
                return tag;
            }
            cx += w + 3;
        }
        return null;
    }

    private void drawTagsPopup(GuiGraphicsExtractor g, int mx, int my) {
        int[] b = this.popupBounds();
        g.fill(b[0] - 1, b[1] - 1, b[0] + b[2] + 1, b[1] + b[3] + 1, 0xFF000000);
        g.fill(b[0], b[1], b[0] + b[2], b[1] + b[3], 0xF01A1F26);
        g.outline(b[0], b[1], b[2], b[3], AMBER_DARK);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite("tag_icon"), b[0] + 6, b[1] + 6, 7, 7);
        g.text(this.font, tr("tags.title"), b[0] + 16, b[1] + 6, AMBER, false);
        String count = SuitCreatorPrefs.TAGS.size() + " / " + SuitTag.values().length;
        g.text(this.font, count, b[0] + b[2] - 6 - this.font.width(count), b[1] + 6, GREY, false);
        this.layoutChips(g, b[0] + 6, b[1] + 20, mx, my);
        int by = b[1] + b[3] - 22;
        int bw = (b[2] - 18) / 2;
        this.button(g, b[0] + 6, by, bw, 16, tr("tags.all"), true, in(mx, my, b[0] + 6, by, bw, 16), false);
        this.button(g, b[0] + 12 + bw, by, bw, 16, tr("tags.none"), true, in(mx, my, b[0] + 12 + bw, by, bw, 16), false);
    }

    // ---- tooltips ----

    private void tooltips(GuiGraphicsExtractor g, int mx, int my) {
        int x = this.leftPos;
        int y = this.topPos;
        int bx = x + W - 8 - 78;
        if (in(mx, my, bx, y + 4, 39, 14) || in(mx, my, x + 96, y + 20, 40, 14)) {
            g.setTooltipForNextFrame(this.font, tr("tags.count", SuitCreatorPrefs.TAGS.size(), SuitTag.values().length), mx, my);
        } else if (in(mx, my, bx + 39, y + 4, 39, 14)) {
            g.setTooltipForNextFrame(this.font, tr("unlocked.count", this.progress().unlockedCount(), Suits.ALL.size()), mx, my);
        }
        Suit suit = this.selected;
        if (suit == null) {
            return;
        }
        boolean locked = !this.unlocked(suit);
        if (this.tab == TAB_CATALOG) {
            int cy = y + 131;
            int hx = x + CENTER_X;
            if (in(mx, my, hx + 65, cy, 16, 14)) {
                g.setTooltipForNextFrame(this.font, tr("pose", tr("pose." + this.pose.name().toLowerCase(Locale.ROOT))), mx, my);
            }
            for (int i = 0; i < 4; i++) {
                if (in(mx, my, hx + 83 + i * 16, cy, 15, 14)) {
                    g.setTooltipForNextFrame(this.font, tr(this.parts[i] ? "part.hide" : "part.show", tr("part." + i)), mx, my);
                }
            }
            for (int i = 0; i < 4; i++) {
                if (this.unlocked(suit) && in(mx, my, x + RIGHT_X + i * 19, y + 31, 18, 18)) {
                    g.setTooltipForNextFrame(this.font, pieceStack(suit, i), mx, my);
                }
            }
            List<SuitInfo.Material> cost = SuitCatalog.cost(suit, this.piece);
            int top = y + MAT_Y + 2;
            for (int i = 0; i < cost.size(); i++) {
                int ry = top + i * MAT_ROW - this.materialScroll;
                int y0 = Math.max(ry, top);
                int y1 = Math.min(ry + MAT_ROW, top + MAT_VISIBLE);
                if (y1 > y0 && in(mx, my, x + RIGHT_X + 2, y0, RIGHT_W - 10, y1 - y0)) {
                    g.setTooltipForNextFrame(this.font, new ItemStack(cost.get(i).item().get()), mx, my);
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
        int x = this.leftPos;
        int y = this.topPos;
        if (this.tagsOpen) {
            int[] b = this.popupBounds();
            if (!in(mx, my, b[0], b[1], b[2], b[3])) {
                this.tagsOpen = false;
                SuitCreatorPrefs.save();
                // a click on the Tags button only closes it
                if (in(mx, my, x + 96, y + 20, 40, 14)) {
                    this.click();
                    return true;
                }
                return true;
            }
            SuitTag tag = this.chipAt(b[0] + 6, b[1] + 20, mx, my);
            if (tag != null) {
                if (!SuitCreatorPrefs.TAGS.remove(tag)) {
                    SuitCreatorPrefs.TAGS.add(tag);
                }
                this.listScroll = 0;
                this.click();
                return true;
            }
            int by = b[1] + b[3] - 22;
            int bw = (b[2] - 18) / 2;
            if (in(mx, my, b[0] + 6, by, bw, 16)) {
                SuitCreatorPrefs.TAGS.addAll(List.of(SuitTag.values()));
                this.click();
            } else if (in(mx, my, b[0] + 12 + bw, by, bw, 16)) {
                SuitCreatorPrefs.TAGS.clear();
                this.click();
            }
            return true;
        }
        if (!in(mx, my, this.search.getX(), this.search.getY(), this.search.getWidth(), this.search.getHeight())) {
            this.search.setFocused(false);
            if (this.getFocused() == this.search) {
                this.setFocused(null);
            }
        }
        if (event.button() == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT && this.clickUi(mx, my, x, y)) {
            return true;
        }
        if (event.button() == com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_RIGHT && this.rightClickList(mx, my, x, y)) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean clickUi(double mx, double my, int x, int y) {
        // tabs
        for (int i = 0; i < TABS.length; i++) {
            int[] b = this.tabBounds(i);
            if (in(mx, my, b[0], b[1], b[2], b[3])) {
                if (this.tab != TABS[i]) {
                    this.tab = TABS[i];
                    this.applyTab();
                }
                this.click();
                return true;
            }
        }
        // tags button
        if (in(mx, my, x + 96, y + 20, 40, 14)) {
            this.tagsOpen = true;
            this.click();
            return true;
        }
        // list
        int ix = x + LIST_X + 2;
        int iy = y + LIST_Y + 2;
        int iw = LIST_W - 10;
        int ih = LIST_H - 4;
        if (in(mx, my, ix, iy, iw, ih)) {
            int ry = iy - this.listScroll;
            for (Row row : this.rows) {
                if (my >= ry && my < ry + row.height()) {
                    if (row.kind == Row.HEADER) {
                        if (!SuitCreatorPrefs.COLLAPSED.remove(row.group)) {
                            SuitCreatorPrefs.COLLAPSED.add(row.group);
                        }
                        SuitCreatorPrefs.save();
                        this.click();
                    } else if (row.suit != null) {
                        if (row.kind == Row.CARD && in(mx, my, ix + iw - 11, ry + 5, 10, 11)) {
                            this.toggleFavorite(row.suit);
                        } else {
                            this.select(row.suit);
                            this.click();
                        }
                    }
                    return true;
                }
                ry += row.height();
            }
            return true;
        }
        // scrollbar drag-free click: jump
        if (in(mx, my, x + LIST_X + LIST_W - 7, iy, 4, ih)) {
            this.listScroll = Mth.clamp((int) ((my - iy) / ih * (this.maxScroll() + ih) - ih / 2.0), 0, this.maxScroll());
            return true;
        }
        Suit suit = this.selected;
        if (suit == null) {
            return false;
        }
        int hx = x + CENTER_X;
        // variant arrows
        int vy = y + 20 + 95;
        if (this.tab != TAB_SCANNER && Suits.variantPosition(suit) != null) {
            if (in(mx, my, hx + 4, vy, 12, 11)) {
                this.select(Suits.variant(suit, -1));
                this.click();
                return true;
            }
            if (in(mx, my, hx + CENTER_W - 16, vy, 12, 11)) {
                this.select(Suits.variant(suit, 1));
                this.click();
                return true;
            }
        }
        if (this.tab == TAB_CATALOG) {
            int cy = y + 131;
            if (in(mx, my, hx, cy, 30, 14) || in(mx, my, hx + 31, cy, 32, 14)) {
                SuitCreatorPrefs.slim = mx >= hx + 31;
                this.click();
                return true;
            }
            if (in(mx, my, hx + 65, cy, 16, 14)) {
                this.pose = this.pose.next();
                this.click();
                return true;
            }
            for (int i = 0; i < 4; i++) {
                if (in(mx, my, hx + 83 + i * 16, cy, 15, 14)) {
                    this.parts[i] = !this.parts[i];
                    this.click();
                    return true;
                }
            }
            for (int i = 0; i < 4; i++) {
                if (in(mx, my, x + RIGHT_X + i * 19, y + 31, 18, 18)) {
                    this.piece = this.piece == i ? SuitCatalog.ALL : i;
                    this.materialScroll = 0;
                    this.sewTicks = -1;
                    this.click();
                    return true;
                }
            }
            if (in(mx, my, x + RIGHT_X + 77, y + 31, RIGHT_W - 77, 18)) {
                this.piece = SuitCatalog.ALL;
                this.materialScroll = 0;
                this.sewTicks = -1;
                this.click();
                return true;
            }
            int rows = SuitCatalog.cost(suit, this.piece).size();
            if (this.maxMaterialScroll(rows) > 0 && in(mx, my, x + RIGHT_X + RIGHT_W - 7, y + MAT_Y + 2, 6, MAT_VISIBLE)) {
                this.draggingMaterialBar = true;
                this.setMaterialScrollFromBar(my, rows);
                return true;
            }
            if (in(mx, my, x + SEW_X, y + SEW_Y, SEW_W, SEW_H)) {
                this.clickSew(suit);
                return true;
            }
            if (in(mx, my, hx, y + 20, CENTER_W, 94)) {
                this.dragging = true;
                return true;
            }
        }
        return false;
    }

    private boolean rightClickList(double mx, double my, int x, int y) {
        int ix = x + LIST_X + 2;
        int iy = y + LIST_Y + 2;
        if (!in(mx, my, ix, iy, LIST_W - 10, LIST_H - 4)) {
            return false;
        }
        int ry = iy - this.listScroll;
        for (Row row : this.rows) {
            if (my >= ry && my < ry + row.height() && row.kind == Row.CARD && row.suit != null) {
                this.toggleFavorite(row.suit);
                return true;
            }
            ry += row.height();
        }
        return true;
    }

    private void toggleFavorite(Suit suit) {
        String id = suit.id().getPath();
        if (!SuitCreatorPrefs.FAVORITES.remove(id)) {
            SuitCreatorPrefs.FAVORITES.add(id);
            this.playSound(SoundEvents.AMETHYST_BLOCK_CHIME, 1.6F);
        } else {
            this.click();
        }
        SuitCreatorPrefs.save();
    }

    private void clickSew(Suit suit) {
        int index = Suits.ALL.indexOf(suit);
        if (!this.unlocked(suit)) {
            SuitInfo info = SuitCatalog.info(suit);
            if (this.minecraft.player.hasInfiniteMaterials() || this.minecraft.player.experienceLevel >= info.xpCost()) {
                this.minecraft.gameMode.handleInventoryButtonClick(this.menu.containerId, SuitCreatorMenu.UNLOCK_BUTTON + index);
                this.click();
            } else {
                this.playSound(SoundEvents.VILLAGER_NO, 1.2F);
            }
            return;
        }
        if (this.sewTicks >= 0) {
            return;
        }
        boolean can = this.minecraft.player.hasInfiniteMaterials()
                || SuitCreatorMenu.hasMaterials(this.minecraft.player.getInventory(), SuitCatalog.cost(suit, this.piece));
        if (!can) {
            this.playSound(SoundEvents.VILLAGER_NO, 1.2F);
            return;
        }
        this.sewButton = index * 8 + this.piece;
        this.sewTicks = 0;
        this.click();
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (this.draggingMaterialBar && this.selected != null) {
            this.setMaterialScrollFromBar(event.y(), SuitCatalog.cost(this.selected, this.piece).size());
            return true;
        }
        if (this.dragging) {
            this.yaw -= (float) dx * 2.5F;
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.draggingMaterialBar) {
            this.draggingMaterialBar = false;
            return true;
        }
        if (this.dragging) {
            this.dragging = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (!this.tagsOpen && in(mx, my, this.leftPos + LIST_X, this.topPos + LIST_Y, LIST_W, LIST_H)) {
            this.listScroll = Mth.clamp(this.listScroll - (int) (scrollY * 14), 0, this.maxScroll());
            return true;
        }
        if (!this.tagsOpen && this.tab == TAB_CATALOG && this.selected != null
                && in(mx, my, this.leftPos + RIGHT_X, this.topPos + MAT_Y, RIGHT_W, 68)) {
            int rows = SuitCatalog.cost(this.selected, this.piece).size();
            this.materialScroll = Mth.clamp(this.materialScroll - (int) (scrollY * 8), 0, this.maxMaterialScroll(rows));
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.tagsOpen && event.isEscape()) {
            this.tagsOpen = false;
            SuitCreatorPrefs.save();
            return true;
        }
        if (this.search.isFocused() && !event.isEscape()) {
            // typing in the search box: don't let E / number keys close the screen or move items
            this.search.keyPressed(event);
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void removed() {
        SuitCreatorPrefs.save();
        super.removed();
    }

    // ---- CI / screenshots ----

    /** Dev/CI only: a real left click in the middle of tab {@code tab}. */
    public void debugClickTab(int tab) {
        int[] b = this.tabBounds(tab == TAB_SCANNER ? 1 : 0);
        this.mouseClicked(new MouseButtonEvent(b[0] + b[2] / 2.0, b[1] + b[3] / 2.0,
                new net.minecraft.client.input.MouseButtonInfo(com.mojang.blaze3d.platform.InputConstants.MOUSE_BUTTON_LEFT, 0)), false);
    }

    /** Dev/CI only: a mouse wheel turn over the blueprint's material list. */
    public void debugScrollMaterials(double amount) {
        this.mouseScrolled(this.leftPos + RIGHT_X + 20, this.topPos + MAT_Y + 20, 0.0, amount);
    }

    /** Dev/CI only: presses the Sew panel. */
    public void debugSew() {
        if (this.selected != null) {
            this.clickSew(this.selected);
        }
    }

    /** Dev/CI only: sets up the screen for a screenshot. */
    public void debugSetup(int tab, @Nullable String suit, int piece, boolean tagsOpen, SuitHologram.Pose pose, boolean slim) {
        this.tab = tab == TAB_SCANNER ? TAB_SCANNER : TAB_CATALOG;
        this.applyTab();
        if (suit != null && Suits.byName(suit) != null) {
            this.select(Suits.byName(suit));
        }
        this.piece = piece;
        this.tagsOpen = tagsOpen;
        this.pose = pose;
        SuitCreatorPrefs.slim = slim;
    }
}
