package nbo;

import lombok.Getter;
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
import java.util.Map;

@Getter
public class NBOFile {

    public static final String KEY_IMPORTS = "imports";
    public static final String KEY_OBJECTS = "objects";

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
            file.objectMap.put(entry.getKey(), NBOSerializer.deserialize(object));
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
            objects.put(key, NBOSerializer.convertObjectToAst(object));
        }
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
        return new NBOPrettyPrinter().formatFile(fileRoot);
    }
}
