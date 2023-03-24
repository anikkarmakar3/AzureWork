package com.example.azurework

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity2 : AppCompatActivity() {
    private lateinit var adapter:BlobAdapter
    val newsarray = ArrayList<FileModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        val newsarray = this.intent.extras?.getParcelableArrayList<FileModel>("ArrayData")
        val recyclerview = findViewById<RecyclerView>(R.id.rv_blob_list)
        recyclerview.layoutManager= LinearLayoutManager(this)

        adapter = BlobAdapter(newsarray!!,this)
        recyclerview.adapter= adapter
    }
}