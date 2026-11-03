package com.star_1mp.powersunchained.client.power;

import com.mojang.blaze3d.platform.InputConstants;
import com.star_1mp.powersunchained.client.ability.AbilityHud;
import com.star_1mp.powersunchained.client.ability.AbilityHudConfig;
import com.star_1mp.powersunchained.client.ability.AbilityHudConfigScreen;
import com.star_1mp.powersunchained.client.ability.AbilitySlots;
import com.star_1mp.powersunchained.client.input.PUKeyMappings;
import com.star_1mp.powersunchained.network.SkillBuyPayload;
import com.star_1mp.powersunchained.platform.Services;
import com.star_1mp.powersunchained.power.PowerLevels;
import com.star_1mp.powersunchained.power.PowerProgress;
import com.star_1mp.powersunchained.power.PowerSkills;
import com.star_1mp.powersunchained.power.Superpower;
import com.star_1mp.powersunchained.power.Superpowers;
import com.star_1mp.powersunchained.power.skill.SkillBranch;
import com.star_1mp.powersunchained.power.skill.SkillNode;
import com.star_1mp.powersunchained.power.skill.SkillTrees;
import com.star_1mp.powersunchained.Constants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/**
 * The powers menu (key J / inventory button), in the style of the Suit Creator / Tech Table, 416x236. Tabs:
 * <ul>
 *   <li>Характеристики: left = what the player has right now (powers, worn suits, worn gadgets — gone when taken off),
 *   centre = the player as a hologram + the selected power's level bar, right = the player's stats;</li>
 *   <li>Умения: the selected power's skill tree (Palladium-like, three branches under the player), bought with that
 *   power's own skill points (experience from using it -> levels -> 1 point a level); gadgets are upgraded on the Tech
 *   Table;</li>
 *   <li>Клавиши: drag abilities onto the 5 keys under the hologram, the HUD preview, "Automatic".</li>
 * </ul>
 * The skill-point counter of the selected power is in the top bar on every tab.
 */
public class PowersMenuScreen extends Screen {

    public static final int TAB_STATS = 0;
    public static final int TAB_SKILLS = 1;
    public static final int TAB_KEYS = 2;
    private static final String[] TAB_KEYS_LANG = {"tab.stats", "tab.skills", "tab.keys"};

    private static final int W = 416;
    private static final int H = 236;
    private static final int LIST_X = 8;
    private static final int LIST_Y = 21;
    private static final int LIST_W = 128;
    private static final int LIST_H = 207;
    private static final int SEARCH_H = 14;
    private static final int LIST_TOP = LIST_Y + 3 + SEARCH_H + 3;
    private static final int CENTER_X = 140;
    private static final int CENTER_W = 162;
    private static final int RIGHT_X = 306;
    private static final int RIGHT_W = 102;
    private static final int CARD_H = 22;
    /** Hologram box (Stats / Keys), as big as the Suit Creator's. */
    private static final int HOLO_Y = 20;
    private static final int HOLO_H = 150;
    private static final int HOLO_SCALE = 48;

    /** Optimisation: icons are made once, not a new ItemStack every frame. */
    private static final Map<Item, ItemStack> ICONS = new HashMap<>();
    private static final Map<String, Identifier> ABILITY_ICONS = new HashMap<>();

    private static ItemStack icon(Item item) {
        return ICONS.computeIfAbsent(item, ItemStack::new);
    }

    private static final int LABEL = 0xFF404040;
    private static final int COPPER = 0xFFFF9A52;
    private static final int COPPER_DARK = 0xFF9C5426;
    private static final int AMBER = 0xFFFFB13B;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GREY = 0xFF9AA3AD;
    private static final int DIM = 0xFF5C646E;
    private static final int GREEN = 0xFF7CFC6A;
    private static final int RED = 0xFFFF5A4A;
    private static final int GLASS = 0xFF12161B;
    private static final int CYAN = 0xFF59E4FF;
    private static final int CYAN_DIM = 0xFF2F8FA8;

    private static int lastTab = TAB_STATS;
    private static @Nullable String lastSelected;

    private final @Nullable Screen parent;
    private int tab;
    private @Nullable String selected;
    private @Nullable String selectedNode;
    private @Nullable EditBox search;
    private int listScroll;
    private float yaw = -20.0F;
    private boolean rotating;
    private float time;
    private int failFlash;
    private String failKey = "skill.not_enough";
    /** Ability being dragged onto a key (Keys tab). */
    private @Nullable String dragging;

    public PowersMenuScreen(@Nullable Screen parent) {
        this(parent, lastTab);
    }

    public PowersMenuScreen(@Nullable Screen parent, int tab) {
        super(Component.translatable("screen.powers_unchained.powers_menu"));
        this.parent = parent;
        this.tab = tab;
        this.selected = lastSelected;
    }

    private static Component tr(String key, Object... args) {
        return Component.translatable("screen.powers_unchained.powers_menu." + key, args);
    }

    private int left() {
        return (this.width - W) / 2;
    }

    private int top() {
        return (this.height - H) / 2;
    }

    // =====================================================================================================
    // setup
    // =====================================================================================================

    @Override
    protected void init() {
        String old = this.search != null ? this.search.getValue() : "";
        this.search = new EditBox(this.font, this.left() + LIST_X + 3, this.top() + LIST_Y + 3, LIST_W - 6, SEARCH_H, tr("search"));
        this.search.setHint(tr("search").copy().withColor(0xFF6F7780));
        this.search.setMaxLength(32);
        this.search.setValue(old);
        this.search.setResponder(v -> this.listScroll = 0);
        this.addRenderableWidget(this.search);
    }

    private void setTab(int tab) {
        if (tab != this.tab && this.search != null) {
            this.search.setValue("");
        }
        this.tab = tab;
        lastTab = tab;
        this.listScroll = 0;
        this.dragging = null;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.failFlash > 0) {
            this.failFlash--;
        }
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private LocalPlayer player() {
        return this.minecraft.player;
    }

