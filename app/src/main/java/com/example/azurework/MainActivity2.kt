package com.example.azurework

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.util.Range
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.toClosedRange
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.blob.CloudBlockBlob
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.io.RandomAccessFile
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class MainActivity2 : AppCompatActivity() {
    private lateinit var adapter: BlobAdapter
    val newsarray = ArrayList<FileModel>()
    lateinit var progressBar: ProgressBar
    private val progressBarStatus = 0
    private val fileSize: Long = 0
    private var filesAbsolteDir: File? = null
    lateinit var mContext: Context
    lateinit var filesDirs: File


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)
        mContext = this.applicationContext

        filesDirs = mContext.getFilesDir()
        val recyclerview = findViewById<RecyclerView>(R.id.rv_blob_list)
        progressBar = findViewById(R.id.progressBar2)
        progressBar.visibility = View.GONE

        val newsarray = this.intent.extras?.getParcelableArrayList<FileModel>("ArrayData")

        recyclerview.layoutManager = LinearLayoutManager(this)
        adapter = BlobAdapter(newsarray!!, this)
        recyclerview.adapter = adapter


    }

    fun getContext(): Context {
        var contextt = this@MainActivity2
        return contextt
    }

    suspend fun downloadBlobFile(url: String, getBlobName: String, chunkSize: Int) {
        try {

            val storageConnectionString =
                "DefaultEndpointsProtocol=https;AccountName=anikkarmakar;AccountKey=dN+fVtZihBvdf0QSSUVl68gHjqcVwsHFTsIfelmsEoeatvx8e2FwTwMEk9WLzzhL7seVCyS2zDGU+AStDBf46A==;EndpointSuffix=core.windows.net"
            val account = CloudStorageAccount.parse(storageConnectionString)
            val blobClient = account.createCloudBlobClient()
            val container = blobClient.getContainerReference("arc-file-container")
            val blob: CloudBlockBlob = container.getBlockBlobReference(getBlobName)

            var inputStream: InputStream = blob.openInputStream()

            val file = File(
                Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download",
                getBlobName
            )
            val outputStream = FileOutputStream(file)

            val buffer = ByteArray(chunkSize)
            var bytesRead: Int
            var count = 1
            do {
                count++
                bytesRead = inputStream.read(buffer)
                var remainPercentage = 1
                if (bytesRead > 0) {

                    outputStream.write(buffer, 0, bytesRead)
                    /*remainPercentage= (blob.properties.length-chunkSize).toInt()*/
                }
                var percentage: Float =
                    ((chunkSize * count).toFloat() / blob.properties.length.toFloat()) * 100
                /*Log.d("data","the read data $remainByte and remaing percentage is $remainPercentage")*/


                Log.d("data", "the percentage is  ${percentage} \n")
            } while (bytesRead != -1)
            Log.d("data", "the count is  $count")
            outputStream.close()
            inputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    suspend fun downloadFile(getBlobName: String, fileSize: Int, chunkSize: Int, context: Context) {
        try {
            val storageConnectionString =
                "DefaultEndpointsProtocol=https;AccountName=anikkarmakar;AccountKey=dN+fVtZihBvdf0QSSUVl68gHjqcVwsHFTsIfelmsEoeatvx8e2FwTwMEk9WLzzhL7seVCyS2zDGU+AStDBf46A==;EndpointSuffix=core.windows.net"
            val account = CloudStorageAccount.parse(storageConnectionString)
            val blobClient = account.createCloudBlobClient()
            val container = blobClient.getContainerReference("arc-file-container")
            val blob: CloudBlockBlob = container.getBlockBlobReference(getBlobName)

            /*val totalSize = fileSize
            val numOfThreads = (totalSize / chunkSize) + 1   //+1
            val getChunkBlob = multiPart(
                getBlobName,
                numOfThreads,
                chunkSize,
                context,
                totalSize,
                blob
            )*/

            val ranges = calculateRange(fileSize.toLong(), chunkSize)
            multiPart(ranges, getBlobName, context, blob)
            /*val isAllChunkDownload = getChunkBlob.filter {
                it == true
            }.count()==0
            if (isAllChunkDownload) {
                copyTemporaryFileToDestinationFile(getBlobName,context)
            }*/

            /*for(i in getChunkBlob.)
            for(element in getChunkBlob){
               Log.d("data","$element")
            }*/
        } catch (e: Exception) {
            e.printStackTrace()
        }
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


    /*fun stichFile(chunkSize:Int){
        val chunks = ByteArray(chunkSize)
        val filename = "myfile.txt"
        val fos = openFileOutput(filename, Context.MODE_PRIVATE)
        for (chunk in chunks) {
            fos.write(chunk.toInt())
        }
    }*/
    fun getContext(context: Context) {
        filesAbsolteDir = context.filesDir
    }

    /*
        suspend fun multiPart(
            blobName: String,
            noOfThreads: Int,
            chunkSize: Int,
            context: Context,
            totalSize: Int,
            blob: CloudBlockBlob
        ): List<Boolean> {

            val arrJob = ArrayList<Deferred<Boolean>>()
            for (i in 1..noOfThreads) {
                val job = CoroutineScope(Dispatchers.Default).async {
                    val inputStream: InputStream = blob.openInputStream()
                    var outputStream: OutputStream? = null
                    try {
                        val startPos = (i - 1) * chunkSize
                        val endPos = if (i == noOfThreads) totalSize else startPos + chunkSize - 1

                        Log.e("anik", "$i  >>>>>  $startPos and $endPos")
                        val outPutFilPath =
                            context.filesDir.absolutePath + blobName + File.separator + "chunk_$i.jpg"
                        val outputFile = File(outPutFilPath)

                        if (outputFile.parentFile?.exists() == false) {
                            outputFile.parentFile?.mkdirs()
                        }
                        if (!outputFile.exists()) {
                            outputFile.createNewFile()
                        }
                        outputStream = outputFile.outputStream()

                        val chunk = endPos - startPos + 1
                        val buffer = ByteArray(chunk)

    //                    var bytesRead: Int
                        var count: Int
                        var currentPosition = startPos

                        while (inputStream.read(buffer, 0, chunk)
                                .also { count = it } != -1 && currentPosition <= endPos
                        ) {
                            outputStream.write(buffer, 0, count)
                            currentPosition += count
                        }
    //                    while (currentPosition <= endPos) {
    //                        bytesRead = inputStream.read(buffer)
    //                        if (bytesRead == -1) break
    //                        outputStream.write(buffer, 0, bytesRead)
    //                        currentPosition += bytesRead
    //                    }
                        writeToFile(startPos.toLong(), outputFile, outPutFilPath, blobName, context)
                        true
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    } finally {
                        inputStream.close()
                        outputStream?.close()

                    }
                }
                arrJob.add(job)
            }
            return arrJob.awaitAll()
        }*/
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
        try {
            blob.downloadRange(closedRange.start, chunkFileSize, outputStream)
            isSuccess = true
        } catch (exception: Exception) {
            isSuccess = false
            exception.printStackTrace()
        } finally {
            outputStream.close()
        }

        writeToFile(closedRange.start, outputFile, outPutFilPath, blobName, context)

        isSuccess
    }

/*fun getUrl(blobName: String): String  {
    val storageConnectionString =
        "DefaultEndpointsProtocol=https;AccountName=anikkarmakar;AccountKey=dN+fVtZihBvdf0QSSUVl68gHjqcVwsHFTsIfelmsEoeatvx8e2FwTwMEk9WLzzhL7seVCyS2zDGU+AStDBf46A==;EndpointSuffix=core.windows.net"
    val account = CloudStorageAccount.parse(storageConnectionString)
    val blobClient = account.createCloudBlobClient()
    val container = blobClient.getContainerReference("arc-file-container")
    val blockBlob: CloudBlockBlob = container.getBlockBlobReference(blobName)
    val policy = SharedAccessBlobPolicy()
    policy.sharedAccessExpiryTime= Date(System.currentTimeMillis()+(60*1000*60))
    policy.permissions=EnumSet.of((SharedAccessBlobPermissions.READ))
    val sharedAccessBlobHeaders= SharedAccessBlobHeaders()
    sharedAccessBlobHeaders.contentDisposition="attachment;filename=\"$blobName\""
    val sasToken=blockBlob.generateSharedAccessSignature(policy,sharedAccessBlobHeaders,null)
    return blockBlob.uri.toString() + sasToken
}*/

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
        val destinationPath =
            Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download"
        val sourcePath = configureTemporaryFilePath(blobName, context)
        if (File(sourcePath).exists()) {
            File(sourcePath).copyTo(File(destinationPath, blobName), true)
            File(sourcePath).delete()
        }
    }
}


