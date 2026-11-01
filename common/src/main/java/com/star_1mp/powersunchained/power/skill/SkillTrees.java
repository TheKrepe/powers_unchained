package com.star_1mp.powersunchained.power.skill;

import com.star_1mp.powersunchained.power.Superpower;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/**
 * Skill trees per superpower (owner: like Palladium's tree, levelled up like Alien Evolution — skill points come from
 * USING the power). Gadgets have no tree: they are upgraded on the Tech Table.
 *
 * <p>Spider-Man (owner: from the start only wall climbing and a weak Spider-Sense; everything else is learnt):
 * <ul>
 *   <li>Agility: roll, faster crawling, spider jump, ceilings, flips, soft landing, acrobat;</li>
 *   <li>Sense: danger sense, night vision, projectile sense, wide sense, dodge, reflexes, sixth sense;</li>
 *   <li>Combat: spider strength, tough body, heavy punch, strength II, spider armour, regeneration, hero's body.</li>
 * </ul>
 * Each branch: one root, two nodes under it, two more, then two capstone rows in the middle.
 */
public final class SkillTrees {

    // ---- Spider-Man node ids (effects read them) ----
    public static final String ROLL = "roll";
    public static final String QUICK_CRAWL = "quick_crawl";
    public static final String SPIDER_JUMP = "spider_jump";
    public static final String CEILINGS = "ceilings";
    public static final String FLIPS = "flips";
    public static final String SOFT_LANDING = "soft_landing";
    public static final String ACROBAT = "acrobat";

    public static final String DANGER_SENSE = "danger_sense";
    public static final String NIGHT_VISION = "night_vision";
    public static final String PROJECTILE_SENSE = "projectile_sense";
    public static final String WIDE_SENSE = "wide_sense";
    public static final String DODGE = "dodge";
    public static final String REFLEXES = "reflexes";
    public static final String SIXTH_SENSE = "sixth_sense";

    public static final String STRENGTH = "strength";
    public static final String TOUGH_BODY = "tough_body";
    public static final String HEAVY_PUNCH = "heavy_punch";
    public static final String STRENGTH_2 = "strength_2";
    public static final String SPIDER_ARMOR = "spider_armor";
    public static final String REGENERATION = "regeneration";
    public static final String HERO_BODY = "hero_body";

    private static final Map<Superpower, List<SkillNode>> TREES = Map.of(Superpower.SPIDERMAN, spiderMan());

    private SkillTrees() {
    }

    public static List<SkillNode> of(Superpower power) {
        return TREES.getOrDefault(power, List.of());
    }

    public static @Nullable SkillNode node(Superpower power, String id) {
        for (SkillNode n : of(power)) {
            if (n.id().equals(id)) {
                return n;
            }
        }
        return null;
    }

    /** Skill points the whole tree costs. */
    public static int totalCost(Superpower power) {
        int sum = 0;
        for (SkillNode n : of(power)) {
            sum += n.cost();
        }
        return sum;
    }

    private static List<SkillNode> spiderMan() {
        String p = Superpower.SPIDERMAN.id();
        List<SkillNode> list = new ArrayList<>();
        branch(list, p, SkillBranch.AGILITY,
                ROLL, Items.FEATHER, QUICK_CRAWL, Items.LADDER, SPIDER_JUMP, Items.RABBIT_FOOT, CEILINGS, Items.COBWEB,
                FLIPS, Items.PHANTOM_MEMBRANE, SOFT_LANDING, Items.HAY_BLOCK, ACROBAT, Items.SUGAR);
        branch(list, p, SkillBranch.SENSE,
                DANGER_SENSE, Items.ENDER_EYE, NIGHT_VISION, Items.SPIDER_EYE, PROJECTILE_SENSE, Items.ARROW, WIDE_SENSE, Items.SPYGLASS,
                DODGE, Items.SHIELD, REFLEXES, Items.CLOCK, SIXTH_SENSE, Items.TOTEM_OF_UNDYING);
        branch(list, p, SkillBranch.COMBAT,
                STRENGTH, Items.IRON_SWORD, TOUGH_BODY, Items.APPLE, HEAVY_PUNCH, Items.PISTON, STRENGTH_2, Items.DIAMOND_SWORD,
                SPIDER_ARMOR, Items.IRON_CHESTPLATE, REGENERATION, Items.GLISTERING_MELON_SLICE, HERO_BODY, Items.GOLDEN_APPLE);
        return List.copyOf(list);
    }

    /**
     * The branch shape: root (row 0, middle) -> a (row 1, left) + b (row 1, right) -> c (row 2, left, after a) +
     * d (row 2, right, after b) -> e (row 3, middle, after c and d) -> f (row 4, middle, after e).
     * Costs 1, 1, 1, 2, 2, 3, 4.
     */
    private static void branch(List<SkillNode> list, String power, SkillBranch branch, Object... idsAndIcons) {
        String[] ids = new String[7];
        Item[] icons = new Item[7];
        for (int i = 0; i < 7; i++) {
            ids[i] = (String) idsAndIcons[i * 2];
            icons[i] = (Item) idsAndIcons[i * 2 + 1];
        }
        int[][] shape = {{1, 0}, {0, 1}, {2, 1}, {0, 2}, {2, 2}, {1, 3}, {1, 4}};
        int[] costs = {1, 1, 1, 2, 2, 3, 4};
        List<List<String>> requires = List.of(List.of(), List.of(ids[0]), List.of(ids[0]), List.of(ids[1]), List.of(ids[2]),
                List.of(ids[3], ids[4]), List.of(ids[5]));
        for (int i = 0; i < 7; i++) {
            Item icon = icons[i];
            list.add(new SkillNode(power, ids[i], branch, shape[i][0], shape[i][1], costs[i], requires.get(i), () -> new ItemStack(icon)));
        }
    }
}
