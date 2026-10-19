package lucraft.mods.lucraftcore.karma.events;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

public abstract class EntityKnockOutEvent extends LivingEvent {

    public EntityKnockOutEvent(EntityLivingBase entity) {
        super(entity);
    }

    @Cancelable
    public static class Start extends LivingEvent {

        private EntityPlayer player;

        public Start(EntityLivingBase entity, EntityPlayer player) {
            super(entity);
            this.player = player;
        }

        public EntityPlayer getAttacker() {
            return player;
        }

    }

    public static class WakeUp extends LivingEvent {

        public WakeUp(EntityLivingBase entity) {
            super(entity);
        }

    }

}