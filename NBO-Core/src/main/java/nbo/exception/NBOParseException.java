package nbo.exception;

public class NBOParseException extends NBOException {

    public NBOParseException(String message) {
        super(message);
    }

    public NBOParseException(String message, String input, int index) {
        super(message + (index >= 10 ? "\n... " : "\n    ") + " ".repeat(Integer.max(0, 10 - index)) + input.substring(Integer.max(0, index - 10), Integer.min(input.length(), index + 10)).replace("\n", "\\n")
                + " ... \n" + " ".repeat(14) + "^ (line: " + (input.substring(0, index).chars().filter(c -> c == '\n').count() + 1) +
                ", col:" + inLineIndex(input, index) + ")");
    }

    private static int inLineIndex(String input, int index) {
        int i = 0;
        while (i < index && input.charAt(index - i) != '\n') {
            i++;
        }
        return i;
    }
}
