package com.example.cameradetection

import android.annotation.SuppressLint
import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.Objects

class ImageUploadActivity : AppCompatActivity() {
    private var progressBar: ProgressBar? = null
    private var fileName: String? = null
    private lateinit var dialog: AlertDialog


    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_upload)


        var bmp: Bitmap? = null
        val filename = intent.getStringExtra("image")
        try {
            val `is` = openFileInput(filename)
            bmp = BitmapFactory.decodeStream(`is`)
            `is`.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (bmp != null) {
            saveImage(getResizedBitmap(bmp, 500, 620))
        }
    }

    private fun saveImage(bmp: Bitmap) {
        val fos: OutputStream?
        fileName = "Img_" + System.currentTimeMillis() + ".jpg"
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                contentValues.put(
                    MediaStore.MediaColumns.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + "AvtoBaholash"
                )
                val uri =
                    resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = uri?.let { resolver.openOutputStream(it) } as OutputStream
                bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos)
                Objects.requireNonNull(fos)
                val iv = findViewById<ImageView>(R.id.iv21)
                iv.setImageURI(uri)
            } else {
                val dir = File(Environment.DIRECTORY_PICTURES + File.separator + "AvtoBaholash")
                if (!dir.exists()) {
                    dir.mkdir()
                }
                val file = File(dir, fileName)
                fos = FileOutputStream(file)
                bmp.compress(Bitmap.CompressFormat.JPEG, 60, fos)
                fos.flush()
                fos.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            progressBar!!.visibility = View.GONE
            Toast.makeText(this, "picture not saved" + e.message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun getResizedBitmap(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.getWidth()
        val height = bm.getHeight()
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        val resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false)
        bm.recycle()
        return resizedBitmap
    }


}