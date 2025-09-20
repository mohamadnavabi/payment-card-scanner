package ir.mohammadnavabi.paymentcardscanner;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

import java.util.List;

public class ImageUtils {

	public static Bitmap drawBoxesOnImage(Bitmap frame, List<DetectedBox> boxes, DetectedBox expiryBox) {
		if (frame == null) {
			return null;
		}

		try {
			Paint paint = new Paint(0);
			paint.setColor(Color.GREEN);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(3);

			Bitmap mutableBitmap = frame.copy(Bitmap.Config.ARGB_8888, true);
			if (mutableBitmap == null) {
				return frame; // Return original if copy fails
			}

			Canvas canvas = new Canvas(mutableBitmap);

			// Simplified approach - draw boxes directly without complex scaling
			if (boxes != null) {
				for (DetectedBox box : boxes) {
					if (box != null && box.getRect() != null) {
						try {
							RectF rect = box.getRect().getNewInstance();
							canvas.drawRect(rect, paint);
						} catch (Exception e) {
							// Skip this box if there's an error
						}
					}
				}
			}

			paint.setColor(Color.RED);
			if (expiryBox != null && expiryBox.getRect() != null) {
				try {
					RectF rect = expiryBox.getRect().getNewInstance();
					canvas.drawRect(rect, paint);
				} catch (Exception e) {
					// Skip expiry box if there's an error
				}
			}

			return mutableBitmap;
		} catch (Exception e) {
			// Return original frame if anything goes wrong
			return frame;
		}
	}

	/**
	 * Helper method to verify aspect ratio synchronization
	 * Returns true if the image matches the card rectangle aspect ratio (10:17)
	 */
	public static boolean isAspectRatioSynchronized(Bitmap frame) {
		float cardAspectRatio = 17.0f / 10.0f; // height/width ratio
		float imageAspectRatio = (float) frame.getHeight() / (float) frame.getWidth();
		float tolerance = 0.01f; // 1% tolerance

		return Math.abs(imageAspectRatio - cardAspectRatio) < tolerance;
	}

}