    private void click() {
        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private String query() {
        return this.search == null ? "" : this.search.getValue().trim().toLowerCase(Locale.ROOT);
    }

    private boolean matches(Component name) {
        String q = this.query();
        return q.isEmpty() || name.getString().toLowerCase(Locale.ROOT).contains(q);
    }

    // =====================================================================================================
    // what the player has right now (left list of Stats / Skills)
    // =====================================================================================================

    private enum Kind { POWER }

    private record Entry(String key, Kind kind, @Nullable Superpower power, ItemStack icon, @Nullable Identifier emblem, Component name,
            Component sub) {
    }

    /**
     * Optimisation: the list is built once per game tick (it was rebuilt several times every frame) — still "what the
     * player has right now", at most one tick old.
     */
    private List<Entry> entries() {
        int tick = this.player() != null ? this.player().tickCount : 0;
        if (this.entriesCache == null || this.entriesTick != tick) {
            this.entriesCache = this.buildEntries();
            this.entriesTick = tick;
        }
        return this.entriesCache;
    }

    private @Nullable List<Entry> entriesCache;
    private int entriesTick = -1;

    private List<Entry> buildEntries() {
        LocalPlayer player = this.player();
        List<Entry> list = new ArrayList<>();
        if (player == null) {
            return list;
        }
        PowerProgress progress = PowerSkills.progress(player);
        for (Superpower power : Superpower.values()) {
            if (Superpowers.data(player).has(power)) {
                int level = PowerLevels.level(power, progress.get(power).xp());
                list.add(new Entry("power:" + power.id(), Kind.POWER, power, ItemStack.EMPTY, emblem(power), power.title(), tr("sub.power", level)));
            }
        }
        // owner: suits and gadgets (web-shooters) are NOT listed here — gadgets only appear in the Keys tab
        return list;
    }

    /** The power's emblem = the hero's group emblem of the Suit Creator (owner). */
    public static Identifier emblem(Superpower power) {
        String group = switch (power) {
            case SPIDERMAN -> "spider_man";
        };
        return com.star_1mp.powersunchained.Constants.id("suit_creator/group/" + group);
    }

    /** Saved pick meaning "deliberately nothing" (a click on the list's empty area, owner). */
    private static final String NONE = "none";

    private @Nullable Entry selectedEntry(List<Entry> entries) {
        if (NONE.equals(this.selected)) {
            return null;
        }
        for (Entry e : entries) {
            if (e.key().equals(this.selected)) {
                return e;
            }
        }
        return entries.isEmpty() ? null : entries.getFirst();
    }

    /** The power whose points / level / tree are shown: the selected one, else the first owned. */
    private @Nullable Superpower shownPower(List<Entry> entries) {
        if (NONE.equals(this.selected)) {
            return null;
        }
        Entry sel = this.selectedEntry(entries);
        if (sel != null && sel.power() != null) {
            return sel.power();
        }
        for (Entry e : entries) {
            if (e.power() != null) {
                return e.power();
            }
        }
        return null;
    }

    // =====================================================================================================
    // drawing
    // =====================================================================================================

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        this.time = (this.minecraft.player != null ? this.minecraft.player.tickCount : 0) + partialTick;
        int x = this.left();
        int y = this.top();
        panel(g, x, y, W, H);
        List<Entry> entries = this.entries();
        this.drawTopBar(g, x, y, mouseX, mouseY, entries);
        if (this.search != null) {
            this.search.visible = true;
        }
        switch (this.tab) {
            case TAB_SKILLS -> this.drawSkills(g, x, y, mouseX, mouseY, entries);
            case TAB_KEYS -> this.drawKeys(g, x, y, mouseX, mouseY);
            default -> this.drawStats(g, x, y, mouseX, mouseY, entries);
        }
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        if (this.dragging != null) {
            g.nextStratum();
            abilitySprite(g, this.dragging, mouseX - 9, mouseY - 9);
        }
    }

    private int[] tabBounds(int i) {
        int x = this.left() + 112;
        for (int k = 0; k < i; k++) {
            x += this.font.width(tr(TAB_KEYS_LANG[k])) + 16;
        }
        return new int[]{x, this.top() + 3, this.font.width(tr(TAB_KEYS_LANG[i])) + 14, 15};
    }

    private void drawTopBar(GuiGraphicsExtractor g, int x, int y, int mx, int my, List<Entry> entries) {
        g.text(this.font, this.title, x + 8, y + 7, LABEL, false);
        for (int i = 0; i < 3; i++) {
            int[] b = this.tabBounds(i);
            this.button(g, b[0], b[1], b[2], b[3], tr(TAB_KEYS_LANG[i]), true, in(mx, my, b[0], b[1], b[2], b[3]), this.tab == i);
        }
        // skill points of the shown power (every tab)
        Superpower power = this.shownPower(entries);
        if (power != null) {
            int points = PowerLevels.points(power, PowerSkills.progress(this.player()).get(power));
            Component text = tr("points", points);
            int bw = this.font.width(text) + 18;
            int bx = x + W - 8 - bw;
            inset(g, bx, y + 4, bw, 14, 0xFF1D2128);
            diamond(g, bx + 4, y + 7, AMBER);
            g.text(this.font, text, bx + 15, y + 7, AMBER, false);
            if (in(mx, my, bx, y + 4, bw, 14)) {
                g.setTooltipForNextFrame(this.font, tr("points.tooltip", power.title()), mx, my);
            }
        }
    }

