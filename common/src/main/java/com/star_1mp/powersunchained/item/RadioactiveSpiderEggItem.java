package com.star_1mp.powersunchained.item;

import com.star_1mp.powersunchained.entity.spider.RadioactiveSpider;
import com.star_1mp.powersunchained.registry.PUEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.gameevent.GameEvent;

/**
 * Radioactive Spider spawn egg. Not a vanilla SpawnEggItem: that one needs the entity type while items register,
 * before our entity types exist — this one only looks the type up when used.
 */
public class RadioactiveSpiderEggItem extends Item {

    public RadioactiveSpiderEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }
        BlockPos clicked = context.getClickedPos();
        BlockPos pos = level.getBlockState(clicked).getCollisionShape(level, clicked).isEmpty() ? clicked : clicked.relative(context.getClickedFace());
        RadioactiveSpider spider = PUEntities.RADIOACTIVE_SPIDER.get().create(level, EntitySpawnReason.SPAWN_ITEM_USE);
        if (spider == null) {
            return InteractionResult.FAIL;
        }
        spider.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        spider.setYRot(level.getRandom().nextFloat() * 360.0F);
        spider.yBodyRot = spider.getYRot();
        spider.yHeadRot = spider.getYRot();
        if (context.getItemInHand().has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
            spider.setCustomName(context.getItemInHand().getHoverName());
        }
        level.addFreshEntity(spider);
        level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, pos);
        context.getItemInHand().consume(1, context.getPlayer());
        return InteractionResult.SUCCESS;
    }
}
