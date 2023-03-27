package com.example.azurework

import android.app.ProgressDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class BlobAdapter(val items : List<FileModel>, val context: Context) : RecyclerView.Adapter<MyViewHolder>() {
    private var mainActivityObject= MainActivity()
    private var mainActivityObject2= MainActivity2()
    lateinit var progressBar: ProgressDialog
    private val progressBarStatus = 0
    val connectionString =
        "DefaultEndpointsProtocol=https;AccountName=anikkarmakar;AccountKey=dN+fVtZihBvdf0QSSUVl68gHjqcVwsHFTsIfelmsEoeatvx8e2FwTwMEk9WLzzhL7seVCyS2zDGU+AStDBf46A==;EndpointSuffix=core.windows.net"


    // Gets the number of animals in the list
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view=LayoutInflater.from(parent.context).inflate(R.layout.blob_item,parent,false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val blobItem = items[position]
        holder.blobItemView.text = blobItem.blobName

        holder.blobItemView.setOnClickListener {

            try {
                /*mainActivityObject.downloadF(blobItem.blobName.toString())*/
                downloadWithprogressBar(blobItem.blobName.toString(),blobItem.blobSize.toLong())
                Toast.makeText(context,"success fully download ${blobItem.blobName}",Toast.LENGTH_LONG).show()
            }catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(context,"download failed ${blobItem.blobName}",Toast.LENGTH_LONG).show()
            }

        }
    }




    fun downloadWithprogressBar(getBlobName:String,fileSize:Long){
        try {
            progressBar = ProgressDialog(context)
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
                Thread.sleep(1000)
                progressBar.setProgress(progressBarStatus);
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            if (progressBarStatus >= 100000) {
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

    fun doOperation(fileSize:Int):Int{

        var i=0
        while ( i < fileSize) {
            i++
            /*if (fileSize == 1000) {
                return 10
            } else if (fileSize == 2000) {
                return 20
            } else if (fileSize == 3000) {
                return 30
            } else if (fileSize == 4000) {
                return 40 // you can add more else if
            }
             else {
                   return 100;
               }*/
        }//end of while
        return i
    }

}


class MyViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val blobItemView:TextView= view.findViewById<TextView>(R.id.blob_list_item)
}