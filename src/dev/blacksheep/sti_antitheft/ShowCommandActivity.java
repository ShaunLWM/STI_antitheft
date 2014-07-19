package dev.blacksheep.sti_antitheft;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

public class ShowCommandActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.show_command_activity);
		TextView textView1 = (TextView) findViewById(R.id.textView1);
		textView1.setText(Html.fromHtml(getResources().getString(R.string.commands)));
	}
}
