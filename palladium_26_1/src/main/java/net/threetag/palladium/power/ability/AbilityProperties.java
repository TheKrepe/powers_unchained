package net.threetag.palladium.power.ability;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.Items;
import net.threetag.palladium.icon.Icon;
import net.threetag.palladium.icon.ItemIcon;
import net.threetag.palladium.logic.value.holder.IntValueHolder;
import net.threetag.palladium.logic.value.holder.TextComponentValueHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AbilityProperties {

    public static final AbilityProperties BASIC = new AbilityProperties();

    public static final Codec<AbilityProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            TextComponentValueHolder.CODEC.optionalFieldOf("title").forGetter(p -> Optional.ofNullable(p.title)),
            Icon.CODEC.optionalFieldOf("icon", new ItemIcon(Items.BARRIER)).forGetter(AbilityProperties::getIcon),
            AbilityDescription.CODEC.optionalFieldOf("description").forGetter(p -> Optional.ofNullable(p.description)),
            AbilityColor.CODEC.optionalFieldOf("color", AbilityColor.GRAY).forGetter(AbilityProperties::getColor),
            Codec.BOOL.optionalFieldOf("hidden_in_gui", false).forGetter(AbilityProperties::isHiddenInGUI),
            Codec.BOOL.optionalFieldOf("hidden_in_bar", false).forGetter(AbilityProperties::isHiddenInBar),
            IntValueHolder.CODEC.optionalFieldOf("list_index",IntValueHolder.MINUS_ONE).forGetter(AbilityProperties::getListIndex),
            AnimationTimerSetting.CODEC.optionalFieldOf("animation_timer").forGetter(p -> Optional.ofNullable(p.animationTimerSetting)),
            Codec.BOOL.optionalFieldOf("allow_dampening", true).forGetter(p -> p.allowDampening)
    ).apply(instance, (title, icon, desc, color, hiddenGui, hiddenBar, listIndex, timer, allowDampening) ->
            new AbilityProperties(title.orElse(null), icon, desc.orElse(null), color, hiddenGui, hiddenBar, listIndex, timer.orElse(null), allowDampening)));

    private TextComponentValueHolder title = null;
    private Icon icon = new ItemIcon(Items.BARRIER);
    private AbilityDescription description = null;
    private AbilityColor color = AbilityColor.GRAY;
    private boolean hiddenInGUI = false;
    private boolean hiddenInBar = false;
    private IntValueHolder listIndex = IntValueHolder.MINUS_ONE;
    private AnimationTimerSetting animationTimerSetting = null;
    private boolean allowDampening = true;

    public AbilityProperties() {

    }

    public AbilityProperties(TextComponentValueHolder title, Icon icon, AbilityDescription description, AbilityColor color,
                              boolean hiddenInGUI, boolean hiddenInBar, IntValueHolder listIndex,
                              AnimationTimerSetting animationTimerSetting,
                              boolean allowDampening) {
        this.title = title;
        this.icon = icon;
        this.description = description;
        this.color = color;
        this.hiddenInGUI = hiddenInGUI;
        this.hiddenInBar = hiddenInBar;
        this.listIndex = listIndex;
        this.animationTimerSetting = animationTimerSetting;
        this.allowDampening = allowDampening;
    }

    public AbilityProperties title(TextComponentValueHolder title) {
        this.title = title;
        return this;
    }

    public AbilityProperties icon(Icon icon) {
        this.icon = icon;
        return this;
    }

    public AbilityProperties description(AbilityDescription description) {
        this.description = description;
        return this;
    }

    public AbilityProperties color(AbilityColor color) {
        this.color = color;
        return this;
    }

    public AbilityProperties hiddenInGUI(boolean hiddenInGUI) {
        this.hiddenInGUI = hiddenInGUI;
        return this;
    }

    public AbilityProperties hiddenInBar(boolean hiddenInBar) {
        this.hiddenInBar = hiddenInBar;
        return this;
    }

    public AbilityProperties listIndex(int listIndex) {
        this.listIndex = new IntValueHolder(listIndex);
        return this;
    }

    public AbilityProperties animationTimer(AnimationTimerSetting setting) {
        this.animationTimerSetting = setting;
        return this;
    }

    @Nullable
    public TextComponentValueHolder getTitle() {
        return this.title;
    }

    public Icon getIcon() {
        return this.icon;
    }

    public AbilityDescription getDescription() {
        return this.description;
    }

    public AbilityColor getColor() {
        return this.color;
    }

    public boolean isHiddenInGUI() {
        return this.hiddenInGUI;
    }

    public boolean isHiddenInBar() {
        return this.hiddenInBar;
    }

    public IntValueHolder getListIndex() {
        return this.listIndex;
    }

    public AnimationTimerSetting getAnimationTimerSetting() {
        return this.animationTimerSetting;
    }

    public boolean canBeDampened() {
        return this.allowDampening;
    }

}
