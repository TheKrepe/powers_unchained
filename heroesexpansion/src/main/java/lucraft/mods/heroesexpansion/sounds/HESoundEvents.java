package lucraft.mods.heroesexpansion.sounds;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@EventBusSubscriber(modid = HeroesExpansion.MODID)
public class HESoundEvents {

    public static SoundEvent CLAWS_OUT;
    public static SoundEvent CLAWS_IN;
    public static SoundEvent SHIELD_THROW;
    public static SoundEvent SHIELD_HIT;
    public static SoundEvent HAMMER_SWING;
    public static SoundEvent HAMMER_DROP;
    public static SoundEvent HAMMER_HIT;
    public static SoundEvent HAMMER_THROW;
    public static SoundEvent WEB_SHOOT;
    public static SoundEvent SPIDER_SENSE;
    public static SoundEvent LEVIATHAN_AMBIENT;
    public static SoundEvent CHITAURI_AMBIENT;
    public static SoundEvent CHITAURI_HURT;
    public static SoundEvent CHITAURI_DEATH;
    public static SoundEvent BOING;
    public static SoundEvent WAKANDAN_SHIELD_OPEN;
    public static SoundEvent WAKANDAN_SHIELD_CLOSE;
    public static SoundEvent HEAT_VISION;
    public static SoundEvent FREEZE_BREATH;

    @SubscribeEvent
    public static void onRegisterSounds(RegistryEvent.Register<SoundEvent> e) {
        e.getRegistry().register(CLAWS_OUT = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "claws_out")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "claws_out")));
        e.getRegistry().register(CLAWS_IN = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "claws_in")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "claws_in")));
        e.getRegistry().register(SHIELD_THROW = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "shield_throw")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "shield_throw")));
        e.getRegistry().register(SHIELD_HIT = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "shield_hit")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "shield_hit")));
        e.getRegistry().register(HAMMER_SWING = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "hammer_swing")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "hammer_swing")));
        e.getRegistry().register(HAMMER_DROP = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "hammer_drop")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "hammer_drop")));
        e.getRegistry().register(HAMMER_HIT = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "hammer_hit")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "hammer_hit")));
        e.getRegistry().register(HAMMER_THROW = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "hammer_throw")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "hammer_throw")));
        e.getRegistry().register(WEB_SHOOT = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "web_shoot")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "web_shoot")));
        e.getRegistry().register(SPIDER_SENSE = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "spider_sense")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "spider_sense")));
        e.getRegistry().register(LEVIATHAN_AMBIENT = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "leviathan_ambient")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "leviathan_ambient")));
        e.getRegistry().register(CHITAURI_AMBIENT = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "chitauri_ambient")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "chitauri_ambient")));
        e.getRegistry().register(CHITAURI_HURT = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "chitauri_hurt")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "chitauri_hurt")));
        e.getRegistry().register(CHITAURI_DEATH = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "chitauri_death")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "chitauri_death")));
        e.getRegistry().register(BOING = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "boing")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "boing")));
        e.getRegistry().register(WAKANDAN_SHIELD_OPEN = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "wakandan_shield_open")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "wakandan_shield_open")));
        e.getRegistry().register(WAKANDAN_SHIELD_CLOSE = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "wakandan_shield_close")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "wakandan_shield_close")));
        e.getRegistry().register(HEAT_VISION = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "heat_vision")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "heat_vision")));
        e.getRegistry().register(FREEZE_BREATH = new SoundEvent(new ResourceLocation(HeroesExpansion.MODID, "freeze_breath")).setRegistryName(new ResourceLocation(HeroesExpansion.MODID, "freeze_breath")));
    }

}
