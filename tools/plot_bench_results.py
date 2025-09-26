import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
import sys


def human(x):
    if x >= 1000:
        return f"{x/1000:.1f}k"
    return f"{x:.0f}"


def plot(csv_path, out_png):
    df = pd.read_csv(csv_path)
    # ensure numeric
    df['pool_max'] = df['pool_max'].astype(int)
    df['batchSize'] = df['batchSize'].astype(int)
    df['rows_per_sec'] = df['rows_per_sec'].astype(float)

    pivot = df.pivot_table(index='pool_max', columns='batchSize', values='rows_per_sec')
    pivot = pivot.sort_index()

    fig, ax = plt.subplots(figsize=(8,5))
    markers = ['o','s','D','^','v','P']
    for i, col in enumerate(pivot.columns):
        y = pivot[col]
        ax.plot(pivot.index, y, marker=markers[i % len(markers)], label=f'batch={col}', linewidth=2)
        for xi, yi in zip(pivot.index, y):
            ax.annotate(human(yi), (xi, yi), textcoords='offset points', xytext=(0,6), ha='center', fontsize=9,
                        bbox=dict(boxstyle='round,pad=0.2', fc='white', ec='black', alpha=0.8))

    ax.set_xlabel('pool_max')
    ax.set_ylabel('rows per second')
    ax.set_title('Insert throughput by pool_max and batchSize')
    ax.grid(axis='y', linestyle='--', alpha=0.5)
    ax.legend(title='batchSize')
    plt.tight_layout()
    plt.savefig(out_png, dpi=150)
    print(f"Saved {out_png}")


if __name__ == '__main__':
    csv_path = sys.argv[1] if len(sys.argv) > 1 else 'tools/bench-results.csv'
    out_png = sys.argv[2] if len(sys.argv) > 2 else 'tools/bench_results.png'
    plot(csv_path, out_png)
