package nbo;

import lombok.Getter;
import lombok.Setter;
import nbo.tree.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Getter
@Setter
public class NBOPrettyPrinter {

	private boolean useHeadings = true;
	private boolean useAliases = true;
	private boolean useReferences = true;
	private int listInlineLength = 3;
	private int mapInlineLength = 3;
	private int maxInlineLength = 70;
	private String indent = "    ";
	private String lineBreak = "\n";
	private Map<Class<? extends NBOTree>, BiFunction<NBOSerializationContext, NBOTree, String>> prettyMethods = loadPrettyMethods();

	private Map<Class<? extends NBOTree>, BiFunction<NBOSerializationContext, NBOTree, String>> loadPrettyMethods() {

		BiFunction<NBOSerializationContext, NBOMap, String> mapString = (context, tree) -> {
			List<String> inner = tree.entrySet().stream()
					.map(e -> e.getKey() + ": " + (e.getValue() == null ? null : format(e.getValue(), context).replace(lineBreak, lineBreak + indent)))
					.collect(Collectors.toCollection(ArrayList::new));

			return inner.size() > mapInlineLength || inner.stream().anyMatch(s -> s.contains("\n")) || inner.stream().mapToInt(String::length).sum() > maxInlineLength ?
					"{" + lineBreak + indent + String.join("," + lineBreak + indent, inner) + lineBreak + "}" :
					"{" + String.join(", ", inner) + "}";
		};

		return new LinkedHashMapBuilder<Class<? extends NBOTree>, BiFunction<NBOSerializationContext, NBOTree, String>>()
				.put(NBONull.class, (context, tree) -> "null")
				.put(NBOString.class, (context, tree) -> tree.toString())
				.put(NBOInteger.class, (context, tree) -> tree.toString())
				.put(NBOFloat.class, (context, tree) -> tree.toString())
				.put(NBODouble.class, (context, tree) -> tree.toString())
				.put(NBOBool.class, (context, tree) -> tree.toString())
				.put(NBOByte.class, (context, tree) -> tree.toString())
				.put(NBOShort.class, (context, tree) -> tree.toString())
				.put(NBOInteger.class, (context, tree) -> tree.toString())
				.put(NBOLong.class, (context, tree) -> tree.toString())
				.put(NBOReference.class, (context, tree) -> tree.toString())
				.put(NBOMap.class, (context, tree) -> {
					String type = ((NBOMap) tree).getType();
					String alias = context.getClassImports().entrySet().stream().filter(e -> e.getValue().equals(type)).map(Map.Entry::getKey).findFirst().orElse(null);
					return (alias == null ? type : alias) + " " + mapString.apply(context, (NBOMap) tree);
				})
				.put(NBOList.class, (context, tree) -> {
					NBOList list = (NBOList) tree;
					String type = list.getType();
					String alias = context.getClassImports().entrySet().stream().filter(e -> e.getValue().equals(type)).map(Map.Entry::getKey).findFirst().orElse(null);
					List<String> inner = list.stream().map(t -> format(t, context).replace(lineBreak, lineBreak + indent)).collect(Collectors.toList());
					return (alias != null ? alias + " " : "") + (list.size() > listInlineLength || inner.stream().anyMatch(s -> s.contains("\n")) ?
							"[" + lineBreak + indent + inner.stream().collect(Collectors.joining("," + lineBreak + indent)) + lineBreak + "]" :
							"[" + String.join(", ", inner) + "]");
				}).build();
	}

	public String format(NBOTree tree, NBOSerializationContext context) {
		return prettyMethods.getOrDefault(tree.getClass(), (c, t) -> "{No Pretty Method: " + tree.getClass().getName() + "}").apply(context, tree);
	}

	public String formatFile(NBOFile context) {
		StringBuilder file = new StringBuilder();

		NBOList includes = (NBOList) context.getRoot().get(NBOFile.KEY_INCLUDES);
		if (includes != null && !includes.isEmpty()) {
			if (useHeadings) {
				file.append("# INCLUDES").append(lineBreak);
			}
			for (NBOTree fileName : includes) {
				file.append(lineBreak).append("<include '").append(fileName.getValueRaw()).append("'>");
			}
			file.append(lineBreak.repeat(3));
		}

		NBOMap imports = (NBOMap) context.getRoot().get(NBOFile.KEY_IMPORTS);
		if (imports != null && !imports.isEmpty()) {
			if (useHeadings) {
				file.append("# IMPORTS").append(lineBreak);
			}
			for (var entry : imports.entrySet()) {
				file.append(lineBreak).append("<with ").append(entry.getKey()).append(" as ").append(entry.getValue().getValueRaw()).append(">");
			}
			file.append(lineBreak.repeat(3));
		}
		NBOMap objects = (NBOMap) context.getRoot().get(NBOFile.KEY_OBJECTS);
		if (objects != null && !objects.isEmpty()) {
			if (useHeadings) {
				file.append("# OBJECTS").append(lineBreak);
			}
			for (var entry : objects.entrySet()) {
				file.append(lineBreak).append(entry.getKey())
						.append(" := ")
						.append(entry.getValue() == null ? "null" : format(entry.getValue(), context))
						.append(lineBreak);
			}
		}
		return file.toString();
	}
}
