package connect3DUtil;

import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glGenerateMipmap;

/**
 * The texture class encapsulates openGL texture calls.
 * @author Benjamin
 *
 */
public class Texture {
	/**
	 * The texture ID assigned by openGL
	 */
	private final int textureID;
	
	/**
	 * 
	 * @param width
	 * @param height
	 * @param buffer
	 */
	public Texture(int width, int height, ByteBuffer buffer) {
		textureID = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, textureID);
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);//explain to openGL how to interpret the information in buffer
		
		//set texture parameters.
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		
		//upload buffer data to GPU
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
		glGenerateMipmap(GL_TEXTURE_2D);
	}
	
	/**
	 * delete the resources allocated on the GPU by this texture.
	 */
	public void delete() {
		glDeleteTextures(textureID);
	}
}
