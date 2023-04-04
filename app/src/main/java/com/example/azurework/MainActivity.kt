package com.example.azurework


import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.BlobServiceClientBuilder
import com.azure.storage.blob.specialized.BlockBlobClient
import com.example.azurework.databinding.ActivityMainBinding
import com.microsoft.azure.storage.CloudStorageAccount
import com.microsoft.azure.storage.blob.BlockEntry
import com.microsoft.azure.storage.blob.CloudBlob
import com.microsoft.azure.storage.blob.CloudBlobClient
import com.microsoft.azure.storage.blob.CloudBlobContainer
import com.microsoft.azure.storage.blob.CloudBlockBlob
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.*
import java.util.concurrent.Executors
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val SELECT_PICTURE = 200
    val FILE_SELECT_CODE = 2
    var arraylist = ArrayList<String>()
    val blobArray = ArrayList<FileModel>()
    lateinit var path: File
    var displayName: String? = null
    val MY_WRITE_EXTERNAL_REQUEST = 300
    val connectionString =
        "DefaultEndpointsProtocol=https;AccountName=anikkarmakar;AccountKey=dN+fVtZihBvdf0QSSUVl68gHjqcVwsHFTsIfelmsEoeatvx8e2FwTwMEk9WLzzhL7seVCyS2zDGU+AStDBf46A==;EndpointSuffix=core.windows.net"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        val view = binding.root
        setContentView(view)

        binding.fileName.visibility = View.GONE


        /*binding.pic.setOnClickListener {
            imageChooser()
        }*/

        binding.details.setOnClickListener {
//            getAllUploadedFile()

            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {

                // Permission is not granted
                // Ask for permission
                ActivityCompat.requestPermissions(
                    this, arrayOf<String>(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MY_WRITE_EXTERNAL_REQUEST
                )
            }
            else{
                val data=null
//                    getAllUploadedFile()
                val intent= Intent(this@MainActivity,MainActivity2::class.java)
                val bundle = Bundle()
                bundle.putParcelableArrayList("ArrayData", data)
                intent.putExtras(bundle)
                startActivity(intent)

            }
        }

        binding.download.setOnClickListener {
            val path: String = Environment.getExternalStorageDirectory().toString();
            val uri: Uri = Uri.parse(path);
            val intent = Intent(Intent.ACTION_GET_CONTENT);

            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            intent.putExtra("CONTENT_TYPE", "*/*");

            intent.setDataAndType(uri, "*/*");
            startActivityForResult(
                Intent.createChooser(intent, "Select a File to Upload"),
                FILE_SELECT_CODE
            );

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_WRITE_EXTERNAL_REQUEST -> if ((grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)
            ) {
                val intent= Intent(this@MainActivity,MainActivity2::class.java)
                startActivity(intent)
                /*downloadF()*/
            }
        }
    }
    fun imageChooser() {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT

        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode === -1) {

            when (requestCode) {
                SELECT_PICTURE -> {
                    val selectedImageUri: Uri = data!!.data!!
                    if (null != selectedImageUri) {

                        val fileName = getRealPathFromURI1(this, data.data)

                        try {

                            val stream = contentResolver.openInputStream(
                                data.data!!
                            )
                            val bitmap = BitmapFactory.decodeStream(stream)
                            stream!!.close()
                            binding.IVPreviewImage.setImageBitmap(bitmap)
                            val stream1 = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream1)
                            val imageInByte = stream1.toByteArray()
                            uploadImageFile(imageInByte, fileName.toString())
                        } catch (e: FileNotFoundException) {
                            e.printStackTrace()
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                        // update the preview image in the layout
                        /*binding.IVPreviewImage.setImageURI(selectedImageUri)*/
                    }
                }
                FILE_SELECT_CODE -> {
                    if (null != data) {
                        val newClipData = data.clipData
                        if (newClipData != null) {
                            for (i in 0 until newClipData.itemCount) {
                                val uri = newClipData.getItemAt(i).uri
                                val uriString = uri.toString()
                                val myFile = File(uriString)
                                path = myFile


                                if (uriString.startsWith("content://")) {
                                    var cursor: Cursor? = null
                                    try {
                                        cursor = applicationContext.getContentResolver()
                                            .query(uri, null, null, null, null)
                                        if (cursor != null && cursor.moveToFirst()) {
                                            displayName =
                                                cursor.getString(
                                                    cursor.getColumnIndex(
                                                        OpenableColumns.DISPLAY_NAME
                                                    )
                                                )
                                            /*arraylist.addAll(displayName)*/
                                            /*binding.fileName.text = displayName*/
                                        }
                                    } finally {
                                        cursor?.close()
                                    }
                                } else if (uriString.startsWith("file://")) {
                                    displayName = myFile.getName()
                                    binding.fileName.visibility = View.VISIBLE
                                    binding.fileName.text = displayName
                                }
                            }
                            binding.fileName.visibility = View.VISIBLE
                            binding.fileName.text = arraylist.toString()

//                                Log.d("filesUri [" + uri + "] : ", uri.toString() );
                        } else {
                            val uri: Uri = data.data!!
                            val uriString = uri.toString()
                            val myFile = File(uriString)
                            /*path = myFile*/
                            val contentResolver = applicationContext.contentResolver
                            val uri2 = Uri.parse(uriString)
                            var inputStream = uriToInputStream(contentResolver, uri2)


                            if (uriString.startsWith("content://")) {
                                var cursor: Cursor? = null
                                try {
                                    cursor = applicationContext.getContentResolver()
                                        .query(uri, null, null, null, null)
                                    if (cursor != null && cursor.moveToFirst()) {
                                        displayName =
                                            cursor.getString(
                                                cursor.getColumnIndex(
                                                    OpenableColumns.DISPLAY_NAME
                                                )
                                            )
                                        /*arraylist.addAll(displayName)*/
                                        /*binding.fileName.text = displayName*/
                                    }
                                } finally {
                                    cursor?.close()
                                }
                            }
                            arraylist.addAll(listOf(displayName.toString()))
                            binding.fileName.visibility = View.VISIBLE
                            binding.fileName.text = displayName
                           /*uploadFile(inputStream, displayName.toString())*/
                            CoroutineScope(Dispatchers.IO).launch {
                                async { uploadFileIntoMultipart(inputStream, displayName.toString()) }.await()
                                CoroutineScope(Dispatchers.Main).launch {
                                    Log.d("status","Succesfully uploaded")
                                }
                            }

                            /*uploadMultipartFile(inputStream, displayName.toString(),myFile.length())*/
                        }
                    } else {
                        val uri = data?.data
                        arraylist.addAll(listOf(uri.toString()))
                        Log.d("filesUri [" + uri + "] : ", uri.toString());
                    }
                    /*displayName?.let { uploadFile(path, it) }*/
                }

            }
        }
    }



    fun uploadFile(takeFile: InputStream?, blobName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val account = CloudStorageAccount.parse(connectionString)
                val blobClient = account.createCloudBlobClient()
                val container = blobClient.getContainerReference("arc-file-container")
                val blob = container.getBlockBlobReference(blobName)
                /*val fileStream: InputStream = java.io.FileInputStream(takeFile)*/
                blob.upload(takeFile, -1)
            } catch (e: Exception) {

            }
        }
    }
    /*fun uploadFileIntoChunk(){

        *//*val container = CloudBlobContainer(connectionString, "arc-file-container")*//*
        val account = CloudStorageAccount.parse(this@MainActivity.connectionString)
        val blobClient: CloudBlobClient = account.createCloudBlobClient()
        val container: CloudBlobContainer = blobClient.getContainerReference("arc-file-container")
// Define the file to be uploaded
        val file = File("/path/to/file")

// Define the chunk size for each upload
        val chunkSize = 1024

// Define an Executor to manage the upload threads
        val executor = Executors.newFixedThreadPool(4)

        // Define a function to upload a single chunk
        fun uploadChunk(blockBlob: CloudBlockBlob, fileStream: FileInputStream, offset: Long, length: Int) {
            val buffer = ByteArray(length)
            fileStream.read(buffer, offset.toInt(), length)
            blockBlob.uploadFromByteArray(buffer, offset.toInt(), length)
        }

        // Define a function to upload the file in chunks
        fun uploadFileInChunks() {
            val blockBlob = container.getBlockBlobReference(file.name)
            val fileStream = FileInputStream(file)
            val fileSize = file.length()
            var offset = 0L
            while (offset < fileSize) {
                val length = Math.min(chunkSize, fileSize - offset).toInt()
                executor.execute {
                    uploadChunk(blockBlob, fileStream, offset, length)
                }
                offset += chunkSize.toLong()
            }
            executor.shutdown()
        }

// Call the uploadFileInChunks function to start the upload
        uploadFileInChunks()
    }
*/

    /*fun uploadMultipartFile(takeFile: InputStream?, blobName: String,length:Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val blobServiceClient =
                    BlobServiceClientBuilder().connectionString(connectionString).buildClient()
                val containerName = "arc-file-container"
                *//*blobServiceClient.createBlobContainer(containerName)*//*
                *//*blobServiceClient.getBlobContainerClient("arc-file-container")*//*
                val fileName = blobName
                val blockBlobClient: BlockBlobClient =
                    blobServiceClient.getBlobContainerClient(containerName)
                        .getBlobClient(fileName).blockBlobClient

                    blockBlobClient.upload(takeFile,0,true)
                *//*blockBlobClient.uploadWithResponse(
                    takeFile,
                    1,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null
                )
*//*
                *//*      blockBlobClient.uploadFromStream(
                          takeFile,
                          your_file_size,
                          null,
                          null,
                          null,
                          null
                      )*//*

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }*/

    fun uploadImageFile(imageFile: ByteArray, imageBlobName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val connectionString =
                    "DefaultEndpointsProtocol=https;AccountName=anikkarmakar;AccountKey=dN+fVtZihBvdf0QSSUVl68gHjqcVwsHFTsIfelmsEoeatvx8e2FwTwMEk9WLzzhL7seVCyS2zDGU+AStDBf46A==;EndpointSuffix=core.windows.net"
                val account = CloudStorageAccount.parse(connectionString)
                val blobClient = account.createCloudBlobClient()
                val container = blobClient.getContainerReference("arc-file-container")
                val blob = container.getBlockBlobReference(imageBlobName)
                /*val fileStream: InputStream = java.io.FileInputStream(takeFile)*/
                blob.uploadFromByteArray(imageFile, 0, imageFile.size);
            } catch (e: Exception) {

            }
        }
    }

    fun uriToInputStream(contentResolver: ContentResolver, uri: Uri): InputStream? {
        return contentResolver.openInputStream(uri)
    }

    fun getRealPathFromURI1(
        activity: Activity,
        contentUri: Uri?
    ): String? {
        val uriString = contentUri.toString()
        if (uriString.startsWith("content://")) {
            var cursor: Cursor? = null
            try {
                cursor = contentUri?.let {
                    applicationContext.getContentResolver()
                        .query(it, null, null, null, null)
                }
                if (cursor != null && cursor.moveToFirst()) {
                    displayName =
                        cursor.getString(
                            cursor.getColumnIndex(
                                OpenableColumns.DISPLAY_NAME
                            )
                        )
                    /*arraylist.addAll(displayName)*/
                    /*binding.fileName.text = displayName*/
                }
            } finally {
                cursor?.close()
            }
        }
        return displayName
    }


    /*fun getAllUploadedFile() :ArrayList<FileModel>{
        CoroutineScope(Dispatchers.IO).launch {

            try {
                // Retrieve storage account from connection-string.
                val storageAccount = CloudStorageAccount.parse(connectionString)

                // Create the blob client.
                val blobClient = storageAccount.createCloudBlobClient()

                // Retrieve reference to a previously created container.
                val container = blobClient.getContainerReference("arc-file-container")

                val blobServiceClient: BlobServiceClient? =
                    BlobServiceClientBuilder().connectionString(connectionString).buildClient()

                // Loop over blobs within the container and output the URI to each of them.
                for (blobItem in container.listBlobs().iterator()) {
                    println(blobItem.uri)
                    if (blobItem is CloudBlob) {
                        // Download the item and save it to a file with the same name.
                        val blob = blobItem
                        val blobData=FileModel(blob.name,blob.properties.length.toInt(),blob.uri.toString())
                        blobArray.add(blobData)
                        *//*blob.download(FileOutputStream(blob.name))*//*
                    }
                }
            } catch (e: java.lang.Exception) {
                // Output the stack trace.
                e.printStackTrace()
            }
        }
        return blobArray
    }*/
    fun downloadF(getBlobName:String){
        CoroutineScope(Dispatchers.IO).launch {
            val account = CloudStorageAccount.parse(this@MainActivity.connectionString)
            val blobClient: CloudBlobClient = account.createCloudBlobClient()
            val container: CloudBlobContainer = blobClient.getContainerReference("arc-file-container")
            val blob: CloudBlockBlob = container.getBlockBlobReference(getBlobName)
            blob.download(FileOutputStream(File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download", getBlobName)))

        }

    }

    suspend fun uploadFileIntoMultipart(takeFile: InputStream?, blobName: String){
        try {
            val storageAccount = CloudStorageAccount.parse(connectionString)
            val blobClient = storageAccount.createCloudBlobClient()
            val container = blobClient.getContainerReference("arc-file-container")
            val blob:CloudBlockBlob= container.getBlockBlobReference(blobName)
            val blockSize = 4 * 1024 * 1024 // 4 MB
            /*val file = File("<your_file_path>")*/

            var blockIds = mutableListOf<String>()
            var blockId = 1
            var bytesRead = 0
            val buffer = ByteArray(blockSize)
            while (bytesRead != -1) {
                bytesRead = takeFile!!.read(buffer)
                if (bytesRead != -1) {
                    val takeblockId = Base64.getEncoder().encodeToString("block-$blockId".toByteArray())

                    /*val blockName = Base64.getEncoder().encodeToString("block-$blockId".toByteArray())*/
                    /*val block = blob
                    block.upload(ByteArrayInputStream(buffer, 0, bytesRead), bytesRead.toLong())*/
                    blob.uploadBlock(takeblockId,ByteArrayInputStream(buffer, 0, bytesRead),bytesRead.toLong())
                    blockIds.add(takeblockId)
                    blockId++
                }

            }
        }catch (e:Exception){
            e.printStackTrace()
        }

        /*val blobs = container.getBlockBlobReference("myblob")
        blobs.commitBlockList(blockIds)*/

    }


}