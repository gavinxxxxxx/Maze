package me.gavin.game.maze;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.gavin.game.maze.util.DisplayUtil;
import me.gavin.game.maze.util.L;

/**
 * MazeView
 *
 * @author gavin.xiong 2017/10/31
 */
public class MazeView extends View {

    private final int count = 30;

    private float cw;
    private float ww;

    private Paint paint, paint2;

    public MazeView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        setKeepScreenOn(true);
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint2 = new Paint();
        paint2.setAntiAlias(true);
        paint2.setColor(Color.TRANSPARENT);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defSize = Math.min(DisplayUtil.getScreenWidth(), DisplayUtil.getScreenHeight());
        setMeasuredDimension(defSize, defSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        cw = w * 1f / count;
        ww = cw * 0.1f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode()) {
            return;
        }
        Cell[][] cells = prim();
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                Cell cell = cells[i][j];
                canvas.drawRect(i * cw, j * cw - ww, i * cw + ww, j * cw + cw + ww, cell.containFlag(Cell.FLAG_LEFT) ? paint2 : paint);
                canvas.drawRect(i * cw - ww, j * cw, i * cw + cw + ww, j * cw + ww, cell.containFlag(Cell.FLAG_TOP) ? paint2 : paint);
                canvas.drawRect(i * cw + cw - ww, j * cw - ww, i * cw + cw, j * cw + cw + ww, cell.containFlag(Cell.FLAG_RIGHT) ? paint2 : paint);
                canvas.drawRect(i * cw - ww, j * cw + cw - ww, i * cw + cw + ww, j * cw + cw, cell.containFlag(Cell.FLAG_BOTTOM) ? paint2 : paint);
            }
        }
    }

    private Cell[][] prim() {
        Cell[][] cells = new Cell[count][count];
        for (int i = 0; i < count; i++) {
            for (int j = 0; j < count; j++) {
                cells[i][j] = new Cell();
                cells[i][j].x = i;
                cells[i][j].y = j;
                if (i == 0 && j == 0) {
                    cells[i][j].addFlag(Cell.FLAG_LEFT);
                } else if (i == count - 1 && j == count - 1) {
                    cells[i][j].addFlag(Cell.FLAG_RIGHT);
                }
            }
        }
        List<Cell> yet = new ArrayList<>();
        yet.add(cells[0][0]);
        List<Cell> able = new ArrayList<>();
        able.add(cells[0][0]);

        Random random = new Random(System.nanoTime());
        List<Cell> neighbor = new ArrayList<>();
        int sum = 0;
        while (yet.size() < count * count) {
            sum++;
            Cell curr = able.get(random.nextInt(able.size()));
            neighbor.clear();
            if (curr.x > 0 && !yet.contains(cells[curr.x - 1][curr.y])) {
                neighbor.add(cells[curr.x - 1][curr.y]);
            }
            if (curr.x < count - 1 && !yet.contains(cells[curr.x + 1][curr.y])) {
                neighbor.add(cells[curr.x + 1][curr.y]);
            }
            if (curr.y > 0 && !yet.contains(cells[curr.x][curr.y - 1])) {
                neighbor.add(cells[curr.x][curr.y - 1]);
            }
            if (curr.y < count - 1 && !yet.contains(cells[curr.x][curr.y + 1])) {
                neighbor.add(cells[curr.x][curr.y + 1]);
            }
            if (neighbor.isEmpty()) {
                able.remove(curr);
                continue;
            }
            Cell next = neighbor.get(random.nextInt(neighbor.size()));
            if (next.x - curr.x == 1) {
                curr.addFlag(Cell.FLAG_RIGHT);
                next.addFlag(Cell.FLAG_LEFT);
            } else if (next.x - curr.x == -1) {
                curr.addFlag(Cell.FLAG_LEFT);
                next.addFlag(Cell.FLAG_RIGHT);
            } else if (next.y - curr.y == 1) {
                curr.addFlag(Cell.FLAG_BOTTOM);
                next.addFlag(Cell.FLAG_TOP);
            } else if (next.y - curr.y == -1) {
                curr.addFlag(Cell.FLAG_TOP);
                next.addFlag(Cell.FLAG_BOTTOM);
            }
            yet.add(next);
            able.add(next);
        }
        L.e(sum);
        return cells;
    }
}
