#include <iostream>
#include <vector>
#include <set>
#include <stack>

using namespace std;

// Define the dimensions of the puzzle
#define N 3

// Structure to store a state of the puzzle
struct PuzzleState {
    vector<vector<int>> board;  
    int x, y;  
    int depth; 

    PuzzleState(vector<vector<int>> b, int i, int j, int d) : board(b), x(i), y(j), depth(d) {}
};

// Possible moves: Left, Right, Up, Down
int row[] = {0, 0, -1, 1};
int col[] = {-1, 1, 0, 0};

// Function to check if a given state is the goal state
bool isGoalState(vector<vector<int>>& board) {
    vector<vector<int>> goal = {{1, 2, 3}, {4, 5, 6}, {7, 8, 0}};
    return board == goal;
}

// Function to check if a move is valid
bool isValid(int x, int y) {
    return (x >= 0 && x < N && y >= 0 && y < N);
}

// Function to print the board
void printBoard(vector<vector<int>>& board) {
    for (auto& row : board) {
        for (auto& num : row)
            cout << num << " ";
        cout << endl;
    }
    cout << "--------" << endl;
}

// Depth-First Search to solve the 8-puzzle problem
void solvePuzzleDFS(vector<vector<int>>& start, int x, int y) {
    stack<PuzzleState> st;
    set<vector<vector<int>>> visited;
    
    st.push(PuzzleState(start, x, y, 0));
    visited.insert(start);

    while (!st.empty()) {
        PuzzleState curr = st.top();
        st.pop();

        // Print the current board
        cout << "Depth: " << curr.depth << endl;
        printBoard(curr.board);

        // Check if goal state is reached
        if (isGoalState(curr.board)) {
            cout << "Goal state reached at depth " << curr.depth << endl;
            return;
        }

        // Explore possible moves
        for (int i = 0; i < 4; i++) {
            int newX = curr.x + row[i];
            int newY = curr.y + col[i];

            if (isValid(newX, newY)) {
                vector<vector<int>> newBoard = curr.board;
                swap(newBoard[curr.x][curr.y], newBoard[newX][newY]);

                // If this state has not been visited before, push to stack
                if (visited.find(newBoard) == visited.end()) {
                    visited.insert(newBoard);
                    st.push(PuzzleState(newBoard, newX, newY, curr.depth + 1));
                }
            }
        }
    }

    cout << "No solution found (DFS Brute Force reached depth limit)" << endl;
}

// Driver Code
int main() {
    vector<vector<int>> start = {{1, 2, 3}, {4, 0, 5}, {6, 7, 8}};
    int x = 1, y = 1; 

    cout << "Initial State: " << endl;
    printBoard(start);

    solvePuzzleDFS(start, x, y);

    return 0;
}
