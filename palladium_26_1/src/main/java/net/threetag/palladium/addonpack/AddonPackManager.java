package net.threetag.palladium.addonpack;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.*;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.Unit;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.BlockTypes;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.addonpack.log.AddonPackLog;
import net.threetag.palladium.addonpack.particle.ParticleTypeLoader;
import net.threetag.palladium.block.BlockPropertiesCodec;
import net.threetag.palladium.item.tab.CreativeModeTabCodec;
import net.threetag.palladium.item.ItemTypes;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiFunction;

@EventBusSubscriber(modid = Palladium.MOD_ID)
public class AddonPackManager {

    public static final PackFormat PACK_FORMAT = new PackFormat(2, 0);
    public static final MetadataSectionType<PackMetadataSection> ADDON_TYPE = new MetadataSectionType<>("addonpack", Objects.requireNonNull(PackMetadataSection.codecForPackType(AddonPackManager.getPackType())));
    public static final MetadataSectionType<PackMetadataSection> OPTIONAL_ADDON_FORMAT = new MetadataSectionType<>("addonpack", Objects.requireNonNull(metadataCodecForPackType(AddonPackManager.getPackType())));

    private static AddonPackManager INSTANCE;
    public static PackType PACK_TYPE;
    public static final PackSource PACK_SOURCE = PackSource.create(PackSource.decorateWithSource("pack.source.addonpack"), true);
    public static final String FOLDER = "palladium/addonpacks";
    private static final List<PreparableReloadListener> BASIC_LOADERS = new ArrayList<>();
    private static final List<LoaderEntry<?>> REGISTRY_LOADERS = new ArrayList<>();

    private final ReloadableResourceManager resourceManager;
    private final RepositorySource folderPackFinder;
    private final PackRepository packRepository;
    private static CompletableFuture<AddonPackManager> loaderFuture;
    private QueueableExecutor mainThreadExecutor;

