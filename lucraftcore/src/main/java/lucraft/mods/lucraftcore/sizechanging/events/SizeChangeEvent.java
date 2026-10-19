package lucraft.mods.lucraftcore.sizechanging.events;

import lucraft.mods.lucraftcore.sizechanging.sizechanger.SizeChanger;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

public class SizeChangeEvent extends LivingEvent {

    protected float currentSize;
    protected SizeChanger sizeChanger;

    public SizeChangeEvent(EntityLivingBase entity, float currentSize, SizeChanger sizeChanger) {
        super(entity);
        this.currentSize = currentSize;
        this.sizeChanger = sizeChanger;
    }

    public float getCurrentSize() {
        return currentSize;
    }

    public SizeChanger getSizeChanger() {
        return sizeChanger;
    }

    @Cancelable
    public static class Pre extends SizeChangeEvent {

        protected float estimatedSize;

        public Pre(EntityLivingBase entity, float currentSize, float estimatedSize, SizeChanger sizeChanger) {
            super(entity, currentSize, sizeChanger);
            this.estimatedSize = estimatedSize;
        }

        public float getEstimatedSize() {
            return estimatedSize;
        }

    }

    public static class Post extends SizeChangeEvent {

        public Post(EntityLivingBase entity, float currentSize, SizeChanger sizeChanger) {
            super(entity, currentSize, sizeChanger);
        }

    }

}
