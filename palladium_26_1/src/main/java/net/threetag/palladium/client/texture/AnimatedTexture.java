package net.threetag.palladium.client.texture;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.AddressMode;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.textures.TextureFormat;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureContents;
import net.minecraft.client.renderer.texture.TickableTexture;
import net.minecraft.client.resources.metadata.animation.AnimationFrame;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.client.resources.metadata.animation.FrameSize;
import net.minecraft.client.resources.metadata.texture.TextureMetadataSection;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.ARGB;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class AnimatedTexture extends SimpleTexture implements TickableTexture {

    private int @Nullable [] frameSlots;
    private int @Nullable [] frameDurations;

    @Nullable
    private NativeImage source;
    @Nullable
    private NativeImage blendBuffer;

    private int frameWidth;
    private int frameHeight;
    private int columns;
    private boolean interpolate;

    private int step;
    private int elapsed;

    public AnimatedTexture(Identifier location) {
        super(location);
    }

    public boolean isAnimated() {
        return this.frameSlots != null;
    }

    @Override
    public TextureContents loadContents(ResourceManager resourceManager) throws IOException {
        this.discardSource();
        this.resetSchedule();

        Resource resource = resourceManager.getResourceOrThrow(this.resourceId());
        try (InputStream in = resource.open()) {
            this.source = NativeImage.read(in);
        }

        resource.metadata().getSection(AnimationMetadataSection.TYPE).ifPresent(this::prepare);

        return new TextureContents(this.source, resource.metadata().getSection(TextureMetadataSection.TYPE).orElse(null));
    }

    @Override
    public void apply(TextureContents contents) {
        AddressMode address = contents.clamp() ? AddressMode.CLAMP_TO_EDGE : AddressMode.REPEAT;
        FilterMode filter = contents.blur() ? FilterMode.LINEAR : FilterMode.NEAREST;
        this.sampler = RenderSystem.getSamplerCache().getSampler(address, address, filter, filter, false);
        this.upload();
    }

    @Override
    public void tick() {
        if (!this.isAnimated() || this.source == null) {
            return;
        }

        int current = this.frameSlots[this.step];
        this.elapsed++;

        if (this.elapsed >= this.frameDurations[this.step]) {
            this.elapsed = 0;
            this.step = (this.step + 1) % this.frameSlots.length;
            int next = this.frameSlots[this.step];

            if (next != current) {
                this.uploadSlot(next);
            }
        } else if (this.interpolate) {
            this.uploadBlend();
        }
    }

    @Override
    public void close() {
        super.close();
        this.discardSource();

        if (this.blendBuffer != null) {
            this.blendBuffer.close();
            this.blendBuffer = null;
        }
    }

    private void prepare(AnimationMetadataSection meta) {
        if (this.source == null) {
            return;
        }

        FrameSize size = meta.calculateFrameSize(this.source.getWidth(), this.source.getHeight());
        this.frameWidth = size.width();
        this.frameHeight = size.height();
        this.columns = Math.max(1, this.source.getWidth() / this.frameWidth);
        int rows = Math.max(1, this.source.getHeight() / this.frameHeight);
        int available = this.columns * rows;
        this.interpolate = meta.interpolatedFrames();

        List<Integer> slots = new ArrayList<>();
        List<Integer> durations = new ArrayList<>();

        if (meta.frames().isPresent()) {
            for (AnimationFrame frame : meta.frames().get()) {
                int slot = frame.index();
                int time = frame.timeOr(meta.defaultFrameTime());

                if (slot >= 0 && slot < available && time > 0) {
                    slots.add(slot);
                    durations.add(time);
                }
            }
        } else {
            for (int slot = 0; slot < available; slot++) {
                slots.add(slot);
                durations.add(meta.defaultFrameTime());
            }
        }

        if (slots.size() <= 1) {
            this.resetSchedule();
            return;
        }

        this.frameSlots = slots.stream().mapToInt(Integer::intValue).toArray();
        this.frameDurations = durations.stream().mapToInt(Integer::intValue).toArray();
    }

    private void upload() {
        if (this.source == null) {
            return;
        }

        GpuDevice device = RenderSystem.getDevice();
        super.close();

        int width = this.isAnimated() ? this.frameWidth : this.source.getWidth();
        int height = this.isAnimated() ? this.frameHeight : this.source.getHeight();

        this.texture = device.createTexture(this.resourceId()::toString, 5, TextureFormat.RGBA8, width, height, 1, 1);
        this.textureView = device.createTextureView(this.texture);

        if (this.isAnimated()) {
            this.step = 0;
            this.elapsed = 0;
            this.uploadSlot(this.frameSlots[0]);
        } else {
            device.createCommandEncoder().writeToTexture(this.texture, this.source);
        }
    }

    private void uploadSlot(int slot) {
        int sourceX = (slot % this.columns) * this.frameWidth;
        int sourceY = (slot / this.columns) * this.frameHeight;
        RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.getTexture(), this.source, 0, 0, 0, 0, this.frameWidth, this.frameHeight, sourceX, sourceY);
    }

    private void uploadBlend() {
        int current = this.frameSlots[this.step];
        int next = this.frameSlots[(this.step + 1) % this.frameSlots.length];

        if (current == next) {
            return;
        }

        if (this.blendBuffer == null) {
            this.blendBuffer = new NativeImage(this.frameWidth, this.frameHeight, false);
        }

        float fraction = this.elapsed / (float) this.frameDurations[this.step];
        int currentX = (current % this.columns) * this.frameWidth;
        int currentY = (current / this.columns) * this.frameHeight;
        int nextX = (next % this.columns) * this.frameWidth;
        int nextY = (next / this.columns) * this.frameHeight;

        for (int y = 0; y < this.frameHeight; y++) {
            for (int x = 0; x < this.frameWidth; x++) {
                int from = this.source.getPixel(currentX + x, currentY + y);
                int to = this.source.getPixel(nextX + x, nextY + y);
                this.blendBuffer.setPixel(x, y, ARGB.linearLerp(fraction, from, to));
            }
        }

        RenderSystem.getDevice().createCommandEncoder().writeToTexture(this.getTexture(), this.blendBuffer, 0, 0, 0, 0, this.frameWidth, this.frameHeight, 0, 0);
    }

    private void resetSchedule() {
        this.frameSlots = null;
        this.frameDurations = null;
        this.interpolate = false;
        this.step = 0;
        this.elapsed = 0;
    }

    private void discardSource() {
        if (this.source != null) {
            this.source.close();
            this.source = null;
        }
    }
}
