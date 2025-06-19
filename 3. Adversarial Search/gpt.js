<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Chain Reaction Game</title>
    <style>
        #board {
            display: grid;
            grid-template-columns: repeat(6, 50px);
            grid-template-rows: repeat(9, 50px);
            gap: 5px;
        }
        .cell {
            width: 50px;
            height: 50px;
            display: flex;
            justify-content: center;
            align-items: center;
            border: 1px solid #ddd;
            font-weight: bold;
        }
        .red {
            background-color: red;
            color: white;
        }
        .blue {
            background-color: blue;
            color: white;
        }
    </style>
</head>
<body>
    <h1>Chain Reaction Game</h1>
    <div id="board"></div>
    <script>
        const board = [
            ['0', '0', '0', '0', '0', '0'],
            ['0', '0', '0', '0', '0', '0'],
            ['0', '0', '0', '0', '0', '0'],
            ['0', '0', '0', '0', '0', '0'],
            ['0', '0', '0', '0', '0', '0'],
            ['0', '0', '0', '0', '0', '0'],
            ['0', '0', '0', '0', '0', '0'],
            ['0', '0', '0', '0', '0', '0'],
            ['0', '0', '0', '0', '0', '0']
        ];

        let isHumanTurn = true; // True if it's the human's turn, false for AI's turn

        // Render the game board
        function renderBoard() {
            const boardElement = document.getElementById('board');
            boardElement.innerHTML = ''; // Clear previous board
            board.forEach((row, i) => {
                row.forEach((cell, j) => {
                    const cellElement = document.createElement('div');
                    cellElement.classList.add('cell');
                    if (cell === '0') {
                        cellElement.textContent = '';
                    } else {
                        const [orbs, color] = cell.split('');
                        cellElement.textContent = orbs;
                        cellElement.classList.add(color.toLowerCase());
                    }
                    cellElement.addEventListener('click', () => handleHumanMove(i, j));
                    boardElement.appendChild(cellElement);
                });
            });
        }

        // Handle human move
        function handleHumanMove(i, j) {
            if (isHumanTurn && board[i][j] === '0') {
                board[i][j] = '1R'; // Human places red orb
                isHumanTurn = false;
                renderBoard();
                handleAIMove(); // AI makes its move after the human
            }
        }

        // AI move using minimax with alpha-beta pruning
        function handleAIMove() {
            const bestMove = minimax(board, 3, -Infinity, Infinity, true);
            makeMove(board, bestMove, 'AI');
            isHumanTurn = true;
            renderBoard();
        }

        // Minimax Algorithm with Alpha-Beta Pruning
        function minimax(board, depth, alpha, beta, maximizingPlayer) {
            if (depth === 0 || gameOver(board)) {
                return { value: evaluateBoard(board), move: null };
            }

            let bestMove = null;
            if (maximizingPlayer) {
                let maxEval = -Infinity;
                const moves = getPossibleMoves(board, 'AI');
                moves.forEach(move => {
                    const newBoard = makeMove([...board], move, 'AI');
                    const eval = minimax(newBoard, depth - 1, alpha, beta, false).value;
                    if (eval > maxEval) {
                        maxEval = eval;
                        bestMove = move;
                    }
                    alpha = Math.max(alpha, eval);
                    if (beta <= alpha) {
                        return { value: maxEval, move: bestMove };
                    }
                });
                return { value: maxEval, move: bestMove };
            } else {
                let minEval = Infinity;
                const moves = getPossibleMoves(board, 'Human');
                moves.forEach(move => {
                    const newBoard = makeMove([...board], move, 'Human');
                    const eval = minimax(newBoard, depth - 1, alpha, beta, true).value;
                    if (eval < minEval) {
                        minEval = eval;
                        bestMove = move;
                    }
                    beta = Math.min(beta, eval);
                    if (beta <= alpha) {
                        return { value: minEval, move: bestMove };
                    }
                });
                return { value: minEval, move: bestMove };
            }
        }

        // Evaluate the board with basic heuristics
        function evaluateBoard(board) {
            let score = 0;
            for (let row of board) {
                for (let cell of row) {
                    if (cell.includes('R')) score++; // Reward for red orbs
                    if (cell.includes('B')) score--; // Penalize for blue orbs
                }
            }
            return score;
        }

        // Generate possible moves for the player (AI or Human)
        function getPossibleMoves(board, player) {
            const moves = [];
            for (let i = 0; i < board.length; i++) {
                for (let j = 0; j < board[i].length; j++) {
                    if (board[i][j] === '0') {
                        moves.push({ i, j });
                    }
                }
            }
            return moves;
        }

        // Apply a move to the board
        function makeMove(board, move, player) {
            const { i, j } = move;
            if (player === 'AI') {
                board[i][j] = '1B'; // AI places blue orb
            } else {
                board[i][j] = '1R'; // Human places red orb
            }
            return board;
        }

        // Check if the game is over (placeholder for actual game-over logic)
        function gameOver(board) {
            // Simple check: if no empty cells are left, game is over
            for (let row of board) {
                for (let cell of row) {
                    if (cell === '0') return false;
                }
            }
            return true;
        }

        // Initialize the game
        renderBoard();
    </script>
</body>
</html>
