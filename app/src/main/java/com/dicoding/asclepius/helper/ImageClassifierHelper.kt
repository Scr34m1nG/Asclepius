package com.dicoding.asclepius.helper

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.media.Image
import android.net.Uri
import android.util.Log
import com.dicoding.asclepius.R
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.vision.classifier.Classifications
import org.tensorflow.lite.task.vision.classifier.ImageClassifier


class ImageClassifierHelper(
        private var threshold: Float = 0.3f,
        private var maxResults: Int = 3,
        val modelName: String = "cancer_classification.tflite",
        val context: Context,
        val classifierListener: ClassifierListener?
        ) {

    init {
        setupImageClassifier()
    }

    private var imageClassifier: ImageClassifier? = null

    private fun setupImageClassifier() {
        // TODO: Menyiapkan Image Classifier untuk memproses gambar.
        val optionBUilder = ImageClassifier
            .ImageClassifierOptions.builder()
            .setScoreThreshold(threshold)
            .setMaxResults(maxResults)

        val baseOptionsBuilder = BaseOptions.builder().setNumThreads(4)

        optionBUilder.setBaseOptions(baseOptionsBuilder.build())

        try {
            imageClassifier = ImageClassifier.createFromFileAndOptions(
                context,
                modelName,
                optionBUilder.build()
            )
        } catch (e: IllegalStateException) {
            classifierListener?.onError(context.getString(R.string.image_classifier_failed))
            Log.e(TAG, e.message.toString())
        }
    }

    fun classifyStaticImage(imageUri: Uri) {
        // TODO: mengklasifikasikan imageUri dari gambar statis.

        if (imageClassifier == null) {
            setupImageClassifier()
        }

        val inputImage = uriToBitMap(imageUri)

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(224, 224, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .add(CastOp(DataType.UINT8))
            .build()

        val tensorImage = TensorImage.fromBitmap(inputImage)

        val imageProcess = imageProcessor.process(tensorImage)

        val result = imageClassifier?.classify(imageProcess)

        if (result != null) {
            classifierListener?.onResult(result)
        } else {
            classifierListener?.onError(context.getString(R.string.image_classifier_failed))
        }
    }

    fun uriToBitMap(imageUri: Uri): Bitmap {
        val source = context.contentResolver.openInputStream(imageUri)
        return BitmapFactory.decodeStream(source)
    }

    interface ClassifierListener{
        fun onError(error: String)
        fun onResult(result: List<Classifications>?)
    }

    companion object {
        private const val TAG = "Image Classifier Helper"
    }

}