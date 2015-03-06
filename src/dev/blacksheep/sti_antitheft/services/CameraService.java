package dev.blacksheep.sti_antitheft.services;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Window;

import dev.blacksheep.sti_antitheft.R;

public class CameraService extends SherlockActivity implements SurfaceHolder.Callback {
	private SurfaceView sv;
	private Bitmap bmp;
	private SurfaceHolder sHolder;
	private Camera mCamera;
	private Parameters parameters;
	String address;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		Intent i = getIntent();
		address = i.getStringExtra("address");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera);
		sv = (SurfaceView) findViewById(R.id.svCameraPreview);
		sHolder = sv.getHolder();
		sHolder.addCallback(this);
		sHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		Log.e("Cam", "END");
	}

	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		Log.e("Cam", "change");
		parameters = mCamera.getParameters();
		try {
			parameters.set("camera-id", 2);
		} catch (Exception e) {

		}
		mCamera.setParameters(parameters);
		mCamera.startPreview();
		final AudioManager manager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		final int tempSound = manager.getStreamVolume(AudioManager.STREAM_SYSTEM);
		manager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		final Camera.PictureCallback mCall = new Camera.PictureCallback() {
			@Override
			public void onPictureTaken(byte[] data, Camera camera) {
				Log.e("Cam", "Callback");
				bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
				String root = Environment.getExternalStorageDirectory().toString();
				File myDir = new File(root + File.separator);
				String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date()) + "_Dropbox";
				String fname = timeStamp + ".jpg";
				File file = new File(myDir, fname);
				if (file.exists()) {
					file.delete();
				}
				try {
					FileOutputStream out = new FileOutputStream(file);
					bmp.compress(Bitmap.CompressFormat.JPEG, 90, out);
					out.flush();
					out.close();
					manager.setStreamVolume(AudioManager.STREAM_SYSTEM, tempSound, AudioManager.FLAG_ALLOW_RINGER_MODES);
					startService(new Intent(CameraService.this, BackupService.class).putExtra("camera", true).putExtra("picname", fname).putExtra("address", address));
					finish();
				} catch (Exception e) {
					e.printStackTrace();
					finish();
				}
			}
		};
		mCamera.autoFocus(new AutoFocusCallback() {

			@Override
			public void onAutoFocus(boolean success, Camera camera) {
				mCamera.takePicture(null, null, mCall);
			}
		});

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.e("Cam", "create - " + Build.VERSION.SDK_INT);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
			Camera.CameraInfo info = new Camera.CameraInfo();
			for (int i = 0; i < Camera.getNumberOfCameras(); i++) {
				Camera.getCameraInfo(i, info);
				if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
					mCamera = Camera.open(i);
				}
			}
		}
		if (mCamera == null) {
			mCamera = Camera.open();
		}
		try {
			mCamera.setPreviewDisplay(holder);
		} catch (Exception exception) {
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		try {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}