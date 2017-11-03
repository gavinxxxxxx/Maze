package me.gavin.game.maze;

import android.graphics.Path;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


class Utils {

    /**
     * 随机米空生成 -  普里姆算法
     */
    static Cell[][] prim(int xc, int yc) {
        Cell[][] cells = new Cell[xc][yc];
        for (int x = 0; x < xc; x++) {
            for (int y = 0; y < yc; y++) {
                cells[x][y] = new Cell(x, y);
                if (x == 0 && y == 0)
                    cells[x][y].addFlag(Cell.FLAG_TOP);
                else if (x == xc - 1 && y == yc - 1)
                    cells[x][y].addFlag(Cell.FLAG_BOTTOM);
            }
        }
        List<Cell> yet = new ArrayList<>();
        yet.add(cells[0][0]);
        List<Cell> able = new ArrayList<>();
        able.add(cells[0][0]);

        Random random = new Random(System.nanoTime());
        List<Cell> neighbor = new ArrayList<>();
        while (yet.size() < xc * yc) {
            Cell curr = able.get(random.nextInt(able.size()));
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
                    curr.addFlag(Cell.FLAG_LEFT);
                    next.addFlag(Cell.FLAG_RIGHT);
                } else if (next.rightOf(curr)) {
                    curr.addFlag(Cell.FLAG_RIGHT);
                    next.addFlag(Cell.FLAG_LEFT);
                } else if (next.topOf(curr)) {
                    curr.addFlag(Cell.FLAG_TOP);
                    next.addFlag(Cell.FLAG_BOTTOM);
                } else if (next.bottomOf(curr)) {
                    curr.addFlag(Cell.FLAG_BOTTOM);
                    next.addFlag(Cell.FLAG_TOP);
                }
                yet.add(next);
                able.add(next);
            } else {
                able.remove(curr);
            }
        }
        return cells;
    }

    static Path toPath(Cell[][] mCells, float width, float offset) {
        Path path = new Path();
        for (int x = 0; x < mCells.length; x++) {
            for (int y = 0; y < mCells[x].length; y++) {
                Cell cell = mCells[x][y];
                if (x > 0 && !cell.containFlag(Cell.FLAG_LEFT)) {
                    path.moveTo(x * width, y * width);
                    path.lineTo(x * width, y * width + width);
                }
                if (y > 0 && !cell.containFlag(Cell.FLAG_TOP)) {
                    path.moveTo(x * width, y * width);
                    path.lineTo(x * width + width, y * width);
                }
            }
        }
        path.offset(offset, offset);
        return path;
    }
}
