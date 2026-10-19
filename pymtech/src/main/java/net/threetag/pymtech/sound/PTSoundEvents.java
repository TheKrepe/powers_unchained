package net.threetag.pymtech.sound;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.threetag.pymtech.PymTech;

public class PTSoundEvents {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PymTech.MODID);

    public static final RegistryObject<SoundEvent> SHRINK = SOUND_EVENTS.register("shrink", () -> makeSound("shrink"));
    public static final RegistryObject<SoundEvent> SHRINK_UNDERWATER = SOUND_EVENTS.register("shrink_underwater", () -> makeSound("shrink_underwater"));
    public static final RegistryObject<SoundEvent> ENLARGE = SOUND_EVENTS.register("enlarge", () -> makeSound("enlarge"));
    public static final RegistryObject<SoundEvent> ENLARGE_UNDERWATER = SOUND_EVENTS.register("enlarge_underwater", () -> makeSound("enlarge_underwater"));
    public static final RegistryObject<SoundEvent> HELMET_OPENING = SOUND_EVENTS.register("helmet_opening", () -> makeSound("helmet_opening"));
    public static final RegistryObject<SoundEvent> HELMET_CLOSING = SOUND_EVENTS.register("helmet_closing", () -> makeSound("helmet_closing"));
    public static final RegistryObject<SoundEvent> BUTTON = SOUND_EVENTS.register("button", () -> makeSound("button"));
    public static final RegistryObject<SoundEvent> STINGER_BLASTER = SOUND_EVENTS.register("stinger_blaster", () -> makeSound("stinger_blaster"));

    public static SoundEvent makeSound(String name) {
        return new SoundEvent(new ResourceLocation(PymTech.MODID, name));
    }

}
