package net.threetag.palladium.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

public class ThrusterModel {

    public final ModelPart thruster;

    public ThrusterModel(ModelPart root) {
        this.thruster = root.getChild("thruster");
    }

    public static Vector3f pulseScale(int phaseIndex, float ageInTicks, float propulsion) {
        float phase = 1.1F * phaseIndex;
        float f = (Mth.sin(ageInTicks * 3.0F + phase) + Mth.sin(ageInTicks * 7.3F + phase) * 0.4F) / 12.0F;
        return new Vector3f(1.0F + f * 0.5F, 1.0F + f + propulsion, 1.0F + f * 0.5F);
    }
}
