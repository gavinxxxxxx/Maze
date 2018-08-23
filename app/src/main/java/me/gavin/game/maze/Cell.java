package me.gavin.game.maze;

/**
 * Cell
 *
 * @author gavin.xiong 2017/10/31
 */
public class Cell {

    static final int FLAG_LEFT = 1;
    static final int FLAG_TOP = 1 << 1;
    static final int FLAG_RIGHT = 1 << 2;
    static final int FLAG_BOTTOM = 1 << 3;
    static final int FLAG_YET = 1 << 4;

    int x;
    int y;

    private int flag;
    int index; // 线程标识

    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    void add(int flag) {
        this.flag |= flag;
    }

    boolean contain(int flag) {
        return (this.flag & flag) == flag;
    }

    boolean leftOf(Cell o) {
        return o.x - x == 1;
    }

    boolean rightOf(Cell o) {
        return x - o.x == 1;
    }

    boolean topOf(Cell o) {
        return o.y - y == 1;
    }

    boolean bottomOf(Cell o) {
        return y - o.y == 1;
    }

}
