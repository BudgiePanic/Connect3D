package connect3DUtil;

import static org.junit.jupiter.api.Assertions.*;
import static connect3DUtil.MathUtil.*;

import org.junit.jupiter.api.Test;

/**
 * Test the functionality of the math utility methods
 * @author Benjamin
 *
 */
class MathUtilTests {

	@Test
	void testPointToScreen() {
		Matrix4 proj = createProjectionM(0.1, 10, 0, 0);
	}

}
