package com.example.azurework

import android.content.Context

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.blob.CloudBlob
import com.microsoft.azure.storage.blob.ListBlobItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.FileOutputStream

class BlobAdapter(val items : List<FileModel>, val context: Context) : RecyclerView.Adapter<MyViewHolder>() {
    private var mainActivityObject= MainActivity()
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
                mainActivityObject.downloadF(blobItem.blobName.toString())

                Toast.makeText(context,"success fully download ${blobItem.blobName}",Toast.LENGTH_LONG).show()
            }catch (e:Exception){
                e.printStackTrace()
                Toast.makeText(context,"download failed ${blobItem.blobName}",Toast.LENGTH_LONG).show()
            }

        }
    }

}

class MyViewHolder (view: View) : RecyclerView.ViewHolder(view) {
    val blobItemView:TextView= view.findViewById<TextView>(R.id.blob_list_item)
}