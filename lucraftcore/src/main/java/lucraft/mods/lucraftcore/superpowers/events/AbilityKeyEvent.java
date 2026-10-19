package lucraft.mods.lucraftcore.superpowers.events;

import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.relauncher.Side;

public class AbilityKeyEvent extends Event {

    public Side side;
    public Ability ability;
    public boolean pressed;

    private AbilityKeyEvent(Ability ability, Side side, boolean pressed) {
        this.ability = ability;
        this.side = side;
        this.pressed = pressed;
    }

    @Cancelable
    public static class Client extends AbilityKeyEvent {

        public Client(Ability ability, boolean pressed) {
            super(ability, Side.CLIENT, pressed);
        }

    }

    @Cancelable
    public static class Server extends AbilityKeyEvent {

        public EntityLivingBase entity;

        public Server(Ability ability, EntityLivingBase entity, boolean pressed) {
            super(ability, Side.SERVER, pressed);
            this.entity = entity;
        }

    }

}
