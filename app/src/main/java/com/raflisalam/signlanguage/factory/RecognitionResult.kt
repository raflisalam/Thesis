package com.raflisalam.signlanguage.factory
import android.graphics.RectF

data class RecognitionResult(
    private var labelId: Int,
    private var labelName : String,
    private var labelScore : Float,
    private var confidence : Float,
    private var location : RectF
)
{
    fun getLabelId(): Int {
        return labelId
    }

    fun getLabelName(): String {
        return labelName
    }

    fun getLabelScore(): Float {
        return labelScore
    }

    fun getConfidence(): Float {
        return confidence
    }

    fun getLocation(): RectF {
        return RectF(location)
    }

    fun setLocation(location: RectF) {
        this.location = location
    }

    fun setLabelName(labelName: String) {
        this.labelName = labelName
    }

    fun setLabelId(labelId: Int) {
        this.labelId = labelId
    }

    fun setLabelScore(labelScore: Float) {
        this.labelScore = labelScore
    }

    fun setConfidence(confidence: Float) {
        this.confidence = confidence
    }

    override fun toString(): String {
        var resultString = ""
        resultString += "$labelId "
        if (labelName != null) {
            resultString += "$labelName "
        }
        if (confidence != null) {
            resultString += String.format("(%.1f%%) ", confidence * 100.0f)
        }
        if (location != null) {
            resultString += "$location "
        }
        return resultString.trim { it <= ' ' }
    }
}
