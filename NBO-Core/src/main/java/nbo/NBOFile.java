package nbo;

import lombok.Getter;
import lombok.Setter;
import nbo.tree.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
@Setter
public class NBOFile extends NBOSerializationContext {

    public static final NBOSerializer DEFAULT_SERIALIZER = new NBOSerializer()
            .registerListSerializer(HashSet.class, HashSet::new, ArrayList::new)
            .registerListSerializer(Stack.class, objects -> {
                var stack = new Stack<>();
                stack.addAll(objects);
                return stack;
            }, ArrayList::new)
            .registerListSerializer(LinkedList.class, LinkedList::new, ArrayList::new)
            .registerListSerializer(Vector.class, objects -> {
                var vector = new Vector<>();
                vector.addAll(objects);
                return vector;
            }, ArrayList::new)
            .registerMapSerializer(HashMap.class, HashMap::new, hashMap -> {
                var map = new LinkedHashMap<String, Object>();
                map.putAll(hashMap);
                return map;
            })
            .registerMapSerializer(Hashtable.class, Hashtable::new, hashtable -> {
                var map = new LinkedHashMap<String, Object>();
                map.putAll(hashtable);
                return map;
            })
            .registerMapSerializer(Properties.class, map -> {
                var properties = new Properties();
                properties.putAll(map);
                return properties;
            }, properties -> {
                var map = new LinkedHashMap<String, Object>();
                properties.forEach((key, value) -> map.put((String) key, value));
                return map;
            });

    public static final String KEY_INCLUDES = "includes";
    public static final String KEY_IMPORTS = "imports";
    public static final String KEY_OBJECTS = "objects";

    private NBOSerializer serializer;
    private NBOPrettyPrinter prettyPrinter = new NBOPrettyPrinter();
    private NBOMap root = new NBOMap();

    public static NBOFile loadFile(File inputFile, NBOSerializer serializer) throws IOException, NBOParseException, ClassNotFoundException {
        return loadFile(inputFile, serializer, new ArrayList<>());
    }

    public static NBOFile loadFile(File inputFile, NBOSerializer serializer, List<File> dontImport) throws IOException, NBOParseException, ClassNotFoundException {
        FileInputStream stream = new FileInputStream(inputFile);
        String input = new String(stream.readAllBytes());
        stream.close();

        NBOFile file = new NBOFile();
        file.setSerializer(serializer);
        file.getFileIncludes().add(inputFile);
        dontImport.add(inputFile);

        NBOParser parser = new NBOParser();
        parser.tokenize(input);
        file.root = parser.createAST();
        new NBOInterpreter().interpret(file.root);

        NBOList includes = (NBOList) file.root.get(KEY_INCLUDES);
        NBOMap imports = (NBOMap) file.root.get(KEY_IMPORTS);
        NBOMap objects = (NBOMap) file.root.get(KEY_OBJECTS);

        // import each file if has not been loaded yet.
        for (NBOTree tree : includes) {
            String relativeFilePath = (String) tree.getValueRaw();
            File loadFile = new File(relativeFilePath);


            // Skip if loaded
            if (file.getFileIncludes().stream().anyMatch(f -> f.getAbsolutePath().equals(loadFile.getAbsolutePath()))
                    || dontImport.stream().anyMatch(f -> f.equals(loadFile))) {
                Logger.getAnonymousLogger().log(Level.WARNING, "Loop detected, ignoring file: " + loadFile.getAbsolutePath());
                continue;
            }

            // Else load and insert in imports and objects
            NBOFile loadedFile = loadFile(loadFile, serializer, dontImport);
            file.getFileIncludes().addAll(loadedFile.getFileIncludes());
            file.getClassImports().putAll(loadedFile.getClassImports());
            file.getReferenceObjects().putAll(loadedFile.getReferenceObjects());
        }
        imports.forEach((s, nboTree) -> file.getClassImports().put(s, ((NBOString) nboTree).getValueRaw()));

        for (var entry : objects.entrySet()) {
            Object deserialized = serializer.deserialize(entry.getValue(), file);
            file.getReferenceObjects().put(entry.getKey(), deserialized);
        }
        return file;
    }

    public <T> T get(String key) {
        return (T) getReferenceObjects().get(key);
    }

    public void setImport(String alias, Class<?> clazz) {
        getClassImports().put(alias, clazz.getName());
        var imports = root.get(KEY_IMPORTS);
        if (imports != null) {
            ((NBOMap) imports).put(alias, new NBOString(clazz.getName()));
        } else {
            imports = new NBOMap();
            ((NBOMap) imports).put(alias, new NBOString(clazz.getName()));
            root.put(KEY_IMPORTS, imports);
        }
    }

    public void setObject(String key, Object object) {
        NBOMap objects = (NBOMap) root.getOrDefault(KEY_OBJECTS, new NBOMap());
        if (objects != null) {
            objects.put(key, serializer.convertObjectToAst(object, this));
        }
        getReferenceObjects().put(key, object);
        root.put(KEY_OBJECTS, objects);
    }

    public void save(File file) throws IOException {
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(formatToFileString().getBytes(StandardCharsets.UTF_8));
        stream.close();
    }

    public String format(NBOTree tree) {
        return prettyPrinter.format(tree, this);
    }

    public String formatToFileString() {
        return prettyPrinter.formatFile(this);
    }
}
