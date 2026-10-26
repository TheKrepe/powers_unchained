package com.star_1mp.powersunchained.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.star_1mp.powersunchained.power.Superpower;
import com.star_1mp.powersunchained.power.Superpowers;
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

/** {@code /superpower add|remove <power|all> [targets]} — gives / takes superpowers. Permission level 2. */
public final class SuperpowerCommand {

    private static final DynamicCommandExceptionType UNKNOWN = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.powers_unchained.superpower.unknown", name));

    private SuperpowerCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("superpower")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(branch("add", true))
                .then(branch("remove", false)));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> branch(String name, boolean add) {
        return Commands.literal(name)
                .then(Commands.argument("power", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                Stream.concat(Stream.of("all"), Arrays.stream(Superpower.values()).map(Superpower::id)), builder))
                        .executes(context -> run(context, List.of(context.getSource().getPlayerOrException()), add))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> run(context, EntityArgument.getPlayers(context, "targets"), add))));
    }

    private static int run(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, boolean add)
            throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "power");
        List<Superpower> powers;
        if (name.equals("all")) {
            powers = List.of(Superpower.values());
        } else {
            Superpower power = Superpower.byId(name);
            if (power == null) {
                throw UNKNOWN.create(name);
            }
            powers = List.of(power);
        }
        for (ServerPlayer player : players) {
            for (Superpower power : powers) {
                if (add) {
                    Superpowers.add(player, power);
                } else {
                    Superpowers.remove(player, power);
                }
            }
        }
        Component what = powers.size() == 1 ? powers.get(0).title() : Component.translatable("commands.powers_unchained.superpower.all");
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.powers_unchained.superpower." + (add ? "added" : "removed"), what, players.size()), true);
        return players.size();
    }
}
