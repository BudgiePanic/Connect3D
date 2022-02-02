package connect3DRender;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import connect3DCore.Piece;
import connect3DResources.FileLoader;
import connect3DUtil.Camera;
import connect3DUtil.Texture;
import connect3DUtil.TransformManager;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

/**
 * Hardware renderer uses the capabilities of LWJGL to display the game via OpenGL.
 * If you do not call the poll events method of the renderer, it will block and eventually crash.
 * Presumably this is because the event queue buffer fills up with unconsumed events.
 * @see "https://www.lwjgl.org/guide"
 * @see "https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/"
 * @author Benjamin
 *
 */
public final class HardwareRenderer implements Renderer {
	
	/**
	 * List of components this renderer is currently drawing.
	 */
	private final List<Component> drawables = new ArrayList<Component>();
	
	/**
	 * List of observers interested in receiving updates from this renderer.
	 */
	private final List<Observer> observers = new ArrayList<Observer>();
	
	private volatile int x = 0,y = 0,z = 0;
	private volatile String message;
//========================================
	/**
	 * The handle of the glfw window.
	 */
	private long a_window; 
	
	/**
	 * And object that encapsulates away the management of the world and projection martices.
	 */
	private final TransformManager transformManager = new TransformManager();
	
	/**
	 * The width of the GLFW window in pixels
	 */
	private int WIDTH = 1280;
	
	/**
	 * The height of the GLFW window in pixels.
	 */
	private int HEIGHT = 720;
	
	/**
	 * the field of view being used by the hardware renderer.
	 */
	private float fov = 90.0f;
	
	/**
	 * remember if the renderer has been initialized.
	 */
	private boolean initialized = false;
	
	/**
	 * The shader program this renderer is using.
	 */
	private ShaderProgram shaderProgram;
	
	/**
	 * TODO TEMP
	 * A mesh that the renderer will draw each redraw.
	 */
	private Mesh mesh;
	
	/**
	 * TODO TEMP
	 * list of models that the renderer will paint each redraw.
	 */
	private final Set<Model> models = new HashSet<>();
	
	/**
	 * Scene camera.
	 */
	private final Camera camera;
	
	/**
	 * The color that the drawable component has selected.
	 */
	private Piece activeColor = Piece.EMPTY;
	
	/**
	 * The N*N*N board dimension, so we can make the camera look at the center of the board.
	 */
	private final int boardDimension;
	
	/**
	 * Create a new Hardware renderer to render a board with boardDimension
	 * @param boardDimension
	 *  The dimensions of the board that this HW will be drawing.
	 */
	HardwareRenderer(int boardDimension){ 
		this.boardDimension = boardDimension;
		this.camera = new Camera();
		float dimension = (float) boardDimension;
		this.camera.radius = dimension * 2.0f;
		this.camera.cameraPointingAt.x = 0.0f;
		this.camera.cameraPointingAt.y = 0.0f;
		this.camera.cameraPointingAt.z = 0.0f;
		this.camera.updatePosition();
	}
	
	
	@Override
	public void addComponent(Component c) { this.drawables.add(c); }

	@Override
	public boolean removeComponent(Component c) { return this.drawables.remove(c); }
	
	@Override
	public void addObserver(Observer o) { this.observers.add(o); }

	@Override
	public void removeObserver(Observer o) { this.observers.remove(o); }

	@Override
	public void setActiveColor(Piece p) { this.activeColor = p; } 

	@Override
	public void notifyObservers() { this.observers.forEach((o)->{o.update(x, y, z, message);}); }

