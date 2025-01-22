package com.dicoding.asclepius.view

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.dicoding.asclepius.R
import com.dicoding.asclepius.databinding.ActivityMainBinding
import com.dicoding.asclepius.helper.ImageClassifierHelper
import org.tensorflow.lite.task.vision.classifier.Classifications
import java.text.NumberFormat

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var imageClassifierHelper: ImageClassifierHelper

    private var currentImageUri: Uri? = null

    private var displayResult: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.galleryButton.setOnClickListener {
            startGallery()
        }

        binding.analyzeButton.setOnClickListener {
            if (currentImageUri != null) {
                analyzeImage(currentImageUri!!)
            } else {
                showToast("Choose Image First")
            }
        }
    }

    private fun startGallery() {
        // TODO: Mendapatkan gambar dari Gallery.
        launchGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launchGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            currentImageUri = uri
            showImage()
        } else {
            Log.d("Launch gallery", "No image selected")
        }
    }

    private fun showImage() {
        // TODO: Menampilkan gambar sesuai Gallery yang dipilih.
        currentImageUri.let {
            binding.previewImageView.setImageURI(it)
        }
    }

    private fun analyzeImage(image: Uri) {
        val listener = object : ImageClassifierHelper.ClassifierListener {
            override fun onError(error: String) {
                showToast(error)
            }

            override fun onResult(result: List<Classifications>?) {
                if (result != null && result.isNotEmpty() && result[0].categories.isNotEmpty()) {
                    val sortedCategories = result[0].categories.sortedBy { it?.score }
                    val highestResult = sortedCategories.last()

                    displayResult = "${highestResult?.label} - ${NumberFormat.getPercentInstance().format(highestResult?.score).trim()}"

                    moveToResult()
                } else {
                    showToast("No results found")
                }
            }
        }

        imageClassifierHelper = ImageClassifierHelper(context = this, classifierListener = listener)
        imageClassifierHelper.classifyStaticImage(image)
    }



    private fun moveToResult() {
        val intent = Intent(this, ResultActivity::class.java)
        intent.putExtra(ResultActivity.EXTRA_IMG, currentImageUri.toString())
        intent.putExtra(ResultActivity.EXTRA_RESULT, displayResult)
        startActivity(intent)
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}