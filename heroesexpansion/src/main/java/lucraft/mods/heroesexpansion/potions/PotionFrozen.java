package lucraft.mods.heroesexpansion.potions;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.lucraftcore.network.LCPacketDispatcher;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import lucraft.mods.lucraftcore.util.network.MessageSyncPotionEffects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AbstractAttributeMap;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(modid = HeroesExpansion.MODID)
public class PotionFrozen extends Potion {

    public static Potion FROZEN = new PotionFrozen().registerPotionAttributeModifier(SharedMonsterAttributes.MOVEMENT_SPEED, "0beb5af9-e6e7-49cc-95b1-cda2f2ea5d36", -10D, 2);

    @SubscribeEvent
    public static void onRegisterPotions(RegistryEvent.Register<Potion> e) {
        e.getRegistry().register(FROZEN);
    }

    @SubscribeEvent
    public static void onUpdate(LivingEvent.LivingUpdateEvent e) {
        if (e.getEntityLiving().isPotionActive(FROZEN)) {
            if (e.getEntityLiving().getActivePotionEffect(FROZEN).getDuration() == 1) {
                e.getEntityLiving().removePotionEffect(FROZEN);
                LCPacketDispatcher.sendToAll(new MessageSyncPotionEffects(e.getEntityLiving()));
                PlayerHelper.playSoundToAll(e.getEntityLiving().world, e.getEntityLiving().posX, e.getEntity().posY + e.getEntity().height / 2D, e.getEntity().posZ, 50, SoundEvents.BLOCK_GLASS_BREAK, SoundCategory.PLAYERS);
            }
        }
    }

    public static void freeze(EntityLivingBase entity, int duration) {
        PotionEffect effect = new PotionEffect(FROZEN, duration, 0, false, false);
        effect.getCurativeItems().clear();
        entity.addPotionEffect(effect);
    }

    //------------------------------------------------------------------------------

    protected PotionFrozen() {
        super(true, 0x32dcff);
        this.setPotionName("potion.frozen");
        this.setRegistryName(HeroesExpansion.MODID, "frozen");
    }

    @Override
    public void applyAttributesModifiersToEntity(EntityLivingBase entityLivingBaseIn, AbstractAttributeMap attributeMapIn, int amplifier) {
        super.applyAttributesModifiersToEntity(entityLivingBaseIn, attributeMapIn, amplifier);
        LCPacketDispatcher.sendToAll(new MessageSyncPotionEffects(entityLivingBaseIn));
        entityLivingBaseIn.getEntityData().setFloat("SavedYaw", entityLivingBaseIn.rotationYaw);
        entityLivingBaseIn.getEntityData().setFloat("SavedPitch", entityLivingBaseIn.rotationPitch);
    }

    @Override
    public void performEffect(EntityLivingBase entityLivingBaseIn, int amplifier) {
        super.performEffect(entityLivingBaseIn, amplifier);

        entityLivingBaseIn.rotationYaw = entityLivingBaseIn.getEntityData().getFloat("SavedYaw");
        entityLivingBaseIn.rotationPitch = entityLivingBaseIn.getEntityData().getFloat("SavedPitch");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        if (effect.getPotion() == this) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            mc.getRenderItem().renderItemIntoGUI(new ItemStack(Blocks.ICE), x + 8, y + 8);

            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderHUDEffect(int x, int y, PotionEffect effect, Minecraft mc, float alpha) {
        if (effect.getPotion() == this) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            mc.getRenderItem().renderItemIntoGUI(new ItemStack(Blocks.ICE), x + 4, y + 4);

            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

}
