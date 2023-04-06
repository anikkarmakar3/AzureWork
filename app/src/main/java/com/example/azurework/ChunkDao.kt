package com.example.azurework

import androidx.room.*

@Dao
interface ChunkDao {

    @Insert
    fun insert(chunkData: DatabaseChunkModel)

    @Update
    fun update(chunkData: DatabaseChunkModel)

    @Delete
    fun delete(chunkData: DatabaseChunkModel)

    @Query("delete from ChunkDB")
    fun deleteAllNotes()

    /*@Query("select * from ChunkDB order by priority desc")
    fun getAllNotes(): LiveData<List<Note>>*/

}