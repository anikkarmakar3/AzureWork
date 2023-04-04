package com.example.azurework

import android.content.Context
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.blob.CloudBlobContainer
import com.microsoft.azure.storage.blob.CloudBlockBlob
import com.microsoft.azure.storage.blob.SharedAccessBlobHeaders
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.RandomAccessFile
import java.util.*
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

            val totalSize = fileSize
            var inputStream: InputStream = blob.openInputStream()
            val numOfThreads = (totalSize / chunkSize) + 1   //+1
            val getChunkBlob = multiPart(
                getBlobName,
                numOfThreads,
                chunkSize,
                inputStream,
                context,
                totalSize,
                blob
            )
            inputStream.close()
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


    suspend fun multiPart(
        blobName: String,
        noOfThreads: Int,
        chunkSize: Int,
        inputStream: InputStream,
        context: Context,
        totalSize: Int,
        blob: CloudBlockBlob
    ): List<Boolean> {

        val arrJob = ArrayList<Deferred<Boolean>>()
        for (i in 1..noOfThreads) {
            val job = CoroutineScope(Dispatchers.Default).async {
                try {
                    val startPos = 0 * chunkSize
                    val endPos = if (i == noOfThreads) totalSize else startPos + chunkSize - 1

                    Log.e("anik", "$i  >>>>>  $startPos and $endPos")
                    val outPutFilPath =
                        context.filesDir.absolutePath + blobName + File.separator + "chunk_$i.temp"
                    val outputFile = File(outPutFilPath)

                    if (outputFile.parentFile?.exists() == false) {
                        outputFile.parentFile?.mkdirs()
                    }
                    if (!outputFile.exists()) {
                        outputFile.createNewFile()
                    }
                    val outputStream = outputFile.outputStream()

                    /*val data = ByteArray(1024)
                    var total: Long = 0
                    var count: Int
                    while (inputStream.read(data).also { count = it } != -1) {
                        total += count.toLong()

                        outputStream.write(data, 0, count)
                    }*/
                    // Create a buffer to read data from the input stream.
                    val buffer = ByteArray(endPos)

                    // Use the input stream to read the data for the corresponding chunk
                    // and write it to the corresponding output stream.
                    var bytesRead: Int
                    var currentPosition = startPos
                    while (currentPosition <= endPos) {
                        bytesRead = inputStream.read(buffer, 0, chunkSize)
                        if (bytesRead == -1) break
                        outputStream.write(buffer, 0, bytesRead)
                        currentPosition += bytesRead
                    }
                    outputStream.close()
                    writeToFile(startPos.toLong(), outputFile, outPutFilPath, blobName, context)
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
            arrJob.add(job)
        }
        return arrJob.awaitAll()
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
        return context.filesDir.absolutePath + blobName + File.separator + "$blobName.temp"
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
            File(sourcePath).copyTo(File(destinationPath,blobName), true)
            File(sourcePath).delete()
        }
    }
}


