package com.thekrepe.powersunchained;

import com.thekrepe.powersunchained.suit.SuitProgress;
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

    private PUAttachments() {
    }

    public static void register(IEventBus modEventBus) {
        TYPES.register(modEventBus);
    }
}
