package lucraft.mods.heroes.speedsterheroes.superpower;

import lucraft.mods.lucraftcore.superpower.ISuperpowerPlayerRenderer;
import lucraft.mods.lucraftcore.superpower.Superpower;
import lucraft.mods.lucraftcore.superpower.SuperpowerPlayerHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public class SpeedforcePlayerRenderer implements ISuperpowerPlayerRenderer {

	@Override
	public void applyColor() {
		GlStateManager.color(1F, 1F, 0.4F);
	}
	
	@Override
	public void onRenderPlayer(RenderLivingBase<?> renderer, Minecraft mc, EntityPlayer player, Superpower superpower, SuperpowerPlayerHandler handler, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float mcScale) {
//		SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);
//		List<Ability> abilities = Ability.getCurrentPlayerAbilities(player);
//		AbilityPhasing phasing = Ability.getAbilityFromClass(abilities, AbilityPhasing.class);
//		boolean vibrate = (phasing != null && phasing.isUnlocked() && phasing.isEnabled()) || (SpeedsterHeroesUtil.getSpeedsterType(player) != null && SpeedsterHeroesUtil.getSpeedsterType(player).doesVibrate() && data.isInSpeed);
//		if(vibrate) {
//			for (int i = 0; i < 10; i++) {
//				GlStateManager.pushMatrix();
//				Random rand = new Random();
//				GlStateManager.translate((rand.nextFloat() - 0.5F) / 15, 0, (rand.nextFloat() - 0.5F) / 15);
//				GlStateManager.color(1, 1, 1, 0.3F);
//				for(EntityEquipmentSlot slots : EntityEquipmentSlot.values()) {
//					if(slots.getSlotType() == Type.ARMOR) {
//						ItemStack stack = player.getItemStackFromSlot(slots);
//						
//						if(stack != null && stack.getItem().getArmorModel(player, stack, slots, (ModelBiped) renderer.getMainModel()) != null) {
//							mc.renderEngine.bindTexture(new ResourceLocation(stack.getItem().getArmorTexture(stack, player, slots, null)));
//							stack.getItem().getArmorModel(player, stack, slots, (ModelBiped) renderer.getMainModel()).render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, mcScale);
//						}
//					}
//				}
//				GlStateManager.enableBlend();
//				mc.renderEngine.bindTexture(((EntityPlayerSP)player).getLocationSkin());
//				renderer.getMainModel().render(player, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, mcScale);
//				GlStateManager.disableBlend();
//				GlStateManager.popMatrix();
//			}
//		}
	}

}
