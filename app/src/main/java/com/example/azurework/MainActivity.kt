package com.example.azurework


import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentResolver
import android.content.Context
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
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.BlobServiceClientBuilder
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
import java.lang.Integer.min
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val SELECT_PICTURE = 200
    val FILE_SELECT_CODE = 2
    var arraylist = ArrayList<String>()
    val blobArray = ArrayList<FileModel>()
    lateinit var path: File
    var blobListData=kotlin.collections.ArrayList<FileModel>()
    var displayName: String? = null
    var sizeIndex:Int?= null
    val MY_WRITE_EXTERNAL_REQUEST = 300
    val connectionString =
        "DefaultEndpointsProtocol=https;AccountName=anikkarmakar;AccountKey=dN+fVtZihBvdf0QSSUVl68gHjqcVwsHFTsIfelmsEoeatvx8e2FwTwMEk9WLzzhL7seVCyS2zDGU+AStDBf46A==;EndpointSuffix=core.windows.net"
    lateinit var progressBar:ProgressBar
    lateinit var progressDialog:ProgressDialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        val view = binding.root
        setContentView(view)
        Chunkdatabase.getDatabase(applicationContext)
        progressDialog = ProgressDialog(this@MainActivity)
        binding.fileName.visibility = View.GONE
        blobListData=getAllUploadedFile()
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
                val intent= Intent(this@MainActivity,MainActivity2::class.java)
                val bundle = Bundle()
                bundle.putParcelableArrayList("ArrayData", blobListData)
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

    /*override fun onResume() {
        super.onResume()
        data=getAllUploadedFile()
    }*/
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
                val bundle = Bundle()
                bundle.putParcelableArrayList("ArrayData", blobListData)
                intent.putExtras(bundle)
                startActivity(intent)
            }else{

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
                                            arraylist.add(displayName.toString())
                                            binding.fileName.text = displayName
                                        }
                                    } finally {
                                        cursor?.close()
                                    }
                                } else if (uriString.startsWith("file://")) {
                                    displayName = myFile.getName()
                                    binding.fileName.visibility = View.VISIBLE
                                    binding.fileName.text = displayName
                                }
                                progressDialog.setMessage("Please wait....");
                                progressDialog.setTitle("Uploading File");
                                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                progressDialog.show();
                                CoroutineScope(Dispatchers.IO).launch {
                                    async {
                                        /*uploadFileIntoMultipart(inputStream, displayName.toString()) */
                                        /*uploadMultipart(inputStream,displayName.toString(),length)*/
                                        uploadToAzure(displayName.toString(),inputStream!!)
                                    }.await()
                                    blobListData.clear()
                                    getAllUploadedFile()
                                    CoroutineScope(Dispatchers.Main).launch {
                                        Log.d("status","Succesfully uploaded")
                                        progressDialog.dismiss()
                                        Toast.makeText(applicationContext,"Successfully Upload",Toast.LENGTH_LONG).show()
                                    }
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

                            val length= getFileSizeFromContentUri(applicationContext,uri)
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
                            progressDialog.setMessage("Please wait....");
                            progressDialog.setTitle("Uploading File");
                            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            progressDialog.show();
                            CoroutineScope(Dispatchers.IO).launch {
                                async {
                                    /*uploadFileIntoMultipart(inputStream, displayName.toString()) */
                                    /*uploadMultipart(inputStream,displayName.toString(),length)*/
                                    uploadToAzure(displayName.toString(),inputStream!!)
                                }.await()
                                blobListData.clear()
                                getAllUploadedFile()
                                CoroutineScope(Dispatchers.Main).launch {
                                    Log.d("status","Succesfully uploaded")
                                    progressDialog.dismiss()
                                    Toast.makeText(applicationContext,"Successfully Upload",Toast.LENGTH_LONG).show()
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

    fun getFileSizeFromContentUri(context: Context, uri: Uri): Long {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        val sizeIndex = cursor?.getColumnIndex(OpenableColumns.SIZE)
        cursor?.moveToFirst()
        val size = cursor?.getLong(sizeIndex!!)
        cursor?.close()
        return size!!
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


    fun getAllUploadedFile() :ArrayList<FileModel>{
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
                        //blob.download(FileOutputStream(blob.name))
                    }
                }
            } catch (e: java.lang.Exception) {
                // Output the stack trace.
                e.printStackTrace()
            }
        }
        return blobArray
    }
    fun downloadF(getBlobName:String){
        CoroutineScope(Dispatchers.IO).launch {
            val account = CloudStorageAccount.parse(this@MainActivity.connectionString)
            val blobClient: CloudBlobClient = account.createCloudBlobClient()
            val container: CloudBlobContainer = blobClient.getContainerReference("arc-file-container")
            val blob: CloudBlockBlob = container.getBlockBlobReference(getBlobName)
            blob.download(FileOutputStream(File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Download", getBlobName)))

        }

    }

    fun uploadToAzure(blobName: String, inputStream: InputStream) {
        try {
            val blobClient = CloudStorageAccount.parse(connectionString).createCloudBlobClient()
            val container = blobClient.getContainerReference("arc-file-container")

            val blockBlob = container.getBlockBlobReference(blobName)

            val blockSize = 2*1024*1024 // 4MB block size
            var blockId = 0
            var blockList= mutableListOf<BlockEntry>()
            inputStream.use { input ->
                var bytesRemaining = input.available()
                var currentPosition = 0
                val numChunks = ((bytesRemaining + blockSize) - 1)/blockSize
                for (i in 0 until numChunks){
                    val blockNumber = String.format("%05d", blockId++)
                    val blockIdEncoded =
                        Base64.getEncoder().encodeToString(blockNumber.toByteArray())
                    val blockLength = Math.min(blockSize, bytesRemaining)

                    val blockData = ByteArray(blockLength)
                    input.read(blockData, 0, blockLength)

                    blockBlob.uploadBlock(
                        blockIdEncoded,
                        ByteArrayInputStream(blockData),
                        blockLength.toLong()
                    )
                    blockList.add(BlockEntry(blockIdEncoded))
                    currentPosition = currentPosition+1
//                    bytesRemaining = input.available() - currentPosition
                }
                /*while (bytesRemaining > 0) {
                    *//*val blockNumber = String.format("%05d", blockId++)
                    val blockIdEncoded =
                        Base64.getEncoder().encodeToString(blockNumber.toByteArray())
                    val blockLength = Math.min(blockSize, bytesRemaining)

                    val blockData = ByteArray(blockLength)
                    input.read(blockData, 0, blockLength)

                    blockBlob.uploadBlock(
                        blockIdEncoded,
                        ByteArrayInputStream(blockData),
                        blockLength.toLong()
                    )
                    blockList.add(BlockEntry(blockIdEncoded))
                    currentPosition += blockLength
                    bytesRemaining = input.available() - currentPosition*//*
                }*/
                val blockEntries: Iterable<BlockEntry> = blockList
                blockBlob.commitBlockList(blockEntries)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    suspend fun uploadMultipart(takeFile: InputStream?, blobName: String,fileLength:Long){
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val storageAccount = CloudStorageAccount.parse(connectionString)
                val blobClient = storageAccount.createCloudBlobClient()
                val container = blobClient.getContainerReference("arc-file-container")
                val blob:CloudBlockBlob= container.getBlockBlobReference(blobName)
                val chunkSize = 4*1024*1024 // 1 MB
                var blockIds = mutableListOf<BlockEntry>()
                val fileSize = fileLength
                val numChunks = ((fileSize + chunkSize) - 1)/chunkSize
                var blockId = 1
                var offset = 0L
                val buffer = ByteArray(chunkSize)
                for (i in 0 until numChunks) {
                    /*val chunkData = ByteArray(chunkSize)
                    val bytesRead = takeFile?.read(chunkData)
                    if (bytesRead != -1) {
                        val takeblockId = Base64.getEncoder().encodeToString("block-$blockId".toByteArray())

                        blob.uploadBlock(takeblockId,ByteArrayInputStream(chunkData, 0, bytesRead!!),
                            bytesRead!!.toLong())
                        blockIds.add(BlockEntry(takeblockId))
                        blockId++
                    }
                    blob.commitBlockList(blockIds)*/
                    val takeblockId = Base64.getEncoder().encodeToString("block-$i".toByteArray())
                    val length = min(chunkSize, (fileSize.toInt() - offset).toInt()).toInt()
                    takeFile?.skip(offset)
                    takeFile?.read(buffer, 0, length)
                    takeFile?.close()

                    blob.uploadBlock(
                        takeblockId,
                        ByteArrayInputStream(buffer, 0, length),
                        length.toLong()
                    )
                    offset += length
                }
                val blockList = (0 until numChunks).map { "block-$it" }.toTypedArray()
                val blockEntries: Iterable<BlockEntry> = blockList.map { BlockEntry(it, null) }
                blob.commitBlockList(blockEntries)
            }catch (e:Exception){
                e.printStackTrace()
            }

            /*val blockIds = (0 until numChunks).map { Base64.getEncoder().encodeToString(it.toString().toByteArray()) }
            blob.commitBlockList(BlockEntry(blockIds)
            takeFile?.close()*/
        }

    }

    suspend fun uploadFileIntoMultipart(takeFile: InputStream?, blobName: String){
        try {
            val storageAccount = CloudStorageAccount.parse(connectionString)
            val blobClient = storageAccount.createCloudBlobClient()
            val container = blobClient.getContainerReference("arc-file-container")
            val blob:CloudBlockBlob= container.getBlockBlobReference(blobName)
            val blockSize =  500000 // 1 MB
            /*val file = File("<your_file_path>")*/

            var blockIds = mutableListOf<BlockEntry>()
            var blockId = 1
            var bytesRead = 0
            val buffer = ByteArray(blockSize)
            while (bytesRead != -1) {
                bytesRead = takeFile!!.read(buffer)
                if (bytesRead != -1) {
                    val takeblockId = Base64.getEncoder().encodeToString("block-$blockId".toByteArray())

                    blob.uploadBlock(takeblockId,ByteArrayInputStream(buffer, 0, bytesRead),bytesRead.toLong())
                    blockIds.add(BlockEntry(takeblockId))
                    blockId++
                }
                blob.commitBlockList(blockIds)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }



    }


}