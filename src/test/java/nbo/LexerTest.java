package nbo;

import org.junit.jupiter.api.Test;

class LexerTest {

	@org.junit.jupiter.api.Test
	void tokenize() throws NBOParseException {

	}

	@Test
	void parse() throws NBOParseException {

		new NBOParser().createAST(new NBOParser().getLexer().tokenize("<with Tree as OtherTree> assign := Type{key: '&reference'}")).lined().forEach(tree -> System.out.println(tree.getClass()));
		System.out.println(new NBOParser().createAST(new NBOParser().getLexer().tokenize("<with Tree as OtherTree><with Cow as Pig>xy := Type{key: 'value'} assign := Type{key: &xy, other: 'xy', block: {key: 'lulu', mehr: 'hai'}, list: ['x', 'y', 'z']}")).toString(0));
	}
}