package nbo;

import lombok.Getter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class NBOSerializationContext {

    private final List<File> fileIncludes = new ArrayList<>();
    private final Map<String, String> classImports = new HashMap<>();
    private final Map<String, Object> referenceObjects = new HashMap<>();
}
