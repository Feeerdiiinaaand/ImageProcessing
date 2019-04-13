import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import javax.imageio.ImageIO;

public class ImageProcessor {
	BufferedImage image;
	BufferedImage newImage;
	File file;
	int[][] red, green, blue;
	Graphics2D graphics;
	int width, height;

	public ImageProcessor(String path) throws IOException {
		java.util.Locale.setDefault(java.util.Locale.ENGLISH);

		Scanner scanner = new Scanner(System.in);
		System.out.println("Please enter a command... (help for a list of commands)");

		String s = scanner.next();

		while (!s.equals("exit")) {
			if (s.equals("help")) {
				printHelp();
			}
			image = ImageIO.read(this.getClass().getResource(path));

			width = image.getWidth();
			height = image.getHeight();

			getRGB();
			getNewImage();

			int x = chooseMethod(s, scanner);
			if (x == 1) {
				draw();
				createFile();
				System.out.println("Finished");
			} else if (x == 2) {
				System.out.println("Entering Multiple-Mode");
				System.out.println("Please enter a command... (help for a list of commands)");
				s = scanner.next();
				while (!s.equals("exit")) {
					chooseMethod(s, scanner);
					System.out.println("Enter the next one..");
					s = scanner.next();
				}
				System.out.println("Multiple-Mode-Exited\nFile is being created..");
				draw();
				createFile();
			}

			System.out.println("Please enter a command... (help for a list of commands)");
			s = scanner.next();
		}
		System.out.println("Succesfully exited");
	}

	private void printHelp() {
		System.out.println("- invert");
		System.out.println("- remColors");
		System.out.println("- toRed");
		System.out.println("- toGreen");
		System.out.println("- toBlue");
		System.out.println("- draw");
		System.out.println("- shuffleHalf");
		System.out.println("- mirrorSideWays");
		System.out.println("- mirrorUpsideDown");
		System.out.println("- mirrorAll");
		System.out.println("- shuffle");
		System.out.println("- dither");
		System.out.println("- blackAndWhite");
		System.out.println("- blur");
		System.out.println("- drawEdges");
		System.out.println("- drawColoredEdges");
		System.out.println("- exit (exits the program)");
		System.out.println("- multiple (to Enter multiple commands)");
	}

	private int chooseMethod(String s, Scanner scanner) {

		if (s.equals("invert"))
			invert();
		else if (s.equals("remColors"))
			remColors();
		else if (s.equals("toRed")) {
			System.out.println("Enter the intensity of the other colors [0-1]");
			double factor = scanner.nextDouble();
			toRed(factor);
		} else if (s.equals("toGreen")) {
			System.out.println("Enter the intensity of the other colors [0-1]");
			double factor = scanner.nextDouble();
			toGreen(factor);
		} else if (s.equals("toBlue")) {
			System.out.println("Enter the intensity of the other colors [0-1]");
			double factor = scanner.nextDouble();
			toBlue(factor);
		} else if (s.equals("draw"))
			draw();
		else if (s.equals("shuffleHalf"))
			shuffleHalf();
		else if (s.equals("mirrorSideWays"))
			mirrorSideWays();
		else if (s.equals("mirrorUpsideDown"))
			mirrorUpsideDown();
		else if (s.equals("mirrorAll"))
			mirrorAll();
		else if (s.equals("shuffle"))
			shuffle();
		else if (s.equals("dither"))
			dither();
		else if (s.equals("blackAndWhite"))
			blackAndWhite();
		else if (s.equals("blur")) {
			System.out.println(
					"Enter the intensity of the blur ]0-1] (High values can take several minuts to infinite but depends on the image size ;Too small values may end in devision by zero)");
			double factor = scanner.nextDouble();
			blur(factor);
		} else if (s.equals("drawEdges"))
			drawEdges();
		else if (s.equals("drawColoredEdges"))
			drawColoredEdges();
		else if (s.equals("multiple"))
			return 2;
		else {
			System.out.println("Try Again..");
			return 0;
		}
		return 1;

	}

