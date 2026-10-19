package net.threetag.pymtech.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.threetag.threecore.ThreeCoreServerConfig;

public class PTServerConfig {

    public static ThreeCoreServerConfig.EnergyConfig STRUCTURE_SHRINKER_ENERGY;
    public static ForgeConfigSpec.ConfigValue<Integer> STRUCTURE_SHRINKER_ENERGY_PER_BLOCK;
    public static ForgeConfigSpec.ConfigValue<Integer> STRUCTURE_SHRINKER_PARTICLE_AMOUNT;

    public static ForgeConfigSpec.ConfigValue<Boolean> NAUSEA_WHEN_SMALL;
    public static ForgeConfigSpec.ConfigValue<Boolean> PASSING_OUT_WHEN_GIANT;
    public static ForgeConfigSpec.ConfigValue<Boolean> HUNGER;

    public static ForgeConfigSpec generateConfig() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Structure Shrinker settings").push("blocks.structureShrinker");
        STRUCTURE_SHRINKER_ENERGY = new ThreeCoreServerConfig.EnergyConfig(builder, "structureShrinker", 80000, 20);
        STRUCTURE_SHRINKER_ENERGY_PER_BLOCK = builder.defineInRange("structureShrinker.energyPerBlock", 10, 1, 100);
        STRUCTURE_SHRINKER_PARTICLE_AMOUNT = builder.defineInRange("structureShrinker.particleAmount", 1000, 1, 5000);
        builder.pop().pop();

        builder.comment("Settings for Pym Particle Size Changing").push("sizeChanging");
        NAUSEA_WHEN_SMALL = builder.comment("Gives you nausea when you stay small for too long").define("nauseaWhenSmall", true);
        PASSING_OUT_WHEN_GIANT = builder.comment("Makes you pass out when you stay giant for too long").define("passingOutWhenGiant", true);
        HUNGER = builder.comment("Increases your hunger when you are small or giant (requires the previous setting to be on)").define("hunger", true);
        builder.pop();

        return builder.build();
    }

}
