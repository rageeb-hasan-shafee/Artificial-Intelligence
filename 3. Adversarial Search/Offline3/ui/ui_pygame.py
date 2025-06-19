import sys

import pygame

from game_engine.engine import *  # This imports all your engine functions

# Initialize pygame
pygame.init()

# Constants
WINDOW_WIDTH = 800
WINDOW_HEIGHT = 600
BOARD_MARGIN = 50
CELL_SIZE = 60
BOARD_WIDTH = COLS * CELL_SIZE
BOARD_HEIGHT = ROWS * CELL_SIZE
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

# Game state
current_player = 'R'  # Red starts first
game_over = False
winner = None

# Create initial board
board = [[(None, 0) for _ in range(COLS)] for _ in range(ROWS)]

# Set up the display
screen = pygame.display.set_mode((WINDOW_WIDTH, WINDOW_HEIGHT))
pygame.display.set_caption("Chain Reaction - Two Player")
font = pygame.font.SysFont('Arial', 24)
large_font = pygame.font.SysFont('Arial', 48)


def draw_board():
    # Draw the background
    screen.fill(WHITE)

    # Draw the board
    for row in range(ROWS):
        for col in range(COLS):
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
    player_text = f"Current Player: {'Red' if current_player == 'R' else 'Blue'}"
    player_surface = font.render(player_text, True, RED if current_player == 'R' else BLUE)
    screen.blit(player_surface, (20, 20))

    # Draw game over message if applicable
    if game_over:
        overlay = pygame.Surface((WINDOW_WIDTH, WINDOW_HEIGHT), pygame.SRCALPHA)
        overlay.fill((0, 0, 0, 128))
        screen.blit(overlay, (0, 0))

        winner_text = f"{'Red' if winner == 'R' else 'Blue'} Player Wins!"
        text_surface = large_font.render(winner_text, True, WHITE)
        text_rect = text_surface.get_rect(center=(WINDOW_WIDTH // 2, WINDOW_HEIGHT // 2))
        screen.blit(text_surface, text_rect)

        restart_text = "Click anywhere to play again"
        restart_surface = font.render(restart_text, True, WHITE)
        restart_rect = restart_surface.get_rect(center=(WINDOW_WIDTH // 2, WINDOW_HEIGHT // 2 + 50))
        screen.blit(restart_surface, restart_rect)


def handle_click(pos,moves_made):
    global board, current_player, game_over, winner

    if game_over:
        # Reset game
        board = [[(None, 0) for _ in range(COLS)] for _ in range(ROWS)]
        current_player = 'R'
        game_over = False
        winner = None
        moves_made[0]=0
        return

    x, y = pos
    # Check if click is within board
    if (BOARD_X <= x < BOARD_X + BOARD_WIDTH and
            BOARD_Y <= y < BOARD_Y + BOARD_HEIGHT):
        col = (x - BOARD_X) // CELL_SIZE
        row = (y - BOARD_Y) // CELL_SIZE

        # Try to make move
        new_board = make_move(board, row, col, current_player)
        if new_board is not None:
            board = new_board
            moves_made[0] += 1

            # Check for winner
            winner = check_winner(board,moves_made)
            if winner is not None:
                game_over = True
            else:
                # Switch player
                current_player = 'B' if current_player == 'R' else 'R'


def main():
    global board, current_player, game_over, winner

    moves_made = [0]
    clock = pygame.time.Clock()

    while True:
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                pygame.quit()
                sys.exit()
            elif event.type == pygame.MOUSEBUTTONDOWN:
                if event.button == 1:  # Left click
                    handle_click(event.pos,moves_made)

        draw_board()
        pygame.display.flip()
        clock.tick(60)


if __name__ == "__main__":
    main()