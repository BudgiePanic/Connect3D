package connect3DUtil;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.system.MemoryUtil.memAllocFloat;
import static org.lwjgl.system.MemoryUtil.memFree;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.Objects;
import java.util.function.Consumer;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;

/**
 * Encapsulate VBO and VAO management
 * @author Benjamin
 *
 */
public class Mesh {
	
	/**
	 * The color white. Used if the color field is not set and not texture was provided.
	 */
	static final Vector3f defaultColor = new Vector3f(1.0f,1.0f,1.0f);
	
	/**
	 * The ID of the Vertex Array Object that this mesh is using.
	 */
	final int vaoID;
	
	/**
	 * The ID of the VBO storing this mesh's vertex location data on the GPU.
	 */
	final int vboID;
	
	/**
	 * The ID of the VBO storing the vertex indices data on the GPU.
	 */
	final int indicesVBOid;
	
	/**
	 * The ID of the VBO storing this mesh's texture coordinates on the GPU.
	 */
	final int textureCoordVBOid;
	
	/**
	 * The number of vertices this Mesh has.
	 */
	final int vertexCount;
	
	/**
	 * The IF of the VBO storing this mesh's normals on the GPU.
	 */
	final int normalsVBOid;
	
	/**
	 * The material that this mesh is using.
	 * The material is a mutable object.
	 * If the same material is aliased over multiple meshes, then there will be problems...
	 */
	private Material material;
	
	/**
	 * Uploads mesh data to the GPU
	 * @param vertices
	 * @param textureCoords 
	 * @param indices 
	 * @param normals
	 * @param material 
	 */
	public Mesh(float[] vertices, float[] textureCoords, int[] indices, float[] normals, Material material) {
		FloatBuffer verticesBuffer = null;
		FloatBuffer textureCoordBuffer = null;
		IntBuffer indicesBuffer = null;
		FloatBuffer normalBuffer = null;
		try {
			//load vert data into auxillary memory
			verticesBuffer = memAllocFloat(vertices.length);
			vertexCount = indices.length;
			verticesBuffer.put(vertices).flip();
			
			//create vertex array object
			vaoID = glGenVertexArrays();
			glBindVertexArray(vaoID);
			
			//create vertex index buffer and upload data to GPU
			indicesVBOid = glGenBuffers();
			indicesBuffer = MemoryUtil.memAllocInt(vertexCount);
			indicesBuffer.put(indices).flip();
			glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indicesVBOid);
			glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
			
			//create vertex buffer object and upload vertex data to GPU
			vboID = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, vboID);
			glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
			int location = 0; //location where the shader can find this data
			int size = 3; //the number of components per vertex attribute. 3 for 3D coordinate.
			int type = GL_FLOAT; // the type of data that the array components are
			boolean normalized = false; //should the data be normalized
			int stride = 0; //the byte offset between consecutive vertex attributes
			int offset = 0; // the distance to the first component in the buffer
			glVertexAttribPointer(location, size, type, normalized, stride, offset);
			
			//texture coord VBO
			textureCoordVBOid = glGenBuffers();
			textureCoordBuffer = MemoryUtil.memAllocFloat(textureCoords.length);
			textureCoordBuffer.put(textureCoords).flip();
			glBindBuffer(GL_ARRAY_BUFFER, textureCoordVBOid);
			glBufferData(GL_ARRAY_BUFFER, textureCoordBuffer, GL_STATIC_DRAW);
			location = 1;
			size = 2;
			glVertexAttribPointer(location, size, type, normalized, stride, offset);
			
			//normals buffer
			normalsVBOid = glGenBuffers();
			normalBuffer = MemoryUtil.memAllocFloat(normals.length);
			normalBuffer.put(normals).flip();
			glBindBuffer(GL_ARRAY_BUFFER, normalsVBOid);
			glBufferData(GL_ARRAY_BUFFER, normalBuffer, GL_STATIC_DRAW);
			location = 2;
			size = 3;
			glVertexAttribPointer(location, size, type, normalized, stride, offset);
			
			glBindVertexArray(0);
		} finally {
			if(verticesBuffer != null) memFree(verticesBuffer);
			if(indicesBuffer != null) memFree(indicesBuffer);
			if(textureCoordBuffer != null) memFree(textureCoordBuffer);
			if(normalBuffer != null) memFree(normalBuffer);
		}
		
		this.material = material;
	}

	/**
	 * Makes LWJGL calls to render this mesh
	 */
	public void draw() {
		startDraw();
		glDrawElements( GL_TRIANGLES,	//rendering primitives being used
						vertexCount,	//the number of elements to render
						GL_UNSIGNED_INT,//the type of data in the indices buffer
						0				//offset in the indices data
		);
		endDraw();
	}
	
	/**
	 * Enables the buffers on the GPU associated with this Mesh.
	 */
	public void startDraw() {
		glBindVertexArray(vaoID);
		glActiveTexture(GL_TEXTURE0);
		material.texture.ifPresent((Texture texture)->{
			glBindTexture(GL_TEXTURE_2D, texture.textureID);
		});
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glEnableVertexAttribArray(2);
	}
	
	/**
	 * Disables the buffers on the GPU associated with this Mesh.
	 */
	public void endDraw() {
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glDisableVertexAttribArray(2);
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindVertexArray(0);
	}
	
	/**
	 * Draws all of the supplied models at once.
	 * @param models
	 *  The models to draw with this mesh.
	 * @param prepare
	 *  Actions needed to prepare OpenGL before drawing.
	 */
	public void draw(Collection<Model> models, Consumer<Model> prepare) {
		models.forEach((Model m)->{
			prepare.accept(m);
			glDrawElements( GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
		});
	}
	
	/**
	 * Makes a single glDrawElements call
	 */
	public void drawSingle() {
		glDrawElements( GL_TRIANGLES, vertexCount, GL_UNSIGNED_INT, 0);
	}

	/**
	 * Cleans up the memory on the GPU that this object allocated
	 */
	public void delete() {
		this.material.delete();
		glDisableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDeleteBuffers(vboID);
		glDeleteBuffers(indicesVBOid);
		glDeleteBuffers(textureCoordVBOid);
		glDeleteBuffers(normalsVBOid);
		glBindVertexArray(0);
		glDeleteVertexArrays(vaoID);
	}
	
	/**
	 * Similar to delete, but leaves the material (and texture) unaffected. 
	 */
	public void clean() {
		glDisableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDeleteBuffers(vboID);
		glDeleteBuffers(indicesVBOid);
		glDeleteBuffers(textureCoordVBOid);
		glDeleteBuffers(normalsVBOid);
		glBindVertexArray(0);
		glDeleteVertexArrays(vaoID);
	}
	
	/**
	 * Get the material that this mesh is using.
	 * @return
	 *  The material of the mesh.
	 */
	public Material getMaterial() {
		return this.material;
	}

	@Override
	public int hashCode() {
		return Objects.hash(indicesVBOid, textureCoordVBOid, vaoID, vboID, vertexCount);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Mesh other = (Mesh) obj;
		return indicesVBOid == other.indicesVBOid && textureCoordVBOid == other.textureCoordVBOid
				&& vaoID == other.vaoID && vboID == other.vboID && vertexCount == other.vertexCount;
	}
}
