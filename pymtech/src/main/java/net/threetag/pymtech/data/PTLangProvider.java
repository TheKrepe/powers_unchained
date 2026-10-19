package net.threetag.pymtech.data;

import net.minecraft.data.DataGenerator;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.ability.PTAbilityTypes;
import net.threetag.pymtech.block.PTBlocks;
import net.threetag.pymtech.container.PTContainerTypes;
import net.threetag.pymtech.entity.PTEntityTypes;
import net.threetag.pymtech.fluid.PTFluids;
import net.threetag.pymtech.item.PTItems;
import net.threetag.pymtech.util.PTDamageSources;
import net.threetag.threecore.data.ExtendedLanguageProvider;

public abstract class PTLangProvider extends ExtendedLanguageProvider {

    public PTLangProvider(DataGenerator gen, String locale) {
        super(gen, PymTech.MODID, locale);
    }

    public static class English extends PTLangProvider {

        public English(DataGenerator gen) {
            super(gen, "en_us");
        }

        @Override
        protected void addTranslations() {
            // Items
            this.addItem(PTItems.SHRINK_DISK, "Shrink Disk");
            this.addItem(PTItems.ENLARGE_DISK, "Enlarge Disk");
            this.addItem(PTItems.ANT_ANTENNA, "Ant Antenna");
            this.addItem(PTItems.EMP_COMMUNICATION_DEVICE, "EMP Communication Device");
            this.addItem(PTItems.AIR_FILTER, "Air Filter");
            this.addItem(PTItems.SHRINKING_SIZE_COIL, "Shrinking Size Coil");
            this.addItem(PTItems.ENLARGEMENT_SIZE_COIL, "Enlargement Size Coil");
            this.addItem(PTItems.FIBER_COMPOSITE_WING, "Fiber Composite Wing");
            this.addItem(PTItems.REGULATOR, "Regulator");
            this.addItem(PTItems.STRUCTURE_SHRINKER_REMOTE, "Structure Shrinker Remote");
            this.addItem(PTItems.SHRINK_PYM_PARTICLES_BUCKET, "Shrink Pym Particles Bucket");
            this.addItem(PTItems.ENLARGE_PYM_PARTICLES_BUCKET, "Enlarge Pym Particles Bucket");
            this.addItem(PTItems.SHRUNKEN_STRUCTURE, "Shrunken Structure");


            // Suits
            this.add(PTItems.Suits.ANT_MAN_HELMET, "Ant-Man's Helmet");
            this.add(PTItems.Suits.ANT_MAN_CHESTPLATE, "Ant-Man's Chestpiece");
            this.add(PTItems.Suits.ANT_MAN_LEGGINGS, "Ant-Man's Pants");
            this.add(PTItems.Suits.ANT_MAN_BOOTS, "Ant-Man's Boots");
            this.add(PTItems.Suits.ANT_MAN_TIER2_HELMET, "Ant-Man's Helmet");
            this.add(PTItems.Suits.ANT_MAN_TIER2_CHESTPLATE, "Ant-Man's Chestpiece");
            this.add(PTItems.Suits.ANT_MAN_TIER2_LEGGINGS, "Ant-Man's Pants");
            this.add(PTItems.Suits.ANT_MAN_TIER2_BOOTS, "Ant-Man's Boots");
            this.add(PTItems.Suits.ANT_MAN_TIER3_HELMET, "Ant-Man's Helmet");
            this.add(PTItems.Suits.ANT_MAN_TIER3_CHESTPLATE, "Ant-Man's Chestpiece");
            this.add(PTItems.Suits.ANT_MAN_TIER3_LEGGINGS, "Ant-Man's Pants");
            this.add(PTItems.Suits.ANT_MAN_TIER3_BOOTS, "Ant-Man's Boots");

            this.add(PTItems.Suits.WASP_HELMET, "Wasp's Helmet");
            this.add(PTItems.Suits.WASP_CHESTPLATE, "Wasp's Chestpiece");
            this.add(PTItems.Suits.WASP_LEGGINGS, "Wasp's Pants");
            this.add(PTItems.Suits.WASP_BOOTS, "Wasp's Boots");
            this.add(PTItems.Suits.WASP_TIER2_HELMET, "Wasp's Helmet");
            this.add(PTItems.Suits.WASP_TIER2_CHESTPLATE, "Wasp's Chestpiece");
            this.add(PTItems.Suits.WASP_TIER2_LEGGINGS, "Wasp's Pants");
            this.add(PTItems.Suits.WASP_TIER2_BOOTS, "Wasp's Boots");

            this.add(PTItems.Suits.GIANT_MAN_HELMET, "Giant-Man's Mask");
            this.add(PTItems.Suits.GIANT_MAN_CHESTPLATE, "Giant-Man's Chestpiece");
            this.add(PTItems.Suits.GIANT_MAN_LEGGINGS, "Giant-Man's Pants");
            this.add(PTItems.Suits.GIANT_MAN_BOOTS, "Giant-Man's Boots");
            this.add(PTItems.Suits.ZOMBIE_GIANT_MAN_HELMET, "Zombie Giant-Man's Mask");
            this.add(PTItems.Suits.ZOMBIE_GIANT_MAN_CHESTPLATE, "Zombie Giant-Man's Chestpiece");
            this.add(PTItems.Suits.ZOMBIE_GIANT_MAN_LEGGINGS, "Zombie Giant-Man's Pants");
            this.add(PTItems.Suits.ZOMBIE_GIANT_MAN_BOOTS, "Zombie Giant-Man's Boots");


            // Suit Conditions
            this.add("item.pymtech.ant_man_chestplate.wear_complete_suit", "Entity must wear complete Ant-Man suit");
            this.add("item.pymtech.ant_man_tier2_chestplate.wear_complete_suit", "Entity must wear complete Ant-Man Tier 2 suit");
            this.add("item.pymtech.ant_man_tier3_chestplate.wear_complete_suit", "Entity must wear complete Ant-Man Tier 3 suit");
            this.add("item.pymtech.wasp_chestplate.wear_complete_suit", "Entity must wear complete Wasp suit");
            this.add("item.pymtech.wasp_tier2_chestplate.wear_complete_suit", "Entity must wear complete Wasp Tier 2 suit");
            this.add("item.pymtech.giant_man_chestplate.wear_complete_suit", "Entity must wear complete Giant-Man suit");
            this.add("item.pymtech.zombie_giant_man_chestplate.wear_complete_suit", "Entity must wear complete Zombie Giant-Man suit");
            this.add("item.pymtech.ant_man_chestplate.helmet_closed", "Helmet must be closed");
            this.add("item.pymtech.ant_man_chestplate.in_water", "Entity must be in water");
            this.add("item.pymtech.wasp_chestplate.be_small", "Entity must be small");


            // Blocks
            this.addBlock(PTBlocks.STRUCTURE_SHRINKER, "Structure Shrinker");


            // Structure Shrinker
            this.add("pymtech.structure_shrinker.from", "From: %s/%s/%s");
            this.add("pymtech.structure_shrinker.to", "To: %s/%s/%s");
            this.add("pymtech.structure_shrinker.shrink", "Shrink");
            this.add("pymtech.structure_shrinker.not_enough_energy", "The structure shrinker does not have enough energy! %s FE required!");
            this.add("pymtech.structure_shrinker.not_enough_particles", "The structure shrinker does not have enough Pym Particles! %s mb required!");
            this.add("pymtech.structure_shrinker.empty_structure", "That structure does not contain any blocks!");
            this.add("pymtech.structure_shrinker.not_connected", "The structure shrinker is not connected with the selection!");


            // Structure Shrinker Remote
            this.add("item.pymtech.structure_shrinker_remote.set_corner1", "Set corner 1 to %s/%s/%s");
            this.add("item.pymtech.structure_shrinker_remote.set_corner2", "Set corner 2 to %s/%s/%s");
            this.add("item.pymtech.structure_shrinker_remote.no_device", "A connected structure shrinker does not exist or is not available!");
            this.add("item.pymtech.structure_shrinker_remote.connected", "Successfully connected!");
            this.add("item.pymtech.structure_shrinker_remote.tooltip.device", "Device: %s/%s/%s");
            this.add("item.pymtech.structure_shrinker_remote.tooltip.no_device", "No device connected");


            // Fluids
            this.add(PTFluids.SHRINK_PYM_PARTICLES, "Shrink Pym Particles");
            this.add(PTFluids.SHRINK_PYM_PARTICLES_FLOWING, "Shrink Pym Particles");
            this.add(PTFluids.ENLARGE_PYM_PARTICLES, "Enlarge Pym Particles");
            this.add(PTFluids.ENLARGE_PYM_PARTICLES_FLOWING, "Enlarge Pym Particles");


            // Entities
            this.add(PTEntityTypes.DISCO_TRAIL, "Disco Trail");
            this.add(PTEntityTypes.PYM_PARTICLE_DISK, "Pym Particle Disk");
            this.add(PTEntityTypes.SHRUNKEN_STRUCTURE, "Shrunken Structure");


            // Container
            this.add(PTContainerTypes.STRUCTURE_SHRINKER, "Structure Shrinker");
            this.add(PTContainerTypes.REGULATOR_TIER1, "Regulator");
            this.add(PTContainerTypes.REGULATOR_TIER2, "Regulator");
            this.add(PTContainerTypes.REGULATOR_TIER3, "Regulator");


            // Abilities
            this.addAbilityType(PTAbilityTypes.REGULATOR_TIER1, "Regulator");
            this.addAbilityType(PTAbilityTypes.REGULATOR_TIER2, "Regulator");
            this.addAbilityType(PTAbilityTypes.REGULATOR_TIER3, "Regulator");
            this.add("ability.pymtech.regulator.size_slider", "Size: %s");

            this.addAbilityType(PTAbilityTypes.PYM_PARTICLE_SIZE_CHANGE, "Pym Particle Size Change");
            this.add("ability.pymtech.pym_particle_size_change.not_enough_shrink_particles", "You don't have enough Shrink Pym Particles!");
            this.add("ability.pymtech.pym_particle_size_change.not_enough_enlarge_particles", "You don't have enough Enlarge Pym Particles!");
            this.add("ability.pymtech.pym_particle_size_change.shrink", "Shrink");
            this.add("ability.pymtech.pym_particle_size_change.enlarge", "Enlarge");

            this.add("ability.pymtech.size_resistance", "Size Resistance");
            this.add("ability.pymtech.stinger_blaster", "Stinger Blaster");


            // Damage Sources
            this.add("death.attack." + PTDamageSources.PYM_PARTICLE_SUFFOCATE.getDamageType(), "%1$s was unable to breath");


            // Subtitles
            this.addSubtitle("shrink", "Pym Particle Shrinking");
            this.addSubtitle("shrink_underwater", "Pym Particle Shrinking (underwater)");
            this.addSubtitle("enlarge", "Pym Particle Enlarging");
            this.addSubtitle("enlarge_underwater", "Pym Particle Enlarging (underwater)");
            this.addSubtitle("helmet_opening", "Opened Helmet");
            this.addSubtitle("helmet_closing", "Closed Helmet");
            this.addSubtitle("button", "Pressed Suit Button");
            this.addSubtitle("stinger_blaster", "Stinger Blaster");


            // Universes
            this.add("universe.earth-616", "Earth-616");
            this.add("universe.earth-199999", "Earth-19999 (MCU)");
            this.add("universe.earth-2149", "Earth-2149");
        }
    }

