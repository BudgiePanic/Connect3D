package connect3DMain;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.SwingUtilities;

import connect3DCore.Piece;
import connect3DGame.Game;
import connect3DRender.RenderFactory;
import connect3DUtil.ConfigDialog;

/**
 * TODO BUG This code works fine in TestMain
 * 			But when the exact same code is run from Main, is crashes with:
 * 
 * Exception in thread "main" java.lang.NoClassDefFoundError: org/lwjgl/Version
	at connect3DRender.HardwareRenderer.initialize(HardwareRenderer.java:83)
	at connect3DGame.Game.<init>(Game.java:55)
	at connect3DMain.Main.main(Main.java:57)
Caused by: java.lang.ClassNotFoundException: org.lwjgl.Version
	at java.base/jdk.internal.loader.BuiltinClassLoader.loadClass(BuiltinClassLoader.java:583)
	at java.base/jdk.internal.loader.ClassLoaders$AppClassLoader.loadClass(ClassLoaders.java:178)
	at java.base/java.lang.ClassLoader.loadClass(ClassLoader.java:521)
	... 3 more
 * @author Benjamin
 *
 */
public class TestMain {

	public static void main(String[] args) {
		//HardwareRenderer r = new HardwareRenderer();
//		Renderer r = RenderFactory.Renderer("hardware", 4);
		/*try {
			r.initialize();
		} catch (InitializationException e) {
			e.printStackTrace();
		}*/
//		new Game(RenderFactory.Renderer("hardware", 4), List.of(Piece.GREEN), 4).run();
		Main main = new Main();
		
		try {
			SwingUtilities.invokeAndWait(()->{
				new ConfigDialog(main);
			});
		} catch (InvocationTargetException | InterruptedException e) {
			e.printStackTrace();
			System.exit(0);
		}
		if(main.shouldStart) {
			new Game(RenderFactory.Renderer(main.renderType, main.boardSize), List.of(Piece.GREEN), main.boardSize).run();
		} 
	}

}

/*class HelloWorld implements Runnable {

	// The window handle
	private long window;

	@Override
	public void run() {
		System.out.println("Hello LWJGL " + Version.getVersion() + "!");

		init();
		loop();

		// Free the window callbacks and destroy the window
		glfwFreeCallbacks(window);
		glfwDestroyWindow(window);

		// Terminate GLFW and free the error callback
		glfwTerminate();
		glfwSetErrorCallback(null).free();
	}

	private void init() {
		// Setup an error callback. The default implementation
		// will print the error message in System.err.
		GLFWErrorCallback.createPrint(System.err).set();

		// Initialize GLFW. Most GLFW functions will not work before doing this.
		if ( !glfwInit() )
			throw new IllegalStateException("Unable to initialize GLFW");

		// Configure GLFW
		glfwDefaultWindowHints(); // optional, the current window hints are already the default
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE); // the window will stay hidden after creation
		glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE); // the window will be resizable

		// Create the window
		window = glfwCreateWindow(300, 300, "Hello World!", NULL, NULL);
		if ( window == NULL )
			throw new RuntimeException("Failed to create the GLFW window");

		// Setup a key callback. It will be called every time a key is pressed, repeated or released.
		glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
			if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
				glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
		});

		// Make the OpenGL context current
		glfwMakeContextCurrent(window);
		// Enable v-sync
		glfwSwapInterval(1);

		// Make the window visible
		glfwShowWindow(window);
	}

	private void loop() {
		// This line is critical for LWJGL's interoperation with GLFW's
		// OpenGL context, or any context that is managed externally.
		// LWJGL detects the context that is current in the current thread,
		// creates the GLCapabilities instance and makes the OpenGL
		// bindings available for use.
		GL.createCapabilities();

		// Set the clear color
		glClearColor(1.0f, 0.0f, 0.0f, 0.0f);

		// Run the rendering loop until the user has attempted to close
		// the window or has pressed the ESCAPE key.
		while ( !glfwWindowShouldClose(window) ) {
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

			glfwSwapBuffers(window); // swap the color buffers

			// Poll for window events. The key callback above will only be
			// invoked during this call.
			glfwPollEvents();
		}
	}
}*/