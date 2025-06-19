import matplotlib.pyplot as plt
import pandas as pd
from matplotlib.backends.backend_pdf import PdfPages

df = pd.read_csv('2105175.csv')

df['KnownBest'] = df['KnownBest'].fillna(0) 

algorithms = ['Randomized-1', 'Greedy-1', 'Semi-Greedy-1', 'LocalSearch-Average', 'GRASP-Best']
graph_names = df['Name'].tolist()

with PdfPages('2105175_comparison.pdf') as pdf:
    num_plots = len(graph_names)
    plots_per_page = 9
    num_pages = (num_plots // plots_per_page) + (1 if num_plots % plots_per_page != 0 else 0)

    for page_num in range(num_pages):
        fig, axs = plt.subplots(3, 3, figsize=(15, 12))  

        for idx, ax in enumerate(axs.flat):
            plot_idx = page_num * plots_per_page + idx
            if plot_idx < len(graph_names):
                graph_name = graph_names[plot_idx]
                row = df[df['Name'] == graph_name]
                
                values = row[algorithms].values.flatten()

                ax.bar(algorithms, values, color=['blue', 'orange', 'yellow', 'green', 'purple'])
                
                ax.axhline(y=row['KnownBest'].values[0], color='red', linestyle='--', label='KnownBest')

                ax.set_title(graph_name)
                ax.set_ylim([0, max(values.max(), row['KnownBest'].values[0]) + 1000])  
                ax.set_xticks(range(len(algorithms)))  
                ax.set_xticklabels(algorithms, rotation=45) 
                
                ax.legend()
        
        plt.tight_layout()

        pdf.savefig(fig)

        plt.close(fig)

print("PDF has been saved as '2105175_comparison.pdf'")