    public static class German extends PTLangProvider {

        public German(DataGenerator gen) {
            super(gen, "de_de");
        }

        @Override
        protected void addTranslations() {
            // Items
            this.addItem(PTItems.SHRINK_DISK, "Schrumpfdisk");
            this.addItem(PTItems.ENLARGE_DISK, "Wachstumsdisk");
            this.addItem(PTItems.ANT_ANTENNA, "Ameisenantenne");
            this.addItem(PTItems.EMP_COMMUNICATION_DEVICE, "EMP Kommunikationsger\u00E4t");
            this.addItem(PTItems.AIR_FILTER, "Luftfilter");
            this.addItem(PTItems.SHRINKING_SIZE_COIL, "Schrumpfspule");
            this.addItem(PTItems.ENLARGEMENT_SIZE_COIL, "Wachstumsspule");
            this.addItem(PTItems.FIBER_COMPOSITE_WING, "K\u00FCnstlicher Fl\u00FCgel");
            this.addItem(PTItems.REGULATOR, "Regulator");
            this.addItem(PTItems.STRUCTURE_SHRINKER_REMOTE, "Blockschrumpfterfernbedienung");
            this.addItem(PTItems.SHRINK_PYM_PARTICLES_BUCKET, "Schrumpf-Pym-Partikel-Eimer");
            this.addItem(PTItems.ENLARGE_PYM_PARTICLES_BUCKET, "Wachstums-Pym-Partikel-Eimer");
            this.addItem(PTItems.SHRUNKEN_STRUCTURE, "Geschrumpfte Bl\u00F6cke");


            // Suits
            this.add(PTItems.Suits.ANT_MAN_HELMET, "Ant-Mans Helm");
            this.add(PTItems.Suits.ANT_MAN_CHESTPLATE, "Ant-Mans Anzug");
            this.add(PTItems.Suits.ANT_MAN_LEGGINGS, "Ant-Mans Hose");
            this.add(PTItems.Suits.ANT_MAN_BOOTS, "Ant-Mans Schuhe");
            this.add(PTItems.Suits.ANT_MAN_TIER2_HELMET, "Ant-Mans Helm");
            this.add(PTItems.Suits.ANT_MAN_TIER2_CHESTPLATE, "Ant-Mans Anzug");
            this.add(PTItems.Suits.ANT_MAN_TIER2_LEGGINGS, "Ant-Mans Hose");
            this.add(PTItems.Suits.ANT_MAN_TIER2_BOOTS, "Ant-Mans Schuhe");
            this.add(PTItems.Suits.ANT_MAN_TIER3_HELMET, "Ant-Mans Helm");
            this.add(PTItems.Suits.ANT_MAN_TIER3_CHESTPLATE, "Ant-Mans Anzug");
            this.add(PTItems.Suits.ANT_MAN_TIER3_LEGGINGS, "Ant-Mans Hose");
            this.add(PTItems.Suits.ANT_MAN_TIER3_BOOTS, "Ant-Mans Schuhe");

            this.add(PTItems.Suits.WASP_HELMET, "Wasps Helm");
            this.add(PTItems.Suits.WASP_CHESTPLATE, "Wasps Anzug");
            this.add(PTItems.Suits.WASP_LEGGINGS, "Wasps Hose");
            this.add(PTItems.Suits.WASP_BOOTS, "Wasps Schuhe");
            this.add(PTItems.Suits.WASP_TIER2_HELMET, "Wasps Helm");
            this.add(PTItems.Suits.WASP_TIER2_CHESTPLATE, "Wasps Anzug");
            this.add(PTItems.Suits.WASP_TIER2_LEGGINGS, "Wasps Hose");
            this.add(PTItems.Suits.WASP_TIER2_BOOTS, "Wasps Schuhe");

            this.add(PTItems.Suits.GIANT_MAN_HELMET, "Giant-Mans Maske");
            this.add(PTItems.Suits.GIANT_MAN_CHESTPLATE, "Giant-Mans Anzug");
            this.add(PTItems.Suits.GIANT_MAN_LEGGINGS, "Giant-Mans Hose");
            this.add(PTItems.Suits.GIANT_MAN_BOOTS, "Giant-Mans Schuhe");
            this.add(PTItems.Suits.ZOMBIE_GIANT_MAN_HELMET, "Zombie Giant-Mans Maske");
            this.add(PTItems.Suits.ZOMBIE_GIANT_MAN_CHESTPLATE, "Zombie Giant-Mans Anzug");
            this.add(PTItems.Suits.ZOMBIE_GIANT_MAN_LEGGINGS, "Zombie Giant-Mans Hose");
            this.add(PTItems.Suits.ZOMBIE_GIANT_MAN_BOOTS, "Zombie Giant-Mans Schuhe");


            // Suit Conditions
            this.add("item.pymtech.ant_man_chestplate.wear_complete_suit", "Kompletter Ant-Man Anzug muss getragen werden");
            this.add("item.pymtech.ant_man_tier2_chestplate.wear_complete_suit", "Kompletter Stufe 2 Ant-Man Anzug muss getragen werden");
            this.add("item.pymtech.ant_man_tier3_chestplate.wear_complete_suit", "Kompletter Stufe 3 Ant-Man Anzug muss getragen werden");
            this.add("item.pymtech.wasp_chestplate.wear_complete_suit", "Kompletter Wasp Anzug muss getragen werden");
            this.add("item.pymtech.wasp_tier2_chestplate.wear_complete_suit", "Kompletter Stufe 2 Wasp Anzug muss getragen werden");
            this.add("item.pymtech.giant_man_chestplate.wear_complete_suit", "Kompletter Giant-Man Anzug muss getragen werden");
            this.add("item.pymtech.zombie_giant_man_chestplate.wear_complete_suit", "Kompletter Zombie Giant-Man Anzug muss getragen werden");
            this.add("item.pymtech.ant_man_chestplate.helmet_closed", "Helm muss geschlossen sein");
            this.add("item.pymtech.wasp_chestplate.be_small", "Man muss klein sein");


            // Blocks
            this.addBlock(PTBlocks.STRUCTURE_SHRINKER, "Blockschrumpfer");


            // Structure Shrinker
            this.add("pymtech.structure_shrinker.from", "Von: %s/%s/%s");
            this.add("pymtech.structure_shrinker.to", "Bis: %s/%s/%s");
            this.add("pymtech.structure_shrinker.shrink", "Schrumpfen");
            this.add("pymtech.structure_shrinker.not_enough_energy", "Der Blockschrumpfer hat nicht genug Energie! %s FE ben\u00F6tigt!");
            this.add("pymtech.structure_shrinker.not_enough_particles", "Der Blockschrumpfer hat nicht genug Energie Pym-Partikel! %s mb ben\u00F6tigt!");
            this.add("pymtech.structure_shrinker.empty_structure", "In der Auswahl befinden sich keine Bl\u00F6cke!");
            this.add("pymtech.structure_shrinker.not_connected", "Der Blockschrumpfer ist nicht mit der Auswahl verbunden!");


            // Structure Shrinker Remote
            this.add("item.pymtech.structure_shrinker_remote.set_corner1", "Ecke 1 auf %s/%s/%s gesetzt");
            this.add("item.pymtech.structure_shrinker_remote.set_corner2", "Ecke 2 auf %s/%s/%s gesetzt");
            this.add("item.pymtech.structure_shrinker_remote.no_device", "Ein verbundener Blockschrumpfer existiert nicht oder ist nicht erreichbar!");
            this.add("item.pymtech.structure_shrinker_remote.connected", "Erfolgreich verbunden!");
            this.add("item.pymtech.structure_shrinker_remote.tooltip.device", "Ger\u00e4t: %s/%s/%s");
            this.add("item.pymtech.structure_shrinker_remote.tooltip.no_device", "Kein Ger\u00e4t verbunden");


            // Fluids
            this.add(PTFluids.SHRINK_PYM_PARTICLES, "Schrumpf-Pym-Partikel");
            this.add(PTFluids.SHRINK_PYM_PARTICLES_FLOWING, "Schrumpf-Pym-Partikel");
            this.add(PTFluids.ENLARGE_PYM_PARTICLES, "Wachstums-Pym-Partikel");
            this.add(PTFluids.ENLARGE_PYM_PARTICLES_FLOWING, "Wachstums-Pym-Partikel");


            // Entities
            this.add(PTEntityTypes.DISCO_TRAIL, "Disco Trail");
            this.add(PTEntityTypes.PYM_PARTICLE_DISK, "Pym-Partikel-Disk");
            this.add(PTEntityTypes.SHRUNKEN_STRUCTURE, "Geschrumpfte Bl\u00F6cke");


            // Container
            this.add(PTContainerTypes.STRUCTURE_SHRINKER, "Blockschrumpfer");
            this.add(PTContainerTypes.REGULATOR_TIER1, "Regulator");
            this.add(PTContainerTypes.REGULATOR_TIER2, "Regulator");
            this.add(PTContainerTypes.REGULATOR_TIER3, "Regulator");


            // Abilities
            this.addAbilityType(PTAbilityTypes.REGULATOR_TIER1, "Regulator");
            this.addAbilityType(PTAbilityTypes.REGULATOR_TIER2, "Regulator");
            this.addAbilityType(PTAbilityTypes.REGULATOR_TIER3, "Regulator");
            this.add("ability.pymtech.regulator.size_slider", "Gr\u00F6\u00DFe: %s");

            this.addAbilityType(PTAbilityTypes.PYM_PARTICLE_SIZE_CHANGE, "Pym-Partikel");
            this.add("ability.pymtech.pym_particle_size_change.not_enough_shrink_particles", "Du hast nicht genug Schrumpf-Pym-Partikel!");
            this.add("ability.pymtech.pym_particle_size_change.not_enough_enlarge_particles", "Du hast nicht genug Wachstums-Pym-Partikel!");
            this.add("ability.pymtech.pym_particle_size_change.shrink", "Schrumpfen");
            this.add("ability.pymtech.pym_particle_size_change.enlarge", "Vergr\u00F6\u00DFern");

            this.add("ability.pymtech.size_resistance", "Widerstandsf\u00e4higkeit");
            this.add("ability.pymtech.stinger_blaster", "Stachelblaster");


            // Damage Sources
            this.add("death.attack." + PTDamageSources.PYM_PARTICLE_SUFFOCATE.getDamageType(), "%1$s erstack");


            // Subtitles
            this.addSubtitle("shrink", "Pym-Partikel-Schrumpfen");
            this.addSubtitle("shrink_underwater", "Pym-Partikel-Schrumpfen (unter Wasser)");
            this.addSubtitle("enlarge", "Pym-Partikel-Vergr\u00F6\u00DFern");
            this.addSubtitle("enlarge_underwater", "Pym-Partikel-Vergr\u00F6\u00DFern (unter Wasser)");
            this.addSubtitle("helmet_opening", "Helm ge\u00F6ffnet");
            this.addSubtitle("helmet_closing", "Helm geschlossen");
            this.addSubtitle("button", "Knopf am Anzug gedr\u00FCckt");
            this.addSubtitle("stinger_blaster", "Stachelblaster");


            // Universes
            this.add("universe.earth-616", "Earth-616");
            this.add("universe.earth-199999", "Earth-19999 (MCU)");
            this.add("universe.earth-2149", "Earth-2149");
        }
    }

    @Override
    public String getName() {
        return "PymTech " + super.getName();
    }

}
