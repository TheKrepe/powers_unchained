package lucraft.mods.heroesexpansion;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = HeroesExpansion.MODID)
public class HEConfig {

    @Config.RangeDouble(min = 0D, max = 20D)
    public static double EXPLOSIVE_ARROW_STRENGTH = 2.5D;

    public static boolean UPDATE_CHECKER = true;

    @Config.Comment("This allows Thor's hammers to be made in an anvil")
    public static boolean ANVIL_CRAFTING = true;

    @Config.RequiresMcRestart
    @Config.Comment("If enabled you can make Thor's hammers in a TinkersConstruct smeltery or ThermalExpansion Fluid Transposer")
    public static boolean MOD_CRAFTING = true;

    @Config.Comment("If enabled the anvil crafting for the Infinity Gauntlet will get disabled if Tinkers Construct/ThermalExpansion is installed")
    public static boolean DISABLE_ANVIL_WITH_MODS = true;

    public static boolean HERB_GROWING_ON_VIBRANIUM = true;

    @Config.Comment("The lifetime of portals in ticks. If set to -1, it will never disappear")
    public static int PORTAL_LIFETIME = -1;

    @Config.Comment("Determines the armor value of Black Panther suits by multiplying its armor material value with this one")
    public static float BLACK_PANTHER_ARMOR_MULTIPLIER = 2.5F;

    public static WorldGeneration worldGeneration = new WorldGeneration();

    public static class WorldGeneration {

        @Config.RangeInt(min = 0, max = 50)
        public int MJOLNIR_CRATRE_CHANCE = 2;

        @Config.Comment("The delay for randomly spawning Mjolnirs in ticks. If set to -1, no hammer will fall down")
        @Config.RangeInt(min = -1)
        public int MJOLNIR_SPAWN_CHANCE = -1;

        @Config.RangeInt(min = 0, max = 50)
        public int KRYPTONIAN_METEORITE_CHANCE = 2;

        @Config.Comment("The delay for randomly spawning kryptonian meteorites in ticks. If set to -1, no meteorite will fall down")
        @Config.RangeInt(min = -1)
        public int KRYPTONIAN_METEORITE_SPAWN_CHANCE = -1;

        @Config.RangeInt(min = 0, max = 50)
        public int RADIOACTIVE_ACID_LAKE_CHANCE = 8;

        public boolean GENERATE_HEART_SHAPED_HERBS = true;

        public boolean GENERATE_NORSE_VILLAGES = true;

        public boolean GENERATE_CRASHED_KREE_SHIPS = true;

        @Config.Comment("The delay for randomly spawning crashed Kree ships in ticks. If set to -1, no ship will fall down")
        @Config.RangeInt(min = -1)
        public int CRASHED_KREE_SHIP_SPAWN_CHANCE = -1;

        public boolean GENERATE_TESSERACT = true;

    }

    @Mod.EventBusSubscriber(modid = HeroesExpansion.MODID)
    private static class EventHandler {

        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (event.getModID().equals(HeroesExpansion.MODID)) {
                ConfigManager.sync(HeroesExpansion.MODID, Config.Type.INSTANCE);
            }
        }

    }

}
