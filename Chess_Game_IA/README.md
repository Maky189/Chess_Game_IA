# Chess Game IA

## Overview
This project is a Java-based chess game implemented using JavaFX. It provides a graphical interface for playing chess, allowing users to interact with the game through mouse events. The game includes features for rendering the chessboard, handling piece movements, and calculating valid moves.

## Project Structure
The project consists of the following main components:

- **Board.java**: Responsible for rendering the chessboard and its pieces on the canvas. It includes methods to draw the board and highlight possible moves.
  
- **DragHandler.java**: Manages mouse events for dragging pieces on the chessboard. It handles piece selection, movement, and validation of moves.
  
- **Game.java**: Represents the chess game logic. It initializes the board, sets up the pieces, and calculates possible moves for each piece.
  
- **Main.java**: The entry point of the application. It sets up the JavaFX application, creates the game instance, and initializes the canvas and event handlers.
  
- **Menu.java**: Implements the starting window menu for the chess game. It includes two buttons: "Start Game" and "Multiplayer Game," with only the "Start Game" button implemented.

## How to Run
1. Ensure you have Java and JavaFX installed on your machine.
2. Clone the repository or download the project files.
3. Navigate to the project directory and compile the Java files.
4. Run the `Main` class to start the application.

## Future Enhancements
- Implement the "Multiplayer Game" functionality.
- Add features for saving and loading game states.
- Improve the user interface with additional options and settings.