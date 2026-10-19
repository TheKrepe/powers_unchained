package lucraft.mods.lucraftcore.superpowers.capabilities;

import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.AbilityContainer;
import lucraft.mods.lucraftcore.superpowers.effects.EffectTrail.EntityTrail;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.LinkedList;

public interface ISuperpowerCapability {

    EntityLivingBase getEntity();

    void setSuperpower(Superpower superpower);

    void setSuperpower(Superpower superpower, boolean update);

    Superpower getSuperpower();

    AbilityContainer getAbilityContainer(Ability.EnumAbilityContext context);

    NBTTagCompound getData();

    boolean hasPlayedBefore();

    void setHasPlayedBefore(boolean played);

    @SideOnly(Side.CLIENT)
    LinkedList<EntityTrail> getTrailEntities();

    @SideOnly(Side.CLIENT)
    void addTrailEntity(Entity entity);

    @SideOnly(Side.CLIENT)
    void removeTrailEntity(Entity entity);

    void onUpdate();

    NBTTagCompound writeNBT();

    void readNBT(NBTTagCompound nbt);

    void syncToPlayer();

    void syncToPlayer(EntityPlayer receiver);

    void syncToAll();

    boolean hasGainedSuperpower();

    void setSuperpowerGained(boolean gained);

}