    private void drawList(GuiGraphicsExtractor g, int x, int y, int mx, int my, List<Entry> entries) {
        monitor(g, x + LIST_X, y + LIST_Y, LIST_W, LIST_H);
        int lx = x + LIST_X + 2;
        int ly = y + LIST_TOP - this.listScroll;
        g.enableScissor(x + LIST_X + 1, y + LIST_TOP - 2, x + LIST_X + LIST_W - 1, y + LIST_Y + LIST_H - 1);
        Entry sel = this.selectedEntry(entries);
        int shown = 0;
        for (Entry e : entries) {
            if (!this.matches(e.name())) {
                continue;
            }
            boolean hover = in(mx, my, lx, ly, LIST_W - 4, CARD_H - 2);
            this.card(g, lx, ly, LIST_W - 4, e.icon(), e.emblem(), e.name(), e.sub(), e == sel, hover);
            ly += CARD_H;
            shown++;
        }
        g.disableScissor();
        if (shown == 0) {
            int ty = y + LIST_TOP + 6;
            for (FormattedCharSequence line : this.font.split(entries.isEmpty() ? tr("list.empty") : tr("search.none"), LIST_W - 12)) {
                g.text(this.font, line, x + LIST_X + LIST_W / 2 - this.font.width(line) / 2, ty, DIM, false);
                ty += 10;
            }
        }
    }

    /** The real player on a hologram pedestal; drag to turn. */
    private void hologram(GuiGraphicsExtractor g, int hx, int hy, int hw, int hh, int scale, Component title) {
        monitor(g, hx, hy, hw, hh);
        brackets(g, hx + 2, hy + 2, hw - 4, hh - 4, CYAN_DIM);
        g.text(this.font, title, hx + 6, hy + 5, CYAN_DIM, false);
        int cx = hx + hw / 2;
        int feet = hy + hh - 8;
        g.fillGradient(cx - 30, hy + 14, cx + 30, feet, 0x0059E4FF, 0x2459E4FF);
        g.fill(cx - 34, feet, cx + 34, feet + 1, CYAN);
        g.fill(cx - 26, feet + 1, cx + 26, feet + 2, 0x8859E4FF);
        g.fill(cx - 18, feet + 2, cx + 18, feet + 3, 0x4459E4FF);
        LocalPlayer player = this.player();
        if (player != null) {
            float spin = this.rotating ? 0.0F : this.time * 0.8F;
            float yawDeg = Mth.wrapDegrees(this.yaw + spin);
            int boxTop = feet - Math.round(1.9F * scale) - 4;
            drawEntity(g, hx + 2, boxTop, hx + hw - 2, feet + 2, scale, yawDeg, player);
        }
    }

