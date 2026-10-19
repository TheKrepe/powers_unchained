package lucraft.mods.lucraftcore.sizechanging.events;

import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class EntityCanChangeInSizeEvent extends LivingEvent {

    public EntityCanChangeInSizeEvent(EntityLivingBase entity) {
        super(entity);
    }

}
