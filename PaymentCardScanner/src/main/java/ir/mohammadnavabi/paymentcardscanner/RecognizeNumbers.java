package ir.mohammadnavabi.paymentcardscanner;

import android.graphics.Bitmap;

import java.util.ArrayList;

class RecognizeNumbers {

	private RecognizedDigits[][] recognizedDigits;
	private final Bitmap image;

	RecognizeNumbers(Bitmap image, int numRows, int numCols) {
		this.image = image;
		this.recognizedDigits = new RecognizedDigits[numRows][numCols];
	}

	String number(RecognizedDigitsModel model, ArrayList<ArrayList<DetectedBox>> lines) {
		for (ArrayList<DetectedBox> line : lines) {
			StringBuilder candidateNumber = new StringBuilder();

			for (DetectedBox word : line) {
				RecognizedDigits recognized = this.cachedDigits(model, word);
				if (recognized == null) {
					return null;
				}

				candidateNumber.append(recognized.stringResult());
			}

			// Check for both traditional 16-digit cards and multi-line cards
			if (candidateNumber.length() == 16 && DebitCardUtils.luhnCheck(candidateNumber.toString())) {
				return candidateNumber.toString();
			}

			// For multi-line cards, we might get 16 digits from 4 lines of 4 digits each
			// The combined sequence should be 16 digits total
			if (candidateNumber.length() == 16) {
				// Try Luhn check for the combined number
				if (DebitCardUtils.luhnCheck(candidateNumber.toString())) {
					return candidateNumber.toString();
				}
			}
		}

		return null;
	}

	private RecognizedDigits cachedDigits(RecognizedDigitsModel model, DetectedBox box) {
		if (this.recognizedDigits[box.row][box.col] == null) {
			this.recognizedDigits[box.row][box.col] = RecognizedDigits.from(model, image, box.getRect());
		}

		return this.recognizedDigits[box.row][box.col];
	}

}
