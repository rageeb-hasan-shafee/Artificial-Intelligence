import pygame
import sys
import os
import time
import threading
from typing import List, Tuple, Optional

# Initialize Pygame
pygame.init()

# Constants
WINDOW_WIDTH = 800
WINDOW_HEIGHT = 800
ROWS = 9
COLS = 6
CELL_SIZE = 70
BOARD_OFFSET_X = (WINDOW_WIDTH - COLS * CELL_SIZE) // 2
BOARD_OFFSET_Y = 50
GAME_STATE_FILE = "/media/rageeb-hasan-shafee/New Volume/3-1/CSE 318/3. Adversarial Search/2105175/shared/gamestate.txt"

# Colors
WHITE = (255, 255, 255)
BLACK = (0, 0, 0)
GRAY = (128, 128, 128)
LIGHT_GRAY = (200, 200, 200)
RED = (255, 50, 50)
BLUE = (50, 50, 255)
GREEN = (50, 255, 50)
DARK_RED = (200, 0, 0)
DARK_BLUE = (0, 0, 200)

class ChainReactionUI:
    def __init__(self):
        self.screen = pygame.display.set_mode((WINDOW_WIDTH, WINDOW_HEIGHT))
        #pygame.display.set_caption("Chain Reaction - Human vs AI")
        self.clock = pygame.time.Clock()
        self.font = pygame.font.Font(None, 24)
        self.big_font = pygame.font.Font(None, 36)
        
        # Game state
        self.board = [[0 for _ in range(COLS)] for _ in range(ROWS)]
        self.colors = [[' ' for _ in range(COLS)] for _ in range(ROWS)]
        self.current_player = 'R'  # Human is Red, AI is Blue
        self.game_over = False
        self.winner = None
        self.waiting_for_ai = False
        self.ai_thinking = False
        self.move_count = 0   
        self.ai_vs_ai_mode = False
        self.ai_vs_ai_button = pygame.Rect(WINDOW_WIDTH - 150, 10, 140, 30)
    
        shared_dir = os.path.dirname(GAME_STATE_FILE)
        os.makedirs(shared_dir, exist_ok=True)
        
        self.initialize_game_state()
        
        self.file_watcher_running = True
        self.file_watcher_thread = threading.Thread(target=self.watch_file_changes)
        self.file_watcher_thread.daemon = True
        self.file_watcher_thread.start()
    
    def initialize_game_state(self):
        """Initialize an empty game state file"""
        try:
            with open(GAME_STATE_FILE, 'w') as f:
                f.write("Game Start:\n")
                for _ in range(ROWS):
                    f.write(" ".join(["0"] * COLS) + "\n")
        except Exception as e:
            print(f"Error initializing game state: {e}")
    
    def get_critical_mass(self, row: int, col: int) -> int:
        """Calculate critical mass for a cell based on its neighbors"""
        neighbors = 0
        directions = [(-1, 0), (1, 0), (0, -1), (0, 1)]
        
        for dr, dc in directions:
            nr, nc = row + dr, col + dc
            if 0 <= nr < ROWS and 0 <= nc < COLS:
                neighbors += 1
        
        return neighbors
    
    def is_valid_move(self, row: int, col: int) -> bool:
        """Check if move is valid for current player"""
        if not (0 <= row < ROWS and 0 <= col < COLS):
            return False
        
        return self.board[row][col] == 0 or self.colors[row][col] == self.current_player
    
    def make_move(self, row: int, col: int) -> bool:
        """Make a move and update game state file"""
        if not self.is_valid_move(row, col) or self.waiting_for_ai or self.game_over:
            return False
        

        self.board[row][col] += 1
        self.colors[row][col] = self.current_player
        self.move_count += 1
        
        self.write_game_state("Human Move:")
        
        self.waiting_for_ai = True
        self.ai_thinking = True
        self.current_player = 'B'
        
        return True
    
    def write_game_state(self, header: str):
        """Write current game state to file"""
        try:
            with open(GAME_STATE_FILE, 'w') as f:
                f.write(header + "\n")
                for i in range(ROWS):
                    row_data = []
                    for j in range(COLS):
                        if self.board[i][j] == 0:
                            row_data.append("0")
                        else:
                            row_data.append(f"{self.board[i][j]}{self.colors[i][j]}")
                    f.write(" ".join(row_data) + "\n")
        except Exception as e:
            print(f"Error writing game state: {e}")
    
    def read_game_state(self) -> bool:
        """Read game state from file"""
        try:
            if not os.path.exists(GAME_STATE_FILE):
                return False
            
            with open(GAME_STATE_FILE, 'r') as f:
                lines = f.readlines()
            
            if len(lines) < ROWS + 1:
                return False
            
            header = lines[0].strip()
            
            # Parse board state
            for i in range(ROWS):
                if i + 1 < len(lines):
                    cells = lines[i + 1].strip().split()
                    for j in range(min(COLS, len(cells))):
                        cell = cells[j]
                        if cell == "0":
                            self.board[i][j] = 0
                            self.colors[i][j] = ' '
                        else:
                            self.board[i][j] = int(cell[:-1])
                            self.colors[i][j] = cell[-1]
            
            # Check if AI made a move
            if header == "AI Move:" and self.waiting_for_ai:
                self.waiting_for_ai = False
                self.ai_thinking = False
                self.current_player = 'R'
                self.check_game_over()
            
            return True
            
        except Exception as e:
            print(f"Error reading game state: {e}")
            return False
    
    def watch_file_changes(self):
        """Watch for file changes in a separate thread"""
        last_modified = 0
        
        while self.file_watcher_running:
            try:
                if os.path.exists(GAME_STATE_FILE):
                    current_modified = os.path.getmtime(GAME_STATE_FILE)
                    if current_modified > last_modified:
                        last_modified = current_modified
                        self.process_game_state()
                
                time.sleep(0.1)  # Check every 100ms
                
            except Exception as e:
                print(f"Error in file watcher: {e}")
                time.sleep(1)
    
    def process_game_state(self):
        """Process game state from file"""
        try:
            with open(GAME_STATE_FILE, 'r') as f:
                content = f.read()
                
            lines = content.split('\n')
            if len(lines) < 1:
                return
                
            move_type = lines[0].strip()
            
            # Handle different move types
            if move_type.startswith("Human Move:"):
                self.waiting_for_ai = True
                self.ai_thinking = True
            elif move_type.startswith("AI Move:"):
                self.current_player = 'R'  # Switch back to human
                self.waiting_for_ai = False
                self.ai_thinking = False
                self.move_count += 1
            elif move_type.startswith("AI vs AI:"):
                self.ai_vs_ai_mode = True
                self.waiting_for_ai = True
                self.ai_thinking = True
            elif move_type.startswith("AI B Move:"):
                self.current_player = 'R'
                self.waiting_for_ai = self.ai_vs_ai_mode
                self.ai_thinking = False
                self.move_count += 1
            elif move_type.startswith("AI R Move:"):
                self.current_player = 'B'
                self.waiting_for_ai = True
                self.ai_thinking = True
                self.move_count += 1
            
            # Parse board state
            for i in range(ROWS):
                if i + 1 < len(lines):
                    cells = lines[i + 1].strip().split()
                    for j in range(COLS):
                        if j < len(cells):
                            cell = cells[j]
                            if cell == '0':
                                self.board[i][j] = 0
                                self.colors[i][j] = ' '
                            else:
                                self.board[i][j] = int(cell[:-1])
                                self.colors[i][j] = cell[-1]
            
            # Check for game over
            self.check_game_over()
            
        except Exception as e:
            print(f"Error processing game state: {e}")
        
    
    def check_game_over(self):
        """Check if game is over and determine winner"""
        # Don't check for winner before 2 moves
        if self.move_count < 2:
            return

        active_colors = set()
        
        for i in range(ROWS):
            for j in range(COLS):
                if self.board[i][j] > 0:
                    active_colors.add(self.colors[i][j])
        
        if len(active_colors) <= 1:
            self.game_over = True
            if len(active_colors) == 1:
                self.winner = list(active_colors)[0]
    
    def get_cell_from_mouse(self, pos: Tuple[int, int]) -> Optional[Tuple[int, int]]:
        """Convert mouse position to board cell coordinates"""
        x, y = pos
        
        # Check if click is within board area
        if (BOARD_OFFSET_X <= x <= BOARD_OFFSET_X + COLS * CELL_SIZE and
            BOARD_OFFSET_Y <= y <= BOARD_OFFSET_Y + ROWS * CELL_SIZE):
            
            col = (x - BOARD_OFFSET_X) // CELL_SIZE
            row = (y - BOARD_OFFSET_Y) // CELL_SIZE
            
            if 0 <= row < ROWS and 0 <= col < COLS:
                return (row, col)
        
        return None
    
    def draw_orb(self, surface, x: int, y: int, color: str, count: int):
        """Draw orbs in a cell"""
        orb_color = RED if color == 'R' else BLUE if color == 'B' else WHITE
        orb_radius = 8
        
        if count == 1:
            # Single orb in center
            pygame.draw.circle(surface, orb_color, (x + CELL_SIZE//2, y + CELL_SIZE//2), orb_radius)
            pygame.draw.circle(surface, BLACK, (x + CELL_SIZE//2, y + CELL_SIZE//2), orb_radius, 2)
        
        elif count == 2:
            # Two orbs side by side
            pygame.draw.circle(surface, orb_color, (x + CELL_SIZE//3, y + CELL_SIZE//2), orb_radius)
            pygame.draw.circle(surface, BLACK, (x + CELL_SIZE//3, y + CELL_SIZE//2), orb_radius, 2)
            pygame.draw.circle(surface, orb_color, (x + 2*CELL_SIZE//3, y + CELL_SIZE//2), orb_radius)
            pygame.draw.circle(surface, BLACK, (x + 2*CELL_SIZE//3, y + CELL_SIZE//2), orb_radius, 2)
        
        elif count == 3:
            # Three orbs in triangle
            pygame.draw.circle(surface, orb_color, (x + CELL_SIZE//2, y + CELL_SIZE//3), orb_radius)
            pygame.draw.circle(surface, BLACK, (x + CELL_SIZE//2, y + CELL_SIZE//3), orb_radius, 2)
            pygame.draw.circle(surface, orb_color, (x + CELL_SIZE//3, y + 2*CELL_SIZE//3), orb_radius)
            pygame.draw.circle(surface, BLACK, (x + CELL_SIZE//3, y + 2*CELL_SIZE//3), orb_radius, 2)
            pygame.draw.circle(surface, orb_color, (x + 2*CELL_SIZE//3, y + 2*CELL_SIZE//3), orb_radius)
            pygame.draw.circle(surface, BLACK, (x + 2*CELL_SIZE//3, y + 2*CELL_SIZE//3), orb_radius, 2)
        
        elif count >= 4:
            # Four orbs in corners, with number in center if more than 4
            positions = [(CELL_SIZE//4, CELL_SIZE//4), (3*CELL_SIZE//4, CELL_SIZE//4),
                        (CELL_SIZE//4, 3*CELL_SIZE//4), (3*CELL_SIZE//4, 3*CELL_SIZE//4)]
            
            for px, py in positions:
                pygame.draw.circle(surface, orb_color, (x + px, y + py), orb_radius)
                pygame.draw.circle(surface, BLACK, (x + px, y + py), orb_radius, 2)
            
            if count > 4:
                # Draw number in center
                text = self.font.render(str(count), True, BLACK)
                text_rect = text.get_rect(center=(x + CELL_SIZE//2, y + CELL_SIZE//2))
                pygame.draw.circle(surface, WHITE, (x + CELL_SIZE//2, y + CELL_SIZE//2), 12)
                pygame.draw.circle(surface, BLACK, (x + CELL_SIZE//2, y + CELL_SIZE//2), 12, 2)
                surface.blit(text, text_rect)
    
    def draw_board(self):
        """Draw the game board"""
        for i in range(ROWS):
            for j in range(COLS):
                x = BOARD_OFFSET_X + j * CELL_SIZE
                y = BOARD_OFFSET_Y + i * CELL_SIZE
                
                # Draw cell background
                cell_color = LIGHT_GRAY
                if self.current_player == 'R' and not self.waiting_for_ai and not self.game_over:
                    if self.is_valid_move(i, j):
                        cell_color = (220, 255, 220)  # Light green for valid moves
                
                pygame.draw.rect(self.screen, cell_color, (x, y, CELL_SIZE, CELL_SIZE))
                pygame.draw.rect(self.screen, BLACK, (x, y, CELL_SIZE, CELL_SIZE), 2)
                
                # Draw critical mass indicator in corner
                critical_mass = self.get_critical_mass(i, j)
                cm_text = self.font.render(str(critical_mass), True, GRAY)
                self.screen.blit(cm_text, (x + 5, y + 5))
                
                # Draw orbs if any
                if self.board[i][j] > 0:
                    self.draw_orb(self.screen, x, y, self.colors[i][j], self.board[i][j])
    
    # def draw_ui(self):
    #     """Draw UI elements"""
    #     # Title
    #     title = self.big_font.render("Chain Reaction", True, BLACK)
    #     title_rect = title.get_rect(center=(WINDOW_WIDTH//2, 25))
    #     self.screen.blit(title, title_rect)
        
    #     # AI vs AI button
    #     pygame.draw.rect(self.screen, GREEN if self.ai_vs_ai_mode else RED, self.ai_vs_ai_button)
    #     button_text = self.font.render("Refresh:" if not self.ai_vs_ai_mode else "Refresh:", True, WHITE)
    #     self.screen.blit(button_text, (self.ai_vs_ai_button.x + 10, self.ai_vs_ai_button.y + 5))
        
    #     # Game status
    #     if self.game_over:
    #         if self.winner == 'R':
    #             status_text = "Red AI Wins!" if self.ai_vs_ai_mode else "You Win! (Red)"
    #             status_color = RED
    #         elif self.winner == 'B':
    #             status_text = "Blue AI Wins!" if self.ai_vs_ai_mode else "AI Wins! (Blue)"
    #             status_color = BLUE
    #         else:
    #             status_text = "Draw!"
    #             status_color = BLACK
    #     elif self.waiting_for_ai:
    #         if self.ai_thinking:
    #             status_text = "AI is thinking..."
    #         else:
    #             status_text = "Waiting for AI..."
    #         status_color = BLUE if self.current_player == 'B' else RED
    #     else:
    #         status_text = "Your turn (Red)" if not self.ai_vs_ai_mode else f"{self.current_player}'s turn"
    #         status_color = RED if self.current_player == 'R' else BLUE
        
    #     status = self.font.render(status_text, True, status_color)
    #     status_rect = status.get_rect(center=(WINDOW_WIDTH//2, WINDOW_HEIGHT - 100))
    #     self.screen.blit(status, status_rect)
        
    #     # Instructions
    #     if not self.game_over and not self.waiting_for_ai and not self.ai_vs_ai_mode:
    #         instruction = self.font.render("Click on a cell to place your orb", True, BLACK)
    #         instruction_rect = instruction.get_rect(center=(WINDOW_WIDTH//2, WINDOW_HEIGHT - 70))
    #         self.screen.blit(instruction, instruction_rect)
        
    #     # Move counter and orb counts
    #     red_count = sum(self.board[i][j] for i in range(ROWS) for j in range(COLS) if self.colors[i][j] == 'R')
    #     blue_count = sum(self.board[i][j] for i in range(ROWS) for j in range(COLS) if self.colors[i][j] == 'B')
        
    #     red_text = self.font.render(f"Red Orbs: {red_count}", True, RED)
    #     blue_text = self.font.render(f"Blue Orbs: {blue_count}", True, BLUE)
    #     moves_text = self.font.render(f"Moves: {self.move_count}", True, BLACK)
        
    #     self.screen.blit(red_text, (10, WINDOW_HEIGHT - 65))
    #     self.screen.blit(blue_text, (10, WINDOW_HEIGHT - 40))
    #     self.screen.blit(moves_text, (10, WINDOW_HEIGHT - 20))
        
    #     # Reset button if game is over
    #     if self.game_over:
    #         reset_text = self.font.render("Press R to restart", True, BLACK)
    #         reset_rect = reset_text.get_rect(center=(WINDOW_WIDTH//2, WINDOW_HEIGHT - 40))
    #         self.screen.blit(reset_text, reset_rect)
    def draw_ui(self):
        """Draw UI elements"""
        # Title
        title = self.big_font.render("Chain Reaction", True, BLACK)
        title_rect = title.get_rect(center=(WINDOW_WIDTH//2, 25))
        self.screen.blit(title, title_rect)
        
        # AI vs AI button
        pygame.draw.rect(self.screen, GREEN if self.ai_vs_ai_mode else RED, self.ai_vs_ai_button)
        button_text = self.font.render("Toggle AI vs AI", True, WHITE)
        self.screen.blit(button_text, (self.ai_vs_ai_button.x + 10, self.ai_vs_ai_button.y + 5))

        # Game status and winner display
        if self.game_over:
            # Draw a semi-transparent background for winner announcement
            winner_surface = pygame.Surface((WINDOW_WIDTH, 150))
            winner_surface.set_alpha(200)
            winner_surface.fill(WHITE)
            self.screen.blit(winner_surface, (0, WINDOW_HEIGHT//2 - 75))
            
            # Determine winner text and color
            if self.winner == 'R':
                status_text = "Red AI Wins!" if self.ai_vs_ai_mode else "You Win! (Red)"
                status_color = RED
            elif self.winner == 'B':
                status_text = "Blue AI Wins!" if self.ai_vs_ai_mode else "AI Wins! (Blue)"
                status_color = BLUE
            else:
                status_text = "Draw!"
                status_color = BLACK

            winner_text = self.big_font.render(status_text, True, status_color)
            winner_rect = winner_text.get_rect(center=(WINDOW_WIDTH//2, WINDOW_HEIGHT//2))
            
            # Add white outline for better visibility
            outline_size = 2
            for dx, dy in [(-1,-1), (-1,1), (1,-1), (1,1)]:
                self.screen.blit(winner_text, 
                    (winner_rect.x + dx * outline_size, 
                    winner_rect.y + dy * outline_size))
            
            self.screen.blit(winner_text, winner_rect)

            # Show game statistics
            stats_text = f"Total Moves: {self.move_count}"
            stats = self.font.render(stats_text, True, BLACK)
            stats_rect = stats.get_rect(center=(WINDOW_WIDTH//2, WINDOW_HEIGHT//2 + 30))
            self.screen.blit(stats, stats_rect)

            # Show refresh button
            refresh_text = self.font.render("Press R to refresh", True, GREEN)
            refresh_rect = refresh_text.get_rect(center=(WINDOW_WIDTH//2, WINDOW_HEIGHT//2 + 60))
            self.screen.blit(refresh_text, refresh_rect)

        else:
            # Game status during play
            if self.waiting_for_ai:
                status_text = "AI is thinking..." if self.ai_thinking else "Waiting for AI..."
                status_color = BLUE if self.current_player == 'B' else RED
            else:
                status_text = "Your turn (Red)" if not self.ai_vs_ai_mode else f"{self.current_player}'s turn"
                status_color = RED if self.current_player == 'R' else BLUE
            
            status = self.font.render(status_text, True, status_color)
            status_rect = status.get_rect(center=(WINDOW_WIDTH//2, WINDOW_HEIGHT - 60))
            self.screen.blit(status, status_rect)

            if not self.waiting_for_ai and not self.ai_vs_ai_mode:
                instruction = self.font.render("Click on a cell to place your orb", True, BLACK)
                instruction_rect = instruction.get_rect(center=(WINDOW_WIDTH//2, WINDOW_HEIGHT - 30))
                self.screen.blit(instruction, instruction_rect)

        red_count = sum(self.board[i][j] for i in range(ROWS) for j in range(COLS) 
                    if self.colors[i][j] == 'R')
        blue_count = sum(self.board[i][j] for i in range(ROWS) for j in range(COLS) 
                        if self.colors[i][j] == 'B')
        
        red_text = self.font.render(f"Red Orbs: {red_count}", True, RED)
        blue_text = self.font.render(f"Blue Orbs: {blue_count}", True, BLUE)
        moves_text = self.font.render(f"Moves: {self.move_count}", True, BLACK)
        
        self.screen.blit(red_text, (10, WINDOW_HEIGHT - 65))
        self.screen.blit(blue_text, (10, WINDOW_HEIGHT - 40))
        self.screen.blit(moves_text, (10, WINDOW_HEIGHT - 15))
    
    def reset_game(self):
        """Reset the game to initial state"""
        self.board = [[0 for _ in range(COLS)] for _ in range(ROWS)]
        self.colors = [[' ' for _ in range(COLS)] for _ in range(ROWS)]
        self.current_player = 'R'
        self.game_over = False
        self.winner = None
        self.waiting_for_ai = self.ai_vs_ai_mode  
        self.ai_thinking = self.ai_vs_ai_mode
        self.move_count = 0
        self.initialize_game_state()
        
        if self.ai_vs_ai_mode:
            self.write_game_state("AI vs AI:")
    
    def handle_events(self):
        """Handle pygame events"""
        for event in pygame.event.get():
            if event.type == pygame.QUIT:
                self.file_watcher_running = False
                return False
            
            elif event.type == pygame.KEYDOWN:
                if event.key == pygame.K_r and self.game_over:
                    self.reset_game()
            
            elif event.type == pygame.MOUSEBUTTONDOWN:
                if event.button == 1:  
                    if self.ai_vs_ai_button.collidepoint(event.pos):
                        self.ai_vs_ai_mode = not self.ai_vs_ai_mode
                        self.reset_game()
                        if self.ai_vs_ai_mode:
                            self.write_game_state("AI vs AI:")

                    elif not self.ai_vs_ai_mode:
                        cell = self.get_cell_from_mouse(event.pos)
                        if cell and self.current_player == 'R':
                            row, col = cell
                            self.make_move(row, col)
        
        return True
    
    def run(self):
        """Main game loop"""
        print("Chain Reaction UI Started")
        print("Make sure to start the Java GameEngine for AI opponent!")
        
        running = True
        while running:
            running = self.handle_events()
            
            self.screen.fill(WHITE)
            self.draw_board()
            self.draw_ui()
            
            pygame.display.flip()
            self.clock.tick(60)
        
        self.file_watcher_running = False
        pygame.quit()
        sys.exit()

if __name__ == "__main__":
    game = ChainReactionUI()
    game.run()