package lucraft.mods.heroes.speedsterheroes.client.models;

import java.util.Random;

import lucraft.mods.heroes.speedsterheroes.abilities.AbilityPhasing;
import lucraft.mods.heroes.speedsterheroes.config.SHConfig;
import lucraft.mods.heroes.speedsterheroes.entity.EntityTimeRemnant;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.heroes.speedsterheroes.util.SpeedsterHeroesUtil;
import lucraft.mods.lucraftcore.abilities.Ability;
import lucraft.mods.lucraftcore.client.model.ModelAdvancedBiped;
import lucraft.mods.lucraftcore.suitset.SuitSet;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;

public class ModelSpeedsterAdvancedBiped extends ModelAdvancedBiped {

	public ModelSpeedsterAdvancedBiped(float f, String normalTex, String lightTex, SuitSet hero, EntityEquipmentSlot slot, boolean hasSmallArms) {
		super(f, normalTex, lightTex, hero, slot, hasSmallArms);
	}
	
	public ModelSpeedsterAdvancedBiped(float f, String normalTex, String lightTex, SuitSet hero, EntityEquipmentSlot slot, boolean hasSmallArms, int width, int height) {
		super(f, normalTex, lightTex, hero, slot, hasSmallArms, width, height);
	}
	
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		renderModel(entity, f, f1, f2, f3, f4, f5);
		
		if(SHConfig.vibrateRender) {
			boolean shouldVibrate = false;
			
			if (entity instanceof EntityPlayer) {
				SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler((EntityPlayer) entity, SpeedforcePlayerHandler.class);
				AbilityPhasing phasing = Ability.getAbilityFromClass(Ability.getCurrentPlayerAbilities((EntityPlayer) entity), AbilityPhasing.class);
				shouldVibrate = data != null && data.isInSpeed && SpeedsterHeroesUtil.hasArmorOn((EntityPlayer) entity) && (SpeedsterHeroesUtil.getSpeedsterType((EntityLivingBase) entity).doesVibrate() || (phasing != null && phasing.isUnlocked() && phasing.isEnabled()));
			} else if (entity instanceof EntityTimeRemnant) {
				shouldVibrate = SpeedsterHeroesUtil.hasArmorOn((EntityLivingBase) entity) && SpeedsterHeroesUtil.getSpeedsterType((EntityLivingBase) entity).doesVibrate();
			}
			
			if (shouldVibrate) {
				for (int i = 0; i < 10; i++) {
					GlStateManager.pushMatrix();
					Random rand = new Random();
					GlStateManager.translate((rand.nextFloat() - 0.5F) / 15, 0, (rand.nextFloat() - 0.5F) / 15);
					GlStateManager.color(1, 1, 1, 0.3F);
					renderModel(entity, f, f1, f2, f3, f4, f5);
					GlStateManager.popMatrix();
				}
			}
		}
	}

}
