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
	 * @see "https://stackoverflow.com/questions/16953897/how-to-read-a-text-file-inside-a-jar"
	 * @see "https://stackoverflow.com/questions/25635636/eclipse-exported-runnable-jar-not-showing-images"
	 * @param relativePath
	 *   The relative path to the file that will be read. 
	 * @return
	 *   The contents of the file
	 * @throws Exception
	 *   Thrown if the file reading fails. 
	 */
	public static String read(String relativePath) throws Exception {
		String answer;
		StringBuilder ans = new StringBuilder();
		try {
			InputStream is = FileLoader.class.getResourceAsStream(relativePath);
			if(is == null) throw new Exception("Could not find: ["+relativePath+"]");
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			for(String line; (line = br.readLine()) != null;) {
				ans.append(line+"\n");
			}
			answer = ans.toString();
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
		return answer;
	}
}
