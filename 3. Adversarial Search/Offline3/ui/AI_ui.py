import pygame
import time
from game_engine.protocol import read_game_state, write_game_state
from game_engine.engine import check_winner, get_legal_moves, make_move, print_board

# Initialize pygame
pygame.init()

# Constants
WINDOW_WIDTH = 800
WINDOW_HEIGHT = 600
BOARD_MARGIN = 50
CELL_SIZE = 60
BOARD_WIDTH = 6 * CELL_SIZE  # 6 columns
BOARD_HEIGHT = 9 * CELL_SIZE  # 9 rows
BOARD_X = (WINDOW_WIDTH - BOARD_WIDTH) // 2
BOARD_Y = (WINDOW_HEIGHT - BOARD_HEIGHT) // 2

# Colors
WHITE = (255, 255, 255)
BLACK = (0, 0, 0)
RED = (255, 0, 0)
BLUE = (0, 0, 255)
GRAY = (200, 200, 200)
LIGHT_RED = (255, 150, 150)
LIGHT_BLUE = (150, 150, 255)

# Game setup
FPS = 30


def draw_board(screen, board, current_player):
    screen.fill(WHITE)

    # Draw the board
    for row in range(9):
        for col in range(6):
            x = BOARD_X + col * CELL_SIZE
            y = BOARD_Y + row * CELL_SIZE

            # Cell background
            owner, count = board[row][col]
            color = WHITE
            if owner == 'R':
                color = LIGHT_RED
            elif owner == 'B':
                color = LIGHT_BLUE

            pygame.draw.rect(screen, color, (x, y, CELL_SIZE, CELL_SIZE))
            pygame.draw.rect(screen, BLACK, (x, y, CELL_SIZE, CELL_SIZE), 1)

            # Draw orbs
            if count > 0:
                if count == 1:
                    pygame.draw.circle(screen, RED if owner == 'R' else BLUE,
                                       (x + CELL_SIZE // 2, y + CELL_SIZE // 2), CELL_SIZE // 3)
                elif count == 2:
                    pygame.draw.circle(screen, RED if owner == 'R' else BLUE,
                                       (x + CELL_SIZE // 3, y + CELL_SIZE // 3), CELL_SIZE // 4)
                    pygame.draw.circle(screen, RED if owner == 'R' else BLUE,
                                       (x + 2 * CELL_SIZE // 3, y + 2 * CELL_SIZE // 3), CELL_SIZE // 4)
                else:  # count >= 3
                    pygame.draw.circle(screen, RED if owner == 'R' else BLUE,
                                       (x + CELL_SIZE // 3, y + CELL_SIZE // 3), CELL_SIZE // 4)
                    pygame.draw.circle(screen, RED if owner == 'R' else BLUE,
                                       (x + 2 * CELL_SIZE // 3, y + CELL_SIZE // 3), CELL_SIZE // 4)
                    pygame.draw.circle(screen, RED if owner == 'R' else BLUE,
                                       (x + CELL_SIZE // 2, y + 2 * CELL_SIZE // 3), CELL_SIZE // 4)

    # Draw current player indicator
    font = pygame.font.SysFont('Arial', 24)
    player_text = f"Current Turn: {'Human (Red)' if current_player == 'R' else 'AI (Blue)'}"
    player_surface = font.render(player_text, True, RED if current_player == 'R' else BLUE)
    screen.blit(player_surface, (20, 20))


def show_game_over(screen, winner):
    overlay = pygame.Surface((WINDOW_WIDTH, WINDOW_HEIGHT), pygame.SRCALPHA)
    overlay.fill((0, 0, 0, 180))
    screen.blit(overlay, (0, 0))

    font = pygame.font.SysFont('Arial', 48)
    if winner == 'D':
        text = "Game Ended in Draw!"
    else:
        text = f"{'Human (Red)' if winner == 'R' else 'AI (Blue)'} Wins!"

    text_surface = font.render(text, True, WHITE)
    text_rect = text_surface.get_rect(center=(WINDOW_WIDTH // 2, WINDOW_HEIGHT // 2))
    screen.blit(text_surface, text_rect)

    restart_font = pygame.font.SysFont('Arial', 24)
    restart_surface = restart_font.render("Press R to restart or Q to quit", True, WHITE)
    restart_rect = restart_surface.get_rect(center=(WINDOW_WIDTH // 2, WINDOW_HEIGHT // 2 + 50))
    screen.blit(restart_surface, restart_rect)

    pygame.display.flip()
    return text_surface, text_rect


def handle_human_move(pos, board, player):
    x, y = pos
    if (BOARD_X <= x < BOARD_X + BOARD_WIDTH and
            BOARD_Y <= y < BOARD_Y + BOARD_HEIGHT):
        col = (x - BOARD_X) // CELL_SIZE
        row = (y - BOARD_Y) // CELL_SIZE

        if (row, col) in get_legal_moves(board, player):
            new_board = make_move(board, row, col, player)
            if new_board is not None:
                write_game_state("AI Move:", new_board)
                return True
    return False


def main():
    screen = pygame.display.set_mode((WINDOW_WIDTH, WINDOW_HEIGHT))
    pygame.display.set_caption("Chain Reaction - Human vs AI")
    clock = pygame.time.Clock()

    # Initialize game state file
    initial_board = [[(None, 0) for _ in range(6)] for _ in range(9)]
    write_game_state( "Human Move:", initial_board)
    moves_made=[0]

    running = True
    game_over = False
    winner = None

    while running:
        # Read current game state
        move_type, board = read_game_state()
        current_player = 'R' if move_type == "Human Move:" else 'B'

        # Check for winner
        if not game_over:
            winner = check_winner(board,moves_made)
            if winner is not None:
                game_over = True

        # Draw the board
        draw_board(screen, board, current_player)

        if game_over:
            show_game_over(screen, winner)

        pygame.display.flip()

        # Event handling
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                running = False

            if event.type == pygame.MOUSEBUTTONDOWN and not game_over:
                if event.button == 1 and current_player == 'R':  # Left click and human turn
                    if handle_human_move(event.pos, board, 'R'):
                        moves_made[0]+=1 #moves increased

            if event.type == pygame.KEYDOWN and game_over:
                if event.key == pygame.K_r:  # Restart game
                    initial_board = [[(None, 0) for _ in range(6)] for _ in range(9)]
                    write_game_state( "Human Move:", initial_board)
                    game_over = False
                    winner = None
                    moves_made=[0]
                elif event.key == pygame.K_q:  # Quit game
                    running = False

        # Small delay to prevent high CPU usage
        time.sleep(0.1)
        clock.tick(FPS)

    pygame.quit()


if __name__ == "__main__":
    main()