	@Override
	public void initialize() throws InitializationException {
		System.out.println("Hello LWJGL " + Version.getVersion() + "! inside hardware renderer");
		GLFWErrorCallback.createPrint(System.err).set();
		if(!glfwInit()) throw new InitializationException("GLFW library failed to initialize!");
		//set some stuff up.
		glfwDefaultWindowHints();
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
		//try to make the window.
		a_window = glfwCreateWindow(WIDTH, HEIGHT, "Connect3D", NULL, NULL);
		if(a_window == NULL) throw new InitializationException("GLFW window creation failed!");
		
		glfwMakeContextCurrent(a_window);
		glfwSwapInterval(1); //enable V-Sync
		glfwShowWindow(a_window);
		
		GL.createCapabilities();
		
		glEnable(GL_DEPTH_TEST);
		glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
		
		glfwSetFramebufferSizeCallback(a_window, (window, width, height)->{
			this.WIDTH = width; this.HEIGHT = height;
			glViewport(0,0,width,height);
		});
		
		glfwSetKeyCallback(a_window, (window, key, scancode, action, modifiers)->{ //TODO this is a good candidate to delegate to some input object...
			if(key == GLFW_KEY_1 && action == GLFW_PRESS) {
				x++;
			} else if (key == GLFW_KEY_2 && action == GLFW_PRESS) {
				x--;
			} else if (key == GLFW_KEY_3 && action == GLFW_PRESS) {
				z++;
			} else if (key == GLFW_KEY_4 && action == GLFW_PRESS) {
				z--;
			} else if(key == GLFW_KEY_SPACE && action == GLFW_PRESS) {
				message = "place";
				notifyObservers();
				return;
			}
			
			float amount = 10.0f;
			if(key == GLFW_KEY_A && action == GLFW_PRESS) {
				camera.addToPhi(amount);
				camera.updatePosition();
			} else
			if(key == GLFW_KEY_D && action == GLFW_PRESS) {
				camera.addToPhi(-amount);
				camera.updatePosition();
			} else 
			if(key == GLFW_KEY_S && action == GLFW_PRESS) {
				camera.addToTheta(amount);
				camera.updatePosition();
			} else 
			if(key == GLFW_KEY_W && action == GLFW_PRESS) {
				camera.addToTheta(-amount);
				camera.updatePosition();
			} else 
			if(key == GLFW_KEY_Q && action == GLFW_PRESS) {
				camera.radius += 0.1;
				camera.updatePosition();
			} else 
			if(key == GLFW_KEY_E && action == GLFW_PRESS) {
				camera.radius -= 0.1;
				camera.updatePosition();
			}
		});
		
		glfwSetCursorPosCallback(a_window, (window, xPos, yPos)->{ //TODO you should delegate this to some input handling object.
			
		});
		
		glfwSetMouseButtonCallback(a_window, (window, button, action, modifier)->{
			//TODO this should be delegated to some input handling object.
		});
		
		//Create shader program!
		String vertexSource;
		String fragmentSource;
		try {
			vertexSource = FileLoader.read("/connect3DResources/vertex.vs");
			fragmentSource = FileLoader.read("/connect3DResources/fragment.fs");
		} catch (Exception e) {
			throw new InitializationException(e.getMessage());
		}

		try {
			this.shaderProgram = new ShaderProgram();
			this.shaderProgram.createVertexShader(vertexSource);
			this.shaderProgram.createFragmentShader(fragmentSource);
			this.shaderProgram.link();
			this.shaderProgram.createUniform("projectionMatrix");
			this.shaderProgram.createUniform("worldAndViewMatrix");
			this.shaderProgram.createUniform("texture_sampler");
		} catch (Exception e) {
			e.printStackTrace();
			throw new InitializationException(e.getMessage());
		}
		
		//create vertex data and send it to the GPU
		float[] vertsCube = new float[] {
			//vertex 0
			-0.5f, 0.5f, 0.5f,
			//vertex 1
			-0.5f, -0.5f, 0.5f,
			//vertex 2
			0.5f, -0.5f, 0.5f,
			//vertex 3
			0.5f, 0.5f, 0.5f,
			//vertex 4
			-0.5f, 0.5f, -0.5f,
			//vertex 5
			0.5f, 0.5f, -0.5f,
			//vertex 6
			-0.5f, -0.5f, -0.5f,
			//vertex 7
			0.5f, -0.5f, -0.5f,
			//Repeating vertices to allow for proper texture coordinates on the cube faces.
			//TOP FACE
			//vertex 4 repeated :: vertex 8
			-0.5f, 0.5f, -0.5f,
			//vertex 5 repeated :: vertex 9
			0.5f, 0.5f, -0.5f,
			//vertex 0 repeated :: vertex 10
			-0.5f, 0.5f, 0.5f,
			//vertex 3 repeated :: vertex 11
			0.5f, 0.5f, 0.5f,
			//RIGHT FACE
			// vertex 3 repeated :: vertex 12
			0.5f, 0.5f, 0.5f,
			// vertex 2 repeated :: vertex 13
			0.5f, -0.5f, 0.5f,
			//LEFT FACE
			// vertex 0 repeated :: vertex 14
			-0.5f, 0.5f, 0.5f,
			// vertex 1 repeated :: vertex 15
			-0.5f, -0.5f, 0.5f,
			//BOTTOM FACE
			// vertex 6 repeated :: vertex 16
			-0.5f, -0.5f, -0.5f,
			// vertex 7 repeated :: vertex 17
			0.5f, -0.5f, -0.5f,
			// vertex 1 repeated :: vertex 18
			-0.5f, -0.5f, 0.5f,
			// vertex 2 repeated :: vertex 19
			0.5f, -0.5f, 0.5f,
			
		};
		//CCW vertex rotation on face
		int[] indicesCube= new int[] {
			0, 1, 3,   3, 1, 2, //front face triangles
			8, 10, 11,   9, 8, 11,  //top face triangles
			12, 13, 7,   5, 12, 7,  //right face
			14, 15, 6,   4, 14, 6,  //left face
			16, 18, 19,   17, 16, 19,  //bottom face
			7, 6, 4,   7, 4, 5   //back face
		};
		
		float[] textureCoords = new float[] {
			//vertex 0 -- FRONT
			0.0f, 0.0f,
			//vertex 1
			1.0f, 0.0f,
			//vertex 2
			1.0f, 1.0f,
			//vertex 3
			0.0f, 1.0f,
			//vertex 4 -- BACK
			0.0f, 0.0f,
			//vertex 5
			1.0f, 0.0f,
			//vertex 6			
			0.0f, 1.0f,
			//vertex 7 -- end of standard vertices
			1.0f, 1.0f,
			//vertex 8 -- start of repeated vertices -- TOP -- 4
			0.0f, 0.0f,
			//vertex 9 -- 5
			1.0f, 0.0f,
			//vertex 10 -- 0
			0.0f, 1.0f,
			//vertex 11 -- 3
			1.0f, 1.0f,
			//vertex 12	-- RIGHT -- 3
			0.0f, 0.0f,
			//vertex 13 -- 2
			0.0f, 1.0f,
			//vertex 14 -- LEFT -- 0	
			1.0f, 0.0f,
			//vertex 15 -- 1
			1.0f, 1.0f,
			//vertex 16 -- BOTTOM -- 6
			0.0f, 0.0f,
			//vertex 17 -- 7
			1.0f, 0.0f,
			//vertex 18 -- 1
			0.0f, 1.0f,
			//vertex 19 -- 2
			1.0f, 1.0f
		};
		
		try {
			this.mesh = new Mesh(vertsCube, textureCoords, indicesCube, 
					FileLoader.loadAndCreateTexture("src/connect3DResources/textures/testImage.jpg"));
		} catch (Exception e) {
			throw new InitializationException(e.getMessage());
		}
		this.initialized = true;
		System.out.println("init HW renderer complete");
	}

