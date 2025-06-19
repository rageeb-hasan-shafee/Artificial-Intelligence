// #include <iostream>
// #include <vector>
// #include <queue>
// #include <set>
// #include <cmath>
// #include <algorithm>
// #include <unordered_map>

// using namespace std;

// struct Node {
//     vector<vector<int>> board;
//     int g, h; // g = moves so far, h = heuristic cost
//     int blank_row, blank_col;
//     Node* parent; // for path reconstruction

//     Node(vector<vector<int>> b, int g_, Node* p = nullptr) : board(b), g(g_), parent(p) {
//         int n = board.size();
//         h = 0;
//         for (int i = 0; i < n; i++) 
//             for (int j = 0; j < n; j++) 
//                 if (board[i][j] != 0) {
//                     int val = board[i][j] - 1;
//                     h += abs(i - val / n) + abs(j - val % n);
//                 }
//                 else {
//                     blank_row = i;
//                     blank_col = j;
//                 }
//     }

//     int f() const { return g + h; }

//     bool operator>(const Node& other) const { return f() > other.f(); }
// };

// string boardToString(const vector<vector<int>>& board) {
//     string s;
//     for (auto& row : board)
//         for (int val : row)
//             s += to_string(val) + ",";
//     return s;
// }

// bool isSolvable(const vector<vector<int>>& board) {
//     int n = board.size();
//     vector<int> flat;
//     int blank_row = 0;
//     for (int i = 0; i < n; i++)
//         for (int j = 0; j < n; j++) {
//             if (board[i][j] == 0) blank_row = i;
//             else flat.push_back(board[i][j]);
//         }
    
//     int inv = 0;
//     for (int i = 0; i < flat.size(); i++)
//         for (int j = i + 1; j < flat.size(); j++)
//             if (flat[i] > flat[j]) inv++;

//     if (n % 2 == 1)
//         return inv % 2 == 0;
//     else
//         return (inv + (n - blank_row)) % 2 == 0;
// }

// void printBoard(const vector<vector<int>>& board) {
//     for (auto& row : board) {
//         for (int val : row)
//             cout << val << " ";
//         cout << "\n";
//     }
//     cout << "\n";
// }

// void solve(vector<vector<int>> start) {
//     int n = start.size();
//     vector<vector<int>> goal(n, vector<int>(n));
//     for (int i = 0; i < n * n - 1; i++) 
//         goal[i / n][i % n] = i + 1;
//     goal[n - 1][n - 1] = 0;

//     if (!isSolvable(start)) {
//         cout << "Unsolvable puzzle\n";
//         return;
//     }

//     priority_queue<Node, vector<Node>, greater<Node>> pq;
//     set<string> visited;
//     int explored = 0, expanded = 0;

//     pq.push(Node(start, 0));
//     explored++; // pushed initial node

//     Node* finalNode = nullptr;

//     while (!pq.empty()) {
//         Node current = pq.top();
//         pq.pop();
//         expanded++;

//         string currState = boardToString(current.board);
//         if (visited.count(currState)) continue; // already expanded
//         visited.insert(currState); // Mark only now

//         if (current.board == goal) {
//             finalNode = new Node(current);
//             break;
//         }

//         int dr[] = {-1, 1, 0, 0};
//         int dc[] = {0, 0, -1, 1};

//         for (int d = 0; d < 4; d++) {
//             int nr = current.blank_row + dr[d];
//             int nc = current.blank_col + dc[d];
//             if (nr >= 0 && nr < n && nc >= 0 && nc < n) {
//                 vector<vector<int>> newBoard = current.board;
//                 swap(newBoard[current.blank_row][current.blank_col], newBoard[nr][nc]);
//                 string newState = boardToString(newBoard);
//                 if (!visited.count(newState)) {
//                     pq.push(Node(newBoard, current.g + 1, new Node(current)));
//                     explored++;
//                 }
//             }
//         }
//     }

//     if (finalNode) {
//         cout << "Minimum number of moves = " << finalNode->g << "\n\n";

//         vector<vector<vector<int>>> path;
//         for (Node* node = finalNode; node != nullptr; node = node->parent)
//             path.push_back(node->board);

//         reverse(path.begin(), path.end());
//         for (auto& board : path)
//             printBoard(board);

//         cout << "Number of nodes explored = " << explored << "\n";
//         cout << "Number of nodes expanded = " << expanded << "\n";
//     }
// }

// int main() {
//     int n;
//     cin >> n;
//     vector<vector<int>> start(n, vector<int>(n));
//     for (int i = 0; i < n; i++) 
//         for (int j = 0; j < n; j++) 
//             cin >> start[i][j];

//     solve(start);
//     return 0;
// }


#include <iostream>
#include <vector>
#include <queue>
#include <cmath>
#include <unordered_set>
#include <algorithm>

using namespace std;

// Utility function to hash the board for closed set
struct BoardHash {
    size_t operator()(const vector<int>& board) const {
        size_t hash = 0;
        for (int num : board) {
            hash = hash * 31 + num;
        }
        return hash;
    }
};

// Heuristic functions
int hamming(const vector<int>& state, const vector<int>& goal) {
    int count = 0;
    for (size_t i = 0; i < state.size(); ++i) {
        if (state[i] != 0 && state[i] != goal[i]) ++count;
    }
    return count;
}

int manhattan(const vector<int>& state, const vector<int>& goal, int n) {
    int distance = 0;
    for (size_t idx = 0; idx < state.size(); ++idx) {
        if (state[idx] != 0) {
            size_t goal_idx = find(goal.begin(), goal.end(), state[idx]) - goal.begin();
            int x1 = idx / n, y1 = idx % n;
            int x2 = goal_idx / n, y2 = goal_idx % n;
            distance += abs(x1 - x2) + abs(y1 - y2);
        }
    }
    return distance;
}

