package nbo;

import lombok.Getter;
import lombok.Setter;
import nbo.tree.NBOMap;
import nbo.tree.NBOObject;
import nbo.tree.NBOString;
import nbo.tree.NBOTree;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Getter
@Setter
public class NBOFile {

    public static final String KEY_IMPORTS = "imports";
    public static final String KEY_OBJECTS = "objects";

    private NBOSerializer serializer;
    private final Map<String, Class<?>> importMap;
    private final Map<String, Object> objectMap;
    private NBOMap root = new NBOMap();

    private NBOFile() {
        importMap = new HashMap<>();
        objectMap = new HashMap<>();
    }

    public static NBOFile loadFile(File file, NBOSerializer serializer) throws IOException, NBOParseException, ClassNotFoundException {
        FileInputStream stream = new FileInputStream(file);
        String input = new String(stream.readAllBytes());
        stream.close();
        return loadString(input, serializer);
    }

    public static NBOFile loadString(String input, NBOSerializer serializer) throws NBOParseException, ClassNotFoundException {
        NBOFile file = new NBOFile();
        file.setSerializer(serializer);

        NBOParser parser = new NBOParser();
        parser.tokenize(input);
        file.root = parser.createAST();
        new NBOInterpreter().interpret(file.root);

        NBOMap objects = (NBOMap) file.root.get(KEY_OBJECTS);
        NBOMap imports = (NBOMap) file.root.get(KEY_IMPORTS);

        NBOSerializationContext context = new NBOSerializationContext();
        imports.forEach((s, nboTree) -> context.getClassImports().put(s, ((NBOString) nboTree).getValueRaw()));

        for (var entry : objects.entrySet()) {
            NBOObject object = (NBOObject) entry.getValue();
            Object deserialized = serializer.deserialize(object, context);
            file.objectMap.put(entry.getKey(), deserialized);
            context.getReferenceObjects().put(entry.getKey(), deserialized);
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
        NBOMap objects = (NBOMap) root.getOrDefault(KEY_OBJECTS, new NBOMap());
        if (objects != null) {
            objects.put(key, serializer.convertObjectToAst(object, this));
        }
        objectMap.put(key, object);
        root.put(KEY_OBJECTS, objects);
    }

    public void save(File file) throws IOException {
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(formatToFileString(root).getBytes(StandardCharsets.UTF_8));
        stream.close();
    }

    public static String format(NBOTree tree) {
        return new NBOPrettyPrinter().format(tree);
    }

    public static String formatToFileString(NBOMap fileRoot) {
        return new NBOPrettyPrinter((NBOMap) fileRoot.getOrDefault(KEY_IMPORTS, new NBOMap())).formatFile(fileRoot);
    }
}
