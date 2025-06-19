import os
import csv
import random
from concurrent.futures import ThreadPoolExecutor
import threading
random.seed(42) 

known_best_solutions = {
    "G1": 12078, "G2": 12084, "G3": 12077, "G14": 3187, "G15": 3169, "G16": 3172,
    "G32": 1560, "G43": 7027, "G44": 7022, "G45": 7020, "G11": 627, "G22": 14123,
    "G35": 8000, "G48": 6000, "G12": 621, "G23": 14129, "G36": 7996, "G49": 6000,
    "G13": 645, "G24": 14131, "G37": 8009, "G50": 5988,
    # Add the remaining graphs from the table...
}

class Graph:
    def __init__(self, n):
        self.n = n
        self.adj = [{} for _ in range(n)]
        self.edge_count = 0

    def add_edge(self, u, v, w):
        self.adj[u][v] = w
        self.adj[v][u] = w
        self.edge_count += 1

def read_rud_file(path):
    with open(path, 'r') as f:
        lines = f.readlines()
    n, m = map(int, lines[0].split())
    g = Graph(n)
    for line in lines[1:]:
        u, v, w = map(int, line.split())
        g.add_edge(u - 1, v - 1, w)
    return g

def cut_value(graph, partition):
    total = 0
    for u in range(graph.n):
        for v, w in graph.adj[u].items():
            if partition[u] != partition[v]:
                total += w
    return total // 2

def random_partition(graph):
    return [random.randint(0, 1) for _ in range(graph.n)]
def randomized_max_cut(graph, n):
    total_cut_weight = 0
    for i in range(n):
        X = set()
        Y = set()

        for v in range(graph.n):
            if random.random() >= 0.5:
                X.add(v)
            else:
                Y.add(v)

        cut_weight = 0  
        for u in range(graph.n):
            for v, w in graph.adj[u].items():
                if (u in X and v in Y) or (u in Y and v in X):
                    cut_weight += w 
        cut_weight = cut_weight // 2             
        total_cut_weight += cut_weight

    # Average the cut weights over n runs
    average_cut_weight = total_cut_weight / n
    
    # average_cut_weight = average_cut_weight * 0.85  # Reducing by 15%

    # # Ensure that the cut weight is never negative
    # if average_cut_weight < 0:
    #     average_cut_weight = 0
    return average_cut_weight

def greedy_partition(graph):
    partition = [0] * graph.n
    for u in range(graph.n):
        score_0 = sum(w for v, w in graph.adj[u].items() if partition[v] == 1)
        score_1 = sum(w for v, w in graph.adj[u].items() if partition[v] == 0)
        partition[u] = 0 if score_0 > score_1 else 1
    return partition

def semi_greedy_initial(graph, alpha=0.3):
    n = graph.n
    partition = [None] * n
    remaining = list(range(n))
    random.shuffle(remaining)
    
    # Initialize the greedy function values for each vertex
    sigma_X = [0] * n
    sigma_Y = [0] * n
    
    for i in range(n):
        for j, w in graph.adj[i].items():
            if partition[j] == 0:
                sigma_X[i] += w
            elif partition[j] == 1:
                sigma_Y[i] += w

    # Determine the minimum and maximum greedy function values
    w_min = min(min(sigma_X), min(sigma_Y))
    w_max = max(max(sigma_X), max(sigma_Y))
    
    # Calculate the cut-off value
    mu = w_min + alpha * (w_max - w_min)

    # Assign vertices to partitions based on the RCL (restricted candidate list)
    for i in remaining:
        gain_if_0 = 0
        gain_if_1 = 0
        for j, w in graph.adj[i].items():
            if partition[j] == 0:
                gain_if_1 += w
            elif partition[j] == 1:
                gain_if_0 += w
        
        # Compute the greedy function value
        greedy_value = max(gain_if_0, gain_if_1)
        
        # If the greedy value is above the threshold, choose the vertex for partitioning
        if greedy_value >= mu:
            partition[i] = 0 if gain_if_0 > gain_if_1 else 1
        else:
            # If the greedy value is less than the threshold, place it randomly
            partition[i] = random.choice([0, 1])
    
    return partition

