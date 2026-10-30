package com.star_1mp.powersunchained.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.star_1mp.powersunchained.platform.Services;
import com.star_1mp.powersunchained.tech.TechProgress;
import com.star_1mp.powersunchained.tech.WebModule;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code /techtable module unlock|lock <module|all> [targets]} — Tech Table module research (Upgrade tab) of the targets
 * (default: the player running the command). Locking does not take modules out of web-shooters that already have them.
 * Module ids are tab-completed. Needs permission level 2.
 */
public final class TechTableCommand {

    private static final DynamicCommandExceptionType UNKNOWN_MODULE = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.powers_unchained.techtable.unknown", name));

    private TechTableCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("techtable")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(Commands.literal("module")
                        .then(moduleCommand("unlock", true))
                        .then(moduleCommand("lock", false))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> moduleCommand(String name, boolean unlock) {
        return Commands.literal(name)
                .then(Commands.argument("module", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                Stream.concat(Stream.of("all"), Arrays.stream(WebModule.values()).map(WebModule::id)), builder))
                        .executes(context -> set(context, List.of(context.getSource().getPlayerOrException()), unlock))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> set(context, EntityArgument.getPlayers(context, "targets"), unlock))));
    }

    private static int set(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, boolean unlock)
            throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "module");
        List<WebModule> modules;
        if (name.equals("all")) {
            modules = List.of(WebModule.values());
        } else {
            WebModule module = WebModule.byId(name);
            if (module == null) {
                throw UNKNOWN_MODULE.create(name);
            }
            modules = List.of(module);
        }
        for (ServerPlayer player : players) {
            TechProgress progress = Services.GAMEPLAY.techProgress(player);
            for (WebModule module : modules) {
                progress = unlock ? progress.withModule(module) : progress.withoutModule(module);
            }
            Services.GAMEPLAY.setTechProgress(player, progress);
        }
        Component what = modules.size() == 1 ? modules.get(0).displayName()
                : Component.translatable("commands.powers_unchained.techtable.all");
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.powers_unchained.techtable." + (unlock ? "unlocked" : "locked"), what, players.size()), true);
        return players.size();
    }
}
