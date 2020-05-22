package com.sust.sustcast;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.audio.AudioListener;
import com.google.android.exoplayer2.audio.AudioRendererEventListener;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import wseemann.media.FFmpegMediaMetadataRetriever;

public class StreamFragment extends Fragment implements Player.EventListener {

    boolean isPlaying;
    private SimpleExoPlayer exoPlayer;
    private Unbinder unbinder;
    private Button bPlay;
    private String TAG = "StreamFrag";
    String iceURL;
    FFmpegMediaMetadataRetriever fmmr;
    public StreamFragment() {
        // Required empty public constructor
    }


    public static StreamFragment newInstance() {
        StreamFragment fragment = new StreamFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_stream, container, false);
        bPlay = rootView.findViewById(R.id.button_stream);
        unbinder = ButterKnife.bind(this, rootView);
        isPlaying = false;
        bPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isPlaying == false && exoPlayer.getPlayWhenReady() == true) { // should stop
                    Log.i("CASE => ", "STOP " + isPlaying + " " + exoPlayer.getPlayWhenReady());
                    exoPlayer.setPlayWhenReady(false);
                    exoPlayer.getPlaybackState();
                    Drawable img = bPlay.getContext().getResources().getDrawable(R.drawable.pause_button);
                    bPlay.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                } else if (isPlaying == true && exoPlayer.getPlayWhenReady() == false) { //should play
                    Log.i("CASE => ", "PLAY" + isPlaying + " " + exoPlayer.getPlayWhenReady());
                    exoPlayer.setPlayWhenReady(true);
                    exoPlayer.getPlaybackState();
                    Drawable img = bPlay.getContext().getResources().getDrawable(R.drawable.play_button);
                    bPlay.setCompoundDrawablesWithIntrinsicBounds(img, null, null, null);
                } else if (exoPlayer.getPlayWhenReady() == true && isPlaying == true) {  //restart
                    Log.i("CASE => ", "RESTART" + isPlaying + " " + exoPlayer.getPlayWhenReady());
                    exoPlayer.release();
                    exoPlayer.stop();
                    exoPlayer.setPlayWhenReady(true);

                }

                isPlaying = !isPlaying;

            }
        });

        getPlayer();
        getAudioListener();
        getMetadata();

        return rootView;
    }


    private void getPlayer() {
        iceURL = getString(R.string.ice_stream);
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        final ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        TrackSelection.Factory trackSelectionFactory =
                new AdaptiveTrackSelection.Factory(bandwidthMeter);
        TrackSelector defaultTrackSelector =
                new DefaultTrackSelector(trackSelectionFactory);
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(
                getContext(),
                Util.getUserAgent(getContext(), "SUSTCast"),
                defaultBandwidthMeter);
        MediaSource mediaSource = new ExtractorMediaSource(
                Uri.parse(iceURL),
                dataSourceFactory,
                extractorsFactory,
                new Handler(), Throwable::printStackTrace);

        exoPlayer = ExoPlayerFactory.newSimpleInstance(getContext(), defaultTrackSelector);
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
        exoPlayer.addAudioListener(new AudioListener() {
            @Override
            public void onAudioSessionId(int audioSessionId) {
                Log.i("AUDIO => ", String.valueOf(audioSessionId));
            }
        });

    }

    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        fmmr.release();
        exoPlayer.release();
    }

    public void getMetadata() {
        fmmr = new FFmpegMediaMetadataRetriever();
        fmmr.setDataSource(iceURL);
        try {
            for (int i = 0; i < Constantss.METADATA_KEYS.length; i++) {
                String key = Constantss.METADATA_KEYS[i];
                String value = fmmr.extractMetadata(key);

                if (value != null) {
                    Log.i("METADATA => ", "Key: " + key + " Value: " + value);
                } else {
                    Log.i("METADATA => ", "Key: " + key + " Value: " + "NONE");

                }
            }
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }

    }

    public void getAudioListener() {
        exoPlayer.addAudioDebugListener(new AudioRendererEventListener() {
            @Override
            public void onAudioEnabled(DecoderCounters counters) {
                Log.i("LINDA1 => ", String.valueOf(exoPlayer.getAudioAttributes()));

            }

            @Override
            public void onAudioSessionId(int audioSessionId) {
                Log.i("LINDA2 => ", String.valueOf(exoPlayer.getAudioAttributes()));

            }

            @Override
            public void onAudioDecoderInitialized(String decoderName, long initializedTimestampMs, long initializationDurationMs) {
                Log.i("LINDA3 => ", String.valueOf(exoPlayer.getAudioAttributes()));

            }

            @Override
            public void onAudioInputFormatChanged(Format format) {
                Log.i("LINDA4 => ", String.valueOf(exoPlayer.getAudioAttributes()));

            }

            @Override
            public void onAudioSinkUnderrun(int bufferSize, long bufferSizeMs, long elapsedSinceLastFeedMs) {
                Log.i("LINDA5 => ", String.valueOf(exoPlayer.getAudioAttributes()));

            }

            @Override
            public void onAudioDisabled(DecoderCounters counters) {
                Log.i("LINDA6 => ", String.valueOf(exoPlayer.getAudioAttributes()));

            }
        });
    }

    @Override
    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
        Log.i("KING1 => ", String.valueOf(exoPlayer.getCurrentWindowIndex()));

    }

    @Override
    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
        Log.i("KING2 => ", String.valueOf(exoPlayer.getContentPosition()));

    }

    @Override
    public void onLoadingChanged(boolean isLoading) {
        Log.i("KING3 => ", String.valueOf(exoPlayer.getCurrentPosition()));

    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        Log.i("KING4 => ", String.valueOf(exoPlayer.getCurrentTrackGroups()));

    }

    @Override
    public void onRepeatModeChanged(int repeatMode) {
        Log.i("KING5 => ", String.valueOf(exoPlayer.getCurrentWindowIndex()));

    }

    @Override
    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
        Log.i("KING6 => ", String.valueOf(exoPlayer.getCurrentWindowIndex()));

    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {

    }

    @Override
    public void onPositionDiscontinuity(int reason) {
        Log.i("KING7 => ", String.valueOf(exoPlayer.getCurrentWindowIndex()));

    }

    @Override
    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
        Log.i("KING8 => ", String.valueOf(exoPlayer.getCurrentWindowIndex()));

    }

    @Override
    public void onSeekProcessed() {
        Log.i("KING9 => ", String.valueOf(exoPlayer.getCurrentWindowIndex()));

    }
}
