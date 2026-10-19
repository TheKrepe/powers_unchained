package lucraft.mods.lucraftcore.util.attributes;

import lucraft.mods.lucraftcore.LucraftCore;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.util.EntityDamageSource;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent;

import java.util.UUID;

@EventBusSubscriber(modid = LucraftCore.MODID)
public class LCAttributes {

    public static final IAttribute STEP_HEIGHT = (new RangedAttribute((IAttribute) null, "lucraftcore.stepHeight", 0.5D, 0D, 20D)).setDescription("Step Height").setShouldWatch(true);
    public static final IAttribute PUNCH_DAMAGE = (new RangedAttribute((IAttribute) null, "lucraftcore.extraPunchDamage", 0D, 0D, Double.MAX_VALUE)).setDescription("Extra Punch Damage").setShouldWatch(true);
    public static final IAttribute FALL_RESISTANCE = (new RangedAttribute((IAttribute) null, "lucraftcore.fallResistance", 0D, 0D, Double.MAX_VALUE)).setDescription("Fall Resistance").setShouldWatch(true);
    public static final IAttribute JUMP_HEIGHT = (new RangedAttribute((IAttribute) null, "lucraftcore.jumpHeight", 0D, 0D, Double.MAX_VALUE)).setDescription("Jump Height").setShouldWatch(true);
    public static final IAttribute SPRINT_SPEED = (new RangedAttribute((IAttribute) null, "lucraftcore.sprintSpeed", 0D, 0D, Double.MAX_VALUE)).setDescription("Sprint Speed").setShouldWatch(true);

    @SubscribeEvent
    public static void onConstruct(EntityConstructing e) {
        if (e.getEntity() instanceof EntityLivingBase) {
            EntityLivingBase entity = (EntityLivingBase) e.getEntity();

            if (((EntityLivingBase) e.getEntity()).getAttributeMap().getAttributeInstanceByName(LCAttributes.STEP_HEIGHT.getName()) == null)
                entity.getAttributeMap().registerAttribute(LCAttributes.STEP_HEIGHT).setBaseValue(1D);

            if (((EntityLivingBase) e.getEntity()).getAttributeMap().getAttributeInstanceByName(LCAttributes.PUNCH_DAMAGE.getName()) == null)
                entity.getAttributeMap().registerAttribute(LCAttributes.PUNCH_DAMAGE);

            if (((EntityLivingBase) e.getEntity()).getAttributeMap().getAttributeInstanceByName(LCAttributes.FALL_RESISTANCE.getName()) == null)
                entity.getAttributeMap().registerAttribute(LCAttributes.FALL_RESISTANCE);

            if (((EntityLivingBase) e.getEntity()).getAttributeMap().getAttributeInstanceByName(LCAttributes.JUMP_HEIGHT.getName()) == null)
                entity.getAttributeMap().registerAttribute(LCAttributes.JUMP_HEIGHT);

            if (((EntityLivingBase) e.getEntity()).getAttributeMap().getAttributeInstanceByName(LCAttributes.SPRINT_SPEED.getName()) == null)
                entity.getAttributeMap().registerAttribute(LCAttributes.SPRINT_SPEED);
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent e) {
        for (AttributeModifier modifier : e.getEntityLiving().getAttributeMap().getAttributeInstance(FALL_RESISTANCE).getModifiers()) {
            if (modifier.getOperation() == 0) {
                e.setDistance((float) (e.getDistance() - modifier.getAmount()));
            }
        }
        for (AttributeModifier modifier : e.getEntityLiving().getAttributeMap().getAttributeInstance(FALL_RESISTANCE).getModifiers()) {
            if (modifier.getOperation() == 1) {
                e.setDistance((float) (e.getDistance() * modifier.getAmount()));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onDamage(LivingHurtEvent e) {
        if (e.getSource() instanceof EntityDamageSource && ((EntityDamageSource) e.getSource()).getImmediateSource() != null && ((EntityDamageSource) e.getSource()).getImmediateSource() instanceof EntityLivingBase) {
            EntityLivingBase player = (EntityLivingBase) ((EntityDamageSource) e.getSource()).getImmediateSource();
            float f = e.getAmount();

            if (!player.getHeldItemMainhand().isEmpty())
                return;

            for (AttributeModifier modifier : player.getAttributeMap().getAttributeInstance(PUNCH_DAMAGE).getModifiers()) {
                if (modifier.getOperation() == 0) {
                    f += modifier.getAmount();
                }
            }
            for (AttributeModifier modifier : player.getAttributeMap().getAttributeInstance(PUNCH_DAMAGE).getModifiers()) {
                if (modifier.getOperation() == 1) {
                    f *= modifier.getAmount();
                }
            }
            e.setAmount(f);

        }
    }

    @SubscribeEvent
    public static void onFall(LivingJumpEvent e) {
        if (!e.getEntityLiving().isSneaking()) {
            float f = 0;
            for (AttributeModifier modifier : e.getEntityLiving().getAttributeMap().getAttributeInstance(JUMP_HEIGHT).getModifiers()) {
                if (modifier.getOperation() == 0) {
                    f += modifier.getAmount();
                }
            }
            for (AttributeModifier modifier : e.getEntityLiving().getAttributeMap().getAttributeInstance(JUMP_HEIGHT).getModifiers()) {
                if (modifier.getOperation() == 1) {
                    if (f == 0)
                        f = 1;
                    f *= modifier.getAmount();
                }
            }

            e.getEntityLiving().motionY += 0.1 * f;
        }
    }

    public static final UUID sprintUUID = UUID.fromString("11faf62f-c271-4601-809e-83d982687b69");
    public static float stepHeight;

    @SubscribeEvent
    public static void onTick(PlayerTickEvent e) {
        if (e.phase == Phase.END) {
            if (e.player.ticksExisted > 20 && e.player.world.isRemote) {
                e.player.stepHeight = stepHeight;
            }
            return;
        }

        if (e.player.ticksExisted > 20 && e.player.world.isRemote) {
            stepHeight = e.player.stepHeight;
            for (AttributeModifier mod : e.player.getAttributeMap().getAttributeInstance(LCAttributes.STEP_HEIGHT).getModifiersByOperation(0))
                e.player.stepHeight += mod.getAmount();
            for (AttributeModifier mod : e.player.getAttributeMap().getAttributeInstance(LCAttributes.STEP_HEIGHT).getModifiersByOperation(1))
                e.player.stepHeight *= mod.getAmount();
            for (AttributeModifier mod : e.player.getAttributeMap().getAttributeInstance(LCAttributes.STEP_HEIGHT).getModifiersByOperation(2))
                e.player.stepHeight *= mod.getAmount();
        }

        e.player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).removeModifier(sprintUUID);
        if (e.player.isSprinting() && e.player.getAttributeMap().getAttributeInstance(SPRINT_SPEED).getModifiers().size() > 0) {
            double amount = 1;
            for (AttributeModifier mod : e.player.getAttributeMap().getAttributeInstance(SPRINT_SPEED).getModifiers())
                amount += mod.getAmount();
            e.player.getAttributeMap().getAttributeInstance(SharedMonsterAttributes.MOVEMENT_SPEED).applyModifier(new AttributeModifier(sprintUUID, "Sprint modifier", amount, 1));
        }
    }

}
