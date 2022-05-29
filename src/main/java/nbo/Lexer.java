package nbo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.regex.MatchResult;

public class Lexer {

	public record Match(String string, Token token) {
	}

	private final Collection<Token> preserve;
	private final Collection<Token> skip;
	private final Collection<Token> tokens;

	public Lexer() {
		preserve = new HashSet<>();
		skip = new HashSet<>();
		tokens = new HashSet<>();
	}

	public void addPreserve(Token token) {
		preserve.add(token);
	}

	public void addToken(Token token) {
		tokens.add(token);
	}

	public void addSkip(Token token) {
		skip.add(token);
	}

	public List<Match> tokenize(String input) {

		List<Match> result = new ArrayList<>();
		int index = 0;
		int length = input.length();
		String subString;

		while (index < length) {

			subString = input.substring(index);
			int currentIndex = index;

			for (Token token : this.preserve) {
				MatchResult match = token.match(subString);
				if (match != null && match.start() == 0) {
					result.add(new Match(subString.substring(match.start(), match.end()), token));
					index += match.end();
					subString = input.substring(index);
				}
			}

			for (Token skip : this.skip) {
				MatchResult match = skip.match(subString);
				if (match != null && match.start() == 0) {
					index += match.end();
					subString = input.substring(index);
				}
			}

			for (Token token : this.tokens) {
				MatchResult match = token.match(subString);
				if (match != null && match.start() == 0) {
					result.add(new Match(subString.substring(match.start(), match.end()), token));
					index += match.end();
					subString = input.substring(index);
				}
			}
			if(currentIndex == index) {
				index++;
			}
		}
		return result;
	}
}
