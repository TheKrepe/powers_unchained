package lucraft.mods.heroes.speedsterheroes.abilities;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.lucraftcore.abilities.Ability;
import net.minecraft.util.ResourceLocation;

public class SHAbilities {

	public static void init() {
		Ability.registerAbility(new ResourceLocation(SpeedsterHeroes.MODID, "super_speed"), AbilitySuperSpeed.class);
		Ability.registerAbility(new ResourceLocation(SpeedsterHeroes.MODID, "accelerate"), AbilityAccelerate.class);
		Ability.registerAbility(new ResourceLocation(SpeedsterHeroes.MODID, "decelerate"), AbilityDecelerate.class);
		Ability.registerAbility(new ResourceLocation(SpeedsterHeroes.MODID, "wall_running"), AbilityWallRunning.class);
		Ability.registerAbility(new ResourceLocation(SpeedsterHeroes.MODID, "water_running"), AbilityWaterRunning.class);
		Ability.registerAbility(new ResourceLocation(SpeedsterHeroes.MODID, "phasing"), AbilityPhasing.class);
		Ability.registerAbility(new ResourceLocation(SpeedsterHeroes.MODID, "lightning_throwing"), AbilityLightningThrowing.class);
		Ability.registerAbility(new ResourceLocation(SpeedsterHeroes.MODID, "time_remnant"), AbilityTimeRemnant.class);
		Ability.registerAbility(new ResourceLocation(SpeedsterHeroes.MODID, "dimension_breach"), AbilityDimensionBreach.class);
		Ability.registerAbility(new ResourceLocation(SpeedsterHeroes.MODID, "speedforce_jump"), AbilitySpeedforceJump.class);
		Ability.registerAbility(new ResourceLocation(SpeedsterHeroes.MODID, "blade"), AbilityBlade.class);
		Ability.registerAbility(new ResourceLocation(SpeedsterHeroes.MODID, "suit_ring"), AbilitySuitRing.class);
		Ability.registerAbility(new ResourceLocation(SpeedsterHeroes.MODID, "slow_motion"), AbilitySlowMotion.class);
		Ability.registerAbility(new ResourceLocation(SpeedsterHeroes.MODID, "speedforce_vision"), AbilitySpeedforceVision.class);
	}
	
}
