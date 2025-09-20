package ir.mohammadnavabi.paymentcardscanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * Organize the boxes to find possible numbers.
 * <p>
 * After running detection, the post processing algorithm will try to find
 * sequences of boxes that are plausible card numbers. The basic techniques
 * that it uses are non-maximum suppression and depth first search on box
 * sequences to find likely numbers. There are also a number of heuristics
 * for filtering out unlikely sequences.
 */
class PostDetectionAlgorithm {

	private final int kDeltaRowForCombine = 2;
	private final int kDeltaColForCombine = 2;

	private ArrayList<DetectedBox> sortedBoxes;
	private final int numRows;
	private final int numCols;

	private static Comparator<DetectedBox> colCompare = new Comparator<DetectedBox>() {
		@Override
		public int compare(DetectedBox o1, DetectedBox o2) {
			return (o1.col < o2.col) ? -1 : ((o1.col == o2.col) ? 0 : 1);
		}
	};

	private static Comparator<DetectedBox> rowCompare = new Comparator<DetectedBox>() {
		@Override
		public int compare(DetectedBox o1, DetectedBox o2) {
			return (o1.row < o2.row) ? -1 : ((o1.row == o2.row) ? 0 : 1);
		}
	};

	PostDetectionAlgorithm(ArrayList<DetectedBox> boxes, FindFourModel findFour) {
		this.numCols = findFour.cols;
		this.numRows = findFour.rows;

		this.sortedBoxes = new ArrayList<>();
		Collections.sort(boxes);
		Collections.reverse(boxes);
		for (DetectedBox box : boxes) {
			int kMaxBoxesToDetect = 20;
			if (this.sortedBoxes.size() >= kMaxBoxesToDetect) {
				break;
			}
			this.sortedBoxes.add(box);
		}
	}

	ArrayList<ArrayList<DetectedBox>> horizontalNumbers() {
		ArrayList<DetectedBox> boxes = this.combineCloseBoxes(kDeltaRowForCombine,
				kDeltaColForCombine);

		// First try to find 4 numbers in a single line (traditional cards)
		int kNumberWordCount = 4;
		ArrayList<ArrayList<DetectedBox>> lines = this.findHorizontalNumbers(boxes, kNumberWordCount);

		ArrayList<ArrayList<DetectedBox>> linesOut = new ArrayList<>();
		// boxes should be roughly evenly spaced, reject any that aren't
		for (ArrayList<DetectedBox> line : lines) {
			ArrayList<Integer> deltas = new ArrayList<>();
			for (int idx = 0; idx < (line.size() - 1); idx++) {
				deltas.add(line.get(idx + 1).col - line.get(idx).col);
			}

			Collections.sort(deltas);
			int maxDelta = deltas.get(deltas.size() - 1);
			int minDelta = deltas.get(0);

			if ((maxDelta - minDelta) <= 2) {
				linesOut.add(line);
			}
		}

		// If no single line found, try to find multiple lines with 4 numbers each
		if (linesOut.isEmpty()) {
			linesOut = this.findMultiLineNumbers(boxes);
		}

		// If still no lines found, try rotated card detection (90 degrees)
		if (linesOut.isEmpty()) {
			linesOut = this.findRotatedCardNumbers(boxes);
		}

		return linesOut;
	}

	ArrayList<ArrayList<DetectedBox>> verticalNumbers() {
		ArrayList<DetectedBox> boxes = this.combineCloseBoxes(kDeltaRowForCombine,
				kDeltaColForCombine);
		ArrayList<ArrayList<DetectedBox>> lines = this.findVerticalNumbers(boxes);

		ArrayList<ArrayList<DetectedBox>> linesOut = new ArrayList<>();
		// boxes should be roughly evenly spaced, reject any that aren't
		for (ArrayList<DetectedBox> line : lines) {
			ArrayList<Integer> deltas = new ArrayList<>();
			for (int idx = 0; idx < (line.size() - 1); idx++) {
				deltas.add(line.get(idx + 1).row - line.get(idx).row);
			}

			Collections.sort(deltas);
			int maxDelta = deltas.get(deltas.size() - 1);
			int minDelta = deltas.get(0);

			if ((maxDelta - minDelta) <= 2) {
				linesOut.add(line);
			}
		}

		// If no vertical lines found, try rotated card detection
		if (linesOut.isEmpty()) {
			linesOut = this.findRotatedCardNumbers(boxes);
		}

		return linesOut;
	}

	private boolean horizontalPredicate(DetectedBox currentWord, DetectedBox nextWord) {
		int kDeltaRowForHorizontalNumbers = 1;
		int deltaRow = kDeltaRowForHorizontalNumbers;
		return nextWord.col > currentWord.col && nextWord.row >= (currentWord.row - deltaRow) &&
				nextWord.row <= (currentWord.row + deltaRow);
	}

