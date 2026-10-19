package lucraft.mods.lucraftcore.superpowers.abilities;

import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import net.minecraft.entity.EntityLivingBase;

public abstract class AbilityConstant extends Ability {

    public AbilityConstant(EntityLivingBase entity) {
        super(entity);
        this.dataManager.register(ENABLED, true);
    }

    @Override
    public void onUpdate() {
        this.updateConditions();

        if (isUnlocked() && isEnabled()) {
            ticks++;
            updateTick();
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

    }

    @Override
    public void onKeyReleased() {

    }

    @Override
    public AbilityType getAbilityType() {
        return AbilityType.CONSTANT;
    }

    @Override
    public abstract void updateTick();

    public boolean showInAbilityBar() {
        return false;
    }
}
