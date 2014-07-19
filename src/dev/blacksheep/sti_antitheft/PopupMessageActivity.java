package dev.blacksheep.sti_antitheft;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.securepreferences.SecurePreferences;

public class PopupMessageActivity extends Activity {

	int unlockTries = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.popup_message_activity);
		Intent i = getIntent();
		((TextView) findViewById(R.id.tvMessage)).setText(i.getStringExtra("message"));
		SecurePreferences sp = new SecurePreferences(PopupMessageActivity.this);
		final String password = sp.getString("password", "");
		final Button bUnlock = (Button) findViewById(R.id.bUnlock);
		bUnlock.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(PopupMessageActivity.this);
				alert.setTitle("Input Password");
				alert.setMessage("Input the password to unlock this phone.\nYou have " + (Consts.MAX_TRIES - unlockTries) + " left.");
				final EditText input = new EditText(PopupMessageActivity.this);
				alert.setView(input);
				alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (input.getText().toString().equals(password)) {
							new Utils(PopupMessageActivity.this).setPopupOnBoot(false, "");
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
	}

	@Override
	public void onBackPressed() {
		return;
	}
}
