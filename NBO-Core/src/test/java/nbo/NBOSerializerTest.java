package nbo;

import nbo.exception.NBOParseException;
import nbo.exception.NBOReferenceException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NBOSerializerTest {

	private static final File TEST_FILE_05 = new File("src/test/resources/test_05.nbo");

	@Test
	public void testPreserveOrder() throws NBOReferenceException, IOException, NBOParseException, ClassNotFoundException {
		NBOFile file = NBOFile.loadFile(TEST_FILE_05, new NBOSerializer());

		List<Integer> orderedList = IntStream.range(1, 21).boxed().collect(Collectors.toCollection(ArrayList::new));
		assertEquals(orderedList, file.getReferenceObjects().get("b"));

		Map<String, Object> orderedMap = new LinkedHashMapBuilder<String, Object>()
				.put("a", 1).put("b", 2).put("c", 3).put("d", 4).put("e", 5).put("f", 6)
				.put("g", 7).put("h", 8).put("i", 9).put("j", 10).build();
		assertEquals(orderedMap, file.getReferenceObjects().get("c"));
	}


}
