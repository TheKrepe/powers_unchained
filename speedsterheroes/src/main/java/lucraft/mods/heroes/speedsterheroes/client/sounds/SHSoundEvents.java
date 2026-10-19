package lucraft.mods.heroes.speedsterheroes.client.sounds;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class SHSoundEvents {

	public static SoundEvent flicker;
	public static SoundEvent vibrating;
	public static SoundEvent blackFlashAmbient;
	public static SoundEvent blackFlashHurt;
	public static SoundEvent blackFlashDeath;
	public static SoundEvent savitarBladeDraw;
	public static SoundEvent savitarBladeBack;
	
	public static void init() {
		registerSound(flicker = new SoundEvent(new ResourceLocation(SpeedsterHeroes.MODID, "flicker")).setRegistryName(new ResourceLocation(SpeedsterHeroes.MODID, "flicker")));
		registerSound(vibrating = new SoundEvent(new ResourceLocation(SpeedsterHeroes.MODID, "vibrating")).setRegistryName(new ResourceLocation(SpeedsterHeroes.MODID, "vibrating")));
		registerSound(blackFlashAmbient = new SoundEvent(new ResourceLocation(SpeedsterHeroes.MODID, "blackflash_ambient")).setRegistryName(new ResourceLocation(SpeedsterHeroes.MODID, "blackflash_ambient")));
		registerSound(blackFlashHurt = new SoundEvent(new ResourceLocation(SpeedsterHeroes.MODID, "blackflash_hurt")).setRegistryName(new ResourceLocation(SpeedsterHeroes.MODID, "blackflash_hurt")));
		registerSound(blackFlashDeath = new SoundEvent(new ResourceLocation(SpeedsterHeroes.MODID, "blackflash_death")).setRegistryName(new ResourceLocation(SpeedsterHeroes.MODID, "blackflash_death")));
		registerSound(savitarBladeDraw = new SoundEvent(new ResourceLocation(SpeedsterHeroes.MODID, "savitarBladeDraw")).setRegistryName(new ResourceLocation(SpeedsterHeroes.MODID, "savitarBladeDraw")));
		registerSound(savitarBladeBack = new SoundEvent(new ResourceLocation(SpeedsterHeroes.MODID, "savitarBladeBack")).setRegistryName(new ResourceLocation(SpeedsterHeroes.MODID, "savitarBladeBack")));
	}
	
	public static void registerSound(SoundEvent event) {
		GameRegistry.register(event);
	}
	
}
