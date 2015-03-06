package dev.blacksheep.sti_antitheft.services;

import java.io.File;
import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class AudioRecorderService extends Service {
	MediaRecorder recorder;
	String address, fileName;

	public void startRecording() {
		recorder = new MediaRecorder();
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		//recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		recorder.setOutputFile(getFilename());
		recorder.setOnErrorListener(new OnErrorListener() {

			@Override
			public void onError(MediaRecorder arg0, int arg1, int arg2) {

			}
		});
		try {
			recorder.prepare();
			recorder.start();
			Log.e("AUDIO", "RECORDING!!!!");
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					Log.e("AUDIO", "STOPPING");
					stopRecording();
					startService(new Intent(getApplicationContext(), BackupService.class).putExtra("audio", true).putExtra("audioFile", fileName).putExtra("address", address));
					stopSelf();
				}
			}, 10000);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void stopRecording() {
		if (recorder != null) {
			recorder.stop();
			recorder.reset();
			recorder.release();
			recorder = null;
		}
	}

	private String getFilename() {
		fileName = System.currentTimeMillis() + ".mp3";
		Log.e("AudioRecord", Environment.getExternalStorageDirectory().toString() + File.separator + fileName);
		return Environment.getExternalStorageDirectory().toString() + File.separator + fileName;
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		address = intent.getExtras().getString("address");
		Log.e("ADDRESS", address);
		startRecording();
		return 1;
	}
}