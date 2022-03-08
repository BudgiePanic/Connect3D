# Connect3D
 Connect3D is a simple 4 in a row game.
 The objective is to make four in a row with your pieces, while prevent other players from doing the same.
 Connect3D offers 2 - 4 player games and the ability to set the size of the board.

 It follows a traditional Model View Controller architecture.
 Connect3D has three different views.
 | view | description |
 |------|:-----------:|
 | Text | Displays the game using the console, accepts text based input. |
 | Software | Displays the game using Java Swing. Uses mouse based input.|
 | Hardware | Displays the game using OpenGL. Uses mouse based input.    |

## How to run

I currently do not know how to package Connect3D's dependencies into a runnable jar file.

As such this is how you can set up an Eclipse project to run Connect3D.

 1. First clone the source files into an Eclipse project.
 2. Connect3D uses LWJGL v3.3.0 to access OpenGL. [The download page for lwjgl is here] (https://www.lwjgl.org/customize)
 3. [This excellent resource] (https://github.com/LWJGL/lwjgl3-wiki/wiki/1.2.-Install) explains how to configure Eclipse to LWJGL.
 4. When Eclipse has been configured to use LWJGL you can run the Main method from Connect3DMain package.

### Game controls

 To place pieces when using the Software renderer, click in the grid that is drawn to the screen. Hovering the mouse over the grid will show you where the piece will be placed.
 Use the right mouse button to pan around the board.

 When using the Hardware renderer, move the mouse to the column you want to place a piece in.
 The right mouse button can be dragged to pan around the board.
 The backspace key toggles the debug view that shows where you can place pieces.

## Acknowledgements

[The sky box texture is here] (http://www.custommapmakers.org/skyboxes/zips/ely_hills.zip)
[I made extensive use of this book while writing Connect3D] (https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/)
[This resource was also useful for learning the basics of OpenGL] (https://learnopengl.com/)
[Some models from lwjglbook were used] (https://github.com/lwjglgamedev/lwjglbook)

I made the sphere model using blender.
