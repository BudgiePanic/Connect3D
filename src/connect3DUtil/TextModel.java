package connect3DUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import connect3DResources.FileLoader;

/**
 * A model for displaying text.
 * @author Benjamin
 *
 */
public class TextModel extends Model{

	/**
	 * The depth of on-screen HUD elements.
	 */
	private static final float ZPOS = 0.0f;
	/**
	 * 
	 */
	private static final int VERTICES = 4;
	/**
	 * The text that this model shows when drawn.
	 */
	private final String text;
	
	/**
	 * Create a new model that will display the given text.
	 * @param text
	 *  The text that will be displayed when this model is drawn.
	 * @param texture 
	 *  The texture containing the text font this model will use.
	 * @param columns 
	 *  The number of columns in the font texture.
	 * @param rows 
	 *  The number of rows in the font texture.
	 */
	public TextModel(String text, Texture texture, int columns, int rows) {
		super(makeMesh(text, texture, columns, rows), ColorVector.WHITE);
		this.text = text;
	}
	
	/**
	 * Releases the Mesh resources held by this model.
	 * Does not release the texture used by this model.
	 * This method should be called when the TextModel is no longer being used, to prevent a memory leak.
	 */
	public void tidyUp() {
		this.getMesh().clean();
	}
	
	/**
	 * Manually creates the texture coords, vertices, indices of a Mesh.
	 * @param message
	 *  The message this mesh will draw. 
	 * @param texture
	 *  A texture containing the font used to draw.
	 * @param cols
	 *  The number of columns in the texture.
	 * @param rows
	 *  The number of rows in the texture.
	 * @return A mesh that will draw the message when drawn.
	 */
	private static Mesh makeMesh(String message, Texture texture, int cols, int rows) {
		byte[] chars = null;
		try {
			chars = message.getBytes("ISO-8859-1");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		int charCount = chars.length;
		List<Float> verts = new ArrayList<>();
		List<Float> textCoords = new ArrayList<>();
		List<Integer> indices = new ArrayList<>();
		float tileWidth = (float)texture.width / (float)cols;
		float tileHeight = (float)texture.height / (float)rows;
		
		for(int i = 0; i < charCount; i++) {
			byte currentChar = chars[i];
			int column = currentChar % cols;
			int row = currentChar / rows;
				//top left vertex
			verts.add((float) i * tileWidth);
			verts.add(0.0f);
			verts.add(ZPOS);
			textCoords.add((float)column / (float)cols);
			textCoords.add((float) row/ (float) rows);
			indices.add(i * VERTICES);
				//bottom left vertex
			verts.add((float) i * tileWidth);
			verts.add(tileHeight);
			verts.add(ZPOS);
			textCoords.add((float)column / (float)cols);
			textCoords.add((float) (row+1)/ (float) rows);
			indices.add(i * VERTICES + 1);
				//bottom right vertex
			verts.add((float) i * tileWidth + tileWidth);
			verts.add(tileHeight);
			verts.add(ZPOS);
			textCoords.add((float)(column+1) / (float)cols);
			textCoords.add((float) (row+1)/ (float) rows);
			indices.add(i * VERTICES + 2);
				//top right vertex
			verts.add((float) i * tileWidth + tileWidth);
			verts.add(0.0f);
			verts.add(ZPOS);
			textCoords.add((float)(column+1) / (float)cols);
			textCoords.add((float) row/ (float) rows);
			indices.add(i * VERTICES + 3);
			
			indices.add(i * VERTICES);
			indices.add(i * VERTICES + 2);
		}
		
		float[] arr_vertices = toArrayf(verts);
		float[] arr_textureCoords = toArrayf(textCoords);
		int[] arr_indices = toArrayi(indices);
		float[] normals = new float[0];
		return new Mesh(arr_vertices, arr_textureCoords, arr_indices, normals, new Material(texture));
	}
	/**
	 * 
	 * @param list
	 * @return
	 */
	private static float[] toArrayf(List<Float> list) {
		float[] answer = new float[list.size()];
		for(int i = 0; i < list.size(); i++) {
			answer[i] = list.get(i);
		}
		return answer;
	}
	/**
	 * 
	 * @param list
	 * @return
	 */
	private static int[] toArrayi(List<Integer> list) {
		int[] answer = new int[list.size()];
		for(int i = 0; i < list.size(); i++) {
			answer[i] = list.get(i);
		}
		return answer;
	}
	
}



//the HW renderer needs to hold a reference to the texture that TextModel is using, because TextModel has no tidy up ability.
//the Mesh needs a new tidy up method that does not destroy the texture, because there will be texture reuse occurring.


