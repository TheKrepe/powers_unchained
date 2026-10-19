package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.network.HEPacketDispatcher;
import lucraft.mods.heroesexpansion.network.MessageKineticEnergyBlast;
import lucraft.mods.lucraftcore.materials.Material;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityAction;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityData;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataColor;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataInteger;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AbilityKineticEnergyAbsorption extends AbilityAction {

    public static final AbilityData<Integer> ENERGY = new AbilityDataInteger("energy").setSyncType(EnumSync.SELF);
    public static final AbilityData<Integer> MAX_ENERGY = new AbilityDataInteger("max_energy").disableSaving().setSyncType(EnumSync.EVERYONE).enableSetting("max_energy", "The maximum energy able to be absorbed");
    public static final AbilityData<Color> COLOR = new AbilityDataColor("color").disableSaving().setSyncType(EnumSync.EVERYONE).enableSetting("color", "The color of the energy blast");

    public AbilityKineticEnergyAbsorption(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(ENERGY, 0);
        this.dataManager.register(MAX_ENERGY, 100);
        this.dataManager.register(COLOR, new Color(1f, 1f, 1f));
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        ItemStack stack = Material.VIBRANIUM.getItemStack(Material.MaterialComponent.INGOT);
        float zLevel = Minecraft.getMinecraft().getRenderItem().zLevel;
        Minecraft.getMinecraft().getRenderItem().zLevel = -100.5F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(stack, 0, 0);
        GlStateManager.popMatrix();
        Minecraft.getMinecraft().getRenderItem().zLevel = zLevel;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public boolean renderCooldown() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public float getCooldownPercentage() {
        return this.getPercentage();
    }

    @Override
    public boolean action() {
        if (this.getDataManager().get(ENERGY) <= 0)
            return false;

        entity.setArrowCountInEntity(0);
        float size = this.getDataManager().get(ENERGY) / 20F;
        Color color = getDataManager().get(COLOR);
        HEPacketDispatcher.sendToAll(new MessageKineticEnergyBlast(entity.posX, entity.posY, entity.posZ, (int) size, color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F));
        Vec3d pos = new Vec3d(entity.posX, entity.posY + entity.height / 2F, entity.posZ);

        for (EntityLivingBase entity : this.entity.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos.x - size, pos.y - size, pos.z - size, pos.x + size, pos.y + size, pos.z + size))) {
            if (entity != this.entity && entity.getDistance(this.entity) <= size) {
                Vec3d push = (entity.getPositionVector().subtract(pos)).scale(1F - (entity.getDistance(this.entity) / size));
                entity.move(MoverType.PLAYER, push.x, push.y, push.z);
                float damage = 1F - (entity.getDistance(this.entity) / size);
                if (this.entity instanceof EntityPlayer)
                    entity.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) this.entity), damage * 15F);
                else
                    entity.attackEntityFrom(DamageSource.causeMobDamage(this.entity), damage * 15F);
            }
        }

        this.getDataManager().set(ENERGY, 0);
        return true;
    }

    public void addEnergy(int energy) {
        getDataManager().set(ENERGY, MathHelper.clamp(getDataManager().get(ENERGY) + energy, 0, getDataManager().get(MAX_ENERGY)));
    }

    public float getPercentage() {
        return (float) this.getDataManager().get(ENERGY) / (float) this.getDataManager().get(MAX_ENERGY);
    }

    @Mod.EventBusSubscriber(modid = HeroesExpansion.MODID)
    public static class EventHandler {

        @SubscribeEvent
        public static void onDamage(LivingHurtEvent e) {
            if (e.getEntity() instanceof EntityPlayer) {

                if (e.getSource().isUnblockable() && e.getSource() != DamageSource.FALL && e.getSource() != DamageSource.WITHER)
                    return;

                EntityPlayer player = (EntityPlayer) e.getEntityLiving();
                List<AbilityKineticEnergyAbsorption> list = new ArrayList<>();
                for (AbilityKineticEnergyAbsorption ab : Ability.getAbilitiesFromClass(Ability.getAbilities(player), AbilityKineticEnergyAbsorption.class)) {
                    if (ab.isUnlocked()) {
                        list.add(ab);
                    }
                }

                if (list.size() == 0)
                    return;

                float energy = e.getAmount() / (float) list.size();

                e.setAmount(e.getAmount() * 0.25F);

                for (AbilityKineticEnergyAbsorption ab : list) {
                    ab.addEnergy((int) energy);
                }
            }
        }

    }

}
