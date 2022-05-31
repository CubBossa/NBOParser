package nbo;

import nbo.tree.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

class TokenizerTest {

	private final String example1 = "<with Tree as OtherTree> assign := Type{key: '&reference', list: ['a', 'x', 'v']}";
	private final String example2 = "<with Tree as OtherTree>\n<with Cow as Pig>\nxy := Type{key: 'a'}  assign := Type{key: &xy, other: 'xy', block: {key: 'lulu', mehr: 'hai'}, list2: [1b, 0B, true], list: ['x', {key: 'value'}, 'z', 'a', 'b']}";

	@org.junit.jupiter.api.Test
	void tokenize() throws NBOParseException {
		new NBOParser().getTokenizer().tokenize("assign := Type{float: 1.0f, int: 0x1f0, bool: 1b}").forEach(t -> System.out.print(t.string() + "(" + t.token() + ") <-> "));
	}

	@Test
	void parse() throws NBOParseException {
		NBOParser parser = new NBOParser();
		parser.tokenize(example2);
		System.out.println(parser.createAST().pretty("|  "));
	}

	@Test
	public void printFile() throws NBOParseException {
		NBOParser parser = new NBOParser();
		parser.tokenize(example2);
		System.out.println(NBOFile.formatToFileString(parser.createAST()));
	}

	@Test
	public void readFile() throws IOException, NBOParseException, ClassNotFoundException {
		NBOFile.register(File.class, s -> new File((String) s.get("path")), f -> Map.of("path", f.getAbsolutePath()));
		NBOFile file = NBOFile.loadFile(new File("src/main/resources/test.nbo"));
		file.setObject("Afile", new File("src/main/resources/test.nbo"));
		System.out.println(NBOFile.formatToFileString(file.getRoot()));
	}

	private void printTree(NBOTree tree, int indent) {
		System.out.println("-".repeat(indent) + tree.getValue());
		for(NBOTree subTree : tree.getSubTrees()) {
			printTree(subTree, indent + 1);
		}
	}
}