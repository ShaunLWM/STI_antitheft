package dev.blacksheep.sti_antitheft;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibrary;

import android.app.Application;
import android.util.Log;

public class LocationLibraryApplication extends Application {
	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("TestApplication", "onCreate()");
		LocationLibrary.showDebugOutput(true);

		try {
			LocationLibrary.initialiseLibrary(getBaseContext(), "dev.blacksheep.sti_antitheft");
		} catch (UnsupportedOperationException ex) {
			Log.d("TestApplication", "UnsupportedOperationException thrown - the device doesn't have any location providers");
		}
	}
}
