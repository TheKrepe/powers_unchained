package lucraft.mods.lucraftcore.tabs;

import lucraft.mods.lucraftcore.extendedinventory.ModuleExtendedInventory;
import lucraft.mods.lucraftcore.extendedinventory.gui.InventoryTabExtendedInventory;
import lucraft.mods.lucraftcore.karma.ModuleKarma;
import lucraft.mods.lucraftcore.karma.gui.InventoryTabKarma;
import lucraft.mods.lucraftcore.superpowers.ModuleSuperpowers;
import lucraft.mods.lucraftcore.superpowers.gui.InventoryTabSuitSetAbilities;
import lucraft.mods.lucraftcore.superpowers.gui.InventoryTabSuperpowerAbilities;
import micdoodle8.mods.galacticraft.api.client.tabs.AbstractTab;
import net.minecraft.client.Minecraft;

import java.util.HashMap;
import java.util.Map;

public abstract class GCTabWrapper extends AbstractTab {

    public final lucraft.mods.lucraftcore.tabs.AbstractTab lcTab;
    public static Map<Class<? extends lucraft.mods.lucraftcore.tabs.AbstractTab>, Class<? extends GCTabWrapper>> CLASSES = new HashMap<>();

    public GCTabWrapper(lucraft.mods.lucraftcore.tabs.AbstractTab lcTab) {
        super(lcTab.id, lcTab.x, lcTab.y, lcTab.renderStack);
        this.lcTab = lcTab;
        this.lcTab.wrapper = this;
        CLASSES.put(lcTab.getClass(), this.getClass());
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        this.lcTab.id = this.id;
        this.lcTab.x = this.x;
        this.lcTab.y = this.y;
        this.lcTab.enabled = this.enabled;
        this.lcTab.potionOffsetLast = this.potionOffsetLast;
        this.lcTab.drawButton(mc, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        return this.lcTab.mousePressed(mc, mouseX, mouseY);
    }

    @Override
    public void onTabClicked() {
        this.lcTab.onTabClicked();
    }

    @Override
    public boolean shouldAddToList() {
        return lcTab.shouldAddToList();
    }

    public static void init() {
        if (ModuleSuperpowers.INSTANCE.isEnabled()) {
            micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry.registerTab(new SuperpowerAbilities());
            micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry.registerTab(new SuitSetAbilities());
        }

        if (ModuleExtendedInventory.INSTANCE.isEnabled())
            micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry.registerTab(new ExtendedInventory());

        if (ModuleKarma.INSTANCE.isEnabled())
            micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry.registerTab(new Karma());
    }

    public static class SuperpowerAbilities extends GCTabWrapper {

        public SuperpowerAbilities() {
            super(new InventoryTabSuperpowerAbilities());
        }
    }

    public static class SuitSetAbilities extends GCTabWrapper {

        public SuitSetAbilities() {
            super(new InventoryTabSuitSetAbilities());
        }
    }

    public static class ExtendedInventory extends GCTabWrapper {

        public ExtendedInventory() {
            super(new InventoryTabExtendedInventory());
        }
    }

    public static class Karma extends GCTabWrapper {

        public Karma() {
            super(new InventoryTabKarma());
        }
    }
}
