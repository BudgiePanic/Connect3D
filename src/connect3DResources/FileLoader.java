package connect3DResources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Helper class the loads stuff from files.
 * @author Benjamin
 *
 */
public final class FileLoader {

	/**
	 * Reads the contents of a file and returns it as a string.
	 * @param relativePath
	 *   The relative path to the file that will be read. 
	 * @return
	 *   The contents of the file
	 * @throws Exception
	 *   Thrown if the file reading fails. 
	 */
	public static String read(String relativePath) throws Exception {
		String answer = "no";
		try {
			InputStream is = FileLoader.class.getResourceAsStream(relativePath);
			if(is == null) throw new Exception("Could not find: ["+relativePath+"]");
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			System.out.println(br.readLine());
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
		return answer;
	}
}
