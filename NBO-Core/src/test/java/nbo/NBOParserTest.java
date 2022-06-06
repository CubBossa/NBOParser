package nbo;

import nbo.exception.NBOParseException;
import nbo.exception.NBOReferenceException;
import nbo.tree.NBOMap;
import nbo.tree.NBOTree;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NBOParserTest {

	public record Vector3f(float x, float y, float z) {
		public static Vector3f deserialize(Map<String, ?> map) {
			return new Vector3f(
					(float) map.get("x"),
					(float) map.get("y"),
					(float) map.get("z")
			);
		}

		public Map<String, Object> serialize() {
			return new LinkedHashMapBuilder<String, Object>().put("x", x).put("y", y).put("z", z).build();
		}
	}

	public record Matrix3f(Vector3f col1, Vector3f col2, Vector3f col3) {
		public static Matrix3f deserialize(Map<String, ?> map) {
			return new Matrix3f(
					(Vector3f) map.get("col1"),
					(Vector3f) map.get("col2"),
					(Vector3f) map.get("col3")
			);
		}

		public Map<String, Object> serialize() {
			return new LinkedHashMapBuilder<String, Object>().put("col1", col1).put("col2", col2).put("col3", col3).build();
		}
	}

	private static final File TEST_FILE_VECTOR = new File("src/test/resources/vector_test.nbo");

	@Test
	void parseStringAssignment() throws NBOParseException, ClassNotFoundException {
		NBOSerializer serializer = NBOFile.DEFAULT_SERIALIZER;
		Map<String, Object> map = serializer.deserialize((NBOTree) new NBOParser().createAST("test-string := 'a test'"), new NBOSerializationContext());
		Map<String, Object> objects = (Map<String, Object>) map.get(NBOFile.KEY_OBJECTS);
		assertEquals(String.class, objects.get("test-string").getClass());
	}

	@Test
	void parseMapAssignment() throws NBOParseException, ClassNotFoundException {
		NBOSerializer serializer = NBOFile.DEFAULT_SERIALIZER;
		Map<String, Object> map = serializer.deserialize((NBOTree) new NBOParser().createAST("list := [0, 1, 2]"), new NBOSerializationContext());
		Map<String, Object> objects = (Map<String, Object>) map.get(NBOFile.KEY_OBJECTS);
		assertEquals(ArrayList.class, objects.get("list").getClass());
	}

	@Test
	void parseHashSet() throws NBOParseException, ClassNotFoundException {
		NBOSerializer serializer = NBOFile.DEFAULT_SERIALIZER;
		Map<String, Object> map = serializer.deserialize((NBOTree) new NBOParser().createAST("list := java.util.HashMap {a: 'b', c: 'd'}"), new NBOSerializationContext());
		Map<String, Object> objects = (Map<String, Object>) map.get(NBOFile.KEY_OBJECTS);
		assertEquals(HashMap.class, objects.get("list").getClass());
	}

	@Test
	void parse() throws NBOParseException {
		NBOParser parser = new NBOParser();
		NBOMap ast = parser.createAST("""
				<with Vec as nbo.NBOParserTest$Vector3f>
				<with Mat as nbo.NBOParserTest$Matrix3f>

				unit_1 := Vec {x: 1.0, y: 0.0, z: 0.0}
				""");

		NBOMap objects = ((NBOMap) ast.getOrDefault(NBOFile.KEY_OBJECTS, new NBOMap()));
		NBOMap imports = ((NBOMap) ast.getOrDefault(NBOFile.KEY_IMPORTS, new NBOMap()));

		assertEquals(1, objects.size());
		assertEquals(2, imports.size());

		assertEquals("nbo.NBOParserTest$Vector3f", imports.get("Vec").getValueRaw());
		assertEquals("nbo.NBOParserTest$Matrix3f", imports.get("Mat").getValueRaw());
		assertEquals(NBOMap.class, objects.get("unit_1").getClass());
	}

	@Test
	public void writeToFile() throws NBOParseException, ClassNotFoundException, IOException {
		NBOSerializer serializer = NBOFile.DEFAULT_SERIALIZER
				.registerMapSerializer(
						Vector3f.class,
						Vector3f::deserialize,
						Vector3f::serialize
				).registerMapSerializer(
						Matrix3f.class,
						Matrix3f::deserialize,
						Matrix3f::serialize
				);

		NBOFile file = new NBOFile();
		file.setSerializer(serializer);
		file.setImport("Vec", Vector3f.class);
		file.setImport("Mat", Matrix3f.class);
		file.setObject("matrix", new Matrix3f(new Vector3f(0, 1, 2), new Vector3f(3, 4, 5), new Vector3f(4, 5, 6)));

		assertEquals("""
				# IMPORTS

				<with Vec as nbo.NBOParserTest$Vector3f>
				<with Mat as nbo.NBOParserTest$Matrix3f>


				# OBJECTS

				matrix := Mat {
				    col1: Vec {x: 0.0, y: 1.0, z: 2.0},
				    col2: Vec {x: 3.0, y: 4.0, z: 5.0},
				    col3: Vec {x: 4.0, y: 5.0, z: 6.0}
				}
				""", file.formatToFileString());
	}

	@Test
	public void readFromFile() throws IOException, NBOParseException, ClassNotFoundException, NBOReferenceException {
		NBOSerializer serializer = NBOFile.DEFAULT_SERIALIZER
				.registerMapSerializer(
						Vector3f.class,
						Vector3f::deserialize,
						Vector3f::serialize
				).registerMapSerializer(
						Matrix3f.class,
						Matrix3f::deserialize,
						Matrix3f::serialize
				);

		NBOFile file = NBOFile.loadFile(TEST_FILE_VECTOR, serializer);

		Vector3f unit1 = file.get("unit_1");
		assertNotNull(unit1, "unit1 should not be null, as it should be defined in vector_test.nbo");
		assertEquals(1.0f, unit1.x);
		assertEquals(0.0f, unit1.y);
		assertEquals(0.0f, unit1.z);

		Vector3f unit2 = file.get("unit_2");
		assertNotNull(unit2, "unit2 should not be null, as it should be defined in vector_test.nbo");
		assertEquals(0.0f, unit2.x);
		assertEquals(1.0f, unit2.y);
		assertEquals(0.0f, unit2.z);

		Vector3f unit3 = file.get("unit_3");
		assertNotNull(unit3, "unit3 should not be null, as it should be defined in vector_test.nbo");
		assertEquals(0.0f, unit3.x);
		assertEquals(0.0f, unit3.y);
		assertEquals(1.0f, unit3.z);

		Matrix3f matrix3f = file.get("mat");
		assertNotNull(matrix3f, "matrix3f should not be null, as it should be defined in vector_test.nbo");
		assertEquals(unit1, matrix3f.col1);
		assertEquals(unit2, matrix3f.col2);
		assertEquals(unit3, matrix3f.col3);
	}

	/*
	Should work but not yet implemented
	@Test
	public void testListElementType() throws NBOReferenceException, IOException, NBOParseException, ClassNotFoundException {
		NBOFile file = NBOFile.loadFile(TEST_FILE_05, NBOFile.DEFAULT_SERIALIZER);

		assertEquals(short[].class, (file.getReferenceObjects().get("a")).getClass());
	}
	 */
}
