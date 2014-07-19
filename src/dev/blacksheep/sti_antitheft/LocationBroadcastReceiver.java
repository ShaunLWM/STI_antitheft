package dev.blacksheep.sti_antitheft;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.littlefluffytoys.littlefluffylocationlibrary.LocationInfo;
import com.littlefluffytoys.littlefluffylocationlibrary.LocationLibraryConstants;

public class LocationBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.d("LocationBroadcastReceiver", "onReceive: received location update");

		final LocationInfo locationInfo = (LocationInfo) intent.getSerializableExtra(LocationLibraryConstants.LOCATION_BROADCAST_EXTRA_LOCATIONINFO);
		Notification notification = new Notification(R.drawable.ic_launcher, "Locaton updated " + locationInfo.getTimestampAgeInSeconds() + " seconds ago", System.currentTimeMillis());

		Intent contentIntent = new Intent(context, MainActivity.class);
		PendingIntent contentPendingIntent = PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		notification.setLatestEventInfo(context, "Location update broadcast received", "Timestamped " + LocationInfo.formatTimeAndDay(locationInfo.lastLocationUpdateTimestamp, true),
				contentPendingIntent);

		// Trigger the notification.
		((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(1234, notification);

	}
}