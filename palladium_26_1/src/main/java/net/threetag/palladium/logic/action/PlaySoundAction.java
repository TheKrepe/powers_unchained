package net.threetag.palladium.logic.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.threetag.palladium.documentation.CodecDocumentationBuilder;
import net.threetag.palladium.logic.context.DataContext;
import net.threetag.palladium.util.PlayerUtil;

public class PlaySoundAction extends Action {

    public static final MapCodec<PlaySoundAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Identifier.CODEC.fieldOf("sound").forGetter(a -> a.sound),
            Codec.FLOAT.optionalFieldOf("volume", 1F).forGetter(a -> a.volume),
            Codec.FLOAT.optionalFieldOf("pitch", 1F).forGetter(a -> a.pitch)
    ).apply(instance, PlaySoundAction::new));

    public final Identifier sound;
    public final float volume;
    public final float pitch;

    public PlaySoundAction(Identifier sound, float volume, float pitch) {
        this.sound = sound;
        this.volume = volume;
        this.pitch = pitch;
    }

    @Override
    public boolean run(DataContext context) {
        var entity = context.getEntity();

        if (entity == null || entity.level().isClientSide()) {
            return false;
        }

        PlayerUtil.playSoundToAll(entity.level(), entity.getX(), entity.getEyeY(), entity.getZ(), 50, this.sound, entity.getSoundSource(), this.volume, this.pitch);
        return true;
    }

    @Override
    public ActionSerializer<?> getSerializer() {
        return ActionSerializers.PLAY_SOUND.get();
    }

    public static class Serializer extends ActionSerializer<PlaySoundAction> {

        @Override
        public MapCodec<PlaySoundAction> codec() {
            return CODEC;
        }

        @Override
        public void addDocumentation(CodecDocumentationBuilder<Action, PlaySoundAction> builder, HolderLookup.Provider provider) {
            builder.setName("Play Sound")
                    .setDescription("Plays a sound at the entity's position.")
                    .add("sound", TYPE_IDENTIFIER, "The ID of the sound event to play.")
                    .addOptional("volume", TYPE_FLOAT, "The volume of the sound. Defaults to 1.")
                    .addOptional("pitch", TYPE_FLOAT, "The pitch of the sound. Defaults to 1.")
                    .addExampleObject(new PlaySoundAction(SoundEvents.ANVIL_LAND.location(), 1F, 1F));
        }
    }
}