	private boolean verticalPredicate(DetectedBox currentWord, DetectedBox nextWord) {
		int kDeltaColForVerticalNumbers = 1;
		int deltaCol = kDeltaColForVerticalNumbers;
		return nextWord.row > currentWord.row && nextWord.col >= (currentWord.col - deltaCol) &&
				nextWord.col <= (currentWord.col + deltaCol);
	}

	private void findNumbers(ArrayList<DetectedBox> currentLine, ArrayList<DetectedBox> words,
			boolean useHorizontalPredicate, int numberOfBoxes,
			ArrayList<ArrayList<DetectedBox>> lines) {
		if (currentLine.size() == numberOfBoxes) {
			lines.add(currentLine);
			return;
		}

		if (words.size() == 0) {
			return;
		}

		DetectedBox currentWord = currentLine.get(currentLine.size() - 1);
		if (currentWord == null) {
			return;
		}

		for (int idx = 0; idx < words.size(); idx++) {
			DetectedBox word = words.get(idx);
			if (useHorizontalPredicate && horizontalPredicate(currentWord, word)) {
				ArrayList<DetectedBox> newCurrentLine = new ArrayList<>(currentLine);
				newCurrentLine.add(word);
				findNumbers(newCurrentLine, dropFirst(words, idx + 1), useHorizontalPredicate,
						numberOfBoxes, lines);
			} else if (verticalPredicate(currentWord, word)) {
				ArrayList<DetectedBox> newCurrentLine = new ArrayList<>(currentLine);
				newCurrentLine.add(word);
				findNumbers(newCurrentLine, dropFirst(words, idx + 1), useHorizontalPredicate,
						numberOfBoxes, lines);
			}
		}
	}

	private ArrayList<DetectedBox> dropFirst(ArrayList<DetectedBox> boxes, int n) {
		ArrayList<DetectedBox> result = new ArrayList<>();
		for (int idx = n; idx < boxes.size(); idx++) {
			result.add(boxes.get(idx));
		}
		return result;
	}

	// Note: this is simple but inefficient. Since we're dealing with small
	// lists (eg 20 items) it should be fine
	private ArrayList<ArrayList<DetectedBox>> findHorizontalNumbers(ArrayList<DetectedBox> words,
			int numberOfBoxes) {
		Collections.sort(words, colCompare);
		ArrayList<ArrayList<DetectedBox>> lines = new ArrayList<>();
		for (int idx = 0; idx < words.size(); idx++) {
			ArrayList<DetectedBox> currentLine = new ArrayList<>();
			currentLine.add(words.get(idx));
			findNumbers(currentLine, dropFirst(words, idx + 1), true,
					numberOfBoxes, lines);
		}

		return lines;
	}

	private ArrayList<ArrayList<DetectedBox>> findVerticalNumbers(ArrayList<DetectedBox> words) {
		int numberOfBoxes = 4;
		Collections.sort(words, rowCompare);
		ArrayList<ArrayList<DetectedBox>> lines = new ArrayList<>();
		for (int idx = 0; idx < words.size(); idx++) {
			ArrayList<DetectedBox> currentLine = new ArrayList<>();
			currentLine.add(words.get(idx));
			findNumbers(currentLine, dropFirst(words, idx + 1), false,
					numberOfBoxes, lines);
		}

		return lines;
	}

	/**
	 * Combine close boxes favoring high confidence boxes.
	 */
	private ArrayList<DetectedBox> combineCloseBoxes(int deltaRow, int deltaCol) {
		boolean[][] cardGrid = new boolean[this.numRows][this.numCols];
		for (int row = 0; row < this.numRows; row++) {
			for (int col = 0; col < this.numCols; col++) {
				cardGrid[row][col] = false;
			}
		}

		for (DetectedBox box : this.sortedBoxes) {
			cardGrid[box.row][box.col] = true;
		}

		// since the boxes are sorted by confidence, go through them in order to
		// result in only high confidence boxes winning. There are corner cases
		// where this will leave extra boxes, but that's ok because we don't
		// need to be perfect here
		for (DetectedBox box : this.sortedBoxes) {
			if (!cardGrid[box.row][box.col]) {
				continue;
			}
			for (int row = (box.row - deltaRow); row <= (box.row + deltaRow); row++) {
				for (int col = (box.col - deltaCol); col <= (box.col + deltaCol); col++) {
					if (row >= 0 && row < this.numRows && col >= 0 && col < this.numCols) {
						cardGrid[row][col] = false;
					}
				}
			}

			// add this box back
			cardGrid[box.row][box.col] = true;
		}

		ArrayList<DetectedBox> combinedBoxes = new ArrayList<>();
		for (DetectedBox box : this.sortedBoxes) {
			if (cardGrid[box.row][box.col]) {
				combinedBoxes.add(box);
			}
		}

		return combinedBoxes;
	}

