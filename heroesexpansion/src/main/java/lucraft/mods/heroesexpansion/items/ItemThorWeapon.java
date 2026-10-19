package lucraft.mods.heroesexpansion.items;

import com.google.common.collect.Multimap;
import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.client.render.item.ItemRendererMjolnir;
import lucraft.mods.heroesexpansion.client.render.item.ItemRendererStormbreaker;
import lucraft.mods.heroesexpansion.client.render.item.ItemRendererUltimateMjolnir;
import lucraft.mods.heroesexpansion.enchantments.HEEnchantments;
import lucraft.mods.heroesexpansion.entities.EntityThorWeapon;
import lucraft.mods.heroesexpansion.entities.EntityThrownThorWeapon;
import lucraft.mods.heroesexpansion.events.PlayerUseGadgetEvent;
import lucraft.mods.heroesexpansion.sounds.HESoundEvents;
import lucraft.mods.heroesexpansion.superpowers.SuperpowerGodOfThunder;
import lucraft.mods.lucraftcore.sizechanging.entities.EntitySizeChanging;
import lucraft.mods.lucraftcore.superpowers.effects.EffectTrail;
import lucraft.mods.lucraftcore.superpowers.models.ModelBipedSuitSet;
import lucraft.mods.lucraftcore.util.events.PlayerEmptyClickEvent;
import lucraft.mods.lucraftcore.util.events.RenderModelEvent;
import lucraft.mods.lucraftcore.util.helper.LCEntityHelper;
import lucraft.mods.lucraftcore.util.helper.PlayerHelper;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import lucraft.mods.lucraftcore.util.items.ExtendedTooltip;
import lucraft.mods.lucraftcore.util.items.ItemBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ItemThorWeapon extends ItemBase implements ExtendedTooltip.IExtendedItemToolTip {

    public static final float NEEDED_STRENGTH = 10;
    public final Item.ToolMaterial material;
    private final float attackDamage;

    public float entityHeight = 0.25F;
    public float entityWidth = 0.25F;

    public ItemThorWeapon(String name, int attackDamage) {
        super(name);
        this.setCreativeTab(HeroesExpansion.CREATIVE_TAB);
        this.material = EnumHelper.addToolMaterial(name, 4, 16384, 10, attackDamage, 8);
        this.attackDamage = 3.0F + material.getAttackDamage();
        this.maxStackSize = 1;
        this.setMaxDamage(material.getMaxUses());
    }

    public Item setEntitySize(float height, float width) {
        this.entityHeight = height;
        this.entityWidth = width;
        return this;
    }

    @SideOnly(Side.CLIENT)
    public void renderThrownEntityModel(EntityThrownThorWeapon entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (entity.item.getItem() == HEItems.MJOLNIR) {
            GlStateManager.rotate(90, 0, 0, 1);
            GlStateManager.translate(0.2F, 0F, -0.05F);
            ItemRendererMjolnir.renderMjolnir(entity.item, partialTicks, false);
        } else if (entity.item.getItem() == HEItems.STORMBREAKER) {
            GlStateManager.rotate(90, 1, 0, 0);
            GlStateManager.rotate(-(entity.ticksExisted + partialTicks) * 60F, 0, 0, 1);
            GlStateManager.translate(0, 0.3F, -0.3F);
            ItemRendererStormbreaker.renderStormbreaker(entity.item, partialTicks);
        } else if (entity.item.getItem() == HEItems.ULTIMATE_MJOLNIR) {
            GlStateManager.rotate(90, 1, 0, 0);
            GlStateManager.rotate(-(entity.ticksExisted + partialTicks) * 60F, 0, 0, 1);
            GlStateManager.translate(0, 0.3F, -0.3F);
            ItemRendererUltimateMjolnir.renderUltimateMjolnir(entity.item, partialTicks);
        }
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return oldStack != newStack;
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            items.add(new ItemStack(this));
            ItemStack stack = new ItemStack(this);
            stack.addEnchantment(HEEnchantments.WORTHINESS, 1);
            items.add(stack);
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand hand) {
        ItemStack stack = playerIn.getHeldItem(hand);
        NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();

        if (playerIn.isSneaking()) {
            if (nbt.hasKey("Owner")) {
                if (UUID.fromString(nbt.getString("Owner")).equals(playerIn.getGameProfile().getId())) {
                    nbt.removeTag("Owner");
                    nbt.removeTag("OwnerName");
                    stack.setTagCompound(nbt);
                    playerIn.sendStatusMessage(new TextComponentTranslation("heroesexpansion.info.remove_possession"), true);
                } else {
                    playerIn.sendStatusMessage(new TextComponentTranslation("heroesexpansion.info.hammer_belongs_to", nbt.getString("OwnerName")), true);
                }
            } else {
                if (HEEnchantments.isWorthy(playerIn)) {
                    nbt.setString("Owner", playerIn.getGameProfile().getId().toString());
                    stack.setTagCompound(nbt);
                    playerIn.sendStatusMessage(new TextComponentTranslation("heroesexpansion.info.weapon_belongs_you"), true);
                } else {
                    playerIn.sendStatusMessage(new TextComponentTranslation("heroesexpansion.info.not_worthy"), true);
                }
            }
        } else if (hand == EnumHand.MAIN_HAND) {
            playerIn.setActiveHand(hand);
            nbt.setBoolean("Active", true);
            stack.setTagCompound(nbt);
        }

        return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return Integer.MAX_VALUE;
    }

    @Override
    public void onCreated(ItemStack stack, World worldIn, EntityPlayer playerIn) {
        super.onCreated(stack, worldIn, playerIn);

        if (!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        nbt.setBoolean("Active", false);
        stack.setTagCompound(nbt);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, ITooltipFlag advanced) {
        if (stack.hasTagCompound() && stack.getTagCompound().hasKey("OwnerName")) {
            tooltip.add(StringHelper.translateToLocal("heroesexpansion.into.owner").replace("%s", stack.getTagCompound().getString("OwnerName")));
        }
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
        String wielder = nbt.getString("Wielder");

        if (!wielder.equalsIgnoreCase(entityIn.getPersistentID().toString())) {
            nbt.setString("Wielder", entityIn.getPersistentID().toString());
            stack.setTagCompound(nbt);
        }

        if (!worldIn.isRemote && entityIn instanceof EntityLivingBase) {
            try {
                canLift((EntityLivingBase) entityIn, stack);
            } catch (Exception e) {
                LCEntityHelper.entityDropItem((EntityLivingBase) entityIn, stack, -0.3F + entityIn.getEyeHeight(), true);
                if (entityIn instanceof EntityPlayer) {
                    ((EntityPlayer) entityIn).sendStatusMessage(new TextComponentTranslation(e.getMessage()), true);
                }
                return;
            }
        }

        if (entityIn instanceof EntityPlayer && !worldIn.isRemote && entityIn.ticksExisted > 5 * 20) {
            EntityPlayer player = (EntityPlayer) entityIn;

            if (nbt.hasKey("Owner") && UUID.fromString(nbt.getString("Owner")).equals(player.getGameProfile().getId()) && !nbt.getString("OwnerName").equalsIgnoreCase(player.getGameProfile().getName())) {
                nbt.setString("OwnerName", player.getGameProfile().getName());
                stack.setTagCompound(nbt);
            }

            if (isSelected && nbt.getBoolean("Active") && player.getActiveItemStack() != stack) {
                nbt.setBoolean("Active", false);
                stack.setTagCompound(nbt);
            }
        }
    }

    public static boolean canLift(EntityLivingBase entity, ItemStack stack) throws Exception {
        if (EnchantmentHelper.getEnchantmentLevel(HEEnchantments.WORTHINESS, stack) > 0) {
            if (HEEnchantments.isWorthy(entity))
                return true;
            else
                throw new Exception("heroesexpansion.info.not_worthy");
        } else {
            if (LCEntityHelper.isStrongEnough(entity, NEEDED_STRENGTH))
                return true;
            else
                throw new Exception("lucraftcore.info.too_heavy");
        }
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public Entity createEntity(World world, Entity location, ItemStack itemstack) {
        EntityThorWeapon mjolnir = new EntityThorWeapon(world, location.posX, location.posY, location.posZ, itemstack, entityHeight, entityWidth);
        mjolnir.motionX = location.motionX;
        mjolnir.motionY = location.motionY;
        mjolnir.motionZ = location.motionZ;
        PlayerHelper.playSoundToAll(world, location.posX, location.posY, location.posZ, 50.0D, HESoundEvents.HAMMER_DROP, SoundCategory.PLAYERS);
        return mjolnir;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        stack.damageItem(1, attacker);
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World worldIn, IBlockState state, BlockPos pos, EntityLivingBase entityLiving) {
        if ((double) state.getBlockHardness(worldIn, pos) != 0.0D) {
            stack.damageItem(2, entityLiving);
        }

        return true;
    }

    @Override
    public boolean canHarvestBlock(IBlockState blockIn) {
        return blockIn.getBlock() == Blocks.WEB;
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
        @SuppressWarnings("deprecation")
        Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);

        if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
            multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", this.attackDamage, 0));
            multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(), new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", -3.5D, 0));
        }

        return multimap;
    }

    @Override
    public boolean shouldShiftTooltipAppear(ItemStack stack, EntityPlayer player) {
        return true;
    }

    @Override
    public List<String> getShiftToolTip(ItemStack stack, EntityPlayer player) {
        List<String> list = new ArrayList<>();
        for (String s : StringHelper.translateToLocal("heroesexpansion.info.thor_weapon_tooltip").split("§n"))
            list.add(s);
        return list;
    }

    @Override
    public boolean shouldCtrlTooltipAppear(ItemStack stack, EntityPlayer player) {
        return false;
    }

    @Override
    public List<String> getCtrlToolTip(ItemStack stack, EntityPlayer player) {
        return null;
    }

    @EventBusSubscriber(modid = HeroesExpansion.MODID)
    public static class EventHandler {

        @SubscribeEvent
        public static void leftClick(PlayerEmptyClickEvent.LeftClick e) {
            if (e.getHand() == EnumHand.OFF_HAND)
                return;

            if (MinecraftForge.EVENT_BUS.post(new PlayerUseGadgetEvent(e.getEntityPlayer(), e.getEntityPlayer().getHeldItemMainhand())))
                return;

            if (e.getEntityPlayer().getHeldItemMainhand().isEmpty()) {
                for (EntityThrownThorWeapon en : e.getEntityPlayer().world.getEntitiesWithinAABB(EntityThrownThorWeapon.class, new AxisAlignedBB(e.getEntityPlayer().getPosition().add(50, 50, 50), e.getEntityPlayer().getPosition().add(-50, -50, -50)))) {
                    if (en.getThrower() == e.getEntityPlayer()) {
                        if (e.getEntityPlayer().isSneaking() && en.ticksExisted > 20) {
                            en.backToUser = true;
                        } else {
                            Vec3d v = e.getEntityPlayer().getLookVec().scale(en.getDistance(e.getEntityPlayer()));
                            en.location = new BlockPos(e.getEntityPlayer().posX + v.x, e.getEntityPlayer().posY + v.y, e.getEntityPlayer().posZ + v.z);
                        }
                    }
                }
            }

            if (!e.getEntityPlayer().getHeldItemMainhand().isEmpty() && e.getEntityPlayer().getHeldItemMainhand().getItem() instanceof ItemThorWeapon) {
                if (e.getEntityPlayer().isSneaking() && !e.getEntityPlayer().getCooldownTracker().hasCooldown(e.getEntityPlayer().getHeldItemMainhand().getItem())) {
                    EntityThrownThorWeapon shield = new EntityThrownThorWeapon(e.getEntityPlayer().getEntityWorld(), e.getEntityPlayer());
                    shield.item = e.getEntityPlayer().getHeldItemMainhand();
                    shield.shoot(e.getEntityPlayer(), e.getEntityPlayer().rotationPitch, e.getEntityPlayer().rotationYaw, 0.0F, 2F, 1.0F);
                    e.getEntityPlayer().getEntityWorld().spawnEntity(shield);
                    e.getEntityPlayer().getCooldownTracker().setCooldown(e.getEntityPlayer().getHeldItemMainhand().getItem(), 30);
                    e.getEntityPlayer().setItemStackToSlot(EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    PlayerHelper.playSoundToAll(e.getEntityPlayer().getEntityWorld(), e.getEntityPlayer().posX, e.getEntityPlayer().posY, e.getEntityPlayer().posZ, 30, HESoundEvents.HAMMER_THROW, SoundCategory.PLAYERS);
                    SuperpowerGodOfThunder.addXP(e.getEntityPlayer(), SuperpowerGodOfThunder.XP_AMOUNT_MJOLNIR_THROW);
                } else {
                    PlayerHelper.playSoundToAll(e.getEntityPlayer().getEntityWorld(), e.getEntityPlayer().posX, e.getEntityPlayer().posY, e.getEntityPlayer().posZ, 30, HESoundEvents.HAMMER_SWING, SoundCategory.PLAYERS);
                }
            }
        }

        @SubscribeEvent
        public static void onLeftClick(PlayerInteractEvent.LeftClickBlock e) {
            if (!e.getEntityPlayer().getEntityWorld().isRemote && !e.getEntityPlayer().getHeldItemMainhand().isEmpty() && e.getEntityPlayer().getHeldItemMainhand().getItem() instanceof ItemThorWeapon) {
                PlayerHelper.playSoundToAll(e.getEntityPlayer().getEntityWorld(), e.getEntityPlayer().posX, e.getEntityPlayer().posY, e.getEntityPlayer().posZ, 30, HESoundEvents.HAMMER_HIT, SoundCategory.PLAYERS);
            }
        }

        @SubscribeEvent
        public static void onToss(ItemTossEvent e) {
            if (e.getEntityItem().getItem().getItem() instanceof ItemThorWeapon) {
                ItemStack stack = e.getEntityItem().getItem();
                if (!stack.hasTagCompound())
                    stack.setTagCompound(new NBTTagCompound());
            }
        }

        @SubscribeEvent
        public static void onLivingUpdate(LivingUpdateEvent e) {
            if (!e.getEntityLiving().getHeldItemMainhand().isEmpty() && e.getEntityLiving().getHeldItemMainhand().getItem() instanceof ItemThorWeapon && e.getEntityLiving().getHeldItemMainhand().hasTagCompound() && e.getEntityLiving().getHeldItemMainhand().getTagCompound().getBoolean("Active")) {
                EntityLivingBase player = e.getEntityLiving();

                if (player instanceof EntityPlayer && MinecraftForge.EVENT_BUS.post(new PlayerUseGadgetEvent((EntityPlayer) player, player.getHeldItemMainhand())))
                    return;

                if (!e.getEntityLiving().onGround) {
                    Vec3d vec = player.getLookVec();
                    player.motionX = vec.x;
                    player.motionY = vec.y;
                    player.motionZ = vec.z;
                    player.fallDistance = 0F;
                }

                if (player instanceof EntityPlayer) {
                    if (!e.getEntityLiving().onGround)
                        SuperpowerGodOfThunder.addXP((EntityPlayer) player, SuperpowerGodOfThunder.XP_AMOUNT_MJOLNIR_FLY);

                    if (e.getEntityLiving().onGround && e.getEntityLiving().getHeldItemMainhand().getItem() != HEItems.MJOLNIR)
                        return;

                    for (EntityLivingBase entityLivingBase : e.getEntity().world.getEntitiesWithinAABB(EntityLivingBase.class, e.getEntity().getEntityBoundingBox())) {
                        if (entityLivingBase != player && !(entityLivingBase instanceof EffectTrail.EntityTrail) && !(entityLivingBase instanceof EntitySizeChanging)) {
                            float value = (float) (player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getAttributeValue() * (e.getEntityLiving().onGround ? 0.1F : 1.3F));
                            entityLivingBase.attackEntityFrom(DamageSource.causePlayerDamage((EntityPlayer) player), value);
                            entityLivingBase.motionX += -Math.sin((double) (player.rotationYaw * (float) Math.PI / 180.0F)) * value / 20;
                            entityLivingBase.motionZ += Math.cos((double) (player.rotationYaw * (float) Math.PI / 180.0F)) * value / 20;
                        }
                    }
                }
            }
        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent
        public static void onRenderModel(RenderModelEvent e) {
            if (e.getEntity() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) e.getEntity();
                if (!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof ItemThorWeapon && player.getHeldItemMainhand().hasTagCompound() && player.getHeldItemMainhand().getTagCompound().getBoolean("Active") && !player.onGround) {
                    float rot = player.rotationPitch + 90;
                    GlStateManager.rotate(rot, 1, 0, 0);
                }
            }
        }

        @SideOnly(Side.CLIENT)
        @SubscribeEvent(receiveCanceled = true)
        public static void onSetupModelBiped(RenderModelEvent.SetRotationAngels e) {
            if (e.getEntity() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) e.getEntity();

                ItemStack heldItemMainHand = player.getHeldItemMainhand();

                if (!heldItemMainHand.isEmpty() && heldItemMainHand.getItem() instanceof ItemThorWeapon && heldItemMainHand.hasTagCompound()) {
                    boolean duringFlight = heldItemMainHand.getTagCompound().getBoolean("Active") && !player.onGround;
                    if (duringFlight) {
                        e.setCanceled(true);
                        ModelRenderer holdingArm = player.getPrimaryHand() == EnumHandSide.RIGHT ? e.model.bipedRightArm : e.model.bipedLeftArm;
                        ModelRenderer otherArm = player.getPrimaryHand() != EnumHandSide.RIGHT ? e.model.bipedRightArm : e.model.bipedLeftArm;
                        holdingArm.rotateAngleX = 10;

                        if (duringFlight) {
                            otherArm.rotateAngleX = 0;
                            e.model.bipedRightLeg.rotateAngleX = 0;
                            e.model.bipedLeftLeg.rotateAngleX = 0;
                            e.model.bipedHead.rotateAngleX = -1.5F;
                            e.model.bipedHeadwear.rotateAngleX = -1.5F;
                        }

                        if (e.model instanceof ModelPlayer) {
                            ModelPlayer model = (ModelPlayer) e.model;
                            ModelRenderer holdingArmWear = player.getPrimaryHand() == EnumHandSide.RIGHT ? model.bipedRightArmwear : model.bipedLeftArmwear;
                            ModelRenderer otherArmWear = player.getPrimaryHand() != EnumHandSide.RIGHT ? model.bipedRightArmwear : model.bipedLeftArmwear;
                            holdingArmWear.rotateAngleX = 10;
                            if (duringFlight) {
                                otherArmWear.rotateAngleX = 0;
                                model.bipedRightLegwear.rotateAngleX = 0;
                                model.bipedLeftLegwear.rotateAngleX = 0;
                            }
                        } else if (e.model instanceof ModelBipedSuitSet) {
                            ModelBipedSuitSet model = (ModelBipedSuitSet) e.model;
                            ModelRenderer holdingArmWear = player.getPrimaryHand() == EnumHandSide.RIGHT ? model.bipedRightArmwear : model.bipedLeftArmwear;
                            ModelRenderer otherArmWear = player.getPrimaryHand() != EnumHandSide.RIGHT ? model.bipedRightArmwear : model.bipedLeftArmwear;
                            holdingArmWear.rotateAngleX = 0;
                            if (duringFlight) {
                                otherArmWear.rotateAngleX = 0;
                                model.bipedRightLegwear.rotateAngleX = 0;
                                model.bipedLeftLegwear.rotateAngleX = 0;
                            }
                        }
                    }
                }
            }
        }
    }

}