# def local_search(graph, partition):
#     improved = True
#     iterations = 0
#     while improved:
#         improved = False
#         iterations += 1
#         for u in range(graph.n):
#             delta = 0
#             for v, w in graph.adj[u].items():
#                 if partition[u] == partition[v]:
#                     delta += w
#                 else:
#                     delta -= w
#             if delta > 0:
#                 partition[u] ^= 1
#                 improved = True
#     return partition, iterations
def local_search(graph, partition):
    improved = True
    iterations = 0
    max_iterations = 100  # Ensure we limit the number of iterations
    
    while improved and iterations < max_iterations:
        improved = False
        iterations += 1
        for u in range(graph.n):
            delta = 0
            for v, w in graph.adj[u].items():
                if partition[u] == partition[v]:
                    delta += w
                else:
                    delta -= w
            if delta > 0:
                partition[u] ^= 1
                improved = True
    return partition, iterations


def grasp(graph, iterations=50, alpha=0.3):
    best_cut = 0
    best_partition = None
    for _ in range(iterations):
        p = semi_greedy_initial(graph, alpha)
        p, _ = local_search(graph, p)
        val = cut_value(graph, p)
        if val > best_cut:
            best_cut = val
            best_partition = p[:]
    return best_cut, best_partition

def parallel_grasp(graph, trials=4, iterations=50):
    best = [0, None]
    lock = threading.Lock()

    def run_grasp(_):
        nonlocal best
        val, part = grasp(graph, iterations)
        with lock:
            if val > best[0]:
                best[0], best[1] = val, part

    with ThreadPoolExecutor(max_workers=trials) as executor:
        executor.map(run_grasp, range(trials))

    return best


def run_all_algorithms(graph, name, n=10):  # Do not pass known_best here
    rand_score = (randomized_max_cut(graph, n))  # Use the new randomized_max_cut function
    rand_part = [None] * graph.n  # Just to maintain consistency in variable naming if needed
    
    greedy_part = greedy_partition(graph)
    greedy_score = cut_value(graph, greedy_part)

    semi_greedy_part = semi_greedy_initial(graph)
    semi_score = cut_value(graph, semi_greedy_part)

    local_runs = 50
    local_total, total_iters = 0, 0
    for _ in range(local_runs):
        part = random_partition(graph)
        part, iters = local_search(graph, part)
        local_total += cut_value(graph, part)
        total_iters += iters
    local_avg = local_total // local_runs
    local_iters = total_iters // local_runs
    # local_iters = total_iters

    grasp_best, _ = parallel_grasp(graph, trials=8, iterations=100)

    return [name, graph.n, graph.edge_count,
            rand_score, greedy_score, semi_score,
            local_iters, local_avg, 100, grasp_best]  # Do not append KnownBest here

def batch_process_rud_files(input_folder, output_csv):
    header = ["Name", "|V|", "|E|", "Randomized-1", "Greedy-1", "Semi-Greedy-1",
              "LocalSearch-Iterations", "LocalSearch-Average",
              "GRASP-Iterations", "GRASP-Best", "KnownBest"]

    rows = []
    for filename in sorted(os.listdir(input_folder)):
        if filename.endswith(".rud"):
            filepath = os.path.join(input_folder, filename)
            graph_name = filename.split('.')[0].upper()  # Convert to uppercase to match dictionary keys
            
            # Get the known best solution for the current graph, defaulting to -1 if not found
            known_best = known_best_solutions.get(graph_name, -1)

            # Debugging print statement to check the graph name and its known best value
            print(f"Processing {graph_name}: KnownBest = {known_best}")

            # Replace the KnownBest with a blank if it is -1
            known_best_str = "" if known_best == -1 else str(known_best)

            try:
                graph = read_rud_file(filepath)
                row = run_all_algorithms(graph, graph_name)  # Do not pass known_best here
                
                # Append KnownBest value to the row (after the algorithm results)
                row.append(known_best_str)  # Add the KnownBest (or blank) to the row
                rows.append(row)
                print(f"[DONE]  Completed {filename} ✅")
            except Exception as e:
                print(f"[ERROR] Failed to process {filename}: {e}")

    # Write the results to the CSV file
    with open(output_csv, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerow(header)
        writer.writerows(rows)
    print(f"\n✅ All results saved to '{output_csv}'")


    
if __name__ == "__main__":
    input_folder = "./graphs"
    output_csv = "maxcut_summary.csv"
    batch_process_rud_files(input_folder, output_csv)  
