package lucraft.mods.lucraftcore.superpowers.abilities;

import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import lucraft.mods.lucraftcore.util.triggers.LCCriteriaTriggers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

public abstract class AbilityToggle extends Ability {

    public AbilityToggle(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void onUpdate() {
        this.updateConditions();

        if (isUnlocked()) {
            if (isEnabled()) {
                if (ticks == 0)
                    firstTick();
                ticks++;
                updateTick();

                if (hasCooldown()) {
                    if (getCooldown() >= getMaxCooldown())
                        setEnabled(false);
                    else
                        setCooldown(getCooldown() + 1);
                }
            } else {
                if (ticks != 0) {
                    lastTick();
                    ticks = 0;
                }

                if (hasCooldown()) {
                    if (getCooldown() > 0)
                        this.setCooldown(getCooldown() - 1);
                }
            }
        } else if (ticks != 0) {
            lastTick();
            ticks = 0;
        }

        if (this.dataManager.sync != null) {
            this.sync = this.sync.add(this.dataManager.sync);
            this.dataManager.sync = EnumSync.NONE;
        }
    }

    @Override
    public void onKeyPressed() {
        if (this.isUnlocked()) {
            this.action();
            for (Ability ability : getAbilities(entity).stream().filter(ability -> ability.getParentAbility() == this)
                    .toArray(Ability[]::new)) {
                ability.onKeyPressed();
            }
            if (entity instanceof EntityPlayerMP) {
                LCCriteriaTriggers.EXECUTE_ABILITY.trigger((EntityPlayerMP) entity, this.getAbilityEntry());
            }
        }
    }

    @Override
    public void onKeyReleased() {

    }

    @Override
    public AbilityType getAbilityType() {
        return AbilityType.TOGGLE;
    }

    @Override
    public abstract void updateTick();

    @Override
    public boolean action() {
        this.setEnabled(!isEnabled());
        return true;
    }

}
