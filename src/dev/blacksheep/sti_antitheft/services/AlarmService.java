package dev.blacksheep.sti_antitheft.services;

import java.io.IOException;

import dev.blacksheep.sti_antitheft.R;
import dev.blacksheep.sti_antitheft.R.raw;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class AlarmService extends Service {
	private MediaPlayer mMediaPlayer;
	private final Handler mHandler = new Handler();

	int duration = 0;
	private static final int DEFAULT_ALARM_DURATION = 5;

	public AlarmService() {
	}

	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			String time = intent.getStringExtra("duration");
			duration = Integer.parseInt(time);
			Log.e("CUSTOM ALARM TIME", duration + "");
		} catch (Exception e) {

		}
		Log.e("AlarmService", "STARTING NOTIFICATION!");
		alarmNotification(this);
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		this.stopSelf();
		stopMediaPlayer();
		// stopForeground(true);
	}

	private void stopMediaPlayer() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	private void alarmNotification(Context context) {
		AudioManager am = (AudioManager) context
				.getSystemService(Context.AUDIO_SERVICE);

		int maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION);
		am.setStreamVolume(AudioManager.STREAM_NOTIFICATION, maxVolume,
				AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

		maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_RING);
		am.setStreamVolume(AudioManager.STREAM_RING, maxVolume,
				AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

		AssetFileDescriptor afd = getResources().openRawResourceFd(R.raw.alarm);

		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
		mMediaPlayer.setLooping(true);
		try {
			mMediaPlayer.setDataSource(afd.getFileDescriptor(),
					afd.getStartOffset(), afd.getLength());
			mMediaPlayer.prepare();
		} catch (IOException e) {
			stopForeground(true);
			mMediaPlayer.release();
			return;
		}
		Log.e("AlarmService", "STARTING MEDIAPLAYER!!!");
		mMediaPlayer.start();
		if (duration == 0) {
			duration = DEFAULT_ALARM_DURATION;
		}

		Log.e("DURATION", "duration : " + duration * 1000);
		mHandler.postDelayed(new Runnable() {
			@Override
			public void run() {
				Log.e("STOPPING", "STOPPING!");
				stopMediaPlayer();
				stopSelf();
			}
		}, duration * 1000);
	}
}