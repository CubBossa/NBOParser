package nbo;

import nbo.tree.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NBOSerializer {

    public interface ConvertTo<T> {
        T convert(Object o);
    }

    public interface ConvertFrom<T> {
        Object convert(T map);
    }

    public record Serializer<T>(Class<?> clazz, ConvertFrom<T> from, ConvertTo<T> to) {
    }

    private final Map<Class<?>, Serializer<Collection<Object>>> listSerializers = new HashMap<>();
    private final Map<Class<?>, Serializer<Map<String, Object>>> objectSerializers = new HashMap<>();

    public NBOTree convertObjectToAst(Object input, NBOFile file) {
        if (input == null) {
            return new NBONull();
        }

        // Handle map based objects
        Serializer<Map<String, Object>> mapSerializer = objectSerializers.get(input.getClass());
        if (mapSerializer != null) {
            // Only create reference
            if (file.getReferenceObjects().containsValue(input)) {
                return new NBOReference(file.getReferenceObjects().entrySet().stream().filter(e -> e.getValue().equals(input)).findFirst().get().getKey());
            }
            // Create actual object
            NBOMap map = new NBOMap();
            map.setType(input.getClass().getName());
            map.putAll(mapSerializer.to().convert(input).entrySet().stream()
                    .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), convertObjectToAst(e.getValue(), file)))
                    .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, (tree, tree2) -> tree2, () -> new LinkedHashMap<>())));

            // Create import
            String alias = input.getClass().getSimpleName();
            if (file.getClassImports().entrySet().stream().noneMatch(e -> e.getKey().equals(alias) || e.getValue().equals(input.getClass().getName()))) {
                file.setImport(alias, input.getClass());
            }
            return map;
        }

        // Handle list based objects
        Serializer<Collection<Object>> listSerializer = listSerializers.get(input.getClass());
        if (listSerializer != null) {
            // Only create reference
            if (file.getReferenceObjects().containsValue(input)) {
                return new NBOReference(file.getReferenceObjects().entrySet().stream().filter(e -> e.getValue().equals(input)).findFirst().get().getKey());
            }
            // Create actual object
            NBOList list = new NBOList();
            list.setType(input.getClass().getName());
            list.addAll(listSerializer.to().convert(input).stream()
                    .map(e -> convertObjectToAst(e, file))
                    .collect(Collectors.toCollection(ArrayList::new)));

            // Create import
            String alias = input.getClass().getSimpleName();
            if (file.getClassImports().entrySet().stream().noneMatch(e -> e.getKey().equals(alias) || e.getValue().equals(input.getClass().getName()))) {
                file.setImport(alias, input.getClass());
            }
            return list;
        }

        if (input instanceof String string) {
            return new NBOString(string);
        } else if (input instanceof Short s) {
            return new NBOShort(s);
        } else if (input instanceof Long l) {
            return new NBOLong(l);
        } else if (input instanceof Integer integer) {
            return new NBOInteger(integer);
        } else if (input instanceof Boolean bool) {
            return new NBOBool(bool);
        } else if (input instanceof Byte bool) {
            return new NBOByte(bool);
        } else if (input instanceof Double val) {
            return new NBODouble(val);
        } else if (input instanceof Float val) {
            return new NBOFloat(val);
        } else if (input instanceof LinkedHashMap) {
            Map<String, Object> map = (Map<String, Object>) input;
            var m = new NBOMap();
            m.putAll(map.entrySet().stream()
                    .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), convertObjectToAst(e.getValue(), file)))
                    .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
            return m;
        } else if (input instanceof ArrayList<?> list) {
            var l = new NBOList();
            l.addAll(list.stream().map(e -> convertObjectToAst(e, file)).collect(Collectors.toList()));
            return l;
        }
        return convertObjectToAst(serialize(input), file);
    }

    public <T> T deserialize(NBOTree nboTree, NBOSerializationContext context) throws ClassNotFoundException {
        if (nboTree == null || nboTree instanceof NBONull) {
            return null;
        } else if (nboTree instanceof NBOBool nboBool) {
            return (T) nboBool.getValueRaw();
        } else if (nboTree instanceof NBOFloat nboFloat) {
            return (T) nboFloat.getValueRaw();
        } else if (nboTree instanceof NBODouble nboDouble) {
            return (T) nboDouble.getValueRaw();
        } else if (nboTree instanceof NBOByte nboByte) {
            return (T) nboByte.getValueRaw();
        } else if (nboTree instanceof NBOLong nboLong) {
            return (T) nboLong.getValueRaw();
        } else if (nboTree instanceof NBOShort nboShort) {
            return (T) nboShort.getValueRaw();
        } else if (nboTree instanceof NBOInteger nboInteger) {
            return (T) nboInteger.getValueRaw();
        } else if (nboTree instanceof NBOString nboString) {
            return (T) nboString.getValueRaw();
        } else if (nboTree instanceof NBOReference nboReference) {
            return (T) context.getReferenceObjects().get(nboReference.getReference());
        } else if (nboTree instanceof NBOTyped typed) {
            return deserialize(typed, context);
        }
        throw new RuntimeException("Could not unpack NBOTree object: " + nboTree);
    }

    public <T> T deserialize(NBOTyped object, NBOSerializationContext context) throws ClassNotFoundException {
        String className = context.getClassImports().getOrDefault(object.getType(), object.getType());
        Class<?> clazz = className == null ? null : Class.forName(className);

        if (object instanceof NBOMap map) {
            Map<String, Object> actualValues = new LinkedHashMap<>();
            for (Map.Entry<String, NBOTree> entry : map.entrySet()) {
                actualValues.put(entry.getKey(), deserialize(entry.getValue(), context));
            }
            for (Map.Entry<String, Object> entry : actualValues.entrySet()) {
                if (entry.getValue() instanceof NBOReference reference) {
                    String referenceName = reference.getValueRaw();
                    Object actualValue = context.getReferenceObjects().get(referenceName);
                    entry.setValue(actualValue);
                }
            }
            if (clazz != null) {
                Serializer<Map<String, Object>> serializer = objectSerializers.get(clazz);
                if (serializer == null) {
                    throw new RuntimeException("Class not registered for serialization: " + clazz.getName());
                }
                return (T) serializer.from().convert(actualValues);
            } else {
                return (T) new LinkedHashMap<>(actualValues);
            }

        } else if (object instanceof NBOList list) {
            ArrayList<Object> actualValues = new ArrayList<>();
            for (NBOTree entry : list) {
                actualValues.add(deserialize(entry, context));
            }
            for (int i = 0; i < actualValues.size(); i++) {
                Object entry = actualValues.get(i);
                if (entry instanceof NBOReference reference) {
                    String referenceName = reference.getValueRaw();
                    Object actualValue = context.getReferenceObjects().get(referenceName);
                    actualValues.set(i, actualValue);
                }
            }
            if (clazz != null) {
                Serializer<Collection<Object>> serializer = listSerializers.get(clazz);
                if (serializer == null) {
                    throw new RuntimeException("Class not registered for serialization: " + clazz.getName());
                }
                return (T) serializer.from().convert(actualValues);
            } else {
                if (list.getElementType() != null) {
                    //TODO return array of type here
                }
                return (T) new ArrayList<>(actualValues);
            }
        }
        throw new IllegalArgumentException("The provided NBOTyped is neither a List nor a Map.");
    }

    public <T> T serialize(Object object) {
        if (object instanceof NBOSerializable serializable) {
            return (T) serializable.serialize();
        }
        Serializer<Map<String, Object>> serializer = objectSerializers.get(object.getClass());
        if (serializer != null) {
            return (T) serializer.to().convert(object);
        }
        Serializer<Collection<Object>> listSerializer = listSerializers.get(object.getClass());
        if (listSerializer != null) {
            return (T) listSerializer.to().convert(object);
        }
        throw new IllegalArgumentException("Input object " + object.getClass() + " must either be registered as Serializable or implement NBOSerializable interface.");
    }

    public <T> NBOSerializer registerMapSerializer(Class<T> serializable, Function<Map<String, Object>, T> to, Function<T, Map<String, Object>> from) {
        objectSerializers.put(serializable, new Serializer<>(serializable, to::apply, o -> from.apply((T) o)));
        return this;
    }

    public <T> NBOSerializer registerListSerializer(Class<T> serializable, Function<Collection<Object>, T> to, Function<T, List<Object>> from) {
        listSerializers.put(serializable, new Serializer<>(serializable, to::apply, o -> from.apply((T) o)));
        return this;
    }

    public NBOSerializer unregister(Class<?> serializable) {
        objectSerializers.remove(serializable);
        return this;
    }

}
