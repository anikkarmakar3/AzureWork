package com.example.azurework

import android.os.Bundle
import android.os.Environment
import android.os.FileUtils.ProgressListener
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.blob.BlobRequestOptions
import java.io.File


class MainActivity2 : AppCompatActivity() {
    private lateinit var adapter:BlobAdapter
    val newsarray = ArrayList<FileModel>()
    lateinit var progressBar: ProgressBar
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        progressBar= findViewById(R.id.progressBar2)
        progressBar.visibility= View.VISIBLE
        val newsarray = this.intent.extras?.getParcelableArrayList<FileModel>("ArrayData")
        val recyclerview = findViewById<RecyclerView>(R.id.rv_blob_list)
        recyclerview.layoutManager= LinearLayoutManager(this)

        adapter = BlobAdapter(newsarray!!,this)
        recyclerview.adapter= adapter
        adapter.notifyDataSetChanged()
    }

    fun downloadWithprogressBar(getBlobName:String){
        val storageAccount = CloudStorageAccount.parse("DefaultEndpointsProtocol=https;AccountName=anikkarmakar;AccountKey=dN+fVtZihBvdf0QSSUVl68gHjqcVwsHFTsIfelmsEoeatvx8e2FwTwMEk9WLzzhL7seVCyS2zDGU+AStDBf46A==;EndpointSuffix=core.windows.net")

        val blobClient = storageAccount.createCloudBlobClient()

        val container = blobClient.getContainerReference("arc-file-container")

        val blob = container.getBlockBlobReference(getBlobName)

        val localFile = File(Environment.getExternalStorageDirectory().absoluteFile.path )
        /*blob.downloadToFile(localFile.absolutePath,null, BlobRequestOptions(),)

        progressBar.setVisibility(View.GONE)*/
    }

    fun test(){

    }
}