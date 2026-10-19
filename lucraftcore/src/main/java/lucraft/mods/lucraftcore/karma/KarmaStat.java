package lucraft.mods.lucraftcore.karma;

import lucraft.mods.lucraftcore.LucraftCore;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistry.AddCallback;
import net.minecraftforge.registries.IForgeRegistryInternal;
import net.minecraftforge.registries.RegistryBuilder;
import net.minecraftforge.registries.RegistryManager;

import java.util.*;

public class KarmaStat extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<KarmaStat> {

    public static IForgeRegistry<KarmaStat> KARMA_STAT_REGISTRY;

    public static KarmaStat MONSTER_KILL;
    public static KarmaStat GOOD_PLAYER_KILL;
    public static KarmaStat BAD_PLAYER_KILL;
    public static KarmaStat GOOD_KNOCKOUT;
    public static KarmaStat BAD_KNOCKOUT;
    public static KarmaStat TAMING;
    public static KarmaStat SAVES;
    public static KarmaStat THEFTS;
    public static KarmaStat MODIFIED;

    public static List<KarmaStat> getKarmaStats() {
        List<KarmaStat> list = new ArrayList<KarmaStat>();
        for (KarmaStat stat : KARMA_STAT_REGISTRY.getValues())
            list.add(stat);
        list.sort(new KarmaStatComparator());
        return list;
    }

    private String name;
    private int amount;

    public KarmaStat(String name) {
        this(name, 0);
    }

    public KarmaStat(String name, int amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return name;
    }

    public String getUnlocalizedName() {
        return "karma.karmastats." + getName();
    }

    public String getUnlocalizedDescription() {
        return "karma.karmastats." + getName() + ".desc";
    }

    public int getAmount() {
        return amount;
    }

    public int getMin() {
        if (this == MODIFIED)
            return Integer.MIN_VALUE;

        return 0;
    }

    public int getMax() {
        return Integer.MAX_VALUE;
    }

    @EventBusSubscriber(modid = LucraftCore.MODID)
    public static class EventHandler {

        @SubscribeEvent
        public static void onRegisterNewRegistries(RegistryEvent.NewRegistry e) {
            KARMA_STAT_REGISTRY = new RegistryBuilder<KarmaStat>().setName(new ResourceLocation(LucraftCore.MODID, "karma_stats")).setType(KarmaStat.class).setIDRange(0, 512).add(new KarmaStatRegistryCallback()).create();
        }

        @SubscribeEvent
        public static void onRegisterKarmaStats(RegistryEvent.Register<KarmaStat> e) {
            e.getRegistry().register(MONSTER_KILL = new KarmaStat("monster_kill", +2).setRegistryName(LucraftCore.MODID, "monster_kill"));
            e.getRegistry().register(GOOD_PLAYER_KILL = new KarmaStat("good_player_kill", -3).setRegistryName(LucraftCore.MODID, "good_player_kill"));
            e.getRegistry().register(BAD_PLAYER_KILL = new KarmaStat("bad_player_kill", +2).setRegistryName(LucraftCore.MODID, "bad_player_kill"));
            e.getRegistry().register(GOOD_KNOCKOUT = new KarmaStat("good_knockout", -1).setRegistryName(LucraftCore.MODID, "good_knockout"));
            e.getRegistry().register(BAD_KNOCKOUT = new KarmaStat("bad_knockout", +3).setRegistryName(LucraftCore.MODID, "bad_knockout"));
            e.getRegistry().register(TAMING = new KarmaStat("taming", +1).setRegistryName(LucraftCore.MODID, "taming"));
            e.getRegistry().register(SAVES = new KarmaStat("saves", +3).setRegistryName(LucraftCore.MODID, "saves"));
            e.getRegistry().register(THEFTS = new KarmaStat("thefts", -2).setRegistryName(LucraftCore.MODID, "thefts"));
            e.getRegistry().register(MODIFIED = new KarmaStat("modified", 1).setRegistryName(LucraftCore.MODID, "modified"));
        }

    }

    public static class KarmaStatComparator implements Comparator<KarmaStat> {

        @Override
        public int compare(KarmaStat ks1, KarmaStat ks2) {
            int id1 = KarmaStatRegistryCallback.SORTING_IDS_BY_ENTRY.get(ks1.getRegistryName());
            int id2 = KarmaStatRegistryCallback.SORTING_IDS_BY_ENTRY.get(ks2.getRegistryName());

            if (id1 > id2)
                return 1;
            else if (id1 < id2)
                return -1;

            return 0;
        }

    }

    public static class KarmaStatRegistryCallback implements AddCallback<KarmaStat> {

        public static Map<Integer, ResourceLocation> SORTING_IDS_BY_ID = new HashMap<Integer, ResourceLocation>();
        public static Map<ResourceLocation, Integer> SORTING_IDS_BY_ENTRY = new HashMap<ResourceLocation, Integer>();
        public static int id = 0;

        @Override
        public void onAdd(IForgeRegistryInternal<KarmaStat> owner, RegistryManager stage, int id, KarmaStat obj, KarmaStat oldObj) {
            if (!SORTING_IDS_BY_ENTRY.containsKey(obj.getRegistryName())) {
                SORTING_IDS_BY_ID.put(KarmaStatRegistryCallback.id, obj.getRegistryName());
                SORTING_IDS_BY_ENTRY.put(obj.getRegistryName(), KarmaStatRegistryCallback.id);
                KarmaStatRegistryCallback.id++;
            }
        }

    }

    public static enum KarmaPlayerType {

        BAD(Integer.MIN_VALUE, -26), NEUTRAL(-25, 25), GOOD(26, Integer.MAX_VALUE);

        private int min;
        private int max;

        private KarmaPlayerType(int min, int max) {
            this.min = min;
            this.max = max;
        }

        public int getMinKarma() {
            return min;
        }

        public int getMaxKarma() {
            return max;
        }

        public boolean hasPlayerThisType(EntityPlayer player) {
            int karma = KarmaHandler.getKarma(player);

            return karma <= getMaxKarma() && karma >= getMinKarma();
        }

    }

    public static enum KarmaClass {

        TYRANT("tyrant", Integer.MIN_VALUE, -501), SUPERVILLAIN("superVillain", -500, -201), VILLAIN("villain", -200, -101), THUG("thug", -100, -26), NEUTRAL("neutral", -25, 25), VIGILANTE("vigilante", 26, 100), HERO("hero", 101, 200), SUPERHERO("superHero", 201, 500), LEGEND("legend", 501, Integer.MAX_VALUE);

        private String name;
        private int min;
        private int max;

        private KarmaClass(String name, int min, int max) {
            this.name = name;
            this.min = min;
            this.max = max;
        }

        public String getName() {
            return name;
        }

        public String getUnlocalizedName() {
            return "karma.karmaclass." + getName();
        }

        public int getMinKarma() {
            return min;
        }

        public int getMaxKarma() {
            return max;
        }

        public boolean hasPlayerThisClass(EntityPlayer player) {
            int karma = KarmaHandler.getKarma(player);

            return karma <= getMaxKarma() && karma >= getMinKarma();
        }

    }

}
