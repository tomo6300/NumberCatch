package com.yagi.android.numbercatch;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextPaint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    GameView gameView;
    SensorManager sensorManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.SurfaceViewMain);
        gameView = new GameView(this, surfaceView);
        setContentView(gameView);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_restart) {
            gameView.thread.interrupt();
            AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
            alertDlg.setTitle("終了しますか？");
            alertDlg.setMessage("プレイ中の記録は戻らなくなります");
            alertDlg.setPositiveButton(
                    "はい",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // OK ボタンクリック処理
                            Intent i = new Intent(MainActivity.this, MainActivity.class);
                            startActivity(i);
                            finish();
                        }
                    });
            alertDlg.setNegativeButton(
                    "いいえ",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Cancel ボタンクリック処理

                        }
                    });

            // 表示
            alertDlg.create().show();


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (!sensors.isEmpty()) {
            sensorManager.registerListener(this, sensors.get(0), SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    protected void onPause() {
        sensorManager.unregisterListener(this);
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Log.d("SensorValues", "\nX軸:" + event.values[0] + "\nY軸" + event.values[1] + "\nZ軸" + event.values[2]);
            if (gameView.player != null) {
                gameView.player.move(-event.values[0]);
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

        int screenWidth, screenHeight;
        int score = 0;
        int life = 400;
        int[] fallingNumbers = new int[5];
        int countUp;

        static final long FPS = 30;
        static final long FRAME_TIME = 1000 / FPS;

        SurfaceHolder surfaceHolder;
        Thread thread;
        Number[] numbers;
        Bitmap[] numberImages;
        Matrix matrix = new Matrix();
        Matrix matrix2 = new Matrix();
        Player player;
        MainActivity mMainActivity;


        public GameView(Context context, SurfaceView sv) {
            super(context);
            numberImages = new Bitmap[5];
            this.mMainActivity = (MainActivity) context;
            getHolder().addCallback(this);
            float presentImage_ratio_w = 0.05f;
            float presentImage_ratio_h = 0.05f;
            float playerImage_ratio_w = 0.08f;
            float playerImage_ratio_h = 0.08f;
            matrix.postScale(presentImage_ratio_w, presentImage_ratio_h);
            matrix2.postScale(playerImage_ratio_w, playerImage_ratio_h);
            Random r = new Random();
            for (int in = 0; in < fallingNumbers.length; in++) {
                fallingNumbers[in] = r.nextInt(30) + 1;
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            surfaceHolder = holder;
            thread = new Thread(this);
            thread.start();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            screenWidth = width;
            screenHeight = height;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            thread = null;
        }


        @Override
        public void run() {
            int counter = 0;
            player = new Player();
            numbers = new Number[5];
            numbers[0] = new Number(0);
            numbers[1] = new Number(1);
            numbers[2] = new Number(2);
            numbers[3] = new Number(3);
            numbers[4] = new Number(4);
            boolean[] pre = new boolean[5];
            pre[0] = true;
            for (int i = 1; i < fallingNumbers.length; i++) {
                pre[i] = false;
            }

            while (thread != null) {
                final Canvas canvas = surfaceHolder.lockCanvas();
                canvas.drawColor(Color.WHITE);
                final Paint catchPaint = new Paint();
                Paint lifePaint = new Paint();
                catchPaint.setTextSize(120);
                lifePaint.setTextSize(100);
                canvas.drawText(String.valueOf(score), player.x, player.y, catchPaint);
                Paint[] textPaint = new Paint[5];
                for (int i = 0; i < textPaint.length; i++) {
                    textPaint[i] = new TextPaint(i);
                    textPaint[i].setTextSize(100);
                }
                textPaint[0].setColor(Color.rgb(255, 70, 0));
                textPaint[1].setColor(Color.BLUE);
                textPaint[2].setColor(Color.rgb(34, 139, 34));
                textPaint[3].setColor(Color.MAGENTA);
                textPaint[4].setColor(Color.GRAY);
                canvas.drawText(String.valueOf(fallingNumbers[0]), numbers[0].x, numbers[0].y, textPaint[0]);
                for (int i = 0; i < fallingNumbers.length; i++) {
                    if (counter > i * 5 - 1) {
                        canvas.drawText(String.valueOf(fallingNumbers[i]), numbers[i].x, numbers[i].y, textPaint[i]);
                        pre[i] = true;
                    }
                }

                for (int in = 0; in < fallingNumbers.length; in++) {
                    if (pre[in]) {
                        if (player.isEnter(numbers[in])) {
                            countUp = score;
                            score += fallingNumbers[in];
                            numbers[in].reset(in);
                            counter++;
                        } else if (numbers[in].y > screenHeight) {
                            life -= fallingNumbers[in];
                            numbers[in].reset(in);
                        } else {
                            numbers[in].update();
                        }
                    }

                }

                canvas.drawText(String.valueOf(score), player.x, player.y, catchPaint);
                for (Paint paint : textPaint) {
                    paint.setFakeBoldText(true);
                }


                if (life <= 0) {
                    life = 0;
                    lifePaint.setColor(Color.RED);
                    canvas.drawText("LIFE:" + 0, 50, 150, lifePaint);
                    canvas.drawText("Game Over", screenWidth / 3, screenHeight / 2, lifePaint);
                    surfaceHolder.unlockCanvasAndPost(canvas);

                    Intent i = new Intent(mMainActivity, FinishActivity.class);
                    Log.d("intent", String.valueOf(score));
                    i.putExtra("score", score);
                    startActivity(i);

                    break;

                } else if (life <= 100) {
                    lifePaint.setColor(Color.RED);
                    canvas.drawText("LIFE:" + life, 50, 150, lifePaint);
                } else {
                    canvas.drawText("LIFE:" + life, 50, 150, lifePaint);
                }
                surfaceHolder.unlockCanvasAndPost(canvas);

                try {
                    Thread.sleep(FRAME_TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        class Number {
            private static final int WIDTH = 100;
            private static final int HEIGHT = 100;

            float x, y;


            private void update() {
                y += 30.0f;
            }

            private Number(int in) {
                Random random = new Random();
                x = random.nextInt(screenWidth - WIDTH);
                y = 0;
                Random r = new Random();
                fallingNumbers[in] = r.nextInt(30) + 1;
            }


            private void reset(int in) {
                Random random = new Random();
                x = random.nextInt(screenWidth - WIDTH);
                y = 0;
                Random r = new Random();
                fallingNumbers[in] = r.nextInt(30) + 1;
            }
        }

        class Player {
            final int WIDTH = 200;
            final int HEIGHT = 200;

            float x, y;

            private Player() {
                x = 0;
                y = screenHeight - HEIGHT;
            }

            private void move(float diffX) {
                this.x += diffX;
                this.x = Math.max(0, x);
                this.x = Math.min(screenWidth - WIDTH, x);
            }

            private boolean isEnter(Number number) {
                return number.x + Number.WIDTH > x && number.x < x + WIDTH && number.y + Number.HEIGHT > y && number.y < y + HEIGHT;
            }
        }
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            // 確認ダイアログの生成
            AlertDialog.Builder alertDlg = new AlertDialog.Builder(this);
            alertDlg.setTitle("終了しますか？");
            alertDlg.setMessage("プレイ中の記録は戻らなくなります");
            alertDlg.setPositiveButton(
                    "はい",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // OK ボタンクリック処理
                            finish();
                        }
                    });
            alertDlg.setNegativeButton(
                    "いいえ",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // Cancel ボタンクリック処理

                        }
                    });

            // 表示
            alertDlg.create().show();
            return true;


        }

        return super.onKeyDown(keyCode, event);
    }


}
