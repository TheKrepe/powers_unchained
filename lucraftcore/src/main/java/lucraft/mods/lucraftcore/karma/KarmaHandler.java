package lucraft.mods.lucraftcore.karma;

import lucraft.mods.lucraftcore.karma.KarmaStat.KarmaClass;
import lucraft.mods.lucraftcore.karma.KarmaStat.KarmaPlayerType;
import lucraft.mods.lucraftcore.karma.capabilities.CapabilityKarma;
import lucraft.mods.lucraftcore.karma.capabilities.IKarmaCapability;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.biome.Biome;

public class KarmaHandler {

    public static int getKarma(EntityPlayer player) {
        return player.getCapability(CapabilityKarma.KARMA_CAP, null).getKarma();
    }

    public static KarmaClass getKarmaClass(EntityPlayer player) {
        for (KarmaClass c : KarmaClass.values()) {
            if (c.hasPlayerThisClass(player))
                return c;
        }

        return KarmaClass.NEUTRAL;
    }

    public static KarmaPlayerType getKarmaPlayerType(EntityPlayer player) {
        for (KarmaPlayerType c : KarmaPlayerType.values()) {
            if (c.hasPlayerThisType(player))
                return c;
        }

        return KarmaPlayerType.NEUTRAL;
    }

    public static int getKarmaStat(EntityPlayer player, KarmaStat stat) {
        return player.getCapability(CapabilityKarma.KARMA_CAP, null).getKarmaStat(stat);
    }

    public static void setKarmaStat(EntityPlayer player, KarmaStat stat, int amount) {
        player.getCapability(CapabilityKarma.KARMA_CAP, null).setKarmaStat(stat, MathHelper.clamp(amount, stat.getMin(), stat.getMax()));
    }

    public static void increaseKarmaStat(EntityPlayer player, KarmaStat stat) {
        increaseKarmaStat(player, stat, 1);
    }

    public static void increaseKarmaStat(EntityPlayer player, KarmaStat stat, int amount) {
        IKarmaCapability cap = player.getCapability(CapabilityKarma.KARMA_CAP, null);
        cap.setKarmaStat(stat, MathHelper.clamp(cap.getKarmaStat(stat) + amount, stat.getMin(), stat.getMax()));
    }

    public static void decreaseKarmaStat(EntityPlayer player, KarmaStat stat) {
        decreaseKarmaStat(player, stat, 1);
    }

    public static void decreaseKarmaStat(EntityPlayer player, KarmaStat stat, int amount) {
        increaseKarmaStat(player, stat, -amount);
    }

    public static boolean isGoodPlayer(EntityPlayer player) {
        return getKarmaPlayerType(player) == KarmaPlayerType.GOOD;
    }

    public static boolean isEvilPlayer(EntityPlayer player) {
        return getKarmaPlayerType(player) == KarmaPlayerType.BAD;
    }

    public static boolean isEvilEntity(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            return isEvilPlayer((EntityPlayer) entity);
        } else {
            return isMonster(entity);
        }

    }

    public static boolean isMonster(EntityLivingBase entity) {
        for (Biome.SpawnListEntry entry : entity.world.getBiome(entity.getPosition()).getSpawnableList(EnumCreatureType.MONSTER)) {
            if (entry.entityClass == entity.getClass()) {
                return true;
            }
        }

        return false;
    }

    public static boolean isGoodEntity(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            return isGoodPlayer((EntityPlayer) entity);
        } else {
            return entity instanceof EntityVillager;
        }
    }

    public static boolean isEntityAnimal(EntityLivingBase entity) {
        if (entity instanceof EntityVillager)
            return false;

        boolean b = false;
        for (Biome.SpawnListEntry entry : entity.world.getBiome(entity.getPosition()).getSpawnableList(EnumCreatureType.MONSTER)) {
            if (entry.entityClass == entity.getClass()) {
                b = true;
            }
        }
        return entity instanceof IAnimals || b;
    }

}
