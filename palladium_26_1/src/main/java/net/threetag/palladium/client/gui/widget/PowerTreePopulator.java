package net.threetag.palladium.client.gui.widget;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec2;
import net.threetag.palladium.addonpack.log.AddonPackLog;
import net.threetag.palladium.client.gui.widget.tree.PowerTree;
import net.threetag.palladium.client.gui.widget.tree.PowerTreeConnection;
import net.threetag.palladium.client.gui.widget.tree.PowerTreeConnectionPath;
import net.threetag.palladium.client.gui.widget.tree.PowerTreeConnectionStyle;
import net.threetag.palladium.client.gui.widget.tree.PowerTreeManager;
import net.threetag.palladium.client.gui.widget.tree.PowerTreeState;
import net.threetag.palladium.power.PowerInstance;
import net.threetag.palladium.power.ability.AbilityInstance;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

public class PowerTreePopulator {

    public static boolean canBeTree(@Nullable LivingEntity entity, @Nullable PowerInstance powerInstance) {
        if (entity == null || powerInstance == null) {
            return false;
        }

        if (PowerTreeManager.INSTANCE.get(powerInstance.getPowerId()) != null) {
            return true;
        }

        for (AbilityInstance<?> ability : powerInstance.getAbilities().values()) {
            if (ability.getAbility().getProperties().isHiddenInGUI()) {
                continue;
            }

            for (var reference : ability.getAbility().getStateManager().getUnlockingHandler().getParentAbilities()) {
                var parent = reference.getInstance(entity, powerInstance);

                if (parent != null && !parent.getAbility().getProperties().isHiddenInGUI()) {
                    return true;
                }
            }
        }

        return false;
    }

    public static void generateTree(PowerInstance powerInstance, List<PowerTreeWidget.AbilityElement> abilities,
                                    List<PowerTreeWidget.CustomElement> customElements, @Nullable PowerTree tree) {
        List<PowerTreeWidget.AbilityElement> root = new LinkedList<>();

        if (tree != null) {
            tree.nodes().forEach((key, node) -> {
                if (!powerInstance.getAbilities().containsKey(key)) {
                    if (node.hasDisplay()) {
                        var element = new PowerTreeWidget.CustomElement(key, node);
                        element.setStyle(tree.resolveNodeStyle(node.style()));
                        customElements.add(element);
                    } else {
                        AddonPackLog.warning("Power tree for {} has a node '{}' that is not an ability and has no title or icon",
                                powerInstance.getPowerId(), key);
                    }
                }
            });
        }

        // Create entry for each ability
        for (AbilityInstance<?> ability : powerInstance.getAbilities().values()) {
            if (!ability.getAbility().getProperties().isHiddenInGUI()) {
                var widget = new PowerTreeWidget.AbilityElement(ability).setGridPos(0, 0);
                abilities.add(widget);

                if (tree != null) {
                    var node = tree.nodes().get(ability.getAbility().getKey());

                    if (node != null) {
                        widget.setGridPosFixed(node.pos().x, node.pos().y);
                        widget.applyNode(node).setStyle(tree.resolveNodeStyle(node.style()));
                    } else {
                        widget.setStyle(tree.resolveNodeStyle(Optional.empty()));
                    }
                }
            }
        }

        // Find parents and children for each
        for (PowerTreeWidget.AbilityElement entry : abilities) {
            entry.parents.clear();
            entry.children.clear();
        }
        for (PowerTreeWidget.AbilityElement entry : abilities) {
            entry.updateRelatives(abilities);
        }

        // Locate and set first row
        int y = 0;
        for (PowerTreeWidget.AbilityElement entry : abilities) {
            if (entry.parents.isEmpty()) {
                if (!entry.fixedPosition) {
                    entry.updatePosition(0, getFreeGridYPos(abilities, 0, y));
                    y++;
                }
                root.add(entry);
            }
        }

        int longest = getLongestGridColumnLength(abilities);

        // Set position for children
        for (int j = 0; j < root.size(); j++) {
            for (PowerTreeWidget.AbilityElement entry : root) {
                for (PowerTreeWidget.AbilityElement child : entry.children) {
                    if (!child.fixedPosition && entry.gridX == child.gridX) {
                        child.setGridPos(child.gridX + 1, getFreeGridYPos(abilities, child.gridX + 1, entry.gridY));
                    }
                }
            }
        }

        // Last Adjustments
        for (int x = 0; x < 100; x++) {
            List<PowerTreeWidget.AbilityElement> entries = getEntriesAtX(abilities, x);
            for (int n = 0; n < entries.size(); n++) {
                PowerTreeWidget.AbilityElement entry = entries.get(n);
                if (!entry.fixedPosition) {
                    entry.setGridPos(entry.gridX, (longest / 2F) - (entries.size() / 2F) + n);
                }
            }
        }

    }

