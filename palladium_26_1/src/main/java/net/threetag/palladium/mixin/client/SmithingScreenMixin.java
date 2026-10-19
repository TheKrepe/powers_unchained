package net.threetag.palladium.mixin.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.SmithingScreen;
import net.minecraft.client.renderer.entity.state.ArmorStandRenderState;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.renderer.entity.state.PalladiumRenderStateKeys;
import net.threetag.palladium.client.renderer.entity.state.SuitStandRenderState;
import net.threetag.palladium.client.util.RenderUtil;
import net.threetag.palladium.entity.PalladiumEntityTypes;
import net.threetag.palladium.entity.SuitStand;
import net.threetag.palladium.item.ItemPropertiesCodec;
import net.threetag.palladium.item.PalladiumItemExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(SmithingScreen.class)
public abstract class SmithingScreenMixin {

    @Shadow
    @Final
    @Mutable
    private ArmorStandRenderState armorStandPreview;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void palladium$replacePreviewState(SmithingMenu menu, Inventory inventory, Component title, CallbackInfo ci) {
        var preview = new SuitStandRenderState();
        preview.showBasePlate = this.armorStandPreview.showBasePlate;
        preview.showArms = this.armorStandPreview.showArms;
        preview.xRot = this.armorStandPreview.xRot;
        preview.bodyRot = this.armorStandPreview.bodyRot;
        preview.color = DyeColor.WHITE;
        this.armorStandPreview = preview;
        this.palladium$setSuitStand(false);
    }

    @Inject(method = "updateArmorStandPreview", at = @At("TAIL"))
    private void palladium$updatePreview(ItemStack stack, CallbackInfo ci) {
        this.palladium$setSuitStand(palladium$isPalladiumItem(stack));
        this.armorStandPreview.setRenderData(PalladiumRenderStateKeys.RENDER_LAYERS, null);

        var equippable = stack.get(DataComponents.EQUIPPABLE);

        if (equippable != null) {
            var registryAccess = Objects.requireNonNull(Minecraft.getInstance().player).registryAccess();
            RenderUtil.addItemRenderLayersInHumanoidRenderStateSlot(registryAccess, this.armorStandPreview, equippable.slot(), stack);
        }
    }

    @Unique
    private void palladium$setSuitStand(boolean suitStand) {
        this.armorStandPreview.entityType = suitStand ? PalladiumEntityTypes.SUIT_STAND.get() : EntityType.ARMOR_STAND;
        this.armorStandPreview.leftArmPose = suitStand ? SuitStand.DEFAULT_LEFT_ARM_POSE : ArmorStand.DEFAULT_LEFT_ARM_POSE;
        this.armorStandPreview.rightArmPose = suitStand ? SuitStand.DEFAULT_RIGHT_ARM_POSE : ArmorStand.DEFAULT_RIGHT_ARM_POSE;
        this.armorStandPreview.leftLegPose = suitStand ? SuitStand.DEFAULT_LEFT_LEG_POSE : ArmorStand.DEFAULT_LEFT_LEG_POSE;
        this.armorStandPreview.rightLegPose = suitStand ? SuitStand.DEFAULT_RIGHT_LEG_POSE : ArmorStand.DEFAULT_RIGHT_LEG_POSE;
    }

    @Unique
    private static boolean palladium$isPalladiumItem(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).getNamespace().equals(Palladium.MOD_ID)
                || ((PalladiumItemExtension) stack.getItem()).palladium$getProperties() instanceof ItemPropertiesCodec.ExtendedProperties;
    }
}
