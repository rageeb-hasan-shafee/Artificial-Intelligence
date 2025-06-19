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

