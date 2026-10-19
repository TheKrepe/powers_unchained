package net.threetag.palladium.command;

import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.PermissionCheck;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.entity.sizechange.PalladiumSizeChangeTypes;
import net.threetag.palladium.entity.sizechange.EntitySizeChangeHandler;
import net.threetag.palladium.entity.sizechange.SizeChangeType;
import net.threetag.palladium.registry.PalladiumRegistryKeys;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public class SizeChangeCommand {

    public static final PermissionCheck PERMISSION_CHECK = new PermissionCheck.Require(Permissions.COMMANDS_GAMEMASTER);

    public static final String ERROR_NO_LIVING_ENTITY = "commands.palladium.size.error.no_living_entity";
    public static final String SET_SUCCESS_SINGLE = "commands.palladium.size.set.success.single";
    public static final String SET_SUCCESS_MULTIPLE = "commands.palladium.size.set.success.multiple";
    public static final String CHANGE_SUCCESS_SINGLE = "commands.palladium.size.change.success.single";
    public static final String CHANGE_SUCCESS_MULTIPLE = "commands.palladium.size.change.success.multiple";
    public static final String RESET_SUCCESS_SINGLE = "commands.palladium.size.reset.success.single";
    public static final String RESET_SUCCESS_MULTIPLE = "commands.palladium.size.reset.success.multiple";
    public static final String RESET_TYPE_SUCCESS_SINGLE = "commands.palladium.size.reset.type.success.single";
    public static final String RESET_TYPE_SUCCESS_MULTIPLE = "commands.palladium.size.reset.type.success.multiple";
    public static final String QUERY_SUCCESS = "commands.palladium.size.query.success";
    public static final String QUERY_TYPE_SUCCESS = "commands.palladium.size.query.type.success";

    public static void register(LiteralArgumentBuilder<CommandSourceStack> builder, CommandBuildContext context) {
        builder.then(Commands.literal("size")
                .requires(Commands.hasPermission(PERMISSION_CHECK))
                .then(Commands.literal("set")
                        .then(Commands.argument("entities", EntityArgument.entities())
                                .then(Commands.argument("size", FloatArgumentType.floatArg(0.0625F, 16F))
                                        .executes(ctx -> changeSize(
                                                ctx.getSource(),
                                                EntityArgument.getEntities(ctx, "entities"),
                                                FloatArgumentType.getFloat(ctx, "size"),
                                                getDefaultType(ctx.getSource()),
                                                SET_SUCCESS_SINGLE,
                                                SET_SUCCESS_MULTIPLE
                                        ))
                                        .then(Commands.argument("type", ResourceArgument.resource(context, PalladiumRegistryKeys.SIZE_CHANGE_TYPE))
                                                .executes(ctx -> changeSize(
                                                        ctx.getSource(),
                                                        EntityArgument.getEntities(ctx, "entities"),
                                                        FloatArgumentType.getFloat(ctx, "size"),
                                                        ResourceArgument.getResource(ctx, "type", PalladiumRegistryKeys.SIZE_CHANGE_TYPE),
                                                        SET_SUCCESS_SINGLE,
                                                        SET_SUCCESS_MULTIPLE
                                                ))
                                        )
                                )
                        )
                )
                .then(Commands.literal("change")
                        .then(Commands.argument("entities", EntityArgument.entities())
                                .then(Commands.argument("size", FloatArgumentType.floatArg(0.0625F, 16F))
                                        .executes(ctx -> changeSize(
                                                ctx.getSource(),
                                                EntityArgument.getEntities(ctx, "entities"),
                                                FloatArgumentType.getFloat(ctx, "size"),
                                                getDefaultType(ctx.getSource()),
                                                CHANGE_SUCCESS_SINGLE,
                                                CHANGE_SUCCESS_MULTIPLE
                                        ))
                                        .then(Commands.argument("type", ResourceArgument.resource(context, PalladiumRegistryKeys.SIZE_CHANGE_TYPE))
                                                .executes(ctx -> changeSize(
                                                        ctx.getSource(),
                                                        EntityArgument.getEntities(ctx, "entities"),
                                                        FloatArgumentType.getFloat(ctx, "size"),
                                                        ResourceArgument.getResource(ctx, "type", PalladiumRegistryKeys.SIZE_CHANGE_TYPE),
                                                        CHANGE_SUCCESS_SINGLE,
                                                        CHANGE_SUCCESS_MULTIPLE
                                                ))
                                        )
                                )
                        )
                )
                .then(Commands.literal("reset")
                        .then(Commands.argument("entities", EntityArgument.entities())
                                .executes(ctx -> resetSize(
                                        ctx.getSource(),
                                        EntityArgument.getEntities(ctx, "entities"),
                                        null
                                ))
                                .then(Commands.argument("type", ResourceArgument.resource(context, PalladiumRegistryKeys.SIZE_CHANGE_TYPE))
                                        .executes(ctx -> resetSize(
                                                ctx.getSource(),
                                                EntityArgument.getEntities(ctx, "entities"),
                                                ResourceArgument.getResource(ctx, "type", PalladiumRegistryKeys.SIZE_CHANGE_TYPE)
                                        ))
                                )
                        )
                )
                .then(Commands.literal("query")
                        .then(Commands.argument("entity", EntityArgument.entity())
                                .executes(ctx -> querySize(
                                        ctx.getSource(),
                                        EntityArgument.getEntity(ctx, "entity"),
                                        null
                                ))
                                .then(Commands.argument("type", ResourceArgument.resource(context, PalladiumRegistryKeys.SIZE_CHANGE_TYPE))
                                        .executes(ctx -> querySize(
                                                ctx.getSource(),
                                                EntityArgument.getEntity(ctx, "entity"),
                                                ResourceArgument.getResource(ctx, "type", PalladiumRegistryKeys.SIZE_CHANGE_TYPE)
                                        ))
                                )
                        )
                )
        );
    }

    private static Holder.Reference<SizeChangeType> getDefaultType(CommandSourceStack source) {
        return source.registryAccess()
                .lookupOrThrow(PalladiumRegistryKeys.SIZE_CHANGE_TYPE)
                .get(PalladiumSizeChangeTypes.DEFAULT)
                .orElseThrow();
    }

    private static int changeSize(CommandSourceStack source, Collection<? extends Entity> entities, float size,
                                  Holder.Reference<SizeChangeType> type, String keySingle, String keyMultiple) throws CommandSyntaxException {
        int count = 0;

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity) {
                var sizeData = PalladiumEntityData.get(entity, PalladiumEntityDataTypes.SIZE_CHANGE.get());
                if (sizeData != null) {
                    sizeData.applyModifier(type, size);
                    count++;
                }
            }
        }

        if (count == 0) {
            source.sendFailure(Component.translatable(ERROR_NO_LIVING_ENTITY));
            return 0;
        }

        String typeId = type.key().identifier().toString();
        if (entities.size() == 1) {
            source.sendSuccess(() -> Component.translatable(keySingle,
                    entities.iterator().next().getDisplayName(), size, typeId), true);
        } else {
            int finalCount = count;
            source.sendSuccess(() -> Component.translatable(keyMultiple,
                    finalCount, size, typeId), true);
        }

        return count;
    }

    private static int resetSize(CommandSourceStack source, Collection<? extends Entity> entities,
                                 @Nullable Holder.Reference<SizeChangeType> type) throws CommandSyntaxException {
        int count = 0;

        for (Entity entity : entities) {
            if (entity instanceof LivingEntity) {
                var sizeData = PalladiumEntityData.get(entity, PalladiumEntityDataTypes.SIZE_CHANGE.get());
                if (sizeData != null) {
                    if (type != null) {
                        sizeData.removeModifier(type);
                    } else {
                        sizeData.setSize(1F);
                    }
                    count++;
                }
            }
        }

        if (count == 0) {
            source.sendFailure(Component.translatable(ERROR_NO_LIVING_ENTITY));
            return 0;
        }

        if (type != null) {
            String typeId = type.key().identifier().toString();
            if (entities.size() == 1) {
                source.sendSuccess(() -> Component.translatable(RESET_TYPE_SUCCESS_SINGLE,
                        entities.iterator().next().getDisplayName(), typeId), true);
            } else {
                int finalCount = count;
                source.sendSuccess(() -> Component.translatable(RESET_TYPE_SUCCESS_MULTIPLE,
                        typeId, finalCount), true);
            }
        } else {
            if (entities.size() == 1) {
                source.sendSuccess(() -> Component.translatable(RESET_SUCCESS_SINGLE,
                        entities.iterator().next().getDisplayName()), true);
            } else {
                int finalCount = count;
                source.sendSuccess(() -> Component.translatable(RESET_SUCCESS_MULTIPLE, finalCount), true);
            }
        }

        return count;
    }

    private static int querySize(CommandSourceStack source, Entity entity,
                                 @Nullable Holder.Reference<SizeChangeType> type) throws CommandSyntaxException {
        if (!(entity instanceof LivingEntity)) {
            source.sendFailure(Component.translatable(ERROR_NO_LIVING_ENTITY));
            return 0;
        }

        var sizeData = PalladiumEntityData.get(entity, PalladiumEntityDataTypes.SIZE_CHANGE.get());
        if (sizeData != null) {
            if (type != null) {
                String typeId = type.key().identifier().toString();
                EntitySizeChangeHandler.ModifierEntry entry = sizeData.getModifiers().get(type.key());
                float current = entry != null ? entry.getCurrentValue() : 1.0F;
                source.sendSuccess(() -> Component.translatable(QUERY_TYPE_SUCCESS,
                        entity.getDisplayName(), typeId, current), false);
            } else {
                float currentSize = sizeData.getCurrentSize();
                source.sendSuccess(() -> Component.translatable(QUERY_SUCCESS,
                        entity.getDisplayName(), currentSize), false);
            }
            return 1;
        }

        return 0;
    }
}
