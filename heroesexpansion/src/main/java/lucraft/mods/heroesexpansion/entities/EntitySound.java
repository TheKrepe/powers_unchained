package lucraft.mods.heroesexpansion.entities;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class EntitySound extends Entity {

    public String title;

    public EntitySound(World worldIn) {
        super(worldIn);
        this.setSize(0.1F, 0.1F);
    }

    public EntitySound(World worldIn, ISound sound) {
        this(worldIn);
        this.setPosition(sound.getXPosF(), sound.getYPosF(), sound.getZPosF());
        SoundEventAccessor soundeventaccessor = sound.createAccessor(Minecraft.getMinecraft().getSoundHandler());
        this.title = soundeventaccessor.getSubtitle() == null ? "" : soundeventaccessor.getSubtitle().getFormattedText();
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();

        if (this.ticksExisted > 100)
            this.setDead();
    }

    @Override
    public boolean getAlwaysRenderNameTag() {
        return true;
    }

    @Override
    public boolean getAlwaysRenderNameTagForRender() {
        return true;
    }

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public String getCustomNameTag() {
        return this.title;
    }

    @Override
    protected void entityInit() {

    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {

    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {

    }
}
