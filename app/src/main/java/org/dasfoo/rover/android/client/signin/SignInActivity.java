package org.dasfoo.rover.android.client.signin;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;

import org.dasfoo.rover.android.client.MainActivity;
import org.dasfoo.rover.android.client.R;

import java.io.IOException;

/**
 * Activity for the Sign-In using Google.
 */
public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener,
        View.OnClickListener {
    /**
     * Use for logging.
     */
    private static final String TAG = SignInActivity.class.getSimpleName();

    /**
     * A request code for starting new activity.
     */
    private static final int RC_SIGN_IN = 9001;

    /**
     * Has access to to the Google Sign-In API.
     */
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AccountManager am = AccountManager.get(this);

        setContentView(R.layout.activity_sign_in);
        // Button listener
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .build();
        // Build a GoogleApiClient with access to the Google Sign-In API and the
        // options specified by gso.
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */,
                        this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private class OnTokenAcquired implements AccountManagerCallback<Bundle> {
        @Override
        public void run(AccountManagerFuture<Bundle> result) {
            // Get the result of the operation from the AccountManagerFuture.
            Bundle bundle = null;
            try {
                bundle = result.getResult();
            } catch (OperationCanceledException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (AuthenticatorException e) {
                e.printStackTrace();
            }

            // The token is a named value in the bundle. The name of the value
            // is stored in the constant AccountManager.KEY_AUTHTOKEN.
            String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
            Log.v(TAG, token);
            Intent launch = (Intent) bundle.get(AccountManager.KEY_INTENT);
            if (launch != null) {
                startActivityForResult(launch, 0);
                return;
            }

        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public final void onClick(final View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            default:
                Log.v(TAG, "Button is not implemented yet.");
                break;
        }
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode returned from launching the Intent from
     *                    GoogleSignInApi.getSignInIntent(...)
     * @param resultCode  returned by the child activity through its setResult()
     * @param data        returns result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    @Override
    public final void onActivityResult(final int requestCode, final int resultCode,
                                       final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result;
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    /**
     * An unresolvable error has occurred and Google APIs (including Sign-In)
     * will not be available. Google Api client has auto manage
     * enableAutoManage(FragmentActivity, OnConnectionFailedListener) that manages all resolvable
     * errors.
     *
     * @param connectionResult
     */
    @Override
    public final void onConnectionFailed(@NonNull final ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
    }

    /**
     * Dispatch onStart() to all fragments.  Ensure any created loaders are
     * now started.
     */
    @Override
    protected final void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi
                .silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
            // and the GoogleSignInResult will be available instantly.
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            // If the user has not previously signed in on this device or the sign-in has expired,
            // this asynchronous branch will attempt to sign in the user silently.  Cross-device
            // single sign-on will occur in this branch.
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(final GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    /**
     * Starts Sign-In Activity.
     */
    private void signIn() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    /**
     * Updates activity if user signed in.
     */
    private void updateUI() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    /**
     * Handle Sign-In result.
     *
     * @param result is gotten from Auth.GoogleSignInApi.getSignInResultFromIntent(..)
     */
    private void handleSignInResult(final GoogleSignInResult result) {
        if (result.isSuccess()) {
            // Signed in successfully, show authenticated UI.
            GoogleSignInAccount acct = result.getSignInAccount();
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString("token", acct.getIdToken()).commit();
            updateUI();
        }
    }
}
