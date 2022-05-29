package nbo;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NBOParser {

	public static final Token QUOTE = new Token("QUOTATION", Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1"));
	public static final Token LINE_COMMENT = new Token("LINE_COMMENT", Pattern.compile("#[^\n\r]*(\n|\r|\r\n)"));
	public static final Token COMMENT = new Token("INLINE_COMMENT", Pattern.compile("#-[^#]*-#"));
	public static final Token SPACE = new Token("SPACE", Pattern.compile("\s+"));
	public static final Token NEWLINE = new Token("NEWLINE", Pattern.compile("\n"));
	public static final Token TAB = new Token("TAB", Pattern.compile("\t"));
	public static final Token SHRUG1 = new Token("NO_IDEA", Pattern.compile("\r"));
	public static final Token SHRUG2 = new Token("NO_IDEA", Pattern.compile("\b"));
	public static final Token REFERENCE = new Token("REFERENCE", Pattern.compile("&[a-zA-Z][a-zA-Z0-9_.]*"));
	public static final Token ASSIGN = new Token("ASSIGN", Pattern.compile("[a-zA-Z][a-zA-Z0-9_.]*:="));
	public static final Token KEY = new Token("KEY", Pattern.compile("[a-zA-Z][a-zA-Z0-9_.]*:"));
	public static final Token SEPARATOR = new Token("SEPARATOR", Pattern.compile(","));
	public static final Token BRACKET_OPEN = new Token("BRACKET_OPEN", Pattern.compile("\\{"));
	public static final Token BRACKET_CLOSE = new Token("BRACKET_CLOSE", Pattern.compile("}"));
	public static final Token LIST_OPEN = new Token("LIST_OPEN", Pattern.compile("\\["));
	public static final Token LIST_CLOSE = new Token("LIST_CLOSE", Pattern.compile("]"));

	public record ASTNode(Token a, Token b) {

	}

	public NBOParser(String input) throws NBOParseException {
		Lexer lexer = new Lexer();
		lexer.addPreserve(QUOTE);
		lexer.addSkip(LINE_COMMENT);
		lexer.addSkip(COMMENT);
		lexer.addSkip(SPACE);
		lexer.addSkip(NEWLINE);
		lexer.addSkip(TAB);
		lexer.addSkip(SHRUG1);
		lexer.addSkip(SHRUG2);
		lexer.addToken(SEPARATOR);
		lexer.addToken(ASSIGN);
		lexer.addToken(KEY);
		lexer.addToken(REFERENCE);
		lexer.addToken(BRACKET_OPEN);
		lexer.addToken(BRACKET_CLOSE);
		lexer.addToken(LIST_OPEN);
		lexer.addToken(LIST_CLOSE);

		createAST(lexer.tokenize(input));
	}

	public void createAST(List<Lexer.Match> matchList) throws NBOParseException {

		Stack<Lexer.Match> matches = new Stack<>();
		for (int i = matchList.size() - 1; i >= 0; i--) {
			matches.add(matchList.get(i));
		}
		NBOObjectTree tree = new NBOObjectTree();
		while (!matches.empty()) {
			tree.addObject(parseDeclaration(matches));
		}
		System.out.println(tree.toString(0));
	}

	private NBOTree parseDeclaration(Stack<Lexer.Match> tokens) throws NBOParseException {
		if (tokens.empty() || !tokens.peek().token().equals(ASSIGN)) {
			throw new NBOParseException(""); //TODO
		}
		return new NBODeclarationTree(tokens.pop(), parseBlock(tokens));
	}

	private NBOTree parseBlock(Stack<Lexer.Match> tokens) throws NBOParseException {
		if (tokens.empty() || !tokens.peek().token().equals(BRACKET_OPEN)) {
			throw new NBOParseException(""); //TODO throw
		}
		tokens.pop();

		NBOEntriesTree tree = new NBOEntriesTree();
		boolean noComma = false;
		while (!tokens.empty() && !tokens.peek().token().equals(BRACKET_CLOSE)) {
			if (noComma) {
				throw new NBOParseException(""); //TODO
			}
			tree.addEntry(parseEntry(tokens));
			if (tokens.empty()) {
				throw new NBOParseException(""); //TODO
			}
			if (!tokens.peek().token().equals(SEPARATOR)) {
				noComma = true;
			} else {
				tokens.pop();
			}
		}
		if (tokens.empty()) {
			throw new NBOParseException(""); //TODO
		}
		tokens.pop();
		return tree;
	}

	private NBOTree parseEntry(Stack<Lexer.Match> tokens) throws NBOParseException {
		if (tokens.empty() || !tokens.peek().token().equals(KEY)) {
			throw new NBOParseException("" + tokens.peek().token()); //TODO
		}
		return new NBOEntryTree(tokens.pop(), parseValue(tokens));
	}

	private NBOTree parseValue(Stack<Lexer.Match> tokens) throws NBOParseException {
		if (tokens.empty()) {
			throw new NBOParseException(""); //TODO
		}
		Token t = tokens.peek().token();
		if (t.equals(QUOTE)) {
			return new NBOPrimitive(tokens.pop());
		} else if (t.equals(REFERENCE)) {
			return new NBOReference(tokens.pop());
		} else if (t.equals(BRACKET_OPEN)) {
			return parseBlock(tokens);
		} else if (t.equals(LIST_OPEN)) {
			return parseList(tokens);
		}
		throw new NBOParseException("");
	}

	private NBOTree parseList(Stack<Lexer.Match> tokens) throws NBOParseException {
		if (tokens.empty() || !tokens.peek().token().equals(LIST_OPEN)) {
			throw new NBOParseException(""); //TODO throw
		}
		tokens.pop();

		NBOValuesTree tree = new NBOValuesTree();
		boolean noComma = false;
		while (!tokens.empty() && !tokens.peek().token().equals(LIST_CLOSE)) {
			if (noComma) {
				throw new NBOParseException(""); //TODO
			}
			tree.addValue(parseValue(tokens));
			if (tokens.empty()) {
				throw new NBOParseException(""); //TODO
			}
			if (!tokens.peek().token().equals(SEPARATOR)) {
				noComma = true;
			} else {
				tokens.pop();
			}
		}
		if (tokens.empty()) {
			throw new NBOParseException(""); //TODO
		}
		tokens.pop();
		return tree;
	}

	private NBOTree parsePrimitive(Stack<Lexer.Match> tokens) throws NBOParseException {
		if (tokens.empty() || !tokens.peek().token().equals(QUOTE)) {
			throw new NBOParseException(""); //TODO
		}
		return new NBOPrimitive(tokens.pop());
	}

	public abstract class NBOTree {
		abstract String toString(int indent);
	}

	@RequiredArgsConstructor
	public class NBODeclarationTree extends NBOTree {
		private final Lexer.Match declaration;
		private final NBOTree block;

		public String toString(int indent) {
			return "\n" + "-".repeat(indent) + declaration.string() + block.toString(indent + 1);
		}
	}

	public class NBOObjectTree extends NBOTree {
		private final List<NBOTree> objects;

		public NBOObjectTree(NBOTree... objects) {
			this.objects = new ArrayList<>();
			this.objects.addAll(List.of(objects));
		}

		public void addObject(NBOTree object) {
			objects.add(object);
		}

		@Override String toString(int indent) {
			return objects.stream().map(t -> t.toString(indent + 1)).collect(Collectors.joining());
		}
	}

	public class NBOEntriesTree extends NBOTree {
		private final List<NBOTree> entries;

		public NBOEntriesTree(NBOTree... entries) {
			this.entries = new ArrayList<>();
			this.entries.addAll(List.of(entries));
		}

		public void addEntry(NBOTree entry) {
			entries.add(entry);
		}

		@Override String toString(int indent) {
			return entries.stream().map(t -> t.toString(indent + 1)).collect(Collectors.joining());
		}
	}

	public class NBOValuesTree extends NBOTree {
		private final List<NBOTree> values;

		public NBOValuesTree(NBOTree... values) {
			this.values = new ArrayList<>();
			this.values.addAll(List.of(values));
		}

		public void addValue(NBOTree value) {
			values.add(value);
		}

		@Override String toString(int indent) {
			return values.stream().map(t -> t.toString(indent + 1)).collect(Collectors.joining());
		}
	}

	@RequiredArgsConstructor
	public class NBOEntryTree extends NBOTree {
		private final Lexer.Match key;
		private final NBOTree value;

		@Override String toString(int indent) {
			return "\n" + "-".repeat(indent) + key.string() + value.toString(indent + 1);
		}
	}

	@RequiredArgsConstructor
	public class NBOPrimitive extends NBOTree {
		private final Lexer.Match primitive;

		@Override String toString(int indent) {
			return "\n" + "-".repeat(indent) + primitive.string();
		}
	}

	@RequiredArgsConstructor
	public class NBOReference extends NBOTree {
		private final Lexer.Match reference;

		@Override String toString(int indent) {
			return "\n" + "-".repeat(indent) + reference.string();
		}
	}
}
