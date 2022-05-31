package nbo;

import lombok.Getter;
import nbo.tree.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
public class NBOFile {

    public interface ConvertTo {
        Map<String, Object> convert(Object o);
    }

    public interface ConvertFrom {
        Object convert(Map<String, Object> map);
    }

    public record NBOSerializer(Class<?> clazz, ConvertFrom from, ConvertTo to) {
    }

    public static final String KEY_IMPORTS = "imports";
    public static final String KEY_OBJECTS = "objects";

    private static final Collection<NBOSerializer> SERIALIZABLES = new HashSet<>();

    private final Map<String, Class<?>> importMap;
    private final Map<String, Object> objectMap;
    private NBOMap root;

    private NBOFile() {
        importMap = new HashMap<>();
        objectMap = new HashMap<>();
    }

    public static NBOFile loadFile(File file) throws IOException, NBOParseException, ClassNotFoundException {
        FileInputStream stream = new FileInputStream(file);
        String input = new String(stream.readAllBytes());
        stream.close();
        return loadString(input);
    }

    public static NBOFile loadString(String input) throws NBOParseException, ClassNotFoundException {
        NBOFile file = new NBOFile();

        NBOParser parser = new NBOParser();
        parser.tokenize(input);
        file.root = parser.createAST();
        new NBOInterpreter().interpret(file.root);

        NBOMap objects = (NBOMap) file.root.get(KEY_OBJECTS);
        for (var entry : objects.entrySet()) {
            NBOObject object = (NBOObject) entry.getValue();
            file.objectMap.put(entry.getKey(), deserialize(object));
        }
        return file;
    }

    public <T> T get(String key) {
        return (T) objectMap.get(key);
    }

    public void setImport(String alias, Class<?> clazz) {
        importMap.put(alias, clazz);
        var imports = root.get(KEY_IMPORTS);
        if (imports != null) {
            ((NBOMap) imports).put(alias, new NBOString(clazz.getName()));
        }
    }

    public void setObject(String key, Object object) {
        objectMap.put(key, object);
        NBOMap objects = (NBOMap) root.get(KEY_OBJECTS);
        if (objects != null) {
            Map<String, Object> mapRepresentation = serialize(object);
            NBOTree nboRepresentation = convertMapToAST(mapRepresentation);
            objects.put(key, nboRepresentation);
        }
    }

    public static Map<String, Object> convertAstToMap(NBOMap input) throws ClassNotFoundException {
        return (Map<String, Object>) convertAstToObject(input);
    }

    private static Object convertAstToObject(NBOTree input) throws ClassNotFoundException {
        if (input instanceof NBOObject object) {
            return deserialize(object);
        } else if (input instanceof NBOMap map) {
            return new LinkedHashMap<>(map.entrySet().stream().map(e -> {
                try {
                    return new AbstractMap.SimpleEntry<>(e.getKey(), convertAstToObject(e.getValue()));
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
        } else if (input instanceof NBOList list) {
            return list.stream().map(e -> {
                try {
                    return convertAstToObject(e);
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
                return null;
            }).distinct().collect(Collectors.toCollection(ArrayList::new));
        } else if (input instanceof NBOString string) {
            return string.getValue();
        }
        return null;
    }

    public static NBOMap convertMapToAST(Map<String, Object> input) {
        return (NBOMap) convertObjectToAst(input);
    }

    private static NBOTree convertObjectToAst(Object input) {
        if (input instanceof String string) {
            return new NBOString(string);
        }
        return null;
    }

    public static Map<String, Object> serialize(Object object) {
        if (object instanceof NBOSerializable serializable) {
            return serializable.serialize();
        }
        NBOSerializer serializer = SERIALIZABLES.stream().filter(s -> s.clazz.equals(object.getClass())).findFirst().orElse(null);
        if (serializer != null) {
            return serializer.to().convert(object);
        }
        throw new IllegalArgumentException("Input object must either be registered as Serializable or implement NBOSerializable interface.");
    }

    public static <T> T deserialize(String objectString) throws ClassNotFoundException {
        return null; //TODO
    }

    public static <T> T deserialize(NBOObject object) throws ClassNotFoundException {
        return null; //TODO
    }

    public void save(File file) throws IOException {
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(formatToFileString(root).getBytes(StandardCharsets.UTF_8));
        stream.close();
    }

    public static String format(NBOTree tree) {
        return tree.pretty("  ");
    }

    public static String formatToFileString(NBOMap fileRoot) {
        StringBuilder file = new StringBuilder();

        NBOMap imports = (NBOMap) fileRoot.get(KEY_IMPORTS);
        if (imports != null && !imports.isEmpty()) {
            file.append("# IMPORTS\n");
            for (var entry : imports.entrySet()) {
                file.append("\n<with ").append(entry.getKey()).append(" as ").append(entry.getValue().getValue()).append(">");
            }
            file.append("\n\n\n");
        }
        NBOMap objects = (NBOMap) fileRoot.get(KEY_OBJECTS);
        if (objects != null && !objects.isEmpty()) {
            file.append("# OBJECTS\n");
            for (var entry : objects.entrySet()) {
                file.append("\n").append(entry.getKey())
                        .append(" := ")
                        .append(entry.getValue() == null ? "null" : entry.getValue().pretty("    "))
                        .append("\n");
            }
        }
        return file.toString();
    }

    public static <T> void register(Class<T> serializable, Function<Map<String, Object>, T> to, Function<T, Map<String, Object>> from) {
        SERIALIZABLES.add(new NBOSerializer(serializable, to::apply, o -> from.apply((T) o)));
    }

    public static void unregister(Class<?> serializable) {
        SERIALIZABLES.removeAll(SERIALIZABLES.stream().filter(nboSerializer -> nboSerializer.clazz.equals(serializable)).collect(Collectors.toSet()));
    }
}
