package net.threetag.palladium.client.renderer.entity.skin;

import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;

public class PlayerSkinInfo {

    private PlayerSkin skin;
    private boolean failed;

    public PlayerSkinInfo() {
        this.skin = null;
    }

    public PlayerSkinInfo(PlayerSkin skin) {
        this.skin = skin;
    }

    public PlayerSkinInfo(Identifier texture, PlayerModelType modelType) {
        this.skin = PlayerSkin.insecure(new ClientAsset.ResourceTexture(texture, texture), null, null, modelType);
    }

    public boolean isLoading() {
        return this.skin == null;
    }

    public void set(PlayerSkin skin) {
        this.skin = skin;
    }

    public boolean hasFailed() {
        return this.failed;
    }

    public void setFailed() {
        this.failed = true;
    }

    public PlayerSkin getSkin() {
        return this.skin;
    }
}
