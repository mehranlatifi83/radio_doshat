package ir.codelighthouse.radio_doshat;

import androidx.appcompat.app.AppCompatActivity;
import android.content.BroadcastReceiver;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.exoplayer2.ExoPlayer;

import ir.codelighthouse.internet.ConnectivityMonitor;
import ir.codelighthouse.ui.UIUpdater;

public class MainActivity extends AppCompatActivity {

	public ExoPlayer player;
	public Button playButton;
	public TextView statusBar;
	public String lastSongName = "";
	public boolean isAppActive = true;
	public boolean isVpnConected = false;
	public BroadcastReceiver networkChangeReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		UIUpdater.initializeUIComponents(this);
		requestDisableBatteryOptimization();
		ConnectivityMonitor.setupNetworkChangeReceiver(this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		ConnectivityMonitor.registerNetworkChangeReceiver(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		ConnectivityMonitor.unregisterNetworkChangeReceiver(this);
	}

	private void requestDisableBatteryOptimization() {
		if (shouldShowBatteryOptimizationDialog()) {
			UIUpdater.showBatteryOptimizationDialog(this);
		}
	}

	private boolean shouldShowBatteryOptimizationDialog() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isIgnoringBatteryOptimizations();
	}

	private boolean isIgnoringBatteryOptimizations() {
		String packageName = getPackageName();
		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		return pm.isIgnoringBatteryOptimizations(packageName);
	}

	@Override
	protected void onPause() {
		super.onPause();
		isAppActive = false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		isAppActive = true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (player != null) {
			player.stop();
			player.release();
		}
	}

	public void openSupportLink(View view) {
		UIUpdater.openLink(this, getString(R.string.url_support));
	}

	public void openTelegramLink(View view) {
		UIUpdater.openLink(this, getString(R.string.url_telegram));
	}

	public void openInstagramLink(View view) {
		UIUpdater.openLink(this, getString(R.string.url_instagram));
	}
}
