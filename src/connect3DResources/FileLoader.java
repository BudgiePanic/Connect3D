package connect3DResources;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryStack;
import static org.lwjgl.stb.STBImage.*;

import connect3DUtil.Texture;

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
	
	/**
	 * Load the RGBA information from an image using the stb library.
	 * @see "https://github.com/lwjglgamedev/lwjglbook/blob/master/chapter08/src/main/java/org/lwjglb/engine/graph/Texture.java"
	 * @param relativePath
	 *  The relative location of the image file from the FileLoader location
	 * @return
	 *  An object holding all the needed texture information.
	 * @throws Exception
	 *  Thrown if no image is found at the specified location
	 */
	public static Texture loadAndCreateTexture(String relativePath) throws Exception{
		ByteBuffer buffer;
		int width, height;
		try(MemoryStack stackFrame = MemoryStack.stackPush()){
			IntBuffer w = stackFrame.mallocInt(1);
			IntBuffer h = stackFrame.mallocInt(1);
			IntBuffer channels = stackFrame.mallocInt(1);
			
			buffer = stbi_load(relativePath, w,h,channels, 4);
			if(buffer == null) {
				throw new Exception("Image: ["+relativePath+"] was not loaded! >>"+stbi_failure_reason());
			}
			width = w.get(); height = h.get();
		}
		Texture answer = new Texture(width, height, buffer);
		stbi_image_free(buffer);
		return answer;
	}
}
