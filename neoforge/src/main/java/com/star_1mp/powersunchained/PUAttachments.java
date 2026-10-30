package com.star_1mp.powersunchained;

import com.star_1mp.powersunchained.suit.SuitProgress;
import java.util.function.Supplier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** NeoForge data attachments. */
public final class PUAttachments {

    private static final DeferredRegister<AttachmentType<?>> TYPES = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, Constants.MOD_ID);

    /** Suit Creator progress of a player: saved, kept on death, synced to that player only. */
    public static final Supplier<AttachmentType<SuitProgress>> SUIT_PROGRESS = TYPES.register("suit_progress",
            () -> AttachmentType.builder(() -> SuitProgress.EMPTY)
                    .serialize(SuitProgress.CODEC)
                    .copyOnDeath()
                    .sync((holder, to) -> holder == to, SuitProgress.STREAM_CODEC)
                    .build());

    /** Tech Table progress of a player (unlocked modules / gadget looks): saved, kept on death, synced to that player only. */
    public static final Supplier<AttachmentType<com.star_1mp.powersunchained.tech.TechProgress>> TECH_PROGRESS = TYPES.register("tech_progress",
            () -> AttachmentType.builder(() -> com.star_1mp.powersunchained.tech.TechProgress.EMPTY)
                    .serialize(com.star_1mp.powersunchained.tech.TechProgress.CODEC)
                    .copyOnDeath()
                    .sync((holder, to) -> holder == to, com.star_1mp.powersunchained.tech.TechProgress.STREAM_CODEC)
                    .build());

    /** Superpowers of a player: saved, kept on death, synced to that player only. */
    public static final Supplier<AttachmentType<com.star_1mp.powersunchained.power.PowerData>> POWERS = TYPES.register("superpowers",
            () -> AttachmentType.builder(() -> com.star_1mp.powersunchained.power.PowerData.EMPTY)
                    .serialize(com.star_1mp.powersunchained.power.PowerData.CODEC)
                    .copyOnDeath()
                    .sync((holder, to) -> holder == to, com.star_1mp.powersunchained.power.PowerData.STREAM_CODEC)
                    .build());

    private PUAttachments() {
    }

    public static void register(IEventBus modEventBus) {
        TYPES.register(modEventBus);
    }
}
