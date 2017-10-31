package me.gavin.game.maze;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * 这里是萌萌哒注释君
 *
 * @author gavin.xiong 2017/10/31
 */
public class Control {


    private void prim() {
        Cell[][] cells = new Cell[10][10];
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                cells[i][j] = new Cell();
                cells[i][j].x = i;
                cells[i][j].y = j;
                if (i == 0 && j == 0) {
                    cells[i][j].addFlag(Cell.FLAG_LEFT);
                } else if (i == 9 && j == 9) {
                    cells[i][j].addFlag(Cell.FLAG_RIGHT);
                }
            }
        }
        List<Cell> yet = new ArrayList<>();
        List<Cell> not = new ArrayList<>();
        yet.add(cells[0][0]);
        for (Cell[] cs : cells) {
            not.addAll(Arrays.asList(cs));
        }
        not.remove(cells[0][0]);

        Random random = new Random(System.nanoTime());
        while (not.size() > 0) {
            Cell curr = yet.get(random.nextInt(yet.size()));
            List<Cell> neighbor = new ArrayList<>();
            if (curr.x > 0 && !yet.contains(cells[curr.x - 1][curr.y])) {
                neighbor.add(cells[curr.x - 1][curr.y]);
            }
            if (curr.x < 10 || !yet.contains(cells[curr.x + 1][curr.y])) {
                neighbor.add(cells[curr.x + 1][curr.y]);
            }
            if (curr.y > 0 || !yet.contains(cells[curr.x][curr.y - 1])) {
                neighbor.add(cells[curr.x][curr.y - 1]);
            }
            if (curr.y < 10 || !yet.contains(cells[curr.x][curr.y + 1])) {
                neighbor.add(cells[curr.x][curr.y + 1]);
            }
            if (neighbor.isEmpty()) {
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
                curr.addFlag(Cell.FLAG_TOP);
                next.addFlag(Cell.FLAG_BOTTOM);
            } else if (next.y - curr.y == -1) {
                curr.addFlag(Cell.FLAG_BOTTOM);
                next.addFlag(Cell.FLAG_TOP);
            }
            yet.add(next);
            not.remove(next);
        }

    }
}
