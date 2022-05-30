package nbo;

import nbo.tree.*;
import org.junit.jupiter.api.Test;

class TokenizerTest {

	private final String example1 = "<with Tree as OtherTree> assign := Type{key: '&reference', list: ['a', 'x', 'v']}";
	private final String example2 = "<with Tree as OtherTree>\n<with Cow as Pig>\nxy := Type{key: 'a'} assign := Type{key: &xy, other: 'xy', block: {key: 'lulu', mehr: 'hai'}, list2: ['x', 'y', 'z'], list: ['x', {key: 'value'}, 'z', 'a', 'b']}";

	@org.junit.jupiter.api.Test
	void tokenize() throws NBOParseException {
		new NBOParser().getTokenizer().tokenize(example2).forEach(t -> System.out.print(t.string() + "(" + t.token() + ") <-> "));
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

	private void printTree(NBOTree tree, int indent) {
		System.out.println("-".repeat(indent) + tree.getValue());
		for(NBOTree subTree : tree.getSubTrees()) {
			printTree(subTree, indent + 1);
		}
	}
}