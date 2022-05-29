package nbo;

import lombok.Getter;
import lombok.NonNull;

import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class Token {

	private final String type;
	private final Pattern pattern;

	public Token(String type, Pattern pattern) {
		this.type = type;
		this.pattern = pattern;
	}

	public MatchResult match(String s) {
		Matcher matcher = pattern.matcher(s);
		return matcher.find() ? matcher.toMatchResult() : null;
	}

	@Override public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (!(o instanceof Token)) {
			return false;
		}

		Token token = (Token) o;

		return type.equals(token.type);
	}

	@Override public int hashCode() {
		return type.hashCode();
	}

	@Override public String toString() {
		return type;
	}
}
