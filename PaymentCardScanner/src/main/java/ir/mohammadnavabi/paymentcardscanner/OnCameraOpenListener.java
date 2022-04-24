package ir.mohammadnavabi.paymentcardscanner;

import android.hardware.Camera;

import androidx.annotation.Nullable;

interface OnCameraOpenListener {

	void onCameraOpen(@Nullable Camera camera);

}
