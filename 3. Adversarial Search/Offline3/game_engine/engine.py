from copy import deepcopy

ROWS=9
COLS=6


def get_critical_mass(row,col):
    if(row in [0 ,ROWS-1]) and (col in [0,COLS-1]):
        return 2
    elif row  in [0,ROWS-1] or col in [0,COLS-1]:
        return 3
    else :
        return 4

def get_neighbors(row, col):
    neighbors = []
    if row > 0:
        neighbors.append((row - 1, col))
    if row < ROWS - 1:
        neighbors.append((row + 1, col))
    if col > 0:
        neighbors.append((row, col - 1))
    if col < COLS - 1:
        neighbors.append((row, col + 1))
    return neighbors

def valid_move(board,row,col,player):
    owner,_=board[row][col]
    return owner is None or owner== player

def make_move(board,row,col,player):
    new_board =deepcopy(board)
    stack = [(row,col)]

    if valid_move(new_board,row,col,player):
        owner,count=new_board[row][col]
        new_board[row][col]=(player,count+1)
    else :
        return None

    while stack:
        r,c=stack.pop()
        owner,count=new_board[r][c]
        if count >= get_critical_mass(r,c):
            new_board[r][c]=(None,count - get_critical_mass(r, c)) #Becomes 0 always

            for new_row,new_col in get_neighbors(r,c):
                new_owner,new_count=new_board[new_row][new_col]
                new_board[new_row][new_col]=(player,new_count+1)

                if new_board[new_row][new_col][1] >=get_critical_mass(new_row,new_col):
                    stack.append((new_row,new_col))

    return new_board

def count_player_orbs(board, player):
    count = 0
    for row in board:
        for owner, cnt in row:
            if owner == player:
                count += cnt
    return count

def has_player_orbs(board, player):
    for row in board:
        for owner,count in row:
            if owner == player:
                return True
    return False


def check_winner(board,moves_made):
    r = has_player_orbs(board,'R')
    b = has_player_orbs(board, 'B')

    if moves_made[0]<2:
        return None

    if r and not b:
        return 'R'
    elif b and not r:
        return 'B'
    else:
        return None

def get_legal_moves(board, player):
    moves = []
    for i in range(ROWS):
        for j in range(COLS):
            if valid_move(board, i, j, player):
                moves.append((i, j))
    return moves

def is_terminal(board,moves_made):
    return check_winner(board,moves_made) is not None

def print_board(board):
    for row in board:
        print(' '.join(f"{count}{owner if owner else ' '}" if count > 0 else "  "
                      for owner, count in row))
    print()


