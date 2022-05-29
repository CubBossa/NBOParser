package nbo;

import org.junit.jupiter.api.Test;

class LexerTest {

	@org.junit.jupiter.api.Test
	void tokenize() throws NBOParseException {

	}

	@Test
	void parse() throws NBOParseException {

		new NBOParser().createAST(new NBOParser().getLexer().tokenize("assign:= Type{key: &reference}")).lined().forEach(tree -> System.out.println(tree.getClass()));
		new NBOParser().parse("assign:= Type{key: &reference, other: 'xy', block: {key: 'lulu', mehr: 'hai'}, list: ['x', 'y', 'z']}");
	}
}