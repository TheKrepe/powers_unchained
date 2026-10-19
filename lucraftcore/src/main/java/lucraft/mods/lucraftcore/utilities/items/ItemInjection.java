package lucraft.mods.lucraftcore.utilities.items;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import lucraft.mods.lucraftcore.util.items.ItemBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.EnumAction;
import net.minecraft.item.IItemPropertyGetter;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ItemInjection extends ItemBase {

    private static HashMap<ResourceLocation, Injection> injections = new HashMap<>();
    private static HashMap<Injection, ResourceLocation> injectionsLocs = new HashMap<>();

    public ItemInjection(String name) {
        super(name);
        this.setMaxStackSize(1);
        this.setCreativeTab(LucraftCore.CREATIVE_TAB);
        this.addPropertyOverride(new ResourceLocation("injection"), new IItemPropertyGetter() {
            @SideOnly(Side.CLIENT)
            public float apply(ItemStack stack, @Nullable World worldIn, @Nullable EntityLivingBase entityIn) {
                Injection injection = getInjection(stack);
                return injection == null ? 0F : 1F;
            }
        });
    }

    @Override
    public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, EntityLivingBase playerIn) {
        return stack;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
        playerIn.setActiveHand(hand);
        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, playerIn.getHeldItem(hand));
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        if (entityLiving instanceof EntityPlayer && timeLeft <= 71980) {
            EntityPlayer player = (EntityPlayer) entityLiving;
            Injection injection = getInjection(stack);

            if (injection == null) {
                int chance = 0;
                Injection in = null;
                for (Injection i : injections.values()) {
                    int x = i.priorityForRemoving(player, stack);
                    if (x > chance) {
                        chance = x;
                        in = i;
                    }
                }
                if (chance > 0 && in != null) {
                    setInjection(in.removeFromPlayer(player, stack), in);
                }
            } else {
                int chance = injection.chanceForInjection(player, stack);
                if (new Random().nextInt(100) >= 100 - chance)
                    setInjection(injection.inject(player, stack), null);
                else
                    player.addPotionEffect(new PotionEffect(MobEffects.NAUSEA, 100, 2));
            }
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab))
            return;

        items.add(new ItemStack(this));

        for (Injection in : injections.values()) {
            ItemStack stack = new ItemStack(this);
            setInjection(stack, in);
            items.add(stack);
        }
    }

    @Nullable
    @Override
    public String getCreatorModId(ItemStack itemStack) {
        Injection injection = getInjection(itemStack);
        if (injection != null)
            return injectionsLocs.get(injection).getNamespace();
        return super.getCreatorModId(itemStack);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        Injection in = getInjection(stack);
        if (in != null) {
            tooltip.add(in.getDisplayName());
            tooltip.add(StringHelper.translateToLocal("lucraftcore.info.injectionsuccess").replace("%s", "" + in.chanceForInjection(Minecraft.getMinecraft().player, stack)));
        }
    }

    public static Injection getInjection(ItemStack stack) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemInjection) || !stack.hasTagCompound())
            return null;

        ResourceLocation loc = new ResourceLocation(stack.getTagCompound().getString("Injection"));
        return injections.containsKey(loc) ? injections.get(loc) : null;
    }

    public static void setInjection(ItemStack stack, Injection injection) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof ItemInjection))
            return;

        NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        if (injection == null) {
            nbt.setString("Injection", "");
        } else {
            ResourceLocation loc = null;
            for (ResourceLocation locs : injections.keySet()) {
                if (injections.get(locs) == injection) {
                    loc = locs;
                }
            }
            if (loc != null) {
                nbt.setString("Injection", loc.toString());
            } else {
                nbt.setString("Injection", "");
            }
        }

        stack.setTagCompound(nbt);
    }

    public static void registerInjection(Injection injection, ResourceLocation loc) {
        injections.put(loc, injection);
        injectionsLocs.put(injection, loc);
    }

    public static abstract class Injection {

        private String name;

        public Injection(String name) {
            this.name = name;
        }

        public String getDisplayName() {
            return StringHelper.translateToLocal("injection." + name + ".name");
        }

        public abstract int chanceForInjection(EntityPlayer player, ItemStack stack);

        public abstract ItemStack inject(EntityPlayer player, ItemStack stack);

        public abstract int priorityForRemoving(EntityPlayer player, ItemStack stack);

        public abstract ItemStack removeFromPlayer(EntityPlayer player, ItemStack stack);

        public abstract int getColor();

    }

    @SideOnly(Side.CLIENT)
    public static class InjectionItemColor implements IItemColor {

        @Override
        public int colorMultiplier(ItemStack stack, int tintIndex) {
            int i = 16777215;
            Injection in = getInjection(stack);

            if (in == null || tintIndex != 1)
                return i;

            return in.getColor();
        }

    }

}
