package nbo.tree;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Getter
@Setter
@AllArgsConstructor
public class NBOByte implements NBOTree {
    private byte value;

    public NBOByte(String input) {
        if (input.startsWith("'") || input.startsWith("\"")) {
            input = input.substring(1, input.length() - 1);
        }
        input = input.toLowerCase(Locale.ROOT).replace("b", "");
        value = Byte.parseByte(input);
    }

    @Override
    public String toString() {
        return getValueRaw().toString() + "b";
    }

    @Override
    public String toNBTString() {
        return toString();
    }

    @Override
    public Object getValueRaw() {
        return value;
    }

    @Override
    public List<NBOTree> getSubTrees() {
        return new ArrayList<>();
    }
}
