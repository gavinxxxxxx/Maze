package me.gavin.game.maze;

import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import me.gavin.game.maze.util.L;

/**
 * 随机迷宫生成 - 普里姆算法
 *
 * @author gavin.xiong 2017/11/16
 */
final class PrimUtil {

    static Cell[][] prim(int xc, int yc) {
        long start = System.currentTimeMillis();

        Cell[][] cells = new Cell[xc][yc];
        for (int x = 0; x < xc; x++) {
            for (int y = 0; y < yc; y++) {
                cells[x][y] = new Cell(x, y);
                if (x == 0 && y == 0)
                    cells[x][y].add(Cell.FLAG_TOP);
                else if (x == xc - 1 && y == yc - 1)
                    cells[x][y].add(Cell.FLAG_BOTTOM);
            }
        }
        Random random = new Random(System.nanoTime());

        List<Cell> yet = new ArrayList<>();
        List<Cell> able = new ArrayList<>();

        Cell curr = cells[random.nextInt(xc)][random.nextInt(yc)];
        yet.add(curr);
        able.add(curr);

        List<Cell> neighbor = new ArrayList<>();
        while (yet.size() < xc * yc) {
            neighbor.clear();
            if (curr.x > 0 && !yet.contains(cells[curr.x - 1][curr.y]))
                neighbor.add(cells[curr.x - 1][curr.y]);
            if (curr.x < xc - 1 && !yet.contains(cells[curr.x + 1][curr.y]))
                neighbor.add(cells[curr.x + 1][curr.y]);
            if (curr.y > 0 && !yet.contains(cells[curr.x][curr.y - 1]))
                neighbor.add(cells[curr.x][curr.y - 1]);
            if (curr.y < yc - 1 && !yet.contains(cells[curr.x][curr.y + 1]))
                neighbor.add(cells[curr.x][curr.y + 1]);
            if (!neighbor.isEmpty()) {
                Cell next = neighbor.get(random.nextInt(neighbor.size()));
                if (next.leftOf(curr)) {
                    curr.add(Cell.FLAG_LEFT);
                    next.add(Cell.FLAG_RIGHT);
                } else if (next.rightOf(curr)) {
                    curr.add(Cell.FLAG_RIGHT);
                    next.add(Cell.FLAG_LEFT);
                } else if (next.topOf(curr)) {
                    curr.add(Cell.FLAG_TOP);
                    next.add(Cell.FLAG_BOTTOM);
                } else if (next.bottomOf(curr)) {
                    curr.add(Cell.FLAG_BOTTOM);
                    next.add(Cell.FLAG_TOP);
                }
                yet.add(next);
                able.add(next);
                // 80% 几率沿用
                curr = random.nextInt(100) < 80 ? next : able.get(random.nextInt(able.size()));
            } else {
                able.remove(curr);
                curr = able.get(random.nextInt(able.size()));
            }
        }
        L.e(xc + "x" + yc + " - " + (System.currentTimeMillis() - start));
        return cells;
    }

    static Path toPath(Cell[][] mCells, float width, float offset) {
        Path path = new Path();
        for (int x = 0; x < mCells.length; x++) {
            for (int y = 0; y < mCells[x].length; y++) {
                Cell cell = mCells[x][y];
                if (x > 0 && !cell.contain(Cell.FLAG_LEFT)) {
                    path.moveTo(x * width, y * width);
                    path.lineTo(x * width, y * width + width);
                }
                if (y > 0 && !cell.contain(Cell.FLAG_TOP)) {
                    path.moveTo(x * width, y * width);
                    path.lineTo(x * width + width, y * width);
                }
            }
        }
        path.offset(offset, offset);
        return path;
    }
}
