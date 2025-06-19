import os
import time
from pathlib import Path

def get_gamestate_path():

    current_dir = Path(__file__).parent
    project_root = current_dir.parent  # Go up one level to project root
    return str(project_root / "gamestate.txt")

def read_game_state():
    filename = get_gamestate_path()
    while True:
        with open(filename,"r") as f:
            lines=f.read().strip().split("\n")

        if not lines or lines[0] not in ["Human Move:","AI Move:"]:
            continue

        move_type=lines[0].strip()
        board=[]
        for line in lines[1:]:
            if not line:
                continue
            row=[]
            for cell in line.strip().split():
                if cell == "0":
                    row.append((None,0))
                else:
                    count =int(cell[0])
                    owner=cell[1]
                    row.append((owner,count))
            board.append(row)

        if len(board)!=9  or any(len(r)!=6 for r in board):
            continue


        return move_type,board

def write_game_state(move_type,board):
    filename = get_gamestate_path()
    lines = [move_type]
    for row in board:
        line=[]
        for cell in row:
            owner,count=cell
            if owner is None or count ==0:
                line.append("0")
            else:
                line.append(f"{count}{owner}")
        lines.append(" ".join(line))


    with open(filename,"w") as f:
        f.write("\n".join(lines)+"\n")


