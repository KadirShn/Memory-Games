package com.kadirsahin.hafizaoyunu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_skorlar.*
import javax.net.ssl.SSLSessionBindingEvent

class Skorlar : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skorlar)

        val context = this
        var db = DataBase(context)
        var data = db.readData4()
        var data2 = db.readData6()
        txt4x4.text = ""
        txt6x6.text = ""
        baslik1.text="\tID\t\t\t\t\t\t\t\t\t\tPUAN\t\t\t\t\t\t\t\t\t\t\t\tSüRE\t\t\t\t\t\t\t\t\t\tZORLUK"
        baslik2.text="\tID\t\t\t\t\t\t\t\t\t\tPUAN\t\t\t\t\t\t\t\t\t\t\t\tSüRE\t\t\t\t\t\t\t\t\t\tZORLUK"
        for (i in 0 until data.size)
        {
            txt4x4.append("\t"+data.get(i).id4.toString()+ "\t\t\t\t\t\t\t\t\t\t\t"
                    +data.get(i).pun+ "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
                    +data.get(i).sure+ "\t\t\t\t\t\t\t\t\t\t\t\t\t"
                    +data.get(i).tur+ "\n" )


        }
        for(j in 0 until data2.size)
        {
            txt6x6.append("\t"+data.get(j).id4.toString()+ "\t\t\t\t\t\t\t\t\t\t\t"
                    +data2.get(j).pun+ "\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t"
                    +data2.get(j).sure+ "\t\t\t\t\t\t\t\t\t\t\t\t\t"
                    +data2.get(j).tur+ "\n" )
        }

    }
}