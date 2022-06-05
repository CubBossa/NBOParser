package nbo;

import nbo.exception.NBOException;
import nbo.exception.NBOParseException;
import nbo.exception.NBOReferenceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class NBOFileTest {

	public static final File TEST_01 = new File("src/test/resources/test_01.nbo");
	public static final File TEST_02 = new File("src/test/resources/test_02.nbo");
	public static final File TEST_03 = new File("src/test/resources/test_03.nbo");
	public static final File TEST_04 = new File("src/test/resources/test_04.nbo");

	@Test
	public void testInclude() throws IOException, ClassNotFoundException, NBOException {
		NBOSerializer serializer = NBOFile.DEFAULT_SERIALIZER
				.registerMapSerializer(
						NBOParserTest.Vector3f.class,
						NBOParserTest.Vector3f::deserialize,
						NBOParserTest.Vector3f::serialize
				).registerMapSerializer(
						NBOParserTest.Matrix3f.class,
						NBOParserTest.Matrix3f::deserialize,
						NBOParserTest.Matrix3f::serialize
				);
		NBOFile file = NBOFile.loadFile(TEST_01, serializer);
		assertEquals(NBOParserTest.Vector3f.class, file.getReferenceObjects().get("unit_1").getClass());
		assertEquals(NBOParserTest.Matrix3f.class, file.getReferenceObjects().get("mat").getClass());
	}

	@Test
	public void testIncludeLoop() throws IOException, NBOParseException, ClassNotFoundException, NBOReferenceException {

		NBOFile file = NBOFile.loadFile(TEST_03, NBOFile.DEFAULT_SERIALIZER);
		System.out.println(file.formatToFileString());
	}
}
