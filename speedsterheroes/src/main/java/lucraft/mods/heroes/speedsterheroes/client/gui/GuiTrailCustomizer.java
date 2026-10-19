package lucraft.mods.heroes.speedsterheroes.client.gui;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.heroes.speedsterheroes.trailtypes.TrailType.EnumTrailType;
import lucraft.mods.heroes.speedsterheroes.util.SpecialTrailPlayerHandler;
import lucraft.mods.lucraftcore.client.gui.GuiColorSlider;
import lucraft.mods.lucraftcore.client.gui.GuiCustomizer;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import lucraft.mods.lucraftcore.util.LCRenderHelper;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiButtonExt;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.fml.client.config.GuiSlider.ISlider;

public class GuiTrailCustomizer extends GuiCustomizer implements ISlider {

	public EnumTrailType trail;
	public float primaryRed;
	public float primaryGreen;
	public float primaryBlue;
	public float secondaryRed;
	public float secondaryGreen;
	public float secondaryBlue;
	public boolean specialColors;

	public SpeedforcePlayerHandler data;
	
	public Map<EnumTrailType, GuiButton> trailButtons = new HashMap<EnumTrailType, GuiButton>();

	@Override
	public void initGui() {
		super.initGui();

		this.xSize = 256;
		this.ySize = 189;
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;

		data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(mc.player, SpeedforcePlayerHandler.class);
		
		NBTTagCompound old = SuperpowerHandler.getSuperpowerPlayerHandler(mc.player).getStyleNBTTag();
		trail = EnumTrailType.getTrailTypeFromName(old.getString("TrailType"));
		if(trail.getRequiredLevel() > data.getLevel())
			trail = EnumTrailType.NORMAL;
		primaryRed = old.getFloat("PrimaryRed");
		primaryGreen = old.getFloat("PrimaryGreen");
		primaryBlue = old.getFloat("PrimaryBlue");
		secondaryRed = old.getFloat("SecondaryRed");
		secondaryGreen = old.getFloat("SecondaryGreen");
		secondaryBlue = old.getFloat("SecondaryBlue");
		specialColors = old.getBoolean("SpecialColors");

		this.buttonList.add(new GuiButtonExt(0, i + 4, j + 167, 50, 18, LucraftCoreUtil.translateToLocal("lucraftcore.info.save")));
		this.buttonList.add(new GuiButtonExt(1, i + 202, j + 167, 50, 18, LucraftCoreUtil.translateToLocal("gui.cancel")));

		GuiButtonExt normalTrailButton = new GuiButtonExt(2, i + 20, j + 30, 101, 18, LucraftCoreUtil.translateToLocal(EnumTrailType.NORMAL.getName()));
		trailButtons.put(EnumTrailType.NORMAL, normalTrailButton);
		buttonList.add(normalTrailButton);
		GuiButtonExt particlesTrailButton = new GuiButtonExt(3, i + 135, j + 30, 101, 18, LucraftCoreUtil.translateToLocal(EnumTrailType.PARTICLES.getName()));
		trailButtons.put(EnumTrailType.PARTICLES, particlesTrailButton);
		buttonList.add(particlesTrailButton);
		GuiButtonExt electricityTrailButton = new GuiButtonExt(4, i + 20, j + 51, 101, 18, LucraftCoreUtil.translateToLocal(EnumTrailType.ELECTRICITY.getName()));
		trailButtons.put(EnumTrailType.ELECTRICITY, electricityTrailButton);
		buttonList.add(electricityTrailButton);
		GuiButtonExt lightningTrailButton = new GuiButtonExt(5, i + 135, j + 51, 101, 18, LucraftCoreUtil.translateToLocal(EnumTrailType.LIGHTNINGS.getName()));
		trailButtons.put(EnumTrailType.LIGHTNINGS, lightningTrailButton);
		buttonList.add(lightningTrailButton);

		updateTrailButton(trail);
		
		this.buttonList.add(new GuiColorSlider(6, i + 20, j + 100, 80, 20, LucraftCoreUtil.translateToLocal("lucraftcore.info.red"), "", 0, 1, primaryRed, true, true, this));
		this.buttonList.add(new GuiColorSlider(7, i + 20, j + 120, 80, 20, LucraftCoreUtil.translateToLocal("lucraftcore.info.green"), "", 0, 1, primaryGreen, true, true, this));
		this.buttonList.add(new GuiColorSlider(8, i + 20, j + 140, 80, 20, LucraftCoreUtil.translateToLocal("lucraftcore.info.blue"), "", 0, 1, primaryBlue, true, true, this));

		this.buttonList.add(new GuiColorSlider(9, i + 135, j + 100, 80, 20, LucraftCoreUtil.translateToLocal("lucraftcore.info.red"), "", 0, 1, secondaryRed, true, true, this));
		this.buttonList.add(new GuiColorSlider(10, i + 135, j + 120, 80, 20, LucraftCoreUtil.translateToLocal("lucraftcore.info.green"), "", 0, 1, secondaryGreen, true, true, this));
		this.buttonList.add(new GuiColorSlider(11, i + 135, j + 140, 80, 20, LucraftCoreUtil.translateToLocal("lucraftcore.info.blue"), "", 0, 1, secondaryBlue, true, true, this));
		
		if(SpecialTrailPlayerHandler.specialUUIDs.contains(mc.player.getUniqueID().toString())) {
			String text = LucraftCoreUtil.translateToLocal("speedsterheroes.info.specialcolors");
			String[] symbols = text.split("");
			String newText = "";
			
			for(String s : symbols) {
				newText = newText + getRandomColor() + s;
			}
			
			this.buttonList.add(new GuiButtonSpecialColor(12, i + 20, j + 75, 216, 20, newText, this));
		}
	}

