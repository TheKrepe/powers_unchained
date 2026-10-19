package lucraft.mods.heroesexpansion.suitsets;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.lucraftcore.superpowers.suitsets.RegisterSuitSetEvent;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@EventBusSubscriber(modid = HeroesExpansion.MODID)
public class HESuitSet extends SuitSet {

    public static final HESuitSet GREEN_ARROW = new SuitSetArrow("green_arrow");
    public static final HESuitSet SUPERMAN = new SuitSetSuperman("superman");
    public static final HESuitSet BLACK_PANTHER = new SuitSetBlackPanther("black_panther");
    public static final HESuitSet THOR = new SuitSetThor("thor");
    public static final HESuitSet THOR_IW = new SuitSetThor("thor_iw");
    public static final HESuitSet CAPTAIN_AMERICA = new SuitSetCaptainAmerica("captain_america");
    public static final HESuitSet CAPTAIN_MARVEL = new SuitSetCaptainMarvel("captain_marvel");
    public static final HESuitSet SPIDER_MAN_HOMEMADE = new SuitSetSpiderMan("spider_man_homemade", ItemArmor.ArmorMaterial.CHAIN);
    public static final HESuitSet SPIDER_MAN = new SuitSetSpiderMan("spider_man", ItemArmor.ArmorMaterial.DIAMOND);
    public static final HESuitSet FALCON = new SuitSetFalcon("falcon");
    public static final HESuitSet DAREDEVIL_HOMEMADE = new SuitSetDaredevil("daredevil_homemade", ItemArmor.ArmorMaterial.CHAIN, new ResourceLocation(HeroesExpansion.MODID, "textures/gui/hud_black.png"));
    public static final HESuitSet DAREDEVIL = new SuitSetDaredevil("daredevil", ItemArmor.ArmorMaterial.DIAMOND, new ResourceLocation(HeroesExpansion.MODID, "textures/gui/hud_red_tint.png"));
    //public static final HESuitSet VULTURE = new SuitSetVulture("vulture");

    @SubscribeEvent
    public static void onRegisterItems(RegisterSuitSetEvent e) {
        e.register(GREEN_ARROW);
        e.register(SUPERMAN);
        e.register(BLACK_PANTHER);
        e.register(THOR);
        e.register(THOR_IW);
        e.register(CAPTAIN_AMERICA);
        e.register(CAPTAIN_MARVEL);
        e.register(SPIDER_MAN_HOMEMADE);
        e.register(SPIDER_MAN);
        e.register(FALCON);
        e.register(DAREDEVIL_HOMEMADE);
        e.register(DAREDEVIL);
        //e.register(VULTURE);
    }

    @SubscribeEvent
    public static void onRegisterItems(RegistryEvent.Register<Item> e) {
        for (ResourceLocation sets : SuitSet.REGISTRY.getKeys()) {
            if (SuitSet.REGISTRY.getObject(sets) instanceof HESuitSet) {
                SuitSet.REGISTRY.getObject(sets).registerItems(e);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onRegisterModels(ModelRegistryEvent e) {
        for (ResourceLocation sets : SuitSet.REGISTRY.getKeys()) {
            if (SuitSet.REGISTRY.getObject(sets) instanceof HESuitSet) {
                SuitSet.REGISTRY.getObject(sets).registerModels();
            }
        }
    }

    // ---------------------------------------------------------------------------------

    public HESuitSet(String name) {
        super(name);
        this.setRegistryName(HeroesExpansion.MODID, name);
    }

    @Override
    public CreativeTabs getCreativeTab() {
        return HeroesExpansion.CREATIVE_TAB;
    }

}
