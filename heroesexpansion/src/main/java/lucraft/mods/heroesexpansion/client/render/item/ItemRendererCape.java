package lucraft.mods.heroesexpansion.client.render.item;

import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.items.HEItems;
import lucraft.mods.heroesexpansion.util.items.IColorableItem;
import lucraft.mods.lucraftcore.SupporterHandler;
import lucraft.mods.lucraftcore.extendedinventory.render.ExtendedInventoryItemRendererRegistry.IItemExtendedInventoryRenderer;
import lucraft.mods.lucraftcore.superpowers.abilities.Ability;
import lucraft.mods.lucraftcore.superpowers.abilities.AbilityFlight;
import lucraft.mods.lucraftcore.util.entity.FakePlayerClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.UUID;

public class ItemRendererCape implements IItemExtendedInventoryRenderer {

    public static ResourceLocation TEXTURE = new ResourceLocation(HeroesExpansion.MODID, "textures/models/cape.png");

    @Override
    public void render(EntityPlayer player, RenderLivingBase<?> render, ItemStack stack, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale, boolean isHead) {
        if (isHead)
            return;

        GlStateManager.pushMatrix();
        GL11.glDisable(GL11.GL_CULL_FACE);

        if (player.isSneaking()) {
            GlStateManager.translate(0, 0.2F, 0);
        }

        ((ModelBiped) render.getMainModel()).bipedBody.postRender(0.0625F);

        GlStateManager.translate(0, -0.02D, 0.15D + (!player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).isEmpty() ? 0.02D : 0D));
        boolean defaultTexture = false;
        if (stack.getItem() == HEItems.SUPPORTER_CLOAK && stack.hasTagCompound() && stack.getTagCompound().hasKey("Owner")) {
            UUID uuid = UUID.fromString(stack.getTagCompound().getString("Owner"));
            SupporterHandler.SupporterData data = SupporterHandler.getSupporterData(uuid);
            if (data != null && data.getNbt().hasKey("cloak") && !data.getNbt().getString("cloak").isEmpty()) {
                if (!data.getNbt().getBoolean("CloakLoaded")) {
                    loadTexture(uuid, data.getNbt().getString("cloak"));
                    data.getNbt().setBoolean("CloakLoaded", true);
                }
                Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(HeroesExpansion.MODID, "cloaks/" + uuid.toString()));
            } else
                defaultTexture = true;
        } else
            defaultTexture = true;

        if (defaultTexture)
            Minecraft.getMinecraft().renderEngine.bindTexture(TEXTURE);

        if (stack.getItem() instanceof IColorableItem) {
            String hex = Integer.toHexString(((IColorableItem) stack.getItem()).getColor(stack));
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            GlStateManager.color(r / 255F, g / 255F, b / 255F);
        }

        Tessellator tes = Tessellator.getInstance();
        BufferBuilder bb = tes.getBuffer();
        int parts = 200;

        bb.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);

        bb.pos(0.4D, 0, 0).tex(0, 8D / 32D).endVertex();
        bb.pos(-0.4D, 0, 0).tex(14D / 64D, 8D / 32D).endVertex();
        bb.pos(-0.4D, 0, -0.4D).tex(14D / 64D, 0D).endVertex();
        bb.pos(0.4D, 0, -0.4D).tex(0D, 0D).endVertex();

        tes.draw();

        bb.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);

        bb.pos(0.4D, 0.0001D, 0).tex(14D / 64D, 8D / 32D).endVertex();
        bb.pos(-0.4D, 0.0001D, 0).tex(28D / 64D, 8D / 32D).endVertex();
        bb.pos(-0.4D, 0.0001D, -0.4D).tex(28D / 64D, 0D).endVertex();
        bb.pos(0.4D, 0.0001D, -0.4D).tex(14D / 64D, 0D).endVertex();

        tes.draw();

        double d0 = player.prevChasingPosX + (player.chasingPosX - player.prevChasingPosX) * (double) partialTicks - (player.prevPosX + (player.posX - player.prevPosX) * (double) partialTicks);
        double d1 = player.prevChasingPosY + (player.chasingPosY - player.prevChasingPosY) * (double) partialTicks - (player.prevPosY + (player.posY - player.prevPosY) * (double) partialTicks);
        double d2 = player.prevChasingPosZ + (player.chasingPosZ - player.prevChasingPosZ) * (double) partialTicks - (player.prevPosZ + (player.posZ - player.prevPosZ) * (double) partialTicks);
        float f = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * partialTicks;
        double d3 = (double) MathHelper.sin(f * 0.017453292F);
        double d4 = (double) (-MathHelper.cos(f * 0.017453292F));
        float f1 = (float) d1 * 10.0F;
        f1 = MathHelper.clamp(f1, -6.0F, 32.0F);
        float f2 = (float) (d0 * d3 + d2 * d4) * 100.0F;
        float f3 = (float) (d0 * d4 - d2 * d3) * 100.0F;

        if (f2 < 0.0F) {
            f2 = 0.0F;
        }

        boolean isFlying = false;
        for (AbilityFlight ab : Ability.getAbilitiesFromClass(Ability.getAbilities(player), AbilityFlight.class)) {
            if (ab != null && ab.isUnlocked() && ab.isEnabled()) {
                isFlying = true;
                break;
            }
        }

        float f4 = player.prevCameraYaw + (player.cameraYaw - player.prevCameraYaw) * partialTicks;
        f1 = f1 + MathHelper.sin((player.prevDistanceWalkedModified + (player.distanceWalkedModified - player.prevDistanceWalkedModified) * partialTicks) * 6.0F) * 32.0F * f4;

        // if (player.isSneaking()) {
        // GlStateManager.translate(0, 0.2F, 0F);
        // f1 += 25.0F;
        // }

        float x = MathHelper.clamp((6.0F + f2 / 2.0F + f1) / 45F, 0, 1.2F);
        if (isFlying || player instanceof FakePlayerClient)
            x = 0.2F;
        GlStateManager.rotate((float) Math.toDegrees(x), 1, 0, 0);

        bb.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);

        bb.pos(0.4D, 0, 0).tex(0, 8D / 32D).endVertex();
        bb.pos(-0.4D, 0, 0).tex(14D / 64D, 8D / 32D).endVertex();
        bb.pos(-0.4D, 1.2D, 0).tex(14D / 64D, 1D).endVertex();
        bb.pos(0.4D, 1.2D, 0).tex(0, 1D).endVertex();

        tes.draw();

        bb.begin(GL11.GL_POLYGON, DefaultVertexFormats.POSITION_TEX);

        bb.pos(0.4D, 0, -0.0001D).tex(14D / 64D, 8D / 32D).endVertex();
        bb.pos(-0.4D, 0, -0.0001D).tex(28D / 64D, 8D / 32D).endVertex();
        bb.pos(-0.4D, 1.2D, -0.0001D).tex(28D / 64D, 1D).endVertex();
        bb.pos(0.4D, 1.2D, -0.0001D).tex(14D / 64D, 1D).endVertex();

        tes.draw();

        GL11.glEnable(GL11.GL_CULL_FACE);
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.popMatrix();
    }

    public void loadTexture(UUID owner, String url) {
        try {
            BufferedImage image = ImageIO.read(new URL(url));
            Minecraft.getMinecraft().getTextureManager().loadTexture(new ResourceLocation(HeroesExpansion.MODID, "cloaks/" + owner.toString()), new DynamicTexture(image));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
