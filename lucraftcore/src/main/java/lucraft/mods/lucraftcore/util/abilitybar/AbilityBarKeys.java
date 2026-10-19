package lucraft.mods.lucraftcore.util.abilitybar;

import lucraft.mods.lucraftcore.LCConfig;
import lucraft.mods.lucraftcore.LucraftCore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;

public class AbilityBarKeys {

    public static ArrayList<KeyBindingAbilityBar> KEYS = new ArrayList<KeyBindingAbilityBar>();
    public static KeyBindingAbilityBar KEY_1 = new KeyBindingAbilityBar("lucraftcore.abilitybar.1", KeyConflictContext.IN_GAME, Keyboard.KEY_X, LucraftCore.NAME);
    public static KeyBindingAbilityBar KEY_2 = new KeyBindingAbilityBar("lucraftcore.abilitybar.2", KeyConflictContext.IN_GAME, Keyboard.KEY_C, LucraftCore.NAME);
    public static KeyBindingAbilityBar KEY_3 = new KeyBindingAbilityBar("lucraftcore.abilitybar.3", KeyConflictContext.IN_GAME, Keyboard.KEY_V, LucraftCore.NAME);
    public static KeyBindingAbilityBar KEY_4 = new KeyBindingAbilityBar("lucraftcore.abilitybar.4", KeyConflictContext.IN_GAME, Keyboard.KEY_B, LucraftCore.NAME);
    public static KeyBindingAbilityBar KEY_5 = new KeyBindingAbilityBar("lucraftcore.abilitybar.5", KeyConflictContext.IN_GAME, Keyboard.KEY_N, LucraftCore.NAME);

    public static KeyBinding UP = new KeyBinding("lucraftcore.abilitybar.up", KeyConflictContext.IN_GAME, Keyboard.KEY_NONE, LucraftCore.NAME);
    public static KeyBinding DOWN = new KeyBinding("lucraftcore.abilitybar.down", KeyConflictContext.IN_GAME, Keyboard.KEY_NONE, LucraftCore.NAME);

    public AbilityBarKeys() {
        MinecraftForge.EVENT_BUS.register(this);

        ClientRegistry.registerKeyBinding(UP);
        ClientRegistry.registerKeyBinding(DOWN);

        KEYS.add(KEY_1);
        ClientRegistry.registerKeyBinding(KEY_1);
        KEYS.add(KEY_2);
        ClientRegistry.registerKeyBinding(KEY_2);
        KEYS.add(KEY_3);
        ClientRegistry.registerKeyBinding(KEY_3);
        KEYS.add(KEY_4);
        ClientRegistry.registerKeyBinding(KEY_4);
        KEYS.add(KEY_5);
        ClientRegistry.registerKeyBinding(KEY_5);
    }

    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent evt) {
        boolean pressed = false;
        for (KeyBindingAbilityBar key : KEYS) {
            if (key.isKeyDown() != key.isPressed) {
                key.isPressed = !key.isPressed;
                IAbilityBarEntry entry = AbilityBarHandler.getEntryFromKey(KEYS.indexOf(key));

                if (entry != null) {
                    if (key.isPressed) {
                        entry.onButtonPress();
                        pressed = true;
                    } else
                        entry.onButtonRelease();
                }
            }
        }

        if (!pressed) {
            if (UP.isKeyDown()) {
                AbilityBarHandler.scroll(true);
            } else if (DOWN.isKeyDown()) {
                AbilityBarHandler.scroll(false);
            }
        }
    }

    @SubscribeEvent
    public void onMouse(MouseEvent e) {
        if (LCConfig.abilityBarScrolling && Minecraft.getMinecraft().player.isSneaking() && e.getDwheel() != 0F) {
            AbilityBarHandler.scroll(e.getDwheel() > 0);
            e.setCanceled(true);
        }
    }

    public static class KeyBindingAbilityBar extends KeyBinding {

        public boolean isPressed;

        public KeyBindingAbilityBar(String description, IKeyConflictContext keyConflictContext, int keyCode, String category) {
            super(description, keyConflictContext, keyCode, category);
        }

        @Override
        public String getDisplayName() {
            return super.getDisplayName();
        }

    }

}
