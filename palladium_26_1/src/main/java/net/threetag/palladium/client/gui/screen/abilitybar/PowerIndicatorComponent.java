package net.threetag.palladium.client.gui.screen.abilitybar;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.threetag.palladium.client.PalladiumKeyMappings;
import net.threetag.palladium.client.util.RenderUtil;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.Easing;

import java.util.ArrayList;
import java.util.List;

public class PowerIndicatorComponent implements AbilityBarComponent {

    private final AbilityBar.AbilityList abilityList;
    private final CompoundAbilityBarComponent keyAndIcon;

    public PowerIndicatorComponent(AbilityBar.AbilityList abilityList, boolean showButton) {
        this.abilityList = abilityList;
        List<AbilityBarComponent> componentList = new ArrayList<>();

        if (showButton) {
            componentList.add(new SwitchKeyComponent(abilityList, PalladiumKeyMappings.ROTATE_ABILITY_LIST));
        }

        componentList.add(new IconAbilityBarComponent(abilityList.getPowerInstance().getPower().value().getIcon()));
        this.keyAndIcon = new CompoundAbilityBarComponent(componentList, false);
        this.keyAndIcon.padding = 4;
        this.keyAndIcon.center = true;
    }

    @Override
    public int getWidth() {
        return 52;
    }

    @Override
    public int getHeight() {
        return 28;
    }

    @Override
    public void extractContent(Minecraft minecraft, GuiGraphicsExtractor gui, int x, int y, AbilityBarAlignment alignment) {
        this.abilityList.getSprites().blit(gui, AbilityBarSprites.powerIndicatorName(alignment), DataContext.forPower(minecraft.player, this.abilityList.getPowerInstance()), x, y, this.getWidth(), this.getHeight());
        int width = this.keyAndIcon.getWidth();
        int height = this.keyAndIcon.getHeight();
        int offsetX = alignment.isLeft() ? 0 : 3;
        int offsetY = alignment.isTop() ? 0 : 3;

        this.keyAndIcon.extractContent(
                minecraft,
                gui,
                x + offsetX + ((this.getWidth() - 3 - width) / 2),
                y + offsetY + ((this.getHeight() - 3 - height) / 2),
                alignment);
    }

    public record SwitchKeyComponent(AbilityBar.AbilityList abilityList,
                                     KeyMapping keyMapping) implements AbilityBarComponent {

        @Override
        public int getWidth() {
            return Minecraft.getInstance().font.width(this.keyMapping.getTranslatedKeyMessage()) + 3 + 8;
        }

        @Override
        public int getHeight() {
            return Minecraft.getInstance().font.lineHeight;
        }

        @Override
        public void extractContent(Minecraft minecraft, GuiGraphicsExtractor gui, int x, int y, AbilityBarAlignment alignment) {
            Component keyText = this.keyMapping.getTranslatedKeyMessage();
            gui.text(minecraft.font, keyText, x, y, RenderUtil.FULL_WHITE, false);

            gui.pose().pushMatrix();
            gui.pose().translate(x + minecraft.font.width(keyText) + 3 + 4, y + 4);

            if (AbilityBar.KEY_ROTATION > 0) {
                gui.pose().rotate(
                        (float) Math.toRadians(
                                Easing.inOutCubic((AbilityBar.KEY_ROTATION - minecraft.getDeltaTracker().getRealtimeDeltaTicks()) / 10F) * 360F
                                        * (AbilityBar.KEY_ROTATION_FORWARD ? -1 : 1)
                        )
                );
            }

            this.abilityList.getSprites().blit(gui, AbilityBar.KEY_ROTATION_FORWARD ? AbilityBarSprites.SWITCH_KEY_FORWARD : AbilityBarSprites.SWITCH_KEY_BACKWARD, DataContext.forPower(minecraft.player, this.abilityList.getPowerInstance()), -4, -4, 8, 8);
            gui.pose().popMatrix();
        }
    }
}
