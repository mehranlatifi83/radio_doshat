package ir.codelighthouse.radio_doshat;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import ir.codelighthouse.radio_doshat.R;

public class MainActivity extends AppCompatActivity {

	private ExoPlayer player;
	private Button playButton;
	private TextView statusBar;
	private String lastSongName = "";
	private boolean isAppActive = true;
	private boolean isVpnConected = false;
	private BroadcastReceiver networkChangeReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		initializeUIComponents();
		requestDisableBatteryOptimization();
		setupNetworkChangeReceiver();
	}

	private void setupNetworkChangeReceiver() {
		networkChangeReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (checkInternetConnection()) {
					checkForVpnIsConected();
					if (! isVpnConected) {
						if (player != null && player.isPlaying()) {
							playButton.setText(getString(R.string.pause));
						} else {
							initializePlayer();
							checkForSongChange();
							playButton.setVisibility(View.VISIBLE);
							statusBar.setText("");
						}
					}
				} else {
					playButton.setVisibility(View.GONE);
					showErrorMessage(getString(R.string.internet_error));
					if (player != null && player.isPlaying()) {
						player.pause();
					}
				}
			}
		};
	}

	private void initializeUIComponents() {
		playButton = findViewById(R.id.playButton);
		playButton.setVisibility(View.GONE);
		statusBar = findViewById(R.id.statusBar);
		playButton.setOnClickListener(v -> togglePlayback());
	}

	@Override
	protected void onStart() {
		super.onStart();
		registerNetworkChangeReceiver();
	}

	private void registerNetworkChangeReceiver() {
		registerReceiver(networkChangeReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterNetworkChangeReceiver();
	}

	private void unregisterNetworkChangeReceiver() {
		unregisterReceiver(networkChangeReceiver);
	}

	private boolean checkInternetConnection() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
	}

	private void initializePlayer() {
		player = new ExoPlayer.Builder(this).build();
		playUrl();
		player.addListener(new Player.Listener() {
			@Override
			public void onPlaybackStateChanged(int state) {
				updatePlaybackState(state);
			}

			@Override
			public void onPlayerError(PlaybackException error) {
				if (! isVpnConected)
					showErrorMessage(getString(R.string.server_error));
			}
		});
	}

	private void updatePlaybackState(int state) {
		switch (state) {
			case Player.STATE_BUFFERING:
				handleStateBuffering();
				break;
			case Player.STATE_READY:
				handleStateReady();
				break;
			case Player.STATE_ENDED:
				handleStateEnded();
				break;
			case Player.STATE_IDLE:
				handleStateIdle();
				break;
		}
	}

	private void handleStateBuffering() {
		showToast(getString(R.string.loading));
	}

	private void handleStateReady() {
		playButton.setText(player.isPlaying() ? getString(R.string.pause) : getString(R.string.play));
	}

	private void handleStateEnded() {
		playButton.setText(getString(R.string.play));
		playUrl();
		togglePlayback();
	}

	private void handleStateIdle() {
		playButton.setText(getString(R.string.play));
	}

	private void playUrl() {
		MediaItem mediaItem = MediaItem.fromUri(Uri.parse(getString(R.string.url_stream)));
		player.setMediaItem(mediaItem);
		player.prepare();
	}

	private void togglePlayback() {
		if (player == null) return;

		if (player.isPlaying()) {
			player.pause();
			playButton.setText(getString(R.string.play));
		} else {
			player.play();
			playButton.setText(getString(R.string.pause));
		}
	}

	private void checkForSongChange() {
		final Handler handler = new Handler();
		Runnable runnable = () -> {
			if (isAppActive && ! isVpnConected) {
				fetchCurrentSongName();
			}
			handler.postDelayed(this::checkForSongChange, 10000);
		};
		handler.post(runnable);
	}

	private void checkForVpnIsConected() {
		final Handler handler = new Handler();
		Runnable runnable = () -> {
			if (isAppActive) {
				fetchIsUserInIran();
			}
			handler.postDelayed(this::checkForVpnIsConected, 15000);
		};
		handler.post(runnable);
	}

	private void fetchCurrentSongName() {
		new Thread(() -> {
			try {
				Document doc = Jsoup.connect(getString(R.string.url_radio_information)).get();
				String fullText = doc.text();
				int startIndex = fullText.lastIndexOf("Currently playing:") + "Currently playing:".length();
				int endIndex = fullText.indexOf("Support", startIndex);
				if (startIndex != -1 && endIndex != -1) {
					String songInformation = fullText.substring(startIndex, endIndex).trim();
					String[] parts = songInformation.split(" - Next song: ");
					String songName = parts[0];
					if (!songName.equals(lastSongName)) {
						lastSongName = songName;
						if (parts.length > 1) {
							String nextSongName = parts[1];
							runOnUiThread(() -> statusBar.setText(getString(R.string.now_playing_with_next_song, songName, nextSongName)));
							showToast(getString(R.string.now_playing_with_next_song, songName, nextSongName));
						} else {
							runOnUiThread(() -> statusBar.setText(getString(R.string.now_playing, songName)));
							showToast(getString(R.string.now_playing, songName));
						}
					}
				}
			} catch (IOException e) {
			}
		}).start();
	}

	private void fetchIsUserInIran() {
		new Thread(() -> {
			try {
				// ارسال درخواست به ip-api.com و دریافت پاسخ به صورت متنی
				String json = Jsoup.connect(getString(R.string.url_get_country)).ignoreContentType(true).execute().body();
				// تبدیل رشته پاسخ به یک شیء JSONObject برای تجزیه آسان
				JSONObject jsonObject = new JSONObject(json);

				// استخراج مقدار کشور از JSON
				String country = jsonObject.getString("country");
				// بررسی اینکه آیا کشور ایران است یا خیر
				if (isVpnConected == (country.equals("Iran"))) {
					if (!country.equals("Iran")) {
						runOnUiThread(() -> playButton.setVisibility(View.GONE));
						runOnUiThread(() -> player.stop());
						showErrorMessage(getString(R.string.vpn_error));
						isVpnConected = true;
					} else {
						runOnUiThread(() -> statusBar.setText(""));
						runOnUiThread(() -> playUrl());
						runOnUiThread(() -> playButton.setVisibility(View.VISIBLE));
						isVpnConected = false;
					}
				}
			} catch (Exception e) {
			}
		}).start();
	}

	private void requestDisableBatteryOptimization() {
		if (shouldShowBatteryOptimizationDialog()) {
			showBatteryOptimizationDialog();
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

	private void showBatteryOptimizationDialog() {
		new AlertDialog.Builder(this)
				.setTitle(getString(R.string.battery_optimization_title))
				.setMessage(getString(R.string.battery_optimization_message))
				.setPositiveButton(getString(R.string.ok_button), (dialog, which) -> navigateToBatteryOptimizationSettings())
				.setNegativeButton(getString(R.string.cancel_button), null)
				.show();
	}

	private void navigateToBatteryOptimizationSettings() {
		Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
				.setData(Uri.parse("package:" + getPackageName()));
		startActivity(intent);
	}

	private void showErrorMessage(String message) {
		runOnUiThread(() -> {
			statusBar.setText(message);
			Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
		});
	}

	private void showToast(String message) {
		runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
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
	private void openLink(String url) {
		Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
		startActivity(browserIntent);
	}
	public void openSupportLink(View view) {
		openLink(getString(R.string.url_support));
	}

	public void openTelegramLink(View view) {
		openLink(getString(R.string.url_telegram));
	}

	public void openInstagramLink(View view) {
		openLink(getString(R.string.url_instagram));
	}
}
