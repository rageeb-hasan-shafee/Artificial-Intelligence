import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import os

csv_file = "F:/3-1/CSE 318/2. Max-cut Problem (GRASP)/2105175/2105175.csv"

if not os.path.exists(csv_file):
    print(f"Error: The file {csv_file} does not exist.")
else:
    print(f"File found at: {os.path.abspath(csv_file)}")

    try:
        df = pd.read_csv(csv_file, skiprows=2, header=None, encoding='utf-8')  
        df.columns = ['Problem', '|V|', '|E|', 'Randomized-1', 'Greedy-1', 'Semi-Greedy-1', 
                      'LocalSearch-Iterations', 'LocalSearch-Average', 'GRASP-Iterations', 
                      'GRASP-Best', 'KnownBest']

        df = df[['Problem', 'Randomized-1', 'Greedy-1', 'Semi-Greedy-1', 'LocalSearch-Average', 'GRASP-Best']]
        
        df[['Randomized-1', 'Greedy-1', 'Semi-Greedy-1', 'LocalSearch-Average', 'GRASP-Best']] = df[[
            'Randomized-1', 'Greedy-1', 'Semi-Greedy-1', 'LocalSearch-Average', 'GRASP-Best'
        ]].apply(pd.to_numeric, errors='coerce')
        
        df = df.dropna().head(10)

        index = np.arange(len(df))
        bar_width = 0.15

        plt.figure(figsize=(14, 6), facecolor='white')
        ax = plt.gca()
        ax.set_facecolor('white')  
        
        plt.bar(index, df['Randomized-1'], bar_width, label='Randomized', color='#1f77b4')
        plt.bar(index + bar_width, df['Greedy-1'], bar_width, label='Greedy', color='#ff7f0e')
        plt.bar(index + 2*bar_width, df['Semi-Greedy-1'], bar_width, label='Semi-Greedy', color='#7f7f7f')
        plt.bar(index + 3*bar_width, df['GRASP-Best'], bar_width, label='GRASP', color='#f9a825')  
        plt.bar(index + 4*bar_width, df['LocalSearch-Average'], bar_width, label='Local Search', color='#003366') 

        plt.xlabel('Graphs', color='black', fontsize=16, fontweight='bold', labelpad=10)
        plt.ylabel('Max Cut', color='black', fontsize=16, fontweight='bold', labelpad=10)
        plt.title('Max Cut (G10-G19)', color='black', fontsize=18, fontweight='bold', pad=15)
        plt.xticks(index + 2*bar_width, df['Problem'], color='black', fontsize=12)
        plt.yticks(color='black')
        plt.legend(facecolor='white', edgecolor='black', labelcolor='black', fontsize=12)
        plt.grid(axis='y', linestyle='--', alpha=0.3, color='gray')
        plt.tight_layout()

        plt.savefig("2105175_plot.pdf", facecolor='white')

        print("Plot generated successfully.")

    except Exception as e:
        print(f"Error reading or processing the CSV file: {e}")