    public static AddonPackManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AddonPackManager();
        }
        return INSTANCE;
    }

    static {
        registerLoader(DataAttachmentLoader.INSTANCE);
        registerLoader(new ToolMaterialLoader());
        registerLoader(new ArmorMaterialLoader());
        registerRegistryLoader(Registries.CREATIVE_MODE_TAB, (callback, provider) -> new AddonObjectLoader<>(provider, CreativeModeTabCodec.CODEC, Registries.CREATIVE_MODE_TAB, callback));
        registerRegistryLoader(Registries.BLOCK, (callback, provider) -> new AddonObjectLoader<>(provider, BlockTypes.CODEC.codec(), Registries.BLOCK, callback));
        registerRegistryLoader(Registries.ITEM, (callback, provider) -> new AddonObjectLoader<>(provider, ItemTypes.CODEC, Registries.ITEM, callback));
        registerRegistryLoader(Registries.ITEM, (registerCallback, _) -> new SuitSetLoader(registerCallback));
        registerRegistryLoader(Registries.PARTICLE_TYPE, (callback, _) -> new ParticleTypeLoader(callback));
    }

    private AddonPackManager() {
        this.resourceManager = new ReloadableResourceManager(getPackType());
        this.folderPackFinder = new FolderRepositorySource(getLocation(), getPackType(), PACK_SOURCE, LevelStorageSource.parseValidator(FMLPaths.GAMEDIR.get().resolve("allowed_symlinks.txt")));
        ;
        this.packRepository = new PackRepository(this.folderPackFinder);
        ResourcePackLoader.populatePackRepository(this.packRepository, AddonPackManager.getPackType(), false);

        // Replace block properties codec because MC's one is not done yet
        BlockPropertiesCodec.replaceBlockPropertiesCodec();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onRegister(RegisterEvent e) {
        if (e.getRegistryKey() == Registries.ATTRIBUTE) {
            AddonPackManager.initiateBasicLoaders();
        }

        AddonPackManager.initiateFor(e.getRegistryKey(), (registry, id, object) -> e.register(registry, id, () -> object));
    }

    @SubscribeEvent
    static void packFinder(AddPackFindersEvent e) {
        if (e.getPackType() != AddonPackManager.getPackType()) {
            e.addRepositorySource(AddonPackManager.getInstance().getWrappedPackFinder(e.getPackType()));
        }
    }

    public static <T> void registerLoader(PreparableReloadListener loader) {
        BASIC_LOADERS.add(loader);
    }

    public static <T> void registerRegistryLoader(ResourceKey<Registry<T>> registry, BiFunction<RegisterCallback<T>, HolderLookup.Provider, SimpleJsonResourceReloadListener<?>> registerCallback) {
        REGISTRY_LOADERS.add(new LoaderEntry<>(registry, registerCallback));
    }

    public static void initiateBasicLoaders() {
        startLoading(BASIC_LOADERS);
        waitForLoading();
    }

    public static <T> void initiateFor(ResourceKey<?> registry, RegisterCallback<T> callback) {
        for (LoaderEntry<?> loader : REGISTRY_LOADERS) {
            if (loader.registry.equals(registry)) {
                startLoading(Collections.singletonList(loader.get(callback, VanillaRegistries.createLookup())));
                waitForLoading();
            }
        }
    }

    private static void startLoading(List<PreparableReloadListener> parser) {
        loaderFuture = getInstance().beginLoading(Util.backgroundExecutor(), parser);
    }

    private static void waitForLoading() {
        getInstance().waitForLoading(loaderFuture);
        loaderFuture = null;
    }

    public static Path getLocation(String path) {
        Path folder = FMLPaths.GAMEDIR.get().resolve(path);
        var file = folder.toFile();
        if (!file.exists() && !file.mkdirs())
            throw new RuntimeException("Could not create addonpacks directory! Please create the directory yourself, or make sure the name is not taken by a file and you have permission to create directories.");
        return folder;
    }

    public static Path getLocation() {
        return getLocation(FOLDER);
    }

    public static PackType getPackType() {
        if (PACK_TYPE == null) {
            for (PackType packType : PackType.values()) {
                if (packType.getDirectory().equalsIgnoreCase("addon")) {
                    PACK_TYPE = packType;
                }
            }
        }
        return PACK_TYPE;
    }

    public RepositorySource getWrappedPackFinder(PackType packType) {
        return (infoConsumer) -> this.folderPackFinder.loadPacks(pack -> {
            pack.location = new PackLocationInfo("addonpack:" + pack.getId(), pack.location.title(), pack.location.source(), pack.location.knownPackInfo());
            pack.metadata = new Pack.Metadata(
                    pack.metadata.description(),
                    pack.metadata.compatibility(),
                    pack.metadata.requestedFeatures(),
                    pack.metadata.overlays(),
                    pack.metadata.isHidden()
            );
            pack.selectionConfig = new PackSelectionConfig(true, pack.selectionConfig.defaultPosition(), pack.selectionConfig.fixedPosition());
            infoConsumer.accept(pack);
        });
    }

    public CompletableFuture<AddonPackManager> beginLoading(Executor backgroundExecutor, List<PreparableReloadListener> parser) {
        this.resourceManager.listeners.clear();
        for (PreparableReloadListener listener : parser) {
            this.resourceManager.registerReloadListener(listener);
        }
        this.packRepository.reload();
        // Enable all packs
        this.packRepository.setSelected(this.packRepository.getAvailableIds());

        this.mainThreadExecutor = new QueueableExecutor();

        return this.resourceManager
                .createReload(backgroundExecutor, mainThreadExecutor, CompletableFuture.completedFuture(Unit.INSTANCE), this.packRepository.openAllSelected())
                .done().whenComplete((unit, throwable) -> {
                    if (throwable != null) {
                        this.resourceManager.close();
                        AddonPackLog.error(throwable.getMessage());
                    }
                })
                .thenRun(mainThreadExecutor::finish)
                .thenRun(() -> {

                })
                .thenApply((unit) -> this);
    }

    private void finish() {
        // finish
    }

    public void waitForLoading(CompletableFuture<AddonPackManager> loaderFuture) {
        try {
            while (!loaderFuture.isDone()) {
                mainThreadExecutor.runQueue();
                mainThreadExecutor.waitForTasks();
            }

            mainThreadExecutor.runQueue();

            loaderFuture.get().finish();
        } catch (InterruptedException e) {
            Palladium.LOGGER.error("Addonpack loader future interrupted!");
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw new ReportedException(CrashReport.forThrowable(cause, "Error loading addonpacks"));
        }
    }

    public static class QueueableExecutor implements Executor {

        private final Thread thread = Thread.currentThread();
        private final ConcurrentLinkedQueue<Runnable> queue = new ConcurrentLinkedQueue<>();
        private final Semaphore sem = new Semaphore(1);

        public boolean isSameThread() {
            return Thread.currentThread() == thread;
        }

        @Override
        public void execute(@NotNull Runnable command) {
            if (!this.isSameThread()) {
                queue.add(command);
                sem.release();
            } else {
                command.run();
            }
        }

        public void runQueue() {
            if (!isSameThread()) {
                throw new IllegalStateException("This method must be called in the main thread.");
            }

            while (!queue.isEmpty()) {
                var run = queue.poll();
                if (run != null) run.run();
            }
        }

        public void finish() {
            sem.release();
        }

        public void waitForTasks() throws InterruptedException {
            sem.acquire();
        }
    }

    @FunctionalInterface
    public interface RegisterCallback<T> {

        void register(ResourceKey<Registry<T>> registry, Identifier id, T object);

    }

    private record LoaderEntry<T>(ResourceKey<Registry<T>> registry,
                                  BiFunction<RegisterCallback<T>, HolderLookup.Provider, SimpleJsonResourceReloadListener<?>> callback) {

        @SuppressWarnings("unchecked")
        public SimpleJsonResourceReloadListener<?> get(RegisterCallback<?> callback, HolderLookup.Provider provider) {
            return this.callback.apply((RegisterCallback<T>) callback, provider);
        }

    }

    private static final InclusiveRange<PackFormat> UNLIMITED_SUPPORT = new InclusiveRange<>(new PackFormat(0, 0), new PackFormat(Integer.MAX_VALUE, Integer.MAX_VALUE));

    private static Codec<PackMetadataSection> metadataCodecForPackType(PackType type) {
        int lastPreMinor = PackFormat.lastPreMinorVersion(type);
        MapCodec<InclusiveRange<PackFormat>> formatCodec = PackFormat.IntermediaryFormat.PACK_CODEC.flatXmap(
                intermediary -> {
                    if (intermediary.min().isEmpty() && intermediary.max().isEmpty() && intermediary.format().isEmpty() && intermediary.supported().isEmpty()) {
                        return DataResult.success(UNLIMITED_SUPPORT);
                    }
                    return intermediary.validate(lastPreMinor, true, false, "Pack", "supported_formats");
                },
                range -> {
                    if (range.equals(UNLIMITED_SUPPORT)) {
                        return DataResult.success(new PackFormat.IntermediaryFormat(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()));
                    }
                    return DataResult.success(PackFormat.IntermediaryFormat.fromRange(range, lastPreMinor));
                });
        return RecordCodecBuilder.create(
                in -> in.group(
                                ComponentSerialization.CODEC.optionalFieldOf("description", Component.empty()).forGetter(PackMetadataSection::description),
                                formatCodec.forGetter(PackMetadataSection::supportedFormats))
                        .apply(in, PackMetadataSection::new));
    }

}
