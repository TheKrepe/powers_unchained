package lucraft.mods.lucraftcore.core;

import lucraft.mods.lucraftcore.addonpacks.AddonPackHandler;
import lucraft.mods.lucraftcore.addonpacks.ModuleAddonPacks;
import lucraft.mods.lucraftcore.addonpacks.resourcepacks.AddonPackFolderResourcePack;
import lucraft.mods.lucraftcore.addonpacks.resourcepacks.AddonPackResourcePack;
import lucraft.mods.lucraftcore.util.events.RenderModelEvent;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.common.MinecraftForge;

import java.io.File;
import java.util.List;

public class LCClientHooks {

    public static void preRenderCallBack(RenderLivingBase<EntityLivingBase> renderer, EntityLivingBase entity) {
        if (entity == null)
            return;
        RenderModelEvent ev = new RenderModelEvent(entity, renderer);
        MinecraftForge.EVENT_BUS.post(ev);
    }

    public static void renderBiped(ModelBiped model, float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
        if (entity == null)
            return;
        RenderModelEvent.SetRotationAngels ev = new RenderModelEvent.SetRotationAngels(entity, model, f, f1, f2, f3, f4, f5, RenderModelEvent.ModelSetRotationAnglesEventType.PRE);
        MinecraftForge.EVENT_BUS.post(ev);
    }

    public static void insertAddonPackResourcePacks(List resourcePacks) {
        if (!ModuleAddonPacks.INSTANCE.isEnabled())
            return;

        if (!AddonPackHandler.getAddonPacksDir().exists()) {
            AddonPackHandler.getAddonPacksDir().mkdir();
        }

        for (File file : AddonPackHandler.getAddonPacksDir().listFiles()) {
            if (file.isDirectory()) {
                resourcePacks.add(new AddonPackFolderResourcePack(file));
            } else {
                resourcePacks.add(new AddonPackResourcePack(file));
            }
        }
    }

}
