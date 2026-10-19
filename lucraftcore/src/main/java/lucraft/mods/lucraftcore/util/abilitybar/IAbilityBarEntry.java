package lucraft.mods.lucraftcore.util.abilitybar;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.math.Vec3d;

public interface IAbilityBarEntry {

    boolean isActive();

    void onButtonPress();

    void onButtonRelease();

    void drawIcon(Minecraft mc, Gui gui, int x, int y);

    String getDescription();

    boolean renderCooldown();

    float getCooldownPercentage();

    default Vec3d getCooldownColor() {
        float f = getCooldownPercentage();
        return new Vec3d(1D - f * 1D, 0.7D * f, 0.06666D * f);
    }

    default boolean showKey() {
        return true;
    }

    default EnumAbilityBarColor getColor() {
        return EnumAbilityBarColor.LIGHT_GRAY;
    }

}
