package nbo;

import org.junit.jupiter.api.Test;

import java.util.regex.Pattern;

class LexerTest {

	@org.junit.jupiter.api.Test
	void tokenize() throws NBOParseException {

		Lexer lexer = new Lexer();
		lexer.addPreserve(NBOParser.QUOTE);
		lexer.addSkip(NBOParser.LINE_COMMENT);
		lexer.addSkip(NBOParser.COMMENT);
		lexer.addSkip(NBOParser.SPACE);
		lexer.addSkip(NBOParser.NEWLINE);
		lexer.addSkip(NBOParser.TAB);
		lexer.addSkip(NBOParser.SHRUG1);
		lexer.addSkip(NBOParser.SHRUG2);
		lexer.addToken(NBOParser.SEPARATOR);
		lexer.addToken(NBOParser.ASSIGN);
		lexer.addToken(NBOParser.KEY);
		lexer.addToken(NBOParser.REFERENCE);
		lexer.addToken(NBOParser.BRACKET_OPEN);
		lexer.addToken(NBOParser.BRACKET_CLOSE);
		lexer.addToken(NBOParser.LIST_OPEN);
		lexer.addToken(NBOParser.LIST_CLOSE);

		lexer.tokenize("assign:= {key: &reference}").forEach(match -> System.out.println(match.string() + " -> " + match.token()));
	}

	@Test
	void parse() throws NBOParseException {

		new NBOParser("assign:= {key: '&reference'}");
		new NBOParser("assign:= {key: &reference, other: 'xy', block: {key: 'lulu', mehr: 'hai'}, list: ['x', 'y', 'z']}");
	}
}