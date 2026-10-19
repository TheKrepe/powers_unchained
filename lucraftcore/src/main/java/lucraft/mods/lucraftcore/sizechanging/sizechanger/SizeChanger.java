package lucraft.mods.lucraftcore.sizechanging.sizechanger;

import lucraft.mods.lucraftcore.LucraftCore;
import lucraft.mods.lucraftcore.sizechanging.capabilities.ISizeChanging;
import lucraft.mods.lucraftcore.sizechanging.entities.EntitySizeChanging;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = LucraftCore.MODID)
public abstract class SizeChanger extends IForgeRegistryEntry.Impl<SizeChanger> {

    public static IForgeRegistry<SizeChanger> SIZE_CHANGER_REGISTRY;
    public static SizeChanger DEFAULT_SIZE_CHANGER = new DefaultSizeChanger();
    public static final UUID ATTRIBUTE_UUID = UUID.fromString("8412f590-3b43-4526-ac59-e52a1969d0de");

    @SubscribeEvent
    public static void onRegisterNewRegistries(RegistryEvent.NewRegistry e) {
        SIZE_CHANGER_REGISTRY = new RegistryBuilder<SizeChanger>().setName(new ResourceLocation(LucraftCore.MODID, "size_changer")).setType(SizeChanger.class).setIDRange(0, 512).create();
    }

    @SubscribeEvent
    public static void onRegisterKarmaStats(RegistryEvent.Register<SizeChanger> e) {
        e.getRegistry().register(DEFAULT_SIZE_CHANGER.setRegistryName("default"));
    }

    public abstract int getSizeChangingTime(EntityLivingBase entity, ISizeChanging data, float estimatedSize);

    public abstract void onSizeChanged(EntityLivingBase entity, ISizeChanging data, float size);

    public abstract void onUpdate(EntityLivingBase entity, ISizeChanging data, float size);

    public abstract boolean start(EntityLivingBase entity, ISizeChanging data, float size, float estimatedSize);

    public abstract void end(EntityLivingBase entity, ISizeChanging data, float size);

    @SideOnly(Side.CLIENT)
    public void render(EntityLivingBase entitylivingbaseIn, RenderLivingBase<EntityLivingBase> renderer, List<EntitySizeChanging> entities, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
    }

    @SideOnly(Side.CLIENT)
    public void renderEntity(EntitySizeChanging entity, double x, double y, double z, float entityYaw, float partialTicks) {
    }
}