	@Override
	public void destroy() {
		glfwFreeCallbacks(a_window);
		glfwDestroyWindow(a_window);
		if(shaderProgram != null) shaderProgram.delete();
		if(mesh != null) mesh.delete();
		glfwTerminate();
		glfwSetErrorCallback(null).free();
		System.out.println("destroyed HW renderer");
	}
	
	@Override
	public boolean isActive() {
		if(!initialized) throw new IllegalStateException("uninitialized renderer cannot be active");
		return !glfwWindowShouldClose(a_window);
	}

	@Override
	public void pollEvents() throws IllegalStateException {
		//System.out.println("events polled");
		glfwPollEvents(); //this method activates any call back methods we registered in the initialize method...
	}
	@Override
	public void redraw() throws IllegalStateException {
		for(Component c : drawables) {
			c.draw(this); //collect draw requests...
		}
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		//activate shader program and update projection matrix and send projection matrix to GPU
		shaderProgram.bind();
		transformManager.updateProjectionMatrix(this.fov, WIDTH, HEIGHT, 0.1f, 100.0f);
		shaderProgram.uploadMat4f("projectionMatrix", transformManager.projectionMatrix);
		shaderProgram.uploadInteger("texture_sampler", 0); //sample from texture unit 0.
		transformManager.updateViewMatrix(camera);
		//draw each model
		for(Model m : models) {
			transformManager.updateWorldAndViewMatrix(m.getPosition(), m.getRotation(), m.getScale());
			shaderProgram.uploadMat4f("worldAndViewMatrix", transformManager.worldAndViewMatrix);
			m.getMesh().draw();
		} 
		shaderProgram.unbind();
		
		glfwSwapBuffers(a_window);
		//System.out.println("redrawn");
	}
	
	@Override
	public void drawCylinderAt(int x, int y, int z, float radius, float height) {} //TODO

	@Override
	public void drawCubeAt(int x, int y, int z, float width, float height) {} //TODO

	@Override
	public void drawSphereAt(int x, int y, int z, float radius) {
		if(this.activeColor == Piece.EMPTY) return;
		Model m = new Model(mesh);
		//translate the model to be centred for the camera
		float shift = (float)this.boardDimension * 0.5f;
		shift -= radius * 0.5f;
		m.updatePosition(x - shift, y, z - shift);
		m.updateScale(radius);
		models.add(m);
	} //TODO

