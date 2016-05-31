package ch.sheremet.dasfoo.rover.android.client.grpc.authentication;

import android.content.Intent;

import com.google.android.gms.security.ProviderInstaller;

/**
 * Created by Katarina Sheremet on 5/31/16 3:22 PM.
 */
public class GrpcProviderInstaller implements ProviderInstaller.ProviderInstallListener {

    /**
     * This method is only called if the provider is successfully updated
     * (or is already up-to-date).
     */
    @Override
    public void onProviderInstalled() {

    }

    /**
     * This method is called if updating fails; the error code indicates
     * whether the error is recoverable.
     */
    @Override
    public void onProviderInstallFailed(int i, Intent intent) {

    }
}
