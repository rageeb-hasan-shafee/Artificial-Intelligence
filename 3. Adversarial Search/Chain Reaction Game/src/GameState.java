class GameState {
    int[][] board;
    boolean redTurn;

    public GameState(int[][] board, boolean redTurn) {
        this.board = new int[board.length][board[0].length];
        for (int i = 0; i < board.length; i++) {
            System.arraycopy(board[i], 0, this.board[i], 0, board[i].length);
        }
        this.redTurn = redTurn;
    }
}
