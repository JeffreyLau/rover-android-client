package ch.sheremet.dasfoo.rover.android.client;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import ch.sheremet.dasfoo.rover.android.client.grpc.task.EncodersReadingTask;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.GettingBoardInfoTask;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.GrpcTask;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.MovingRoverTask;
import ch.sheremet.dasfoo.rover.android.client.grpc.task.OnTaskCompleted;

public class MainActivity extends AppCompatActivity implements OnTaskCompleted {

    private Button mMoveForwardButton;
    private Button mInfoButton;
    private Button mReadEncodersButton;
    private EditText mHostEdit;
    private EditText mPortEdit;
    private TextView mResultText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mMoveForwardButton = (Button) findViewById(R.id.move_forward_button);
        mMoveForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveForward();
            }
        });
        mInfoButton = (Button) findViewById(R.id.info_button);
        mInfoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getInfo();
            }
        });
        mReadEncodersButton = (Button) findViewById(R.id.read_encoders_button);
        mReadEncodersButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readEncoders();
            }
        });
        mHostEdit = (EditText) findViewById(R.id.host_edit_text);
        mPortEdit = (EditText) findViewById(R.id.port_edit_text);
        mResultText = (TextView) findViewById(R.id.grpc_response_text);
    }

    public void moveForward() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mHostEdit.getWindowToken(), 0);
        GrpcTask movingRoverTask = new MovingRoverTask(this);
        executeGrpcTask(movingRoverTask);
    }

    public void getInfo() {
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mHostEdit.getWindowToken(), 0);
        GrpcTask getBoardInfoTask = new GettingBoardInfoTask(this);
        executeGrpcTask(getBoardInfoTask);
    }

    public void readEncoders() {
        GrpcTask encodersReadingTask = new EncodersReadingTask(this);
        executeGrpcTask(encodersReadingTask);
    }

    @Override
    public void onTaskCompleted(String result) {
        if (result == null) mResultText.setText(R.string.getting_null_result_text);
        mResultText.setText(result);
        mMoveForwardButton.setEnabled(true);
        mInfoButton.setEnabled(true);
        mReadEncodersButton.setEnabled(true);
    }

    private void executeGrpcTask(GrpcTask task) {
        String host = mHostEdit.getText().toString();
        String port = mPortEdit.getText().toString();
        if (TextUtils.isEmpty(host) || TextUtils.isEmpty(port)) {
            Toast.makeText(this, "You did not enter a host or a port", Toast.LENGTH_SHORT).show();
            return;
        }
        mMoveForwardButton.setEnabled(false);
        mInfoButton.setEnabled(false);
        mReadEncodersButton.setEnabled(false);
        task.execute(host, port);
    }
}
