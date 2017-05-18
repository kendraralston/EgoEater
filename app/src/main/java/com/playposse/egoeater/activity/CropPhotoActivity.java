package com.playposse.egoeater.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.playposse.egoeater.ExtraConstants;
import com.playposse.egoeater.GlobalRouting;
import com.playposse.egoeater.R;
import com.playposse.egoeater.clientactions.ApiClientAction;
import com.playposse.egoeater.clientactions.UploadProfilePhotoToServletClientAction;
import com.playposse.egoeater.storage.EgoEaterPreferences;
import com.playposse.egoeater.util.SimpleAlertDialog;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.IOException;

/**
 * An {@link android.app.Activity} that crops profile photos to fit the dimensions of the app.
 */
// TODO: Resize image if it is larger than an expected screen size for a phone.
public class CropPhotoActivity extends ActivityWithProgressDialog {

    private static final String LOG_TAG = CropPhotoActivity.class.getSimpleName();

    /**
     * URL pattern to the FB profile photo of a user. The width is set to an arbitrary large size to
     * get the full size photo.
     */
    private static final String FB_PROFILE_PHOTO_URL =
            "https://graph.facebook.com/%1$s/picture?width=9999";
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final String GALLERY_PHOTO_URI_KEY = "galleryPhotoUri";

    private int photoIndex;
    private boolean hasFirstProfilePhoto;
    private boolean hasPhotoFromGallery = false;
    private Uri galleryPhotoUri;

    private ImageView discardImageView;
    private TextView saveTextView;
    private CropImageView cropImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_crop_photo);

        photoIndex = ExtraConstants.getPhotoIndex(getIntent());
        hasFirstProfilePhoto = EgoEaterPreferences.hasFirstProfilePhoto(this);

        discardImageView = (ImageView) findViewById(R.id.discardImageView);
        saveTextView = (TextView) findViewById(R.id.saveTextView);
        cropImageView = (CropImageView) findViewById(R.id.cropImageView);

        // If the user hasn't picked a profile photo yet, the user must pick a profile photo to
        // continue.
        discardImageView.setVisibility(hasFirstProfilePhoto ? View.VISIBLE : View.GONE);

        setTitle(R.string.crop_photo_activity_title);

        discardImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                discardAndExit();
            }
        });

        saveTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingProgress();
                new UploadProfilePhotoToServletClientAction(
                        getApplicationContext(),
                        photoIndex,
                        cropImageView.getCroppedImage(),
                        new ApiClientAction.Callback<String>() {
                            @Override
                            public void onResult(String photoUrl) {
                                onServerActionComplete();
                            }
                        })
                        .execute();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!hasFirstProfilePhoto) {
            // Pick the Facebook profile photo.
            loadFbProfilePhoto();
        } else if (!hasPhotoFromGallery) {
            // Pick a photo from the gallery.
            startActivityToRequestPhoto();
        } else {
            // The photo is already loaded in onActivityResult. There is nothing to do.
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST) {
            if (resultCode == RESULT_OK) {
                galleryPhotoUri = data.getData();
                hasPhotoFromGallery = true;

                loadImageFromLocalUri(galleryPhotoUri);
            } else {
                // The user canceled. Go back to the edit profile activity.
                startEditProfileActivity();
            }
        }
    }

    private void loadImageFromLocalUri(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                    getContentResolver(),
                    uri);
            cropImageView.setImageBitmap(bitmap);
        } catch (IOException ex) {
            Log.e(LOG_TAG, "onActivityResult: Failed to get photo from gallery.", ex);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (hasPhotoFromGallery) {
            outState.putString(GALLERY_PHOTO_URI_KEY, galleryPhotoUri.toString());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        String uriString = savedInstanceState.getString(GALLERY_PHOTO_URI_KEY, null);
        if (uriString != null) {
            galleryPhotoUri = Uri.parse(uriString);
            hasPhotoFromGallery = true;

            loadImageFromLocalUri(galleryPhotoUri);
        }
    }

    private void loadFbProfilePhoto() {
        String fbProfileId = EgoEaterPreferences.getUser(this).getFbProfileId();
        String fbPhotoUrl = String.format(FB_PROFILE_PHOTO_URL, fbProfileId);

        if (fbProfileId == null) {
            // Force the user to log in again and the app to download the FB profile id.
            GlobalRouting.onCloudError(this);
        }

        Glide
                .with(this)
                .load(fbPhotoUrl)
                .asBitmap()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap bitmap, GlideAnimation anim) {
                        // Set the image manually, so that Glide won't do any resizing of the photo.
                        // For the cropping to work, the maximum resolution (not the screen
                        // resolution) has to be used.
                        cropImageView.setImageBitmap(bitmap);
                    }
                });

        Log.i(LOG_TAG, "loadFbProfilePhoto: " + fbPhotoUrl);
    }

    private void onServerActionComplete() {
        dismissLoadingProgress();

        startEditProfileActivity();
    }

    private void startEditProfileActivity() {
        finish();
        startActivity(new Intent(this, ViewOwnProfileActivity.class));
    }

    private void startActivityToRequestPhoto() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        Intent chooserIntent =
                Intent.createChooser(intent, getString(R.string.select_profile_photo_chooser));
        startActivityForResult(chooserIntent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onBackPressed() {
        discardAndExit();
    }

    private void discardAndExit() {
        SimpleAlertDialog.confirmDiscard(
                this,
                new Runnable() {
                    @Override
                    public void run() {
                        startEditProfileActivity();
                    }
                });
    }
}
