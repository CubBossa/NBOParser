package nbo;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TokenizerTest {

	@Test
	void tokenizeSimpleList() {

	}

	@Test
	void tokenizeValid() throws NBOParseException {

		String input = "assign := Type {float: 1.0f, int: 0x1f0, bool: 1b}";
		List<Token> output = List.of(
				NBOParser.KEY, NBOParser.ASSIGN, NBOParser.KEY, NBOParser.BRACKET_OPEN,
				NBOParser.KEY, NBOParser.COLON, NBOParser.FLOAT, NBOParser.SEPARATOR,
				NBOParser.KEY, NBOParser.COLON, NBOParser.INTEGER, NBOParser.SEPARATOR,
				NBOParser.KEY, NBOParser.COLON, NBOParser.BOOLEAN, NBOParser.BRACKET_CLOSE
		);
		assertEquals(new NBOParser().getTokenizer().tokenize(input).stream().map(Tokenizer.Match::token).toList(), output);
	}

	@Test
	void tokenizeInvalid() throws NBOParseException {

		String input = "{1b],:=x'}'p_p";
		List<Token> output = List.of(
				NBOParser.BRACKET_OPEN, NBOParser.BOOLEAN, NBOParser.LIST_CLOSE,
				NBOParser.SEPARATOR, NBOParser.ASSIGN, NBOParser.KEY, NBOParser.QUOTE, NBOParser.KEY
		);
		assertEquals(new NBOParser().getTokenizer().tokenize(input).stream().map(Tokenizer.Match::token).toList(), output);
	}

	@Test
	void tokenizeNotAToken() {

		String input = "{1b]ß,:=x'}'p_p";
		assertThrows(NBOParseException.class, () -> new NBOParser().getTokenizer().tokenize(input));
	}

	@Test
	void tokenizeComment() throws NBOParseException {

		String input = "#-{1b]ß-##-,:=x'}'p_p-#";
		assertEquals(new NBOParser().getTokenizer().tokenize(input).stream().map(Tokenizer.Match::token).toList(), new ArrayList<>());
	}

}