package me.gavin.game.maze;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import me.gavin.game.maze.util.L;

/**
 * 随机迷宫生成 - 普里姆算法 - 多线程版
 *
 * @author gavin.xiong 2017/11/16
 */
final class PrimUtil4ThreadPool {

    private volatile Cell[][] cells;
    private volatile List<Cell> yet;

    private volatile Random random = new Random(System.nanoTime());

    Cell[][] prim(int xc, int yc) {
        cells = new Cell[xc][yc];
        for (int x = 0; x < xc; x++) {
            for (int y = 0; y < yc; y++) {
                cells[x][y] = new Cell(x, y);
                if (x == 0 && y == 0)
                    cells[x][y].addFlag(Cell.FLAG_TOP);
                if (x == xc - 1 && y == yc - 1)
                    cells[x][y].addFlag(Cell.FLAG_BOTTOM);
            }
        }

        yet = new ArrayList<>();

        ExecutorService executor = Executors.newCachedThreadPool();
        int poolCount = Math.min(30, xc * yc / 1024 + 1);
        for (int i = 0; i < poolCount; i++) {
            Cell curr = random(xc, yc, 0);
            if (curr != null) {
                curr.index = 1 << i;
                executor.execute(new MyRunnable(curr, xc, yc, 1 << i));
            }
        }
        executor.shutdown();

        try {
            while (!executor.awaitTermination(1, TimeUnit.SECONDS)) ;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int flag = cells[0][0].index;
        for (Cell[] cs : cells) {
            for (Cell c : cs) {
                if ((flag & c.index) == 0) {
                    if (c.x > 0 && !c.containFlag(Cell.FLAG_LEFT)) {
                        cells[c.x - 1][c.y].addFlag(Cell.FLAG_RIGHT);
                        c.addFlag(Cell.FLAG_LEFT);
                    } else {
                        cells[c.x][c.y - 1].addFlag(Cell.FLAG_BOTTOM);
                        c.addFlag(Cell.FLAG_TOP);
                    }
                    flag |= c.index;
                }
            }
        }
        return cells;
    }

    private synchronized boolean add(Cell next) {
        if (!yet.contains(next)) {
            yet.add(next);
            return true;
        }
        return false;
    }

    private Cell random(int xc, int yc, int count) {
        if (count > 3) return null;
        Cell curr = cells[random.nextInt(xc)][random.nextInt(yc)];
        return !yet.contains(curr) ? curr : random(xc, yc, count + 1);
    }

    private class MyRunnable implements Runnable {

        Cell curr;
        int xc, yc;
        int index;
        List<Cell> able;

        MyRunnable(Cell curr, int xc, int yc, int index) {
            this.curr = curr;
            this.xc = xc;
            this.yc = yc;
            this.index = index;
            able = new ArrayList<>();
            add(curr);
            able.add(curr);
        }

        @Override
        public void run() {
            List<Cell> neighbor = new ArrayList<>();
            while (curr != null && yet.size() < xc * yc) {
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
                    if (add(next)) {
                        able.add(next);
                        next.index = this.index;
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
                        }// 80% 几率沿用
                        curr = random.nextInt(100) < 80 ? next : nextAble();
                    } else {
                        curr = nextAble();
                    }
                } else {
                    able.remove(curr);
                    curr = nextAble();
                }
            }
        }

        private Cell nextAble() {
            return able.isEmpty() ? null : able.get(random.nextInt(able.size()));
        }

    }
}
