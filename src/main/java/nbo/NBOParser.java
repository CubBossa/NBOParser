package nbo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class NBOParser {

	public static final Token QUOTE = new Token("QUOTATION", Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1"));
	public static final Token LINE_COMMENT = new Token("LINE_COMMENT", Pattern.compile("#[^\n\r]*(\n|\r|\r\n)"));
	public static final Token COMMENT = new Token("INLINE_COMMENT", Pattern.compile("#-[^#]*-#"));
	public static final Token SPACE = new Token("SPACE", Pattern.compile("\s+"));
	public static final Token NEWLINE = new Token("NEWLINE", Pattern.compile("\n"));
	public static final Token TAB = new Token("TAB", Pattern.compile("\t"));
	public static final Token SHRUG1 = new Token("NO_IDEA", Pattern.compile("\r"));
	public static final Token SHRUG2 = new Token("NO_IDEA", Pattern.compile("\b"));
	public static final Token COLON = new Token("COLON", Pattern.compile(":"));
	public static final Token ASSIGN = new Token("COLON", Pattern.compile(":="));
	public static final Token KEY = new Token("KEY", Pattern.compile("[a-zA-Z][a-zA-Z0-9_.$]*"));
	public static final Token REFERENCE = new Token("REFERENCE", Pattern.compile("&[a-zA-Z][a-zA-Z0-9_.$]*"));
	public static final Token SEPARATOR = new Token("SEPARATOR", Pattern.compile(","));
	public static final Token BRACKET_OPEN = new Token("BRACKET_OPEN", Pattern.compile("\\{"));
	public static final Token BRACKET_CLOSE = new Token("BRACKET_CLOSE", Pattern.compile("}"));
	public static final Token LIST_OPEN = new Token("LIST_OPEN", Pattern.compile("\\["));
	public static final Token LIST_CLOSE = new Token("LIST_CLOSE", Pattern.compile("]"));
	public static final Token WITH_OPEN = new Token("WITH_OPEN", Pattern.compile("<"));
	public static final Token WITH_CLOSE = new Token("WITH_CLOSE", Pattern.compile(">"));
	public static final Token WITH = new Token("WITH", Pattern.compile("with"));
	public static final Token AS = new Token("AS", Pattern.compile("as"));

	private static final Collection<Class<? extends NBOSerializable>> SERIALIZABLES = new HashSet<>();

	private final Lexer lexer;
	private final List<Lexer.Match> tokenMatches;

	public NBOParser() throws NBOParseException {
		lexer = new Lexer();
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
		lexer.addToken(COLON);

		lexer.addToken(WITH);
		lexer.addToken(AS);
		lexer.addToken(KEY);
		lexer.addToken(REFERENCE);

		lexer.addToken(BRACKET_OPEN);
		lexer.addToken(BRACKET_CLOSE);
		lexer.addToken(LIST_OPEN);
		lexer.addToken(LIST_CLOSE);
		lexer.addToken(WITH_OPEN);
		lexer.addToken(WITH_CLOSE);

		this.tokenMatches = new ArrayList<>();
	}

	public static void register(Class<? extends NBOSerializable> serializable) {
		SERIALIZABLES.add(serializable);
	}

	public static void unregister(Class<? extends NBOSerializable> serializable) {
		SERIALIZABLES.add(serializable);
	}

	public NBOParser(Lexer lexer) {
		this.lexer = lexer;
		this.tokenMatches = new ArrayList<>();
	}

	public Map<String, Object> parse(String string) throws NBOParseException {
		tokenize(string);
		return new NBOInterpreter().interpret(createAST(tokenMatches));
	}

	public void tokenize(String string) {
		this.tokenMatches.clear();
		this.tokenMatches.addAll(lexer.tokenize(string));
	}

	public NBOObjectTree createAST(List<Lexer.Match> matchList) throws NBOParseException {

		Stack<Lexer.Match> matches = new Stack<>();
		for (int i = matchList.size() - 1; i >= 0; i--) {
			matches.add(matchList.get(i));
		}
		NBOObjectTree tree = new NBOObjectTree();
		while (!matches.empty()) {
			tree.addObject(parseDeclaration(matches));
		}
		return tree;
	}

	private NBOTree parseDeclaration(Stack<Lexer.Match> tokens) throws NBOParseException {
		if (tokens.empty() || !tokens.peek().token().equals(KEY)) {
			throw new NBOParseException(""); //TODO
		}
		Lexer.Match object = tokens.pop();
		if (tokens.empty() || !tokens.pop().token().equals(ASSIGN)) {
			throw new NBOParseException(""); //TODO
		}
		if (tokens.empty() || !tokens.peek().token().equals(KEY)) {
			throw new NBOParseException(""); //TODO
		}
		return new NBODeclarationTree(object.string(), tokens.pop().string(), parseBlock(tokens));
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
			throw new NBOParseException(""); //TODO
		}
		Lexer.Match key = tokens.pop();
		if (tokens.empty() || !tokens.pop().token().equals(COLON)) {
			throw new NBOParseException(""); //TODO
		}
		return new NBOEntryTree(key, parseValue(tokens));
	}

	private NBOTree parseValue(Stack<Lexer.Match> tokens) throws NBOParseException {
		if (tokens.empty()) {
			throw new NBOParseException(""); //TODO
		}
		Token t = tokens.peek().token();
		if (t.equals(QUOTE)) {
			return new NBOPrimitive(tokens.pop());
		} else if (t.equals(REFERENCE)) {
			return new NBOReference(tokens.pop().string());
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
		public abstract String toString(int indent);

		public abstract List<NBOTree> lined();
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public class NBODeclarationTree extends NBOTree {
		private String declaration;
		private String type;
		private final NBOTree block;

		public String toString(int indent) {
			return "\n" + "-".repeat(indent) + declaration + " := " + type + block.toString(indent + 1);
		}

		public List<NBOTree> lined() {
			return block.lined();
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

		public String toString(int indent) {
			return objects.stream().map(t -> t.toString(indent + 1)).collect(Collectors.joining());
		}

		public List<NBOTree> lined() {
			return addAndReturn(objects.stream().flatMap((Function<NBOTree, Stream<NBOTree>>) tree -> tree.lined().stream()).collect(Collectors.toList()), this);
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

		public String toString(int indent) {
			return entries.stream().map(t -> t.toString(indent + 1)).collect(Collectors.joining());
		}

		public List<NBOTree> lined() {
			return addAndReturn(entries.stream().flatMap((Function<NBOTree, Stream<NBOTree>>) tree -> tree.lined().stream()).collect(Collectors.toList()), this);
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

		public String toString(int indent) {
			return values.stream().map(t -> t.toString(indent + 1)).collect(Collectors.joining());
		}

		public List<NBOTree> lined() {
			return addAndReturn(values.stream().flatMap((Function<NBOTree, Stream<NBOTree>>) tree -> tree.lined().stream()).collect(Collectors.toList()), this);
		}
	}

	@RequiredArgsConstructor
	public class NBOEntryTree extends NBOTree {
		private final Lexer.Match key;
		private final NBOTree value;

		public String toString(int indent) {
			return "\n" + "-".repeat(indent) + key.string() + value.toString(indent + 1);
		}

		public List<NBOTree> lined() {
			return addAndReturn(value.lined(), this);
		}
	}

	@RequiredArgsConstructor
	public class NBOPrimitive extends NBOTree {
		private final Lexer.Match primitive;

		public String toString(int indent) {
			return "\n" + "-".repeat(indent) + primitive.string();
		}

		@Override public List<NBOTree> lined() {
			return addAndReturn(new ArrayList<>(), this);
		}
	}

	@Getter
	@Setter
	@AllArgsConstructor
	public class NBOReference extends NBOTree {
		private String reference;

		public String toString(int indent) {
			return "\n" + "-".repeat(indent) + reference;
		}

		@Override public List<NBOTree> lined() {
			return addAndReturn(new ArrayList<>(), this);
		}
	}

	private static <T> List<T> addAndReturn(List<T> list, T element) {
		list.add(element);
		return list;
	}
}
