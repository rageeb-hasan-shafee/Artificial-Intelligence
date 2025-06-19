# ===== ai.py =====
from engine import *
import math

shift =1

# Heuristic 1: Orb Count Difference
def heuristic1(board, player):
    opp = 'B' if player == 'R' else 'R'
    player_orb = count_player_orbs(board, player)
    opp_orb = count_player_orbs(board, opp)
    return player_orb - opp_orb


# Heuristic 2: Critical Mass Proximity
def heuristic2(board, player):
    total = 0
    count_cells = 0
    for i in range(ROWS):
        for j in range(COLS):
            owner, cnt = board[i][j]
            if owner == player:
                critical = get_critical_mass(i, j)
                total += cnt / critical
                count_cells += 1
    return total / count_cells if count_cells else 0


# Heuristic 3: Mobility
def heuristic3(board, player):
    return len(get_legal_moves(board, player))


# Heuristic 4: Threat Detection
def heuristic4(board, player):
    opp = 'B' if player == 'R' else 'R'
    threats = 0
    for i in range(ROWS):
        for j in range(COLS):
            owner, cnt = board[i][j]
            if owner == opp:
                critical = get_critical_mass(i, j)
                if cnt == critical - 1:
                    for ni, nj in get_neighbors(i, j):
                        n_owner, _ = board[ni][nj]
                        if n_owner == player:
                            threats += 1
                            break
    return threats


# Heuristic 5: Strategic Position
def heuristic5(board, player):
    score = 0
    for i in range(ROWS):
        for j in range(COLS):
            owner, cnt = board[i][j]
            if owner == player:
                if i in [0, ROWS - 1] and j in [0, COLS - 1]:
                    weight = 3  # Corner
                elif i in [0, ROWS - 1] or j in [0, COLS - 1]:
                    weight = 2  # Edge
                else:
                    weight = 1  # Center
                score += weight * cnt
    return score


class AIPlayer:
    def __init__(self, player, heuristic, depth=3):
        self.player = player
        self.opponent = 'B' if player == 'R' else 'R'
        self.heuristic = heuristic
        self.depth = depth
        self.moves_made=[0]

    def minimax(self, state, depth, alpha, beta, maximizing):
        #global shift
        #print(f"insideMinmax{shift}")
        #shift +=1
        if depth == 0 or is_terminal(state,self.moves_made):
            return self.heuristic(state, self.player), None

        moves = get_legal_moves(state, self.player if maximizing else self.opponent)
        if not moves:
            return self.heuristic(state, self.player), None

        best_move = None
        if maximizing:
            max_eval = -math.inf
            for move in moves:
                r, c = move
                new_state = make_move(state, r, c, self.player)
                if new_state is None:
                    continue

                eval_val, _ = self.minimax(new_state, depth - 1, alpha, beta, False)
                if eval_val > max_eval:
                    max_eval = eval_val
                    best_move = move
                alpha = max(alpha, eval_val)
                if beta <= alpha:
                    break
            return max_eval, best_move
        else:
            min_eval = math.inf
            for move in moves:
                r, c = move
                new_state = make_move(state, r, c, self.opponent)
                if new_state is None:
                    continue

                eval_val, _ = self.minimax(new_state, depth - 1, alpha, beta, True)
                if eval_val < min_eval:
                    min_eval = eval_val
                    best_move = move
                beta = min(beta, eval_val)
                if beta <= alpha:
                    break
            return min_eval, best_move

    def get_move(self, state):
        _, move = self.minimax(state, self.depth, -math.inf, math.inf, True)
        self.moves_made[0]+=1
        print(move)
        return move


# ===== END ai.py =====

# ===== game_engine.py =====
from protocol import read_game_state, write_game_state
from engine import make_move, is_terminal, check_winner

import time


def main():
    ai_player = AIPlayer('B', heuristic1, depth=3)
    moves_made=[0]

    while True:


        try:
            move_type, board = read_game_state()

            if move_type == "AI Move:":
                # AI's turn
                move = ai_player.get_move(board)
                #print(move)
                if move:
                    r, c = move
                    new_board = make_move(board, r, c, 'B')
                    moves_made[0]+=1


                    if new_board is None:
                        new_board = board  # Fallback if move fails
                else:
                    new_board = board  # No valid moves

                write_game_state("Human Move:", new_board)

            # Check for game end
            winner = check_winner(board,moves_made)
            if winner:
                print(f"Game Over! Winner: {winner}")
                print("Winner")
                break
            #print("sleeping")
            time.sleep(0.5)  # Prevent busy waiting

        except KeyboardInterrupt:
            print("AI engine stopped")
            break
        except Exception as e:
            print(f"Error: {e}")
            time.sleep(1)


if __name__ == "__main__":
    main()
# ===== END game_engine.py =====