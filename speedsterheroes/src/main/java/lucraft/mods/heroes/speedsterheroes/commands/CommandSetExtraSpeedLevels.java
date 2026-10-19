package lucraft.mods.heroes.speedsterheroes.commands;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import lucraft.mods.heroes.speedsterheroes.SpeedsterHeroes;
import lucraft.mods.heroes.speedsterheroes.superpower.SpeedforcePlayerHandler;
import lucraft.mods.lucraftcore.superpower.SuperpowerHandler;
import lucraft.mods.lucraftcore.util.LucraftCoreUtil;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;

public class CommandSetExtraSpeedLevels extends CommandBase {

	@Override
	public String getName() {
		return "setextraspeedlevels";
	}

	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "commands.setextraspeedlevels.usage";
	}

	// /setspeedsterlevel player level

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length != 2) {
			throw new WrongUsageException("commands.setspeedsterlevel.usage", new Object[0]);
		} else {
			EntityPlayer player = getPlayer(server, sender, args[0]);
			int level = MathHelper.clamp(parseInt(args[1]), 0, 10);

			if (SuperpowerHandler.hasSuperpower(player, SpeedsterHeroes.speedforce)) {
				SpeedforcePlayerHandler data = SuperpowerHandler.getSpecificSuperpowerPlayerHandler(player, SpeedforcePlayerHandler.class);
				data.extraSpeedLevels = level;
				LucraftCoreUtil.sendSuperpowerUpdatePacket(player);

				sender.sendMessage(new TextComponentTranslation("commands.setextraspeedlevels.success.self", new Object[] { level }));

				if (player != sender) {
					notifyCommandListener(sender, this, 1, "commands.setextraspeedlevels.success.other", new Object[] { player.getName(), level });
				} else {
					notifyCommandListener(sender, this, 1, "commands.setextraspeedlevels.success.self", new Object[] { level });
				}
			} else {
				sender.sendMessage(new TextComponentTranslation("commands.setextraspeedlevels.nopower", new Object[0]));
			}
		}
	}

	protected String[] getListOfPlayerUsernames(MinecraftServer server) {
		return server.getOnlinePlayerNames();
	}

	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos pos) {
		return args.length == 1 ? (getListOfStringsMatchingLastWord(args, this.getListOfPlayerUsernames(sender.getServer()))) : new ArrayList<String>();
	}

}
