import pygame
import sys
import random
import time

pygame.init()

SCREEN_WIDTH = 600
SCREEN_HEIGHT = 400
screen = pygame.display.set_mode((SCREEN_WIDTH, SCREEN_HEIGHT))
pygame.display.set_caption("Chain Reaction")

RED = (255, 0, 0)
BLUE = (0, 0, 255)
WHITE = (255, 255, 255)
BLACK = (0, 0, 0)

BOARD_ROWS = 9
BOARD_COLS = 6
CELL_SIZE = 50

board = [[0 for _ in range(BOARD_COLS)] for _ in range(BOARD_ROWS)]


# Task 1: Drawing the Board
def draw_board():
    for row in range(BOARD_ROWS):
        for col in range(BOARD_COLS):
            x = col * CELL_SIZE
            y = row * CELL_SIZE
            pygame.draw.rect(screen, WHITE, (x, y, CELL_SIZE, CELL_SIZE), 2)  # Draw grid

            if board[row][col] != 0:
                count, color = board[row][col]
                orb_color = RED if color == 'R' else BLUE
                pygame.draw.circle(screen, orb_color, (x + CELL_SIZE // 2, y + CELL_SIZE // 2), CELL_SIZE // 2)


# Task 2: File Communication Functions

def read_game_state(filename="gamestate.txt"):
    """Read the current game state from the file"""
    with open(filename, "r") as f:
        header = f.readline().strip()  # Read the header (Human Move or AI Move)
        board = []
        for _ in range(BOARD_ROWS):
            row = f.readline().strip().split()
            board.append([parse_cell(c) for c in row])  # Convert each cell in the row
    return header, board  # Return header ("Human Move" or "AI Move") and the board

def parse_cell(c):
    """Parse each cell in the game state"""
    if c == "0":
        return 0  # Empty cell
    return (int(c[:-1]), c[-1])  # Convert cell to (count, color)

def format_cell(cell):
    """Format each cell back to string for saving"""
    if cell == 0:
        return "0"
    return f"{cell[0]}{cell[1]}"  # Format (count, color) as string "1R", "2B", etc.

def write_game_state(turn, board, filename="gamestate.txt"):
    """Write the current game state to a file"""
    with open(filename, "w") as f:
        f.write(f"{turn} Move:\n")  # Header indicating who made the move (Human or AI)
        for row in board:
            f.write(" ".join(format_cell(c) for c in row) + "\n")  # Write each row of the board


# Task 3: Explosion mechanics and board updates
def critical_mass(r, c):
    cm = 4
    if r == 0 or r == BOARD_ROWS - 1:
        cm -= 1
    if c == 0 or c == BOARD_COLS - 1:
        cm -= 1
    return cm

def apply_move(board, row, col, current_player):
    if board[row][col] == 0:
        board[row][col] = (1, current_player)
    else:
        cnt, _ = board[row][col]
        board[row][col] = (cnt + 1, current_player)
    trigger_explosions(board, row, col)

def trigger_explosions(board, r, c):
    queue = [(r, c)]
    while queue:
        rr, cc = queue.pop(0)
        cnt, col = board[rr][cc]
        cm = critical_mass(rr, cc)

        if cnt >= cm:
            board[rr][cc] = (cnt - cm, col) if cnt > cm else 0
            for dr, dc in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                nr, nc = rr + dr, cc + dc
                if 0 <= nr < BOARD_ROWS and 0 <= nc < BOARD_COLS:
                    if board[nr][nc] == 0:
                        board[nr][nc] = (1, col)
                    else:
                        ncnt, _ = board[nr][nc]
                        board[nr][nc] = (ncnt + 1, col)
                    if board[nr][nc][0] >= critical_mass(nr, nc):
                        queue.append((nr, nc))


# Task 4: Minimax with Alpha-Beta
def minimax(state, depth, alpha, beta, maximizing_player):
    if depth == 0 or is_game_over(state):
        return combined_evaluation(state)

    if maximizing_player:
        max_eval = float('-inf')
        for child in generate_children(state, 'R'):
            eval = minimax(child, depth - 1, alpha, beta, False)
            max_eval = max(max_eval, eval)
            alpha = max(alpha, eval)
            if beta <= alpha:
                break
        return max_eval
    else:
        min_eval = float('inf')
        for child in generate_children(state, 'B'):
            eval = minimax(child, depth - 1, alpha, beta, True)
            min_eval = min(min_eval, eval)
            beta = min(beta, eval)
            if beta <= alpha:
                break
        return min_eval

def generate_children(state, player):
    children = []
    for row in range(BOARD_ROWS):
        for col in range(BOARD_COLS):
            if state[row][col] == 0:  # Empty cell
                new_state = [row[:] for row in state]
                new_state[row][col] = (1, player)  # Place a new orb
                children.append(new_state)
    return children

def is_game_over(state):
    red_count = sum(1 for row in state for cell in row if cell != 0 and cell[1] == 'R')
    blue_count = sum(1 for row in state for cell in row if cell != 0 and cell[1] == 'B')
    return red_count == 0 or blue_count == 0

def combined_evaluation(state):
    red_orbs = sum(1 for row in state for cell in row if cell != 0 and cell[1] == 'R')
    blue_orbs = sum(1 for row in state for cell in row if cell != 0 and cell[1] == 'B')
    score = red_orbs - blue_orbs
    return score

# Task 5: AI Move Selection
def ai_move(player):
    best_move = minimax_search(board, depth=3)
    if best_move:
        row, col = best_move
        apply_move(board, row, col, player)
    else:
        move = random_move_agent(board, player)
        row, col = move
        apply_move(board, row, col, player)

def minimax_search(board, depth, maximizing_player=True):
    best_move = None
    best_score = float('-inf') if maximizing_player else float('inf')
    for row in range(BOARD_ROWS):
        for col in range(BOARD_COLS):
            if board[row][col] == 0:  # If the cell is empty
                new_board = [row[:] for row in board]
                apply_move(new_board, row, col, 'R' if maximizing_player else 'B')
                score = minimax(new_board, depth-1, float('-inf'), float('inf'), not maximizing_player)
                if (maximizing_player and score > best_score) or (not maximizing_player and score < best_score):
                    best_score = score
                    best_move = (row, col)
    return best_move


# Task 6: Game Loop
def game_loop():
    """The main game loop for Human vs AI interaction"""
    current_player = 'R'  # Human player starts with Red
    running = True

    while running:
        # Read the current game state from file
        header, board = read_game_state("gamestate.txt")

        # Handle Human Move
        if current_player == 'R' and header == "Human Move":
            for event in pygame.event.get():
                if event.type == pygame.QUIT:
                    running = False
                elif event.type == pygame.MOUSEBUTTONDOWN:
                    mouse_x, mouse_y = pygame.mouse.get_pos()
                    row = mouse_y // CELL_SIZE
                    col = mouse_x // CELL_SIZE

                    if board[row][col] == 0:  # If the cell is empty
                        board[row][col] = ('R', 1)  # Human places a red orb
                        current_player = 'B'  # Switch to AI's turn
                        write_game_state("AI", board, "gamestate.txt")  # Write the updated state
                        break  # Exit the loop after making the move

        # Handle AI Move
        if current_player == 'B' and header == "AI Move":
            ai_move('B')  # AI places a blue orb
            current_player = 'R'  # Switch back to Human's turn
            write_game_state("Human", board, "gamestate.txt")  # Write the updated state

        # Update the display
        screen.fill(BLACK)
        draw_board()  # Draw the board based on the updated game state
        pygame.display.flip()

        # Check for game over
        if is_game_over(board):
            print(f"{current_player} wins!")
            running = False

    pygame.quit()
    sys.exit()

# Entry point for the game
if __name__ == "__main__":
    game_loop("f:\3-1\CSE 318\3. Adversarial Search\gametext.txt")
