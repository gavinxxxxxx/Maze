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

    private final int xc, yc, count;

    private final Cell[][] cells;
    private final List<Cell> yet;

    private static final Random random = new Random(System.nanoTime());

    PrimUtil4ThreadPool(int xc, int yc) {
        this.xc = xc;
        this.yc = yc;
        this.count = xc * yc;

        this.cells = new Cell[xc][yc];
        this.yet = new ArrayList<>(count);
    }

    Cell[][] prim() {
        long start = System.currentTimeMillis();

        for (int x = 0; x < xc; x++)
            for (int y = 0; y < yc; y++)
                cells[x][y] = new Cell(x, y);
        cells[0][0].add(Cell.FLAG_TOP);
        cells[xc - 1][yc - 1].add(Cell.FLAG_BOTTOM);

        ExecutorService executor = Executors.newCachedThreadPool();
        int poolCount = Math.min(30, count / 1024 + 1);
        for (int i = 0; i < poolCount; i++) {
            Cell curr = random(0);
            if (curr != null) {
                curr.index = 1 << i;
                executor.execute(new IRunnable(curr));
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
                    if (c.x > 0 && !c.contain(Cell.FLAG_LEFT)) {
                        cells[c.x - 1][c.y].add(Cell.FLAG_RIGHT);
                        c.add(Cell.FLAG_LEFT);
                    } else {
                        cells[c.x][c.y - 1].add(Cell.FLAG_BOTTOM);
                        c.add(Cell.FLAG_TOP);
                    }
                    flag |= c.index;
                }
            }
        }
        L.e(xc + "x" + yc + " - " + (System.currentTimeMillis() - start));
        return cells;
    }

    private synchronized boolean add(Cell next) {
        if (!next.contain(Cell.FLAG_YET)) {
            next.add(Cell.FLAG_YET);
            yet.add(next);
            return true;
        }
        return false;
    }

    private Cell random(int times) {
        if (times > 3) return null;
        Cell curr = cells[random.nextInt(xc)][random.nextInt(yc)];
        return !curr.contain(Cell.FLAG_YET) ? curr : random(times + 1);
    }

    private class IRunnable implements Runnable {

        private final int index;
        private Cell curr;
        private final List<Cell> able;

        IRunnable(Cell curr) {
            this.index = curr.index;
            this.curr = curr;
            this.able = new ArrayList<>(count / 1024);

            add(curr);
            this.able.add(curr);
        }

        @Override
        public void run() {
            List<Cell> neighbor = new ArrayList<>(4);
            while (curr != null && yet.size() < count) {
                neighbor.clear();
                if (curr.x > 0 && !cells[curr.x - 1][curr.y].contain(Cell.FLAG_YET))
                    neighbor.add(cells[curr.x - 1][curr.y]);
                if (curr.x < xc - 1 && !cells[curr.x + 1][curr.y].contain(Cell.FLAG_YET))
                    neighbor.add(cells[curr.x + 1][curr.y]);
                if (curr.y > 0 && !cells[curr.x][curr.y - 1].contain(Cell.FLAG_YET))
                    neighbor.add(cells[curr.x][curr.y - 1]);
                if (curr.y < yc - 1 && !cells[curr.x][curr.y + 1].contain(Cell.FLAG_YET))
                    neighbor.add(cells[curr.x][curr.y + 1]);
                if (!neighbor.isEmpty()) {
                    Cell next = neighbor.get(random.nextInt(neighbor.size()));
                    if (add(next)) {
                        able.add(next);
                        next.index = this.index;
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
                        // 80% 几率沿用
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