	/**
	 * Find multiple lines of numbers for cards with 4 groups of 4 digits each
	 * This handles the new card format: 1234 5678 9012 3456
	 */
	private ArrayList<ArrayList<DetectedBox>> findMultiLineNumbers(ArrayList<DetectedBox> boxes) {
		ArrayList<ArrayList<DetectedBox>> allLines = new ArrayList<>();

		// Group boxes by their row (line)
		ArrayList<ArrayList<DetectedBox>> linesByRow = new ArrayList<>();
		Collections.sort(boxes, rowCompare);

		int currentRow = -1;
		ArrayList<DetectedBox> currentLine = null;

		for (DetectedBox box : boxes) {
			if (box.row != currentRow) {
				if (currentLine != null && currentLine.size() >= 4) {
					linesByRow.add(currentLine);
				}
				currentLine = new ArrayList<>();
				currentRow = box.row;
			}
			currentLine.add(box);
		}

		// Add the last line if it has enough numbers
		if (currentLine != null && currentLine.size() >= 4) {
			linesByRow.add(currentLine);
		}

		// For each line, try to find 4 numbers
		for (ArrayList<DetectedBox> line : linesByRow) {
			Collections.sort(line, colCompare);

			// Try to find 4 consecutive numbers in this line
			ArrayList<DetectedBox> fourNumbers = findFourConsecutiveNumbers(line);
			if (fourNumbers != null) {
				allLines.add(fourNumbers);
			}
		}

		// If we found 4 lines with 4 numbers each, combine them into one sequence
		if (allLines.size() == 4) {
			ArrayList<DetectedBox> combinedSequence = new ArrayList<>();
			for (ArrayList<DetectedBox> line : allLines) {
				combinedSequence.addAll(line);
			}

			// Sort the combined sequence by position (row first, then col)
			Collections.sort(combinedSequence, new Comparator<DetectedBox>() {
				@Override
				public int compare(DetectedBox o1, DetectedBox o2) {
					if (o1.row != o2.row) {
						return o1.row - o2.row;
					}
					return o1.col - o2.col;
				}
			});

			ArrayList<ArrayList<DetectedBox>> result = new ArrayList<>();
			result.add(combinedSequence);
			return result;
		}

		return allLines;
	}

	/**
	 * Find 4 consecutive numbers in a line
	 */
	private ArrayList<DetectedBox> findFourConsecutiveNumbers(ArrayList<DetectedBox> line) {
		if (line.size() < 4) {
			return null;
		}

		// Try to find 4 numbers that are roughly evenly spaced
		for (int i = 0; i <= line.size() - 4; i++) {
			ArrayList<DetectedBox> candidate = new ArrayList<>();
			for (int j = 0; j < 4; j++) {
				candidate.add(line.get(i + j));
			}

			// Check if the spacing is reasonable
			ArrayList<Integer> deltas = new ArrayList<>();
			for (int idx = 0; idx < 3; idx++) {
				deltas.add(candidate.get(idx + 1).col - candidate.get(idx).col);
			}

			Collections.sort(deltas);
			int maxDelta = deltas.get(deltas.size() - 1);
			int minDelta = deltas.get(0);

			// Allow more flexible spacing for multi-line cards
			if ((maxDelta - minDelta) <= 4) {
				return candidate;
			}
		}

		return null;
	}

