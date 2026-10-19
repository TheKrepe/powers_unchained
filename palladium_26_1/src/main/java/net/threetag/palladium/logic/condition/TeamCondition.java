package net.threetag.palladium.logic.condition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.documentation.SettingType;
import net.threetag.palladium.logic.context.DataContext;

import java.util.Optional;

public record TeamCondition(Optional<String> name, Optional<Boolean> allowFriendlyFire, Optional<Boolean> seeFriendlyInvisibles, Optional<Team.Visibility> nameTagVisibility, Optional<Team.Visibility> deathMessageVisibility, Optional<Team.CollisionRule> collisionRule, Optional<ChatFormatting> color) implements Condition {

    public static final MapCodec<TeamCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("name").forGetter(TeamCondition::name),
            Codec.BOOL.optionalFieldOf("allow_friendly_fire").forGetter(TeamCondition::allowFriendlyFire),
            Codec.BOOL.optionalFieldOf("see_friendly_invisibles").forGetter(TeamCondition::seeFriendlyInvisibles),
            Team.Visibility.CODEC.optionalFieldOf("name_tag_visibility").forGetter(TeamCondition::nameTagVisibility),
            Team.Visibility.CODEC.optionalFieldOf("death_message_visibility").forGetter(TeamCondition::deathMessageVisibility),
            Team.CollisionRule.CODEC.optionalFieldOf("collision_rule").forGetter(TeamCondition::collisionRule),
            ChatFormatting.CODEC.optionalFieldOf("color").forGetter(TeamCondition::color)
    ).apply(instance, TeamCondition::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TeamCondition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.STRING_UTF8), TeamCondition::name,
            ByteBufCodecs.optional(ByteBufCodecs.BOOL), TeamCondition::allowFriendlyFire,
            ByteBufCodecs.optional(ByteBufCodecs.BOOL), TeamCondition::seeFriendlyInvisibles,
            ByteBufCodecs.optional(Team.Visibility.STREAM_CODEC), TeamCondition::nameTagVisibility,
            ByteBufCodecs.optional(Team.Visibility.STREAM_CODEC), TeamCondition::deathMessageVisibility,
            ByteBufCodecs.optional(Team.CollisionRule.STREAM_CODEC), TeamCondition::collisionRule,
            ByteBufCodecs.optional(NeoForgeStreamCodecs.enumCodec(ChatFormatting.class)), TeamCondition::color,
            TeamCondition::new
    );

    @Override
    public boolean test(DataContext context) {
        var entity = context.getEntity();
        if (entity == null || !(entity.getTeam() instanceof PlayerTeam playerTeam)) return false;

        if (name.isPresent() && !playerTeam.getName().equals(name.get())) return false;
        if (allowFriendlyFire.isPresent() && playerTeam.isAllowFriendlyFire() != allowFriendlyFire.get()) return false;
        if (seeFriendlyInvisibles.isPresent() && playerTeam.canSeeFriendlyInvisibles() != seeFriendlyInvisibles.get()) return false;
        if (nameTagVisibility.isPresent() && playerTeam.getNameTagVisibility() != nameTagVisibility.get()) return false;
        if (deathMessageVisibility.isPresent() && playerTeam.getDeathMessageVisibility() != deathMessageVisibility.get()) return false;
        if (collisionRule.isPresent() && playerTeam.getCollisionRule() != collisionRule.get()) return false;
        if (color.isPresent() && playerTeam.getColor() != color.get()) return false;

        return true;
    }

    @Override
    public ConditionSerializer<TeamCondition> getSerializer() {
        return ConditionSerializers.TEAM.get();
    }

    public static class Serializer extends ConditionSerializer<TeamCondition> {

        @Override
        public MapCodec<TeamCondition> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Condition, TeamCondition> builder, HolderLookup.Provider provider) {
            builder.setName("Team")
                    .setDescription("Checks the team properties of the entity. Leave all other fields out to check just if the entity belongs to a team.")
                    .addOptional("name", TYPE_STRING, "Checks the name of the team.")
                    .addOptional("allow_friendly_fire", TYPE_BOOLEAN, "Checks if the team allows friendly fire.")
                    .addOptional("see_friendly_invisibles", TYPE_BOOLEAN, "Checks if teammates can see invisible teammates.")
                    .addOptional("name_tag_visibility", SettingType.enumList(Team.Visibility.values()), "Checks the nametag visibility rule of the team.")
                    .addOptional("death_message_visibility", SettingType.enumList(Team.Visibility.values()), "Checks the death message visibility rule of the team.")
                    .addOptional("collision_rule", SettingType.enumList(Team.CollisionRule.values()), "Checks the collision rule of the team.")
                    .addOptional("color", SettingType.enumList(ChatFormatting.values()), "Checks the color of the team.")
                    .addExampleObject(new TeamCondition(Optional.of("Avengers"), Optional.of(false), Optional.of(true), Optional.of(Team.Visibility.ALWAYS), Optional.of(Team.Visibility.HIDE_FOR_OTHER_TEAMS), Optional.of(Team.CollisionRule.PUSH_OTHER_TEAMS), Optional.of(ChatFormatting.AQUA)));
        }
    }

}