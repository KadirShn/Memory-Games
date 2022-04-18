package com.kadirsahin.hafizaoyunu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class Zorluk : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_zorluk)

        val dortluk = findViewById<Button>(R.id.dortlukbtn)
        dortluk.setOnClickListener {
            val intent = Intent(this, Dortluk::class.java)
            startActivity(intent)
        }
        val altilik = findViewById<Button>(R.id.altilikbtn)
        altilik.setOnClickListener {
            val intent = Intent(this, Altilik::class.java)
            startActivity(intent)
        }


    }
}