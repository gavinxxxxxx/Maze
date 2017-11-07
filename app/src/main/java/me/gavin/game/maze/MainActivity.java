package me.gavin.game.maze;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import me.gavin.game.maze.util.InputUtil;
import me.gavin.game.maze.util.L;
import me.gavin.game.maze.util.SPUtil;
import me.gavin.game.rocker.RockerView;

public class MainActivity extends Activity {

    MazeView mazeView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main);
        mazeView = findViewById(R.id.maze);
        progressBar = findViewById(R.id.progressBar);
        RockerView rocker = findViewById(R.id.rocker);
        rocker.setOnDirectionListener(i -> {
            switch (i) {
                case RockerView.EVENT_DIRECTION_LEFT:
                    mazeView.left();
                    break;
                case RockerView.EVENT_DIRECTION_UP:
                    mazeView.up();
                    break;
                case RockerView.EVENT_DIRECTION_RIGHT:
                    mazeView.right();
                    break;
                case RockerView.EVENT_DIRECTION_DOWN:
                    mazeView.down();
                    break;
                default:
                    break;
            }
        });
        createMaze(SPUtil.getInt("lineCount", 2));
    }

    public void createMaze2(int count) {
        SPUtil.saveInt("lineCount", count);
        mazeView.setCells(null, null);
        progressBar.setVisibility(View.VISIBLE);
        getActionBar().setSubtitle("生成中...");
        if (count > 100) {
            Toast.makeText(this, "地图生成中，生成单列大于 100 的地图将消耗大量时间，请耐心等候", Toast.LENGTH_LONG).show();
        }
        new Thread(() -> {
            Cell[][] cells = Utils.prim(count, count);
            mazeView.post(() -> {
                mazeView.setCells(cells, this::onComplete);
                progressBar.setVisibility(View.GONE);
                getActionBar().setSubtitle(String.format("%s x %s", count, count));
            });
        }).start();
    }

    public void createMaze(int count) {
        SPUtil.saveInt("lineCount", count);
        mazeView.setCells(null, null);
        progressBar.setVisibility(View.VISIBLE);
        getActionBar().setSubtitle("生成中...");
        if (count > 100) {
            Toast.makeText(this, "地图生成中，生成单列大于 100 的地图将消耗大量时间，请耐心等候", Toast.LENGTH_LONG).show();
        }
        new Thread(() -> {
            Cell[][] cells = prim(count, count);
            mazeView.post(() -> {
                mazeView.setCells(cells, this::onComplete);
                progressBar.setVisibility(View.GONE);
                getActionBar().setSubtitle(String.format("%s x %s", count, count));
            });
        }).start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                createMaze(SPUtil.getInt("lineCount", 2));
                return true;
//            case R.id.action_refresh2:
//                createMaze2(SPUtil.getInt("lineCount", 2));
//                return true;
            case R.id.action_config:
                showDiyDialog();
                return true;
            default:
                return false;
        }
    }

    private void onComplete(int count) {
        int doneCount = SPUtil.getInt("doneCount", 0) + 1;
        SPUtil.saveInt("doneCount", doneCount);
        if (doneCount % 10 == 0 && SPUtil.getInt("feedback", 0) == 0) {
            new AlertDialog.Builder(this)
                    .setTitle("恭喜过关")
                    .setMessage("亲，赏脸给个好评呗？")
                    .setPositiveButton("下一局", (dialog, which)
                            -> createMaze(count + 2))
                    .setNegativeButton("取消", null)
                    .setNeutralButton("去评价", (dialog, which)
                            -> goToMarket())
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("恭喜过关")
                    .setMessage("少年看你骨骼惊奇，不如再来一局？")
                    .setPositiveButton("下一局", (dialog, which)
                            -> createMaze(count + 2))
                    .setNegativeButton("取消", null)
                    .setNeutralButton("自定义", (dialog, which)
                            -> showDiyDialog())
                    .show();
        }
    }

    private void showDiyDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_diy, null);
        EditText editText = view.findViewById(R.id.editText);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("自定义难度")
                .setView(view)
                .setPositiveButton("确定", (dialog, which)
                        -> tryCreate(editText.getText().toString()))
                .setNegativeButton("取消", null)
                .show();
        editText.postDelayed(() -> InputUtil.show(this, editText), 100);
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                alertDialog.dismiss();
                tryCreate(editText.getText().toString());
            }
            return true;
        });
    }

    private void tryCreate(String numStr) {
        try {
            int count = Integer.parseInt(numStr);
            if (count <= 1) {
                Toast.makeText(this, "请输入大于 1 小于 1000 的整数", Toast.LENGTH_LONG).show();
            } else {
                createMaze(count);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入大于 1 小于 1000 的整数", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 去手机应用市场
     */
    private void goToMarket() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + getPackageName())); //跳转到应用市场，非Google Play市场一般情况也实现了这个接口
        if (intent.resolveActivity(getPackageManager()) != null) { //可以接收
            startActivityForResult(intent, 0);
        } else {
            Toast.makeText(this, "连应用市场都没有，你这是存心过不去啊", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SPUtil.saveInt("feedback", 1);
    }

    volatile Cell[][] cells;
    volatile List<Cell> yet = new ArrayList<>();
    Random random = new Random(System.nanoTime());

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
            L.e("index: " + index);
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

    private Cell[][] prim(int xc, int yc) {
        cells = new Cell[xc][yc];
        for (int x = 0; x < xc; x++) {
            for (int y = 0; y < yc; y++) {
                cells[x][y] = new Cell(x, y);
                if (x == 0 && y == 0)
                    cells[x][y].addFlag(Cell.FLAG_TOP);
                else if (x == xc - 1 && y == yc - 1)
                    cells[x][y].addFlag(Cell.FLAG_BOTTOM);
            }
        }

        yet.clear();

        int poolCount = Math.min(30, xc * yc / 1024 + 1);

        if (executor != null) executor.shutdownNow();
        executor = Executors.newFixedThreadPool(poolCount);

        for (int i = 0; i < poolCount; i++) {
            Cell curr = random(xc, yc, 0);
            if (curr != null) {
                curr.index = 1 << i;
                MyRunnable runnable = new MyRunnable(curr, xc, yc, 1 << i);
                executor.execute(runnable);
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

    ExecutorService executor;
}