	public void updateTrailButton(EnumTrailType type) {
		this.trail = type;
		
		for (EnumTrailType types : trailButtons.keySet()) {
			if (types == type)
				trailButtons.get(types).enabled = false;
			else
				trailButtons.get(types).enabled = true;
			
			if(types.getRequiredLevel() > data.getLevel())
				trailButtons.get(types).enabled = false;
		}
	}

	@Override
	public NBTTagCompound getStyleNBTTag() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString("TrailType", trail.toString());
		nbt.setFloat("PrimaryRed", primaryRed);
		nbt.setFloat("PrimaryGreen", primaryGreen);
		nbt.setFloat("PrimaryBlue", primaryBlue);
		nbt.setFloat("SecondaryRed", secondaryRed);
		nbt.setFloat("SecondaryGreen", secondaryGreen);
		nbt.setFloat("SecondaryBlue", secondaryBlue);
		nbt.setBoolean("SpecialColors", specialColors);
		return nbt;
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		for(EnumTrailType types : EnumTrailType.values()) {
			if(isHovering(trailButtons.get(types), mouseX, mouseY)) {
				if(types.getRequiredLevel() > data.getLevel()) {
					drawHoveringText(Arrays.asList(TextFormatting.RED + LucraftCoreUtil.translateToLocal("lucraftcore.info.requireslevel").replace("%LEVEL", "" + types.getRequiredLevel())), mouseX, mouseY);
				}
			}
		}
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;

		if (SuperpowerHandler.getSuperpower(mc.player).getPlayerRenderer() != null)
			SuperpowerHandler.getSuperpower(mc.player).getPlayerRenderer().applyColor();

		mc.getTextureManager().bindTexture(GuiCustomizer.DEFAULT_TEX);
		this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);

		LCRenderHelper.drawRect(i + 99, j + 100, i + 121, j + 160, 0.1F, 0.1F, 0.1F, 1);
		LCRenderHelper.drawRect(i + 100, j + 101, i + 120, j + 159, primaryRed, primaryGreen, primaryBlue, 1);
		
		LCRenderHelper.drawRect(i + 214, j + 100, i + 236, j + 160, 0.1F, 0.1F, 0.1F, 1);
		LCRenderHelper.drawRect(i + 215, j + 101, i + 235, j + 159, secondaryRed, secondaryGreen, secondaryBlue, 1);
		
		this.drawString(mc.fontRendererObj, LucraftCoreUtil.translateToLocal("lucraftcore.info.customizer"), i + 5, j + 5, 0xffffff);
		{
			String name = LucraftCoreUtil.translateToLocal(trail.getName());
			int length = mc.fontRendererObj.getStringWidth(name);
			this.drawString(mc.fontRendererObj, name, i + 130 - length/2, j + 19, 0xffffff);
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id == 0)
			sendStyleNBTTagToServer();
		if (button.id == 1)
			mc.player.closeScreen();
		if(button.id == 2)
			updateTrailButton(EnumTrailType.NORMAL);
		if(button.id == 3)
			updateTrailButton(EnumTrailType.PARTICLES);
		if(button.id == 4)
			updateTrailButton(EnumTrailType.ELECTRICITY);
		if(button.id == 5)
			updateTrailButton(EnumTrailType.LIGHTNINGS);
		else if(button.id == 12)
			this.specialColors = !this.specialColors;
	}
	
	@Override
	public void onChangeSliderValue(GuiSlider slider) {
		if (slider.id == 6)
			this.primaryRed = (float) slider.sliderValue;
		else if (slider.id == 7)
			this.primaryGreen = (float) slider.sliderValue;
		else if (slider.id == 8)
			this.primaryBlue = (float) slider.sliderValue;
		else if (slider.id == 9)
			this.secondaryRed = (float) slider.sliderValue;
		else if (slider.id == 10)
			this.secondaryGreen = (float) slider.sliderValue;
		else if (slider.id == 11)
			this.secondaryBlue = (float) slider.sliderValue;
	}
	
	public TextFormatting getRandomColor() {
		Random rand = new Random();
		
		TextFormatting c = TextFormatting.values()[rand.nextInt(TextFormatting.values().length)];
		
		if(c == TextFormatting.BOLD || c == TextFormatting.ITALIC || c == TextFormatting.OBFUSCATED || c == TextFormatting.RESET || c == TextFormatting.UNDERLINE || c == TextFormatting.STRIKETHROUGH)
			return getRandomColor();
		else
			return c;
	}

	public boolean isHovering(GuiButton button, int mouseX, int mouseY) {
		return mouseX >= button.xPosition && mouseX < button.xPosition + button.width && mouseY >= button.yPosition && mouseY < button.yPosition + button.height;
	}
	
}
