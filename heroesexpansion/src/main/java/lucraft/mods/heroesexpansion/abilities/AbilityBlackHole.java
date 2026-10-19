package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.entities.EntityBlackHole;
import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityHeld;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import lucraft.mods.lucraftcore.util.triggers.LCCriteriaTriggers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityBlackHole extends AbilityHeld {

    public EntityBlackHole entityBlackHole;

    public AbilityBlackHole(EntityLivingBase entity) {
        super(entity);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        HEIconHelper.drawIcon(mc, gui, x, y, 2, 0);
    }

    @Override
    public void onUpdate() {
        if (isUnlocked()) {
            if (isEnabled()) {
                if (ticks == 0)
                    firstTick();
                ticks++;
                updateTick();
            } else {
                if (ticks != 0) {
                    lastTick();
                    if (this.hasCooldown())
                        this.setCooldown(this.getMaxCooldown());
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
    public void updateTick() {
        if (this.entityBlackHole != null) {
            Vec3d pos = getSingularityPos();
            this.entityBlackHole.setPositionAndUpdate(pos.x, pos.y, pos.z);
            this.entityBlackHole.setSize(entityBlackHole.getSize() + 2);
        }
    }

    @Override
    public void onKeyPressed() {
        if (this.isUnlocked()) {
            if (this.getCooldown() == 0) {
                this.setEnabled(true);
                Vec3d pos = getSingularityPos();
                this.entityBlackHole = new EntityBlackHole(this.entity.world, pos.x, pos.y, pos.z, this.entity);
                this.entity.world.spawnEntity(entityBlackHole);
            }

            for (Ability ability : getAbilities(entity).stream().filter(ability -> ability.getParentAbility() == this)
                    .toArray(Ability[]::new)) {
                ability.onKeyPressed();
            }

            if (entity instanceof EntityPlayerMP)
                LCCriteriaTriggers.EXECUTE_ABILITY.trigger((EntityPlayerMP) entity, this.getAbilityEntry());
        }
    }

    @Override
    public void onKeyReleased() {
        super.onKeyReleased();

        if (this.entityBlackHole != null) {
            this.entityBlackHole.shoot(this.entity, this.entity.rotationPitch, this.entity.rotationYaw, 0, 0.5F, 0);
            this.entityBlackHole = null;
        }
    }

    public Vec3d getSingularityPos() {
        return this.entity.getPositionVector().add(0, this.entity.getEyeHeight(), 0).add(this.entity.getLookVec().scale(this.entity.width * 3D)).subtract(0, 0.25F, 0);
    }

}
