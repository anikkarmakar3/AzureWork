package com.example.azurework

import android.app.ProgressDialog
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.blob.CloudBlob
import com.microsoft.azure.storage.blob.CloudBlockBlob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.net.URL


class MainActivity2 : AppCompatActivity() {
    private lateinit var adapter:BlobAdapter
    val newsarray = ArrayList<FileModel>()
    lateinit var progressBar: ProgressDialog
    private val progressBarStatus = 0
    private val fileSize: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        /*progressBar= findViewById(R.id.progressBar2)
        progressBar.visibility= View.VISIBLE*/

        val newsarray = this.intent.extras?.getParcelableArrayList<FileModel>("ArrayData")
        val recyclerview = findViewById<RecyclerView>(R.id.rv_blob_list)
        recyclerview.layoutManager= LinearLayoutManager(this)
        adapter = BlobAdapter(newsarray!!,this)
        recyclerview.adapter= adapter
        adapter.notifyDataSetChanged()


    }

    fun downloadIntoMultipart(getBlobName:String,fileSize:Long){
        CoroutineScope(Dispatchers.IO).launch {
            val storageConnectionString =
                "DefaultEndpointsProtocol=https;AccountName=anikkarmakar;AccountKey=dN+fVtZihBvdf0QSSUVl68gHjqcVwsHFTsIfelmsEoeatvx8e2FwTwMEk9WLzzhL7seVCyS2zDGU+AStDBf46A==;EndpointSuffix=core.windows.net"
            val account = CloudStorageAccount.parse(storageConnectionString)
            val blobClient = account.createCloudBlobClient()
            val container = blobClient.getContainerReference("arc-file-container")
            val blob: CloudBlockBlob  = container.getBlockBlobReference(getBlobName)
            val outputStream: OutputStream = FileOutputStream(File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download", getBlobName))
            blob.downloadRange(0,fileSize,outputStream,null,null,null)
            Log.d("length1","the length is ${fileSize}")
            outputStream.close();
        }

    }


    fun downloadBlobFile(url: URL, getBlobName:String, chunkSize: Int) {
        CoroutineScope(Dispatchers.IO).launch{
            try{
                val storageConnectionString =
                    "DefaultEndpointsProtocol=https;AccountName=anikkarmakar;AccountKey=dN+fVtZihBvdf0QSSUVl68gHjqcVwsHFTsIfelmsEoeatvx8e2FwTwMEk9WLzzhL7seVCyS2zDGU+AStDBf46A==;EndpointSuffix=core.windows.net"
                val account = CloudStorageAccount.parse(storageConnectionString)
                val blobClient = account.createCloudBlobClient()
                val container = blobClient.getContainerReference("arc-file-container")
                val blob: CloudBlockBlob  = container.getBlockBlobReference(getBlobName)

                var inputStream: InputStream = blob.openInputStream()

                val file = File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download", getBlobName)
                val outputStream = FileOutputStream(file)

                val buffer = ByteArray(chunkSize)
                var bytesRead: Int
                var count=0
                do {
                    bytesRead = inputStream.read(buffer)
                    if (bytesRead > 0) {
                        outputStream.write(buffer, 0, bytesRead)
                    }
                    count++
                } while (bytesRead != -1)
                Log.d("data","the read data $bytesRead and count is $count")
                outputStream.close()
                inputStream.close()
            }catch (e:Exception){
                e.printStackTrace()
            }

        }

    }

    fun downloadWithprogressBar(getBlobName:String,fileSize:Long) {
        /*try {
            progressBar = ProgressDialog(this@MainActivity2)
            progressBar.setCancelable(true)
            progressBar.setMessage("File downloading ...")
            progressBar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            progressBar.progress = 0
            progressBar.max = 100
            progressBar.show()

        }catch (e:Exception){
            e.printStackTrace()
        }


        while (progressBarStatus<100){
            val progressBarStatus = doOperation(fileSize.toInt())

            try {
                *//*Thread.sleep(1000)*//*
                progressBar.setProgress(progressBarStatus);
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            if (progressBarStatus >= 100) {
                // sleeping for 1 second after operation completed
                try {
                    Thread.sleep(1000);
                } catch (e:InterruptedException) {
                    e.printStackTrace();
                }
                // close the progress bar dialog
                progressBar.dismiss();
            }
        }*/


    }

    fun doOperation(fileSize:Int):Int{
        while (fileSize <= 10000) {
            fileSize+1
            if (fileSize == 1000) {
                return 10
            } else if (fileSize == 2000) {
                return 20
            } else if (fileSize == 3000) {
                return 30
            } else if (fileSize == 4000) {
                return 40 // you can add more else if
            }
            /* else {
                   return 100;
               }*/

        }//end of while
        return 100
    }
}