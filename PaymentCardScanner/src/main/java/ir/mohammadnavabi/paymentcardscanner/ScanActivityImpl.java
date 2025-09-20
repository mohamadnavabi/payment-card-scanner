package ir.mohammadnavabi.paymentcardscanner;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ScanActivityImpl extends ScanBaseActivity {

	private static final String TAG = "ScanActivityImpl";

	public static final String SCAN_CARD_TEXT = "topText";
	public static final String POSITION_CARD_TEXT = "bottomText";
	public static Typeface topTextTypeface = null;
	public static Typeface bottomTextTypeface = null;

	public static final String RESULT_CARD_NUMBER = "cardNumber";
	public static final String RESULT_EXPIRY_MONTH = "expiryMonth";
	public static final String RESULT_EXPIRY_YEAR = "expiryYear";

	private ImageView mDebugImageView;
	private boolean mInDebugMode = false;
	private static long startTimeMs = 0;

	protected void onCreate(Bundle savedInstanceState) {
		try {
			Log.d(TAG, "ScanActivityImpl onCreate started");
			super.onCreate(savedInstanceState);
			Log.d(TAG, "setContentView called");
			setContentView(R.layout.activity_scan_card);
			Log.d(TAG, "Layout loaded successfully");
		} catch (Exception e) {
			Log.e(TAG, "Error in onCreate: " + e.getMessage());
			e.printStackTrace();
			finish();
			return;
		}

		String topCardText = getIntent().getStringExtra(SCAN_CARD_TEXT);
		if (!TextUtils.isEmpty(topCardText)) {
			TextView topText = (TextView) findViewById(R.id.topText);
			if (topText != null) {
				topText.setText(topCardText);
				if (topTextTypeface != null) {
					topText.setTypeface(topTextTypeface);
				}
			}
		}

		String bottomCardText = getIntent().getStringExtra(POSITION_CARD_TEXT);
		if (!TextUtils.isEmpty(bottomCardText)) {
			TextView bottomText = (TextView) findViewById(R.id.bottomText);
			if (bottomText != null) {
				bottomText.setText(bottomCardText);
				if (bottomTextTypeface != null) {
					bottomText.setTypeface(bottomTextTypeface);
				}
			}
		}

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[] { Manifest.permission.CAMERA }, 110);
			} else {
				mIsPermissionCheckDone = true;
			}
		} else {
			// no permission checks
			mIsPermissionCheckDone = true;
		}

		View closeButton = findViewById(R.id.closeButton);
		if (closeButton != null) {
			closeButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					onBackPressed();
				}
			});
		}

		mDebugImageView = findViewById(R.id.debugImageView);
		mInDebugMode = getIntent().getBooleanExtra("debug", false);
		if (!mInDebugMode && mDebugImageView != null) {
			mDebugImageView.setVisibility(View.INVISIBLE);
		}

		Log.d(TAG, "Setting up view IDs");
		setViewIds(R.id.cardRectangle, R.id.shadedBackground, R.id.texture,
				R.id.cardNumber, R.id.expiry);

		Log.d(TAG, "ScanActivityImpl onCreate completed successfully");
	}

	@Override
	protected void onCardScanned(String numberResult, String month, String year) {
		Intent intent = new Intent();
		intent.putExtra(RESULT_CARD_NUMBER, numberResult);
		intent.putExtra(RESULT_EXPIRY_MONTH, month);
		intent.putExtra(RESULT_EXPIRY_YEAR, year);
		setResult(RESULT_OK, intent);
		finish();
	}

	@Override
	public void onPrediction(final String number, final Expiry expiry, final Bitmap bitmap,
			final List<DetectedBox> digitBoxes, final DetectedBox expiryBox) {
		if (mInDebugMode) {
			try {
				// Temporarily disable debug box drawing to isolate the crash
				Log.d(TAG, "Debug mode enabled - bitmap size: " + bitmap.getWidth() + "x" + bitmap.getHeight());

				// Simple debug without box drawing
				if (mDebugImageView != null && bitmap != null) {
					mDebugImageView.setImageBitmap(bitmap);
				}

				Log.d(TAG, "Prediction (ms): " + (SystemClock.uptimeMillis() - mPredictionStartMs));
				if (startTimeMs != 0) {
					Log.d(TAG, "time to first prediction: " + (SystemClock.uptimeMillis() - startTimeMs));
					startTimeMs = 0;
				}
			} catch (Exception e) {
				Log.e(TAG, "Error in debug mode: " + e.getMessage());
				e.printStackTrace();
			}
		}

		super.onPrediction(number, expiry, bitmap, digitBoxes, expiryBox);
	}
}
