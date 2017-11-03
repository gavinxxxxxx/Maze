package me.gavin.game.maze;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

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
        createMaze(SPUtil.getInt("lineCount", 10));
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
            Cell[][] cells = Utils.prim(count, count);
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
                createMaze(SPUtil.getInt("lineCount", 10));
                return true;
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
        if (doneCount == 5) {
            new AlertDialog.Builder(this)
                    .setTitle("恭喜过关")
                    .setMessage("给个好评？")
                    .setPositiveButton("去评价", (dialog, which)
                            -> goToMarket())
                    .setNegativeButton("再来一局", (dialog, which)
                            -> createMaze(count + 10))
                    .setNeutralButton("自定义", (dialog, which)
                            -> showDiyDialog())
                    .show();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("恭喜过关")
                    .setMessage("少年看你骨骼惊奇，不如再来一局？")
                    .setPositiveButton("再来一局", (dialog, which)
                            -> createMaze(count + 10))
                    .setNegativeButton("取消", null)
                    .setNeutralButton("自定义", (dialog, which)
                            -> showDiyDialog())
                    .show();
        }
    }

    private void showDiyDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_diy, null);
        new AlertDialog.Builder(this)
                .setTitle("自定义难度")
                .setView(view)
                .setPositiveButton("确定", (dialog, which) -> {
                    EditText editText = view.findViewById(R.id.editText);
                    try {
                        int count = Integer.parseInt(editText.getText().toString());
                        if (count <= 1) {
                            Toast.makeText(this, "请输入大于 1 小于 1000 的有效数字", Toast.LENGTH_LONG).show();
                            return;
                        }
                        createMaze(count);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "请输入大于 1 小于 1000 的有效数字", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    /**
     * 去手机应用市场
     */
    private void goToMarket() {
        //这里开始执行一个应用市场跳转逻辑，默认this为Context上下文对象
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("market://details?id=" + getPackageName())); //跳转到应用市场，非Google Play市场一般情况也实现了这个接口
        //存在手机里没安装应用市场的情况，跳转会包异常，做一个接收判断
        if (intent.resolveActivity(getPackageManager()) != null) { //可以接收
            startActivity(intent);
        } else {
            Toast.makeText(this, "连应用市场都没有，这我没法接", Toast.LENGTH_LONG).show();
        }
    }
}
