package net.threetag.palladium.logic.action;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.ParsedCommands;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

public class RunCommandAction extends Action implements CommandSource {

    public static final MapCodec<RunCommandAction> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ParsedCommands.CODEC.optionalFieldOf("command", ParsedCommands.EMPTY).forGetter(a -> a.command)
            ).apply(instance, RunCommandAction::new));

    public final ParsedCommands command;

    public RunCommandAction(ParsedCommands command) {
        this.command = command;
    }

    @Override
    public boolean run(DataContext context) {
        var level = context.getLevel();

        if (level instanceof ServerLevel serverLevel) {
            var source = this.createCommandSourceStack(serverLevel, context.getEntity());
            Objects.requireNonNull(serverLevel.getServer()).getFunctions()
                    .execute(this.command.getCommandFunction(serverLevel.getServer()), source.withSuppressedOutput().withMaximumPermission(PermissionSet.ALL_PERMISSIONS));
            return true;
        }

        return false;
    }

    public CommandSourceStack createCommandSourceStack(ServerLevel serverLevel, @Nullable Entity entity) {
        return new CommandSourceStack(this,
                entity != null ? entity.position() : Vec3.ZERO,
                entity != null ? entity.getRotationVector() : Vec2.ZERO,
                serverLevel,
                PermissionSet.ALL_PERMISSIONS,
                entity != null ? entity.getName().getString() : "Run Command Action",
                entity != null ? entity.getDisplayName() : Component.literal("Run Command Action"),
                serverLevel.getServer(),
                entity
        ).withSuppressedOutput();
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return ActionSerializers.RUN_COMMAND.get();
    }

    @Override
    public void sendSystemMessage(Component component) {

    }

    @Override
    public boolean acceptsSuccess() {
        return false;
    }

    @Override
    public boolean acceptsFailure() {
        return false;
    }

    @Override
    public boolean shouldInformAdmins() {
        return false;
    }

    public static class Serializer extends ActionSerializer<RunCommandAction> {

        @Override
        public MapCodec<RunCommandAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, RunCommandAction> builder, HolderLookup.Provider provider) {
            builder.setName("Run Command").setDescription("Runs a Minecraft command")
                    .add("command", TYPE_STRING_ARRAY, "The command(s) that are being run.")
                    .addExampleObject(new RunCommandAction(new ParsedCommands(Collections.singletonList("say Hello World"))))
                    .addExampleObject(new RunCommandAction(new ParsedCommands(Arrays.asList("say Command 1", "say Command 2"))));
        }
    }
}