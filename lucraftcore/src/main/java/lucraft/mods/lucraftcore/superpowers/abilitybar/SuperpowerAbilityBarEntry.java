package lucraft.mods.lucraftcore.superpowers.abilitybar;

import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.events.AbilityKeyEvent;
import lucraft.mods.lucraftcore.superpowers.network.MessageAbilityKey;
import lucraft.mods.lucraftcore.util.abilitybar.EnumAbilityBarColor;
import lucraft.mods.lucraftcore.util.abilitybar.IAbilityBarEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraftforge.common.MinecraftForge;

public class SuperpowerAbilityBarEntry implements IAbilityBarEntry {

    public Ability ability;

    public SuperpowerAbilityBarEntry(Ability ability) {
        this.ability = ability;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void onButtonPress() {
        if (MinecraftForge.EVENT_BUS.post(new AbilityKeyEvent.Client(ability, true)))
            return;
        LCPacketDispatcher.sendToServer(new MessageAbilityKey(true, this.ability.getKey(), this.ability.context));
    }

    @Override
    public void onButtonRelease() {
        if (MinecraftForge.EVENT_BUS.post(new AbilityKeyEvent.Client(ability, false)))
            return;
        LCPacketDispatcher.sendToServer(new MessageAbilityKey(false, this.ability.getKey(), this.ability.context));
    }

    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        Ability.drawIcon(this.ability, mc, gui, x, y);
        this.ability.drawAdditionalInfo(mc, gui, x, y);
    }

    @Override
    public String getDescription() {
        return this.ability.getDisplayName();
    }

    @Override
    public boolean renderCooldown() {
        return this.ability.renderCooldown();
    }

    @Override
    public float getCooldownPercentage() {
        return this.ability.getCooldownPercentage();
    }

    @Override
    public boolean showKey() {
        return this.ability.getAbilityType() != Ability.AbilityType.CONSTANT;
    }

    @Override
    public EnumAbilityBarColor getColor() {
        return this.ability.getDataManager().get(Ability.BAR_COLOR);
    }
}
