package lucraft.mods.heroesexpansion.items;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.client.models.ModelArrow;
import lucraft.mods.heroesexpansion.entities.EntityHEArrow;
import lucraft.mods.heroesexpansion.entities.EntityHEArrow.*;
import lucraft.mods.heroesexpansion.util.items.IColorableItem;
import lucraft.mods.lucraftcore.util.helper.StringHelper;
import net.minecraft.block.BlockDispenser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.dispenser.BehaviorProjectileDispense;
import net.minecraft.dispenser.IPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.InvocationTargetException;

public class ItemHEArrow extends ItemArrow implements IColorableItem {

    public final ArrowType type;

    public ItemHEArrow(ArrowType type) {
        this.type = type;
        this.setCreativeTab(HeroesExpansion.CREATIVE_TAB);
        this.setTranslationKey(type.getName());
        this.setRegistryName(StringHelper.unlocalizedToResourceName(type.getName()));
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(this, new BehaviorProjectileDispense() {
            @Override
            protected IProjectile getProjectileEntity(World worldIn, IPosition position, ItemStack stackIn) {
                EntityArrow entityArrow = createArrow(worldIn, stackIn, null);
                entityArrow.setPosition(position.getX(), position.getY(), position.getZ());
                entityArrow.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
                return entityArrow;
            }
        });
    }

    @Override
    public EntityArrow createArrow(World worldIn, ItemStack stack, EntityLivingBase shooter) {
        try {
            if (shooter != null) {
                EntityHEArrow entity = type.getEntityClass().getConstructor(World.class, EntityLivingBase.class, int.class).newInstance(worldIn, shooter, this.getColor(stack));
                return entity;
            } else {
                EntityHEArrow entity = type.getEntityClass().getConstructor(World.class, double.class, double.class, double.class, int.class).newInstance(worldIn, 0D, 0D, 0D, this.getColor(stack));
                return entity;
            }
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            e.printStackTrace();
        }
        return super.createArrow(worldIn, stack, shooter);
    }

    @Override
    public boolean isInfinite(ItemStack stack, ItemStack bow, EntityPlayer player) {
        return false;
    }

    @Override
    public int getDefaultColor(ItemStack stack) {
        return 2521149;
    }

    @Override
    public void onUpdate(ItemStack stack, World worldIn, Entity entityIn, int itemSlot, boolean isSelected) {
        if (this == HEItems.KRYPTONITE_ARROW) {
            if (entityIn instanceof EntityPlayer) {
                ItemKryptonite.giveKryptonitePoison((EntityPlayer) entityIn, 10 * 20);
            }
        }
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
        if (this == HEItems.KRYPTONITE_ARROW) {
            int radius = 5;
            for (EntityPlayer player : entityItem.getEntityWorld().getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(entityItem.getPosition().add(radius, radius, radius), entityItem.getPosition().add(-radius, -radius, -radius))))
                ItemKryptonite.giveKryptonitePoison(player, 5 * 20);
        }
        return false;
    }

    public static enum ArrowType {

        SHARPENED("sharpened_arrow", EntitySharpenedArrow.class),
        EXPLOSIVE("explosive_arrow", EntityExplosiveArrow.class),
        SMOKE("smoke_arrow", EntitySmokeArrow.class),
        GAS("gas_arrow", EntityGasArrow.class),
        GRAPPLING_HOOK("grappling_hook_arrow", EntityGrapplingHookArrow.class),
        KRYPTONITE("kryptonite_arrow", EntityKryptoniteArrow.class),
        VIBRANIUM("vibranium_arrow", EntityVibraniumArrow.class);

        public static ResourceLocation ARROW_TEXTURE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/arrow.png");
        public static ResourceLocation ARROW_EXPLOSIVE_TEXTURE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/arrow_explosive.png");
        public static ResourceLocation ARROW_SMOKE_TEXTURE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/arrow_smoke.png");
        public static ResourceLocation ARROW_KRYPTONITE_TEXTURE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/arrow_kryptonite.png");
        public static ResourceLocation ARROW_VIBRANIUM_TEXTURE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/arrow_vibranium.png");
        public static ResourceLocation ARROW_OVERLAY_TEXTURE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/arrow_overlay.png");
        public static ResourceLocation ARROW_KRYPTONITE_OVERLAY_TEXTURE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/arrow_kryptonite_overlay.png");

        private String name;
        private Class<? extends EntityHEArrow> entityClass;

        private ArrowType(String name, Class<? extends EntityHEArrow> entityClass) {
            this.name = name;
            this.entityClass = entityClass;
        }

        public String getName() {
            return name;
        }

        public Class<? extends EntityHEArrow> getEntityClass() {
            return entityClass;
        }

        @SideOnly(Side.CLIENT)
        public void renderArrow(EntityArrow entity, boolean quiver, boolean bow, int color, Minecraft mc) {
            GlStateManager.pushMatrix();
            float scale = 0.2F;
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.color(1, 1, 1, 1);
            if (bow) {
                GlStateManager.rotate(-90, 1, 0, 0);
                GlStateManager.rotate(90, 0, 1, 0);
                GlStateManager.translate(-0.1F, 2.42F, 0.04F);
            }
            if (entity != null) {
                GlStateManager.rotate(90, 0, 0, 1);
                GlStateManager.translate(0, 2.2F, 0);
            }

            ModelArrow ARROW_MODEL = new ModelArrow();
            ARROW_MODEL.shape44.showModel = this != SHARPENED && this != KRYPTONITE;
            ARROW_MODEL.shape44_1.showModel = ARROW_MODEL.shape44.showModel;
            mc.renderEngine.bindTexture(this == EXPLOSIVE ? ARROW_EXPLOSIVE_TEXTURE : (this == SMOKE ? ARROW_SMOKE_TEXTURE : (this == KRYPTONITE ? ARROW_KRYPTONITE_TEXTURE : (this == VIBRANIUM ? ARROW_VIBRANIUM_TEXTURE : ARROW_TEXTURE))));
            ARROW_MODEL.renderModel(0.0625F);

            String hex = Integer.toHexString(color);
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            GlStateManager.color(r / 255F, g / 255F, b / 255F);
            mc.renderEngine.bindTexture((this == ArrowType.KRYPTONITE || this == ArrowType.VIBRANIUM) ? ARROW_KRYPTONITE_OVERLAY_TEXTURE : ARROW_OVERLAY_TEXTURE);
            ARROW_MODEL.renderModel(0.0625F);

            GlStateManager.popMatrix();
        }

    }

}
