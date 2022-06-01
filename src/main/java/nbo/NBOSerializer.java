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

	private static final Collection<Serializer> SERIALIZABLES = new HashSet<>();

	public static Map<String, Object> convertAstToMap(NBOMap input) throws ClassNotFoundException {
		return (Map<String, Object>) convertAstToObject(input);
	}

	public static Object convertAstToObject(NBOTree input) throws ClassNotFoundException {
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

	public static NBOTree convertObjectToAst(Object input) {
		Serializer serializer = SERIALIZABLES.stream().filter(s -> s.clazz.equals(input.getClass())).findFirst().orElse(null);
		if (serializer != null) {
			NBOObject object = new NBOObject(input.getClass().getName());
			object.putAll(serializer.to().convert(input).entrySet().stream()
					.map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), convertObjectToAst(e.getValue())))
					.collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
			return object;

		} else if (input instanceof String string) {
			return new NBOString(string);
		} else if (input instanceof Integer integer) {
			return new NBOInteger(integer);
		} else if (input instanceof Boolean bool) {
			return new NBOBool(bool);
		} else if (input instanceof Float val) {
			return new NBOFloat(val);
		} else if (input instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) input;
			var m = new NBOMap();
			m.putAll(map.entrySet().stream()
					.map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), convertObjectToAst(e.getValue())))
					.collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue)));
			return m;
		} else if (input instanceof List list) {
			var l = new NBOList();
			l.addAll(list);
			return l;
		}
		return null;
	}

	public static <T> T deserialize(String objectString) throws ClassNotFoundException {
		return null; //TODO
	}

	public static <T> T deserialize(NBOObject object) throws ClassNotFoundException {
		return null; //TODO
	}

	public static <T> void register(Class<T> serializable, Function<Map<String, Object>, T> to, Function<T, Map<String, Object>> from) {
		SERIALIZABLES.add(new Serializer(serializable, to::apply, o -> from.apply((T) o)));
	}

	public static void unregister(Class<?> serializable) {
		SERIALIZABLES.removeAll(SERIALIZABLES.stream().filter(nboSerializer -> nboSerializer.clazz.equals(serializable)).collect(Collectors.toSet()));
	}

	public static Map<String, Object> serialize(Object object) {
		if (object instanceof NBOSerializable serializable) {
			return serializable.serialize();
		}
		Serializer serializer = SERIALIZABLES.stream().filter(s -> s.clazz.equals(object.getClass())).findFirst().orElse(null);
		if (serializer != null) {
			return serializer.to().convert(object);
		}
		throw new IllegalArgumentException("Input object " + object.getClass() + " must either be registered as Serializable or implement NBOSerializable interface.");
	}

}
