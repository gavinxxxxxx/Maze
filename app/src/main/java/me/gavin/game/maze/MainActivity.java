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

import me.gavin.game.maze.util.InputUtil;
import me.gavin.game.maze.util.SPUtil;
import me.gavin.game.rocker.RockerView;

public class MainActivity extends Activity {

    MazeView mazeView;
    ProgressBar progressBar;

    private boolean isInProgress;

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
        mazeView.postDelayed(() -> createMaze(Math.min(SPUtil.getInt("lineCount", 2), 100)), 200);
    }

    public void createMaze(int count) {
        SPUtil.saveInt("lineCount", count);
        mazeView.setCells(null, null);
        progressBar.setVisibility(View.VISIBLE);
        getActionBar().setSubtitle(String.format("%s x %s 生成中...", count, count));
        if (count > 100) {
            Toast.makeText(this, "地图生成中，生成单列大于 100 的地图将消耗大量时间，请耐心等候", Toast.LENGTH_LONG).show();
        }
        isInProgress = true;
        new Thread(() -> {
            // Cell[][] cells = PrimUtil.prim(count, count);
            Cell[][] cells = new PrimUtil4ThreadPool().prim(count, count);
            mazeView.post(() -> {
                mazeView.setCells(cells, this::onComplete);
                progressBar.setVisibility(View.GONE);
                getActionBar().setSubtitle(String.format("%s x %s", count, count));
                isInProgress = false;
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
                if (isInProgress) {
                    Toast.makeText(this, "迷宫生成中，请稍候...", Toast.LENGTH_LONG).show();
                } else {
                    createMaze(SPUtil.getInt("lineCount", 2));
                }
                return true;
            case R.id.action_config:
                if (isInProgress) {
                    Toast.makeText(this, "迷宫生成中，请稍候...", Toast.LENGTH_LONG).show();
                } else {
                    showDiyDialog();
                }
                return true;
            default:
                // goToMarket();
                return false;
        }
    }

    private void onComplete(int count) {
        int doneCount = SPUtil.getInt("doneCount", 0) + 1;
        SPUtil.saveInt("doneCount", doneCount);

        boolean shouldFeed = doneCount % 10 == 0 && SPUtil.getInt("feedback", 0) == 0;
        new AlertDialog.Builder(this)
                .setTitle("恭喜过关")
                .setMessage(shouldFeed ? "亲，赏脸给个好评呗？" : "少年看你骨骼惊奇，不如再来一局？")
                .setPositiveButton("下一局", (dialog, which)
                        -> createMaze(count + 2))
                .setNegativeButton(shouldFeed ? "去评价" : "取消", (dialog, which) -> {
                    if (shouldFeed) {
                        goToMarket();
                    }
                })
                .setNeutralButton("自定义", (dialog, which)
                        -> showDiyDialog())
                .show();
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
            if (count <= 1 || count > 200) {
                Toast.makeText(this, "请输入大于 1 小于等于 200 的整数", Toast.LENGTH_LONG).show();
            } else {
                createMaze(count);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "请输入大于 1 小于等于 200 的整数", Toast.LENGTH_LONG).show();
        }
    }

    private void goToMarket() {
        startActivityForResult(new Intent(Intent.ACTION_CHOOSER)
                .putExtra(Intent.EXTRA_TITLE, "给个好评")
                .putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_VIEW)
                        .setData(Uri.parse("market://details?id=" + getPackageName()))), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SPUtil.saveInt("feedback", 1);
    }

}
