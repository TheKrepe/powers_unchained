package lucraft.mods.lucraftcore.superpowers.abilities;

import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import lucraft.mods.lucraftcore.util.triggers.LCCriteriaTriggers;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

public abstract class AbilityAction extends Ability {

    public AbilityAction(EntityLivingBase entity) {
        super(entity);
        this.setCooldown(getMaxCooldown());
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
            } else {
                if (ticks != 0) {
                    lastTick();
                    ticks = 0;
                }

                if (hasCooldown()) {
                    if (getCooldown() > 0)
                        this.setCooldown(getCooldown() - 1);
                    else
                        setEnabled(true);
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
            if (this.hasCooldown()) {
                if (this.getCooldown() == 0) {
                    if (this.action()) {
                        this.setEnabled(false);
                        this.setCooldown(this.getMaxCooldown());
                        if (entity instanceof EntityPlayerMP)
                            LCCriteriaTriggers.EXECUTE_ABILITY.trigger((EntityPlayerMP) entity, this.getAbilityEntry());
                        for (Ability ability : getAbilities(entity).stream().filter(ability -> ability.getParentAbility() == this)
                                .toArray(Ability[]::new)) {
                            ability.onKeyPressed();
                        }
                    }
                }
            } else {
                this.action();
                for (Ability ability : getAbilities(entity).stream().filter(ability -> ability.getParentAbility() == this)
                        .toArray(Ability[]::new)) {
                    ability.onKeyPressed();
                }
                if (entity instanceof EntityPlayerMP)
                    LCCriteriaTriggers.EXECUTE_ABILITY.trigger((EntityPlayerMP) entity, this.getAbilityEntry());
            }
        }
    }

    @Override
    public void onKeyReleased() {

    }

    @Override
    public AbilityType getAbilityType() {
        return AbilityType.ACTION;
    }

    @Override
    public abstract boolean action();

}
