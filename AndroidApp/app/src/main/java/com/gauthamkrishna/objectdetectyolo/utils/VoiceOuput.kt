package com.gauthamkrishna.objectdetectyolo.utils

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.appcompat.app.AppCompatActivity
import com.gauthamkrishna.objectdetectyolo.R
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.io.IOException
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*

class VoiceOuput : AppCompatActivity() {
    private var tts: TextToSpeech? = null
    private var tflite: Interpreter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the TextToSpeech object
        tts = TextToSpeech(this) { status ->
            if (status != TextToSpeech.ERROR) {
                // Set the language to the default locale
                tts!!.language = Locale.getDefault()
            }
        }
// ithu net lendhu sutta code. Idk if it works or if it is correct. No. Ingaye error iruke da. Main activity ku. Alread
        // Load the TFLite model from assets folder.
        try {
            tflite = Interpreter(loadModelFile(this@VoiceOuput)) //spelling mistake class name
        } catch (e: IOException) {
            e.printStackTrace()
        }

        // Run the TFLite model and get the output
        val output = Array(1) {
            FloatArray(
                1
            )
        }
        val input = Array(1) {
            FloatArray(
                1
            )
        }
        input[0][0] = 1.0f
        tflite!!.run(input, output)

        // Convert the TFLite model output to speech using TextToSpeech
        val speechOutput = String.format(
            Locale.US, "The output is %.2f",
            output[0][0]
        )
        tts!!.speak(speechOutput, TextToSpeech.QUEUE_FLUSH, null) // why is it crossed?
    }//yes

    @Throws(IOException::class)
    private fun loadModelFile(context: Context): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel: FileChannel = inputStream.getChannel()
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    override fun onDestroy() {
        if (tts != null) {//logic correct ah?. Dei na summa tflite to voice nu chatgpt la poten. Project anupata?
            tts!!.stop()
            tts!!.shutdown()
        }//cable thedren. Phone ku. college full ah slack than. iru front desk la iruka pakren. Ente cable ile
        super.onDestroy()
    }
}
//kaasu ? nope. issue lam ile. Sound varle full ah iruku. antha sound ey varle.