package lucraft.mods.lucraftcore.superpowers.abilities;

import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityData;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataFloat;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class AbilityTeleport extends AbilityAction {

    public static final AbilityData<Float> DISTANCE = new AbilityDataFloat("distance").disableSaving().setSyncType(EnumSync.NONE).enableSetting("distance", "The maximum amount of blocks you can teleport to");

    public AbilityTeleport(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(DISTANCE, 1F);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        float zLevel = Minecraft.getMinecraft().getRenderItem().zLevel;
        Minecraft.getMinecraft().getRenderItem().zLevel = -100.5F;
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(new ItemStack(Items.ENDER_PEARL), 0, 0);
        GlStateManager.popMatrix();
        Minecraft.getMinecraft().getRenderItem().zLevel = zLevel;
    }

    @Override
    public boolean action() {
        Vec3d lookVec = entity.getLookVec().scale(this.dataManager.get(DISTANCE));
        RayTraceResult rtr = entity.world.rayTraceBlocks(new Vec3d(entity.posX, entity.posY + entity.getEyeHeight(), entity.posZ),
                new Vec3d(entity.posX + lookVec.x, entity.posY + entity.getEyeHeight() + lookVec.y, entity.posZ + lookVec.z));

        if (rtr != null && rtr.hitVec != null) {
            PlayerHelper.playSoundToAll(entity.world, entity.posX, entity.posY, entity.posZ, 50, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS);
            entity.setPositionAndUpdate(rtr.hitVec.x, rtr.hitVec.y, rtr.hitVec.z);
            PlayerHelper.playSoundToAll(entity.world, entity.posX, entity.posY, entity.posZ, 50, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.PLAYERS);
            for (int i = 0; i < 30; ++i) {
                PlayerHelper.spawnParticleForAll(
                        entity.world, 50, EnumParticleTypes.PORTAL, true, (float) entity.posX,
                        (float) entity.posY + entity.world.rand.nextFloat() * (float) entity.height, (float) entity.posZ, (
                                entity.world.rand.nextFloat() - 0.5F) * 2.0F, -entity.world.rand.nextFloat(), (entity.world.rand.nextFloat() - 0.5F) * 2.0F, 1,
                        10);
            }
            return true;
        }
        return false;
    }
}
