package nbo;

import lombok.Getter;
import nbo.tree.*;

import java.util.*;
import java.util.regex.Pattern;

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
    public static final Token ASSIGN = new Token("ASSIGN", Pattern.compile(":="));
    public static final Token KEY = new Token("KEY", Pattern.compile("[a-zA-Z][a-zA-Z0-9_.$]*"));
    public static final Token BOOLEAN = new Token("BOOLEAN", Pattern.compile("([\"']?)((true|false)|([10][bB]))\\1"));
    public static final Token FLOAT = new Token("FLOAT", Pattern.compile("([\"']?)([0-9]+(([fF]|\\.[0-9]+)|\\.))[fF]?\\1"));
    public static final Token INTEGER = new Token("INT", Pattern.compile("([\"']?)(0[xX][0-9a-fA-F]+[iI]?|[0-9]+[iI]?)\\1"));
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

    private String input = "";
    private final Tokenizer tokenizer;
    private final List<Tokenizer.Match> tokenMatches;

    public NBOParser() throws NBOParseException {
        tokenizer = new Tokenizer();
        tokenizer.addPreserve(QUOTE);

        tokenizer.addSkip(LINE_COMMENT);
        tokenizer.addSkip(COMMENT);
        tokenizer.addSkip(SPACE);
        tokenizer.addSkip(NEWLINE);
        tokenizer.addSkip(TAB);
        tokenizer.addSkip(SHRUG1);
        tokenizer.addSkip(SHRUG2);

        tokenizer.addToken(SEPARATOR);
        tokenizer.addToken(ASSIGN);
        tokenizer.addToken(COLON);

        tokenizer.addToken(BOOLEAN);
        tokenizer.addToken(FLOAT);
        tokenizer.addToken(INTEGER);
        tokenizer.addToken(KEY);
        tokenizer.addToken(REFERENCE);

        tokenizer.addToken(BRACKET_OPEN);
        tokenizer.addToken(BRACKET_CLOSE);
        tokenizer.addToken(LIST_OPEN);
        tokenizer.addToken(LIST_CLOSE);
        tokenizer.addToken(WITH_OPEN);
        tokenizer.addToken(WITH_CLOSE);

        this.tokenMatches = new ArrayList<>();
    }

    public NBOParser(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.tokenMatches = new ArrayList<>();
    }

    public void tokenize(String string) throws NBOParseException {
        this.input = string;
        this.tokenMatches.clear();
        this.tokenMatches.addAll(tokenizer.tokenize(string));
    }

    public NBOMap createAST(String input) throws NBOParseException {
        tokenize(input);
        return createAST();
    }

    public NBOMap createAST() throws NBOParseException {
        Stack<Tokenizer.Match> matches = new Stack<>();
        for (int i = tokenMatches.size() - 1; i >= 0; i--) {
            matches.add(tokenMatches.get(i));
        }
        NBOMap imports = new NBOMap();
        while (!matches.empty() && matches.peek().token().equals(WITH_OPEN)) {
            var entry = parseImport(matches);
            imports.put(entry.getKey(), entry.getValue());
        }
        NBOMap objects = new NBOMap();
        while (!matches.empty()) {
            var entry = parseDeclaration(matches);
            objects.put(entry.getKey(), entry.getValue());
        }
//        remapObjectsWithImports(objects, imports);
        NBOMap root = new NBOMap();
        root.put(NBOFile.KEY_IMPORTS, imports);
        root.put(NBOFile.KEY_OBJECTS, objects);
        return root;
    }

    private void remapObjectsWithImports(NBOMap objects, NBOMap imports) {
        for (Map.Entry<String, NBOTree> entry : objects.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (value instanceof NBOObject nboObject) {
                NBOString aliasedType = (NBOString) imports.get(nboObject.getType());
                if (aliasedType != null) {
                    nboObject.setType(aliasedType.getValueRaw());
                }
            } else if (value instanceof NBOMap subMap) {
                remapObjectsWithImports(subMap, imports);
                objects.put(key, subMap);
            }
        }
    }

    private Map.Entry<String, NBOString> parseImport(Stack<Tokenizer.Match> tokens) throws NBOParseException {
        if (tokens.empty() || !tokens.peek().token().equals(WITH_OPEN)) {
            throw new NBOParseException("An import segment has to start with '<'.", input, tokens.peek().startIndex());
        }
        tokens.pop();
        Tokenizer.Match with;
        if (!tokens.empty() && tokens.peek().token().equals(KEY)) {
            with = tokens.pop();
            if (!with.string().equalsIgnoreCase("with")) {
                throw new NBOParseException("An import segment has to be formatted <with [alias] as [classname]>.", input, with.startIndex());
            }
        } else {
            throw new NBOParseException("An import segment has to be formatted <with [alias] as [classname]>.");
        }
        Tokenizer.Match alias;
        if (!tokens.empty() && tokens.peek().token().equals(KEY)) {
            alias = tokens.pop();
        } else {
            throw new NBOParseException("An import segment has to be formatted <with [alias] as [classname]>.");
        }
        if (!tokens.empty() && tokens.peek().token().equals(KEY)) {
            Tokenizer.Match as = tokens.pop();
            if (!as.string().equalsIgnoreCase("as")) {
                throw new NBOParseException("An import segment has to be formatted <with [alias] as [classname]>.");
            }
        } else {
            throw new NBOParseException("An import segment has to be formatted <with [alias] as [classname]>.");
        }
        Tokenizer.Match className;
        if (!tokens.empty() && tokens.peek().token().equals(KEY)) {
            className = tokens.pop();
        } else {
            throw new NBOParseException("An import segment has to be formatted <with [alias] as [classname]>.");
        }
        if (tokens.empty() || !tokens.pop().token().equals(WITH_CLOSE)) {
            throw new NBOParseException("An import segment has to be closed with '>'.", input, className.startIndex() + className.string().length());
        }
        return new AbstractMap.SimpleEntry<>(alias.string(), new NBOString(className.string()));
    }

    private Map.Entry<String, NBOTree> parseDeclaration(Stack<Tokenizer.Match> tokens) throws NBOParseException {
        if (tokens.empty() || !tokens.peek().token().equals(KEY)) {
            throw new NBOParseException("A declaration must be of format [name] := [type]{[data...]}.", input, tokens.empty() ? input.length() : tokens.peek().startIndex());
        }
        Tokenizer.Match object = tokens.pop();
        if (tokens.empty() || !tokens.pop().token().equals(ASSIGN)) {
            throw new NBOParseException("A declaration must be of format [name] := [type]{[data...]}.", input, tokens.empty() ? input.length() : tokens.peek().startIndex());
        }
        return new AbstractMap.SimpleEntry<>(object.string(), parseObject(tokens));
    }

    private NBOObject parseObject(Stack<Tokenizer.Match> tokens) throws NBOParseException {
        if (tokens.empty() || !tokens.peek().token().equals(KEY)) {
            throw new NBOParseException("An object segment must start with a type specification [type]{[data...]}", input, tokens.empty() ? input.length() : tokens.peek().startIndex());
        }
        NBOObject obj = new NBOObject(tokens.pop().string());
        obj.putAll(parseBlock(tokens));
        return obj;
    }

    private NBOMap parseBlock(Stack<Tokenizer.Match> tokens) throws NBOParseException {
        if (tokens.empty() || !tokens.peek().token().equals(BRACKET_OPEN)) {
            throw new NBOParseException("A block must be surrounded with '{' and '}'.", input, tokens.empty() ? input.length() : tokens.peek().startIndex());
        }
        tokens.pop();

        NBOMap tree = new NBOMap();
        boolean noComma = false;
        while (!tokens.empty() && !tokens.peek().token().equals(BRACKET_CLOSE)) {
            if (noComma) {
                throw new NBOParseException("Entry elements must be separated with a ','.", input, tokens.empty() ? input.length() : tokens.peek().startIndex());
            }
            var entry = parseEntry(tokens);
            tree.put(entry.getKey(), entry.getValue());
            if (tokens.empty()) {
                throw new NBOParseException("Missing entry between separator and closing brackets.", input, input.length());
            }
            if (!tokens.peek().token().equals(SEPARATOR)) {
                noComma = true;
            } else {
                tokens.pop();
            }
        }
        if (tokens.empty()) {
            throw new NBOParseException("A block must be surrounded with '{' and '}'.", input, input.length());
        }
        tokens.pop();
        return tree;
    }

    private Map.Entry<String, NBOTree> parseEntry(Stack<Tokenizer.Match> tokens) throws NBOParseException {
        if (tokens.empty() || !tokens.peek().token().equals(KEY)) {
            throw new NBOParseException("An entry segment must start with a key.", input, tokens.empty() ? input.length() : tokens.peek().startIndex());
        }
        Tokenizer.Match key = tokens.pop();
        if (tokens.empty() || !tokens.pop().token().equals(COLON)) {
            throw new NBOParseException("An entry segment must be defined with a colon.", input, tokens.empty() ? input.length() : tokens.peek().startIndex());
        }
        return new AbstractMap.SimpleEntry<>(key.string(), parseValue(tokens));
    }

    private NBOTree parseValue(Stack<Tokenizer.Match> tokens) throws NBOParseException {
        if (tokens.empty()) {
            throw new NBOParseException("Could not parse value, no token found.", input, input.length());
        }
        Token t = tokens.peek().token();
        if (t.equals(KEY)) {
            return parseObject(tokens);
        } else if (t.equals(QUOTE)) {
            return new NBOString(tokens.pop().string());
        } else if (t.equals(BOOLEAN)) {
            return new NBOBool(tokens.pop().string());
        } else if (t.equals(FLOAT)) {
            return new NBOFloat(tokens.pop().string());
        } else if (t.equals(INTEGER)) {
            return new NBOInteger(tokens.pop().string());
        } else if (t.equals(REFERENCE)) {
            return new NBOReference(tokens.pop().string().substring(1));
        } else if (t.equals(BRACKET_OPEN)) {
            return parseBlock(tokens);
        } else if (t.equals(LIST_OPEN)) {
            return parseList(tokens);
        }
        throw new NBOParseException("The specified value '" + tokens.peek().string() + "' is of no known type.", input, tokens.peek().startIndex());
    }

    private NBOTree parseList(Stack<Tokenizer.Match> tokens) throws NBOParseException {
        if (tokens.empty() || !tokens.peek().token().equals(LIST_OPEN)) {
            throw new NBOParseException("List segments must be opened with a '['.", input, tokens.empty() ? input.length() : tokens.peek().startIndex());
        }
        tokens.pop();

        NBOList tree = new NBOList();
        boolean noComma = false;
        while (!tokens.empty() && !tokens.peek().token().equals(LIST_CLOSE)) {
            if (noComma) {
                throw new NBOParseException("List elements must be separated with a comma.", input, tokens.peek().startIndex());
            }
            tree.add(parseValue(tokens));
            if (tokens.empty()) {
                throw new NBOParseException("No value found between separator and closing brackets.");
            }
            if (!tokens.peek().token().equals(SEPARATOR)) {
                noComma = true;
            } else {
                tokens.pop();
            }
        }
        if (tokens.empty()) {
            throw new NBOParseException("Lists must be closed with ']'.", input, input.length());
        }
        tokens.pop();
        return tree;
    }
}
