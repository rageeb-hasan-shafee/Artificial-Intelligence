class Explosion {

    public static void processExplosions(int[][] board) {
        boolean explosionOccurred = true;
        while (explosionOccurred) {
            explosionOccurred = false;
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 6; j++) {
                    if (Math.abs(board[i][j]) >= getCriticalMass(i, j)) {
                        explosionOccurred = true;
                        explodeCell(board, i, j);
                    }
                }
            }
        }
    }

    private static int getCriticalMass(int row, int col) {
        if ((row == 0 || row == 8) && (col == 0 || col == 5)) {
            return 2; // Corners
        } else if (row == 0 || row == 8 || col == 0 || col == 5) {
            return 3; // Edges
        } else {
            return 4; // Interior cells
        }
    }

    private static void explodeCell(int[][] board, int row, int col) {
        int criticalMass = getCriticalMass(row, col);
        int orbs = Math.abs(board[row][col]) - criticalMass;

        // Set the current cell to 0 (after explosion)
        board[row][col] = 0;

        // Define the neighbors (up, down, left, right)
        int[][] neighbors = {
            {row - 1, col}, {row + 1, col}, {row, col - 1}, {row, col + 1}
        };

        // Distribute orbs to the neighbors
        for (int[] neighbor : neighbors) {
            int r = neighbor[0];
            int c = neighbor[1];
            if (r >= 0 && r < 9 && c >= 0 && c < 6) {
                board[r][c] += orbs;  // Add the orbs to the neighboring cells
            }
        }
    }
}
