package ir.mohammadnavabi.paymentcardscanner;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

class CameraThread extends Thread {

	private OnCameraOpenListener listener;

	synchronized void startCamera(OnCameraOpenListener listener) {
		this.listener = listener;
		notify();
	}

	private synchronized OnCameraOpenListener waitForOpenRequest() {
		while (this.listener == null) {
			try {
				wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		OnCameraOpenListener listener = this.listener;
		this.listener = null;
		return listener;
	}

	@Override
	public void run() {
		while (true) {
			final OnCameraOpenListener listener = waitForOpenRequest();

			Camera camera = null;
			int retryCount = 0;
			int maxRetries = 3;

			while (camera == null && retryCount < maxRetries) {
				try {
					Log.d("CameraThread",
							"Attempting to open camera... (attempt " + (retryCount + 1) + "/" + maxRetries + ")");
					camera = Camera.open();
					if (camera != null) {
						Log.d("CameraThread", "Camera opened successfully");
						break;
					} else {
						Log.e("CameraThread", "Camera.open() returned null");
					}
				} catch (Exception e) {
					Log.e("CameraThread",
							"Failed to open Camera (attempt " + (retryCount + 1) + "): " + e.getMessage());
					e.printStackTrace();
				} catch (Error e) {
					Log.e("CameraThread", "Error opening Camera (attempt " + (retryCount + 1) + "): " + e.getMessage());
					e.printStackTrace();
				}

				retryCount++;
				if (camera == null && retryCount < maxRetries) {
					Log.d("CameraThread", "Retrying in 500ms...");
					try {
						Thread.sleep(500);
					} catch (InterruptedException ie) {
						Log.e("CameraThread", "Sleep interrupted");
						break;
					}
				}
			}

			final Camera resultCamera = camera;
			Handler handler = new Handler(Looper.getMainLooper());
			handler.post(new Runnable() {
				@Override
				public void run() {
					Log.d("CameraThread",
							"Notifying listener with camera result: " + (resultCamera != null ? "SUCCESS" : "FAILED"));
					listener.onCameraOpen(resultCamera);
				}
			});
		}
	}
}
