package com.raflisalam.signlanguage.factory

object UtilsClassifier {

    @JvmStatic
    fun calculateOutputSize(input_size : Int, total_label : Int ) : IntArray
    {
        val inputSizeDiv_8 = input_size shr 3
        val inputSizeDiv_16 = input_size shr 4
        val inputSizeDiv_32 = input_size shr 5

        val valueToStore = (inputSizeDiv_8 * inputSizeDiv_8) + (inputSizeDiv_16 * inputSizeDiv_16) + (inputSizeDiv_32 * inputSizeDiv_32)
        val finalOutSize = valueToStore * 3
        return intArrayOf(1, finalOutSize, total_label + 5)

    }

}