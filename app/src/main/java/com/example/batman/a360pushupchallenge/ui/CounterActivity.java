package com.example.batman.a360pushupchallenge.ui;

import android.content.ContentValues;
import android.database.Cursor;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.batman.a360pushupchallenge.R;
import com.example.batman.a360pushupchallenge.data.PushupContract;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CounterActivity
        extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "CounterActivity";

    public static final int PUSHUP_LOADER = 601;

    private String pushupLastPath;

    private int livePushupCounter = 0;
    private Uri currentUri;

    private MediaPlayer mediaPlayer;
    private MediaPlayer mediaPlayer10;

    @BindView(R.id.counter_count_button)

    TextView counterButton;
    @BindView(R.id.counter_done_button)
    TextView doneButton;
    @BindView(R.id.counter_record)
    TextView record;
    @BindView(R.id.personal_record)
    TextView personalRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_counter);
        ButterKnife.bind(this);

        String customFont = "Teko-Medium.ttf";
        Typeface typeface = Typeface.createFromAsset(getAssets(), customFont);

        //Ting sound effect for touch
        mediaPlayer = MediaPlayer.create(getApplicationContext(), R.raw.adapter_clink);
        mediaPlayer10 = MediaPlayer.create(getApplicationContext(), R.raw.adapter_clink_10);

        counterButton.setTypeface(typeface);
        doneButton.setTypeface(typeface);
        record.setTypeface(typeface);
        personalRecord.setTypeface(typeface);

        if (getIntent().hasExtra(getString(R.string.extra_last_path)) &&
                getIntent().hasExtra(getString(R.string.extra_type_title))) {

            setTitle(getIntent().getStringExtra(getString(R.string.extra_type_title)));
            pushupLastPath = getIntent().getStringExtra(getString(R.string.extra_last_path));
            currentUri = Uri.parse(PushupContract.BASE_CONTENT_URI + "/" + pushupLastPath);
        }

        counterButton.setText(String.valueOf(livePushupCounter));

        counterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pushupCounter();
            }
        });
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDoneButton();
            }
        });

        //Kickoff Loader
        getSupportLoaderManager().initLoader(PUSHUP_LOADER, null, this);
    }

    private void pushupCounter() {
        livePushupCounter++;
        counterButton.setText(String.valueOf(livePushupCounter));

        //If it hits multiple of 10 then sound 2
        if (livePushupCounter % 10 == 0) {
            mediaPlayer10.start();
        } else {
            mediaPlayer.start();
        }
    }

    private void setDoneButton() {

        if (livePushupCounter > 0) {
            //Getting Score
            Integer currentScore = livePushupCounter;

            //Putting values in Column
            ContentValues values = new ContentValues();
            values.put(PushupContract.PushupKnee.COLUMN_SCORE, currentScore);

            // Show a toast message depending on whether or not the insertion was successful
            Uri newUri = getContentResolver().insert(currentUri, values);

            Log.d(TAG, "onCreate: NEW URI" + newUri);
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, getString(R.string.insert_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.insert_successful),
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            finish();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        String[] projection = {
                PushupContract.UNIVERSAL_SCORE};

        return new CursorLoader(this,
                currentUri,
                projection,
                null,
                null,
                "score DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        if (data == null || data.getCount() < 1) {
            return;
        }
        if (data.moveToFirst()) {
            String score = data.getString(0);
            record.setText(String.valueOf(String.valueOf(score)));
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
    }

    @Override
    protected void onPause() {
        super.onPause();
        mediaPlayer.release();
        mediaPlayer10.release();
    }
}
