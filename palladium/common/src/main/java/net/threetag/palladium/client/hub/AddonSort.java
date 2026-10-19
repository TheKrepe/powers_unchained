package net.threetag.palladium.client.hub;

import net.minecraft.network.chat.Component;

public enum AddonSort {

    DOWNLOADS("downloads", "gui.palladium.addon_hub.sort.downloads"),
    LATEST("latest", "gui.palladium.addon_hub.sort.latest"),
    NAME("name", "gui.palladium.addon_hub.sort.name");

    private final String query;
    private final String translationKey;

    AddonSort(String query, String translationKey) {
        this.query = query;
        this.translationKey = translationKey;
    }

    public String getQuery() {
        return this.query;
    }

    public String getTranslationKey() {
        return this.translationKey;
    }

    public String getDescriptionKey() {
        return this.translationKey + ".desc";
    }

    public Component getLabel() {
        return Component.translatable(this.translationKey);
    }

    public Component getTooltip() {
        return Component.translatable(this.getDescriptionKey());
    }
}
