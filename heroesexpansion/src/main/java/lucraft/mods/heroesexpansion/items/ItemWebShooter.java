package lucraft.mods.heroesexpansion.items;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.entities.EntityWebShoot;
import lucraft.mods.heroesexpansion.events.PlayerUseGadgetEvent;
import lucraft.mods.heroesexpansion.fluids.HEFluids;
import lucraft.mods.heroesexpansion.network.HEPacketDispatcher;
import lucraft.mods.heroesexpansion.network.MessageWebModesGUI;
import lucraft.mods.heroesexpansion.sounds.HESoundEvents;
import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.InventoryExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.capabilities.CapabilityExtendedInventory;
import lucraft.mods.lucraftcore.util.events.PlayerEmptyClickEvent;
import lucraft.mods.lucraftcore.util.fluids.LCFluidUtil;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import lucraft.mods.lucraftcore.util.items.ItemBase;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.RegistryNamespaced;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ItemWebShooter extends ItemBase implements IItemExtendedInventory {

    public static RegistryNamespaced<ResourceLocation, WebMode> WEB_MODES = new RegistryNamespaced<>();

    public int tier;
    public int capacity;
    public float velocity;

    public ItemWebShooter(String name, int tier, int capacity, float velocity) {
        super(name);
        this.setCreativeTab(HeroesExpansion.CREATIVE_TAB);
        this.setMaxStackSize(1);
        this.tier = tier;
        this.capacity = capacity;
        this.velocity = velocity;
    }

    @Override
    public ExtendedInventoryItemType getEIItemType(ItemStack stack) {
        return ExtendedInventoryItemType.WRIST;
    }

    @Override
    public boolean useButton(ItemStack stack, EntityPlayer player) {
        return true;
    }

    @Override
    public String getAbilityBarDescription(ItemStack stack, EntityPlayer player) {
        if (getWebMode(stack) != null && getWebMode(stack).name != null) {
            return getWebMode(stack).name.getFormattedText();
        }

        return stack.getDisplayName();
    }

    @Override
    public void onPressedButton(ItemStack itemstack, EntityPlayer player, boolean pressed) {
        if (!player.world.isRemote && pressed) {
            HEPacketDispatcher.sendTo(new MessageWebModesGUI(this.tier), (EntityPlayerMP) player);
        }
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        super.getSubItems(tab, items);
        if (this.isInCreativeTab(tab)) {
            ItemStack stack = new ItemStack(this);
            stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null).fill(new FluidStack(HEFluids.WEB_FLUID, getCapacity(stack)), true);
            items.add(stack);
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(StringHelper.translateToLocal("heroesexpansion.info.tier", this.tier));
        if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
            IFluidHandlerItem cap = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            for (int i = 0; i < cap.getTankProperties().length; i++) {
                IFluidTankProperties properties = cap.getTankProperties()[i];
                int amount = properties.getContents() != null ? properties.getContents().amount : 0;
                tooltip.add(new FluidStack(HEFluids.WEB_FLUID, 1).getLocalizedName() + ": " + LCFluidUtil.getFormattedFluidInfo(amount, properties.getCapacity()));
            }
        }
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        if (!stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null))
            return 1D;
        IFluidHandlerItem cap = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        FluidStack fluid = getWebFluid(stack);
        int amount = fluid != null ? fluid.amount : 0;
        double damage = 1D - ((double) amount / (double) getCapacity(stack));
        return damage;
    }

    @Override
    public int getRGBDurabilityForDisplay(ItemStack stack) {
        return MathHelper.hsvToRGB(0, 0, 0.839F);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public boolean isDamaged(ItemStack stack) {
        return true;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new WebFluidWrapper(stack, this);
    }

    public int getCapacity(ItemStack stack) {
        return this.capacity;
    }

    public FluidStack getWebFluid(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }

        String tag = "WebFluid";

        if (!stack.getTagCompound().hasKey(tag)) {
            return null;
        }

        return FluidStack.loadFluidStackFromNBT(stack.getTagCompound().getCompoundTag(tag));
    }

    public int fill(ItemStack stack, FluidStack resource, boolean doFill) {
        FluidStack fluid = getWebFluid(stack);
        int capacity = getCapacity(stack);

        if (resource == null || resource.amount <= 0) {
            return 0;
        }

        if (!doFill) {
            if (fluid == null) {
                return Math.min(capacity, resource.amount);
            }

            if (!fluid.isFluidEqual(resource)) {
                return 0;
            }

            return Math.min(capacity - fluid.amount, resource.amount);
        }

        if (fluid == null) {
            fluid = new FluidStack(resource, Math.min(capacity, resource.amount));
            NBTTagCompound nbt = fluid.writeToNBT(new NBTTagCompound());
            stack.getTagCompound().setTag("WebFluid", nbt);
            return fluid.amount;
        }

        if (!fluid.isFluidEqual(resource)) {
            return 0;
        }
        int filled = capacity - fluid.amount;

        if (resource.amount < filled) {
            fluid.amount += resource.amount;
            filled = resource.amount;
        } else {
            fluid.amount = capacity;
        }

        NBTTagCompound nbt = fluid.writeToNBT(new NBTTagCompound());
        stack.getTagCompound().setTag("WebFluid", nbt);

        return filled;
    }

    public FluidStack drain(ItemStack stack, FluidStack resource, boolean doDrain) {
        if (resource == null || !resource.isFluidEqual(getWebFluid(stack)))
            return null;
        return drain(stack, resource.amount, doDrain);
    }

    public FluidStack drain(ItemStack stack, int maxDrain, boolean doDrain) {
        FluidStack fluid = getWebFluid(stack);

        if (fluid == null || maxDrain <= 0)
            return null;

        int drained = maxDrain;
        if (fluid.amount < drained) {
            drained = fluid.amount;
        }

        FluidStack fluidStack = new FluidStack(fluid, drained);
        if (doDrain) {
            fluid.amount -= drained;
            if (fluid.amount <= 0) {
                fluid = null;
            }

            NBTTagCompound nbt = fluid == null ? new NBTTagCompound() : fluid.writeToNBT(new NBTTagCompound());
            stack.getTagCompound().setTag("WebFluid", nbt);
        }

        return fluidStack;
    }

    public static WebMode getWebMode(ItemStack stack) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("WebMode") && WEB_MODES.containsKey(new ResourceLocation(stack.getTagCompound().getString("WebMode")))) {
            WebMode webMode = WEB_MODES.getObject(new ResourceLocation(stack.getTagCompound().getString("WebMode")));
            return webMode == null ? WEB_MODES.getObjectById(0) : webMode;
        } else {
            return WEB_MODES.getObjectById(0);
        }
    }

    public static void setWebMode(ItemStack stack, WebMode mode) {
        if (!(stack.getItem() instanceof ItemWebShooter))
            return;
        if (mode.requiredTier > ((ItemWebShooter) stack.getItem()).tier)
            return;
        NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        nbt.setString("WebMode", WEB_MODES.getNameForObject(mode).toString());
        stack.setTagCompound(nbt);
    }

    public static List<WebMode> getWebModesForTier(int tier) {
        List<WebMode> list = new ArrayList<>();
        for (ResourceLocation loc : WEB_MODES.getKeys()) {
            WebMode webMode = WEB_MODES.getObject(loc);

            if (webMode.requiredTier <= tier)
                list.add(webMode);
        }

        return list;
    }

    @Mod.EventBusSubscriber(modid = HeroesExpansion.MODID)
    public static class EventHandler {

        @SubscribeEvent
        public static void onLeftClick(PlayerEmptyClickEvent.LeftClick e) {
            boolean sound = false;
            float radius = 200F;
            for (EntityWebShoot entity : e.getEntityLiving().getEntityWorld().getEntitiesWithinAABB(EntityWebShoot.class, new AxisAlignedBB(e.getEntityPlayer().getPosition().add(radius, radius, radius), e.getEntityPlayer().getPosition().add(-radius, -radius, -radius)))) {
                if (!entity.world.isRemote && entity.canBeCut() && entity.getThrower() == e.getEntityPlayer()) {
                    entity.setDead();
                    sound = true;
                }
            }
            if (sound)
                PlayerHelper.playSoundToAll(e.getEntityPlayer().world, e.getEntity().posX, e.getEntity().posY, e.getEntity().posZ, 50, SoundEvents.ENTITY_SHEEP_SHEAR, SoundCategory.PLAYERS);
        }

        @SubscribeEvent
        public static void onRightClick(PlayerEmptyClickEvent.RightClick e) {
            InventoryExtendedInventory inv = e.getEntityPlayer().getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).getInventory();
            ItemStack stack = inv.getStackInSlot(InventoryExtendedInventory.SLOT_WRIST);

            if (e.getEntityPlayer().getHeldItemMainhand().isEmpty() && !stack.isEmpty() && stack.getItem() instanceof ItemWebShooter && e.getEntityPlayer().getCooldownTracker().getCooldown(stack.getItem(), 0) <= 0 && stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null) && !MinecraftForge.EVENT_BUS.post(new PlayerUseGadgetEvent(e.getEntityPlayer(), stack))) {
                IFluidHandlerItem cap = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
                FluidStack drain = cap.drain(50, false);
                if (drain != null && drain.amount >= 50) {
                    cap.drain(50, true);
                    EntityPlayer player = e.getEntityPlayer();
                    EntityWebShoot webShoot = getWebMode(stack).createEntity(player.world, player);
                    webShoot.shoot(player, player.rotationPitch, player.rotationYaw, 0.0F, ((ItemWebShooter) stack.getItem()).velocity, 1.0F);
                    player.world.spawnEntity(webShoot);
                    player.getCooldownTracker().setCooldown(stack.getItem(), 20);
                    PlayerHelper.playSoundToAll(player.world, player.posX, player.posY, player.posZ, 50, HESoundEvents.WEB_SHOOT, SoundCategory.PLAYERS);
                    PlayerHelper.swingPlayerArm(player, new Random().nextBoolean() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND);
                    e.getEntityPlayer().getCapability(CapabilityExtendedInventory.EXTENDED_INVENTORY_CAP, null).syncToAll();
                }
            }
        }

    }

    public static class WebMode {

        public Class<? extends EntityWebShoot> entityClass;
        public ITextComponent name;
        public int requiredTier;

        public WebMode(Class<? extends EntityWebShoot> entityClass, ITextComponent name, int requiredTier) {
            this.entityClass = entityClass;
            this.name = name;
            this.requiredTier = requiredTier;
        }

        public EntityWebShoot createEntity(World world, EntityLivingBase shooter) {
            try {
                EntityWebShoot entity = entityClass.getConstructor(World.class, EntityLivingBase.class).newInstance(world, shooter);
                return entity;
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
            return null;
        }

    }

    public class WebFluidWrapper implements IFluidHandlerItem, ICapabilityProvider {

        @Nonnull
        protected ItemStack container;
        protected ItemWebShooter item;

        public WebFluidWrapper(@Nonnull ItemStack container, ItemWebShooter item) {
            this.container = container;
            this.item = item;
        }

        @Override
        public boolean hasCapability(@Nonnull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;
        }

        @Override
        @Nullable
        public <T> T getCapability(@Nonnull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY) {
                return CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.cast(this);
            }
            return null;
        }

        @Nonnull
        @Override
        public ItemStack getContainer() {
            return this.container;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            FluidStack stack = item.getWebFluid(getContainer());
            int capacity = item.getCapacity(getContainer());
            return new IFluidTankProperties[]{new FluidTankProperties(stack, capacity, true, true)};
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return item.fill(getContainer(), resource, doFill);
        }

        @Nullable
        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return item.drain(getContainer(), resource, doDrain);
        }

        @Nullable
        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            if (drain(new FluidStack(HEFluids.WEB_FLUID, maxDrain), false) != null && drain(new FluidStack(HEFluids.WEB_FLUID, maxDrain), false).amount > 0)
                return drain(new FluidStack(HEFluids.WEB_FLUID, maxDrain), doDrain);
            return null;
        }

    }

}
