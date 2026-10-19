package net.threetag.palladium.mixin.client;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.ReloadableTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.neoforged.fml.ModList;
import net.threetag.palladium.client.texture.AnimatedTexture;
import net.threetag.palladium.client.texture.transformer.TransformedTexture;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mixin(TextureManager.class)
public abstract class TextureManagerMixin {

    @Shadow
    @Final
    public Map<Identifier, AbstractTexture> byPath;

    @Shadow
    public abstract void release(Identifier id);

    @Shadow
    protected abstract TextureContents loadContentsSafe(Identifier textureId, ReloadableTexture texture);

    @Shadow
    public abstract void register(Identifier path, AbstractTexture texture);

    @Inject(method = "reload", at = @At("HEAD"))
    private void reload(PreparableReloadListener.SharedState sharedState, Executor executor, PreparableReloadListener.PreparationBarrier barrier, Executor executor1, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        List<Identifier> toRemove = new ArrayList<>();
        for (Map.Entry<Identifier, AbstractTexture> e : this.byPath.entrySet()) {
            if (e.getValue() instanceof TransformedTexture) {
                toRemove.add(e.getKey());
            }
        }

        for (Identifier id : toRemove) {
            this.release(id);
        }
    }

    @Inject(method = "getTexture(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/texture/AbstractTexture;", at = @At("HEAD"), cancellable = true)
    private void palladium$guardNullTexture(Identifier location, CallbackInfoReturnable<AbstractTexture> cir) {
        if (location == null) {
            cir.setReturnValue(this.byPath.get(MissingTextureAtlasSprite.getLocation()));
        }
    }

    @WrapOperation(
            method = "getTexture(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/texture/AbstractTexture;",
            at = @At(value = "NEW", target = "(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/texture/SimpleTexture;"),
            require = 0
    )
    private SimpleTexture palladium$animateIfNeeded(Identifier location, Operation<SimpleTexture> original) {
        if (palladium$geckoLibPresent()) {
            return original.call(location);
        }

        AnimatedTexture animated = new AnimatedTexture(location);
        TextureContents contents = this.loadContentsSafe(location, animated);

        if (animated.isAnimated()) {
            animated.apply(contents);
            this.register(location, animated);
            return animated;
        }

        animated.close();
        return original.call(location);
    }

    @WrapWithCondition(
            method = "getTexture(Lnet/minecraft/resources/Identifier;)Lnet/minecraft/client/renderer/texture/AbstractTexture;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/texture/TextureManager;registerAndLoad(Lnet/minecraft/resources/Identifier;Lnet/minecraft/client/renderer/texture/ReloadableTexture;)V"),
            require = 0
    )
    private boolean palladium$skipDoubleRegister(TextureManager self, Identifier id, ReloadableTexture texture) {
        return !(texture instanceof AnimatedTexture);
    }

    @Unique
    private static boolean palladium$geckoLibPresent() {
        ModList modList = ModList.get();
        return modList != null && modList.isLoaded("geckolib");
    }

}
