package net.threetag.palladium.item.tab;

import net.mcexpanded.fancytabsections.FancyTabSections;
import net.mcexpanded.fancytabsections.Section.SectionAnimatedTextured;
import net.mcexpanded.fancytabsections.Section.SectionColored;
import net.mcexpanded.fancytabsections.Section.SectionTextured;
import net.minecraft.resources.Identifier;

public class SectionSerializers {

    public static void init() {
        SectionSerializer.register(Identifier.fromNamespaceAndPath(FancyTabSections.MOD_ID, "colored"), SectionColored.class, new ColoredSectionSerializer());
        SectionSerializer.register(Identifier.fromNamespaceAndPath(FancyTabSections.MOD_ID, "textured"), SectionTextured.class, new TexturedSectionSerializer());
        SectionSerializer.register(Identifier.fromNamespaceAndPath(FancyTabSections.MOD_ID, "animated_textured"), SectionAnimatedTextured.class, new AnimatedTexturedSectionSerializer());
    }

}
