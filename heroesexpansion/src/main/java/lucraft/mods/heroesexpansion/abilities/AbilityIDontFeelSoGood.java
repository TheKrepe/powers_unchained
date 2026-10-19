package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.network.HEPacketDispatcher;
import lucraft.mods.heroesexpansion.network.MessagePlayerChat;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityConstant;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import lucraft.mods.lucraftcore.util.particles.ParticleColoredCloud;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.Random;

public class AbilityIDontFeelSoGood extends AbilityConstant {

    public AbilityIDontFeelSoGood(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void updateTick() {
        if (this.ticks == 200 && entity instanceof EntityPlayerMP)
            HEPacketDispatcher.sendTo(new MessagePlayerChat("Mr Stark, I don't feel so good..."), (EntityPlayerMP) entity);

        Random rand = new Random();
        for (int i = 0; i < this.ticks / 10; i++) {
            double x = entity.posX + (rand.nextFloat() - 0.5F) * entity.width;
            double y = entity.posY + rand.nextFloat() * entity.height;
            double z = entity.posZ + (rand.nextFloat() - 0.5F) * entity.width;
            double xSpeed = (rand.nextFloat() - 0.5D) / 10D;
            double ySpeed = 0.2D;
            double zSpeed = (rand.nextFloat() - 0.5D) / 10D;
            double brightness = rand.nextFloat();
            PlayerHelper.spawnParticleForAll(entity.world, 50, ParticleColoredCloud.ID, x, y, z, xSpeed, ySpeed, zSpeed, (int) (45 + brightness * 90), (int) (28 + brightness * 57), (int) (19 + brightness * 37));
        }

        if (this.ticks == 400)
            this.entity.setHealth(0);
    }

}