int euclidean(const vector<int>& state, const vector<int>& goal, int n) {
    int distance = 0;
    for (size_t idx = 0; idx < state.size(); ++idx) {
        if (state[idx] != 0) {
            size_t goal_idx = find(goal.begin(), goal.end(), state[idx]) - goal.begin();
            int x1 = idx / n, y1 = idx % n;
            int x2 = goal_idx / n, y2 = goal_idx % n;
            distance += sqrt(pow(x1 - x2, 2) + pow(y1 - y2, 2));
        }
    }
    return distance;
}

int linear_conflict(const vector<int>& state, const vector<int>& goal, int n) {
    int manh = manhattan(state, goal, n);
    int conflict = 0;
    for (int row = 0; row < n; ++row) {
        for (int i = 0; i < n; ++i) {
            for (int j = i + 1; j < n; ++j) {
                int tile1 = state[row * n + i];
                int tile2 = state[row * n + j];
                if (tile1 && tile2) {
                    int goal_i = find(goal.begin(), goal.end(), tile1) - goal.begin();
                    int goal_j = find(goal.begin(), goal.end(), tile2) - goal.begin();
                    if (goal_i / n == row && goal_j / n == row && goal_i > goal_j)
                        ++conflict;
                }
            }
        }
    }
    return manh + 2 * conflict;
}

// Solvability Check
bool is_solvable(const vector<int>& board, int n) {
    int inv_count = 0;
    vector<int> temp;
    for (int num : board) if (num) temp.push_back(num);
    for (size_t i = 0; i < temp.size(); ++i) {
        for (size_t j = i + 1; j < temp.size(); ++j) {
            if (temp[i] > temp[j]) ++inv_count;
        }
    }

    if (n % 2 == 1) return inv_count % 2 == 0;
    else {
        int zero_row_from_bottom = n - (find(board.begin(), board.end(), 0) - board.begin()) / n;
        if (zero_row_from_bottom % 2 == 0) return inv_count % 2 == 1;
        else return inv_count % 2 == 0;
    }
}

// Node structure
struct Node {
    vector<int> board;
    int moves;
    int priority;
    Node* parent;

    Node(const vector<int>& b, int m, int p, Node* pr) : board(b), moves(m), priority(p), parent(pr) {}

    bool operator>(const Node& other) const {
        return priority > other.priority;
    }
};

// A* solver
void solve_puzzle(const vector<int>& start_board, int n, int (*heuristic)(const vector<int>&, const vector<int>&, int)) {
    vector<int> goal_board(n * n);
    for (int i = 0; i < n * n - 1; ++i) goal_board[i] = i + 1;

    if (start_board == goal_board) {
        cout << "Already solved!\n";
        return;
    }

    if (!is_solvable(start_board, n)) {
        cout << "Unsolvable puzzle\n";
        return;
    }

    auto cmp = [](Node* a, Node* b) { return *a > *b; };
    priority_queue<Node*, vector<Node*>, decltype(cmp)> open_list(cmp);
    unordered_set<vector<int>, BoardHash> closed_set;

    Node* start = new Node(start_board, 0, heuristic(start_board, goal_board, n), nullptr);
    open_list.push(start);

    int explored = 0, expanded = 0;

    while (!open_list.empty()) {
        Node* current = open_list.top(); open_list.pop();
        ++expanded;

        if (current->board == goal_board) {
            vector<Node*> path;
            while (current) {
                path.push_back(current);
                current = current->parent;
            }
            reverse(path.begin(), path.end());
            cout << "Minimum number of moves = " << path.size() - 1 << "\n";
            for (auto node : path) {
                for (int i = 0; i < n; ++i) {
                    for (int j = 0; j < n; ++j) cout << node->board[i * n + j] << " ";
                    cout << "\n";
                }
                cout << "\n";
            }
            cout << "Explored nodes: " << explored << "\n";
            cout << "Expanded nodes: " << expanded << "\n";
            return;
        }

        closed_set.insert(current->board);

        int zero_pos = find(current->board.begin(), current->board.end(), 0) - current->board.begin();
        int x = zero_pos / n, y = zero_pos % n;
        vector<pair<int, int>> directions = {{-1,0},{1,0},{0,-1},{0,1}};

        for (auto [dx, dy] : directions) {
            int nx = x + dx, ny = y + dy;
            if (nx >= 0 && nx < n && ny >= 0 && ny < n) {
                vector<int> new_board = current->board;
                swap(new_board[x * n + y], new_board[nx * n + ny]);
                if (closed_set.find(new_board) == closed_set.end()) {
                    Node* neighbor = new Node(new_board, current->moves + 1, current->moves + 1 + heuristic(new_board, goal_board, n), current);
                    open_list.push(neighbor);
                    ++explored;
                }
            }
        }
    }

    cout << "No solution found.\n";
}

int main() {
    int n;
    cin >> n;
    vector<int> board(n * n);
    for (int i = 0; i < n * n; ++i) cin >> board[i];

    cout << "Choose heuristic:\n1. Hamming\n2. Manhattan\n3. Euclidean\n4. Linear Conflict\n";
    int choice;
    cin >> choice;

    switch (choice) {
        case 1:
            solve_puzzle(board, n, [](const vector<int>& a, const vector<int>& b, int n) { return hamming(a, b); });
            break;
        case 2:
            solve_puzzle(board, n, manhattan);
            break;
        case 3:
            solve_puzzle(board, n, euclidean);
            break;
        case 4:
            solve_puzzle(board, n, linear_conflict);
            break;
        default:
            cout << "Invalid choice\n";
            break;
    }
    return 0;
}