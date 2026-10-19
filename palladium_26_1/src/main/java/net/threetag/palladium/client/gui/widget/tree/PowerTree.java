package net.threetag.palladium.client.gui.widget.tree;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.phys.Vec2;
import net.threetag.palladium.client.texture.TextureReference;
import net.threetag.palladium.icon.Icon;
import net.threetag.palladium.util.PalladiumCodecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public record PowerTree(int gridSize,
                        boolean autoConnect,
                        Map<String, PowerTreeNodeStyle> nodeStyles,
                        Map<String, PowerTreeConnectionStyle> connectionStyles,
                        Map<String, Node> nodes,
                        List<Connection> connections,
                        List<Region> regions) {

    public static final String DEFAULT_STYLE = "default";
    public static final int DEFAULT_GRID_SIZE = 50;

    public static final Codec<PowerTree> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.POSITIVE_INT.optionalFieldOf("grid_size", DEFAULT_GRID_SIZE).forGetter(PowerTree::gridSize),
            Codec.BOOL.optionalFieldOf("auto_connect", true).forGetter(PowerTree::autoConnect),
            Codec.unboundedMap(Codec.STRING, PowerTreeNodeStyle.CODEC).optionalFieldOf("node_styles", Map.of()).forGetter(PowerTree::nodeStyles),
            Codec.unboundedMap(Codec.STRING, PowerTreeConnectionStyle.CODEC).optionalFieldOf("connection_styles", Map.of()).forGetter(PowerTree::connectionStyles),
            Codec.unboundedMap(Codec.STRING, Node.CODEC).optionalFieldOf("nodes", Map.of()).forGetter(PowerTree::nodes),
            Connection.CODEC.listOf().optionalFieldOf("connections", List.of()).forGetter(PowerTree::connections),
            Region.CODEC.listOf().optionalFieldOf("regions", List.of()).forGetter(PowerTree::regions)
    ).apply(instance, PowerTree::new));

    public PowerTreeNodeStyle.Resolved resolveNodeStyle(Optional<Either<String, PowerTreeNodeStyle>> style) {
        List<PowerTreeNodeStyle> chain = new ArrayList<>();
        style.flatMap(s -> s.map(name -> Optional.ofNullable(this.nodeStyles.get(name)), Optional::of)).ifPresent(chain::add);
        Optional.ofNullable(this.nodeStyles.get(DEFAULT_STYLE)).ifPresent(chain::add);
        chain.add(PowerTreeNodeStyle.DEFAULT);
        return new PowerTreeNodeStyle.Resolved(chain);
    }

    public PowerTreeConnectionStyle.Resolved resolveConnectionStyle(Optional<Either<String, PowerTreeConnectionStyle>> style) {
        List<PowerTreeConnectionStyle> chain = new ArrayList<>();
        style.flatMap(s -> s.map(name -> Optional.ofNullable(this.connectionStyles.get(name)), Optional::of)).ifPresent(chain::add);
        Optional.ofNullable(this.connectionStyles.get(DEFAULT_STYLE)).ifPresent(chain::add);
        chain.add(PowerTreeConnectionStyle.DEFAULT);
        return new PowerTreeConnectionStyle.Resolved(chain);
    }

    public void validate(Consumer<String> problems) {
        this.nodes.forEach((key, node) -> node.style().flatMap(s -> s.left()).ifPresent(name -> {
            if (!this.nodeStyles.containsKey(name)) {
                problems.accept("Unknown node style '" + name + "' used by node '" + key + "'");
            }
        }));

        for (Connection connection : this.connections) {
            connection.style().flatMap(s -> s.left()).ifPresent(name -> {
                if (!this.connectionStyles.containsKey(name)) {
                    problems.accept("Unknown connection style '" + name + "' used by connection '" + connection.from() + "' -> '" + connection.to() + "'");
                }
            });
        }
    }

    public record Node(Vec2 pos, Optional<Either<String, PowerTreeNodeStyle>> style, Optional<Component> title,
                       Optional<Icon> icon, Optional<Component> description) {

        private static final Codec<Node> OBJECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PalladiumCodecs.VEC2_CODEC.fieldOf("pos").forGetter(Node::pos),
                Codec.either(Codec.STRING, PowerTreeNodeStyle.CODEC).optionalFieldOf("style").forGetter(Node::style),
                ComponentSerialization.CODEC.optionalFieldOf("title").forGetter(Node::title),
                Icon.CODEC.optionalFieldOf("icon").forGetter(Node::icon),
                ComponentSerialization.CODEC.optionalFieldOf("description").forGetter(Node::description)
        ).apply(instance, Node::new));

        public static final Codec<Node> CODEC = Codec.either(PalladiumCodecs.VEC2_CODEC, OBJECT_CODEC).xmap(
                either -> either.map(pos -> new Node(pos, Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty()), node -> node),
                node -> node.isPositionOnly() ? Either.left(node.pos) : Either.right(node)
        );

        private boolean isPositionOnly() {
            return this.style.isEmpty() && this.title.isEmpty() && this.icon.isEmpty() && this.description.isEmpty();
        }

        public boolean hasDisplay() {
            return this.title.isPresent() || this.icon.isPresent();
        }

    }

    public record Connection(String from, String to, Optional<Either<String, PowerTreeConnectionStyle>> style,
                             Optional<PowerTreeConnectionPath> path, List<Vec2> via) {

        public static final Codec<Connection> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.STRING.fieldOf("from").forGetter(Connection::from),
                Codec.STRING.fieldOf("to").forGetter(Connection::to),
                Codec.either(Codec.STRING, PowerTreeConnectionStyle.CODEC).optionalFieldOf("style").forGetter(Connection::style),
                PowerTreeConnectionPath.CODEC.optionalFieldOf("path").forGetter(Connection::path),
                PalladiumCodecs.VEC2_CODEC.listOf().optionalFieldOf("via", List.of()).forGetter(Connection::via)
        ).apply(instance, Connection::new));

        public boolean connects(String from, String to) {
            return this.from.equals(from) && this.to.equals(to);
        }

    }

    public record Region(Vec2 pos, Vec2 size, Optional<Component> label, int labelColor, boolean labelShadow,
                         Vec2 labelOffset, Optional<Integer> color, Optional<TextureReference> background) {

        public static final Vec2 DEFAULT_LABEL_OFFSET = new Vec2(4, 4);

        public static final Codec<Region> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                PalladiumCodecs.VEC2_CODEC.fieldOf("pos").forGetter(Region::pos),
                PalladiumCodecs.VEC2_CODEC.fieldOf("size").forGetter(Region::size),
                ComponentSerialization.CODEC.optionalFieldOf("label").forGetter(Region::label),
                PowerTreeConnectionStyle.COLOR_CODEC.optionalFieldOf("label_color", 0xFFFFFFFF).forGetter(Region::labelColor),
                Codec.BOOL.optionalFieldOf("label_shadow", true).forGetter(Region::labelShadow),
                PalladiumCodecs.VEC2_CODEC.optionalFieldOf("label_offset", DEFAULT_LABEL_OFFSET).forGetter(Region::labelOffset),
                PowerTreeConnectionStyle.COLOR_CODEC.optionalFieldOf("color").forGetter(Region::color),
                TextureReference.CODEC.optionalFieldOf("background").forGetter(Region::background)
        ).apply(instance, Region::new));

    }
}
