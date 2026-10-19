package lucraft.mods.lucraftcore.util.updatechecker;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Scanner;

public class UpdateChecker {

    protected String currentVersion;
    protected String prefix;
    protected String website;
    protected String updateFile;

    protected String newestVersion;
    protected String updateStatus = "NULL";
    protected boolean show = false;

    public UpdateChecker(String currentVersion, String prefix, String website, String updateFile) {
        this.currentVersion = currentVersion;
        this.prefix = prefix;
        this.website = website;
        this.updateFile = updateFile;

        setNewestVersion();

        if (newestVersion != null && !newestVersion.equalsIgnoreCase(currentVersion)) {
            show = true;
            updateStatus = "New version is available!";
        }

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setNewestVersion() {
        try {
            URL url = new URL(this.updateFile);
            Scanner s = new Scanner(url.openStream());
            newestVersion = s.next();
            s.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onWorldJoin(PlayerLoggedInEvent e) {
        if (show) {
            sendInfoMessage(e.player, "lucraftcore.info.newupdateavailable");
            sendDownloadMessageToPlayer(e.player);
            show = false;
        }
    }

    public void sendDownloadMessageToPlayer(EntityPlayer player) {
        TextComponentBase chat = new TextComponentString("");
        Style download = new Style();
        download.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("lucraftcore.info.clickdownload")));

        chat.appendText(TextFormatting.DARK_GREEN + "[");
        Style data = download.createShallowCopy();
        data.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, this.website));
        chat.appendSibling(new TextComponentString(TextFormatting.GREEN + "Download").setStyle(data));
        chat.appendText(TextFormatting.DARK_GREEN + "] ");
        chat.appendSibling(new TextComponentString(TextFormatting.GRAY + this.newestVersion));

        player.sendMessage(chat);
    }

    public void sendInfoMessage(EntityPlayer player, String msg) {
        TextComponentString text = new TextComponentString(this.prefix + " " + TextFormatting.GRAY);
        text.appendSibling(new TextComponentTranslation(msg));
        player.sendMessage(text);
    }

}
