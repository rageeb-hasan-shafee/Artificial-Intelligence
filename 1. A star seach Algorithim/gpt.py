import math
import heapq
from typing import List, Tuple

# Heuristic functions

def hamming(state, goal):
    return sum(1 for i, tile in enumerate(state) if tile != 0 and tile != goal[i])

def manhattan(state, goal, n):
    distance = 0
    for idx, tile in enumerate(state):
        if tile != 0:
            goal_idx = goal.index(tile)
            x1, y1 = divmod(idx, n)
            x2, y2 = divmod(goal_idx, n)
            distance += abs(x1 - x2) + abs(y1 - y2)
    return distance

def euclidean(state, goal, n):
    distance = 0
    for idx, tile in enumerate(state):
        if tile != 0:
            goal_idx = goal.index(tile)
            x1, y1 = divmod(idx, n)
            x2, y2 = divmod(goal_idx, n)
            distance += math.sqrt((x1 - x2) ** 2 + (y1 - y2) ** 2)
    return distance

def linear_conflict(state, goal, n):
    manh = manhattan(state, goal, n)
    conflict = 0
    for row in range(n):
        current_row = state[row*n:(row+1)*n]
        goal_row = goal[row*n:(row+1)*n]
        for i in range(n):
            for j in range(i+1, n):
                if current_row[i] and current_row[j] and (goal_row.index(current_row[i]) > goal_row.index(current_row[j])):
                    conflict += 1
    return manh + 2 * conflict

# Utility functions

def is_solvable(board: List[int], n: int) -> bool:
    inv_count = 0
    board_wo_zero = [num for num in board if num != 0]
    for i in range(len(board_wo_zero)):
        for j in range(i + 1, len(board_wo_zero)):
            if board_wo_zero[i] > board_wo_zero[j]:
                inv_count += 1

    if n % 2 == 1:
        return inv_count % 2 == 0
    else:
        zero_row_from_bottom = n - (board.index(0) // n)
        if zero_row_from_bottom % 2 == 0:
            return inv_count % 2 == 1
        else:
            return inv_count % 2 == 0

# A* Search Implementation

def solve_puzzle(start_board: List[int], n: int, heuristic_func):
    goal_board = list(range(1, n*n)) + [0]

    if start_board == goal_board:
        print("Already solved!")
        return

    if not is_solvable(start_board, n):
        print("Unsolvable puzzle")
        return

    class Node:
        def __init__(self, board, moves, prev):
            self.board = board
            self.moves = moves
            self.prev = prev
            self.priority = self.moves + heuristic_func(self.board, goal_board, n)

        def __lt__(self, other):
            return self.priority < other.priority

    def neighbors(board):
        neighbors = []
        zero_pos = board.index(0)
        x, y = divmod(zero_pos, n)
        directions = [(-1, 0), (1, 0), (0, -1), (0, 1)]
        for dx, dy in directions:
            nx, ny = x + dx, y + dy
            if 0 <= nx < n and 0 <= ny < n:
                new_board = board.copy()
                new_zero_pos = nx * n + ny
                new_board[zero_pos], new_board[new_zero_pos] = new_board[new_zero_pos], new_board[zero_pos]
                neighbors.append(new_board)
        return neighbors

    open_list = []
    closed_set = set()
    explored_nodes = 0
    expanded_nodes = 0

    start_node = Node(start_board, 0, None)
    heapq.heappush(open_list, start_node)

    while open_list:
        current = heapq.heappop(open_list)
        expanded_nodes += 1

        if current.board == goal_board:
            path = []
            while current:
                path.append(current.board)
                current = current.prev
            path.reverse()

            print(f"Minimum number of moves = {len(path) - 1}")
            for board in path:
                for i in range(n):
                    print(*board[i*n:(i+1)*n])
                print()
            print(f"Explored nodes: {explored_nodes}")
            print(f"Expanded nodes: {expanded_nodes}")
            return

        closed_set.add(tuple(current.board))

        for neighbor in neighbors(current.board):
            if tuple(neighbor) not in closed_set:
                heapq.heappush(open_list, Node(neighbor, current.moves + 1, current))
                explored_nodes += 1

    print("No solution found.")

# Main function to take input
if __name__ == "__main__":
    n = int(input())
    board = []
    for _ in range(n):
        board.extend(map(int, input().split()))

    print("Choose heuristic:")
    print("1. Hamming")
    print("2. Manhattan")
    print("3. Euclidean")
    print("4. Linear Conflict")
    choice = int(input())

    if choice == 1:
        heuristic = lambda board, goal, n: hamming(board, goal)
    elif choice == 2:
        heuristic = manhattan
    elif choice == 3:
        heuristic = euclidean
    elif choice == 4:
        heuristic = linear_conflict
    else:
        print("Invalid choice")
        exit(1)

    solve_puzzle(board, n, heuristic)
