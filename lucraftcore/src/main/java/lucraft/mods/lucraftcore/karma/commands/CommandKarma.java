package lucraft.mods.lucraftcore.karma.commands;

import lucraft.mods.lucraftcore.karma.KarmaHandler;
import lucraft.mods.lucraftcore.karma.KarmaStat;
import lucraft.mods.lucraftcore.karma.KarmaStat.KarmaClass;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraft.util.text.event.HoverEvent.Action;

import javax.annotation.Nullable;
import javax.naming.NoPermissionException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandKarma extends CommandBase {

    @Override
    public String getName() {
        return "karma";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "commands.karma.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            if (sender.canUseCommand(2, getName())) {
                EntityPlayer player = getPlayer(server, sender, args[1]);
                int amount = parseInt(args[2]);
                KarmaHandler.setKarmaStat(player, KarmaStat.MODIFIED, amount);
                sender.sendMessage(new TextComponentTranslation("commands.karma.karmaset", new Object[]{player.getName(), amount}));
                notifyCommandListener(sender, this, 1, "commands.karma.karmaset", new Object[]{player.getName(), amount});
            } else {
                try {
                    throw new NoPermissionException();
                } catch (NoPermissionException e) {
                    e.printStackTrace();
                }
            }
        } else if (args.length == 0) {
            sendKarmaInfoMessage(sender, getCommandSenderAsPlayer(sender));
        } else if (args.length == 1) {
            EntityPlayer player = getPlayer(server, sender, args[0]);
            sendKarmaInfoMessage(sender, player);
        } else {
            throw new WrongUsageException("commands.karma.usage", new Object[0]);
        }
    }

    public void sendKarmaInfoMessage(ICommandSender sender, EntityPlayer player) {

        {
            TextComponentTranslation text = new TextComponentTranslation("commands.karma.playersstats", new Object[]{TextFormatting.BOLD + "" + TextFormatting.UNDERLINE + player.getName()});
            Style style = new Style();
            style.setBold(true);
            style.setUnderlined(true);
            text.setStyle(style);
            sender.sendMessage(text);
        }

        for (KarmaStat stat : KarmaStat.getKarmaStats()) {
            TextComponentString text = new TextComponentString("   ");
            text.appendSibling(new TextComponentTranslation(stat.getUnlocalizedName(), new Object[0]));
            int statAmount = KarmaHandler.getKarmaStat(player, stat);
            text.appendText(": " + statAmount);
            text.appendText(" " + TextFormatting.GRAY + TextFormatting.ITALIC + "(* " + stat.getAmount() + " = " + (stat.getAmount() * statAmount) + ")");
            Style style = new Style();
            style.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new TextComponentTranslation(stat.getUnlocalizedDescription(), new Object[0])));
            text.setStyle(style);
            sender.sendMessage(text);
        }

        {
            TextComponentString text = new TextComponentString("   ");
            text.appendSibling(new TextComponentTranslation("commands.karma.karma", new Object[0]));
            text.appendText("" + KarmaHandler.getKarma(player));
            sender.sendMessage(text);
        }

        {
            TextComponentString text = new TextComponentString("   ");
            text.appendSibling(new TextComponentTranslation("commands.karma.class", new Object[0]));
            text.appendSibling(new TextComponentTranslation(KarmaHandler.getKarmaClass(player).getUnlocalizedName(), new Object[0]));

            Style style = new Style();
            TextComponentString hover = new TextComponentString("");
            for (int i = 0; i < KarmaClass.values().length; i++) {
                KarmaClass kc = KarmaClass.values()[i];
                TextComponentTranslation t = new TextComponentTranslation(kc.getUnlocalizedName(), new Object[0]);
                if (KarmaHandler.getKarmaClass(player) == kc) {
                    Style s = new Style();
                    s.setBold(true);
                    t.setStyle(s);
                }
                hover.appendSibling(t);
                if (i < KarmaClass.values().length - 1)
                    hover.appendText("\n");
            }
            style.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, hover));
            text.setStyle(style);

            sender.sendMessage(text);
        }
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
        if (args.length == 1) {
            List<String> args1 = new ArrayList<String>();
            args1.add("set");
            for (String s : getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()))
                args1.add(s);
            return args1;
        }
        return args.length == 2 ? getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames()) : Collections.<String>emptyList();
    }

}