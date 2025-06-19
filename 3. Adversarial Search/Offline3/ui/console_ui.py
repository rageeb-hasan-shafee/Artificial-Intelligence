

from game_engine.engine import *



def run_two_player_game():
    # Initialize game state
    board = [[(None, 0) for _ in range(COLS)] for _ in range(ROWS)]
    current_player = 'R'  # Red starts first

    while True:
        print(f"\nCurrent Player: {'Red' if current_player == 'R' else 'Blue'}")
        print_board(board)
        moves_made=[0]

        # Get valid moves
        valid_moves = get_legal_moves(board, current_player)
        if not valid_moves:
            print(f"No valid moves for {'Red' if current_player == 'R' else 'Blue'}!")
            break

        # Get player input
        while True:
            try:
                move = input(f"Enter your move (row col) for {'Red' if current_player == 'R' else 'Blue'}: ")
                row, col = map(int, move.split())
                if (row, col) in valid_moves:
                    break
                else:
                    print("Invalid move. Try again.")
            except:
                print("Invalid input. Please enter row and column numbers separated by space.")

        # Make the move
        new_board = make_move(board, row, col, current_player)
        moves_made[0]+=1
        if new_board is None:
            print("Invalid move. Try again.")
            continue

        board = new_board

        # Check for winner
        winner = check_winner(board,moves_made)
        if winner is not None:
            print_board(board)
            print(f"\n{'Red' if winner == 'R' else 'Blue'} player wins!")
            break

        # Switch player
        current_player = 'B' if current_player == 'R' else 'R'


if __name__ == "__main__":
    print("Chain Reaction - Two Player Game")
    print("Red (R) goes first, then Blue (B)")
    print("Enter moves as row and column numbers (0-based)")

    while True:
        run_two_player_game()
        play_again = input("Play again? (y/n): ").lower()
        if play_again != 'y':
            break