package lucraft.mods.heroes.speedsterheroes.util;

import java.util.HashMap;
import java.util.UUID;

import net.minecraft.entity.ai.attributes.AttributeModifier;

public class SpeedsterAttributeModifiers {

	private static HashMap<Integer, AttributeModifier> attributes = new HashMap<Integer, AttributeModifier>();
	public static final int amountOfAttributes = 35;
	
	public static AttributeModifier savitarScale;
	public static AttributeModifier velocity9;
	public static AttributeModifier velocity9StepHeight;
	
	public static void preInit() {
		for(int i = 0; i < amountOfAttributes; i++) {
			attributes.put(i + 1, (new AttributeModifier("speedsterheroes.speed", i + 1, 2)).setSaved(false));
		}
		
		savitarScale = new AttributeModifier(UUID.fromString("0669d99d-b34d-40fc-a4d8-c7ee963cc842"), "Size modifier", 1.5D, 2);
		velocity9 = new AttributeModifier(UUID.fromString("0669d99d-b34d-40fc-a4d8-c7ee963cc842"), "Velocity-9", 5, 2);
		velocity9StepHeight = new AttributeModifier(UUID.fromString("0669d99d-b34d-40fc-a4d8-c7ee963cc842"), "Velocity-9 Step height", 2, 2);
	}
	
	public static AttributeModifier getSpeedsterAttributeModifier(int i) {
		return i <= 0 ? attributes.get(1) : i > amountOfAttributes ? attributes.get(amountOfAttributes) : attributes.get(i);
	}
	
}
