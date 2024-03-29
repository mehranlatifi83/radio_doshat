package ir.codelighthouse.radio_doshat.player;

import android.os.Handler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;

import ir.codelighthouse.radio_doshat.MainActivity;
import ir.codelighthouse.radio_doshat.R;
import ir.codelighthouse.ui.UIUpdater;

public class MediaManager {
	public static void checkForSongChange(MainActivity mainActivity) {
		final Handler handler = new Handler();
		Runnable runnable = () -> {
			if (mainActivity.isAppActive && !mainActivity.isVpnConected) {
				fetchCurrentSongName(mainActivity);
			}
			handler.postDelayed(() -> checkForSongChange(mainActivity), 10000);
		};
		handler.post(runnable);
	}

	private static void fetchCurrentSongName(MainActivity mainActivity) {
		new Thread(() -> {
			try {
				Document doc = Jsoup.connect(mainActivity.getString(R.string.url_radio_information)).get();
				String fullText = doc.text();
				int startIndex = fullText.lastIndexOf("Currently playing:") + "Currently playing:".length();
				int endIndex = fullText.indexOf("Support", startIndex);
				if (startIndex != -1 && endIndex != -1) {
					String songInformation = fullText.substring(startIndex, endIndex).trim();
					String[] parts = songInformation.split(" - Next song: ");
					String songName = parts[0];
					if (!songName.equals(mainActivity.lastSongName)) {
						mainActivity.lastSongName = songName;
						if (parts.length > 1) {
							String nextSongName = parts[1];
							mainActivity.runOnUiThread(() -> mainActivity.statusBar.setText(mainActivity.getString(R.string.now_playing_with_next_song, songName, nextSongName)));
							UIUpdater.showToast(mainActivity, mainActivity.getString(R.string.now_playing_with_next_song, songName, nextSongName));
						} else {
							mainActivity.runOnUiThread(() -> mainActivity.statusBar.setText(mainActivity.getString(R.string.now_playing, songName)));
							UIUpdater.showToast(mainActivity, mainActivity.getString(R.string.now_playing, songName));
						}
					}
				}
			} catch (IOException e) {
			}
		}).start();
	}
}
