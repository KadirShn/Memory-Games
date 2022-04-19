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
        textView6.text = ""
        textView8.text = ""
        textView10.text = ""
        textView17.text = ""
        textView18.text = ""
        textView19.text = ""
        for (i in 0 until data.size)
        {
            textView6.append(data.get(i).tur+ "\n")
            textView8.append(data.get(i).pun.toString()+ "\n")
            textView10.append(data.get(i).sure.toString()+ "\n")
        }
        for(j in 0 until data2.size)
        {
            textView17.append(data2.get(j).tur+ "\n")
            textView18.append(data2.get(j).pun.toString()+ "\n")
            textView19.append(data2.get(j).sure.toString()+ "\n")
        }

    }
}