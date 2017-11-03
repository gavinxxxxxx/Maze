package me.gavin.game.rocker;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

public class RockerView extends View {

    private float mRockerRadius = 36, mAreaRadius = 160, mValidRadius = 50;

    private float mDistance;

    private Paint mPaint;

    private PointF sPoint, ePoint, cPoint;

    private Path mPath;

    private IntConsumer mOnDirectionListener;
    public static final int MESSAGE_WHAT_DIRECTION = 0x2333;
    public static final int EVENT_DIRECTION_CENTER = 0;
    public static final int EVENT_DIRECTION_LEFT = 1;
    public static final int EVENT_DIRECTION_RIGHT = 2;
    public static final int EVENT_DIRECTION_UP = 3;
    public static final int EVENT_DIRECTION_DOWN = 4;
    private int mLastDirectionEvent = -1;

    private IntConsumer mOnActionListener;
    public static final int EVENT_ACTION_DOWN = 0;
    public static final int EVENT_ACTION_UP = 1;

    public RockerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(0x20000000);
        sPoint = new PointF(-1, -1);
        ePoint = new PointF(-1, -1);
        cPoint = new PointF(-1, -1);
        mPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isInEditMode() || sPoint.x < 0) return;

        if (ePoint.x < 0 || ePoint.equals(sPoint)) {
            canvas.drawCircle(sPoint.x, sPoint.y, mRockerRadius, mPaint);
        } else if (cPoint.x < 0) {
            mPath.reset();
            mPath.addCircle(sPoint.x, sPoint.y, mRockerRadius, Path.Direction.CCW);
            mPath.addCircle(ePoint.x, ePoint.y, mRockerRadius, Path.Direction.CCW);
            canvas.drawPath(mPath, mPaint);
        } else {
            mPath.reset();
            double a = Math.acos(mRockerRadius / mDistance);
            double b = Math.acos((cPoint.x - sPoint.x) / mDistance);
            double c = Math.acos((cPoint.y - sPoint.y) / mDistance);

            float tx1 = sPoint.x + mRockerRadius * (float) Math.cos(ePoint.y > sPoint.y ? a - b : a + b);
            float ty1 = sPoint.y - mRockerRadius * (float) Math.sin(ePoint.y > sPoint.y ? a - b : a + b);

            float tx2 = sPoint.x - mRockerRadius * (float) Math.sin(ePoint.x > sPoint.x ? a - c : a + c);
            float ty2 = sPoint.y + mRockerRadius * (float) Math.cos(ePoint.x > sPoint.x ? a - c : a + c);

            float tx3 = ePoint.x + mRockerRadius * (float) Math.sin(ePoint.x > sPoint.x ? a - c : a + c);
            float ty3 = ePoint.y - mRockerRadius * (float) Math.cos(ePoint.x > sPoint.x ? a - c : a + c);

            float tx4 = ePoint.x - mRockerRadius * (float) Math.cos(ePoint.y > sPoint.y ? a - b : a + b);
            float ty4 = ePoint.y + mRockerRadius * (float) Math.sin(ePoint.y > sPoint.y ? a - b : a + b);

            float sa = -(float) Math.toDegrees(ePoint.y > sPoint.y ? a - b : a + b);
            float ea = -(float) Math.toDegrees(Math.PI * 2 - a * 2);
            mPath.addArc(sPoint.x - mRockerRadius, sPoint.y - mRockerRadius, sPoint.x + mRockerRadius, sPoint.y + mRockerRadius, sa, ea);
            mPath.addArc(ePoint.x - mRockerRadius, ePoint.y - mRockerRadius, ePoint.x + mRockerRadius, ePoint.y + mRockerRadius, 180 + sa, ea);
            mPath.moveTo(tx1, ty1);
            mPath.quadTo(cPoint.x, cPoint.y, tx3, ty3);
            mPath.lineTo(tx4, ty4);
            mPath.quadTo(cPoint.x, cPoint.y, tx2, ty2);
            canvas.drawPath(mPath, mPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (mOnActionListener != null) {
                    mOnActionListener.accept(EVENT_ACTION_DOWN);
                }
                sPoint.set(event.getX() * 2 < getWidth() ? Math.max(event.getX(), mAreaRadius) : Math.min(event.getX(), getWidth() - mAreaRadius),
                        event.getY() * 2 < getHeight() ? Math.max(event.getY(), mAreaRadius) : Math.min(event.getY(), getHeight() - mAreaRadius));
                ePoint.set(event.getX() * 2 < getWidth() ? Math.max(event.getX(), mRockerRadius) : Math.min(event.getX(), getWidth() - mRockerRadius),
                        event.getY() * 2 < getHeight() ? Math.max(event.getY(), mRockerRadius) : Math.min(event.getY(), getHeight() - mRockerRadius));
                if (ePoint.equals(sPoint)) {
                    mDistance = 0;
                } else {
                    mDistance = (float) Math.sqrt(Math.pow(ePoint.x - sPoint.x, 2) + Math.pow(ePoint.y - sPoint.y, 2)) / 2;
                    cPoint.set(mDistance > mRockerRadius ? (sPoint.x + ePoint.x) / 2 : -1, mDistance > mRockerRadius ? (sPoint.y + ePoint.y) / 2 : -1);
                }
                invalidate();
                if (mOnDirectionListener != null) {
                    responseDirectionEvent();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                ePoint.set(event.getX() * 2 < getWidth() ? Math.max(event.getX(), mRockerRadius) : Math.min(event.getX(), getWidth() - mRockerRadius),
                        event.getY() * 2 < getHeight() ? Math.max(event.getY(), mRockerRadius) : Math.min(event.getY(), getHeight() - mRockerRadius));
                float distance = (float) Math.sqrt(Math.pow(ePoint.x - sPoint.x, 2) + Math.pow(ePoint.y - sPoint.y, 2));
                if (mAreaRadius != -1 && distance > mAreaRadius) {
                    float scale = (distance - mAreaRadius) / distance;
                    sPoint.offset((ePoint.x - sPoint.x) * scale, (ePoint.y - sPoint.y) * scale);
                    mDistance = mAreaRadius / 2;
                } else {
                    mDistance = distance / 2;
                }
                cPoint.set(mDistance > mRockerRadius ? (sPoint.x + ePoint.x) / 2 : -1, mDistance > mRockerRadius ? (sPoint.y + ePoint.y) / 2 : -1);
                invalidate();
                if (mOnDirectionListener != null) {
                    responseDirectionEvent();
                }
                break;
            case MotionEvent.ACTION_UP:
                cancelTimer();
                sPoint.set(-1, -1);
                ePoint.set(-1, -1);
                cPoint.set(-1, -1);
                invalidate();
                mLastDirectionEvent = -1;
                if (mOnActionListener != null) {
                    mOnActionListener.accept(EVENT_ACTION_UP);
                }
                break;
        }
        return true;
    }

    private void responseDirectionEvent() {
//        float distance = MathUtils.getDistance(mAreaPosition.x, mAreaPosition.y, mRockerPosition.x, mRockerPosition.y);
        int directionEvent = EVENT_DIRECTION_CENTER;
        if (mDistance > mValidRadius) {
            float radian = MathUtils.getRadian(sPoint, ePoint);
            int angle = getAngleConvert(radian);
            if (angle >= 315) {
                directionEvent = EVENT_DIRECTION_RIGHT;
            } else if (angle > 225) {
                directionEvent = EVENT_DIRECTION_DOWN;
            } else if (angle >= 135) {
                directionEvent = EVENT_DIRECTION_LEFT;
            } else if (angle > 45) {
                directionEvent = EVENT_DIRECTION_UP;
            } else if (angle >= 0) {
                directionEvent = EVENT_DIRECTION_RIGHT;
            }
        }
        if (directionEvent != mLastDirectionEvent) {
            cancelTimer();
            mOnDirectionListener.accept(directionEvent);
            if (directionEvent != EVENT_DIRECTION_CENTER) {
                initTimer(directionEvent);
            }
            mLastDirectionEvent = directionEvent;
        }
    }

    /**
     * 获取摇杆偏移角度 0-360°
     */
    private int getAngleConvert(float radian) {
        int tmp = (int) Math.round(radian / Math.PI * 180);
        if (tmp < 0) {
            return -tmp;
        } else {
            return 180 + (180 - tmp);
        }
    }

    private Handler mHandler = new Handler(msg -> {
        if (mOnDirectionListener != null) {
            mOnDirectionListener.accept(((Integer) msg.obj));
        }
        return true;
    });

    private Timer timer;

    private void initTimer(int directionEvent) {
        if (timer != null) {
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                mHandler.sendMessage(mHandler.obtainMessage(MESSAGE_WHAT_DIRECTION, directionEvent));
            }
        }, 250, 50);
    }

    private void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
        mHandler.removeMessages(MESSAGE_WHAT_DIRECTION);
    }

    public void setOnActionListener(IntConsumer listener) {
        this.mOnActionListener = listener;
    }

    public void setOnDirectionListener(IntConsumer listener) {
        this.mOnDirectionListener = listener;
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        if (!hasWindowFocus) {
            cancelTimer();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelTimer();
    }
}
