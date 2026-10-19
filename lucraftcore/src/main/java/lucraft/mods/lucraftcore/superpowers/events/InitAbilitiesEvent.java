package lucraft.mods.lucraftcore.superpowers.events;

import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingEvent;

import java.util.Map;

public class InitAbilitiesEvent extends LivingEvent {

    protected Map<String, Ability> abilities;
    protected Ability.EnumAbilityContext context;

    public InitAbilitiesEvent(EntityLivingBase entity, Map<String, Ability> abilities, Ability.EnumAbilityContext context) {
        super(entity);
        this.abilities = abilities;
        this.context = context;
    }

    public Map<String, Ability> getAbilities() {
        return abilities;
    }

    public Ability.EnumAbilityContext getContext() {
        return context;
    }

    public static class Pre extends InitAbilitiesEvent {

        public Pre(EntityLivingBase entity, Map<String, Ability> abilities, Ability.EnumAbilityContext context) {
            super(entity, abilities, context);
        }
    }

    public static class Post extends InitAbilitiesEvent {

        public Post(EntityLivingBase entity, Map<String, Ability> abilities, Ability.EnumAbilityContext context) {
            super(entity, abilities, context);
        }
    }

}
