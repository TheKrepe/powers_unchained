package lucraft.mods.lucraftcore.util.triggers;

import net.minecraft.advancements.CriteriaTriggers;

public class LCCriteriaTriggers {

    public static final GetSuperpowerTrigger GET_SUPERPOWER = new GetSuperpowerTrigger();
    public static final LoseSuperpowerTrigger LOSE_SUPERPOWER = new LoseSuperpowerTrigger();
    public static final ExecuteAbilityTrigger EXECUTE_ABILITY = new ExecuteAbilityTrigger();

    public static void init() {
        CriteriaTriggers.register(GET_SUPERPOWER);
        CriteriaTriggers.register(LOSE_SUPERPOWER);
        CriteriaTriggers.register(EXECUTE_ABILITY);
    }

}
