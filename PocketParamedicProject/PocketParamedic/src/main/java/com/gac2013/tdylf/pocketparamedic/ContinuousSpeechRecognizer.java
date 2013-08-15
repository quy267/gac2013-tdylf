package com.gac2013.tdylf.pocketparamedic;


import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;

public class ContinuousSpeechRecognizer {

    private Context context;
    private SpeechRecognizer sr;
    //private ContinuousRecognitionListener listener = new ContinuousRecognitionListener();
    private static final String TAG = "ContinuousSpeechRecognizer";
    private Intent intent;

    private AudioManager audioManager;
    private RecognizedTextListener output;

    public ContinuousSpeechRecognizer(Context context) {
        this.context = context;
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    public void stopRecognition() {
        Log.e(TAG, "Stopping recognition");
        sr.stopListening();
        Log.d(TAG, "Listening stopped");
        sr.cancel();
        Log.d(TAG, "Speech recognizer cancelled");
    }

    public void setListener(RecognizedTextListener output) {
        this.output = output;
    }

    public void startRecognition() {
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toString());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "voice.recognition.test");

        intent.putExtra(RecognizerIntent.
                EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, new Long(20000));
        intent.putExtra(RecognizerIntent.
                EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, new Long(20000));
        intent.putExtra(RecognizerIntent.
                EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, new Long(20000));

        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
        Log.d(TAG, "Intent prepared");

        sr = SpeechRecognizer.createSpeechRecognizer(context);
        Log.d(TAG, "SpeechRecognizer created");
        sr.setRecognitionListener(new ContinuousRecognitionListener());
        Log.d(TAG, "Listener set up");
        sr.startListening(intent);
        Log.e(TAG, "Recognition started");
    }


    private void restartRecognition() {
        stopRecognition();
        startRecognition();
    }

    private class ContinuousRecognitionListener implements RecognitionListener {
        private boolean speechBegun;
        private Runnable runnable;
        private Handler handler = new Handler();

        public void onReadyForSpeech(Bundle params) {
            Log.d(TAG, "onReadyForSpeech");
            //tts.speak("foobar", TextToSpeech.QUEUE_ADD, null);

        /*private void playNotificationSound(int soundId) {

            AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
            float maxVolume = (float) audioManager
                    .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            // Is the sound loaded already?
            if (loaded)
                soundPool.play(soundId, maxVolume, maxVolume, 1, 0, 1f);
        }*/

            //speechBegun = false;
            runnable = new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "do the checkup");
                    //if (!speechBegun) {
                    restartRecognition();
                    //}
                }
            };
            handler.postDelayed(runnable, 10000);


            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
                Log.d(TAG, "Sound muted");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
                        Log.d(TAG, "Sound is on again");
                    }
                }, 500);
            }

        }

        public void onBeginningOfSpeech() {
            Log.d(TAG, "onBeginningOfSpeech");
            if (runnable != null) {
                handler.removeCallbacks(runnable);
                runnable = null;
            }
            //speechBegun = true;
        }

        public void onRmsChanged(float rmsdB) {
            //Log.d(TAG, "onRmsChanged");
        }

        public void onBufferReceived(byte[] buffer) {
            Log.d(TAG, "onBufferReceived");
        }

        public void onEndOfSpeech() {
            Log.d(TAG, "onEndOfSpeech");
            //restartRecognition();
        }

        @Override
        public void onError(int errorCode) {
            String message = getMessageForErrorCode(errorCode);

            if ((errorCode == SpeechRecognizer.ERROR_NO_MATCH)
                    || (errorCode == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                Log.e(TAG, "didn't recognize anything");
                // keep going
                //recognizeSpeechDirectly();
                sr.startListening(intent);

            } else if (errorCode == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
                Log.e(TAG, "stopping listening");
                sr.stopListening();
                Log.e(TAG, "cancelling listening");
                sr.cancel();
                Log.e(TAG, "destroying sr");
                sr.destroy();
                Log.e(TAG, "running listen");
                /*new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {*/
                sr.startListening(intent);
                   /* }
                }, 2000);*/
                Log.e(TAG, "done");
                //sr.startListening(intent);
            } else {
                Log.d(TAG,
                        "FAILED " + errorCode);
                //+ SpeechRecognitionUtil
                //.diagnoseErrorCode(errorCode));


                //mText.setText("An error occured: " + message);
            }

            Log.e(TAG, "error " + errorCode + ": " + message);

        }

        public void onResults(Bundle results) {
            Log.d(TAG, "onResults " + results);
            if (output != null)
                output.onResults(results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION));

            //sr.stopListening();
            //sr.cancel();
            //sr.destroy();
            //onClick(speakButton);
            //mText.setText("results: "+String.valueOf(data.size()));
            sr.startListening(intent);
        }

        public void onPartialResults(Bundle partialResults) {
            Log.d(TAG, "onPartialResults");
        }

        public void onEvent(int eventType, Bundle params) {
            Log.d(TAG, "onEvent " + eventType);
        }

    }

    private static String getMessageForErrorCode(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Audio recording error";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                message = "Client side error";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                message = "Insufficient permissions";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                message = "Network error";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                message = "Network timeout";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                message = "No match";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                message = "RecognitionService busy";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                message = "error from server";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                message = "No speech input";
                break;
            default:
                message = "Didn't understand, please try again.";
                break;
        }
        return message;
    }

    public interface RecognizedTextListener {
        void onResults(ArrayList<String> results);
    }
}