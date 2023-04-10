package com.example.azurework

import android.app.Application
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext

class ChunksController(val context: Context) {

    suspend fun insertData(chunkDatabaseChunkModel: DatabaseChunkModel) {
        context?.let { Chunkdatabase.getDatabase(context).chunkDao().insert(chunkDatabaseChunkModel) }
    }

    suspend fun updateData(chunkDatabaseChunkModel: DatabaseChunkModel) {
        context?.let { Chunkdatabase.getDatabase(context).chunkDao().update(chunkDatabaseChunkModel) }
    }

    suspend fun deleteListData(blobName:String) {
        context?.let {Chunkdatabase.getDatabase(context).chunkDao().deleteListData(blobName) }
    }

    suspend fun getListData(blobName: String): List<DatabaseChunkModel> {

        return Chunkdatabase.getDatabase(context).chunkDao().getListData(blobName)!!
    }

    suspend fun getChunkData(chunkId:Long){
        context?.let {Chunkdatabase.getDatabase(context).chunkDao().getData(chunkId) }
    }

    suspend fun getAllFaliureChunkData(failure:String):List<DatabaseChunkModel>{
        return Chunkdatabase.getDatabase(context).chunkDao().getFalireList(failure)
    }

}