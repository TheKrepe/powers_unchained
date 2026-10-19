package net.threetag.pymtech.entity.attributes;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.*;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.threetag.pymtech.PymTech;

import java.util.Map;

public class PTAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, PymTech.MODID);

    public static final RegistryObject<Attribute> SIZE_RESISTANCE = ATTRIBUTES.register("size_resistance", () -> new RangedAttribute("pymtech.size_resistance", 0D, 0D, 5000).setShouldWatch(true));

    public static void initAttributes() {
        for (EntityType<?> value : ForgeRegistries.ENTITIES.getValues()) {
            AttributeModifierMap map = GlobalEntityTypeAttributes.getAttributesForEntity((EntityType<? extends LivingEntity>) value);
            if (map != null) {
                Map<Attribute, ModifiableAttributeInstance> oldAttributes = map.attributeMap;
                AttributeModifierMap.MutableAttribute newMap = AttributeModifierMap.createMutableAttribute();
                newMap.attributeMap.putAll(oldAttributes);
                newMap.createMutableAttribute(SIZE_RESISTANCE.get());
                GlobalEntityTypeAttributes.put((EntityType<? extends LivingEntity>) value, newMap.create());
            }
        }
    }

}
