package lucraft.mods.lucraftcore.superpowers.effects;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import lucraft.mods.lucraftcore.util.render.LayerRendererAprilFools;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.JsonUtils;
import net.minecraftforge.common.MinecraftForge;

import java.awt.*;
import java.util.*;
import java.util.List;

public class EffectHandler {

    public static Map<String, Class<? extends Effect>> TYPES = new HashMap<>();
    public static Map<String, Class<? extends EffectCondition>> CONDITIONS = new HashMap<>();
    private static final EffectTrail FLASH_TRAIL = new EffectTrail();

    static {
        TYPES.put("glow", EffectGlow.class);
        TYPES.put("skin_change", EffectSkinChange.class);
        TYPES.put("skin_overlay", EffectSkinOverlay.class);
        TYPES.put("vibrating", EffectVibrating.class);
        TYPES.put("flickering", EffectFlickering.class);
        TYPES.put("trail", EffectTrail.class);
        TYPES.put("glowing_hand", EffectGlowingHand.class);
        TYPES.put("name_change", EffectNameChange.class);
        TYPES.put("hud", EffectHUD.class);

        CONDITIONS.put("not", EffectConditionNot.class);
        CONDITIONS.put("always", EffectConditionAlways.class);
        CONDITIONS.put("ability_enabled", EffectConditionAbilityEnabled.class);
        CONDITIONS.put("ability_unlocked", EffectConditionAbilityUnlocked.class);
        CONDITIONS.put("moving", EffectConditionMoving.class);
        CONDITIONS.put("open_armor", EffectConditionOpenArmor.class);

        MinecraftForge.EVENT_BUS.register(new EffectNameChange.EventHandler());

        FLASH_TRAIL.type = EffectTrail.TrailType.LIGHTNINGS;
        FLASH_TRAIL.color = new Color(1F, 0.2F, 0F);
    }

    public static Effect makeEffect(JsonObject json) throws Exception {
        String type = JsonUtils.getString(json, "type");

        if (!TYPES.containsKey(type))
            throw new Exception("The effect type '" + type + "' doesn't exist!");

        Effect effect = TYPES.get(type).newInstance();
        effect.readSettings(json);

        if (JsonUtils.hasField(json, "conditions")) {
            JsonArray conArray = JsonUtils.getJsonArray(json, "conditions");
            for (int i = 0; i < conArray.size(); i++) {
                JsonObject condition = conArray.get(i).getAsJsonObject();
                String conType = JsonUtils.getString(condition, "type");

                if (!CONDITIONS.containsKey(conType))
                    throw new Exception("The effect condition '" + conType + "' doesn't exist!");

                EffectCondition c = CONDITIONS.get(conType).newInstance();
                c.readSettings(condition);
                effect.conditions.add(c);
            }
        } else {
            effect.conditions.add(new EffectConditionAlways());
        }

        return effect;
    }

    public static <T extends Effect> T makeEffect(Class<T> effect, EffectCondition... conditions) {
        try {
            Effect e = effect.newInstance();
            e.conditions.addAll(Arrays.asList(conditions));
            return (T) e;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static <T extends Effect> List<T> getEffectsByClass(EntityPlayer player, Class<T> effect) {
        List<T> effects = new ArrayList<>();
        Superpower superpower = SuperpowerHandler.getSuperpower(player);
        SuitSet suitSet = SuitSet.getSuitSet(player);

        if (superpower != null && superpower.getEffects() != null) {
            for (Effect e : superpower.getEffects()) {
                if (e.getClass() == effect) {
                    effects.add((T) e);
                }
            }
        }

        if (suitSet != null && suitSet.getEffects() != null) {
            for (Effect e : suitSet.getEffects()) {
                if (e.getClass() == effect) {
                    effects.add((T) e);
                }
            }
        }

        if(effect == EffectTrail.class && LayerRendererAprilFools.isAprilFoolsDay()) {
            effects.add((T) FLASH_TRAIL);
        }

        return effects;
    }

    public static boolean canEffectBeDisplayed(Effect effect, EntityPlayer player) {
        for (EffectCondition con : effect.conditions) {
            if (!con.isFulFilled(player)) {
                return false;
            }
        }

        return true;
    }

}
