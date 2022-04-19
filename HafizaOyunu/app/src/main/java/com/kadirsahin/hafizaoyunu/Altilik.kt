package com.kadirsahin.hafizaoyunu

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_altilik.*
import kotlinx.android.synthetic.main.activity_altilik.Puan
import kotlinx.android.synthetic.main.activity_altilik.eslesme
import kotlinx.android.synthetic.main.activity_altilik.fotobtn1
import kotlinx.android.synthetic.main.activity_altilik.fotobtn10
import kotlinx.android.synthetic.main.activity_altilik.fotobtn11
import kotlinx.android.synthetic.main.activity_altilik.fotobtn12
import kotlinx.android.synthetic.main.activity_altilik.fotobtn13
import kotlinx.android.synthetic.main.activity_altilik.fotobtn14
import kotlinx.android.synthetic.main.activity_altilik.fotobtn15
import kotlinx.android.synthetic.main.activity_altilik.fotobtn16
import kotlinx.android.synthetic.main.activity_altilik.fotobtn2
import kotlinx.android.synthetic.main.activity_altilik.fotobtn3
import kotlinx.android.synthetic.main.activity_altilik.fotobtn4
import kotlinx.android.synthetic.main.activity_altilik.fotobtn5
import kotlinx.android.synthetic.main.activity_altilik.fotobtn6
import kotlinx.android.synthetic.main.activity_altilik.fotobtn7
import kotlinx.android.synthetic.main.activity_altilik.fotobtn8
import kotlinx.android.synthetic.main.activity_altilik.fotobtn9
import kotlinx.android.synthetic.main.activity_altilik.gerisayim
import kotlinx.android.synthetic.main.activity_altilik.hamle

class Altilik : AppCompatActivity() {
    private lateinit var buttons: List<ImageButton>
    private lateinit var cards: List<Kartlar>
    private var indexOfSingleSelectedCard: Int? = null
    private var eslesmesayisi =0
    private var hamlesayisi =0
    private var puan =0
    private var zmn=""
    private var knt = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_altilik)
        object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                gerisayim.text=(millisUntilFinished / 1000).toString()
                if(eslesmesayisi == 18)
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
        val images = mutableListOf(R.drawable.aa, R.drawable.bb, R.drawable.cc, R.drawable.dd, R.drawable.ee,R.drawable.ff,R.drawable.gg,R.drawable.hh,
            R.drawable.ii,R.drawable.jj,R.drawable.kk,R.drawable.ll,R.drawable.mm,R.drawable.nn,
            R.drawable.oo, R.drawable.pp, R.drawable.rr,R.drawable.ss)
        images.addAll(images)
        images.shuffle()
        images.shuffle()
        buttons = listOf(fotobtn1,fotobtn2,fotobtn3,fotobtn4,fotobtn5,fotobtn6,fotobtn7,fotobtn8,fotobtn9,
            fotobtn10,fotobtn11,fotobtn12,fotobtn13,fotobtn14,fotobtn15,fotobtn16,fotobtn17,fotobtn18,fotobtn19,fotobtn20,fotobtn22,
            fotobtn23,fotobtn24,fotobtn25,fotobtn26,fotobtn27,fotobtn28,fotobtn29,fotobtn30,fotobtn31,fotobtn32,fotobtn33,fotobtn34,
            fotobtn35,fotobtn36,fotobtn37)
        cards = buttons.indices.map { index ->
            Kartlar(images[index])
        }
        buttons.forEachIndexed { index, button ->
            button.setOnClickListener {

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
    private fun updateModels(position: Int) {
        val card = cards[position]
        // Error checking:
        if (card.isFaceUp) {
            return
        }
        if(!card.isFaceUp)
        {
            hamlesayisi+=1
            hamle.text = hamlesayisi.toString()
        }
        if (indexOfSingleSelectedCard == null) {
            // 0 or 2 selected cards previously
            restoreCards()
            indexOfSingleSelectedCard = position
        } else {
            // exactly 1 card was selected previously
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
        val db = DataBase(context)

        if(eslesmesayisi==18)
        {
            val tur = "6x6"
            val p6 = Puan.text.toString()
            val s6 = 120 - (gerisayim.text.toString()).toInt()
            val sk = skor(p6.toInt(),s6,tur)
            db.insertData4(sk)
        }
    }
    private fun mesajkutusu()
    {
        val mesaj = AlertDialog.Builder(this)
        if(eslesmesayisi == 18)
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
                this@Altilik.finish()
                startActivity(intent)
            }
            .setNegativeButton("Tekrar Oyna"){dialogInterface, it ->
                val intent = Intent(this,Altilik::class.java)
                this@Altilik.finish()
                startActivity(intent)
            }.show()
    }
}
