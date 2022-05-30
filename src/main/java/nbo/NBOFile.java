package nbo;

import nbo.tree.NBOMap;
import nbo.tree.NBOObject;
import nbo.tree.NBOTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class NBOFile {

    public static final String KEY_IMPORTS = "imports";
    public static final String KEY_OBJECTS = "objects";

    private static final Collection<Class<? extends NBOSerializable>> SERIALIZABLES = new HashSet<>();

    private final Map<String, Class<?>> importMap;
    private final Map<String, Object> objectMap;

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
        NBOMap map = parser.createAST();
        new NBOInterpreter().interpret(map);

        NBOMap objects = (NBOMap) map.get(KEY_OBJECTS);
        for (var entry : objects.entrySet()) {
            NBOObject object = (NBOObject) entry.getValue();
            file.objectMap.put(entry.getKey(), deserialize(object));
        }
        return file;
    }

    public <T> T get(String key) {
        return (T) objectMap.get(key);
    }

    public static String serialize(NBOSerializable serializable) {
        return "";
    }

    public static <T> T deserialize(String objectString) throws ClassNotFoundException {
        return null; //TODO
    }

    public static <T> T deserialize(NBOObject object) throws ClassNotFoundException {
        return null; //TODO
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
                        .append(entry.getValue().pretty("    "))
                        .append("\n");
            }
        }
        return file.toString();
    }

    public static void register(Class<? extends NBOSerializable> serializable) {
        SERIALIZABLES.add(serializable);
    }

    public static void unregister(Class<? extends NBOSerializable> serializable) {
        SERIALIZABLES.add(serializable);
    }
}