    public static List<PowerTreeConnection> generateConnections(PowerInstance powerInstance, List<PowerTreeWidget.AbilityElement> abilities,
                                                                 List<PowerTreeWidget.CustomElement> customElements,
                                                                 @Nullable PowerTree tree, Function<Vec2, int[]> toPixel) {
        List<PowerTreeConnection> connections = new ArrayList<>();
        List<PowerTree.Connection> manual = tree != null ? tree.connections() : List.of();

        if (tree == null || tree.autoConnect()) {
            var style = tree != null ? tree.resolveConnectionStyle(Optional.empty()) : PowerTreeConnectionStyle.Resolved.DEFAULT;

            for (PowerTreeWidget.AbilityElement child : abilities) {
                for (PowerTreeWidget.AbilityElement parent : child.parents) {
                    if (manual.stream().noneMatch(c -> c.connects(parent.getKey(), child.getKey()))) {
                        connections.add(new PowerTreeConnection(parent, child, style, style.getPath(), List.of()));
                    }
                }
            }
        }

        if (!manual.isEmpty()) {
            Map<String, PowerTreeWidget.Element> byKey = new HashMap<>();
            abilities.forEach(element -> byKey.put(element.getKey(), element));
            customElements.forEach(element -> byKey.put(element.getKey(), element));

            for (PowerTreeWidget.CustomElement element : customElements) {
                element.setState(getCustomState(element, manual, byKey, new HashSet<>()));
            }

            for (PowerTree.Connection connection : manual) {
                var from = byKey.get(connection.from());
                var to = byKey.get(connection.to());

                if (from == null || to == null) {
                    if (isUnknown(powerInstance, tree, connection.from()) || isUnknown(powerInstance, tree, connection.to())) {
                        AddonPackLog.warning("Power tree for {} has a connection with an unknown node: '{}' -> '{}'",
                                powerInstance.getPowerId(), connection.from(), connection.to());
                    }
                    continue;
                }

                var style = tree.resolveConnectionStyle(connection.style());
                connections.add(new PowerTreeConnection(from, to, style, connection.path().orElse(style.getPath()),
                        connection.via().stream().map(toPixel).toList()));
            }
        }

        Map<PowerTreeWidget.Element, Map<PowerTreeConnectionPath, List<PowerTreeConnection>>> docking = new HashMap<>();
        for (PowerTreeConnection connection : connections) {
            if (connection.canDock()) {
                docking.computeIfAbsent(connection.getTo(), e -> new EnumMap<>(PowerTreeConnectionPath.class))
                        .computeIfAbsent(connection.getPath(), p -> new ArrayList<>()).add(connection);
            }
        }

        docking.values().forEach(byPath -> byPath.values().forEach(incoming -> {
            int spacing = incoming.stream().mapToInt(PowerTreeConnection::getDockingSpacing).max().orElse(0);

            for (int i = 0; i < incoming.size(); i++) {
                incoming.get(i).setDockingOffset(i * spacing - ((incoming.size() - 1) * spacing) / 2);
            }
        }));

        for (PowerTreeConnection connection : connections) {
            connection.buildPoints();
        }

        return connections;
    }

    private static boolean isUnknown(PowerInstance powerInstance, PowerTree tree, String key) {
        return !powerInstance.getAbilities().containsKey(key) && !tree.nodes().containsKey(key);
    }

    private static PowerTreeState getCustomState(PowerTreeWidget.CustomElement element, List<PowerTree.Connection> connections,
                                                 Map<String, PowerTreeWidget.Element> byKey, Set<PowerTreeWidget.CustomElement> visiting) {
        if (!visiting.add(element)) {
            return PowerTreeState.LOCKED;
        }

        int incoming = 0;
        int unlocked = 0;

        for (PowerTree.Connection connection : connections) {
            if (connection.to().equals(element.getKey())) {
                var from = byKey.get(connection.from());

                if (from != null) {
                    incoming++;
                    var state = from instanceof PowerTreeWidget.CustomElement custom
                            ? getCustomState(custom, connections, byKey, visiting) : from.getState();

                    if (state == PowerTreeState.UNLOCKED) {
                        unlocked++;
                    }
                }
            }
        }

        visiting.remove(element);

        if (unlocked == incoming) {
            return PowerTreeState.UNLOCKED;
        }

        return unlocked > 0 ? PowerTreeState.AVAILABLE : PowerTreeState.LOCKED;
    }

    public static float getFreeGridYPos(List<PowerTreeWidget.AbilityElement> abilities, float x, float y) {
        for (int i = (int) y; i < 100; i++) {
            if (getEntryInGrid(abilities, x, i) == null) {
                return i;
            }
        }

        return 0;
    }

    public static PowerTreeWidget.AbilityElement getEntryInGrid(List<PowerTreeWidget.AbilityElement> abilities, float x,
            float y) {
        for (PowerTreeWidget.AbilityElement entry : abilities) {
            if (entry.gridX == x && entry.gridY == y) {
                return entry;
            }
        }

        return null;
    }

    public static int getLongestGridColumnLength(List<PowerTreeWidget.AbilityElement> abilities) {
        int l = 0;
        for (int i = 0; i < 100; i++) {
            int k = getEntriesAtX(abilities, i).size();
            l = Math.max(l, k);
        }
        return l;
    }

    public static List<PowerTreeWidget.AbilityElement> getEntriesAtX(List<PowerTreeWidget.AbilityElement> abilities,
            double x) {
        List<PowerTreeWidget.AbilityElement> list = new LinkedList<>();
        for (PowerTreeWidget.AbilityElement entry : abilities) {
            if (entry.gridX == x) {
                list.add(entry);
            }
        }

        return list;
    }

}
