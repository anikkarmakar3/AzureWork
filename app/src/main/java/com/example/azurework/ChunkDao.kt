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
    fun deleteAllChunks()
    @Query("DELETE from ChunkDB where blobName = :blobName")
    fun deleteListData(blobName:String)

    @Query("SELECT * from ChunkDB where blobName = :blobName")
    fun getListData(blobName:String): List<DatabaseChunkModel>?

    @Query("SELECT * from ChunkDB where chunkId = :chunkId limit 1")
    fun getData(chunkId: Long): DatabaseChunkModel?
    @Query("SELECT * from ChunkDB where downloadStatus= :failure")
    fun getFalireList(failure:String) :List<DatabaseChunkModel>
}