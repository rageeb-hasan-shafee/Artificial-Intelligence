
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

class GameFileCommunication {

    // Reads the game state from a file and returns the current board
    public static int[][] readGameState() {
        int[][] board = new int[9][6];
        try {
            BufferedReader reader = new BufferedReader(new FileReader("gamestate.txt"));
            String line = reader.readLine(); // Read the header (Human Move: or AI Move:)

            // Read the board state
            for (int i = 0; i < 9; i++) {
                line = reader.readLine();
                String[] tokens = line.split(" ");
                for (int j = 0; j < 6; j++) {
                    if (tokens[j].equals("0")) {
                        board[i][j] = 0;
                    } else {
                        int orbCount = Integer.parseInt(tokens[j].substring(0, tokens[j].length() - 1));
                        char color = tokens[j].charAt(tokens[j].length() - 1);
                        board[i][j] = (color == 'R') ? orbCount : -orbCount; // Store color as negative for Blue
                    }
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return board;
    }

    // Writes the updated game state to the file
    public static void writeGameState(int[][] board, String moveType) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("gamestate.txt"));
            writer.write(moveType + "\n");

            // Write the board state
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 6; j++) {
                    if (board[i][j] == 0) {
                        writer.write("0 ");
                    } else {
                        int orbCount = Math.abs(board[i][j]);
                        char color = (board[i][j] > 0) ? 'R' : 'B';
                        writer.write(orbCount + "" + color + " ");
                    }
                }
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}