package lucraft.mods.lucraftcore.util.abilitybar;

public enum EnumAbilityBarColor {

    WHITE(0, 36),
    ORANGE(22, 36),
    MAGENTA(44, 36),
    LIGHT_BLUE(66, 36),
    YELLOW(88, 36),
    LIME(110, 36),
    PINK(132, 36),
    GRAY(154, 36),
    LIGHT_GRAY(176, 36),
    CYAN(198, 36),
    PURPLE(220, 36),
    BLUE(0, 58),
    BROWN(22, 58),
    GREEN(44, 58),
    RED(66, 58),
    BLACK(88, 58);

    protected int x;
    protected int y;

    EnumAbilityBarColor(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public static EnumAbilityBarColor fromName(String name) {
        for(EnumAbilityBarColor color : values()) {
            if(color.toString().equalsIgnoreCase(name)) {
                return color;
            }
        }
        return null;
    }

}
