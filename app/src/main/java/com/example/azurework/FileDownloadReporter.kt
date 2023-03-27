package com.example.azurework

import com.azure.storage.blob.ProgressReceiver

class FileDownloadReporter: ProgressReceiver {
    override fun reportProgress(bytesTransferred: Long) {
        print("You have download bytes $bytesTransferred");
    }

}