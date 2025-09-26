import sys
import pandas as pd
import matplotlib.pyplot as plt
from matplotlib import font_manager, rcParams

"""Usage:
    python plot_poolsize_latency.py pool_latency.csv pool_latency.png

CSV required columns:
    pool,avg_ms,p95_ms,p99_ms

Example CSV:
    pool,avg_ms,p95_ms,p99_ms
    2,1.586,1.882,2.177
    3,1.591,1.861,2.109
    5,1.669,2.012,2.503
    10,1.963,2.678,3.117
    20,2.515,3.545,4.182
    30,3.155,4.664,5.775

Output:
    단일 선형(line) 차트: avg / p95 / p99 모두 선 + 마커 (막대 없음, 로그 없음)
"""

def _set_korean_font():
    candidates = [
        'Malgun Gothic','맑은 고딕','NanumGothic','Noto Sans CJK KR','AppleGothic'
    ]
    for name in candidates:
        try:
            prop = font_manager.FontProperties(family=name)
            font_manager.findfont(prop, fallback_to_default=False)
            rcParams['font.family'] = name
            return name
        except Exception:
            continue
    return None


def plot(csv_path: str, out_png: str):
    _set_korean_font()
    df = pd.read_csv(csv_path)
    df = df.sort_values('pool')

    fig, ax = plt.subplots(figsize=(11,5.5))

    x = df['pool'].astype(str)
    line_styles = [
        ('avg_ms', '#4E79A7', 'o', 'avg'),
        ('p95_ms', '#F28E2B', 's', 'p95'),
        ('p99_ms', '#E15759', '^', 'p99'),
    ]

    for col, color, marker, label in line_styles:
        ax.plot(x, df[col], color=color, marker=marker, markersize=7, linewidth=2, label=label)
        # annotate each point with offset for readability
        for xi, y in zip(x, df[col]):
            ax.annotate(f'{y:.2f}', (xi, y), textcoords='offset points', xytext=(0, 10),
                        ha='center', va='bottom', fontsize=9, color=color,
                        bbox=dict(boxstyle='round,pad=0.25', fc='white', ec=color, lw=0.5, alpha=0.85))

    ax.set_xlabel('Pool Size', fontsize=11)
    ax.set_ylabel('Latency (ms)', fontsize=11)
    ax.set_title('Redis Connection Pool Size vs Latency (avg / p95 / p99)', fontsize=13)
    ax.grid(axis='y', linestyle='--', alpha=0.4)
    ax.tick_params(axis='both', labelsize=10)
    ax.legend(fontsize=10)
    plt.tight_layout()
    plt.savefig(out_png)
    print(f'Saved {out_png}')


if __name__ == '__main__':
    if len(sys.argv) < 3:
        print('Usage: python plot_poolsize_latency.py <csv> <out.png>')
        sys.exit(1)
    plot(sys.argv[1], sys.argv[2])
