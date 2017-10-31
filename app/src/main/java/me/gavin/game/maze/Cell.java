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

    int x;
    int y;

    private int flag;

    void addFlag(int flag) {
        this.flag |= flag;
    }

    boolean containFlag(int flag) {
        return (this.flag & flag) == flag;
    }

    @Override
    public String toString() {
        return x + ":" + y;
    }
}
