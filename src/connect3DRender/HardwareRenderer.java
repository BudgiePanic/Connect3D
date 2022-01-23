package connect3DRender;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import connect3DCore.Piece;
import connect3DResources.FileLoader;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
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
	
	private volatile int x,y,z;
	private volatile String message;
//========================================
	/**
	 * The handle of the glfw window.
	 */
	private long a_window; 
	
	/**
	 * The width of the GLFW window in pixels
	 */
	private int WIDTH = 1280;
	
	/**
	 * The height of the GLFW window in pixels.
	 */
	private int HEIGHT = 720;
	
	/**
	 * remember if the renderer has been initialized.
	 */
	private boolean initialized = false;
	
	/**
	 * The shader program this renderer is using.
	 */
	private ShaderProgram shaderProgram;
	
	private int vboID;
	private int vaoID;
	
	
	@Override
	public void addComponent(Component c) { this.drawables.add(c); }

	@Override
	public boolean removeComponent(Component c) { return this.drawables.remove(c); }
	
	@Override
	public void addObserver(Observer o) { this.observers.add(o); }

	@Override
	public void removeObserver(Observer o) { this.observers.remove(o); }

	@Override
	public void setActiveColor(Piece p) {} //TODO

	@Override
	public void notifyObservers() {
		this.observers.forEach((o)->{o.update(x, y, z, message);});
	}

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
		
		glfwSetFramebufferSizeCallback(a_window, (window, width, height)->{
			this.WIDTH = width; this.HEIGHT = height;
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
		} catch (Exception e) {
			e.printStackTrace();
			throw new InitializationException(e.getMessage());
		}
		
		//create vertex data and send it to the GPU
		float[] verts = new float[] {
			 0.0f,  0.5f, 0.0f,
			-0.5f, -0.5f, 0.0f,
			 0.5f, -0.5f, 0.0f
		};
		FloatBuffer vertBuffer = MemoryUtil.memAllocFloat(verts.length);
		vertBuffer.put(verts).flip();
		
		this.vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);
		this.vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, this.vboID);
		glBufferData(GL_ARRAY_BUFFER, vertBuffer, GL_STATIC_DRAW); //upload data to GPU
		memFree(vertBuffer); 
		
		//Tell the GPU how to interpret the data we sent it.
		int index = 0; //location where the shader can find this data
		int size = 3; //the number of components per vertex attribute. 3 for 3D coordinate.
		int type = GL_FLOAT; // the type of data that the array components are
		boolean normalized = false; //should the data be normalized
		int stride = 0; //the byte offset between consecutive vertex attributes
		int offset = 0; // the distance to the first component in the buffer
		glVertexAttribPointer(index, size, type, normalized, stride, offset);
		//tidy up
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glBindVertexArray(0);
		
		this.initialized = true;
		System.out.println("init HW renderer complete");
	}

	@Override
	public void destroy() {
		glfwFreeCallbacks(a_window);
		glfwDestroyWindow(a_window);
		if(shaderProgram != null) shaderProgram.delete();
		glDisableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		glDeleteBuffers(vboID);
		glBindVertexArray(0);
		glDeleteVertexArrays(vaoID);
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
		System.out.println("events polled");
		glfwPollEvents();
		if(isKeyPressed(GLFW_KEY_Q)) red += 0.01;
		if(isKeyPressed(GLFW_KEY_W)) green += 0.01;
		if(isKeyPressed(GLFW_KEY_E)) blue += 0.01;
	}
	float red = 0.0f;
	float green = 0.0f;
	float blue = 0.0f;
	@Override
	public void redraw() throws IllegalStateException {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		
		shaderProgram.bind();
		glBindVertexArray(vaoID);
		glEnableVertexAttribArray(0);
		
		glDrawArrays(GL_TRIANGLES,0,3);
		
		glDisableVertexAttribArray(0);
		glBindVertexArray(0);
		shaderProgram.unbind();
		
		glClearColor(red, green, blue, 1.0f);
		glfwSwapBuffers(a_window);
		System.out.println("redrawn");
	}
	
	@Override
	public void drawCylinderAt(int x, int y, int z, float radius, float height) {} //TODO

	@Override
	public void drawCubeAt(int x, int y, int z, float width, float height) {} //TODO

	@Override
	public void drawSphereAt(int x, int y, int z, float radius) {} //TODO

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
}
