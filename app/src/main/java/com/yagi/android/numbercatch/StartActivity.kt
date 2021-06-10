package com.yagi.android.numbercatch

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        val start: Button = findViewById(R.id.start_btn)

        start.setOnClickListener{
            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }

    }
}