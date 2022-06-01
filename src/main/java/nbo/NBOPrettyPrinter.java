package nbo;

import lombok.Getter;
import lombok.Setter;
import nbo.tree.*;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Getter
@Setter
public class NBOPrettyPrinter {

	private int lineCollapseLength = 60;
	private int lineWrapLength = 80;
	private String indent = "    ";
	private String lineBreak = "\n";
	private final Map<Class<?>, BiFunction<NBOTree, String, String>> prettyMethods;

	public NBOPrettyPrinter() {

		BiFunction<NBOMap, String, String> mapString = (tree, indent) -> {
			boolean breakLines = tree.size() > 1;
			StringBuilder s = new StringBuilder("{");
			if (breakLines) {
				s.append(lineBreak).append(indent);
			}
			s.append(tree.entrySet().stream()
					.map(e -> e.getKey() + ": " + format(e.getValue(), indent).replace(lineBreak, lineBreak + indent))
					.collect(Collectors.joining("," + lineBreak + indent)));
			if (breakLines) {
				s.append(lineBreak);
			}
			s.append("}");
			return s.toString();
		};

		this.prettyMethods = Map.of(
				NBOString.class, (tree, string) -> tree.toString(),
				NBOInteger.class, (tree, string) -> tree.toString(),
				NBOFloat.class, (tree, string) -> tree.toString(),
				NBOBool.class, (tree, string) -> tree.toString(),
				NBOReference.class, (tree, string) -> tree.toString(),
				NBOObject.class, (tree, string) -> ((NBOObject) tree).getType() + mapString.apply((NBOMap) tree, string),
				NBOMap.class, (tree, string) -> mapString.apply((NBOMap) tree, string),
				NBOList.class, (tree, string) -> {
					NBOList list = (NBOList) tree;
					return list.size() > 4 ?
							"[" + lineBreak + indent + list.stream().map(t -> format(t, indent).replace(lineBreak, lineBreak + indent)).collect(Collectors.joining("," + lineBreak + indent)) + lineBreak + "]" :
							"[" + list.stream().map(t -> format(t, indent).replace(lineBreak, lineBreak + indent)).collect(Collectors.joining(", ")) + "]";
				}
		);
	}

	public String format(NBOTree tree) {
		return prettyMethods.getOrDefault(tree.getClass(), (t, s) -> "NO PRETTY METHOD PROVIDED").apply(tree, indent);
	}

	public String format(NBOTree tree, String indent) {
		return prettyMethods.getOrDefault(tree.getClass(), (t, s) -> "NO PRETTY METHOD PROVIDED").apply(tree, indent);
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
