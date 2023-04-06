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

}