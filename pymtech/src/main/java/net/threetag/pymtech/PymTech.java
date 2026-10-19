package net.threetag.pymtech;

import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.threetag.pymtech.ability.PTAbilityTypes;
import net.threetag.pymtech.block.PTBlocks;
import net.threetag.pymtech.client.gui.RegulatorTier1Screen;
import net.threetag.pymtech.client.gui.RegulatorTier2Screen;
import net.threetag.pymtech.client.gui.RegulatorTier3Screen;
import net.threetag.pymtech.client.gui.StructureShrinkerScreen;
import net.threetag.pymtech.client.renderer.entity.model.WaspWingsModel;
import net.threetag.pymtech.client.renderer.entity.modellayer.OpenSkullModelLayer;
import net.threetag.pymtech.client.renderer.model.ShrunkenStructureBakedModel;
import net.threetag.pymtech.client.renderer.tileentity.StructureShrinkerTileEntityRenderer;
import net.threetag.pymtech.config.PTServerConfig;
import net.threetag.pymtech.container.PTContainerTypes;
import net.threetag.pymtech.container.PymParticleVialSlot;
import net.threetag.pymtech.container.RegulatorSlot;
import net.threetag.pymtech.data.*;
import net.threetag.pymtech.entity.attributes.PTAttributes;
import net.threetag.pymtech.item.PTItems;
import net.threetag.pymtech.network.*;
import net.threetag.pymtech.sound.PTSoundEvents;
import net.threetag.threecore.client.renderer.entity.model.ModelRegistry;
import net.threetag.threecore.client.renderer.entity.modellayer.ModelLayerManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Mod(PymTech.MODID)
public class PymTech {

    public static final String MODID = "pymtech";
    private static final Logger LOGGER = LogManager.getLogger();
    public static SimpleChannel NETWORK_CHANNEL;
    private static int networkId = -1;

    public PymTech() {
        FMLJavaModLoadingContext.get().getModEventBus().register(this);
        MinecraftForge.EVENT_BUS.register(this);

        // Config
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, PTServerConfig.generateConfig());

        // Registries
        PTBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        PTBlocks.TILE_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        PTItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        PTAbilityTypes.ABILITY_TYPES.register(FMLJavaModLoadingContext.get().getModEventBus());
        PTSoundEvents.SOUND_EVENTS.register(FMLJavaModLoadingContext.get().getModEventBus());
        PTAttributes.ATTRIBUTES.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent e) {
        // Packets
        registerMessage(SyncShrunkenStructureMessage.class, SyncShrunkenStructureMessage::toBytes, SyncShrunkenStructureMessage::new, SyncShrunkenStructureMessage::handle);
        registerMessage(RequestShrunkenStructureMessage.class, RequestShrunkenStructureMessage::toBytes, RequestShrunkenStructureMessage::new, RequestShrunkenStructureMessage::handle);
        registerMessage(StructureShrinkActivateMessage.class, StructureShrinkActivateMessage::toBytes, StructureShrinkActivateMessage::new, StructureShrinkActivateMessage::handle);
        registerMessage(NotifyShrunkenStructureUpdate.class, NotifyShrunkenStructureUpdate::toBytes, NotifyShrunkenStructureUpdate::new, NotifyShrunkenStructureUpdate::handle);
        registerMessage(SetRegulatedSizeMessage.class, SetRegulatedSizeMessage::toBytes, SetRegulatedSizeMessage::new, SetRegulatedSizeMessage::handle);

        //Attributes
        e.enqueueWork(PTAttributes::initAttributes);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void setupClient(final FMLClientSetupEvent e) {
        // Screens
        ScreenManager.registerFactory(PTContainerTypes.STRUCTURE_SHRINKER, StructureShrinkerScreen::new);
        ScreenManager.registerFactory(PTContainerTypes.REGULATOR_TIER1, RegulatorTier1Screen::new);
        ScreenManager.registerFactory(PTContainerTypes.REGULATOR_TIER2, RegulatorTier2Screen::new);
        ScreenManager.registerFactory(PTContainerTypes.REGULATOR_TIER3, RegulatorTier3Screen::new);

        // TESR
        ClientRegistry.bindTileEntityRenderer(PTBlocks.STRUCTURE_SHRINKER_TILE_ENTITY.get(), StructureShrinkerTileEntityRenderer::new);

        // Models
        ModelRegistry.registerModel("pymtech:wasp_wings", new WaspWingsModel(0F));

        // Model Layer
        ModelLayerManager.registerModelLayer(new ResourceLocation(PymTech.MODID, "open_skull"), OpenSkullModelLayer::parse);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void textureStichPre(final TextureStitchEvent.Pre e) {
        e.addSprite(PymParticleVialSlot.OVERLAY_TEXTURE);
        e.addSprite(RegulatorSlot.OVERLAY_TEXTURE);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onModelRegistry(ModelRegistryEvent e) {
        ModelLoader.addSpecialModel(new ResourceLocation(PymTech.MODID, "item/shrunken_structure_gui"));
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onModelBake(ModelBakeEvent e) {
        IBakedModel defaultModel = e.getModelRegistry().get(new ModelResourceLocation(new ResourceLocation(PymTech.MODID, "shrunken_structure"), "inventory"));
        IBakedModel placeholderModel = e.getModelRegistry().get(new ResourceLocation(PymTech.MODID, "item/shrunken_structure_gui"));
        e.getModelRegistry().put(new ModelResourceLocation(new ResourceLocation(PymTech.MODID, "shrunken_structure"), "inventory"), new ShrunkenStructureBakedModel(defaultModel, placeholderModel));
    }

    @SubscribeEvent
    public void gatherData(GatherDataEvent e) {
        e.getGenerator().addProvider(new PTRecipeProvider(e.getGenerator()));
        e.getGenerator().addProvider(new PTItemTagsProvider(e.getGenerator(), new PTBlockTagProvider(e.getGenerator())));
        e.getGenerator().addProvider(new PTLangProvider.English(e.getGenerator()));
        e.getGenerator().addProvider(new PTLangProvider.German(e.getGenerator()));
        e.getGenerator().addProvider(new PTLootTableProvider(e.getGenerator()));
    }

    public static <MSG> int registerMessage(Class<MSG> messageType, BiConsumer<MSG, PacketBuffer> encoder, Function<PacketBuffer, MSG> decoder, BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        if (NETWORK_CHANNEL == null) {
            NETWORK_CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, MODID), () -> "1.0", (s) -> true, (s) -> true);
        }

        int id = networkId++;
        NETWORK_CHANNEL.registerMessage(id, messageType, encoder, decoder, messageConsumer);
        return id;
    }
}
