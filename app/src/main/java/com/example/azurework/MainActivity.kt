package com.example.azurework

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.azurework.databinding.ActivityMainBinding
import java.io.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding
    val MY_READ_EXTERNAL_REQUEST : Int = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        val view = binding.root
        setContentView(view)

        binding.buttonFile.setOnClickListener {
            checkPermission()
        }
        


    }

    fun checkPermission(){
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Permission is not granted
            // Ask for permission
            ActivityCompat.requestPermissions(
                this, arrayOf<String>(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_READ_EXTERNAL_REQUEST
            )
        } else {
            // Permission has already been granted
            // Access the file

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_READ_EXTERNAL_REQUEST -> if ((grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED)
            ) {
                // Permission is granted. Continue the action or workflow
                // in your app.
            }

        }}
}