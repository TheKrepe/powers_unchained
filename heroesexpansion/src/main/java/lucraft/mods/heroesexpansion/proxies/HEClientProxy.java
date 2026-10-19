package lucraft.mods.heroesexpansion.proxies;

import lucraft.mods.heroesexpansion.HEConfig;
import lucraft.mods.heroesexpansion.HeroesExpansion;
import lucraft.mods.heroesexpansion.client.models.TintedOBJLoader;
import lucraft.mods.heroesexpansion.client.particles.ParticleKineticEnergy;
import lucraft.mods.heroesexpansion.client.render.item.ItemRendererWebShooter;
import lucraft.mods.heroesexpansion.client.render.layer.LayerRendererSpiderHam;
import lucraft.mods.heroesexpansion.client.render.tileentity.TESRPortalDevice;
import lucraft.mods.heroesexpansion.entities.HEEntities;
import lucraft.mods.heroesexpansion.items.HEItems;
import lucraft.mods.heroesexpansion.tileentities.TileEntityPortalDevice;
import lucraft.mods.heroesexpansion.util.items.IColorableItem;
import lucraft.mods.lucraftcore.extendedinventory.IItemExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.render.ExtendedInventoryItemRendererRegistry;
import lucraft.mods.lucraftcore.util.updatechecker.UpdateChecker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityPig;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class HEClientProxy extends HECommonProxy {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        super.preInit(event);
        HEEntities.loadRenderers();

        // Tinted OBJ Loader
        TintedOBJLoader.INSTANCE.addDomain(HeroesExpansion.MODID);
        ModelLoaderRegistry.registerLoader(TintedOBJLoader.INSTANCE);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        super.init(event);

        // Item Colors
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IColorableItem.IColorableItemColor(), HEItems.QUIVER, HEItems.CAPE, HEItems.SHARPENED_ARROW, HEItems.EXPLOSIVE_ARROW, HEItems.SMOKE_ARROW, HEItems.GAS_ARROW, HEItems.GAS_ARROW, HEItems.GRAPPLING_HOOK_ARROW, HEItems.KRYPTONITE_ARROW, HEItems.VIBRANIUM_ARROW, HEItems.BILLY_CLUB, HEItems.BILLY_CLUB_SEPARATE);
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler((stack, i) -> {
            int primary = stack.hasTagCompound() && stack.getTagCompound().hasKey("display") && stack.getSubCompound("display").hasKey("PrimaryColor") ? stack.getSubCompound("display").getInteger("PrimaryColor") : 0x3d41ff;
            int secondary = stack.hasTagCompound() && stack.getTagCompound().hasKey("display") && stack.getSubCompound("display").hasKey("SecondaryColor") ? stack.getSubCompound("display").getInteger("SecondaryColor") : 0xff1a1a;
            return i == 0 ? primary : (i == 1 ? secondary : -1);
        }, HEItems.CAPTAIN_AMERICA_SHIELD);

        // Particles
        ParticleManager pm = Minecraft.getMinecraft().effectRenderer;
        pm.registerParticle(ParticleKineticEnergy.ID, new ParticleKineticEnergy.Factory());

        // UpdateChecker
        if (HEConfig.UPDATE_CHECKER)
            new UpdateChecker(HeroesExpansion.VERSION, TextFormatting.BLACK + "[" + TextFormatting.DARK_GRAY + "HeroesExpansion" + TextFormatting.BLACK + "]", "https://www.curseforge.com/minecraft/mc-mods/heroesexpansion", "https://drive.google.com/uc?export=download&id=11s_pKVKz7elxO6HqQBpwZ8KUw-FpnElr");

        // TESR
        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityPortalDevice.class, new TESRPortalDevice());

        // Spider-Ham Layer
        Render<Entity> renderer = Minecraft.getMinecraft().getRenderManager().getEntityClassRenderObject(EntityPig.class);
        if (renderer instanceof RenderLivingBase)
            ((RenderLivingBase<?>) renderer).addLayer(new LayerRendererSpiderHam((RenderLivingBase<?>) renderer));

        // EI Item Renderers
        ExtendedInventoryItemRendererRegistry.registerRenderer((IItemExtendedInventory) HEItems.WEB_SHOOTER_1, new ItemRendererWebShooter(new ResourceLocation(HeroesExpansion.MODID, "textures/models/web_shooter_1.png"), new ResourceLocation(HeroesExpansion.MODID, "textures/models/web_shooter_1_smallarms.png")));
        ExtendedInventoryItemRendererRegistry.registerRenderer((IItemExtendedInventory) HEItems.WEB_SHOOTER_2, new ItemRendererWebShooter(new ResourceLocation(HeroesExpansion.MODID, "textures/models/web_shooter_2.png"), new ResourceLocation(HeroesExpansion.MODID, "textures/models/web_shooter_2_smallarms.png")));
    }

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        super.postInit(event);
    }

    @Override
    public EntityPlayer getPlayerEntity(MessageContext ctx) {
        return (ctx.side.isClient() ? Minecraft.getMinecraft().player : super.getPlayerEntity(ctx));
    }

    @Override
    public IThreadListener getThreadFromContext(MessageContext ctx) {
        return (ctx.side.isClient() ? Minecraft.getMinecraft() : super.getThreadFromContext(ctx));
    }

}
