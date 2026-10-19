package lucraft.mods.lucraftcore.superpowers.events;

import lucraft.mods.lucraftcore.superpowers.Superpower;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class LivingSuperpowerEvent extends LivingEvent {

	private final Superpower superpower;

	public LivingSuperpowerEvent(EntityLivingBase entity, Superpower superpower) {
		super(entity);
		this.superpower = superpower;
	}

	public Superpower getSuperpower() {
		return this.superpower;
	}

	public Superpower getPreviousSuperpower() {
		return SuperpowerHandler.getSuperpower(getEntityLiving());
	}

	@Cancelable
	public static class PlayerGetsSuperpowerEvent extends LivingSuperpowerEvent {

		public PlayerGetsSuperpowerEvent(EntityPlayer player, Superpower superpower) {
			super(player, superpower);
		}

	}

}
