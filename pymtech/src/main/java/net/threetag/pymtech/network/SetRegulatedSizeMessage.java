package net.threetag.pymtech.network;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;
import net.threetag.pymtech.ability.IAdvancedRegulatorAbility;
import net.threetag.threecore.ability.Ability;
import net.threetag.threecore.ability.AbilityHelper;

import java.util.function.Supplier;

public class SetRegulatedSizeMessage {

    public String abilityId;
    public float size;

    public SetRegulatedSizeMessage(String abilityId, float size) {
        this.abilityId = abilityId;
        this.size = size;
    }

    public SetRegulatedSizeMessage(PacketBuffer buf) {
        this.abilityId = buf.readString(64);
        this.size = buf.readFloat();
    }

    public void toBytes(PacketBuffer buf) {
        buf.writeString(this.abilityId);
        buf.writeFloat(this.size);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        (ctx.get()).enqueueWork(() -> {
            ServerPlayerEntity player = ctx.get().getSender();
            if (player != null) {
                Ability ability = AbilityHelper.getAbilityById(player, this.abilityId, null);

                if (ability instanceof IAdvancedRegulatorAbility) {
                    ((IAdvancedRegulatorAbility) ability).setRegulatedSize(this.size);
                }
            }
        });
        (ctx.get()).setPacketHandled(true);
    }
}
