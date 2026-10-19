package lucraft.mods.heroes.speedsterheroes.client;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentBase;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

public class UpdateChecker {

	private static String currentVersion = SpeedsterHeroes.VERSION;
	public static String newestVersion;
	public static String updateStatus = "NULL";
	public static boolean show = false;
	public static boolean failedConnection = false;

	public static void init() {
		MinecraftForge.EVENT_BUS.register(new UpdateChecker());
		getNewestVersion();

		if (newestVersion != null) {
			if (newestVersion.equalsIgnoreCase(currentVersion)) {
				updateStatus = "Your version is up to date!";
			} else {
				show = true;
				updateStatus = "New version is available!";
			}
		} else {
			show = true;
			failedConnection = true;
			updateStatus = "Failed to connect to check if update is available!";
		}
	}

	private static void getNewestVersion() {
		try {
			URL url = new URL("https://drive.google.com/uc?export=download&id=0B6_wwPkl6fmOR25vSVhyb0FCY1U");
			Scanner s = new Scanner(url.openStream());
			newestVersion = s.next();
//			newestVersion = newestVersion + " " + s.next();
			s.close();
		} catch (IOException ex) {
			ex.printStackTrace();
//			newestVersion = AntMan.VERSION;
		}
	}
	
	public static boolean hasShownUpdate = false;
	
	@SubscribeEvent
	public void onWorldJoin(PlayerEvent.PlayerLoggedInEvent e) {
		if(!hasShownUpdate) {
			sendInfoMessage(e.player, UpdateChecker.updateStatus, false);
			if(!UpdateChecker.failedConnection && !UpdateChecker.newestVersion.equalsIgnoreCase(SpeedsterHeroes.VERSION))
				sendDownloadMessageToPlayer(e.player);
			hasShownUpdate = true;
		}
	}
	
	private static final Style download = new Style();
	
	public static void sendDownloadMessageToPlayer(EntityPlayer player) {
		TextComponentBase chat = new TextComponentString("");
		download.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString(LucraftCoreUtil.translateToLocal("lucraftcore.info.clickdownload"))));
		
		chat.appendText(TextFormatting.DARK_GREEN + "[");
		Style data = download.createShallowCopy();
		data.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://mods.curse.com/mc-mods/minecraft/252318-speedster-heroes"));
		chat.appendSibling(new TextComponentString(TextFormatting.GREEN + "Download").setStyle(data));
		chat.appendText(TextFormatting.DARK_GREEN + "] ");
		chat.appendSibling(new TextComponentString(TextFormatting.GRAY + UpdateChecker.newestVersion));
		
		player.sendMessage(chat);
	}
	
	public static void sendInfoMessage(EntityPlayer player, String msg) {
		sendInfoMessage(player, msg, true);
	}

	public static void sendInfoMessage(EntityPlayer player, String msg, boolean translate) {
		String message = translate ? LucraftCoreUtil.translateToLocal(msg) : msg;
		if (message == null)
			message = "null";
		player.sendMessage(new TextComponentString(TextFormatting.GOLD + "[" + TextFormatting.YELLOW + "SpeedsterHeroes" + TextFormatting.GOLD + "] " + TextFormatting.GRAY + message));
	}
	
}