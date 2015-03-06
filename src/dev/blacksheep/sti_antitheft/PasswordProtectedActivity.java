package dev.blacksheep.sti_antitheft;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.securepreferences.SecurePreferences;

import dev.blacksheep.sti_antitheft.classes.Utils;

public class PasswordProtectedActivity extends Activity {
	SecurePreferences sp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.password_protected_activity);
		sp = new SecurePreferences(PasswordProtectedActivity.this);
		final String password = sp.getString("password", "");
		if (password.equals("")) {
			showFirstrun();
		}
		final EditText etPassword = (EditText) findViewById(R.id.etPassword);
		Button bLogin = (Button) findViewById(R.id.bLogin);
		bLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SecurePreferences sp = new SecurePreferences(PasswordProtectedActivity.this);
				String password = sp.getString("password", "");
				if (etPassword.getText().toString().equals(password)) {
					startActivity(new Intent(PasswordProtectedActivity.this, MainActivity.class));
					finish();
				}
			}
		});
	}

	public void showFirstrun() {
		AlertDialog.Builder alert = new AlertDialog.Builder(PasswordProtectedActivity.this);

		alert.setTitle("Set Password");
		alert.setMessage(Html.fromHtml("This is your master password for login and for encrypting files which do not have its own password.<br><br>Additionally, this is your SIM info. Any changes to your SIM card will trigger an alarm.<br><b>" + new Utils(PasswordProtectedActivity.this).tempSimInfo() + "</b>"));
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = input.getText().toString();
				sp.edit().putString("password", value).commit();
				new Utils(PasswordProtectedActivity.this).storeSimInfo();
			}
		});
		alert.show();
	}

}
