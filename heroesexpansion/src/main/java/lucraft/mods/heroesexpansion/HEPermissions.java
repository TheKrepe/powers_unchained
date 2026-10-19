package lucraft.mods.heroesexpansion;

import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

public class HEPermissions {

    public static String SPACE_STONE_PORTAL = "heroesexpansion.space_stone.portal";
    public static String SPACE_STONE_DRAGGING = "heroesexpansion.space_stone.dragging";

    public static void registerPermissions() {
        PermissionAPI.registerNode(SPACE_STONE_PORTAL, DefaultPermissionLevel.ALL, "Allows players to open portals with the Space Stone");
        PermissionAPI.registerNode(SPACE_STONE_DRAGGING, DefaultPermissionLevel.ALL, "Allows players to drag blocks and entities");
    }

}
