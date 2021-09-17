package com.yagi.android.numbercatch

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.text.TextPaint
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import android.graphics.RectF
import kotlin.math.max


class MainActivity : AppCompatActivity(), SensorEventListener {
    var gameView: GameView? = null
    var sensorManager: SensorManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        findViewById<SurfaceView>(R.id.SurfaceViewMain)
        gameView = GameView(this)
        setContentView(gameView)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.menu_restart) {
            gameView!!.thread!!.interrupt()
            val alertDlg = AlertDialog.Builder(this)
            alertDlg.setTitle("ゲームをリスタートしますか？")
            alertDlg.setMessage("プレイ中の記録は戻らなくなります")
            alertDlg.setPositiveButton(
                "はい"
            ) { dialog, which -> // OK ボタンクリック処理
                val i = Intent(this@MainActivity, MainActivity::class.java)
                startActivity(i)
                finish()
            }
            alertDlg.setNegativeButton(
                "いいえ"
            ) { dialog, which ->
                // Cancel ボタンクリック処理
            }

            // 表示
            alertDlg.create().show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        val sensors = sensorManager!!.getSensorList(Sensor.TYPE_ACCELEROMETER)
        if (!sensors.isEmpty()) {
            sensorManager!!.registerListener(this, sensors[0], SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(this)
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            if (gameView!!.player != null) {
                gameView!!.player!!.move(-event.values[0])
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    inner class GameView(context: Context?) : SurfaceView(context),
        SurfaceHolder.Callback, Runnable {
        var screenWidth = 0
        var screenHeight = 0
        var score = 0
        var life = 400
        var fallingNumbers = IntArray(5)
        var countUp = 0
        var surfaceHolder: SurfaceHolder? = null
        var thread: Thread? = null
        lateinit var numbers: Array<Number?>
        var numberImages: Array<Bitmap?>
        var matrix1 = Matrix()
        var matrix2 = Matrix()
        var player: Player? = null
        var mMainActivity: MainActivity?
        override fun surfaceCreated(holder: SurfaceHolder) {
            surfaceHolder = holder
            thread = Thread(this)
            thread!!.start()
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
            screenWidth = width
            screenHeight = height
        }

        override fun surfaceDestroyed(holder: SurfaceHolder) {
            thread = null
        }

        override fun run() {
            var counter = 0
            player = Player()
            numbers = arrayOfNulls(5)
            numbers[0] = Number(0)
            numbers[1] = Number(1)
            numbers[2] = Number(2)
            numbers[3] = Number(3)
            numbers[4] = Number(4)
            val pre = BooleanArray(5)
            pre[0] = true
            for (i in 1 until fallingNumbers.size) {
                pre[i] = false
            }
            while (thread != null) {
                val canvas = surfaceHolder!!.lockCanvas()
                canvas.drawColor(Color.WHITE)
                val catchPaint = Paint()
                val lifePaint = Paint()
                val lifeBackPaint = Paint()
                catchPaint.textSize = 120f
                lifePaint.textSize = 100f
                canvas.drawText(score.toString(), player!!.x, player!!.y, catchPaint)
                val textPaint = arrayOfNulls<Paint>(5)
                for (i in textPaint.indices) {
                    textPaint[i] = TextPaint(i)
                    textPaint[i]?.setTextSize(100f)
                }
                textPaint[0]!!.color = Color.rgb(255, 70, 0)
                textPaint[1]!!.color = Color.BLUE
                textPaint[2]!!.color = Color.rgb(34, 139, 34)
                textPaint[3]!!.color = Color.MAGENTA
                textPaint[4]!!.color = Color.GRAY
                canvas.drawText(
                    fallingNumbers[0].toString(),
                    numbers[0]!!.x,
                    numbers[0]!!.y,
                    textPaint[0]!!
                )
                for (i in fallingNumbers.indices) {
                    if (counter > i * 5 - 1) {
                        canvas.drawText(
                            fallingNumbers[i].toString(),
                            numbers[i]!!.x,
                            numbers[i]!!.y,
                            textPaint[i]!!
                        )
                        pre[i] = true
                    }
                }
                for (`in` in fallingNumbers.indices) {
                    if (pre[`in`]) {
                        if (player!!.isEnter(numbers[`in`])) {
                            countUp = score
                            score += fallingNumbers[`in`]
                            numbers[`in`]!!.reset(`in`)
                            counter++
                        } else if (numbers[`in`]!!.y > screenHeight) {
                            life = max(life - fallingNumbers[`in`], 0)
                            numbers[`in`]!!.reset(`in`)
                        } else {
                            numbers[`in`]!!.update()
                        }
                    }
                }
                canvas.drawText(score.toString(), player!!.x, player!!.y, catchPaint)
                for (paint in textPaint) {
                    paint!!.isFakeBoldText = true
                }
                val rectF = RectF(0F, 0F, screenWidth.toFloat(), 200F)
                lifeBackPaint.setColor(Color.rgb(240, 240, 240))
                canvas.drawRect(rectF, lifeBackPaint)
                if (life <= 100){
                    lifePaint.color = Color.RED
                }
                canvas.drawText("LIFE:$life", 50f, 135f, lifePaint)
                if (life == 0) {
                    canvas.drawText(
                        "Game Over",
                        (screenWidth / 3).toFloat(),
                        (screenHeight / 2).toFloat(),
                        lifePaint
                    )
                    surfaceHolder!!.unlockCanvasAndPost(canvas)
                    val i = Intent(mMainActivity, FinishActivity::class.java)
                    i.putExtra("score", score)
                    startActivity(i)
                    break
                }
                surfaceHolder!!.unlockCanvasAndPost(canvas)
                try {
                    Thread.sleep(FRAME_TIME)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }

        inner class Number(`in`: Int) {
            var x: Float
            var y: Float
            fun update() {
                y += 30.0f
            }

            val WIDTH = 100
            val HEIGHT = 100


            fun reset(`in`: Int) {
                val random = Random()
                x = random.nextInt(screenWidth - WIDTH).toFloat()
                y = 0f
                val r = Random()
                fallingNumbers[`in`] = r.nextInt(30) + 1
            }

            init {
                val random = Random()
                x = random.nextInt(screenWidth - WIDTH).toFloat()
                y = 0f
                val r = Random()
                fallingNumbers[`in`] = r.nextInt(30) + 1
            }
        }

        inner class Player {
            val WIDTH = 200
            val HEIGHT = 200
            var x = 0f
            var y: Float
            fun move(diffX: Float) {
                x += diffX
                x = Math.max(0f, x)
                x = Math.min((screenWidth - WIDTH).toFloat(), x)
            }

            fun isEnter(number: Number?): Boolean {
                return number!!.x + number.WIDTH > x && number.x < x + WIDTH && number.y + number.HEIGHT > y && number.y < y + HEIGHT
            }

            init {
                y = (screenHeight - HEIGHT).toFloat()
            }
        }


        private val FPS: Long = 30
        val FRAME_TIME = 1000 / FPS


        init {
            numberImages = arrayOfNulls(5)
            mMainActivity = context as MainActivity?
            holder.addCallback(this)
            val presentImage_ratio_w = 0.05f
            val presentImage_ratio_h = 0.05f
            val playerImage_ratio_w = 0.08f
            val playerImage_ratio_h = 0.08f
            matrix1.postScale(presentImage_ratio_w, presentImage_ratio_h)
            matrix2.postScale(playerImage_ratio_w, playerImage_ratio_h)
            val r = Random()
            for (`in` in fallingNumbers.indices) {
                fallingNumbers[`in`] = r.nextInt(30) + 1
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            // 確認ダイアログの生成
            val alertDlg = AlertDialog.Builder(this)
            alertDlg.setTitle("終了しますか？")
            alertDlg.setMessage("プレイ中の記録は戻らなくなります")
            alertDlg.setPositiveButton(
                "はい"
            ) { dialog, which -> // OK ボタンクリック処理
                finish()
            }
            alertDlg.setNegativeButton(
                "いいえ"
            ) { dialog, which ->
                // Cancel ボタンクリック処理
            }

            // 表示
            alertDlg.create().show()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}