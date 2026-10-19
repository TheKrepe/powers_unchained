package lucraft.mods.heroesexpansion.trigger;

import net.minecraft.advancements.CriteriaTriggers;

public class HECriteriaTriggers {

    public static final SpiderSenseTrigger SPIDER_SENSE = new SpiderSenseTrigger();
    public static final TeamUpTrigger TEAM_UP = new TeamUpTrigger();

    public static void load() {
        CriteriaTriggers.register(SPIDER_SENSE);
        CriteriaTriggers.register(TEAM_UP);
    }

}
