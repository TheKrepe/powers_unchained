package net.threetag.pymtech.client.icon;

import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.threetag.pymtech.PymTech;
import net.threetag.pymtech.ability.PymParticleSizeChangeAbility;
import net.threetag.threecore.capability.CapabilitySizeChanging;
import net.threetag.threecore.util.RenderUtil;
import net.threetag.threecore.util.icon.IIcon;
import net.threetag.threecore.util.icon.IIconSerializer;
import net.threetag.threecore.util.icon.IconSerializer;

public class PymParticleIcon implements IIcon {

    public static final ResourceLocation TEXTURE = new ResourceLocation(PymTech.MODID, "textures/gui/icons.png");
    private final float size;

    static {
        IconSerializer.register(Serializer.INSTANCE);
    }

    public PymParticleIcon(float size) {
        this.size = size;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void draw(Minecraft minecraft, MatrixStack matrixStack, int x, int y) {
        minecraft.getTextureManager().bindTexture(TEXTURE);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, 0);
        RenderSystem.scalef(0.5F, 0.5F, 1F);
        float changeToSize = RenderUtil.getCurrentAbilityInIconRendering() instanceof PymParticleSizeChangeAbility ? ((PymParticleSizeChangeAbility) RenderUtil.getCurrentAbilityInIconRendering()).getSize(minecraft.player) : 1F;
        float currentSize = minecraft.player != null && minecraft.player.getCapability(CapabilitySizeChanging.SIZE_CHANGING).isPresent() ? minecraft.player.getCapability(CapabilitySizeChanging.SIZE_CHANGING).orElse(null).getScale() : 1F;
        if (changeToSize < 1F)
            AbstractGui.blit(matrixStack, 0, 0, changeToSize <= currentSize ? 0 : 32, 0, 32, 32, 256, 256);
        else
            AbstractGui.blit(matrixStack, 0, 0, changeToSize < currentSize ? 0 : 32, 0, 32, 32, 256, 256);
        String s = "" + changeToSize;
        int length = minecraft.fontRenderer.getStringWidth(s);
        minecraft.fontRenderer.drawString(matrixStack, s, 16 - length / 2F, 25, 0xfefefe);
        RenderSystem.popMatrix();
    }

    @Override
    public int getWidth() {
        return 16;
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public IIconSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements IIconSerializer<PymParticleIcon> {

        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID = new ResourceLocation(PymTech.MODID, "pym_particles");

        @Override
        public PymParticleIcon read(JsonObject json) {
            return new PymParticleIcon(JSONUtils.getFloat(json, "size"));
        }

        @Override
        public PymParticleIcon read(CompoundNBT nbt) {
            return new PymParticleIcon(nbt.getFloat("Size"));
        }

        @Override
        public CompoundNBT serialize(PymParticleIcon icon) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putFloat("Size", icon.size);
            return nbt;
        }

        public JsonObject serializeJson(PymParticleIcon icon) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("size", icon.size);
            return jsonObject;
        }

        public ResourceLocation getId() {
            return ID;
        }
    }
}
