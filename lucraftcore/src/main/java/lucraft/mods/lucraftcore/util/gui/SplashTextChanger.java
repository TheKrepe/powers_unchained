package lucraft.mods.lucraftcore.util.gui;

import lucraft.mods.lucraftcore.LucraftCore;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.*;

@Mod.EventBusSubscriber(modid = LucraftCore.MODID, value = Side.CLIENT)
public class SplashTextChanger {

    @SubscribeEvent
    public static void onInitGuiPost(GuiScreenEvent.InitGuiEvent.Post event) {
        if (event.getGui() instanceof GuiMainMenu) {
            GuiMainMenu gui = (GuiMainMenu) event.getGui();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());

            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int year = calendar.get(Calendar.YEAR);

            if ((month == Calendar.NOVEMBER && year == 2018) || (month == Calendar.NOVEMBER && day == 12) || (month == Calendar.DECEMBER && day == 28)) {
                Random random = new Random();
                List<String> list = Arrays.asList("R.I.P. Stan Lee", "Excelsior!", "'Nuff said!", "With great power comes great responsibility!", "I guess one person can make a difference", "Superheroes in New York? Gimme a break", "Are you...Tony Stank?", "I have so much more stories to tell!", "The 60's were fun but now I'm payin' for it!");
                String s = list.get(random.nextInt(list.size()));
                ObfuscationReflectionHelper.setPrivateValue(GuiMainMenu.class, (GuiMainMenu) event.getGui(), s, "splashText", "field_73975_c");
            }
        }
    }

}
