package lucraft.mods.lucraftcore.infinity;

import net.minecraft.util.text.TextFormatting;

public enum EnumInfinityStone {

    SPACE(TextFormatting.AQUA),
    SOUL(TextFormatting.GOLD),
    REALITY(TextFormatting.RED),
    TIME(TextFormatting.GREEN),
    POWER(TextFormatting.LIGHT_PURPLE),
    MIND(TextFormatting.YELLOW);

    protected TextFormatting textColor;

    private EnumInfinityStone(TextFormatting textColor) {
        this.textColor = textColor;
    }

    public TextFormatting getTextColor() {
        return textColor;
    }
}