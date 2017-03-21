package com.playposse.egoeater.clientactions;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.util.Log;

import com.playposse.egoeater.GlobalRouting;
import com.playposse.egoeater.backend.egoEaterApi.EgoEaterApi;

import java.io.IOException;

/**
 * A base class for calls to the cloud to implement.
 */
public abstract class ApiClientAction<D> {

    private static final String LOG_TAG = ApiClientAction.class.getSimpleName();

    @Nullable
    private static EgoEaterApi api;

    @Nullable
    private final Callback<D> callback;
    private final Context context;

    @Nullable
    private D returnedData;

    public ApiClientAction(Context context) {
        this.context = context;

        callback = null;
    }

    public ApiClientAction(Context context, Callback<D> callback) {
        this.context = context;
        this.callback = callback;
    }

    protected EgoEaterApi getApi() {
        if (api == null) {
            api = new EgoEaterApi.Builder(
                    AndroidHttp.newCompatibleTransport(),
                    new AndroidJsonFactory(),
                    null)
                    .setApplicationName("Ego Eater")
                    .setRootUrl("https://ego-eater.appspot.com/_ah/api/")
                    .build();
        }
        return api;
    }

    public final void execute() {
        new ClientActionAsyncTask().execute();
    }

    /**
     * Child classes can override this to execute code on the UI thread before calling the server.
     */
    @UiThread
    protected void preExecute() {
    }

    /**
     * Child classes can override this to execute methods in a separate thread.
     */
    @WorkerThread
    protected abstract D executeAsync() throws IOException;

    /**
     * Child classes can override this to execute code on the UI thread after calling the server.
     */
    @UiThread
    protected void postExecute() {
    }

    public Context getContext() {
        return context;
    }

    /**
     * An {@link AsyncTask} to deal with the threading.
     */
    private class ClientActionAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            preExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                String actionName = ApiClientAction.this.getClass().getSimpleName();
                Log.i(LOG_TAG, "Executing client action: " + actionName);
                returnedData = executeAsync();
                Log.i(LOG_TAG, "Finished execution client action: " + actionName);
            } catch (IOException ex) {
                Log.e(LOG_TAG, "Failed to execute: " + this.getClass().getName(), ex);
                GlobalRouting.onCloudError(context);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            postExecute();

            if (callback != null) {
                callback.onResult(returnedData);
            }
        }
    }

    /**
     * A callback interface that is called when the cloud service is finished.
     *
     * @param <D> The data type that the action returns.
     */
    public interface Callback<D> {

        void onResult(D data);
    }
}