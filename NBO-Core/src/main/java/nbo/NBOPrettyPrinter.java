package nbo;

import lombok.Getter;
import lombok.Setter;
import nbo.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@Setter
public class NBOPrettyPrinter {

	private int listInlineLength = 3;
	private int mapInlineLength = 3;
	private int maxInlineLength = 70;
	private String indent = "    ";
	private String lineBreak = "\n";
	private final Map<Class<?>, Function<NBOTree, String>> prettyMethods;

	public NBOPrettyPrinter() {
		this(new NBOMap());
	}

	public NBOPrettyPrinter(NBOMap imports) {

		Function<NBOMap, String> mapString = tree -> {
			List<String> inner = tree.entrySet().stream()
					.map(e -> e.getKey() + ": " + (e.getValue() == null ? null : format(e.getValue()).replace(lineBreak, lineBreak + indent)))
					.collect(Collectors.toCollection(ArrayList::new));

			return inner.size() > mapInlineLength || inner.stream().anyMatch(s -> s.contains("\n")) || inner.stream().mapToInt(String::length).sum() > maxInlineLength ?
					"{" + lineBreak + indent + String.join("," + lineBreak + indent, inner) + lineBreak + "}" :
					"{" + String.join(", ", inner) + "}";
		};

		this.prettyMethods = Map.of(
				NBONull.class, tree -> "null",
				NBOString.class, Object::toString,
				NBOInteger.class, Object::toString,
				NBOFloat.class, Object::toString,
				NBOBool.class, Object::toString,
				NBOReference.class, Object::toString,
				NBOObject.class, tree -> {
					String type = ((NBOObject) tree).getType();
					String alias = imports.entrySet().stream().filter(e -> e.getValue().getValueRaw().equals(type)).map(Map.Entry::getKey).findFirst().orElse(null);
					return (alias == null ? type : alias) + " " + mapString.apply((NBOMap) tree);
				},
				NBOMap.class, tree -> mapString.apply((NBOMap) tree),
				NBOList.class, tree -> {
					NBOList list = (NBOList) tree;
					List<String> inner = list.stream().map(t -> format(t).replace(lineBreak, lineBreak + indent)).collect(Collectors.toList());
					return list.size() > listInlineLength || inner.stream().anyMatch(s -> s.contains("\n")) ?
							"[" + lineBreak + indent + inner.stream().collect(Collectors.joining("," + lineBreak + indent)) + lineBreak + "]" :
							"[" + String.join(", ", inner) + "]";
				}
		);
	}

	public String format(NBOTree tree) {
		return prettyMethods.getOrDefault(tree.getClass(), t -> "NO PRETTY METHOD PROVIDED").apply(tree);
	}

	public String formatFile(NBOMap fileRoot) {
		StringBuilder file = new StringBuilder();

		NBOMap imports = (NBOMap) fileRoot.get(NBOFile.KEY_IMPORTS);
		if (imports != null && !imports.isEmpty()) {
			file.append("# IMPORTS").append(lineBreak);
			for (var entry : imports.entrySet()) {
				file.append(lineBreak).append("<with ").append(entry.getKey()).append(" as ").append(entry.getValue().getValue()).append(">");
			}
			file.append(lineBreak.repeat(3));
		}
		NBOMap objects = (NBOMap) fileRoot.get(NBOFile.KEY_OBJECTS);
		if (objects != null && !objects.isEmpty()) {
			file.append("# OBJECTS").append(lineBreak);
			for (var entry : objects.entrySet()) {
				file.append(lineBreak).append(entry.getKey())
						.append(" := ")
						.append(entry.getValue() == null ? "null" : format(entry.getValue()))
						.append(lineBreak);
			}
		}
		return file.toString();
	}
}
