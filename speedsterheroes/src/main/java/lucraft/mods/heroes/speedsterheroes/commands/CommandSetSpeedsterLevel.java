package lucraft.mods.heroes.speedsterheroes.commands;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class CommandSetSpeedsterLevel extends CommandBase {

	@Override
	public String getName() {
		return "setspeedsterlevel";
	}

	public int getRequiredPermissionLevel() {
		return 2;
	}
	
	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.setspeedsterlevel.usage";
	}

	// /setspeedsterlevel player level
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		sender.sendMessage(new TextComponentString(TextFormatting.RED + "Old Command! Use /setsuperpowerlevel instead!"));
	}

	protected String[] getListOfPlayerUsernames(MinecraftServer server) {
		return server.getOnlinePlayerNames();
	}

	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		return args.length == 1 ? (getListOfStringsMatchingLastWord(args, this.getListOfPlayerUsernames(sender.getServer()))) : new ArrayList<String>();
	}
	
}