	private void drawColoredEdges() {
		remColors();

		int[][] xFilter = new int[][] { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
		int[][] yFilter = new int[][] { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };

		int[][] oldRed = copyArr(red);

		int[][] xDiffArr = new int[width][height];
		int[][] yDiffArr = new int[width][height];
		int[][] diffArr = new int[width][height];

		float[][] arcTanArr = new float[width][height];

		for (int i = 0; i < width - 2; i++) {
			for (int j = 0; j < height - 2; j++) {
				int xDiff = 0;
				int yDiff = 0;

				for (int k = 0; k < xFilter.length; k++) {
					for (int l = 0; l < xFilter[0].length; l++) {
						xDiff += xFilter[k][l] * oldRed[i + k][j + l];
						yDiff += yFilter[k][l] * oldRed[i + k][j + l];
					}
				}
				xDiffArr[i + 1][j + 1] = xDiff;
				yDiffArr[i + 1][j + 1] = yDiff;
			}
		}

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				diffArr[i][j] = (int) Math.sqrt(xDiffArr[i][j] * xDiffArr[i][j] + yDiffArr[i][j] * yDiffArr[i][j]);
			}
		}

		int min = minArrValue(diffArr);
		int max = maxArrValue(diffArr);

		mapArrValues(diffArr, min, max);

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				red[i][j] = diffArr[i][j];
				green[i][j] = diffArr[i][j];
				blue[i][j] = diffArr[i][j];
			}
		}

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (xDiffArr[i][j] != 0)
					arcTanArr[i][j] = (float) Math.toDegrees(Math.atan(yDiffArr[i][j] / xDiffArr[i][j]));
				else
					arcTanArr[i][j] = (float) Math.toDegrees(yDiffArr[i][j] > 0 ? 1.5707f : -1.5707);
			}
		}

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				if (red[i][j] >10) {
					Color color = getColorFromATan(arcTanArr[i][j]);
					red[i][j] = color.getRed();
					green[i][j] = color.getGreen();
					blue[i][j] = color.getBlue();
				}
			}
		}

	}

	private Color getColorFromATan(double tan) {
		int red = 0, green = 0, blue = 0;
		if (tan < -60) {
			red = 255;
			blue = map30to255(-tan - 60);
		} else if (tan < -30) {
			blue = 255;
			red = map30to255Reversed(-tan - 30);
		} else if (tan < 0) {
			blue = 255;
			green = map30to255(-tan);
		} else if (tan < 30) {
			green = 255;
			blue = map30to255Reversed(tan);
		} else if (tan < 60) {
			green = 255;
			red = map30to255(tan - 30);
		} else {
			red = 255;
			green = map30to255Reversed(tan - 60);
		}
		return new Color(red, green, blue);
	}

	private int map30to255Reversed(double d) {
		double factor = d / 30.0;
		return (int) (255 - 255 * factor);
	}

	private int map30to255(double d) {
		double factor = d / 30.0;
		return (int) (255 * factor);
	}

	private void drawEdges() {
		remColors();

		int[][] xFilter = new int[][] { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };
		int[][] yFilter = new int[][] { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };

		int[][] oldRed = copyArr(red);

		int[][] xDiffArr = new int[width][height];
		int[][] yDiffArr = new int[width][height];
		int[][] diffArr = new int[width][height];

		for (int i = 0; i < width - 2; i++) {
			for (int j = 0; j < height - 2; j++) {
				int xDiff = 0;
				int yDiff = 0;

				for (int k = 0; k < xFilter.length; k++) {
					for (int l = 0; l < xFilter[0].length; l++) {
						xDiff += xFilter[k][l] * oldRed[i + k][j + l];
						yDiff += yFilter[k][l] * oldRed[i + k][j + l];
					}
				}
				xDiffArr[i + 1][j + 1] = xDiff;
				yDiffArr[i + 1][j + 1] = yDiff;
			}
		}

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				diffArr[i][j] = (int) Math.sqrt(xDiffArr[i][j] * xDiffArr[i][j] + yDiffArr[i][j] * yDiffArr[i][j]);
			}
		}

		int min = minArrValue(diffArr);
		int max = maxArrValue(diffArr);

		mapArrValues(diffArr, min, max);

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				red[i][j] = diffArr[i][j];
				green[i][j] = diffArr[i][j];
				blue[i][j] = diffArr[i][j];
			}
		}
	}

	private int[][] mapArrValues(int[][] arr, int yMin, int yMax) {
		int diff = yMax - yMin;
		double factor = 255.0 / diff;
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				arr[i][j] = (int) (factor * Math.abs(yMin) + factor * arr[i][j]);
			}
		}

		return arr;
	}

	private int maxArrValue(int[][] arr) {
		int max = Integer.MIN_VALUE;
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				if (arr[i][j] > max)
					max = arr[i][j];
			}
		}
		return max;
	}

	private int minArrValue(int[][] arr) {
		int min = Integer.MAX_VALUE;
		for (int i = 0; i < arr.length; i++) {
			for (int j = 0; j < arr[i].length; j++) {
				if (arr[i][j] < min)
					min = arr[i][j];
			}
		}
		return min;
	}

	private void blur(double blurStrength) {
		int[][] oldRed = copyArr(red);
		int[][] oldGreen = copyArr(green);
		int[][] oldBlue = copyArr(blue);

		int xSize = (int) (blurStrength * width);
		int ySize = (int) (blurStrength * height);

		int[][] blurFilter = new int[xSize][ySize];

		for (int i = 0; i < blurFilter.length; i++) {
			for (int j = 0; j < blurFilter[i].length; j++) {
				blurFilter[i][j] = 1;
			}
		}
		int divider = sizeOfFilter(blurFilter);

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int newRed = 0;
				int newGreen = 0;
				int newBlue = 0;
				for (int k = 0; k < blurFilter.length; k++) {
					for (int l = 0; l < blurFilter[k].length; l++) {
						if (i + k >= width || j + l >= height)
							continue;
						newRed += blurFilter[k][l] * oldRed[i + k][j + l];
						newGreen += blurFilter[k][l] * oldGreen[i + k][j + l];
						newBlue += blurFilter[k][l] * oldBlue[i + k][j + l];
					}
				}

				newRed /= divider;
				newGreen /= divider;
				newBlue /= divider;

				int xIndex = i + (int) (0.5 * xSize);
				int yIndex = j + (int) (0.5 * ySize);

				if (xIndex >= width || yIndex >= height)
					continue;

				red[xIndex][yIndex] = newRed;
				green[xIndex][yIndex] = newGreen;
				blue[xIndex][yIndex] = newBlue;

			}
		}

	}

	private int sizeOfFilter(int[][] blurFilter) {
		int x = 0;
		for (int i = 0; i < blurFilter.length; i++) {
			for (int j = 0; j < blurFilter[i].length; j++) {
				x += blurFilter[i][j];
			}
		}
		return x;
	}

	private void getNewImage() {
		newImage = new BufferedImage(width, height, image.getType());
		graphics = newImage.createGraphics();
	}

	private void blackAndWhite() {
		remColors();

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int color = red[i][j];
				int newColor = color > 127 ? 255 : 0;
				red[i][j] = newColor;
				green[i][j] = newColor;
				blue[i][j] = newColor;
			}
		}
	}

	private void dither() {
		remColors();

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int color = red[i][j];
				int newColor = 0;
				if (i % 2 == 0) {
					if (j % 2 == 0) {
						if (color > 64)
							newColor = 255;
					} else {
						if (color > 128)
							newColor = 255;
					}
				} else {
					if (j % 2 == 0) {
						if (color > 192)
							newColor = 255;
					} else {
						if (color > 0)
							newColor = 255;
					}
				}
				red[i][j] = newColor;
				green[i][j] = newColor;
				blue[i][j] = newColor;
			}
		}

	}

	private void shuffle() {

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int xRan = 0, yRan = 0;
				xRan = (int) (Math.random() * width);
				yRan = (int) (Math.random() * height);

				int redOld = red[xRan][yRan];
				int greenOld = green[xRan][yRan];
				int blueOld = blue[xRan][yRan];

				red[xRan][yRan] = red[i][j];
				green[xRan][yRan] = green[i][j];
				blue[xRan][yRan] = blue[i][j];

				red[i][j] = redOld;
				green[i][j] = greenOld;
				blue[i][j] = blueOld;

			}
		}
	}

	private void mirrorAll() {
		int[][] oldRed = copyArr(red);
		int[][] oldGreen = copyArr(green);
		int[][] oldBlue = copyArr(blue);

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				red[i][j] = oldRed[width - i - 1][height - j - 1];
				green[i][j] = oldGreen[width - i - 1][height - j - 1];
				blue[i][j] = oldBlue[width - i - 1][height - j - 1];
			}
		}
	}

	private void mirrorUpsideDown() {
		int[][] oldRed = copyArr(red);
		int[][] oldGreen = copyArr(green);
		int[][] oldBlue = copyArr(blue);

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				red[i][j] = oldRed[i][height - j - 1];
				green[i][j] = oldGreen[i][height - j - 1];
				blue[i][j] = oldBlue[i][height - j - 1];
			}
		}
	}

	private void mirrorSideWays() {
		int[][] oldRed = copyArr(red);
		int[][] oldGreen = copyArr(green);
		int[][] oldBlue = copyArr(blue);

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				red[i][j] = oldRed[width - i - 1][j];
				green[i][j] = oldGreen[width - i - 1][j];
				blue[i][j] = oldBlue[width - i - 1][j];
			}
		}
	}

	private void createFile() {
		file = new File("res/newImage.jpg");
		try {
			ImageIO.write(newImage, "jpg", file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void shuffleHalf() {

		for (int i = 0; i < width; i += 2) {
			for (int j = 0; j < height; j += 2) {
				int xRan = 0, yRan = 0;
				xRan = (int) (Math.random() * width);
				yRan = (int) (Math.random() * height);

				int redOld = red[xRan][yRan];
				int greenOld = green[xRan][yRan];
				int blueOld = blue[xRan][yRan];

				red[xRan][yRan] = red[i][j];
				green[xRan][yRan] = green[i][j];
				blue[xRan][yRan] = blue[i][j];

				red[i][j] = redOld;
				green[i][j] = greenOld;
				blue[i][j] = blueOld;

			}
		}
	}

	private int[][] copyArr(int[][] arr) {
		int[][] newArr = new int[arr.length][];
		for (int i = 0; i < arr.length; i++) {
			newArr[i] = new int[arr[i].length];
			for (int j = 0; j < arr[i].length; j++) {
				newArr[i][j] = arr[i][j];
			}
		}
		return newArr;
	}

	private void draw() {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				graphics.setColor(new Color(red[i][j], green[i][j], blue[i][j]));
				graphics.drawLine(i, j, i, j);
			}
		}
	}

	private void toColor(double factor, int index) {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				switch (index) {
				case 0:
					green[i][j] = (int) (factor * green[i][j]);
					blue[i][j] = (int) (factor * blue[i][j]);
					break;
				case 1:
					red[i][j] = (int) (factor * red[i][j]);
					blue[i][j] = (int) (factor * blue[i][j]);
					break;
				case 2:
					red[i][j] = (int) (factor * red[i][j]);
					green[i][j] = (int) (factor * green[i][j]);
					break;

				}
			}
		}
	}

	private void toRed(double factor) {
		toColor(factor, 0);
	}

	private void toGreen(double factor) {
		toColor(factor, 1);
	}

	private void toBlue(double factor) {
		toColor(factor, 2);
	}

	private void remColors() {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int newColor = (red[i][j] + green[i][j] + blue[i][j]) / 3;
				red[i][j] = newColor;
				green[i][j] = newColor;
				blue[i][j] = newColor;
			}
		}
	}

	private void invert() {
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				red[i][j] = 255 - red[i][j];
				green[i][j] = 255 - green[i][j];
				blue[i][j] = 255 - blue[i][j];
			}
		}
	}

	private void getRGB() {
		red = new int[width][height];
		green = new int[width][height];
		blue = new int[width][height];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int pixel = image.getRGB(i, j);
				red[i][j] = (pixel & 0x00ff0000) >> 16;
				green[i][j] = (pixel & 0x0000ff00) >> 8;
				blue[i][j] = pixel & 0x000000ff;
			}
		}
	}

	public static void main(String[] args) throws IOException {
		ImageProcessor ip = new ImageProcessor("urlaub.jpg");
	}
}
