package com.playposse.egoeater.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.playposse.egoeater.ExtraConstants;
import com.playposse.egoeater.R;
import com.playposse.egoeater.backend.egoEaterApi.model.UserBean;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.clientactions.DeleteProfilePhotoClientAction;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.storage.ProfileParcelable;
import com.playposse.egoeater.util.GlideUtil;
import com.playposse.egoeater.util.ProfileFormatter;
import com.playposse.egoeater.util.SimpleAlertDialog;

/**
 * A {@link Fragment} that lets the user edit his/her profile.
 */
public class ViewOwnProfileFragment extends Fragment {

    private ImageView profilePhoto0ImageView;
    private ImageView profilePhoto1ImageView;
    private ImageView profilePhoto2ImageView;
    private ImageView emptyPhoto1ImageView;
    private ImageView emptyPhoto2ImageView;
    private TextView headlineTextView;
    private TextView subHeadTextView;
    private TextView profileTextView;
    private FloatingActionButton editButton;

    public ViewOwnProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_view_own_profile, container, false);

        profilePhoto0ImageView = (ImageView) rootView.findViewById(R.id.profilePhoto0ImageView);
        profilePhoto1ImageView = (ImageView) rootView.findViewById(R.id.profilePhoto1ImageView);
        profilePhoto2ImageView = (ImageView) rootView.findViewById(R.id.profilePhoto2ImageView);
        emptyPhoto1ImageView = (ImageView) rootView.findViewById(R.id.emptyPhoto1ImageView);
        emptyPhoto2ImageView = (ImageView) rootView.findViewById(R.id.emptyPhoto2ImageView);
        headlineTextView = (TextView) rootView.findViewById(R.id.headlineTextView);
        subHeadTextView = (TextView) rootView.findViewById(R.id.subHeadTextView);
        profileTextView = (TextView) rootView.findViewById(R.id.profileTextView);
        editButton = (FloatingActionButton) rootView.findViewById(R.id.editButton);

        initProfilePhoto(
                0,
                profilePhoto0ImageView,
                null,
                EgoEaterPreferences.getProfilePhotoUrl0(getContext()));
        initProfilePhoto(
                1,
                profilePhoto1ImageView,
                emptyPhoto1ImageView,
                EgoEaterPreferences.getProfilePhotoUrl1(getContext()));
        initProfilePhoto(
                2,
                profilePhoto2ImageView,
                emptyPhoto2ImageView,
                EgoEaterPreferences.getProfilePhotoUrl2(getContext()));

        UserBean userBean = EgoEaterPreferences.getUser(getContext());
        ProfileParcelable profile = new ProfileParcelable(userBean);
        headlineTextView.setText(ProfileFormatter.formatNameAndAge(getContext(), profile));
        subHeadTextView.setText(ProfileFormatter.formatCityStateAndDistance(getContext(), profile));
        profileTextView.setText(profile.getProfileText());

        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
                startActivity(new Intent(getContext(), EditProfileActivity.class));
            }
        });

        return rootView;
    }

    private void initProfilePhoto(
            final int photoIndex,
            ImageView imageView,
            @Nullable ImageView emptyView,
            String photoUrl) {

        if (photoUrl != null) {
            GlideUtil.load(imageView, photoUrl);
            imageView.setVisibility(View.VISIBLE);
            if (emptyView != null) {
                emptyView.setVisibility(View.GONE);
            }
        } else {
            imageView.setVisibility(View.GONE);
            if (emptyView != null) {
                emptyView.setVisibility(View.VISIBLE);
            }
        }

        View.OnClickListener clickListenerToDialog = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SimpleAlertDialog.confirmPhoto(
                        getContext(),
                        new Runnable() {
                            @Override
                            public void run() {
                                pickPhoto(photoIndex);
                            }
                        }, new Runnable() {
                            @Override
                            public void run() {
                                deletePhoto(photoIndex);
                            }
                        });
            }
        };
        View.OnClickListener clickListenerToPickActivity = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickPhoto(photoIndex);
            }
        };
        if (photoIndex == 0) {
            imageView.setOnClickListener(clickListenerToPickActivity);
        } else {
            imageView.setOnClickListener(clickListenerToDialog);
        }
        if (emptyView != null) {
            emptyView.setOnClickListener(clickListenerToPickActivity);
        }
    }

    private void pickPhoto(int photoIndex) {
        Intent intent = ExtraConstants.createCropPhotoIntent(getContext(), photoIndex);
        startActivity(intent);
    }

    private void deletePhoto(final int photoIndex) {
        ((ActivityWithProgressDialog) getActivity()).showLoadingProgress();
        new DeleteProfilePhotoClientAction(
                getContext(),
                photoIndex,
                new ApiClientAction.Callback<Void>() {
                    @Override
                    public void onResult(Void data) {
                        ((ActivityWithProgressDialog) getActivity()).dismissLoadingProgress();
                        clearPhotoSlot(photoIndex);
                    }
                }).execute();
    }

    private void clearPhotoSlot(int photoIndex) {
        switch (photoIndex) {
            case 1:
                profilePhoto1ImageView.setImageBitmap(null);
                profilePhoto1ImageView.setVisibility(View.GONE);
                emptyPhoto1ImageView.setVisibility(View.VISIBLE);
                break;
            case 2:
                profilePhoto2ImageView.setImageBitmap(null);
                profilePhoto2ImageView.setVisibility(View.GONE);
                emptyPhoto2ImageView.setVisibility(View.VISIBLE);
                break;
        }
    }
}
