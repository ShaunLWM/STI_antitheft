package dev.blacksheep.sti_antitheft;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.securepreferences.SecurePreferences;

import dev.blacksheep.sti_antitheft.classes.Utils;

public class PopupMessageActivity extends Activity {

	int unlockTries = 0;
	Handler mHandler = new Handler();
	asyncPoll mTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.popup_message_activity);
		((TextView) findViewById(R.id.tvMessage)).setText(new Utils(PopupMessageActivity.this).getPopupMessageOnBoot());

		SecurePreferences sp = new SecurePreferences(PopupMessageActivity.this);
		// startPoll();

		final String password = sp.getString("password", "");
		final Button bUnlock = (Button) findViewById(R.id.bUnlock);
		bUnlock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(PopupMessageActivity.this);
				alert.setTitle("Input Password");
				alert.setMessage("Input the password to unlock this phone.\nYou have " + (Consts.MAX_TRIES - unlockTries) + " tries left.");
				final EditText input = new EditText(PopupMessageActivity.this);
				alert.setView(input);
				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (input.getText().toString().equals(password)) {
							new Utils(PopupMessageActivity.this).setPopupOnBoot(false, "");
							if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED) {
								mTask.cancel(true);
							}
							finish();
						} else {
							unlockTries++;
							if (unlockTries == Consts.MAX_TRIES) {
								bUnlock.setVisibility(View.GONE);
							}
						}
					}
				});
				alert.show();
			}
		});
		mTask = new asyncPoll();
		mTask.execute();
	}

	private class asyncPoll extends AsyncTask<Void, Void, Void> {
		boolean stop = false;

		@Override
		protected Void doInBackground(Void... params) {
			while (!stop && !isCancelled()) {
				if (!new Utils(PopupMessageActivity.this).getPopupOnBoot()) {
					stop = true;
					cancel(true);
				} else {
					try {
						Log.e("SLEEPING", "SLEEPING!!!!");
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			PopupMessageActivity.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					if (!isCancelled()) {
						if (stop) {
							finish();
						}
					}
				}
			});
		}

	}

	private void startPoll() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(10000);
						Log.e("PopupMessage", "Checking unlock..");
						if (!new Utils(PopupMessageActivity.this).getPopupOnBoot()) {
							mHandler.removeCallbacks(this);
							finish();
							break;
						}
						Log.e("PopupMessage", "Nope. Still lock");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	@Override
	public void onBackPressed() {
		return;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.e("onDestroy", "onDestroy");
		if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED) {
			mTask.cancel(true);
		}

		if (new Utils(PopupMessageActivity.this).getPopupOnBoot()) {
			Intent mStartActivity = new Intent(PopupMessageActivity.this, PopupMessageActivity.class);
			int mPendingIntentId = 123456;
			PendingIntent mPendingIntent = PendingIntent.getActivity(PopupMessageActivity.this, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager mgr = (AlarmManager) PopupMessageActivity.this.getSystemService(Context.ALARM_SERVICE);
			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
			System.exit(0);
		} else {
			Log.e("NOT OPENING", "NOT OPENING");
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		if (mTask != null && mTask.getStatus() != AsyncTask.Status.FINISHED) {
			mTask.cancel(true);
		}
		Log.e("onPause", "onPause");
		if (new Utils(PopupMessageActivity.this).getPopupOnBoot()) {
			Intent mStartActivity = new Intent(PopupMessageActivity.this, PopupMessageActivity.class);
			int mPendingIntentId = 123456;
			PendingIntent mPendingIntent = PendingIntent.getActivity(PopupMessageActivity.this, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
			AlarmManager mgr = (AlarmManager) PopupMessageActivity.this.getSystemService(Context.ALARM_SERVICE);
			mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
			System.exit(0);
		} else {
			Log.e("NOT OPENING", "NOT OPENING");
		}
	}
}
