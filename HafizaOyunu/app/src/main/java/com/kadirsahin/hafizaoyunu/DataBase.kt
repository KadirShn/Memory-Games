package com.kadirsahin.hafizaoyunu

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast

val database_name = "HafizaOyunu2"
val table_name = "Skor"
val col_id4 = "id4"
val col_pun4 = "puan4"
val col_sure4 = "sure4"
val col_tur = "tur"

class DataBase(var context: Context):SQLiteOpenHelper(context, database_name,null,1)
{
    override fun onCreate(p0: SQLiteDatabase?)
    {
        var createTable = " CREATE TABLE " + table_name+ "(" +
                col_id4 +" INTEGER PRIMARY KEY AUTOINCREMENT,"+
                col_pun4 +" INTEGER,"+
                col_sure4 +" INTEGER,"+
                col_tur +" VARCHAR(20))"
                p0?.execSQL(createTable)
    }

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int)
    {

    }

    fun insertData4(skr: skor)
    {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(col_pun4,skr.pun)
        cv.put(col_sure4,skr.sure)
        cv.put(col_tur,skr.tur)
        val sonuc = db.insert(table_name,null,cv)
        if (sonuc == (-1).toLong())
        {
            Toast.makeText(context, "Hatalı", Toast.LENGTH_SHORT).show()
        }
        else
        {
            Toast.makeText(context, "Başarılı", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("Range")
    fun readData4(): MutableList<skor> {
        var liste:MutableList<skor> = ArrayList()
        val db = this.readableDatabase
        var sorgu = "Select * From " + table_name + " Where Tur= '4x4' ORDER BY " + col_pun4 + " DESC "
        var sonuc = db.rawQuery(sorgu,null)
        if(sonuc.moveToFirst())
        {
            do {
                var sk = skor()
                sk.id4 = sonuc.getString(sonuc.getColumnIndex(col_id4)).toInt()
                sk.pun = sonuc.getString(sonuc.getColumnIndex(col_pun4)).toInt()
                sk.sure = sonuc.getString(sonuc.getColumnIndex(col_sure4)).toInt()
                sk.tur = sonuc.getString(sonuc.getColumnIndex(col_tur))
                liste.add(sk)
            }
            while (sonuc.moveToNext())
        }
        sonuc.close()
        db.close()
        return liste
    }

    @SuppressLint("Range")
    fun readData6(): MutableList<skor> {
        var liste2:MutableList<skor> = ArrayList()
        val db = this.readableDatabase
        var sorgu2 = "Select * From " + table_name + " Where Tur= '6x6' ORDER BY " + col_pun4 + " DESC "
        var sonuc2 = db.rawQuery(sorgu2,null)
        if(sonuc2.moveToFirst())
        {
            do {
                var sk = skor()
                sk.id4 = sonuc2.getString(sonuc2.getColumnIndex(col_id4)).toInt()
                sk.pun = sonuc2.getString(sonuc2.getColumnIndex(col_pun4)).toInt()
                sk.sure = sonuc2.getString(sonuc2.getColumnIndex(col_sure4)).toInt()
                sk.tur = sonuc2.getString(sonuc2.getColumnIndex(col_tur))
                liste2.add(sk)
            }
            while (sonuc2.moveToNext())
        }
        sonuc2.close()
        db.close()
        return liste2
    }

    @SuppressLint("Range")
    fun max4(): MutableList<skor> {
        var liste3:MutableList<skor> = ArrayList()
        val db = this.readableDatabase
        var sorgu3 = "Select * From " + table_name + " Where Tur= '4x4' ORDER BY " + col_pun4 + " DESC LIMIT 1 "
        var sonuc3 = db.rawQuery(sorgu3,null)
        if(sonuc3.moveToFirst())
        {
            do {
                var sk = skor()
                sk.id4 = sonuc3.getString(sonuc3.getColumnIndex(col_id4)).toInt()
                sk.pun = sonuc3.getString(sonuc3.getColumnIndex(col_pun4)).toInt()
                sk.sure = sonuc3.getString(sonuc3.getColumnIndex(col_sure4)).toInt()
                sk.tur = sonuc3.getString(sonuc3.getColumnIndex(col_tur))
                liste3.add(sk)
            }
            while (sonuc3.moveToNext())
        }
        sonuc3.close()
        db.close()
        return liste3
    }
    @SuppressLint("Range")
    fun max6(): MutableList<skor> {
        var liste4:MutableList<skor> = ArrayList()
        val db = this.readableDatabase
        var sorgu4 = "Select * From " + table_name + " Where Tur= '6x6' ORDER BY " + col_pun4 + " DESC LIMIT 1 "
        var sonuc4 = db.rawQuery(sorgu4,null)
        if(sonuc4.moveToFirst())
        {
            do {
                var sk = skor()
                sk.id4 = sonuc4.getString(sonuc4.getColumnIndex(col_id4)).toInt()
                sk.pun = sonuc4.getString(sonuc4.getColumnIndex(col_pun4)).toInt()
                sk.sure = sonuc4.getString(sonuc4.getColumnIndex(col_sure4)).toInt()
                sk.tur = sonuc4.getString(sonuc4.getColumnIndex(col_tur))
                liste4.add(sk)
            }
            while (sonuc4.moveToNext())
        }
        sonuc4.close()
        db.close()
        return liste4
    }
}