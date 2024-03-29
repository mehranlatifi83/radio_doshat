package ir.codelighthouse.radio_doshat.player;

import android.net.Uri;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;

import ir.codelighthouse.radio_doshat.MainActivity;
import ir.codelighthouse.radio_doshat.R;
import ir.codelighthouse.ui.UIUpdater;

public class PlayerManager {
	public static void initializePlayer(final MainActivity mainActivity) {
		mainActivity.player = new ExoPlayer.Builder(mainActivity).build();
		playUrl(mainActivity);
		mainActivity.player.addListener(new Player.Listener() {
			@Override
			public void onPlaybackStateChanged(int state) {
				updatePlaybackState(mainActivity, state);
			}

			@Override
			public void onPlayerError(PlaybackException error) {
				if (!mainActivity.isVpnConected)
					UIUpdater.showErrorMessage(mainActivity, mainActivity.getString(R.string.server_error));
			}
		});
	}

	public static void updatePlaybackState(MainActivity mainActivity, int state) {
		switch (state) {
			case Player.STATE_BUFFERING:
				handleStateBuffering(mainActivity);
				break;
			case Player.STATE_READY:
				handleStateReady(mainActivity);
				break;
			case Player.STATE_ENDED:
				handleStateEnded(mainActivity);
				break;
			case Player.STATE_IDLE:
				handleStateIdle(mainActivity);
				break;
		}
	}

	public static void handleStateBuffering(MainActivity mainActivity) {
		UIUpdater.showToast(mainActivity, mainActivity.getString(R.string.loading));
	}

	public static void playUrl(MainActivity mainActivity) {
		MediaItem mediaItem = MediaItem.fromUri(Uri.parse(mainActivity.getString(R.string.url_stream)));
		mainActivity.player.setMediaItem(mediaItem);
		mainActivity.player.prepare();
	}

	public static void togglePlayback(MainActivity mainActivity) {
		if (mainActivity.player == null) return;

		if (mainActivity.player.isPlaying()) {
			mainActivity.player.pause();
			mainActivity.playButton.setText(mainActivity.getString(R.string.play));
		} else {
			mainActivity.player.play();
			mainActivity.playButton.setText(mainActivity.getString(R.string.pause));
		}
	}

	public static void handleStateReady(MainActivity mainActivity) {
		mainActivity.playButton.setText(mainActivity.player.isPlaying() ? mainActivity.getString(R.string.pause) : mainActivity.getString(R.string.play));
	}

	public static void handleStateEnded(MainActivity mainActivity) {
		mainActivity.playButton.setText(mainActivity.getString(R.string.play));
		playUrl(mainActivity);
		togglePlayback(mainActivity);
	}

	public static void handleStateIdle(MainActivity mainActivity) {
		mainActivity.playButton.setText(mainActivity.getString(R.string.play));
	}
}
