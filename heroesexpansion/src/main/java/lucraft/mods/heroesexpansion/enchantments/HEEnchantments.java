package lucraft.mods.heroesexpansion.enchantments;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.items.ItemThorWeapon;
import lucraft.mods.lucraftcore.karma.KarmaHandler;
import lucraft.mods.lucraftcore.karma.KarmaStat;
import lucraft.mods.lucraftcore.sizechanging.entities.EntitySizeChanging;
import lucraft.mods.lucraftcore.superpowers.effects.EffectTrail;
import lucraft.mods.lucraftcore.util.helper.LCEntityHelper;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = HeroesExpansion.MODID)
public class HEEnchantments {

    public static Enchantment WORTHINESS = new EnchantmentWorthiness(Enchantment.Rarity.VERY_RARE, EnumEnchantmentType.ALL, EntityEquipmentSlot.values());

    @SubscribeEvent
    public static void onRegisterEnchantments(RegistryEvent.Register<Enchantment> e) {
        e.getRegistry().register(WORTHINESS);
    }

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingUpdateEvent e) {
        if (e.getEntityLiving().ticksExisted % 10 == 0 && !e.getEntityLiving().world.isRemote) {

            if (e.getEntityLiving() instanceof EffectTrail.EntityTrail || e.getEntityLiving() instanceof EntitySizeChanging || isWorthy(e.getEntityLiving()))
                return;

            for (ItemStack stack : WORTHINESS.getEntityEquipment(e.getEntityLiving())) {
                if (EnchantmentHelper.getEnchantmentLevel(WORTHINESS, stack) > 0 && !(stack.getItem() instanceof ItemThorWeapon)) {
                    LCEntityHelper.entityDropItem(e.getEntityLiving(), stack, -0.3F + e.getEntityLiving().getEyeHeight(), true);
                    if (e.getEntityLiving() instanceof EntityPlayer)
                        ((EntityPlayer) e.getEntityLiving()).sendStatusMessage(new TextComponentTranslation("heroesexpansion.info.not_worthy_for_item"), true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPickUp(EntityItemPickupEvent e) {
        if (EnchantmentHelper.getEnchantmentLevel(WORTHINESS, e.getItem().getItem()) > 0 && !isWorthy(e.getEntityLiving())) {
            e.setCanceled(true);

            if (e.getEntityLiving() instanceof EntityPlayer)
                ((EntityPlayer) e.getEntityLiving()).sendStatusMessage(new TextComponentTranslation("heroesexpansion.info.not_worthy_for_item"), true);
        }
    }

    public static boolean isWorthy(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer)
            return ((EntityPlayer) entity).isCreative() || KarmaHandler.getKarma((EntityPlayer) entity) >= KarmaStat.KarmaClass.SUPERHERO.getMinKarma();
        else
            return false;
    }

}