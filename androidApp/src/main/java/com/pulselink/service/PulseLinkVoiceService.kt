package com.pulselink.service

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class PulseLinkVoiceService : Service(), RecognitionListener {

    @Inject lateinit var alertRouter: AlertRouter

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var recognizerIntent: Intent

    override fun onCreate() {
        super.onCreate()
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            stopSelf()
            return
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this).apply {
            setRecognitionListener(this@PulseLinkVoiceService)
        }
        recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        startListening()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    private fun startListening() {
        try {
            speechRecognizer?.startListening(recognizerIntent)
        } catch (t: Throwable) {
            Log.e(TAG, "Unable to start listening", t)
            restartWithDelay()
        }
    }

    private fun restartWithDelay() {
        scope.launch {
            kotlinx.coroutines.delay(750)
            startListening()
        }
    }

    override fun onDestroy() {
        speechRecognizer?.destroy()
        scope.cancel()
        super.onDestroy()
    }

    override fun onReadyForSpeech(params: Bundle?) {}

    override fun onBeginningOfSpeech() {}

    override fun onRmsChanged(rmsdB: Float) {}

    override fun onBufferReceived(buffer: ByteArray?) {}

    override fun onEndOfSpeech() {
        restartWithDelay()
    }

    override fun onError(error: Int) {
        restartWithDelay()
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
        scope.launch {
            matches.forEach { phrase ->
                alertRouter.onPhraseDetected(phrase)
            }
        }
        restartWithDelay()
    }

    override fun onPartialResults(partialResults: Bundle?) {
        val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION) ?: return
        scope.launch {
            matches.forEach { phrase ->
                alertRouter.onPhraseDetected(phrase)
            }
        }
    }

    override fun onEvent(eventType: Int, params: Bundle?) {}

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val TAG = "PulseLinkVoiceService"
    }
}
