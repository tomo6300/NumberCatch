package com.yagi.android.numbercatch;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class FinishActivity extends AppCompatActivity{

    int score;
    TextView finalScoreText;
    TextView bestScoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_game_finish);

        Intent i = getIntent();
        score = i.getIntExtra("score", 0);
        finalScoreText = (TextView)findViewById(R.id.textView_FinalScore);
        bestScoreText = (TextView)findViewById(R.id.textView_bestScore);
        Log.d("intent2", String.valueOf(score));
        finalScoreText.setText(String.valueOf(score));
        Log.d("text", String.valueOf(finalScoreText));
        SharedPreferences mHighScorePref = this.getSharedPreferences("NumberCatch", 0);
        int highScore = mHighScorePref.getInt("BEST SCORE", 0);
        if (highScore < this.score) {
            SharedPreferences.Editor editor = mHighScorePref.edit();
            editor.putInt("BEST SCORE", this.score);
            editor.apply();
            this.bestScoreText.setText(String.valueOf(this.score));
        } else {
            this.bestScoreText.setText(String.valueOf(highScore));
        }
    }

    public void restart (View v){
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }


}
