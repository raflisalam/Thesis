package com.raflisalam.signlanguage.factory

import android.content.Context
import android.icu.lang.UCharacter
import android.graphics.RectF
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.view.PreviewView
import android.graphics.Bitmap
import android.graphics.Matrix
import java.lang.Double.max

class ImageAnalyse (
    private val context : Context,
    private val previewView : PreviewView,
    private val rotation : Int,
    private val yoloV5 : ModelTensorflowYOLO,
    private val imageProcess: ImageProcess = ImageProcess(),
    private val graphicOverlay : GraphicOverlay,
    private val onResult: (String) -> Unit

    ) : ImageAnalysis.Analyzer {


    private var onDetect : Boolean = true

    override fun analyze(image: ImageProxy) {

        val previewHeight = previewView.height;
        val previewWidth = previewView.width;

        val yuvBytes = arrayOfNulls<ByteArray>(3)

        val planes = image.planes
        val imageHeight = image.height
        val imageWidth = image.width

        graphicOverlay.setImageSourceInfo(imageWidth, imageHeight, false)
        imageProcess.fillBytes(planes, yuvBytes)
        val yRowStride = planes[0].rowStride
        val uvRowStride = planes[1].rowStride
        val uvPixelStride = planes[1].pixelStride

        val rgbBytes = IntArray(imageHeight * imageWidth)
        imageProcess.YUV420ToARGB8888(
            yuvBytes[0] as ByteArray,
            yuvBytes[1] as ByteArray,
            yuvBytes[2] as ByteArray,
            imageWidth,
            imageHeight,
            yRowStride,
            uvRowStride,
            uvPixelStride,
            rgbBytes
        )

        val imageBitmap = Bitmap.createBitmap(imageWidth, imageHeight, Bitmap.Config.ARGB_8888)
        imageBitmap.setPixels(rgbBytes, 0, imageWidth, 0,0, imageWidth, imageHeight)

        // Scale, ya ini basically cuman nyari scale , dari image ke preview
        val scale : Double = max(
             previewHeight / (if (rotation % 180 == 0) imageWidth else imageHeight).toDouble(),
                 previewWidth / (if (rotation % 180 == 0) imageHeight else imageWidth).toDouble()
        )

        val fullScreenTransform : Matrix = imageProcess.getTransformationMatrix(
            imageWidth, imageHeight, (scale * imageHeight).toInt(), (scale*imageWidth).toInt(),
            if (rotation % 180 == 0 ) 90 else 0, false
        )

        val fullImageBitmap = Bitmap.createBitmap(
            imageBitmap,
            0,0,
            imageWidth,imageHeight,
            fullScreenTransform,false
        )

        val cropImageBitmap = Bitmap.createBitmap(
            fullImageBitmap, 0,0,previewWidth,previewHeight)


        val previewToModelTransform : Matrix = imageProcess.getTransformationMatrix(
            previewWidth, previewHeight,
            yoloV5.inputSize.width,
            yoloV5.inputSize.height,
            0, false
        )

        val modelInputBitmap = Bitmap.createBitmap(
            cropImageBitmap, 0,0,
            cropImageBitmap.width,
            cropImageBitmap.height,
            previewToModelTransform,false
        )

        val modelToPreviewTransform = Matrix()
        previewToModelTransform.invert(modelToPreviewTransform)

        if(onDetect)
        {
            val recognitionResult : ArrayList<RecognitionResult> = yoloV5.detect(modelInputBitmap)

            graphicOverlay.clear()
            for(res in recognitionResult)
            {
                val location : RectF = res.getLocation()
                modelToPreviewTransform.mapRect(location, location)
                res.setLocation(location)
                graphicOverlay.add(ObjectGraphic(this.graphicOverlay, res))
            }

            recognitionResult.toSet().forEach{
                onResult(it.getLabelName())
            }
        }

        image.close()
    }
}