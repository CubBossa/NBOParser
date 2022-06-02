package nbo;

import nbo.tree.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NBOSerializer {

    public interface ConvertTo {
        Map<String, Object> convert(Object o);
    }

    public interface ConvertFrom {
        Object convert(Map<String, Object> map);
    }

    public record Serializer(Class<?> clazz, ConvertFrom from, ConvertTo to) {
    }

    private final Map<Class<?>, Serializer> serializers = new HashMap<>();


    public Map<String, Object> convertAstToMap(NBOMap input) throws ClassNotFoundException {
        return (Map<String, Object>) convertAstToObject(input);
    }

    public Object convertAstToObject(NBOTree input) throws ClassNotFoundException {
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

    public NBOTree convertObjectToAst(Object input, NBOFile file, Collection<Object> referencedObjects) {
        if (input == null) {
            return new NBOString("null");
        }
        Serializer serializer = serializers.get(input.getClass());
        if (serializer != null) {
            // Only create reference
            if (file.getObjectMap().containsValue(input) && referencedObjects.contains(input)) {
                return new NBOReference(file.getObjectMap().entrySet().stream().filter(e -> e.getValue().equals(input)).findFirst().get().getKey());
            }
            // Create actual object
            NBOObject object = new NBOObject(input.getClass().getName());
            object.putAll(serializer.to().convert(input).entrySet().stream()
                    .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), convertObjectToAst(e.getValue(), file, referencedObjects)))
                    .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));

            // Create import
            String alias = input.getClass().getSimpleName();
            if (file.getImportMap().keySet().stream().noneMatch(string -> string.equals(alias))) {
                file.setImport(alias, input.getClass());
            }

            // Allow to be referenced from now on
            referencedObjects.add(input);
            return object;

        } else if (input instanceof String string) {
            return new NBOString(string);
        } else if (input instanceof Integer integer) {
            return new NBOInteger(integer);
        } else if (input instanceof Boolean bool) {
            return new NBOBool(bool);
        } else if (input instanceof Float val) {
            return new NBOFloat(val);
        } else if (input instanceof Double val) {
            return new NBOFloat(val.floatValue());
        } else if (input instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) input;
            var m = new NBOMap();
            m.putAll(map.entrySet().stream()
                    .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), convertObjectToAst(e.getValue(), file, referencedObjects)))
                    .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
            return m;
        } else if (input instanceof List<?> list) {
            var l = new NBOList();
            l.addAll(list.stream().map(e -> convertObjectToAst(e, file, referencedObjects)).collect(Collectors.toList()));
            return l;
        }
        throw new RuntimeException("Could not convert object to ast: " + input);
    }

    public <T> T deserialize(String objectString) throws ClassNotFoundException {
        return null; //TODO
    }

    public <T> T deserialize(NBOObject object, NBOSerializationContext context) throws ClassNotFoundException {
        String className = context.getClassImports().getOrDefault(object.getType(), object.getType());
        Class<?> clazz = Class.forName(className);
        Serializer serializer = serializers.get(clazz);
        if (serializer == null) {
            throw new RuntimeException("Class not registered for serialization: " + clazz.getName());
        }
        Map<String, Object> actualValues = new HashMap<>();
        for (Map.Entry<String, NBOTree> entry : object.entrySet()) {
            actualValues.put(entry.getKey(), deserialize(entry.getValue()));
        }
        for (Map.Entry<String, Object> entry : actualValues.entrySet()) {
            if (entry.getValue() instanceof NBOReference reference) {
                String referenceName = reference.getValueRaw();
                Object actualValue = context.getReferenceObjects().get(referenceName);
                entry.setValue(actualValue);
            }
        }
        return (T) serializer.from().convert(actualValues);
    }

    public <T> T deserialize(NBOTree nboTree) throws ClassNotFoundException {
        if (nboTree instanceof NBOBool nboBool) {
            return (T) nboBool.getValueRaw();
        } else if (nboTree instanceof NBOFloat nboFloat) {
            return (T) nboFloat.getValueRaw();
        } else if (nboTree instanceof NBOInteger nboInteger) {
            return (T) nboInteger.getValueRaw();
        } else if (nboTree instanceof NBOString nboString) {
            return (T) nboString.getValueRaw();
        } else if (nboTree instanceof NBOReference nboReference) {
            return (T) nboReference;
        } else if (nboTree instanceof NBOObject nboObject) {
            return deserialize(nboObject);
        }
        throw new RuntimeException("Could not unpack NBOTree object: " + nboTree);
    }

    public <T> NBOSerializer register(Class<T> serializable, Function<Map<String, Object>, T> to, Function<T, Map<String, Object>> from) {
        serializers.put(serializable, new Serializer(serializable, to::apply, o -> from.apply((T) o)));
        return this;
    }

    public NBOSerializer unregister(Class<?> serializable) {
        serializers.remove(serializable);
        return this;
    }

    public Map<String, Object> serialize(Object object) {
        if (object instanceof NBOSerializable serializable) {
            return serializable.serialize();
        }
        Serializer serializer = serializers.get(object.getClass());
        if (serializer != null) {
            return serializer.to().convert(object);
        }
        throw new IllegalArgumentException("Input object " + object.getClass() + " must either be registered as Serializable or implement NBOSerializable interface.");
    }

}
