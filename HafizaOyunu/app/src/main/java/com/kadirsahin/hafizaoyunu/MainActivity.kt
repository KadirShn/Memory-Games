package com.kadirsahin.hafizaoyunu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_skorlar.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val baslabtn = findViewById<Button>(R.id.baslabtn)
        baslabtn.setOnClickListener {
            val intent = Intent(this,Zorluk::class.java)
            startActivity(intent)
        }
        val skorbtn = findViewById<Button>(R.id.skorbtn)
        skorbtn.setOnClickListener {
            val intent = Intent(this,Skorlar::class.java)

            startActivity(intent)
        }
        val cikisbtn = findViewById<Button>(R.id.cikisbtn)
        cikisbtn.setOnClickListener {
            finishAffinity();
            this@MainActivity.finish()

            exitProcess(0)
        }
        val context = this
        var db = DataBase(context)
        var data3 = db.max4()
        var data4 = db.max6()
        txt4luk.text=""
        txt6lik.text=""
        for (i in 0 until data3.size)
        {
            txt4luk.append("\t"+data3.get(i).pun )
        }
        for (j in 0 until data4.size)
        {
            txt6lik.append("\t"+data4.get(j).pun )
        }
    }
}