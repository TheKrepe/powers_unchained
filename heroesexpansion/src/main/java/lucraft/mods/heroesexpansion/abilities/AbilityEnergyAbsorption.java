package lucraft.mods.heroesexpansion.abilities;

import lucraft.mods.heroesexpansion.util.helper.HEIconHelper;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityToggle;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityData;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataFloat;
import lucraft.mods.lucraftcore.superpowers.abilities.data.AbilityDataInteger;
import lucraft.mods.lucraftcore.superpowers.abilities.supplier.EnumSync;
import lucraft.mods.lucraftcore.superpowers.entities.EntityEnergyBlast;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

public class AbilityEnergyAbsorption extends AbilityToggle {

    public static final AbilityData<Float> ENERGY = new AbilityDataFloat("energy").setSyncType(EnumSync.SELF);
    public static final AbilityData<Integer> MAX_ENERGY = new AbilityDataInteger("max_energy").disableSaving().setSyncType(EnumSync.SELF).enableSetting("max_energy", "The maximum amount of energy that you can absorb.");
    public static final AbilityData<Float> MAX_DAMAGE = new AbilityDataFloat("max_damage").disableSaving().setSyncType(EnumSync.SELF).enableSetting("max_damage", "The maximum amount of damage that you can cause.");

    public AbilityEnergyAbsorption(EntityLivingBase entity) {
        super(entity);
    }

    @Override
    public void registerData() {
        super.registerData();
        this.dataManager.register(ENERGY, 0F);
        this.dataManager.register(MAX_ENERGY, 100);
        this.dataManager.register(MAX_DAMAGE, 10F);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawIcon(Minecraft mc, Gui gui, int x, int y) {
        HEIconHelper.drawIcon(mc, gui, x, y, 1, 7);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void drawAdditionalInfo(Minecraft mc, Gui gui, int x, int y) {
        super.drawAdditionalInfo(mc, gui, x, y);

        GlStateManager.pushMatrix();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.disableTexture2D();
        Tessellator tes = Tessellator.getInstance();
        BufferBuilder bb = tes.getBuffer();
        Vec3d color = new Vec3d(0.7F, 0.7F, 0);
        float percentage = this.getDataManager().get(ENERGY) / (float) this.getDataManager().get(MAX_ENERGY);
        GlStateManager.translate(0, this.renderCooldown() ? -6 : -3, 0);

        GlStateManager.color(0, 0, 0);
        bb.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        bb.pos(4, y + 16, 50).endVertex();
        bb.pos(4 + 14, y + 16, 50).endVertex();
        bb.pos(4 + 14, y + 18, 50).endVertex();
        bb.pos(4, y + 18, 50).endVertex();
        tes.draw();

        GlStateManager.color((float) color.x, (float) color.y, (float) color.z, 1);
        bb.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION);
        bb.pos(4, y + 16, 50).endVertex();
        bb.pos(4 + percentage * 14, y + 16, 50).endVertex();
        bb.pos(4 + percentage * 14, y + 17, 50).endVertex();
        bb.pos(4, y + 17, 50).endVertex();
        tes.draw();

        GlStateManager.enableTexture2D();
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    @Override
    public void updateTick() {

    }

    @Override
    public void onHurt(LivingHurtEvent e) {
        if (this.isEnabled() && this.getDataManager().get(ENERGY) > 0) {
            float f = MathHelper.clamp(this.getDataManager().get(ENERGY), 0, this.getDataManager().get(MAX_ENERGY));
            this.setDataValue(ENERGY, this.getDataManager().get(ENERGY) - f);
            e.setAmount(e.getAmount() + f);
        }
    }

    @Override
    public void onEntityHurt(LivingHurtEvent e) {
        if (!this.isEnabled() && e.getSource() == DamageSource.LIGHTNING_BOLT || e.getSource() == DamageSource.DRAGON_BREATH || e.getSource() == DamageSource.WITHER || e.getSource() == DamageSource.MAGIC || (e.getSource().getImmediateSource() != null && e.getSource().getImmediateSource() instanceof EntityEnergyBlast)) {
            float f = 10F;
            this.getDataManager().set(ENERGY, MathHelper.clamp(this.getDataManager().get(ENERGY) + f, 0, this.getDataManager().get(MAX_ENERGY)));
        }
    }
}
