package lucraft.mods.lucraftcore.util.helper;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class LCMathHelper {

    public static double round(double value, int places) {
        if (places < 0)
            throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static Vec3d rotateZ(Vec3d vec, float degree) {
        float f = MathHelper.cos(degree);
        float f1 = MathHelper.sin(degree);
        double d0 = vec.x * (double) f + vec.y * (double) f1;
        double d1 = vec.y * (double) f - vec.x * (double) f1;
        double d2 = vec.z;
        return new Vec3d(d0, d1, d2);
    }

}
