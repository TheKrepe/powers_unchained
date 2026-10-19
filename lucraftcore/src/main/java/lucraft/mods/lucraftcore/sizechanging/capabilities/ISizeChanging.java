package lucraft.mods.lucraftcore.sizechanging.capabilities;

import lucraft.mods.lucraftcore.sizechanging.entities.EntitySizeChanging;
import lucraft.mods.lucraftcore.sizechanging.sizechanger.SizeChanger;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import java.util.List;

public interface ISizeChanging {

    void tick();

    float getSize();

    float getRenderSize(float partialTick);

    boolean setSize(float size);

    boolean setSize(float size, SizeChanger sizeChanger);

    void setSizeDirectly(float size);

    SizeChanger getSizeChanger();

    void setOriginalSize(float width, float height);

    void changeSizeChanger(SizeChanger sizeChanger);

    float getOriginalWidth();

    float getOriginalHeight();

    EntitySizeChanging spawnEntity(SizeChanger sizeChanger, int lifeTime, float size);

    List<EntitySizeChanging> getEntities();

    NBTTagCompound writeNBT();

    void readNBT(NBTTagCompound nbt);

    void syncToPlayer(EntityPlayer receiver);

    void syncToAll();

}