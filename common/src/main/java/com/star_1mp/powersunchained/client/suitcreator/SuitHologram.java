package com.star_1mp.powersunchained.client.suitcreator;

import com.star_1mp.powersunchained.registry.PUItems;
import com.star_1mp.powersunchained.suit.Suit;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Suit preview for the Suit Creator: a player-shaped render state (Steve or Alex skin, no entity behind it) wearing
 * the chosen suit pieces, drawn with the GUI entity renderer. Poses are applied from {@code PlayerModelMixin}
 * (the pose is encoded in the render state's id, since model submission is deferred).
 */
public final class SuitHologram {

    /** Render state ids {@code ID_BASE + pose}: no real entity has a negative id this low. */
    public static final int ID_BASE = Integer.MIN_VALUE + 4096;

    public enum Pose {
        STAND, RUN;

        public Pose next() {
            return values()[(this.ordinal() + 1) % values().length];
        }
    }

    private static final PlayerSkin STEVE = skin("entity/player/wide/steve", PlayerModelType.WIDE);
    private static final PlayerSkin ALEX = skin("entity/player/slim/alex", PlayerModelType.SLIM);
    private static final ArmorType[] PIECES = {ArmorType.HELMET, ArmorType.CHESTPLATE, ArmorType.LEGGINGS, ArmorType.BOOTS};

    private SuitHologram() {
    }

    private static PlayerSkin skin(String path, PlayerModelType model) {
        return new PlayerSkin(new ClientAsset.ResourceTexture(Identifier.withDefaultNamespace(path)), null, null, model, true);
    }

    /**
     * Draws the suit in the rectangle, feet at {@code y1 - 2}.
     *
     * @param parts   which pieces to show (mask, chestplate, pants, boots); {@code suit == null} = bare skin
     * @param yaw     turn in degrees (0 = facing the viewer)
     * @param scale   pixels per block
     */
    public static void draw(GuiGraphicsExtractor graphics, int x0, int y0, int x1, int y1, float scale, Suit suit,
            boolean[] parts, boolean slim, Pose pose, float yaw, float ageInTicks) {
        AvatarRenderState state = new AvatarRenderState();
        state.entityType = net.minecraft.world.entity.EntityTypes.PLAYER;
        state.skin = slim ? ALEX : STEVE;
        state.id = ID_BASE + pose.ordinal();
        state.ageInTicks = ageInTicks;
        state.bodyRot = 180.0F + yaw;
        state.yRot = 0.0F;
        state.xRot = 0.0F;
        state.boundingBoxWidth = 0.6F;
        state.boundingBoxHeight = 1.8F;
        state.eyeHeight = 1.62F;
        state.scale = 1.0F;
        state.showCape = false;
        if (pose == Pose.RUN) {
            state.walkAnimationPos = ageInTicks * 0.9F;
            state.walkAnimationSpeed = 1.0F;
        }
        if (suit != null) {
            ItemStack[] stacks = new ItemStack[4];
            for (int i = 0; i < 4; i++) {
                stacks[i] = parts[i] ? PUItems.piece(suit, PIECES[i]).get().getDefaultInstance() : ItemStack.EMPTY;
            }
            state.headEquipment = stacks[0];
            state.chestEquipment = stacks[1];
            state.legsEquipment = stacks[2];
            state.feetEquipment = stacks[3];
            state.showHat = !parts[0];
            state.showJacket = !parts[1];
            state.showLeftSleeve = !parts[1];
            state.showRightSleeve = !parts[1];
            state.showLeftPants = !parts[2] && !parts[3];
            state.showRightPants = !parts[2] && !parts[3];
        }
        // like InventoryScreen: flip Y; the origin (feet) is moved down from the rectangle's centre to y1 - 2
        Quaternionf rotation = new Quaternionf().rotateZ((float) Math.PI);
        float boxCenter = (y0 + y1) / 2.0F;
        float feet = y1 - 2.0F;
        Vector3f translation = new Vector3f(0.0F, (feet - boxCenter) / scale, 0.0F);
        graphics.entity(state, scale, translation, rotation, null, x0, y0, x1, y1);
    }

    /** {@code PlayerModelMixin}: poses for the hologram (after vanilla's pose). */
    public static void applyPose(PlayerModel model, AvatarRenderState state) {
        int pose = state.id - ID_BASE;
        if (pose < 0 || pose >= Pose.values().length) {
            return;
        }
        float t = state.ageInTicks;
        switch (Pose.values()[pose]) {
            case RUN -> {
                // arms pumping harder than vanilla's walk, head down a little
                model.head.xRot -= 0.12F;
                float s = Mth.cos(t * 0.9F * 0.6662F);
                model.rightArm.xRot = s * 1.3F;
                model.leftArm.xRot = -s * 1.3F;
                model.rightArm.zRot = 0.08F;
                model.leftArm.zRot = -0.08F;
            }
            default -> {
            }
        }
    }
}
