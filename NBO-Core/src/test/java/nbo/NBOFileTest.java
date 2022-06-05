package nbo;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

public class NBOFileTest {

	public static final File TEST_01 = new File("src/test/resources/test_01.nbo");
	public static final File TEST_02 = new File("src/test/resources/test_02.nbo");
	public static final File TEST_03 = new File("src/test/resources/test_03.nbo");
	public static final File TEST_04 = new File("src/test/resources/test_04.nbo");

	@Test
	public void testInclude() throws IOException, NBOParseException, ClassNotFoundException {

		NBOFile file = NBOFile.loadFile(TEST_01, NBOFile.DEFAULT_SERIALIZER);
		System.out.println(file.formatToFileString());
	}

	@Test
	public void testIncludeLoop() throws IOException, NBOParseException, ClassNotFoundException {

		NBOFile file = NBOFile.loadFile(TEST_03, NBOFile.DEFAULT_SERIALIZER);
		System.out.println(file.formatToFileString());
	}
}
