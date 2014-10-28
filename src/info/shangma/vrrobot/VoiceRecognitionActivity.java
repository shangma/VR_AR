package info.shangma.vrrobot;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

public class VoiceRecognitionActivity extends Activity implements RecognitionListener {
	
	private TextView returnedText;
	
	private SpeechRecognizer speech = null;
	private Intent recognizerIntent;
	private String LOG_TAG = "VoiceRecognitionActivity";
	
	private Handler customHandler = new Handler();
	
	private long detectTime = 0L;
	private long sleepTime = 0L;
	private long timeInMilliseconds = 0L;
	
	private static final int SLEEPING = -1;
	private static final int DETECTING = 0;
	private static final int PROCESSING = 1;
	
	private static final int DETECTING_INTERVAL = 6000;
	private static final int SLEEPING_INTERVAL = 2000;
	
	private int currentStatus = SLEEPING;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_voice_recognition);
		
		returnedText = (TextView) findViewById(R.id.textView1);
		
		// initialize the SpeechRecognizer object
		speech = SpeechRecognizer.createSpeechRecognizer(this);
		speech.setRecognitionListener(this);
		recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,
				"en");
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE,
				this.getPackageName());
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
		recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);
				
		speech.startListening(recognizerIntent);
		
		detectTime = SystemClock.uptimeMillis();
		customHandler.postDelayed(updateTimerThread, 0);
		currentStatus = DETECTING;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (speech != null) {
			speech.destroy();
			Log.i(LOG_TAG, "destroy");
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.voice_recognition, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, "onReadyForSpeech");
	}

	@Override
	public void onBeginningOfSpeech() {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, "onBeginningOfSpeech");
		
		currentStatus = PROCESSING;
		customHandler.removeCallbacks(updateTimerThread);
		timeInMilliseconds = 0;
	}

	@Override
	public void onRmsChanged(float rmsdB) {
		// TODO Auto-generated method stub
		//Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
	}

	@Override
	public void onBufferReceived(byte[] buffer) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, "onBufferReceived: " + buffer);
	}

	@Override
	public void onEndOfSpeech() {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, "onEndOfSpeech");
	}

	@Override
	public void onError(int error) {
		// TODO Auto-generated method stub
		String errorMessage = getErrorText(error);
		Log.d(LOG_TAG, "FAILED " + errorMessage);
		returnedText.setText(errorMessage);
	}

	@Override
	public void onResults(Bundle results) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, "onResults");
		ArrayList<String> matches = results
				.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
		String text = "";
		if (matches.size() != 0) {
			for (String result : matches)
				text += result + "\n";

			returnedText.setText(text);
		}
		
		
		detectTime = SystemClock.uptimeMillis();
		
		customHandler.postDelayed(updateTimerThread, 0);
		currentStatus = DETECTING;
	}

	@Override
	public void onPartialResults(Bundle partialResults) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, "onPartialResults");
	}

	@Override
	public void onEvent(int eventType, Bundle params) {
		// TODO Auto-generated method stub
		Log.i(LOG_TAG, "onEvent");
	}
	
	public static String getErrorText(int errorCode) {
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
	
	private Runnable updateTimerThread = new Runnable() {
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (currentStatus == DETECTING) {
				timeInMilliseconds = SystemClock.uptimeMillis() - detectTime;
				
				if (timeInMilliseconds > DETECTING_INTERVAL) {
					
					//Log.i(LOG_TAG, "current time 2 " + SystemClock.uptimeMillis());
					Log.i(LOG_TAG, "interval: " + timeInMilliseconds);
					speech.cancel();
					
					currentStatus = SLEEPING;
					sleepTime = SystemClock.uptimeMillis();
					Log.i(LOG_TAG, "Start sleeping at " + sleepTime);
				}
				
			} else if (currentStatus == SLEEPING) {
				
				timeInMilliseconds = SystemClock.uptimeMillis() - sleepTime;
				if (timeInMilliseconds > SLEEPING_INTERVAL) {
					
					speech.startListening(recognizerIntent);
					currentStatus = DETECTING;
					detectTime = SystemClock.uptimeMillis();
					
					Log.i(LOG_TAG, "Start detecting at " + detectTime);
				}
			}
			
			//Log.i(LOG_TAG, "Counting");
			customHandler.postDelayed(this, 0);
		}
	};
}