	/**
	 * Find numbers for rotated cards (90 degrees)
	 * This handles cards that are rotated vertically
	 */
	private ArrayList<ArrayList<DetectedBox>> findRotatedCardNumbers(ArrayList<DetectedBox> boxes) {
		ArrayList<ArrayList<DetectedBox>> allLines = new ArrayList<>();

		// For rotated cards, we need to look at columns instead of rows
		// Group boxes by their column (since the card is rotated 90 degrees)
		ArrayList<ArrayList<DetectedBox>> linesByCol = new ArrayList<>();
		Collections.sort(boxes, colCompare);

		int currentCol = -1;
		ArrayList<DetectedBox> currentLine = null;

		for (DetectedBox box : boxes) {
			if (box.col != currentCol) {
				if (currentLine != null && currentLine.size() >= 4) {
					linesByCol.add(currentLine);
				}
				currentLine = new ArrayList<>();
				currentCol = box.col;
			}
			currentLine.add(box);
		}

		// Add the last line if it has enough numbers
		if (currentLine != null && currentLine.size() >= 4) {
			linesByCol.add(currentLine);
		}

		// For each column, try to find 4 numbers
		for (ArrayList<DetectedBox> line : linesByCol) {
			Collections.sort(line, rowCompare);

			// Try to find 4 consecutive numbers in this column
			ArrayList<DetectedBox> fourNumbers = findFourConsecutiveNumbersInColumn(line);
			if (fourNumbers != null) {
				allLines.add(fourNumbers);
			}
		}

		// If we found 4 columns with 4 numbers each, combine them into one sequence
		if (allLines.size() == 4) {
			ArrayList<DetectedBox> combinedSequence = new ArrayList<>();
			for (ArrayList<DetectedBox> line : allLines) {
				combinedSequence.addAll(line);
			}

			// Sort the combined sequence by position (col first, then row for rotated
			// cards)
			Collections.sort(combinedSequence, new Comparator<DetectedBox>() {
				@Override
				public int compare(DetectedBox o1, DetectedBox o2) {
					if (o1.col != o2.col) {
						return o1.col - o2.col;
					}
					return o1.row - o2.row;
				}
			});

			ArrayList<ArrayList<DetectedBox>> result = new ArrayList<>();
			result.add(combinedSequence);
			return result;
		}

		// Also try to find single column with 16 numbers (for traditional rotated
		// cards)
		if (allLines.isEmpty()) {
			ArrayList<DetectedBox> singleColumn = findSingleColumnWithSixteenNumbers(boxes);
			if (singleColumn != null) {
				ArrayList<ArrayList<DetectedBox>> result = new ArrayList<>();
				result.add(singleColumn);
				return result;
			}
		}

		return allLines;
	}

	/**
	 * Find 4 consecutive numbers in a column (for rotated cards)
	 */
	private ArrayList<DetectedBox> findFourConsecutiveNumbersInColumn(ArrayList<DetectedBox> line) {
		if (line.size() < 4) {
			return null;
		}

		// Try to find 4 numbers that are roughly evenly spaced vertically
		for (int i = 0; i <= line.size() - 4; i++) {
			ArrayList<DetectedBox> candidate = new ArrayList<>();
			for (int j = 0; j < 4; j++) {
				candidate.add(line.get(i + j));
			}

			// Check if the vertical spacing is reasonable
			ArrayList<Integer> deltas = new ArrayList<>();
			for (int idx = 0; idx < 3; idx++) {
				deltas.add(candidate.get(idx + 1).row - candidate.get(idx).row);
			}

			Collections.sort(deltas);
			int maxDelta = deltas.get(deltas.size() - 1);
			int minDelta = deltas.get(0);

			// Allow flexible spacing for rotated cards
			if ((maxDelta - minDelta) <= 4) {
				return candidate;
			}
		}

		return null;
	}

	/**
	 * Find a single column with 16 numbers (for traditional rotated cards)
	 */
	private ArrayList<DetectedBox> findSingleColumnWithSixteenNumbers(ArrayList<DetectedBox> boxes) {
		// Group boxes by column
		Collections.sort(boxes, colCompare);

		int currentCol = -1;
		ArrayList<DetectedBox> currentColumn = null;

		for (DetectedBox box : boxes) {
			if (box.col != currentCol) {
				if (currentColumn != null && currentColumn.size() >= 16) {
					// Check if this column has 16 numbers with reasonable spacing
					Collections.sort(currentColumn, rowCompare);
					if (isValidSixteenNumberSequence(currentColumn)) {
						return currentColumn;
					}
				}
				currentColumn = new ArrayList<>();
				currentCol = box.col;
			}
			currentColumn.add(box);
		}

		// Check the last column
		if (currentColumn != null && currentColumn.size() >= 16) {
			Collections.sort(currentColumn, rowCompare);
			if (isValidSixteenNumberSequence(currentColumn)) {
				return currentColumn;
			}
		}

		return null;
	}

	/**
	 * Check if a sequence of 16 numbers has valid spacing
	 */
	private boolean isValidSixteenNumberSequence(ArrayList<DetectedBox> sequence) {
		if (sequence.size() < 16) {
			return false;
		}

		// Take the first 16 numbers
		ArrayList<DetectedBox> sixteenNumbers = new ArrayList<>();
		for (int i = 0; i < 16 && i < sequence.size(); i++) {
			sixteenNumbers.add(sequence.get(i));
		}

		// Check vertical spacing between consecutive numbers
		ArrayList<Integer> deltas = new ArrayList<>();
		for (int idx = 0; idx < sixteenNumbers.size() - 1; idx++) {
			deltas.add(sixteenNumbers.get(idx + 1).row - sixteenNumbers.get(idx).row);
		}

		Collections.sort(deltas);
		int maxDelta = deltas.get(deltas.size() - 1);
		int minDelta = deltas.get(0);

		// Allow reasonable spacing variation
		return (maxDelta - minDelta) <= 6;
	}
}
