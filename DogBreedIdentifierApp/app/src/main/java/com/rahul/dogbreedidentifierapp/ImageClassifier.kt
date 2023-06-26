package com.rahul.dogbreedidentifierapp

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import com.rahul.dogbreedidentifierapp.ml.ImgClfacc84cls30ofMiddle40
import com.rahul.dogbreedidentifierapp.ml.ImgClfacc87cls30ofLast40
import com.rahul.dogbreedidentifierapp.ml.ImgClfacc88cls30ofFirst40
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class ImageClassifier {

    companion object {
//        val classes = listOf<String>("Beagle", "Black", "Blenheim Spaniel", "Bloodhound",  "Boxer", "Brittany Spaniel", "Bull Mastiff", "Chesapeake Bay Retriever", "Chihuahua", "Dhole", "Dingo", "Doberman", "English Foxhound", "French Bulldog", "German Shepherd", "Golden Retriever", "Great Dane", "Italian Greyhound",  "Kelpie", "Kuvasz", "Labrador Retriever", "Miniature Poodle", "Norwich Terrier", "Papillon", "Pug",  "Rottweiler",  "Siberian Husky", "Standard Poodle", "Tibetan Terrier", "Toy Poodle", "Toy Terrier", "Yorkshire Terrier")
        val classes = listOf<String>(
            "African Hunting Dog", "Airedale", "American Staffordshire Terrier", "Appenzeller", "Australian Terrier", "Basenji", "Beagle", "Bedlington Terrier", "Bernese Mountain Dog", "Black", "Blenheim Spaniel", "Bloodhound", "Border Collie", "Border Terrier", "Borzoi", "Boston Bull", "Boxer", "Brabancon Griffon", "Briard", "Brittany Spaniel", "Bull Mastiff", "Cardigan", "Chihuahua", "Chow", "Cocker Spaniel",  "Dandie Dinmont", "Dhole", "Dingo", "Doberman", "English Foxhound",
            "EntleBucher", "Eskimo Dog", "Flat", "French Bulldog", "German Shepherd", "Giant Schnauzer", "Golden Retriever", "Great Dane", "Great Pyrenees", "Greater Swiss Mountain Dog", "Groenendael", "Ibizan Hound", "Irish Terrier", "Italian Greyhound", "Keeshond", "Kelpie", "Kerry Blue Terrier", "Komondor", "Kuvasz", "Labrador Retriever", "Lakeland Terrier", "Leonberg", "Malamute", "Malinois", "Maltese Dog", "Mexican Hairless", "Miniature Pinscher", "Miniature Poodle", "Miniature Schnauzer", "Norfolk Terrier",
            "Norwegian Elkhound", "Norwich Terrier", "Old English Sheepdog", "Otterhound", "Papillon", "Pekinese", "Pembroke", "Pomeranian", "Pug", "Redbone", "Rhodesian Ridgeback", "Rottweiler", "Saint Bernard", "Saluki", "Samoyed", "Schipperke", "Scotch Terrier", "Scottish Deerhound", "Sealyham Terrier", "Shetland Sheepdog", "Siberian Husky", "Staffordshire Bullterrier", "Standard Poodle", "Standard Schnauzer", "Tibetan Mastiff", "Toy Poodle", "Vizsla", "Walker Hound", "West Highland White Terrier", "Yorkshire Terrier"
        )
        val imgsize = 256
        var img_bitmap: Bitmap? = null

        lateinit var m1 :ImgClfacc88cls30ofFirst40
        lateinit var m2 :ImgClfacc84cls30ofMiddle40
        lateinit var m3 :ImgClfacc87cls30ofLast40
        fun setup(context: Context){
            //Initialise instances
            if (!::m1.isInitialized || m1 == null) m1 = ImgClfacc88cls30ofFirst40.newInstance(context)
            if (!::m2.isInitialized || m2 == null) m2 = ImgClfacc84cls30ofMiddle40.newInstance(context)
            if (!::m3.isInitialized || m3 == null) m3 = ImgClfacc87cls30ofLast40.newInstance(context)
        }
        fun classifyImage(bmap: Bitmap, context : Context): String{
            img_bitmap = bmap

            setup(context)
//            Toast.makeText(context, "Inside clsImg", Toast.LENGTH_SHORT).show()
            val bitmap = Bitmap.createScaledBitmap(bmap, imgsize, imgsize, false)

            val byteBuffer = ByteBuffer.allocateDirect(4*imgsize*imgsize*3)
            byteBuffer.order(ByteOrder.nativeOrder())

            val intValues = IntArray(imgsize*imgsize)
            bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            var pixel = 0;
            for (i in 0..(imgsize-1)){
                for (j in 0..(imgsize-1)){
                    var value = intValues[pixel++] //stores RGB value in one
                    byteBuffer.putFloat(((value shr  16) and 0xff)*(1f/255)) //shr is right shift in kotlin... 0xff is in hexadecimal, accounts for 255
                    byteBuffer.putFloat(((value shr  8) and 0xff)*(1f/255))
                    byteBuffer.putFloat((value and 0xff)*(1f/255))
                }
            }

// Creates inputs for reference.
            val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 256, 256, 3), DataType.FLOAT32)
            inputFeature0.loadBuffer(byteBuffer)

// Runs model inference and gets result.
            val confidences = arrayOf(m1.process(inputFeature0).outputFeature0AsTensorBuffer.floatArray,
                m2.process(inputFeature0).outputFeature0AsTensorBuffer.floatArray,
                m3.process(inputFeature0).outputFeature0AsTensorBuffer.floatArray
            ) //FloatArray returns confidence values
            var maxInd = 0
            var maxConfidence = 0f
            for (i in 0..2){
                for (j in 0..(confidences[i].size-1)){
                    if (confidences[i][j] > maxConfidence){
                        maxConfidence = confidences[i][j]
                        maxInd = i*30 + j
                    }
                }
            }
//            Toast.makeText(context, "${classes[maxInd]}, confidence=$maxConfidence", Toast.LENGTH_LONG).show()
// Releases model resources if no longer used.
//            m1.close()
//            m2.close()
//            m3.close()
            return "${classes[maxInd]}\n(confidence=${(maxConfidence*100).toInt()}%)\n"
        }
    }
}