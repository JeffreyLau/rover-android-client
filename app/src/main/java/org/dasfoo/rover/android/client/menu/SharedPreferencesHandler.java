package org.dasfoo.rover.android.client.menu;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

/**
 * Created by Katarina Sheremet on 6/18/16 4:01 PM.
 * <p/>
 * Class is for managing SharedPreferences to get host, port and password.
 */
public class SharedPreferencesHandler {

    /**
     * Context for getting shared preferences.
     */
    private final Context mContext;

    /**
     * Constructor.
     *
     * @param context context
     */
    public SharedPreferencesHandler(final Context context) {
        this.mContext = context;
    }

    /**
     * Gets host for video.
     *
     * @return host
     * @throws IllegalArgumentException if host is empty
     */
    public final String getVideoHost() throws IllegalArgumentException {
        return getString(Settings.VIDEO_HOST);
    }

    /**
     * Gets port for video.
     *
     * @return port
     * @throws IllegalArgumentException if port is empty
     */
    public final int getVideoPort() throws IllegalArgumentException {
        return Integer.parseInt(getString(Settings.VIDEO_PORT));
    }

    /**
     * Gets grpc host. If host is empty, it tries to return host from video setting.
     *
     * @return host
     * @throws IllegalArgumentException if host is empty
     */
    public final String getGrpcHost() throws IllegalArgumentException {
        try {
            return getString(Settings.GRPC_HOST);
        } catch (IllegalArgumentException e) {
            return getVideoHost();
        }

    }

    /**
     * Gets grpc post.
     *
     * @return grpc port
     * @throws IllegalArgumentException if post is empty
     */
    public final int getGrpcPort() throws IllegalArgumentException {
        return Integer.parseInt(getString(Settings.GRPC_PORT));
    }

    /**
     * Gets password for video.
     *
     * @return password for video
     * @throws IllegalArgumentException if password is empty
     */
    public final String getPassword() throws IllegalArgumentException {
        return getString(Settings.VIDEO_PASSWORD);
    }

    public final String getToken() throws IllegalArgumentException {
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        final String token = sharedPreferences.getString("token", "");
        if (TextUtils.isEmpty(token)) {
            throw new IllegalArgumentException("Empty string in settings");
        }
        return token;
    }

    /**
     * Gets string using key from settings.
     *
     * @param key gets SharedPreferences
     * @return string that saved in settings
     * @throws IllegalArgumentException if string is empty
     */
    private String getString(final Settings key) throws IllegalArgumentException {
        final SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        final String host = sharedPreferences.getString(key.toString(), "");
        if (TextUtils.isEmpty(host)) {
            throw new IllegalArgumentException("Empty string in settings");
        }
        return host;
    }
}
