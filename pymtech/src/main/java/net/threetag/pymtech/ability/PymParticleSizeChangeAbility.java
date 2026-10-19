package net.threetag.pymtech.ability;

import com.google.common.collect.Maps;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectUtils;
import net.minecraft.util.DamageSource;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.threetag.pymtech.client.icon.PymParticleIcon;
import net.threetag.pymtech.fluid.PTFluids;
import net.threetag.pymtech.item.PTItems;
import net.threetag.pymtech.sizechangetype.PTSizeChangeTypes;
import net.threetag.threecore.ability.Ability;
import net.threetag.threecore.ability.AbilityHelper;
import net.threetag.threecore.fluid.ItemHandlerFluidHandler;
import net.threetag.threecore.util.threedata.FloatThreeData;
import net.threetag.threecore.util.threedata.StringThreeData;
import net.threetag.threecore.util.threedata.ThreeData;

import java.util.Map;
import java.util.function.Consumer;

import static net.threetag.threecore.capability.CapabilitySizeChanging.SIZE_CHANGING;

public class PymParticleSizeChangeAbility extends Ability {

    public static final ThreeData<String> REGULATOR_ABILITY = new StringThreeData("regulator_ability").enableSetting("The id of the linked regulator ability to use pym particles from");
    public static final ThreeData<Float> SIZE = new FloatThreeData("size").enableSetting("The size this entity will change to");

    public static final Map<Fluid, Consumer<LivingEntity>> FLUID_ACTIONS = Maps.newHashMap();

    static {
        FLUID_ACTIONS.put(Fluids.WATER, entity -> {
            boolean flag1 = entity instanceof PlayerEntity && ((PlayerEntity) entity).abilities.disableDamage;
            if (!entity.canBreatheUnderwater() && !EffectUtils.canBreatheUnderwater(entity) && !flag1) {
                entity.setAir(0);
                entity.attackEntityFrom(DamageSource.DROWN, 8.0F);
            }
        });

        FLUID_ACTIONS.put(Fluids.LAVA, entity -> {
            entity.setFire(10);
        });
    }

    public PymParticleSizeChangeAbility() {
        super(PTAbilityTypes.PYM_PARTICLE_SIZE_CHANGE.get());
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(ICON, new PymParticleIcon(0.1F));
        this.dataManager.register(REGULATOR_ABILITY, "");
        this.dataManager.register(SIZE, 0.1F);
    }

    @Override
    public void action(LivingEntity entity) {

        if (!entity.world.isRemote) {
            entity.getCapability(SIZE_CHANGING).ifPresent(sizeChanging -> {
                Ability linked = AbilityHelper.getAbilityById(entity, this.dataManager.get(REGULATOR_ABILITY), this.container);

                if (linked instanceof RegulatorAbility) {
                    RegulatorAbility regulatorAbility = (RegulatorAbility) linked;
                    float changeTo = this.getSize(entity);
                    float f = sizeChanging.getScale() - changeTo;
                    if (f != 0F) {
                        ItemHandlerFluidHandler fluidHandler = new ItemHandlerFluidHandler(regulatorAbility.itemHandler);
                        ItemStack regulator = regulatorAbility.itemHandler.getStackInSlot(0);
                        if (!regulator.isEmpty()) {
                            // TODO use tags
                            boolean grow = regulator.getItem() == PTItems.ENLARGE_DISK.get() && f >= 0F;

                            if (grow || (regulator.getItem() == PTItems.SHRINK_DISK.get() && f <= 0F) || regulator.isEmpty() && f <= 0F)
                                return;
                            else {
                                FluidStack toDrain = new FluidStack(f > 0F ? PTFluids.SHRINK_PYM_PARTICLES : PTFluids.ENLARGE_PYM_PARTICLES, 250);
                                FluidStack drained = fluidHandler.drain(toDrain, IFluidHandler.FluidAction.SIMULATE);
                                float size = regulator.isEmpty() ? 0.1F : changeTo;

                                if (!drained.isEmpty() && drained.getAmount() == 250) {
                                    if (sizeChanging.startSizeChange(PTSizeChangeTypes.PYM_PARTICLES, size)) {
                                        fluidHandler.drain(toDrain, IFluidHandler.FluidAction.EXECUTE).getAmount();
                                    }
                                } else {
                                    for (Fluid fluid : FLUID_ACTIONS.keySet()) {
                                        FluidStack drained2 = fluidHandler.drain(new FluidStack(fluid, 500), IFluidHandler.FluidAction.EXECUTE);

                                        if (!drained2.isEmpty()) {
                                            FLUID_ACTIONS.get(fluid).accept(entity);
                                            return;
                                        }
                                    }

                                    if (entity instanceof PlayerEntity)
                                        ((PlayerEntity) entity).sendStatusMessage(new TranslationTextComponent(f > 0F ? "ability.pymtech.pym_particle_size_change.not_enough_shrink_particles" : "ability.pymtech.pym_particle_size_change.not_enough_enlarge_particles"), true);
                                }
                            }
                        }
                    }
                }
            });
        }
    }

    public float getSize(LivingEntity entity) {
        if (entity == null || !entity.getCapability(SIZE_CHANGING, null).isPresent())
            return 1F;

        float playerSize = entity.getCapability(SIZE_CHANGING, null).orElse(null).getScale();
        Ability linked = AbilityHelper.getAbilityById(entity, this.get(REGULATOR_ABILITY), this.getContainer());
        float abilitySize = this.get(SIZE);
        float customSize = linked instanceof IAdvancedRegulatorAbility ? ((IAdvancedRegulatorAbility) linked).getRegulatedSize() : 0;

        if ((playerSize < 1F && abilitySize > 1F) || (playerSize > 1F && abilitySize < 1F))
            return 1F;

        if (customSize <= 0F)
            return abilitySize;

        return customSize < 1F && abilitySize < 1F ? customSize : (customSize > 1F && abilitySize > 1F ? customSize : abilitySize);
    }


}
