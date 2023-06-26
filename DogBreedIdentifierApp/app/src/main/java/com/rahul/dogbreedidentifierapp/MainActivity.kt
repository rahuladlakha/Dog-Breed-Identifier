package com.rahul.dogbreedidentifierapp

import android.content.Intent
import android.graphics.Bitmap
import android.media.ThumbnailUtils
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        supportActionBar?.title = "Identified Breed"
        supportActionBar?.hide()
        val result = intent.getStringExtra("result")
        TV_Breed.text = result
        capturedIV.setImageBitmap(ImageClassifier.img_bitmap)
        val des = DogDescriptionFactory.getDescription(result?.split("\n")?.get(0), this)
        if (des != null)
            TV_des.text = des
    }
}