package com.cikoapps.deezeralarm.Fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cikoapps.deezeralarm.Activities.RingtoneActivity;
import com.cikoapps.deezeralarm.HelperClasses.SimpleDividerItemDecoration;
import com.cikoapps.deezeralarm.R;
import com.cikoapps.deezeralarm.adapters.DeezerAlbumAdapter;
import com.deezer.sdk.model.AImageOwner;
import com.deezer.sdk.model.Album;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.network.request.event.RequestListener;

import java.util.ArrayList;

/**
 * Created by arvis.taurenis on 2/16/2015.
 */
public class DeezerAlbumFragment extends Fragment {

    static Activity callingActivity;
    private static final String ARG_POSITION = "position";
    private static Context context;
    private static ProgressDialog progress;
    private int position;
    DeezerAlbumAdapter mAdapter;
    private ArrayList<Album> albumsArrayList;
    RecyclerView recyclerView;

    public static Fragment newInstance(int position, Context mContext, Activity activity) {
        DeezerAlbumFragment f = new DeezerAlbumFragment();
        callingActivity = activity;
        context = mContext;
        Bundle b = new Bundle();
        b.putInt(ARG_POSITION, position);
        f.setArguments(b);
        progress = new ProgressDialog(activity);
        progress.setTitle("Loading");
        progress.setMessage("Please wait...");
        return f;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible()) {
            if (!isVisibleToUser) {
                if (mAdapter != null) {
                    if (mAdapter.selectedPosition >= 0) {
                        mAdapter.notifyItemChanged(mAdapter.selectedPosition);
                    }
                }
            } else if (isVisibleToUser) {
                if (albumsArrayList == null) {
                    progress.show();
                }
                if (mAdapter != null) {
                    if (mAdapter.selectedPosition >= 0) {
                        mAdapter.notifyItemChanged(mAdapter.selectedPosition);
                    }
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        position = getArguments().getInt(ARG_POSITION);
        getUserAlbums();
    }

    public static void updateSelectedRingtone(long id, String name) {
        RingtoneActivity.selectedRingtone.updateDeezerRingtone(2, id, name);
    }

    public void getUserAlbums() {
        albumsArrayList = new ArrayList<>();
        final ArrayList<com.cikoapps.deezeralarm.models.Album> localAlbumList = new ArrayList<>();
        RequestListener requestListener = new JsonRequestListener() {
            public void onResult(Object result, Object requestId) {
                progress.dismiss();
                albumsArrayList = (ArrayList<Album>) result;
                for (Album album : albumsArrayList) {
                    com.cikoapps.deezeralarm.models.Album albumLocal = new com.cikoapps.deezeralarm.models.Album(album.getId(), album.getTitle(), album.getArtist().getName(),
                            album.getCoverUrl(), timeConversion(album.getDuration()), album.getImageUrl(AImageOwner.ImageSize.small), album.getImageUrl(AImageOwner.ImageSize.medium),
                            album.getImageUrl(AImageOwner.ImageSize.big));
                    localAlbumList.add(albumLocal);
                }
                if (localAlbumList.size() < 1) {
                    localAlbumList.add(new com.cikoapps.deezeralarm.models.Album(-1, "No albums found", "", "", "", "", "", ""));
                }
                mAdapter = new DeezerAlbumAdapter(context, localAlbumList);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setAdapter(mAdapter);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
            }

            public void onUnparsedResult(String requestResponse, Object requestId) {
            }

            public void onException(Exception e, Object requestId) {
            }
        };
        DeezerRequest currUserAlbumRequest = DeezerRequestFactory.requestCurrentUserAlbums();
        currUserAlbumRequest.setId("currUserAlbumRequest");
        ((RingtoneActivity) getActivity()).deezerConnect.requestAsync(currUserAlbumRequest, requestListener);
    }

    private static String timeConversion(int totalSeconds) {

        final int MINUTES_IN_AN_HOUR = 60;
        final int SECONDS_IN_A_MINUTE = 60;

        int seconds = totalSeconds % SECONDS_IN_A_MINUTE;
        int totalMinutes = totalSeconds / SECONDS_IN_A_MINUTE;
        int minutes = totalMinutes % MINUTES_IN_AN_HOUR;
        int hours = totalMinutes / MINUTES_IN_AN_HOUR;
        if (hours > 0) {
            return hours + " h " + minutes + " min " + seconds + " sec";
        } else {
            return minutes + " min " + seconds + " sec";
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.deezer_albums_fragment_layout, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.albumRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        return rootView;
    }
}