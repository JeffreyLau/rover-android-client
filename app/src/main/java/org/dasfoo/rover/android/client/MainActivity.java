package org.dasfoo.rover.android.client;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.security.ProviderInstaller;

import org.dasfoo.rover.android.client.grpc.task.AbstractGrpcTaskExecutor;
import org.dasfoo.rover.android.client.grpc.task.EncodersReadingTask;
import org.dasfoo.rover.android.client.grpc.task.GettingBoardInfoTask;
import org.dasfoo.rover.android.client.grpc.task.GrpcConnection;
import org.dasfoo.rover.android.client.grpc.task.MovingRoverTask;
import org.dasfoo.rover.android.client.menu.MenuFragment;
import org.dasfoo.rover.android.client.menu.SharedPreferencesHandler;

public class MainActivity extends AppCompatActivity
        implements ProviderInstaller.ProviderInstallListener {
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private static final String PROVIDER_NOT_INSTALLED =
            "The security provider installation failed, " +
                    "encrypted communication is not available: %s";
    private static final String TAG = MainActivity.class.getName();

    /**
     * Moves rover forward.
     */
    private Button mMoveForwardButton;

    /**
     * Gets information about battery, humidity, temperature and light.
     */
    private Button mInfoButton;

    /**
     * Gets the number of turns the rights and left wheels.
     */
    private Button mReadEncodersButton;

    /**
     * Result text view.
     */
    private TextView mResultText;

    private GrpcConnection mGrpcConnection;

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(final View v) {
            switch (v.getId()) {
                case R.id.move_forward_button:
                    executeGrpcTask(new MovingRoverTask());
                    break;
                case R.id.info_button:
                    executeGrpcTask(new GettingBoardInfoTask());
                    break;
                case R.id.read_encoders_button:
                    executeGrpcTask(new EncodersReadingTask());
                    break;
                default:
                    Log.v(TAG, "Button is not implemented yet.");
                    break;
            }
        }
    };

    @Override
    protected final void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMoveForwardButton = (Button) findViewById(R.id.move_forward_button);
        mMoveForwardButton.setOnClickListener(onClickListener);
        mInfoButton = (Button) findViewById(R.id.info_button);
        mInfoButton.setOnClickListener(onClickListener);
        mReadEncodersButton = (Button) findViewById(R.id.read_encoders_button);
        mReadEncodersButton.setOnClickListener(onClickListener);
        mResultText = (TextView) findViewById(R.id.grpc_response_text);

        // Android relies on a security Provider to provide secure network communications.
        // It verifies that the security provider is up-to-date.
        ProviderInstaller.installIfNeededAsync(this, this);

        // Add menu fragment
        final FragmentManager fragmentManager = getFragmentManager();
        final FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        final MenuFragment menuFragment = new MenuFragment();
        fragmentTransaction.add(menuFragment, "menu");
        fragmentTransaction.commit();
    }

    @Override
    protected final void onSaveInstanceState(final Bundle state) {
        super.onSaveInstanceState(state);
    }

    /**
     * This method is called after {@link #onStart} when the activity is
     * being re-initialized from a previously saved state, given here in
     * savedInstanceState. Most implementations will simply use {@link #onCreate}
     * to restore their state, but it is sometimes convenient to do it here
     * after all of the initialization has been done or to allow subclasses to
     * decide whether to use your default implementation.  The default
     * implementation of this method performs a restore of any view state that
     * had previously been frozen by {@link #onSaveInstanceState}.
     * <p/>
     * <p>This method is called between {@link #onStart} and
     * {@link #onPostCreate}.
     *
     * @param savedInstanceState the data most recently supplied in {@link #onSaveInstanceState}.
     * @see #onCreate
     * @see #onPostCreate
     * @see #onResume
     * @see #onSaveInstanceState
     */
    @Override
    protected final void onRestoreInstanceState(final Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected final void onDestroy() {
        super.onDestroy();
        if (mGrpcConnection != null) {
            mGrpcConnection.shutDownConnection();
            mGrpcConnection = null;
        }
    }

    private void executeGrpcTask(final AbstractGrpcTaskExecutor task) {
        new GrpcTask().execute(task);
    }

    private void enableButtons(final boolean isEnabled) {
        mMoveForwardButton.setEnabled(isEnabled);
        mInfoButton.setEnabled(isEnabled);
        mReadEncodersButton.setEnabled(isEnabled);
    }

    @Override
    public final void onProviderInstalled() {
        Toast.makeText(this, "The security provider installed.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public final void onProviderInstallFailed(final int errorCode, final Intent intent) {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        if (googleAPI.isUserResolvableError(errorCode)) {
            googleAPI.getErrorDialog(MainActivity.this, errorCode,
                    PLAY_SERVICES_RESOLUTION_REQUEST).show();
        } else {
            onProviderInstallerNotAvailable(errorCode);
        }
    }

    /**
     * Executes when provider installation was failed. And it is not possible to install it.
     * Shows the dialog to a user and returns to the main screen.
     *
     * @param errorCode code of exception
     */
    private void onProviderInstallerNotAvailable(final int errorCode) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(String.format(PROVIDER_NOT_INSTALLED, errorCode))
                .setCancelable(false)
                .setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .create()
                .show();
    }

    public class GrpcTask extends AsyncTask<AbstractGrpcTaskExecutor, Void, String> {

        @Override
        protected final void onPreExecute() {
            super.onPreExecute();
            enableButtons(Boolean.FALSE);
        }

        @Override
        protected final String doInBackground(final AbstractGrpcTaskExecutor... params) {
            try {
                final SharedPreferencesHandler sharedPreferences =
                        new SharedPreferencesHandler(MainActivity.this);
                final String host = sharedPreferences.getGrpcHost();
                final int port = sharedPreferences.getGrpcPort();
                final String password = sharedPreferences.getToken();
                // TODO(ksheremet): implement onSharedPreferencesChanged
                if (mGrpcConnection == null ||
                        !host.equals(mGrpcConnection.getHost()) ||
                        !password.equals(mGrpcConnection.getToken()) ||
                        port != mGrpcConnection.getPort()) {
                    mGrpcConnection = new GrpcConnection(host, port, password);
                }
            } catch (IllegalArgumentException e) {
                return e.getMessage();
            }
            return params[0].execute(mGrpcConnection.getStub());
        }

        @Override
        protected final void onPostExecute(final String result) {
            mResultText.setText(result);
            enableButtons(Boolean.TRUE);
        }
    }
}
