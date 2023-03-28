package com.example.azurework

import android.os.Parcel
import android.os.Parcelable
import java.net.URL

 data class FileModel(var blobName:String?, var blobSize:Int, var blobUrl:String) :Parcelable{
     constructor(parcel: Parcel) : this(
         parcel.readString(),
         parcel.readInt(),
         parcel.readString().toString()
     ) {
     }

     override fun writeToParcel(parcel: Parcel, flags: Int) {
         parcel.writeString(blobName)
         parcel.writeInt(blobSize)
         parcel.writeString(blobUrl)
     }

     override fun describeContents(): Int {
         return 0
     }

     companion object CREATOR : Parcelable.Creator<FileModel> {
         override fun createFromParcel(parcel: Parcel): FileModel {
             return FileModel(parcel)
         }

         override fun newArray(size: Int): Array<FileModel?> {
             return arrayOfNulls(size)
         }
     }

 }