	@Override
	public void drawMessage(String msg) {} //TODO
	
	//==============================================
	
	private boolean isKeyPressed(int key) {
		return glfwGetKey(a_window, key) == GLFW_PRESS;
	}
	
	//==============================================

}

/**
 * The shader program class encapsulates shader related code.
 * @author Benjamin
 *
 */
class ShaderProgram {
	
	private final int programID;
	
	private int vertexShaderID;
	
	private int fragmentShaderID;
	
	private final Map<String, Integer> uniforms = new HashMap<>();
	
	ShaderProgram() throws Exception {
		programID = glCreateProgram();
		if(programID == 0) throw new Exception("Shader creation failed!");
	}
	
	/**
	 * 
	 * @param source
	 *  The source code of the vertex shader
	 * @throws Exception
	 */
	void createVertexShader(String source) throws Exception {
		vertexShaderID = createShader(source, GL_VERTEX_SHADER);
	}
	
	/**
	 * 
	 * @param source
	 *  The source code of the fragment shader
	 * @throws Exception
	 */
	void createFragmentShader(String source) throws Exception {
		fragmentShaderID = createShader(source, GL_FRAGMENT_SHADER);
	}
	/**
	 * Creates a shader, compiles it, and attaches it to the GL program.
	 * @param source
	 * @param type
	 * @return
	 *  The ID of the shader program.
	 * @throws Exception
	 */
	private int createShader(String source, int type) throws Exception {
		int shaderID = glCreateShader(type);
		if(shaderID == 0) throw new Exception("Failed to create shader! >>"+type);
		glShaderSource(shaderID, source);
		
		glCompileShader(shaderID);
		
		if(glGetShaderi(shaderID, GL_COMPILE_STATUS) == 0) {
			throw new Exception("Shader compilation failed! >>"+glGetShaderInfoLog(shaderID,1024));
		}
		
		glAttachShader(programID, shaderID);
		return shaderID;
	}
	/**
	 * Attempts to link the GL program.
	 * Checks for errors.
	 * @throws Exception
	 */
	void link() throws Exception {
		glLinkProgram(programID);
		if(glGetProgrami(programID, GL_LINK_STATUS) == 0){
			throw new Exception("GL program linking failed! >>"+glGetProgramInfoLog(programID, 1024));
		}
		//detatch shaders, they are not needed after linking is completed
		if(vertexShaderID != 0) {
			glDetachShader(programID, vertexShaderID);
		}
		
		if(fragmentShaderID != 0) {
			glDetachShader(programID, fragmentShaderID);
		}
		
		glValidateProgram(programID);
		if(glGetProgrami(programID, GL_VALIDATE_STATUS) == 0) {
			System.err.println("Warning! Shader program linking >>"+glGetProgramInfoLog(programID,1024));
		}
	}
	
	void bind() {
		glUseProgram(programID);
	}
	
	void unbind() {
		glUseProgram(0);
	}
	
	void delete() {
		unbind();
		if(programID != 0) glDeleteProgram(programID);
	}
	
	/**
	 * Checks to see if the shader program has a uniform with uniformName.
	 * Adds uniformLocation to Map, if it exists.
	 * @param uniformName
	 * @throws Exception
	 *  Thrown if the shader program does not have a uniform with uniformName.
	 */
	void createUniform(String uniformName) throws Exception {
		int uniformLocation = glGetUniformLocation(programID, uniformName);
		if(uniformLocation < 0) throw new Exception("Shader Program:"+programID+" Could not find uniform with name: "+uniformName);
		uniforms.put(uniformName, uniformLocation);
	}
	
	/**
	 * Upload a matrix4f to the GPU via a uniform.
	 * @param uniformName
	 * @param data
	 */
	public void uploadMat4f(String uniformName, Matrix4f data) {
		//Use auto managed external memory, the 'stack'.
		try(MemoryStack stackFrame = MemoryStack.stackPush()){
			FloatBuffer buffer = stackFrame.mallocFloat(4*4);
			data.get(buffer); //transfer the data out of the Matrix4f and into the buffer
			glUniformMatrix4fv(uniforms.get(uniformName), false, buffer); //upload the buffer to the GPU
		}
	}
	
	/**
	 * Upload an integer to the GPU via a uniform in the shader program.
	 * @param uniformName
	 * @param data
	 */
	public void uploadInteger(String uniformName, int data) {
		glUniform1i(uniforms.get(uniformName), data);
	}
}

/**
 * Encapsulate VBO and VAO management
 * @author Benjamin
 *
 */
