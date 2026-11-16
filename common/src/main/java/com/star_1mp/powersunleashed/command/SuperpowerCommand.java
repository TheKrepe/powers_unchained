package com.star_1mp.powersunleashed.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.star_1mp.powersunleashed.power.Superpower;
import com.star_1mp.powersunleashed.power.Superpowers;
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
 * {@code /superpower add|remove <power|all> [targets]} — gives / takes superpowers; {@code xp <power> <amount> [targets]} —
 * power experience; {@code skill unlock|lock <power> <skill|all> [targets]} — skill-tree nodes. Permission level 2.
 */
public final class SuperpowerCommand {

    private static final DynamicCommandExceptionType UNKNOWN = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.powers_unleashed.superpower.unknown", name));

    private static final DynamicCommandExceptionType UNKNOWN_SKILL = new DynamicCommandExceptionType(
            name -> Component.translatable("commands.powers_unleashed.superpower.unknown_skill", name));

    private SuperpowerCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("superpower")
                .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .then(branch("add", true))
                .then(branch("remove", false))
                .then(Commands.literal("xp").then(powerArg()
                        .then(Commands.argument("amount", com.mojang.brigadier.arguments.IntegerArgumentType.integer(0))
                                .executes(context -> xp(context, List.of(context.getSource().getPlayerOrException())))
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .executes(context -> xp(context, EntityArgument.getPlayers(context, "targets")))))))
                .then(Commands.literal("skill")
                        .then(skillBranch("unlock", true))
                        .then(skillBranch("lock", false))));
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> powerArg() {
        return Commands.argument("power", StringArgumentType.word())
                .suggests((context, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(Superpower.values()).map(Superpower::id), builder));
    }

    /** {@code /superpower xp <power> <amount> [targets]}: adds power experience. */
    private static int xp(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players) throws CommandSyntaxException {
        Superpower power = power(context);
        int amount = com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(context, "amount");
        for (ServerPlayer player : players) {
            com.star_1mp.powersunleashed.power.PowerSkills.addXp(player, power, amount);
            com.star_1mp.powersunleashed.power.PowerSkills.flushXp(player);
        }
        context.getSource().sendSuccess(() -> Component.translatable("commands.powers_unleashed.superpower.xp", amount, power.title(),
                players.size()), true);
        return players.size();
    }

    /** {@code /superpower skill unlock|lock <power> <skill|all> [targets]}: skill-tree nodes without points. */
    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack> skillBranch(String name, boolean unlock) {
        return Commands.literal(name).then(powerArg()
                .then(Commands.argument("skill", StringArgumentType.word())
                        .suggests((context, builder) -> {
                            Superpower power = Superpower.byId(StringArgumentType.getString(context, "power"));
                            Stream<String> ids = power == null ? Stream.empty()
                                    : com.star_1mp.powersunleashed.power.skill.SkillTrees.of(power).stream().map(n -> n.id());
                            return SharedSuggestionProvider.suggest(Stream.concat(Stream.of("all"), ids), builder);
                        })
                        .executes(context -> skill(context, List.of(context.getSource().getPlayerOrException()), unlock))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> skill(context, EntityArgument.getPlayers(context, "targets"), unlock)))));
    }

    private static int skill(CommandContext<CommandSourceStack> context, Collection<ServerPlayer> players, boolean unlock)
            throws CommandSyntaxException {
        Superpower power = power(context);
        String name = StringArgumentType.getString(context, "skill");
        List<String> ids = new java.util.ArrayList<>();
        for (var node : com.star_1mp.powersunleashed.power.skill.SkillTrees.of(power)) {
            if (name.equals("all") || node.id().equals(name)) {
                ids.add(node.id());
            }
        }
        if (ids.isEmpty()) {
            throw UNKNOWN_SKILL.create(name);
        }
        for (ServerPlayer player : players) {
            var progress = com.star_1mp.powersunleashed.platform.Services.GAMEPLAY.powerProgress(player);
            for (String id : ids) {
                progress = progress.withNode(power, id, unlock);
            }
            com.star_1mp.powersunleashed.platform.Services.GAMEPLAY.setPowerProgress(player, progress);
            com.star_1mp.powersunleashed.power.PowerSkills.applyAttributes(player);
        }
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.powers_unleashed.superpower.skill_" + (unlock ? "unlocked" : "locked"), ids.size(), players.size()), true);
        return players.size();
    }

    private static Superpower power(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String name = StringArgumentType.getString(context, "power");
        Superpower power = Superpower.byId(name);
        if (power == null) {
            throw UNKNOWN.create(name);
        }
        return power;
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
        Component what = powers.size() == 1 ? powers.get(0).title() : Component.translatable("commands.powers_unleashed.superpower.all");
        context.getSource().sendSuccess(() -> Component.translatable(
                "commands.powers_unleashed.superpower." + (add ? "added" : "removed"), what, players.size()), true);
        return players.size();
    }
}
