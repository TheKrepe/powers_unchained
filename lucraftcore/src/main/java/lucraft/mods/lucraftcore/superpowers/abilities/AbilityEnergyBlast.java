package lucraft.mods.lucraftcore.superpowers.abilities;

import lucraft.mods.lucraftcore.superpowers.abilities.data.*;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import lucraft.mods.lucraftcore.superpowers.entities.EntityEnergyBlast;
import lucraft.mods.lucraftcore.util.helper.LCRenderHelper;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import lucraft.mods.lucraftcore.util.sounds.LCSoundEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

public class AbilityEnergyBlast extends AbilityAction {

    public static final AbilityData<Float> DAMAGE = new AbilityDataFloat("damage").disableSaving().setSyncType(EnumSync.SELF).enableSetting("damage", "The amount of damage that the energy blast entity will cause");
    public static final AbilityData<Color> COLOR = new AbilityDataColor("color").disableSaving().enableSetting("color", "Sets the color for the energy blast");
    public static final AbilityData<NBTTagCompound> ENTITY_DATA = new AbilityDataNBTTagCompound("entity_data").disableSaving().setSyncType(EnumSync.NONE).enableSetting("entity_data", "NBT tag for the entity that will be spawned");
    public static final AbilityData<Boolean> PLAY_SOUND = new AbilityDataBoolean("play_sound").disableSaving().setSyncType(EnumSync.NONE).enableSetting("play_sound", "Determines if a laser sound is played upon shooting the entity");
    public static final AbilityData<Boolean> INVISIBLE = new AbilityDataBoolean("invisible").disableSaving().setSyncType(EnumSync.NONE).enableSetting("invisible", "Makes the energy blast invisible");

    public AbilityEnergyBlast(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(DAMAGE, 3F);
        this.dataManager.register(COLOR, Color.RED);
        this.dataManager.register(ENTITY_DATA, new NBTTagCompound());
        this.dataManager.register(PLAY_SOUND, true);
        this.dataManager.register(INVISIBLE, false);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        Color color = this.dataManager.get(COLOR);
        GlStateManager.color(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
        LCRenderHelper.drawIcon(mc, gui, x, y, 0, 15);
        GlStateManager.color(1, 1, 1);
        LCRenderHelper.drawIcon(mc, gui, x, y, 0, 14);
    }

    @Override
    public boolean action() {
        if (!entity.world.isRemote) {
            EntityEnergyBlast entity = new EntityEnergyBlast(this.entity.world, this.entity, this.dataManager.get(DAMAGE), this.dataManager.get(COLOR));
            if (!this.dataManager.get(ENTITY_DATA).isEmpty()) {
                NBTTagCompound compound = this.dataManager.get(ENTITY_DATA);
                entity.readFromNBT(compound);
                if (compound.hasKey("Passengers", 9)) {
                    NBTTagList nbttaglist = compound.getTagList("Passengers", 10);

                    for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                        Entity entity1 = AnvilChunkLoader.readWorldEntityPos(nbttaglist.getCompoundTagAt(i), this.entity.world, entity.posX, entity.posY, entity.posZ, true);

                        if (entity1 != null) {
                            entity1.startRiding(entity, true);
                        }
                    }
                }
            }

            entity.setPosition(this.entity.posX, this.entity.posY + (double) this.entity.getEyeHeight() - 0.10000000149011612D, this.entity.posZ);
            entity.color = this.dataManager.get(COLOR);
            entity.shoot(this.entity, this.entity.rotationPitch, this.entity.rotationYaw, 0.0F, 1.5F, 1.0F);
            if (this.dataManager.get(INVISIBLE)) {
                entity.setInvisible(true);
            }
            this.entity.world.spawnEntity(entity);
            if (this.dataManager.get(PLAY_SOUND)) {
                PlayerHelper.playSoundToAll(this.entity.world, this.entity.posX, this.entity.posY, this.entity.posZ, 50, LCSoundEvents.ENERGY_BLAST,
                        SoundCategory.PLAYERS);
            }
        }
        return true;
    }

}
