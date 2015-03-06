package dev.blacksheep.sti_antitheft;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.ipaulpro.afilechooser.utils.FileUtils;

import dev.blacksheep.sti_antitheft.classes.SQLFunctions;

//https://github.com/iPaulPro/aFileChooser
public class FileChooserActivity extends SherlockActivity {
	private static final int REQUEST_CODE = 6384;
	ListView lvFiles;
	ArrayList<String> data = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_file_chooser);
		getSupportActionBar().setHomeButtonEnabled(true);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		lvFiles = (ListView) findViewById(R.id.lvFiles);
		new loadFileDatabase().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(Menu.NONE, 1, Menu.NONE, "Add").setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case 1:
			Intent getContentIntent = FileUtils.createGetContentIntent();
			Intent intent = Intent.createChooser(getContentIntent, "Select a file");
			startActivityForResult(intent, REQUEST_CODE);
			break;
		case android.R.id.home:
			finish();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE:
			if (resultCode == RESULT_OK) {
				final Uri uri = data.getData();
				String path = FileUtils.getPath(this, uri);
				SQLFunctions sql = new SQLFunctions(FileChooserActivity.this);
				sql.open();
				sql.insertFilePath(path);
				sql.close();
				Log.e("FILE PATH", path);
				new loadFileDatabase().execute();
			}
			break;
		}
	}

	private class loadFileDatabase extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... arg0) {
			SQLFunctions sql = new SQLFunctions(FileChooserActivity.this);
			sql.open();
			data = sql.loadFileList();
			sql.close();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			FileChooserActivity.this.runOnUiThread(new Runnable() {

				@Override
				public void run() {
					ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(FileChooserActivity.this, android.R.layout.simple_list_item_1, data);
					lvFiles.setAdapter(arrayAdapter);
					lvFiles.setOnItemClickListener(new OnItemClickListener() {

						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
							final SQLFunctions sql = new SQLFunctions(FileChooserActivity.this);
							sql.open();
							AlertDialog.Builder alert = new AlertDialog.Builder(FileChooserActivity.this);
							alert.setTitle("Passphrase");
							alert.setMessage("Enter passphrase to encrypt files with. It is recommended to encrypt different files with different passphrase.");
							final EditText input = new EditText(FileChooserActivity.this);
							input.setText(sql.getPasswordOfFiles(data.get(arg2)));
							alert.setView(input);
							alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									String value = input.getText().toString();

									sql.updatePassword(data.get(arg2), value);
									sql.close();
								}
							});

							alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
								}
							});

							alert.show();
						}
					});
					lvFiles.setOnItemLongClickListener(new OnItemLongClickListener() {

						@Override
						public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int arg2, long arg3) {
							DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									switch (which) {
									case DialogInterface.BUTTON_POSITIVE:
										SQLFunctions sql = new SQLFunctions(FileChooserActivity.this);
										sql.open();
										sql.deleteFilePath(data.get(arg2));
										sql.close();
										new loadFileDatabase().execute();
										break;
									case DialogInterface.BUTTON_NEGATIVE:
										break;
									}
								}
							};

							AlertDialog.Builder builder = new AlertDialog.Builder(FileChooserActivity.this);
							builder.setMessage("Delete file?").setTitle("Confirmation").setPositiveButton("Yes", dialogClickListener).setNegativeButton("No", dialogClickListener).show();
							return false;
						}
					});
				}
			});
		}

	}
}
