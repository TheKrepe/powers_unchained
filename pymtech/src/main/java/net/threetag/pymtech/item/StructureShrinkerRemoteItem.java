package net.threetag.pymtech.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.tileentity.StructureShrinkerTileEntity;

import javax.annotation.Nullable;
import java.util.List;

public class StructureShrinkerRemoteItem extends Item {

    public StructureShrinkerRemoteItem(Properties properties) {
        super(properties);
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void addInformation(ItemStack stack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag advanced) {
        if (stack.getOrCreateTag().contains("TileEntityPos")) {
            BlockPos pos = NBTUtil.readBlockPos(stack.getOrCreateTag().getCompound("TileEntityPos"));
            tooltip.add(new TranslationTextComponent("item.pymtech.structure_shrinker_remote.tooltip.device", pos.getX(), pos.getY(), pos.getZ()).mergeStyle(TextFormatting.GRAY));
        } else
            tooltip.add(new TranslationTextComponent("item.pymtech.structure_shrinker_remote.tooltip.no_device").mergeStyle(TextFormatting.GRAY));
    }

    public static StructureShrinkerTileEntity getConnectedTileEntity(World world, ItemStack stack) {
        if (stack.getOrCreateTag().contains("TileEntityPos")) {
            BlockPos pos = NBTUtil.readBlockPos(stack.getOrCreateTag().getCompound("TileEntityPos"));
            TileEntity tileEntity = world.getTileEntity(pos);
            return tileEntity instanceof StructureShrinkerTileEntity ? (StructureShrinkerTileEntity) tileEntity : null;
        }
        return null;
    }

    @Mod.EventBusSubscriber(modid = PymTech.MODID)
    public static class EventHandler {

        @SubscribeEvent
        public static void onLeftClickBlock(PlayerInteractEvent.RightClickBlock e) {
            if (e.getItemStack().getItem() instanceof StructureShrinkerRemoteItem && !e.getWorld().isRemote) {
                TileEntity tileEntity = e.getWorld().getTileEntity(e.getPos());

                if (tileEntity instanceof StructureShrinkerTileEntity) {
                    e.getItemStack().getOrCreateTag().put("TileEntityPos", NBTUtil.writeBlockPos(e.getPos()));
                    e.getPlayer().sendStatusMessage(new TranslationTextComponent("item.pymtech.structure_shrinker_remote.connected"), true);
                    e.setUseBlock(Event.Result.DENY);
                    e.setCanceled(true);
                }
            }
        }

        @SubscribeEvent
        public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock e) {
            if (e.getItemStack().getItem() instanceof StructureShrinkerRemoteItem && !e.getWorld().isRemote) {
                StructureShrinkerTileEntity tileEntity = getConnectedTileEntity(e.getWorld(), e.getItemStack());
                e.setUseBlock(Event.Result.DENY);
                e.setCanceled(true);
                if (tileEntity == null) {
                    e.getPlayer().sendStatusMessage(new TranslationTextComponent("item.pymtech.structure_shrinker_remote.no_device").mergeStyle(TextFormatting.RED), true);
                    return;
                }
                if (e.getPlayer().isCrouching())
                    tileEntity.setSelectionEnd(e.getPos());
                else
                    tileEntity.setSelectionStart(e.getPos());
                e.getPlayer().sendStatusMessage(new TranslationTextComponent("item.pymtech.structure_shrinker_remote.set_corner" + (e.getEntity().isCrouching() ? 2 : 1), e.getPos().getX(), e.getPos().getY(), e.getPos().getZ()), true);

            }
        }

    }

}
