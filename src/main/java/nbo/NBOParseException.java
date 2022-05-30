package nbo;

public class NBOParseException extends Exception {

	public NBOParseException(String message) {
		super(message);
	}

	public NBOParseException(String message, String input, int index) {
		super(message + (index >= 10 ? "\n... " : "\n    ") + " ".repeat(Integer.max(0, 10 - index)) + input.substring(Integer.max(0, index - 10), Integer.min(input.length(), index + 10)).replace("\n", "\\n")
		+ " ... \n" + " ".repeat(14) + "^ (index: " + index + ")");
	}
}
