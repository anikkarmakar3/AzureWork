package com.example.azurework

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [DatabaseChunkModel::class], version = 1)
abstract class Chunkdatabase : RoomDatabase(){

    abstract fun chunkDao(): ChunkDao

    companion object {

        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: Chunkdatabase? = null

        fun getDatabase(context: Context): Chunkdatabase {
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    Chunkdatabase::class.java,
                    "chunk_database"
                ).build()
                INSTANCE = instance
                // return instance
                instance
            }
        }
    }

}
