package com.mildp.jetpackcompose.utils

import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipUtils {

    fun zipFolders(srcFolder: String, destZipFile: String): Boolean {
        try {
            val fileWriter = FileOutputStream(destZipFile)
            val zip = ZipOutputStream(fileWriter)
            addFolderToZip("", srcFolder, zip)
            zip.flush()
            zip.close()
            return true
        } catch (e:Exception){
            Log.d("Zip","Error:$e")
        }
        return  false
    }

    private fun addFolderToZip(path: String, srcFolder: String,zip: ZipOutputStream){
        val folder = File(srcFolder)
        val fileNames: Array<String>? = folder.list()

        fileNames?.forEach { fileName ->
            val filePath = if (path.isEmpty()) srcFolder else "$path/${folder.name}"
            val file = File(folder, fileName)
            if (file.isDirectory) {
                addFolderToZip(filePath, file.absolutePath, zip)
            } else {
                addFileToZip(filePath, file.absolutePath, zip)
            }
        }
    }

    private fun addFileToZip(path: String, srcFile: String, zip: ZipOutputStream){
        val file = File(srcFile)
        val buf = ByteArray(2048)
        val inputStream = FileInputStream(srcFile)
        zip.putNextEntry(ZipEntry("$path/${file.name}"))
        var len: Int
        while (inputStream.read(buf).also { len = it } > 0) {
            zip.write(buf, 0, len)
        }
        inputStream.close()
        zip.closeEntry()
    }
}