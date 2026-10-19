package lucraft.mods.heroesexpansion.entities;

import lucraft.mods.heroesexpansion.superpowers.HESuperpowers;
import lucraft.mods.lucraftcore.materials.potions.MaterialsPotions;
import lucraft.mods.lucraftcore.superpowers.SuperpowerHandler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.monster.EntitySpider;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.World;
import net.minecraft.world.storage.loot.LootTableList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Random;

public class EntityRadioactiveSpider extends EntitySpider {

    public EntityRadioactiveSpider(World worldIn) {
        super(worldIn);
        this.setSize(0.175F, 0.2F);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(12.0D);
    }

    @Override
    public boolean attackEntityAsMob(Entity entityIn) {
        if (super.attackEntityAsMob(entityIn)) {
            if (entityIn instanceof EntityLivingBase) {
                PotionEffect radiation = new PotionEffect(MaterialsPotions.RADIATION, 20);
                radiation.setCurativeItems(new ArrayList<ItemStack>());
                if (new Random().nextInt(10) == 0 && entityIn instanceof EntityPlayer) {
                    SuperpowerHandler.giveSuperpower((EntityPlayer) entityIn, HESuperpowers.SPIDER_POWERS);
                }
                ((EntityLivingBase) entityIn).addPotionEffect(radiation);
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    @Nullable
    public IEntityLivingData onInitialSpawn(DifficultyInstance difficulty, @Nullable IEntityLivingData livingdata) {
        return livingdata;
    }

    @Override
    public float getEyeHeight() {
        return 0.45F * 0.25F;
    }

    @Override
    @Nullable
    protected ResourceLocation getLootTable() {
        return LootTableList.ENTITIES_CAVE_SPIDER;
    }

}
