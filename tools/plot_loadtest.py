import sys
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from matplotlib import font_manager, rcParams


def plot(csv_path, out_png):
    df = pd.read_csv(csv_path).set_index('test')

    # 한글 표시를 위한 폰트 설정 (있으면 자동 선택)
    def _set_korean_font():
        candidates = [
            'Malgun Gothic',        # Windows
            '맑은 고딕',
            'NanumGothic',          # Linux (Ubuntu Nanum)
            'Noto Sans CJK KR',
            'AppleGothic'           # macOS
        ]
        for name in candidates:
            try:
                prop = font_manager.FontProperties(family=name)
                # fallback_to_default=False will raise if not found
                font_manager.findfont(prop, fallback_to_default=False)
                rcParams['font.family'] = name
                print(f"Using font: {name}")
                return name
            except Exception:
                continue
        print('No Korean font found in system matplotlib fonts. If labels look broken, install NanumGothic or Noto Sans CJK.')
        return None

    _set_korean_font()

    # 기본 비교 (로그 스케일로 전체 비교)
    metrics = ['avg_ms', 'p95_ms', 'p99_ms']

    fig, axes = plt.subplots(1, 2, figsize=(12, 5), gridspec_kw={'width_ratios': [2, 1]})

    # 왼쪽: 로그 스케일로 avg/p95/p99 비교
    ax = axes[0]
    df[metrics].plot(kind='bar', ax=ax)
    ax.set_yscale('log')
    ax.set_ylabel('ms (log scale)')
    ax.set_title('Redis Load Test (log scale)')
    ax.set_xticklabels(df.index, rotation=0)

    # annotate each bar with its numeric value using a small rounded "bubble" background
    for p in ax.patches:
        try:
            height = p.get_height()
        except Exception:
            continue
        if height is None or np.isnan(height):
            continue
        x = p.get_x() + p.get_width() / 2
        # place label slightly above the bar; use multiplicative offset to work with log scale
        y = height * 1.08 if height > 0 else 0.1
        # format value depending on magnitude for readability
        if height >= 1000:
            text = f'{height:,.0f}ms'
        elif height >= 100:
            text = f'{height:,.1f}ms'
        elif height >= 10:
            text = f'{height:.1f}ms'
        elif height >= 1:
            text = f'{height:.3f}ms'
        else:
            text = f'{height:.3e}ms'
        ax.annotate(text,
                    (x, y),
                    ha='center', va='bottom', fontsize=8,
                    bbox=dict(boxstyle='round,pad=0.3', fc='white', ec='black', alpha=0.85),
                    clip_on=False)

    # 오른쪽: fold-change (NoPool / Reuse) for avg/p95/p99
    ax2 = axes[1]
    # compute fold relative to the faster one (Reuse)
    if 'Reuse' in df.index and 'NoPool' in df.index:
        fold = df.loc['NoPool', metrics] / df.loc['Reuse', metrics]
        fold = fold.replace([np.inf, -np.inf], np.nan)
        bars = fold.plot(kind='bar', ax=ax2, color=['#1f77b4', '#ff7f0e', '#2ca02c'])
        # add larger top padding so the annotation bubbles don't touch the top border
        ymax = fold.max()
        # increase headroom fraction to give clear space above tallest bar
        headroom = max(0.30 * ymax, 1.0)
        ax2.set_ylim(0, ymax + headroom)
        ax2.set_ylabel('fold (NoPool / Reuse)')
        ax2.set_title('Fold difference (NoPool vs Reuse)')
        ax2.set_xticklabels(metrics, rotation=0)
        # annotate values
        for p in ax2.patches:
            h = p.get_height()
            # place annotation slightly above the bar so the bubble doesn't overlap the bar top
            y_annot = h + headroom * 0.08
            ax2.annotate(f'{h:.1f}배 차이',
                         (p.get_x() + p.get_width() / 2, y_annot),
                         ha='center', va='bottom', fontsize=9,
                         bbox=dict(boxstyle='round,pad=0.3', fc='white', ec='black', alpha=0.85),
                         clip_on=False)
    else:
        ax2.text(0.5, 0.5, 'Need both NoPool and Reuse rows', ha='center')

    plt.tight_layout()
    plt.savefig(out_png)
    print(f"Saved {out_png}")


if __name__ == '__main__':
    if len(sys.argv) < 3:
        print('Usage: python plot_loadtest.py <csv> <out.png>')
        sys.exit(1)
    plot(sys.argv[1], sys.argv[2])
