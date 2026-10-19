package lucraft.mods.heroes.speedsterheroes.client.sounds;

import lucraft.mods.heroes.speedsterheroes.speedstertypes.SpeedsterType;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;

public class MovingSoundFlickering extends MovingSound {

	private final EntityPlayer player;
	
	public MovingSoundFlickering(EntityPlayer player) {
		super(SHSoundEvents.flicker, SoundCategory.PLAYERS);
		this.player = player;
        this.attenuationType = ISound.AttenuationType.NONE;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.00001F;
	}

	@Override
	public void update() {
		SpeedsterType type = SpeedsterHeroesUtil.getSpeedsterType(player);
		SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);
		
		if(((type != null && type.doesFlicker()) || SpeedsterHeroesUtil.isVelocity9Active(player)) && data != null && data.isInSpeed) {
			this.volume = 0.2F;
		} else {
			this.volume = 0F;
		}
	}
	
}
