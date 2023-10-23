package com.gauthamkrishna.objectdetectyolo.model

import android.annotation.SuppressLint
import android.graphics.*
import android.speech.tts.TextToSpeech
import androidx.camera.view.PreviewView
import com.gauthamkrishna.objectdetectyolo.utils.ImageProcess
import androidx.camera.core.ImageProxy
import io.reactivex.rxjava3.core.ObservableEmitter
import android.util.Log
import android.widget.ImageView
import androidx.camera.core.ImageAnalysis
import com.gauthamkrishna.objectdetectyolo.utils.Recognition
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable

class ImageAnalysis(
    val tts: TextToSpeech?,
    var previewView: PreviewView,
    var boxLabelCanvas: ImageView,
    var rotation: Int,
    yoloV5ObjectDetector: YoloV5ObjectDetector
) : ImageAnalysis.Analyzer {
    class Result(
        var costTime: Long,
        var bitmap: Bitmap,
        var labels: String
        )

    var imageProcess: ImageProcess = ImageProcess()
    var detectedObjectName = ""
    private val yoloV5ObjectDetector: YoloV5ObjectDetector
    var timeArray : Array<Int> = emptyArray()

    @SuppressLint("SetTextI18n", "CheckResult")
    override fun analyze(image: ImageProxy) {
        val previewHeight = previewView.height
        val previewWidth = previewView.width
        Observable.create { emitter: ObservableEmitter<Result> ->
            val start = System.currentTimeMillis()
            Log.i("image", "$previewWidth/$previewHeight")
            val yuvBytes = arrayOfNulls<ByteArray>(3)
            val planes = image.planes
            val imageHeight = image.height
            val imagewWidth = image.width
            imageProcess.fillBytes(planes, yuvBytes)
            val yRowStride = planes[0].rowStride
            val uvRowStride = planes[1].rowStride
            val uvPixelStride = planes[1].pixelStride
            val rgbBytes = IntArray(imageHeight * imagewWidth)
            imageProcess.YUV420ToARGB8888(
                yuvBytes[0]!!,
                yuvBytes[1]!!,
                yuvBytes[2]!!,
                imagewWidth,
                imageHeight,
                yRowStride,
                uvRowStride,
                uvPixelStride,
                rgbBytes
            )
            val imageBitmap = Bitmap.createBitmap(imagewWidth, imageHeight, Bitmap.Config.ARGB_8888)
            imageBitmap.setPixels(rgbBytes, 0, imagewWidth, 0, 0, imagewWidth, imageHeight)
            val scale =
                (previewHeight / (if (rotation % 180 == 0) imagewWidth else imageHeight).toDouble()).coerceAtLeast(
                    previewWidth / (if (rotation % 180 == 0) imageHeight else imagewWidth).toDouble()
                )
            val fullScreenTransform = imageProcess.getTransformationMatrix(
                imagewWidth,
                imageHeight,
                (scale * imageHeight).toInt(),
                (scale * imagewWidth).toInt(),
                if (rotation % 180 == 0) 90 else 0,
                false
            )
            val fullImageBitmap = Bitmap.createBitmap(
                imageBitmap,
                0,
                0,
                imagewWidth,
                imageHeight,
                fullScreenTransform,
                false
            )
            val cropImageBitmap = Bitmap.createBitmap(
                fullImageBitmap, 0, 0,
                previewWidth, previewHeight
            )
            val previewToModelTransform = imageProcess.getTransformationMatrix(
                cropImageBitmap.width, cropImageBitmap.height,
                yoloV5ObjectDetector.inputSize.width,
                yoloV5ObjectDetector.inputSize.height,
                0, false
            )
            val modelInputBitmap = Bitmap.createBitmap(
                cropImageBitmap, 0, 0,
                cropImageBitmap.width, cropImageBitmap.height,
                previewToModelTransform, false
            )
            val modelToPreviewTransform = Matrix()
            previewToModelTransform.invert(modelToPreviewTransform)
            //time stamp
            val time1 = System.currentTimeMillis()
            val recognitions = yoloV5ObjectDetector.detect(modelInputBitmap)
            val timetaken = (System.currentTimeMillis() - time1)
            timeArray += timetaken.toInt()
            val timeArrayAvg = timeArray.average()
            val arraySize = timeArray.size
            var median :Int
            timeArray.sortedArray()
            //median
            if (timeArray.size % 2 == 0 ) {
                median = (timeArray[arraySize / 2] + timeArray[(arraySize / 2) - 1])
                median /= 2
            }
            else {
              median =  timeArray[timeArray.size / 2]
            }


            Log.i("TimeTaken Average", "Time taken : $timeArrayAvg ms")
            Log.i("TimeTaken Median", "Time AAtaken median : $median ms")
            val emptyCropSizeBitmap =
                Bitmap.createBitmap(previewWidth, previewHeight, Bitmap.Config.ARGB_8888)
            val cropCanvas = Canvas(emptyCropSizeBitmap)
            val boxPaint = Paint()
            boxPaint.strokeWidth = 5f
            boxPaint.style = Paint.Style.STROKE
            boxPaint.color = Color.RED
            val textPain = Paint()
            textPain.textSize = 50f
            textPain.color = Color.RED
            textPain.style = Paint.Style.FILL
            for (res in recognitions) {
                val location = res!!.getLocation()
                val label = res.labelName
                val confidence = res.confidence!!
                modelToPreviewTransform.mapRect(location)
                cropCanvas.drawRect(location, boxPaint)
                cropCanvas.drawText(
                    label + ":" + String.format("%.2f", confidence), //remember
                    location.left,
                    location.top,
                    textPain
                )
            }
            val detectedObjectNames = if(recognitions.isNotEmpty()) {
                recognitions.toList().sortedWith(compareByDescending<Recognition?> { it?.confidence?: 0.0 }).firstOrNull()?.labelName?:""
            } else {
                ""
            }
            val end = System.currentTimeMillis()
            val costTime = end - start
            image.close()
            emitter.onNext(Result(costTime, emptyCropSizeBitmap, detectedObjectNames))
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { result: Result ->
                boxLabelCanvas.setImageBitmap(result.bitmap)
                if(result.labels != detectedObjectName) {
                    detectedObjectName = result.labels
                    tts?.speak(detectedObjectName, TextToSpeech.QUEUE_FLUSH, null, null)
                } else {
                    // Doing nothing
                }
            }
    }

    init {
        this.yoloV5ObjectDetector = yoloV5ObjectDetector
    }
}