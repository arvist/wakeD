package com.cikoapps.deezeralarm.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.cikoapps.deezeralarm.Activities.RingtoneActivity;
import com.cikoapps.deezeralarm.HelperClasses.HelperClass;
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
import java.util.Collections;
import java.util.Comparator;

public class DeezerAlbumFragment extends Fragment {

    private static final String TAG = "DeezerAlbumFragment";
    private static Context context;
    private static ProgressBar progress;
    private static boolean onlyWiFi;
    private DeezerAlbumAdapter mAdapter;
    private ArrayList<Album> albumsArrayList;
    private RecyclerView recyclerView;
    private boolean enableNoWiFiTextView = false;

    public static Fragment newInstance(Context mContext, boolean onlyWifiConnection) {
        DeezerAlbumFragment fragment = new DeezerAlbumFragment();
        context = mContext;
        onlyWiFi = onlyWifiConnection;
        return fragment;
    }

    public static void updateSelectedRingtone(long id, String name, String artist) {
        RingtoneActivity.selectedRingtone.updateDeezerRingtone(RingtoneActivity.ALBUM_ID, id, name, artist);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (this.isVisible()) {
            if (!isVisibleToUser) {
                if (mAdapter != null) {
                    if (DeezerAlbumAdapter.selectedPosition >= 0) {
                        mAdapter.notifyItemChanged(DeezerAlbumAdapter.selectedPosition);
                    }
                }
            } else {
                if (albumsArrayList == null) {
                }
                if (mAdapter != null) {
                    if (DeezerAlbumAdapter.selectedPosition >= 0)
                        mAdapter.notifyItemChanged(DeezerAlbumAdapter.selectedPosition);
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean WiFiConnected = (new HelperClass(context)).isWifiConnected();
        if ((onlyWiFi && WiFiConnected) || !onlyWiFi) {
            getUserAlbums();
        } else {
            enableNoWiFiTextView = true;
        }
    }

    void getUserAlbums() {
        albumsArrayList = new ArrayList<>();
        final ArrayList<com.cikoapps.deezeralarm.models.Album> localAlbumList = new ArrayList<>();
        RequestListener requestListener = new JsonRequestListener() {
            public void onResult(Object result, Object requestId) {
                recyclerView.setVisibility(View.VISIBLE);
                progress.setVisibility(View.GONE);
                //noinspection unchecked
                albumsArrayList = (ArrayList<Album>) result;
                for (Album album : albumsArrayList) {
                    com.cikoapps.deezeralarm.models.Album albumLocal = new com.cikoapps.deezeralarm.models.Album(album.getId(), album.getTitle(), album.getArtist().getName(),
                            album.getCoverUrl(), HelperClass.timeConversion(album.getDuration()), album.getImageUrl(AImageOwner.ImageSize.small), album.getImageUrl(AImageOwner.ImageSize.medium),
                            album.getImageUrl(AImageOwner.ImageSize.big));
                    localAlbumList.add(albumLocal);
                }
                if (localAlbumList.size() < 1) {
                    localAlbumList.add(new com.cikoapps.deezeralarm.models.Album(-1, "No albums found", "", "", "", "", "", ""));
                }
                Collections.sort(localAlbumList, new Comparator<com.cikoapps.deezeralarm.models.Album>() {
                    @Override
                    public int compare(com.cikoapps.deezeralarm.models.Album lhs, com.cikoapps.deezeralarm.models.Album rhs) {
                        return lhs.title.compareTo(rhs.title);
                    }
                });
                mAdapter = new DeezerAlbumAdapter(context, localAlbumList);
                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
                recyclerView.setLayoutManager(mLayoutManager);
                recyclerView.setAdapter(mAdapter);
                recyclerView.setItemAnimator(new DefaultItemAnimator());
                recyclerView.addItemDecoration(new SimpleDividerItemDecoration(context));
            }

            public void onUnparsedResult(String requestResponse, Object requestId) {
                Log.e(TAG, "Unparsed Result");
            }

            public void onException(Exception e, Object requestId) {
                Log.e(TAG, "Getting user album error - " + e.getMessage());
            }
        };
        DeezerRequest currUserAlbumRequest = DeezerRequestFactory.requestCurrentUserAlbums();
        currUserAlbumRequest.setId(TAG);
        ((RingtoneActivity) getActivity()).deezerConnect.requestAsync(currUserAlbumRequest, requestListener);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.deezer_albums_fragment_layout, container, false);
        recyclerView = (RecyclerView) rootView.findViewById(R.id.albumRecyclerView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setVisibility(View.GONE);
        progress = (ProgressBar) rootView.findViewById(R.id.cover_progress);
        progress.setVisibility(View.VISIBLE);
        TextView noWifiTextView = (TextView) rootView.findViewById(R.id.noWifiTextView);
        if (enableNoWiFiTextView) {
            noWifiTextView.setVisibility(View.VISIBLE);
        } else {
            noWifiTextView.setVisibility(View.GONE);
        }
        return rootView;
    }
}
