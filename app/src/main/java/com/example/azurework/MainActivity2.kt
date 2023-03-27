package com.example.azurework

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


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

    fun downloadWithprogressBar(getBlobName:String,fileSize:Long){
        try {
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
                /*Thread.sleep(1000)*/
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
        }
    }




        /*val storageAccount = CloudStorageAccount.parse("DefaultEndpointsProtocol=https;AccountName=anikkarmakar;AccountKey=dN+fVtZihBvdf0QSSUVl68gHjqcVwsHFTsIfelmsEoeatvx8e2FwTwMEk9WLzzhL7seVCyS2zDGU+AStDBf46A==;EndpointSuffix=core.windows.net")

        val blobClient = storageAccount.createCloudBlobClient()

        val container = blobClient.getContainerReference("arc-file-container")

        val blob = container.getBlockBlobReference(getBlobName)

        val localFile = File(Environment.getExternalStorageDirectory().absoluteFile.path )*/
        /*blob.downloadToFile(localFile.absolutePath,null, BlobRequestOptions(),)

        progressBar.setVisibility(View.GONE)*/

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