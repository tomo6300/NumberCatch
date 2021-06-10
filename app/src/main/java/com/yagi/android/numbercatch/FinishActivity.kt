package com.yagi.android.numbercatch

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class FinishActivity : AppCompatActivity() {
    var score = 0
    var finalScoreText: TextView? = null
    var bestScoreText: TextView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_game_finish)
        val i = intent
        score = i.getIntExtra("score", 0)
        finalScoreText = findViewById<View>(R.id.textView_FinalScore) as TextView
        bestScoreText = findViewById<View>(R.id.textView_bestScore) as TextView
        Log.d("intent2", score.toString())
        finalScoreText!!.text = score.toString()
        Log.d("text", finalScoreText.toString())
        val mHighScorePref = getSharedPreferences("NumberCatch", 0)
        val highScore = mHighScorePref.getInt("BEST SCORE", 0)
        if (highScore < score) {
            val editor = mHighScorePref.edit()
            editor.putInt("BEST SCORE", score)
            editor.apply()
            bestScoreText!!.text = score.toString()
        } else {
            bestScoreText!!.text = highScore.toString()
        }
    }

    fun restart(v: View?) {
        val i = Intent(this, MainActivity::class.java)
        startActivity(i)
        finish()
    }
}