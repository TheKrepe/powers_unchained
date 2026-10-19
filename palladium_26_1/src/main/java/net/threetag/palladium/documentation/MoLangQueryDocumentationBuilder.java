package net.threetag.palladium.documentation;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.threetag.palladium.Palladium;
import team.unnamed.mocha.runtime.binding.Binding;

import java.lang.reflect.Method;
import java.util.*;

public class MoLangQueryDocumentationBuilder {

    private final String namespace;
    private final Class<?> queryClass;
    private String description;
    private final Map<String, Entry> entries = new LinkedHashMap<>();

    public MoLangQueryDocumentationBuilder(String namespace, Class<?> queryClass) {
        this.namespace = namespace;
        this.queryClass = queryClass;
    }

    public String getNamespace() {
        return this.namespace;
    }

    public MoLangQueryDocumentationBuilder setDescription(String description) {
        this.description = description;
        return this;
    }

    public MoLangQueryDocumentationBuilder add(String name, String description, Argument... arguments) {
        this.entries.put(name, new Entry(name, description, List.of(arguments)));
        return this;
    }

    public static Argument argument(String name, String description) {
        return new Argument(name, description);
    }

    private Map<String, Method> collectBindings() {
        Map<String, Method> bindings = new TreeMap<>();

        for (Method method : this.queryClass.getMethods()) {
            var binding = method.getAnnotation(Binding.class);

            if (binding != null) {
                for (String name : binding.value()) {
                    bindings.put(name, method);
                }
            }
        }

        return bindings;
    }

    public JsonArray build() {
        var bindings = this.collectBindings();
        var array = new JsonArray();

        for (String name : this.entries.keySet()) {
            if (!bindings.containsKey(name)) {
                Palladium.LOGGER.error("MoLang query {}.{} is documented but does not exist", this.namespace, name);
            }
        }

        bindings.forEach((name, method) -> {
            var entry = this.entries.get(name);

            if (entry == null) {
                Palladium.LOGGER.error("No documentation entry found for MoLang query {}.{}", this.namespace, name);
                return;
            }

            var parameters = method.getParameterTypes();

            if (parameters.length != entry.arguments.size()) {
                Palladium.LOGGER.error("MoLang query {}.{} takes {} arguments, but {} are documented", this.namespace, name, parameters.length, entry.arguments.size());
            }

            var json = new JsonObject();
            json.addProperty("namespace", this.namespace);
            json.addProperty("path", name);
            json.addProperty("name", this.namespace + "." + name);
            json.addProperty("description", entry.description);
            json.addProperty("returns", typeName(method.getReturnType()));

            var arguments = new JsonArray();
            for (int i = 0; i < entry.arguments.size(); i++) {
                var argument = entry.arguments.get(i);
                var argumentJson = new JsonObject();
                argumentJson.addProperty("key", argument.name);
                argumentJson.addProperty("type", i < parameters.length ? typeName(parameters[i]) : "Any");
                argumentJson.addProperty("description", argument.description);
                arguments.add(argumentJson);
            }
            json.add("arguments", arguments);

            array.add(json);
        });

        return array;
    }

    public void addToHtml(HTMLBuilder.HTMLObject div) {
        div.add(HTMLBuilder.subHeading(this.namespace));

        if (this.description != null && !this.description.isEmpty()) {
            div.add(HTMLBuilder.paragraph(this.description));
        }

        List<Iterable<?>> rows = new ArrayList<>();

        for (var element : this.build()) {
            var json = element.getAsJsonObject();
            var signature = new StringBuilder(json.get("name").getAsString());
            var arguments = json.getAsJsonArray("arguments");

            if (!arguments.isEmpty()) {
                List<String> names = new ArrayList<>();
                arguments.forEach(a -> names.add(a.getAsJsonObject().get("key").getAsString()));
                signature.append("(").append(String.join(", ", names)).append(")");
            }

            rows.add(Arrays.asList(signature.toString(), json.get("returns").getAsString(), json.get("description").getAsString()));
        }

        div.add(HTMLBuilder.table(Arrays.asList("Query", "Returns", "Description"), rows));
    }

    private static String typeName(Class<?> type) {
        if (type == boolean.class || type == Boolean.class) {
            return "Boolean";
        } else if (type == String.class) {
            return "String";
        } else if (type.isPrimitive() || Number.class.isAssignableFrom(type)) {
            return "Number";
        } else {
            return "Any";
        }
    }

    private record Entry(String name, String description, List<Argument> arguments) {

    }

    public record Argument(String name, String description) {

    }
}
