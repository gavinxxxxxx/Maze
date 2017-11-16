package me.gavin.game.maze;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import me.gavin.game.maze.util.DisplayUtil;
import me.gavin.game.rocker.IntConsumer;

/**
 * MazeView
 *
 * @author gavin.xiong 2017/10/31
 */
public class MazeView extends View {

    private float mBorderWidth;

    private int count = 30;
    private Cell[][] mCells;

    private float mCellWidth;
    private final float mWellScale = 0.2f;

    private Paint mBorderPaint, mWellPaint, mCursorPaint;
    private Path mBorderPath, mWellPath;

    private Point mCursorPoint;

    private boolean isReady;

    private IntConsumer mCallback;

    public MazeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) return;
        setKeepScreenOn(true);

        mBorderPaint = new Paint();
        mBorderPaint.setColor(0xFFAA66CC);
        mBorderPath = new Path();

        mWellPaint = new Paint();
        mWellPaint.setAntiAlias(true);
        mWellPaint.setColor(0xFFAA66CC);
        mWellPaint.setStyle(Paint.Style.STROKE);
        mWellPaint.setStrokeJoin(Paint.Join.ROUND);
        mWellPaint.setStrokeCap(Paint.Cap.ROUND);

        mCursorPaint = new Paint();
        mCursorPaint.setAntiAlias(true);
        mCursorPaint.setColor(0xFFFF4444);
    }

    public void setCells(Cell[][] cells, IntConsumer callback) {
        this.mCells = cells;
        this.mCallback = callback;
        if (cells != null) {
            this.count = cells.length;

            mBorderWidth = Math.max(getWidth() / (count + 1) * mWellScale, 20f);
            mCellWidth = (getWidth() - mBorderWidth * 2) / count;
            float wellWidth = mCellWidth * mWellScale;

            mBorderPath.reset();
            mBorderPath.moveTo(0, 0);
            mBorderPath.rLineTo(mBorderWidth, 0);
            mBorderPath.rLineTo(0, getWidth() - mBorderWidth);
            mBorderPath.rLineTo(getWidth() - mBorderWidth * 2 - mCellWidth + wellWidth / 2, 0);
            mBorderPath.rLineTo(0, mBorderWidth);
            mBorderPath.rLineTo(mBorderWidth + mCellWidth - getWidth() - wellWidth / 2, 0);
            mBorderPath.rLineTo(0, -getWidth());
            mBorderPath.rMoveTo(mBorderWidth + mCellWidth - wellWidth / 2, 0);
            mBorderPath.rLineTo(0, mBorderWidth);
            mBorderPath.rLineTo(getWidth() - mBorderWidth * 2 - mCellWidth + wellWidth / 2, 0);
            mBorderPath.rLineTo(0, getWidth() - mBorderWidth);
            mBorderPath.rLineTo(mBorderWidth, 0);
            mBorderPath.rLineTo(0, -getWidth());
            mBorderPath.close();

            mWellPaint.setStrokeWidth(wellWidth);
            mWellPath = PrimUtil.toPath(cells, mCellWidth, mBorderWidth);

            mCursorPoint = new Point(0, 0);
        }
        invalidate();
        isReady = cells != null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int defSize = Math.min(DisplayUtil.getScreenWidth(), DisplayUtil.getScreenHeight());
        setMeasuredDimension(defSize, defSize);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode() || mCells == null) return;
        // 画边界
        canvas.drawPath(mBorderPath, mBorderPaint);
        // 画迷宫墙
        canvas.drawPath(mWellPath, mWellPaint);
        // 画游标
        canvas.drawCircle(mBorderWidth + mCursorPoint.x * mCellWidth + mCellWidth / 2, mBorderWidth + mCursorPoint.y * mCellWidth + mCellWidth / 2, mCellWidth / 3, mCursorPaint);
    }

    public void left() {
        if (isReady && mCursorPoint.x > 0 && mCells[mCursorPoint.x][mCursorPoint.y].containFlag(Cell.FLAG_LEFT)) {
            mCursorPoint.offset(-1, 0);
            invalidate();
            check();
        }
    }

    public void up() {
        if (isReady && mCursorPoint.y > 0 && mCells[mCursorPoint.x][mCursorPoint.y].containFlag(Cell.FLAG_TOP)) {
            mCursorPoint.offset(0, -1);
            invalidate();
            check();
        }
    }

    public void right() {
        if (isReady && mCursorPoint.x < count - 1 && mCells[mCursorPoint.x][mCursorPoint.y].containFlag(Cell.FLAG_RIGHT)) {
            mCursorPoint.offset(1, 0);
            invalidate();
            check();
        }
    }

    public void down() {
        if (isReady && mCursorPoint.y < count - 1 && mCells[mCursorPoint.x][mCursorPoint.y].containFlag(Cell.FLAG_BOTTOM)) {
            mCursorPoint.offset(0, 1);
            invalidate();
            check();
        }
    }

    public void check() {
        if (mCursorPoint.x == count - 1 && mCursorPoint.y == count - 1) {
            isReady = false;
            if (mCallback != null) {
                mCallback.accept(count);
            }
        }
    }
}
