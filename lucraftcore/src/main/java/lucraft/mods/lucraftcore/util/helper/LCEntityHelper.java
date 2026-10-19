package lucraft.mods.lucraftcore.util.helper;

import lucraft.mods.lucraftcore.superpowers.ModuleSuperpowers;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityStrength;
import lucraft.mods.lucraftcore.superpowers.suitsets.SuitSet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class LCEntityHelper {
    public static boolean isInFrontOfEntity(Entity entity, Entity target) {
        Vec3d vec3d = target.getPositionVector();
        Vec3d vec3d1 = entity.getLook(1.0F);
        Vec3d vec3d2 = vec3d.subtractReverse(new Vec3d(entity.posX, entity.posY, entity.posZ)).normalize();
        vec3d2 = new Vec3d(vec3d2.x, 0.0D, vec3d2.z);

        return vec3d2.dotProduct(vec3d1) < 0.0D;
    }

    public static Entity getEntityByUUID(World world, UUID uuid) {
        for (Entity entity : world.loadedEntityList) {
            if (entity.getPersistentID().equals(uuid)) {
                return entity;
            }
        }

        return null;
    }

    public static Entity entityDropItem(EntityLivingBase entity, ItemStack stack, float offsetY, boolean removeFromInv) {
        if (stack.isEmpty()) {
            return null;
        } else {
            if (removeFromInv) {
                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                        if (player.inventory.getStackInSlot(i) == stack) {
                            player.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
                            break;
                        }
                    }
                } else {
                    for (EntityEquipmentSlot slots : EntityEquipmentSlot.values()) {
                        if (entity.getItemStackFromSlot(slots) == stack) {
                            entity.setItemStackToSlot(slots, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            }

            EntityItem entityitem = new EntityItem(entity.world, entity.posX, entity.posY + (double) offsetY, entity.posZ, stack);
            entityitem.setDefaultPickupDelay();

            if (stack.getItem().hasCustomEntity(stack)) {
                Entity entityNew = stack.getItem().createEntity(entity.world, entityitem, stack);
                entity.world.spawnEntity(entityNew);
                return entityNew;
            }

            entity.world.spawnEntity(entityitem);
            return entityitem;
        }
    }

    public static List<ItemStack> setSuitOfPlayer(EntityLivingBase entity, SuitSet suit) {
        List<ItemStack> list = new ArrayList<ItemStack>();
        if (suit.getHelmet() != null) {
            if (!entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty())
                list.add(entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD));
            entity.setItemStackToSlot(EntityEquipmentSlot.HEAD, new ItemStack(suit.getHelmet()));
        }
        if (suit.getChestplate() != null) {
            if (!entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty())
                list.add(entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST));
            entity.setItemStackToSlot(EntityEquipmentSlot.CHEST, new ItemStack(suit.getChestplate()));
        }
        if (suit.getLegs() != null) {
            if (!entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS).isEmpty())
                list.add(entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS));
            entity.setItemStackToSlot(EntityEquipmentSlot.LEGS, new ItemStack(suit.getLegs()));
        }
        if (suit.getBoots() != null) {
            if (!entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).isEmpty())
                list.add(entity.getItemStackFromSlot(EntityEquipmentSlot.FEET));
            entity.setItemStackToSlot(EntityEquipmentSlot.FEET, new ItemStack(suit.getBoots()));
        }

        return list;
    }

    public static ItemStack getItemStackInHandSide(EntityLivingBase entity, EnumHandSide side) {
        if (side == EnumHandSide.RIGHT) {
            return entity.getPrimaryHand() == EnumHandSide.RIGHT ? entity.getHeldItemMainhand() : entity.getHeldItemOffhand();
        } else {
            return entity.getPrimaryHand() == EnumHandSide.LEFT ? entity.getHeldItemMainhand() : entity.getHeldItemOffhand();
        }
    }

    public static boolean hasNoArmor(EntityLivingBase entity) {
        return entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD).isEmpty() && entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty() && entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS).isEmpty() && entity.getItemStackFromSlot(EntityEquipmentSlot.FEET).isEmpty();
    }

    public static double horizontalDistance(Entity entity1, Entity entity2) {
        float f = (float) (entity1.posX - entity2.posX);
        float f1 = (float) (entity1.posZ - entity2.posZ);
        return MathHelper.sqrt(f * f + f1 * f1);
    }

    public static double verticalDistance(Entity entity1, Entity entity2) {
        return entity2.posY - entity1.posY;
    }

    public static double getStrength(EntityLivingBase entity) {
        double d = 2D;

        if (ModuleSuperpowers.INSTANCE.isEnabled()) {
            List<AbilityStrength> list = Ability.getAbilitiesFromClass(Ability.getAbilities(entity), AbilityStrength.class);
            list.sort((a1, a2) -> a1.getOperation() > a2.getOperation() ? 1 : (a1.getOperation() < a2.getOperation() ? -1 : 0));
            double multiplicate = 0D;
            for (AbilityStrength strength : list) {
                if (strength.getOperation() == 0)
                    d += strength.getAmount();
                else if (strength.getOperation() == 1)
                    multiplicate += strength.getAmount();
            }
            d *= 1 + multiplicate;
            for (AbilityStrength strength : list) {
                if (strength.getOperation() == 2)
                    d *= 1 + strength.getAmount();
            }
        }

        return d;
    }

    public static boolean isStrongEnough(EntityLivingBase entity, double weight) {
        if ((entity instanceof EntityPlayer && ((EntityPlayer) entity).isCreative()) || !ModuleSuperpowers.INSTANCE.isEnabled())
            return true;
        else
            return getStrength(entity) >= weight;
    }

    public static boolean canDestroyBlock(World world, BlockPos pos, @Nullable EntityLivingBase entity) {
        IBlockState state = world.getBlockState(pos);

        if (state.getBlockHardness(world, pos) == -1F)
            return false;
        else if (entity == null)
            return false;
        if (entity instanceof EntityPlayer)
            return ((EntityPlayer) entity).capabilities.allowEdit && !MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(world, pos, world.getBlockState(pos), (EntityPlayer) entity));
        else
            return world.getGameRules().getBoolean("mobGriefing");
    }

}
