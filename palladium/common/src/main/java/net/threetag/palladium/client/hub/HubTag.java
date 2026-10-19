package net.threetag.palladium.client.hub;

import com.google.gson.JsonObject;
import net.minecraft.util.FastColor;
import net.minecraft.util.GsonHelper;

public record HubTag(int id, String name, int color) {

    public static HubTag fromJson(JsonObject json) {
        return new HubTag(
                GsonHelper.getAsInt(json, "id", 0),
                GsonHelper.getAsString(json, "name", ""),
                parseColor(GsonHelper.getAsString(json, "color", "#808080"))
        );
    }

    public static int parseColor(String hex) {
        try {
            String value = hex.startsWith("#") ? hex.substring(1) : hex;

            if (value.length() == 6) {
                int rgb = Integer.parseInt(value, 16);
                return FastColor.ARGB32.color(255, (rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
            } else if (value.length() == 8) {
                long rgba = Long.parseLong(value, 16);
                int r = (int) ((rgba >> 24) & 0xFF);
                int g = (int) ((rgba >> 16) & 0xFF);
                int b = (int) ((rgba >> 8) & 0xFF);
                int a = (int) (rgba & 0xFF);
                return FastColor.ARGB32.color(a, r, g, b);
            }
        } catch (NumberFormatException ignored) {
        }

        return FastColor.ARGB32.color(255, 0x80, 0x80, 0x80);
    }
}