class Mesh {
	final int vaoID;
	
	final int vboID;
	
	final int indicesVBOid;
	
	final int textureCoordVBOid;
	
	final int vertexCount;
	
	final Texture texture;
	
	/**
	 * Uploads mesh data to the GPU
	 * @param vertices
	 * @param textureCoords 
	 * @param indices 
	 * @param texture 
	 */
	Mesh(float[] vertices, float[] textureCoords, int[] indices, Texture texture) {
		FloatBuffer verticesBuffer = null;
		FloatBuffer textureCoordBuffer = null;
		IntBuffer indicesBuffer = null;
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
			
			glBindVertexArray(0);
		} finally {
			if(verticesBuffer != null) memFree(verticesBuffer);
			if(indicesBuffer != null) memFree(indicesBuffer);
			if(textureCoordBuffer != null) memFree(textureCoordBuffer);
		}
		this.texture = texture;
	}

	/**
	 * Makes LWJGL calls to render this mesh
	 */
	public void draw() {
		//set state
		glBindVertexArray(vaoID);
		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, texture.textureID);
		glEnableVertexAttribArray(0);
		glEnableVertexAttribArray(1);
		glDrawElements( GL_TRIANGLES,	//rendering primitives being used
						vertexCount,	//the number of elements to render
						GL_UNSIGNED_INT,//the type of data in the indices buffer
						0				//offset in the indices data
		);
		//unset state
		glDisableVertexAttribArray(0);
		glDisableVertexAttribArray(1);
		glBindVertexArray(0);
	}

	/**
	 * Cleans up the memory on the GPU that this object allocated
	 */
	public void delete() {
		this.texture.delete();
		glDisableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDeleteBuffers(vboID);
		glDeleteBuffers(indicesVBOid);
		glDeleteBuffers(textureCoordVBOid);
		glBindVertexArray(0);
		glDeleteVertexArrays(vaoID);
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

/**
 * A model is a Mesh combined with transformations that move it into world space when applied.
 * The transformations are mutable objects.
 * Multiple models can reuse the same mesh object.
 * @author Benjamin
 *
 */
class Model{
	private final Mesh mesh;
	private final Vector3f position;
	private float scale;
	private final Vector3f rotation;
	
	/**
	 * Create a new model.
	 * @param mesh
	 *  The mesh that this model will use.
	 */
	Model(Mesh mesh){
		this.mesh = mesh;
		this.position = new Vector3f(0,0,0);
		this.scale = 1.0f;
		this.rotation = new Vector3f(0,0,0);
	}
	
	/**
	 * Return the position of this model in world space.
	 * @return
	 *  The mutable position object of this model. 
	 */
	Vector3f getPosition() {
		return position;
	}
	
	/**
	 * Return the rotation values this model uses.
	 * @return
	 *  The mutable rotation object of this model.
	 */
	Vector3f getRotation() {
		return rotation;
	}
	
	/**
	 * Return the scale value of this model.
	 * @return
	 *  The scale being applied to this model.
	 */
	float getScale() {
		return scale;
	}
	
	/**
	 * Update the transform that moves this model from model space to world space.
	 * @param x
	 *  The distance to move in the X dimension.
	 * @param y
	 *  The distance to move in the Y dimension.
	 * @param z
	 *  The distance to move in the Z dimension.
	 */
	void updatePosition(float x, float y, float z) {
		this.position.x = x;
		this.position.y = y;
		this.position.z = z;
	}
	
	/**
	 * Update the scale this model will use.
	 * @param scale
	 *  The new scale from model space to world space.
	 */
	void updateScale(float scale) {
		this.scale = scale;
	}
	
	/**
	 * Update the rotation values this model will use.
	 * @param pitch
	 *  The rotation along the X axis IN DEGREES
	 * @param roll
	 *  The rotation along the Z axis IN DEGREES
	 * @param yaw
	 *  The rotation along the Y axis IN DEGREES
	 */
	void updateRotation(float pitch, float roll, float yaw) {
		rotation.x = pitch;
		rotation.y = yaw;
		rotation.z = roll;
	}
	
	/**
	 * Get the mesh that this model is using.
	 * @return
	 *  The mesh that this model is using
	 */
	Mesh getMesh() {
		return this.mesh;
	}

	@Override
	public int hashCode() {
		return Objects.hash(mesh, position, rotation, scale);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Model other = (Model) obj;
		return Objects.equals(mesh, other.mesh) && Objects.equals(position, other.position)
				&& Objects.equals(rotation, other.rotation)
				&& Float.floatToIntBits(scale) == Float.floatToIntBits(other.scale);
	}
}