    /** owner: the emblem hologram stands still, no spinning, no flicker / scan-line glitch — just a steady holo tint. */
    private void emblemHologram(GuiGraphicsExtractor g, Identifier sprite, int cx, int top, int size) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, cx - size / 2, top, size, size, 0xF0C8F6FF);
    }

    /** The live player turned by {@code yawDeg} (like the inventory doll, without following the mouse). */
    private static void drawEntity(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, int size, float yawDeg, LocalPlayer player) {
        net.minecraft.client.renderer.entity.EntityRenderer<? super LocalPlayer, ?> renderer =
                net.minecraft.client.Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        net.minecraft.client.renderer.entity.state.EntityRenderState state = renderer.createRenderState(player, 1.0F);
        state.shadowPieces.clear();
        state.outlineColor = 0;
        if (state instanceof net.minecraft.client.renderer.entity.state.LivingEntityRenderState living) {
            living.bodyRot = 180.0F + yawDeg;
            living.yRot = 0.0F;
            living.xRot = 0.0F;
            living.boundingBoxWidth = living.boundingBoxWidth / living.scale;
            living.boundingBoxHeight = living.boundingBoxHeight / living.scale;
            living.scale = 1.0F;
        }
        org.joml.Quaternionf rotation = new org.joml.Quaternionf().rotateZ((float) Math.PI);
        org.joml.Vector3f translation = new org.joml.Vector3f(0.0F, state.boundingBoxHeight / 2.0F + 0.0625F, 0.0F);
        g.entity(state, size, translation, rotation, null, x0, y0, x1, y1);
    }

    // ---- Характеристики ----

    private void drawStats(GuiGraphicsExtractor g, int x, int y, int mx, int my, List<Entry> entries) {
        this.drawList(g, x, y, mx, my, entries);
        this.hologram(g, x + CENTER_X, y + HOLO_Y, CENTER_W, HOLO_H, HOLO_SCALE, tr("hologram"));
        // the selected power's level under the hologram
        int by = y + HOLO_Y + HOLO_H + 3;
        monitor(g, x + CENTER_X, by, CENTER_W, H - 8 - (HOLO_Y + HOLO_H + 3));
        Superpower power = this.shownPower(entries);
        if (power != null) {
            this.levelBar(g, x + CENTER_X, by, power);
        } else {
            g.centeredText(this.font, tr("no_power"), x + CENTER_X + CENTER_W / 2, by + 20, DIM);
        }
        // right: the player's stats
        int rx = x + RIGHT_X;
        int ry = y + 20;
        monitor(g, rx, ry, RIGHT_W, H - 28);
        g.text(this.font, tr("stats"), rx + 5, ry + 4, COPPER, false);
        LocalPlayer p = this.player();
        int ty = ry + 17;
        boolean spider = Superpowers.data(p).has(Superpower.SPIDERMAN);
        ty = this.stat(g, rx, ty, icon(Items.APPLE), "stat.health", fmt(p.getMaxHealth() / 2.0) + "♥", p.getMaxHealth() / 40.0, RED, mx, my);
        ty = this.stat(g, rx, ty, icon(Items.IRON_SWORD), "stat.attack", fmt(p.getAttributeValue(Attributes.ATTACK_DAMAGE)),
                p.getAttributeValue(Attributes.ATTACK_DAMAGE) / 12.0, 0xFFFF7A4A, mx, my);
        ty = this.stat(g, rx, ty, icon(Items.IRON_CHESTPLATE), "stat.armor", fmt(p.getArmorValue()), p.getArmorValue() / 30.0,
                0xFFB8C4D0, mx, my);
        double speed = p.getAttributeValue(Attributes.MOVEMENT_SPEED) / 0.1;
        ty = this.stat(g, rx, ty, icon(Items.SUGAR), "stat.speed", Math.round(speed * 100) + "%", speed / 2.0, 0xFF7CE0FF, mx, my);
        double jump = p.getAttributeValue(Attributes.JUMP_STRENGTH) / 0.42;
        ty = this.stat(g, rx, ty, icon(Items.RABBIT_FOOT), "stat.jump", Math.round(jump * 100) + "%", jump / 2.0, 0xFF9CFF7A, mx, my);
        ty = this.stat(g, rx, ty, icon(Items.CLOCK), "stat.attack_speed", fmt(p.getAttributeValue(Attributes.ATTACK_SPEED)),
                p.getAttributeValue(Attributes.ATTACK_SPEED) / 6.0, 0xFFFFD27A, mx, my);
        boolean regen = PowerSkills.spider(p, SkillTrees.REGENERATION);
        ty = this.stat(g, rx, ty, icon(Items.GLISTERING_MELON_SLICE), "stat.regen", regen ? tr("stat.regen.on").getString() : "—",
                regen ? 0.5 : 0.0, 0xFFFF7AB8, mx, my);
        if (spider) {
            ty += 3;
            g.text(this.font, Superpower.SPIDERMAN.title(), rx + 5, ty, COPPER_DARK, false);
            ty += 12;
            boolean quick = PowerSkills.spider(p, SkillTrees.QUICK_CRAWL);
            ty = this.stat(g, rx, ty, icon(Items.LADDER), "stat.climb", quick ? "130%" : "100%", quick ? 0.65 : 0.5, GREEN, mx, my);
            double range = SpiderSense.range(p);
            ty = this.stat(g, rx, ty, icon(Items.ENDER_EYE), "stat.sense", fmt(range), range / 24.0, 0xFFC08CFF, mx, my);
            boolean soft = Superpowers.fallImmune(p);
            this.stat(g, rx, ty, icon(Items.FEATHER), "stat.fall", soft ? tr("stat.fall.none").getString() : "100%",
                    soft ? 1.0 : 0.0, 0xFFE8E8E8, mx, my);
        }
    }

    private int stat(GuiGraphicsExtractor g, int rx, int ty, ItemStack icon, String key, String value, double fill, int color, int mx, int my) {
        g.pose().pushMatrix();
        g.pose().translate(rx + 4, ty - 1);
        g.pose().scale(0.625F);
        g.item(icon, 0, 0);
        g.pose().popMatrix();
        Component name = tr(key);
        g.text(this.font, this.trim(name.getString(), RIGHT_W - 22 - this.font.width(value)), rx + 16, ty, 0xFFDFE6EC, false);
        g.text(this.font, value, rx + RIGHT_W - 5 - this.font.width(value), ty, GREY, false);
        int bx = rx + 16;
        int bw = RIGHT_W - 21;
        g.fill(bx, ty + 9, bx + bw, ty + 11, 0xFF2A3038);
        g.fill(bx, ty + 9, bx + (int) Math.round(bw * Mth.clamp(fill, 0.0, 1.0)), ty + 11, color);
        if (in(mx, my, rx + 2, ty - 1, RIGHT_W - 4, 13)) {
            g.setTooltipForNextFrame(this.font, tr(key + ".desc"), mx, my);
        }
        return ty + 15;
    }

    private void levelBar(GuiGraphicsExtractor g, int bx, int by, Superpower power) {
        PowerProgress.Entry entry = PowerSkills.progress(this.player()).get(power);
        int level = PowerLevels.level(power, entry.xp());
        boolean max = level >= PowerLevels.maxLevel(power);
        g.text(this.font, power.title(), bx + 5, by + 5, WHITE, true);
        Component lv = tr("level", level);
        g.text(this.font, lv, bx + CENTER_W - 5 - this.font.width(lv), by + 5, AMBER, true);
        int in = PowerLevels.xpInLevel(power, entry.xp());
        int need = PowerLevels.xpForNext(max ? level - 1 : level);
        inset(g, bx + 5, by + 17, CENTER_W - 10, 7, 0xFF1D1F24);
        g.fill(bx + 6, by + 18, bx + 6 + Math.round((CENTER_W - 12) * Mth.clamp(in / (float) need, 0.0F, 1.0F)), by + 23, CYAN);
        Component xp = max ? tr("level.max") : tr("xp", in, need);
        g.pose().pushMatrix();
        g.pose().translate(bx + CENTER_W / 2.0F - this.font.width(xp) * 0.75F / 2.0F, by + 27);
        g.pose().scale(0.75F);
        g.text(this.font, xp, 0, 0, GREY, false);
        g.pose().popMatrix();
    }

    // ---- Умения: the skill tree ----

    private static final int TREE_X = CENTER_X;
    private static final int TREE_W = W - 8 - CENTER_X;
    private static final int TREE_TOP = 20;
    private static final int ROOT_H = 42;
    private static final int ROW0 = TREE_TOP + 64;
    private static final int ROW_STEP = 23;
    private static final int NODE = 20;
    private static final int STRIP_Y = H - 8 - 30;

    /** Top-left of a node (inside the GUI). */
    private static int[] nodePos(SkillNode n) {
        int bw = TREE_W / 3;
        int bx = TREE_X + n.branch().ordinal() * bw;
        int cx = bx + bw / 2 + (n.col() - 1) * 23;
        return new int[]{cx - NODE / 2, ROW0 + n.row() * ROW_STEP};
    }

    private enum NodeState { OWNED, AVAILABLE, NO_POINTS, LOCKED }

    private NodeState state(Superpower power, SkillNode n) {
        PowerProgress.Entry entry = PowerSkills.progress(this.player()).get(power);
        if (entry.nodes().contains(n.id())) {
            return NodeState.OWNED;
        }
        for (String r : n.requires()) {
            if (!entry.nodes().contains(r)) {
                return NodeState.LOCKED;
            }
        }
        return PowerLevels.points(power, entry) >= n.cost() ? NodeState.AVAILABLE : NodeState.NO_POINTS;
    }

    private void drawSkills(GuiGraphicsExtractor g, int x, int y, int mx, int my, List<Entry> entries) {
        this.drawList(g, x, y, mx, my, entries);
        int tx = x + TREE_X;
        int ty = y + TREE_TOP;
        monitor(g, tx, ty, TREE_W, H - 8 - TREE_TOP);
        brackets(g, tx + 2, ty + 2, TREE_W - 4, H - 8 - TREE_TOP - 4, CYAN_DIM);
        Entry sel = this.selectedEntry(entries);
        if (sel == null || sel.kind() != Kind.POWER || sel.power() == null) {
            Component msg = tr("skills.none");
            int ly = ty + (H - 8 - TREE_TOP) / 2 - 10;
            for (FormattedCharSequence line : this.font.split(msg, TREE_W - 40)) {
                g.text(this.font, line, tx + TREE_W / 2 - this.font.width(line) / 2, ly, GREY, false);
                ly += 10;
            }
            return;
        }
        Superpower power = sel.power();
        List<SkillNode> nodes = SkillTrees.of(power);
        // root: the power's emblem as a still hologram at the top (owner: no spinning, no glitch lines)
        int cx = tx + TREE_W / 2;
        int rootY = ty + 3;
        int emblemSize = 27;  // 3x the 9x9 emblem
        int emblemTop = rootY + (ROOT_H - 4 - emblemSize) / 2;
        g.fillGradient(cx - 20, rootY + 1, cx + 21, rootY + ROOT_H - 4, 0x0059E4FF, 0x2459E4FF);
        g.fill(cx - 21, rootY + ROOT_H - 4, cx + 22, rootY + ROOT_H - 3, CYAN);
        this.emblemHologram(g, emblem(power), cx, emblemTop, emblemSize);
        // branch titles + lines from the root
        int bw = TREE_W / 3;
        for (SkillBranch b : SkillBranch.values()) {
            int bcx = x + TREE_X + b.ordinal() * bw + bw / 2;
            Component t = b.title();
            g.text(this.font, t, bcx - this.font.width(t) / 2, y + ROW0 - 13, b.color(), true);
        }
        for (SkillNode n : nodes) {
            if (n.requires().isEmpty()) {
                int[] p = nodePos(n);
                boolean owned = this.state(power, n) == NodeState.OWNED;
                int c = owned ? n.branch().color() : 0xFF3A4652;
                int nx = x + p[0] + NODE / 2;
                int midY = rootY + ROOT_H + 1;
                g.fill(cx, rootY + ROOT_H - 3, cx + 1, midY, c);
                g.fill(Math.min(cx, nx), midY, Math.max(cx, nx) + 1, midY + 1, c);
                g.fill(nx, midY, nx + 1, y + ROW0 - 14, c);
                g.fill(nx, y + ROW0 - 4, nx + 1, y + ROW0, c);
            }
            for (String r : n.requires()) {
                SkillNode parent = SkillTrees.node(power, r);
                if (parent == null) {
                    continue;
                }
                int[] a = nodePos(parent);
                int[] b2 = nodePos(n);
                boolean lit = this.state(power, n) == NodeState.OWNED;
                int c = lit ? n.branch().color() : 0xFF3A4652;
                int ax = x + a[0] + NODE / 2;
                int ay = y + a[1] + NODE;
                int bx2 = x + b2[0] + NODE / 2;
                int by2 = y + b2[1];
                int mid = (ay + by2) / 2;
                g.fill(ax, ay, ax + 1, mid, c);
                g.fill(Math.min(ax, bx2), mid, Math.max(ax, bx2) + 1, mid + 1, c);
                g.fill(bx2, mid, bx2 + 1, by2, c);
            }
        }
        SkillNode hovered = null;
        for (SkillNode n : nodes) {
            int[] p = nodePos(n);
            int nx = x + p[0];
            int ny = y + p[1];
            NodeState st = this.state(power, n);
            boolean hover = in(mx, my, nx, ny, NODE, NODE);
            if (hover) {
                hovered = n;
            }
            int border = switch (st) {
                case OWNED -> n.branch().color();
                case AVAILABLE -> ((int) ((0.55F + 0.45F * Mth.sin(this.time * 0.25F)) * 255) << 24) | 0x59E4FF;
                case NO_POINTS -> 0xFF6B7682;
                case LOCKED -> 0xFF2A3038;
            };
            g.fill(nx, ny, nx + NODE, ny + NODE, st == NodeState.OWNED ? 0xFF2E2418 : 0xFF161C22);
            outline(g, nx, ny, NODE, NODE, border);
            if (st == NodeState.OWNED) {
                g.fill(nx + 1, ny + 1, nx + NODE - 1, ny + 2, (n.branch().color() & 0xFFFFFF) | 0x60000000);
            }
            g.blitSprite(RenderPipelines.GUI_TEXTURED, n.icon(), nx + 1, ny + 1, 18, 18);
            if (st == NodeState.LOCKED || st == NodeState.NO_POINTS) {
                g.fill(nx + 1, ny + 1, nx + NODE - 1, ny + NODE - 1, st == NodeState.LOCKED ? 0xB0101418 : 0x70101418);
            }
            if (n.id().equals(this.selectedNode)) {
                outline(g, nx - 2, ny - 2, NODE + 4, NODE + 4, COPPER);
            } else if (hover) {
                outline(g, nx - 1, ny - 1, NODE + 2, NODE + 2, 0x80FFFFFF);
            }
        }
        // the picked node: name, cost, "learn"
        int sy = y + STRIP_Y;
        g.fill(tx + 3, sy, tx + TREE_W - 3, sy + 1, 0x402F8FA8);
        SkillNode pick = this.selectedNode != null ? SkillTrees.node(power, this.selectedNode) : null;
        if (pick == null) {
            g.text(this.font, tr("skills.pick"), tx + 8, sy + 10, DIM, false);
        } else {
            NodeState st = this.state(power, pick);
            g.text(this.font, pick.title(), tx + 8, sy + 5, pick.branch().color(), true);
            Component cost = st == NodeState.OWNED ? tr("skill.owned") : tr("skill.cost", pick.cost());
            g.text(this.font, cost, tx + 8, sy + 17, st == NodeState.OWNED ? GREEN : AMBER, false);
            int bx = tx + TREE_W - 8 - 70;
            if (st != NodeState.OWNED) {
                this.button(g, bx, sy + 5, 70, 18, tr("skill.learn"), st == NodeState.AVAILABLE, in(mx, my, bx, sy + 5, 70, 18), false);
            }
            if (this.failFlash > 0) {
                Component f = tr(this.failKey);
                g.text(this.font, f, bx - 6 - this.font.width(f), sy + 17, 0xFFFF9B8F, false);
            }
        }
        if (hovered != null) {
            List<FormattedCharSequence> lines = new ArrayList<>();
            lines.add(hovered.title().copy().withColor(hovered.branch().color()).getVisualOrderText());
            lines.addAll(this.font.split(hovered.description(), 180));
            NodeState st = this.state(power, hovered);
            Component state = switch (st) {
                case OWNED -> tr("skill.owned").copy().withColor(GREEN);
                case AVAILABLE -> tr("skill.cost", hovered.cost()).copy().withColor(AMBER);
                case NO_POINTS -> tr("skill.cost", hovered.cost()).copy().withColor(RED);
                case LOCKED -> tr("skill.locked").copy().withColor(RED);
            };
            lines.add(state.getVisualOrderText());
            g.setTooltipForNextFrame(this.font, lines, mx, my);
        }
    }

    // ---- Клавиши ----

    /** Owner: own 18x18 icons for every key ability (the roll shares the skill tree's roll icon). */
    private static Identifier abilityIcon(String id) {
        return ABILITY_ICONS.computeIfAbsent(id, k -> Constants.id("ability/" + switch (k) {
            case AbilitySlots.WEB_SHOT, AbilitySlots.WEB_ZIP, AbilitySlots.WEB_SWING, AbilitySlots.ROLL, AbilitySlots.ABILITY_WHEEL -> k;
            default -> "unknown";
        }));
    }

    private static void abilitySprite(GuiGraphicsExtractor g, String id, int x, int y) {
        g.blitSprite(RenderPipelines.GUI_TEXTURED, abilityIcon(id), x, y, 18, 18);
    }

    private static Component abilityName(String id) {
        AbilitySlots.Ability a = AbilitySlots.get(id);
        return a != null ? a.previewName() : Component.literal(id);
    }

    private @Nullable List<String> keysCache;
    private int keysTick = -1;
    private String keysQuery = "";

    /** Cached per game tick and search text (optimisation, like {@link #entries()}). */
    private List<String> keyAbilities() {
        int tick = this.player() != null ? this.player().tickCount : 0;
        if (this.keysCache == null || this.keysTick != tick || !this.keysQuery.equals(this.query())) {
            this.keysCache = this.buildKeyAbilities();
            this.keysTick = tick;
            this.keysQuery = this.query();
        }
        return this.keysCache;
    }

    private List<String> buildKeyAbilities() {
        List<String> list = new ArrayList<>();
        for (String id : AbilitySlots.owned(this.player())) {
            if (this.matches(abilityName(id))) {
                list.add(id);
            }
        }
        return list;
    }

    private int[] socket(int i) {
        int x0 = this.left() + CENTER_X + (CENTER_W - (5 * 30 - 4)) / 2;
        return new int[]{x0 + i * 30, this.top() + HOLO_Y + HOLO_H + 5, 26, 26};
    }

    private void drawKeys(GuiGraphicsExtractor g, int x, int y, int mx, int my) {
        // left: everything the player has right now (drag onto a key)
        monitor(g, x + LIST_X, y + LIST_Y, LIST_W, LIST_H);
        int lx = x + LIST_X + 2;
        int ly = y + LIST_TOP - this.listScroll;
        g.enableScissor(x + LIST_X + 1, y + LIST_TOP - 2, x + LIST_X + LIST_W - 1, y + LIST_Y + LIST_H - 1);
        List<String> abilities = this.keyAbilities();
        for (String id : abilities) {
            boolean hover = in(mx, my, lx, ly, LIST_W - 4, CARD_H - 2);
            this.card(g, lx, ly, LIST_W - 4, ItemStack.EMPTY, abilityIcon(id), abilityName(id), tr("keys.drag"), id.equals(this.dragging), hover);
            ly += CARD_H;
        }
        g.disableScissor();
        if (abilities.isEmpty()) {
            int ty = y + LIST_TOP + 6;
            for (FormattedCharSequence line : this.font.split(tr("keys.empty"), LIST_W - 12)) {
                g.text(this.font, line, x + LIST_X + LIST_W / 2 - this.font.width(line) / 2, ty, DIM, false);
                ty += 10;
            }
        }
        // centre: hologram + the 5 keys under it
        this.hologram(g, x + CENTER_X, y + HOLO_Y, CENTER_W, HOLO_H, HOLO_SCALE, tr("keys.set"));
        for (int i = 0; i < PUKeyMappings.ALL.size(); i++) {
            int[] s = this.socket(i);
            String id = AbilitySlots.chosen(this.player(), i);
            boolean empty = AbilitySlots.NONE.equals(id) || AbilitySlots.get(id) == null;
            boolean target = this.dragging != null && in(mx, my, s[0], s[1], s[2], s[3]);
            inset(g, s[0], s[1], s[2], s[3], 0xFF1A2027);
            outline(g, s[0] - 1, s[1] - 1, s[2] + 2, s[3] + 2, target ? COPPER : empty ? 0xFF3A4652 : CYAN);
            if (!empty) {
                abilitySprite(g, id, s[0] + 4, s[1] + 4);
                if (in(mx, my, s[0], s[1], s[2], s[3]) && this.dragging == null) {
                    g.setTooltipForNextFrame(this.font, List.of(abilityName(id).getVisualOrderText(),
                            tr("keys.remove").copy().withColor(GREY).getVisualOrderText()), mx, my);
                }
            } else {
                g.centeredText(this.font, Component.literal("+"), s[0] + 13, s[1] + 9, DIM);
            }
            Component key = AbilityHud.keyLabel(PUKeyMappings.ALL.get(i));
            int kw = Math.max(11, this.font.width(key) + 5);
            int kx = s[0] + s[2] / 2 - kw / 2;
            int ky = y + H - 8 - 13;
            g.fill(kx, ky, kx + kw, ky + 11, 0xFF1D2128);
            outline(g, kx, ky, kw, 11, empty ? DIM : GREY);
            g.centeredText(this.font, key, kx + kw / 2, ky + 2, empty ? DIM : WHITE);
        }
        // right: how the HUD will look + buttons
        int rx = x + RIGHT_X;
        int ry = y + 20;
        monitor(g, rx, ry, RIGHT_W, H - 28);
        g.text(this.font, tr("keys.preview"), rx + 5, ry + 4, CYAN_DIM, false);
        AbilityHud.Entry[] hud = AbilityHud.collect(this.player());
        if (hud != null) {
            AbilityHudConfig preview = new AbilityHudConfig();
            preview.scale = 0.75F;
            g.enableScissor(rx + 2, ry + 15, rx + RIGHT_W - 2, ry + 110);
            AbilityHud.draw(g, this.font, hud, preview, rx + 5, ry + 17);
            g.disableScissor();
        }
        g.text(this.font, tr(AbilitySlots.custom() ? "keys.custom" : "keys.automatic"), rx + 5, ry + 114, GREY, false);
        int b1 = y + H - 8 - 42;
        this.button(g, rx + 3, b1, RIGHT_W - 6, 16, tr("keys.auto"), AbilitySlots.custom(), in(mx, my, rx + 3, b1, RIGHT_W - 6, 16), false);
        this.button(g, rx + 3, b1 + 19, RIGHT_W - 6, 16, tr("keys.hud"), true, in(mx, my, rx + 3, b1 + 19, RIGHT_W - 6, 16), false);
    }

    // =====================================================================================================
    // input
    // =====================================================================================================

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        if (this.search != null && !in(mx, my, this.search.getX(), this.search.getY(), this.search.getWidth(), this.search.getHeight())) {
            this.search.setFocused(false);
            if (this.getFocused() == this.search) {
                this.setFocused(null);
            }
        }
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT && this.clickUi(mx, my)) {
            return true;
        }
        if (event.button() == InputConstants.MOUSE_BUTTON_RIGHT && this.tab == TAB_KEYS) {
            for (int i = 0; i < PUKeyMappings.ALL.size(); i++) {
                int[] s = this.socket(i);
                if (in(mx, my, s[0], s[1], s[2], s[3])) {
                    AbilitySlots.choose(this.player(), i, AbilitySlots.NONE);
                    this.click();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    private boolean clickUi(double mx, double my) {
        int x = this.left();
        int y = this.top();
        for (int i = 0; i < 3; i++) {
            int[] b = this.tabBounds(i);
            if (in(mx, my, b[0], b[1], b[2], b[3])) {
                if (this.tab != i) {
                    this.setTab(i);
                    this.click();
                }
                return true;
            }
        }
        boolean inList = in(mx, my, x + LIST_X, y + LIST_TOP - 2, LIST_W, LIST_H - (LIST_TOP - 2 - LIST_Y));
        int lx = x + LIST_X + 2;
        if (this.tab == TAB_KEYS) {
            if (inList) {
                int ly = y + LIST_TOP - this.listScroll;
                for (String id : this.keyAbilities()) {
                    if (in(mx, my, lx, ly, LIST_W - 4, CARD_H - 2)) {
                        this.dragging = id;
                        this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_PICKUP, 1.6F));
                        return true;
                    }
                    ly += CARD_H;
                }
            }
            int b1 = y + H - 8 - 42;
            if (AbilitySlots.custom() && in(mx, my, x + RIGHT_X + 3, b1, RIGHT_W - 6, 16)) {
                AbilitySlots.resetChoices();
                this.click();
                return true;
            }
            if (in(mx, my, x + RIGHT_X + 3, b1 + 19, RIGHT_W - 6, 16)) {
                this.click();
                this.minecraft.gui.setScreen(new AbilityHudConfigScreen(this));
                return true;
            }
            if (in(mx, my, x + CENTER_X, y + HOLO_Y, CENTER_W, HOLO_H)) {
                this.rotating = true;
                return true;
            }
            return false;
        }
        if (inList) {
            int ly = y + LIST_TOP - this.listScroll;
            for (Entry e : this.entries()) {
                if (!this.matches(e.name())) {
                    continue;
                }
                if (in(mx, my, lx, ly, LIST_W - 4, CARD_H - 2)) {
                    this.selected = e.key();
                    lastSelected = e.key();
                    this.selectedNode = null;
                    this.click();
                    return true;
                }
                ly += CARD_H;
            }
            // owner: a click on the list's empty area clears the pick
            this.selected = NONE;
            lastSelected = NONE;
            this.selectedNode = null;
            return true;
        }
        if (this.tab == TAB_STATS) {
            if (in(mx, my, x + CENTER_X, y + HOLO_Y, CENTER_W, HOLO_H)) {
                this.rotating = true;
                return true;
            }
            return false;
        }
        // skills
        List<Entry> entries = this.entries();
        Entry sel = this.selectedEntry(entries);
        if (sel == null || sel.power() == null) {
            return false;
        }
        Superpower power = sel.power();
        for (SkillNode n : SkillTrees.of(power)) {
            int[] p = nodePos(n);
            if (in(mx, my, x + p[0], y + p[1], NODE, NODE)) {
                this.selectedNode = n.id();
                this.click();
                return true;
            }
        }
        SkillNode pick = this.selectedNode != null ? SkillTrees.node(power, this.selectedNode) : null;
        int bx = x + TREE_X + TREE_W - 8 - 70;
        int sy = y + STRIP_Y;
        if (pick != null && in(mx, my, bx, sy + 5, 70, 18)) {
            NodeState st = this.state(power, pick);
            if (st == NodeState.AVAILABLE) {
                Services.GAMEPLAY.sendToServer(new SkillBuyPayload(power.id(), pick.id()));
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.PLAYER_LEVELUP, 1.5F));
            } else if (st != NodeState.OWNED) {
                this.failKey = st == NodeState.LOCKED ? "skill.need_previous" : "skill.not_enough";
                this.failFlash = 40;
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.2F));
            }
            return true;
        }
        // owner: a click on the tree's empty area clears the picked skill
        if (in(mx, my, x + TREE_X, y + TREE_TOP, TREE_W, H - 8 - TREE_TOP)) {
            this.selectedNode = null;
            return true;
        }
        return false;
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
            String id = this.dragging;
            this.dragging = null;
            for (int i = 0; i < PUKeyMappings.ALL.size(); i++) {
                int[] s = this.socket(i);
                if (in(event.x(), event.y(), s[0] - 2, s[1] - 2, s[2] + 4, s[3] + 4)) {
                    AbilitySlots.choose(this.player(), i, id);
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ITEM_PICKUP, 1.0F));
                    return true;
                }
            }
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double scrollX, double scrollY) {
        if (in(mx, my, this.left() + LIST_X, this.top() + LIST_Y, LIST_W, LIST_H)) {
            this.listScroll = Math.max(0, this.listScroll - (int) (scrollY * 12));
            return true;
        }
        return super.mouseScrolled(mx, my, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (this.search != null && this.search.isFocused() && !event.isEscape()) {
            this.search.keyPressed(event);
            return true;
        }
        if (PUKeyMappings.POWERS_MENU.matches(event)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    // =====================================================================================================
    // dev / CI
    // =====================================================================================================

    public void debugTab(int tab) {
        this.setTab(tab);
    }

    public void debugSelect(@Nullable String key, @Nullable String node) {
        this.selected = key;
        this.selectedNode = node;
    }

    // =====================================================================================================
    // drawing helpers (the Suit Creator / Tech Table look)
    // =====================================================================================================

    private String trim(String text, int width) {
        if (this.font.width(text) <= width) {
            return text;
        }
        while (!text.isEmpty() && this.font.width(text + "…") > width) {
            text = text.substring(0, text.length() - 1);
        }
        return text + "…";
    }

    private static String fmt(double v) {
        return v == Math.round(v) ? String.valueOf(Math.round(v)) : String.format(Locale.ROOT, "%.1f", v);
    }

    private static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }

    private void card(GuiGraphicsExtractor g, int x, int y, int w, ItemStack icon, @Nullable Identifier emblem, Component name,
            @Nullable Component sub, boolean selected, boolean hover) {
        g.fill(x, y, x + w, y + CARD_H - 2, selected ? 0x40FF9A52 : hover ? 0x20FFFFFF : 0x0CFFFFFF);
        if (selected) {
            outline(g, x, y, w, CARD_H - 2, COPPER);
        }
        inset(g, x + 2, y + 1, 18, 18, 0xFF2A3038);
        if (emblem != null && emblem.getPath().startsWith("ability/")) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, emblem, x + 2, y + 1, 18, 18);
        } else if (emblem != null) {
            // owner: a bit smaller — 16x16 like an item, inside the same 18x18 frame
            g.blitSprite(RenderPipelines.GUI_TEXTURED, emblem, x + 3, y + 2, 16, 16);
        } else {
            g.item(icon, x + 3, y + 2);
        }
        int tw = w - 26;
        g.text(this.font, this.trim(name.getString(), tw), x + 23, y + (sub == null ? 6 : 2), selected ? 0xFFFFD9B8 : WHITE, true);
        if (sub != null) {
            g.text(this.font, this.trim(sub.getString(), tw), x + 23, y + 11, GREY, false);
        }
    }

    private void button(GuiGraphicsExtractor g, int x, int y, int w, int h, Component text, boolean active, boolean hovered, boolean selected) {
        Identifier id = Identifier.withDefaultNamespace(!active ? "widget/button_disabled"
                : hovered || selected ? "widget/button_highlighted" : "widget/button");
        g.blitSprite(RenderPipelines.GUI_TEXTURED, id, x, y, w, h);
        int color = !active ? 0xFFA0A0A0 : selected ? 0xFFFFD9B8 : WHITE;
        g.centeredText(this.font, text, x + w / 2, y + (h - 8) / 2 + 1, color);
        if (selected) {
            g.fill(x + 3, y + h - 3, x + w - 3, y + h - 2, COPPER);
        }
    }

    private static void diamond(GuiGraphicsExtractor g, int x, int y, int c) {
        for (int i = 0; i < 4; i++) {
            g.fill(x + 3 - i, y + i, x + 4 + i, y + i + 1, c);
            g.fill(x + 3 - i, y + 7 - i, x + 4 + i, y + 8 - i, c);
        }
    }

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
        com.star_1mp.powersunchained.client.gui.GuiFx.scanlines(g, x, y, w, h);
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
}
