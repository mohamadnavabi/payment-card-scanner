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

	public static final String RESULT_CARD_NUMBER = "cardNumber";
	public static final String RESULT_EXPIRY_MONTH = "expiryMonth";
	public static final String RESULT_EXPIRY_YEAR = "expiryYear";

	private ImageView mDebugImageView;
	private boolean mInDebugMode = false;
	private static long startTimeMs = 0;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan_card);

		String topCardText = getIntent().getStringExtra(SCAN_CARD_TEXT);
		if (!TextUtils.isEmpty(topCardText)) {
			((TextView) findViewById(R.id.topText)).setText(topCardText);
		}

		String bottomCardText = getIntent().getStringExtra(POSITION_CARD_TEXT);
		if (!TextUtils.isEmpty(bottomCardText)) {
			((TextView) findViewById(R.id.bottomText)).setText(bottomCardText);
		}

		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
				requestPermissions(new String[]{Manifest.permission.CAMERA}, 110);
			} else {
				mIsPermissionCheckDone = true;
			}
		} else {
			// no permission checks
			mIsPermissionCheckDone = true;
		}

		findViewById(R.id.closeButton).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		mDebugImageView = findViewById(R.id.debugImageView);
		mInDebugMode = getIntent().getBooleanExtra("debug", false);
		if (!mInDebugMode) {
			mDebugImageView.setVisibility(View.INVISIBLE);
		}
		setViewIds(R.id.cardRectangle, R.id.shadedBackground, R.id.texture,
				R.id.cardNumber, R.id.expiry);
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
			Log.d("ASDASDASDASDASD", String.valueOf(bitmap.getHeight()));
			mDebugImageView.setImageBitmap(ImageUtils.drawBoxesOnImage(bitmap, digitBoxes, expiryBox));
			Log.d(TAG, "Prediction (ms): " + (SystemClock.uptimeMillis() - mPredictionStartMs));
			if (startTimeMs != 0) {
				Log.d(TAG, "time to first prediction: " + (SystemClock.uptimeMillis() - startTimeMs));
				startTimeMs = 0;
			}
		}

		super.onPrediction(number, expiry, bitmap, digitBoxes, expiryBox);
	}

	public final void setTopTextFont(Typeface fontFamily) {
		((TextView) findViewById(R.id.topText)).setTypeface(fontFamily);
	}
}
