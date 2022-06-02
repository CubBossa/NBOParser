package nbo;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@Getter
public class NBOSerializationContext {

    private final Map<String, String> classImports = new HashMap<>();
    private final Map<String, Object> referenceObjects = new HashMap<>();

}
