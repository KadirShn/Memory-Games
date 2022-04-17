package com.kadirsahin.hafizaoyunu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_altilik.*
import kotlinx.android.synthetic.main.activity_dortluk.*
import kotlinx.android.synthetic.main.activity_dortluk.Puan
import kotlinx.android.synthetic.main.activity_dortluk.eslesme
import kotlinx.android.synthetic.main.activity_dortluk.fotobtn1
import kotlinx.android.synthetic.main.activity_dortluk.fotobtn10
import kotlinx.android.synthetic.main.activity_dortluk.fotobtn11
import kotlinx.android.synthetic.main.activity_dortluk.fotobtn12
import kotlinx.android.synthetic.main.activity_dortluk.fotobtn13
import kotlinx.android.synthetic.main.activity_dortluk.fotobtn14
import kotlinx.android.synthetic.main.activity_dortluk.fotobtn15
import kotlinx.android.synthetic.main.activity_dortluk.fotobtn16
import kotlinx.android.synthetic.main.activity_dortluk.fotobtn2
import kotlinx.android.synthetic.main.activity_dortluk.fotobtn3
import kotlinx.android.synthetic.main.activity_dortluk.fotobtn4
import kotlinx.android.synthetic.main.activity_dortluk.fotobtn5
import kotlinx.android.synthetic.main.activity_dortluk.fotobtn6
import kotlinx.android.synthetic.main.activity_dortluk.fotobtn7
import kotlinx.android.synthetic.main.activity_dortluk.fotobtn8
import kotlinx.android.synthetic.main.activity_dortluk.fotobtn9
import kotlinx.android.synthetic.main.activity_dortluk.gerisayim
import kotlinx.android.synthetic.main.activity_dortluk.hamle

private const val TAG = "Dortluk"
class Dortluk : AppCompatActivity() {

    private lateinit var buttons: List<ImageButton>
    private lateinit var cards: List<Kartlar>
    private var indexOfSingleSelectedCard: Int? = null
    private var eslesmesayisi =0
    private var hamlesayisi =0
    private var puan =0
    private var zmn=""
    private var knt=0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dortluk)
        object : CountDownTimer(60000, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                gerisayim.text=(millisUntilFinished / 1000).toString()
                if(eslesmesayisi == 8)
                {
                    if(zmn=="")
                    {
                        zmn = (millisUntilFinished / 1000).toString()
                        mesajkutusu()
                        knt=1
                    }
                    gerisayim.text= zmn
                }
            }
            override fun onFinish() {
                if(knt==0)
                {
                    mesajkutusu()
                }

            }
        }.start()
        val images = mutableListOf(R.drawable.a, R.drawable.b, R.drawable.c, R.drawable.d, R.drawable.e,R.drawable.f,R.drawable.g,R.drawable.h)
        images.addAll(images)
        images.shuffle()
        images.shuffle()
        buttons = listOf(fotobtn1,fotobtn2,fotobtn3,fotobtn4,fotobtn5,fotobtn6,fotobtn7,fotobtn8,fotobtn9,
            fotobtn10,fotobtn11,fotobtn12,fotobtn13,fotobtn14,fotobtn15,fotobtn16)
        cards = buttons.indices.map { index ->
            Kartlar(images[index])
        }
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {

                hamlesayisi+=1

                hamle.text = hamlesayisi.toString()
                updateModels(index)
                updateViews()
            }
        }
    }
    private fun updateViews() {
        cards.forEachIndexed { index, card ->
            val button = buttons[index]
            if (card.isMatched) {
                button.alpha = 0.5f
                button.isEnabled=false
            }
            button.setImageResource(if (card.isFaceUp) card.identifier else R.drawable.codee)
        }
    }
    private val tekrarkont=0
    private fun updateModels(position: Int) {

        val card = cards[position]
        // Error checking:
        if (card.isFaceUp) {
            return
        }
        if (indexOfSingleSelectedCard == null) {
            restoreCards()
            indexOfSingleSelectedCard = position
        } else {
            checkForMatch(indexOfSingleSelectedCard!!, position)
            indexOfSingleSelectedCard = null
        }
        card.isFaceUp = !card.isFaceUp
    }
    private fun restoreCards() {
        for (card in cards) {
            if (!card.isMatched) {
                card.isFaceUp = false

            }
        }
    }
    private fun checkForMatch(position1: Int, position2: Int) {
        if (cards[position1].identifier == cards[position2].identifier) {
            eslesmesayisi+=1
            eslesme.text = eslesmesayisi.toString()
            puan +=10
            cards[position1].isMatched = true
            cards[position2].isMatched = true
        }
        else
        {
            puan -=2
        }
        Puan.text =puan.toString()

        val context = this
        var db = DataBase(context)
        if(eslesmesayisi==8)
        {
            var tur = "4x4"
            var p4 = Puan.text.toString()
            var s4 = 60 - (gerisayim.text.toString()).toInt()
            var sk = skor(p4.toInt(),s4.toInt(),tur)
            db.insertData4(sk)
        }
    }
    private fun mesajkutusu()
    {
        val mesaj = AlertDialog.Builder(this)
        if(eslesmesayisi == 8)
        {
            mesaj.setTitle("!!TEBRİKLER!!")
                .setMessage("OYUNU KAZANDINIZ.")
        }
        else
        {
            mesaj.setTitle("!!MAALESEF!!")
                .setMessage("SÜRE DOLDU OYUNU BİTİREMEDİNİZ.")
        }

            mesaj.setPositiveButton("Ana Menüye Dön"){dialogInterface, it ->
                val intent = Intent(this,MainActivity::class.java)
                this@Dortluk.finish()
                startActivity(intent)
            }
            .setNegativeButton("Tekrar Oyna"){dialogInterface, it ->
                val intent = Intent(this,Dortluk::class.java)
                this@Dortluk.finish()
                startActivity(intent)
            }.show()
    }
}
