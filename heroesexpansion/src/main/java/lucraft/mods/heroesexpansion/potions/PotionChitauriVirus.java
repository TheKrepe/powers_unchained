package lucraft.mods.heroesexpansion.potions;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.items.HEItems;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = HeroesExpansion.MODID)
public class PotionChitauriVirus extends Potion {

    public static PotionChitauriVirus CHITAURI_VIRUS = new PotionChitauriVirus();

    @SubscribeEvent
    public static void onRegisterPotions(RegistryEvent.Register<Potion> e) {
        e.getRegistry().register(CHITAURI_VIRUS);
    }

    @SubscribeEvent
    public static void onTick(LivingEvent.LivingUpdateEvent e) {
        if (e.getEntityLiving().ticksExisted % 20 == 0) {
            int level = 0;
            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                if (!e.getEntityLiving().getItemStackFromSlot(slot).isEmpty() && e.getEntityLiving().getItemStackFromSlot(slot).hasTagCompound() && e.getEntityLiving().getItemStackFromSlot(slot).getTagCompound().getBoolean("ChitauriVirus")) {
                    level++;
                }
            }

            if (level > 0) {
                PotionEffect effect = new PotionEffect(CHITAURI_VIRUS, 60 * 20, level - 1);
                effect.setCurativeItems(new ArrayList<>());
                e.getEntityLiving().addPotionEffect(effect);
            }
        }

        PotionEffect potion = e.getEntityLiving().getActivePotionEffect(CHITAURI_VIRUS);
        if (potion != null) {
            if (potion.getDuration() == 1) {
                e.getEntityLiving().setHealth(0);
                e.getEntityLiving().world.newExplosion(e.getEntity(), e.getEntity().posX, e.getEntity().posY, e.getEntity().posZ, 0, false, false);
            }
            int range = (potion.getAmplifier() + 1) * 3;
            for (EntityItem entity : e.getEntityLiving().world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(e.getEntity().posX - range, e.getEntity().posY - range, e.getEntity().posZ - range, e.getEntity().posX + range, e.getEntity().posY + range, e.getEntity().posZ + range))) {
                List<String> list = new ArrayList<>();

                if (!entity.getItem().isEmpty()) {
                    list.add(entity.getItem().getItem().getTranslationKey());

                    for (int i : OreDictionary.getOreIDs(entity.getItem())) {
                        String ore = OreDictionary.getOreName(i).toLowerCase();
                        list.add(ore);
                    }

                    if (entity.getItem().getItem() instanceof ItemTool) {
                        ItemTool tool = (ItemTool) entity.getItem().getItem();
                        list.add(tool.getToolMaterialName());
                    }
                }

                boolean b = false;

                for (String s : list) {
                    if (s.toLowerCase().contains("iron") || s.toLowerCase().contains("cobalt") || s.toLowerCase().contains("nickel")) {
                        b = true;
                        break;
                    }
                }

                if (b)
                    entity.motionY = 0.2F;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent e) {
        if (e.getItemStack().hasTagCompound() && e.getItemStack().getTagCompound().getBoolean("ChitauriVirus")) {
            e.getToolTip().add(TextFormatting.RED + "" + TextFormatting.BOLD + StringHelper.translateToLocal(CHITAURI_VIRUS.getName()));
        }
    }

    // ------------------------------------------------------------------------------

    public PotionChitauriVirus() {
        super(true, 0xe200b0);
        this.setPotionName("potion.chitauri_virus");
        this.setRegistryName(HeroesExpansion.MODID, "chitauri_virus");
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void renderInventoryEffect(int x, int y, PotionEffect effect, Minecraft mc) {
        if (effect.getPotion() == this) {
            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            mc.getRenderItem().renderItemIntoGUI(new ItemStack(HEItems.CHITAURI_METAL), x + 8, y + 8);

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

            mc.getRenderItem().renderItemIntoGUI(new ItemStack(HEItems.CHITAURI_METAL), x + 4, y + 4);

            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }

}
