package net.threetag.palladium.dialog;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.dialog.action.Action;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.addonpack.log.AddonPackLog;
import net.threetag.palladium.logic.trigger.PalladiumCriteriaTriggers;
import net.threetag.palladium.network.OpenCustomizationScreenPacket;
import net.threetag.palladium.network.OpenScreenPacket;
import net.threetag.palladium.registry.PalladiumRegistryKeys;

public class PalladiumDialogActions {

    public static final DeferredRegister<MapCodec<? extends Action>> ACTIONS = DeferredRegister.create(Registries.DIALOG_ACTION_TYPE, Palladium.MOD_ID);

    public static final DeferredHolder<MapCodec<? extends Action>, MapCodec<OpenScreenAction>> OPEN_SCREEN = ACTIONS.register("open_screen", () -> OpenScreenAction.CODEC);
    public static final DeferredHolder<MapCodec<? extends Action>, MapCodec<OpenCustomizationScreenAction>> OPEN_CUSTOMIZATION_SCREEN = ACTIONS.register("open_customization_screen", () -> OpenCustomizationScreenAction.CODEC);
    public static final DeferredHolder<MapCodec<? extends Action>, MapCodec<CustomEventAction>> CUSTOM_EVENT = ACTIONS.register("custom_event", () -> CustomEventAction.CODEC);

    public static boolean handleCustom(ServerPlayer player, Identifier id, Tag tag) {
        if (id.equals(OPEN_SCREEN.getId()) && tag instanceof StringTag stringTag) {
            PacketDistributor.sendToPlayer(player, new OpenScreenPacket(Identifier.parse(stringTag.asString().orElse(""))));
            return true;
        } else if (id.equals(OPEN_CUSTOMIZATION_SCREEN.getId()) && tag instanceof StringTag stringTag) {
            PacketDistributor.sendToPlayer(player, new OpenCustomizationScreenPacket(stringTag.asString().map(s -> s.isEmpty() ? null : ResourceKey.create(PalladiumRegistryKeys.CUSTOMIZATION_CATEGORY, Identifier.parse(s)))));
            return true;
        } else if (id.equals(CUSTOM_EVENT.getId())) {
            handleCustomEvent(player, tag);
            return true;
        }

        return false;
    }

    private static void handleCustomEvent(ServerPlayer player, Tag tag) {
        if (!(tag instanceof CompoundTag payload)) {
            AddonPackLog.warning("Received custom event from " + player.getGameProfile().name() + " with malformed payload: " + tag);
            return;
        }

        Identifier eventId = payload.getString(CustomEventAction.TAG_ID).map(Identifier::tryParse).orElse(null);

        if (eventId == null) {
            AddonPackLog.warning("Received custom event from " + player.getGameProfile().name() + " with missing or invalid ID: " + payload);
            return;
        }

        CompoundTag data = payload.getCompound(CustomEventAction.TAG_DATA).orElseGet(CompoundTag::new);
        PalladiumCriteriaTriggers.CUSTOM_EVENT.get().trigger(player, eventId, data);
    }

}
