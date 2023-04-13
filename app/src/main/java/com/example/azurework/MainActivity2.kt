package com.example.azurework

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Range
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.toClosedRange
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.blob.CloudBlockBlob
import kotlinx.coroutines.*
import java.io.File
import java.io.RandomAccessFile
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity2 : AppCompatActivity() {
    private lateinit var adapter: BlobAdapter
    val newsarray = ArrayList<FileModel>()
    private val progressBarStatus = 0
    private val fileSize: Long = 0
    private var filesAbsolteDir: File? = null
    lateinit var mContext: Context
    lateinit var filesDirs: File
    private var chunksController: ChunksController? = null
    private var chunks: HashMap<Long, DatabaseChunkModel> = HashMap()
    private var myChunkDatabase: Chunkdatabase? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        mContext = this.applicationContext
        filesDirs = mContext.getFilesDir()
        val recyclerview = findViewById<RecyclerView>(R.id.rv_blob_list)
        chunksController = ChunksController(mContext)
        val newsarray = this.intent.extras?.getParcelableArrayList<FileModel>("ArrayData")

        recyclerview.layoutManager = LinearLayoutManager(this)
        adapter = BlobAdapter(newsarray!!, this)
        recyclerview.adapter = adapter


    }

    suspend fun downloadFile(getBlobName: String, fileSize: Int, chunkSize: Int, context: Context) {
        try {
            val storageConnectionString =
                "DefaultEndpointsProtocol=https;AccountName=anikkarmakar;AccountKey=dN+fVtZihBvdf0QSSUVl68gHjqcVwsHFTsIfelmsEoeatvx8e2FwTwMEk9WLzzhL7seVCyS2zDGU+AStDBf46A==;EndpointSuffix=core.windows.net"
            val account = CloudStorageAccount.parse(storageConnectionString)
            val blobClient = account.createCloudBlobClient()
            val container = blobClient.getContainerReference("arc-file-container")
            val blob: CloudBlockBlob = container.getBlockBlobReference(getBlobName)

            val existData = fetchAllChunks(getBlobName, context)

            if (!existData.isNullOrEmpty()) {
                val failureChunk = existData?.filter {
                    it.downloadStatus == DownloadStatus.FAILURE.toString()
                }
                val faliureRange = calculateFaliureRange(failureChunk!!)
                val chunksMultipart = multiPart(faliureRange, getBlobName, context, blob)
                val isResumeDownloadComplete = chunksMultipart.filter {
                    it == false
                }.count() == 0

                if (isResumeDownloadComplete) {
                    deleteAllChunksFromDB(getBlobName, context)
                    copyTemporaryFileToDestinationFile(getBlobName, context)
                }
            } else {

                val ranges = calculateRange(fileSize.toLong(), chunkSize)

                val chunksMultipart = multiPart(ranges, getBlobName, context, blob)

                val isDownloadComplete = chunksMultipart.filter {
                    it == false
                }.count() == 0


                if (isDownloadComplete) {
                    deleteAllChunksFromDB(getBlobName, context)
                    copyTemporaryFileToDestinationFile(getBlobName, context)
                } else {
                    Toast.makeText(applicationContext, "File download failed", Toast.LENGTH_LONG)
                        .show()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun calculateFaliureRange(failureChunk: List<DatabaseChunkModel>): ArrayList<ClosedRange<Long>> {
        val ranges: ArrayList<ClosedRange<Long>> = ArrayList()
        failureChunk.forEach {
            val range = Range(it.lowerRange, it.upperRange)
            ranges.add(range.toClosedRange())
        }
        return ranges
    }

    private suspend fun multiPart(
        ranges: ArrayList<ClosedRange<Long>>,
        blobName: String,
        context: Context,
        blob: CloudBlockBlob
    ): List<Boolean> {
        val arrList = ArrayList<Deferred<Boolean>>()
        for (i in ranges) {
            val job = downloadChunkFile(i, blob, blobName, context)
            arrList.add(job)
        }
        return arrList.awaitAll()
    }

    private suspend fun calculateRange(fileSize: Long, size: Int): ArrayList<ClosedRange<Long>> {
        val chunkSize = size * 1000 * 1000
        var ranges: ArrayList<ClosedRange<Long>> = ArrayList()
        val numberOfChunks = fileSize / chunkSize
        var tempSize: Long = chunkSize.toLong()
        var lower = 0L
// we need to add extra byte in last request itself
        var numberOfChunksGenerated = 1

        while (numberOfChunksGenerated < numberOfChunks) {
            val range = Range(lower, tempSize)
            ranges.add(range.toClosedRange())
            lower = tempSize + 1
            tempSize += chunkSize
            numberOfChunksGenerated += 1
        }

        val lastClosedRange = Range(lower, fileSize)
        ranges.add(lastClosedRange.toClosedRange())
        return ranges
    }

    private fun downloadChunkFile(
        closedRange: ClosedRange<Long>,
        blob: CloudBlockBlob,
        blobName: String,
        context: Context
    ) = CoroutineScope(Dispatchers.IO).async {

        var isSuccess = false
        val chunkId = System.nanoTime()
        val outPutFilPath =
            context.filesDir.absolutePath + blobName + File.separator + "$chunkId.temp"
        val outputFile = File(outPutFilPath)

        if (outputFile.parentFile?.exists() == false) {
            outputFile.parentFile?.mkdirs()
        }
        if (!outputFile.exists()) {
            outputFile.createNewFile()
        }

        val chunkFileSize = closedRange.endInclusive - closedRange.start
        val outputStream = outputFile.outputStream()
        val chunkModel = DatabaseChunkModel(
            chunkId = chunkId,
            filePath = outPutFilPath,
            downloadStatus = DownloadStatus.DOWNLOAD_PENDING.toString(),
            blobName = blobName,
            lowerRange = closedRange.start,
            upperRange = closedRange.endInclusive,
            size = chunkFileSize
        )
        CoroutineScope(Dispatchers.IO).launch {
            ChunksController(context)?.insertData(chunkModel)
        }
        try {
            blob.downloadRange(closedRange.start, chunkFileSize, outputStream)
            isSuccess = true

        } catch (exception: Exception) {
            isSuccess = false
            exception.printStackTrace()
        } finally {
            outputStream.close()
        }

        chunks[chunkId] = chunkModel
        writeToFile(closedRange.start, outputFile, outPutFilPath, blobName, context)
        if (isSuccess) {
            update(chunkId, DownloadStatus.SUCCESS.toString(), context)
        } else {
            update(chunkId, DownloadStatus.FAILURE.toString(), context)
        }
        isSuccess
    }

    private suspend fun update(chunkId: Long, downloadStatus: String, context: Context) {
        chunks[chunkId]?.let {
            it.downloadStatus = downloadStatus
            ChunksController(context)?.updateData(it)
        }
    }

    private suspend fun fetchAllChunks(
        blobName: String,
        context: Context
    ): List<DatabaseChunkModel>? {
        val listOfChunks = ChunksController(context)?.getListData(blobName)
        chunks = listOfChunks?.let {
            it.associateBy {
                it.chunkId
            } as HashMap<Long, DatabaseChunkModel>
        } ?: kotlin.run {
            HashMap()
        }
        return listOfChunks
    }

    private suspend fun fetchAllFailureChunks(
        faliure: String,
        context: Context
    ): List<DatabaseChunkModel> {
        val listOfFaliureChunks = ChunksController(context).getAllFaliureChunkData(faliure)
        return listOfFaliureChunks
    }

    private suspend fun deleteAllChunksFromDB(blobName: String, context: Context) {
        chunks.clear()
        ChunksController(context)?.deleteListData(blobName)
    }


    private suspend fun writeToFile(
        startPos: Long,
        chunkFile: File,
        outPutFilPath: String,
        blobName: String,
        context: Context
    ) {
        withContext(Dispatchers.IO) {
            try {
                val tempFilePath = configureTemporaryFilePath(blobName, context)
                createTemporaryFile(tempFilePath, context)
                val randomAccessFile = RandomAccessFile(tempFilePath, "rw")
                randomAccessFile.seek(startPos)
                Log.d("check", "$startPos and ${chunkFile.readBytes()}")
                randomAccessFile.write(chunkFile.readBytes())
                randomAccessFile.close()
                File(outPutFilPath).delete()

            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun configureTemporaryFilePath(blobName: String, context: Context): String {
        /*val fileNameWithoutExtension = filePath.substring(0, filePath.lastIndexOf("."))*/
        return context.filesDir.absolutePath + blobName + File.separator + "$blobName"
    }

    private fun createTemporaryFile(tempFilePath: String, context: Context) {
        val downloadedFile = File(tempFilePath)
        if (downloadedFile.parentFile?.exists() == false) {
            downloadedFile.parentFile?.mkdirs()
        }
        if (!downloadedFile.exists()) {
            downloadedFile.createNewFile()
        }
    }

    private fun copyTemporaryFileToDestinationFile(blobName: String, context: Context) {
        try {
            val destinationPath =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            val sourcePath = configureTemporaryFilePath(blobName, context)
            if (File(sourcePath).exists()) {
                File(sourcePath).copyTo(File(destinationPath, blobName), true)
                File(sourcePath).delete()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}


