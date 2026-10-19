package net.threetag.palladium.client.trail;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.threetag.palladium.client.util.ModelUtil;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.threetag.palladium.Palladium;
import net.threetag.palladium.client.renderer.entity.state.PalladiumRenderStateKeys;
import net.threetag.palladium.entity.data.PalladiumEntityData;
import net.threetag.palladium.entity.data.PalladiumEntityDataTypes;
import net.threetag.palladium.util.CollectionDelta;

import java.util.*;

@EventBusSubscriber(modid = Palladium.MOD_ID, value = Dist.CLIENT)
public class ClientEntityTrailHandler extends EntityTrailHandler {

    public static boolean COMPUTING_LIVE_ANCHOR = false;

    private final Map<Trail, TrailInstance> trailHandlerMap = new HashMap<>();

    @Override
    public void tick() {
        if (!this.getEntity().level().isClientSide()) {
            return;
        }

        new CollectionDelta<>(new ArrayList<>(this.trailHandlerMap.keySet()), Trail.getAvailableTrailsForEntity(this.getEntity()))
                .onAdd(t -> {
                    if (this.trailHandlerMap.containsKey(t)) {
                        this.trailHandlerMap.get(t).disabled = false;
                    } else {
                        this.trailHandlerMap.put(t, new TrailInstance(t));
                    }
                })
                .onRemove(t -> this.trailHandlerMap.getOrDefault(t, new TrailInstance(t)).disabled = true)
                .apply();

        this.trailHandlerMap.values().forEach(trailInstance -> trailInstance.tick(this.getEntity()));

        for (Map.Entry<Trail, TrailInstance> e : Set.copyOf(this.trailHandlerMap.entrySet())) {
            if (e.getValue().segments.isEmpty() && e.getValue().disabled) {
                this.trailHandlerMap.remove(e.getKey());
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @SubscribeEvent
    static void onRenderLiving(RenderLivingEvent.Post<LivingEntity, LivingEntityRenderState, EntityModel<LivingEntityRenderState>> e) {
        var renderState = e.getRenderState();

        for (TrailInstance instance : renderState.getRenderDataOrDefault(PalladiumRenderStateKeys.TRAILS, Collections.emptyMap()).values()) {
            for (TrailSegment segment : instance.segments) {
                e.getPoseStack().pushPose();
                var offset = segment.getPosition().subtract(renderState.x, renderState.y, renderState.z);
                e.getPoseStack().translate(offset);
                instance.trail.trailRenderer().submit(e.getPoseStack(), e.getSubmitNodeCollector(), e.getRenderState(), segment,
                        renderState.lightCoords, renderState.partialTick);
                e.getPoseStack().popPose();
            }
        }
    }

    public static ClientEntityTrailHandler get(LivingEntity entity) {
        return (ClientEntityTrailHandler) PalladiumEntityData.get(entity, PalladiumEntityDataTypes.TRAIL_HANDLER.get());
    }

    public Map<Trail, TrailInstance> getTrails() {
        return this.trailHandlerMap;
    }

    public static class TrailInstance {

        private final Trail trail;
        private final List<TrailSegment<?>> segments;
        private boolean disabled = false;
        @Nullable
        private Vec3 liveAnchor;

        public TrailInstance(Trail trail) {
            this.trail = trail;
            this.segments = new LinkedList<>();
        }

        public Trail getTrail() {
            return this.trail;
        }

        public void updateLiveAnchor(AbstractClientPlayer player, float partialTick) {
            this.liveAnchor = this.trail.anchor()
                    .map(part -> ModelUtil.getInWorldPosition(part, new Vector3f(this.trail.offset()), player, partialTick))
                    .orElse(null);
        }

        @Nullable
        public Vec3 getLiveAnchor() {
            return this.liveAnchor;
        }

        public List<TrailSegment<?>> getSegments() {
            return this.segments;
        }

        public void tick(Entity entity) {
            if (!this.disabled) {
                if (!this.segments.isEmpty()) {
                    var last = this.segments.getLast();
                    int tickDelay = this.trail.tickDelay().orElse(this.trail.trailRenderer().defaultTickDelay());
                    boolean shouldAdd = tickDelay > 0
                            ? last.getTickCount() >= tickDelay
                            : last.getPosition().distanceTo(entity.position()) >= entity.getBbWidth() * this.trail.spacing();

                    if (shouldAdd && (!this.trail.requiresMovement() || isMoving(entity))) {
                        this.addSegment(entity);
                    }
                } else if (!this.trail.requiresMovement() || isMoving(entity)) {
                    this.addSegment(entity);
                }
            }

            for (TrailSegment<?> segment : Set.copyOf(this.segments)) {
                if (segment.isObsolete()) {
                    this.segments.remove(segment);

                    if (segment.nextSegment != null) {
                        segment.nextSegment.previousSegment = null;
                    }
                } else {
                    segment.tick();
                }
            }
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private void addSegment(Entity entity) {
            var segment = new TrailSegment(entity, this.trail, this.trail.trailRenderer());
            var last = this.segments.isEmpty() ? null : this.segments.getLast();

            if (last != null) {
                last.nextSegment = segment;
                segment.previousSegment = last;
            }

            this.segments.add(segment);
        }
    }

    private static boolean isMoving(Entity entity) {
        if (entity instanceof LivingEntity) {
            return entity.xo != entity.getX() || entity.yo != entity.getY() || entity.zo != entity.getZ();
        } else {
            return entity.getDeltaMovement().length() != 0F;
        }
    }
}
