package scripts.radial.symmetry.process;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import net.imglib2.Cursor;
import net.imglib2.FinalInterval;
import net.imglib2.RandomAccessible;
import net.imglib2.RealPoint;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;
import net.imglib2.img.Img;
import net.imglib2.interpolation.randomaccess.NLinearInterpolatorFactory;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.util.RealSum;
import net.imglib2.view.Views;

import cluster.radial.symmetry.process.ImageData;
import radial.symmetry.utils.IOUtils;
import util.ImgLib2Util;
import util.NotSoUsefulOutput;

public class ProcessIntronsAndDapi {

	// take smFISH detections in exon channel and find intensities for the corresponding locations 
	// in intron and dapi channels (in general, any other channel)
	
	// same as process image but for all good images
	public static void processImages(File anotherChannelImagesPath, File exonFolderPath, File anotherChannelFolderPath, File smFishDbPath) {
		// read the list of the good exon images 
		// imageData contains the information about all images
		String [] types = new String[] {"DPY-23_EX", "DPY-23_INT", "DAPI"};
		ArrayList<ImageData> imageData = IOUtils.readDb(smFishDbPath, types);
		// now searching should be faster
		// HashMap<String, ImageData> filteredImageData = new HashMap<>();
		
		//  ArrayList<ImageData> filteredImageData = filterImageData(ArrayList<ImageData> imageData, String type)
		// find the corresponding intron images 
		// DEBUG: remove once down
		NotSoUsefulOutput.printImageDataParameters(imageData);
		// run processing on each image 

		// 
	}
	
	
	// pick images of the specific stain type and without the defects
	public static HashMap<String, ImageData> filterImageData(ArrayList<ImageData> imageData, String type) {
		HashMap<String, ImageData> filteredImageData = new HashMap<>();
		
		for (ImageData id : imageData) {
			if (!id.getDefects() && id.getType().equals(type))
				filteredImageData.put(id.getFilename(), id);
		} 
		
		return filteredImageData;
	}
	
	// grab anotherChannelImagePath and exonPath and put the detections to the intronPath
	public static void processImage(File anotherChannelImagePath, File exonPath, File anotherChannelPath) {
		// reading 
		Img<FloatType> img = ImgLib2Util.openAs32Bit(anotherChannelImagePath);
		ArrayList<RealPoint> exonSpots = IOUtils.readPositionsFromCSV(exonPath, '\t');
		// processing
		ArrayList<Double> allIntensities = calculateAnotherChannelSignals(exonSpots, img);
		// writing 
		// IOUtils.writeIntensitiesToCSV(anotherChannelPath, allIntensities, '\t');
		IOUtils.writePositionsAndIntensitiesToCSV(anotherChannelPath, exonSpots, allIntensities);
	}

	public static ArrayList<Double> calculateAnotherChannelSignals(ArrayList<RealPoint> exonSpots, Img<FloatType> img){
		ArrayList<Double> allIntesities = new ArrayList<>();

		int numDimensions = img.numDimensions();

		NLinearInterpolatorFactory< FloatType > factory = new NLinearInterpolatorFactory<>();
		RealRandomAccessible< FloatType > interpolant = Views.interpolate(Views.extendMirrorSingle( img ), factory);

		int [] kernelSize = new int[] {5, 5, 3};
		long [] min = new long[numDimensions];
		long [] max = new long[numDimensions];
		double [] offset = new double[numDimensions];

		long numPixels = getNumberPixels(kernelSize);

		for (int i = 0; i < exonSpots.size(); i++) {
			RealPoint spot = exonSpots.get(i);
			offset = getOffset(spot);

			for (int d = 0; d < img.numDimensions(); d++) {
				min[d] = (long)spot.getDoublePosition(d) - kernelSize[d]/2;
				max[d] = (long)spot.getDoublePosition(d) + kernelSize[d]/2;
			}

			// DEBUG: remove once done
			// System.out.println(Util.printCoordinates(min));
			// System.out.println(Util.printCoordinates(max));

			FinalInterval interval = new FinalInterval(min, max);
			double intensity = calulateAnotherChannelSignal(exonSpots.get(i), img, interpolant, interval, offset, numPixels);
			allIntesities.add(intensity);
			// break;
		}

		return allIntesities;
	}

	public static long getNumberPixels(int[] kernelSize) {
		long res = 1;
		for (int d = 0; d < kernelSize.length; d++)
			res *= kernelSize[d];
		return res;
	}
	

	public static double[] getOffset(RealPoint spot) {
		double [] offset = new double[spot.numDimensions()];
		for (int d = 0; d < spot.numDimensions(); d++)
			offset[d] = spot.getDoublePosition(d) - (long) spot.getDoublePosition(d);  
		return offset;
	}

	public static double calulateAnotherChannelSignal(RealPoint spot, RandomAccessible<FloatType> img, RealRandomAccessible<FloatType> rImg, FinalInterval interval, double [] offset, long numPixels) {
		Cursor<FloatType> cursor = Views.interval(img, interval).cursor();
		RealRandomAccess<FloatType> rra = rImg.realRandomAccess();

		RealSum sum = new RealSum();

		while(cursor.hasNext()){
			cursor.fwd();

			double [] position = new double[spot.numDimensions()];

			cursor.localize(position);

			rra.setPosition(position);
			rra.move(offset);
			rra.localize(position);

			sum.add(rra.get().get());
		}

		double intensity = sum.getSum()/numPixels;
		return intensity;
	}


	public static void main(String[] args) {
		
		String root = "/Users/kkolyva/Desktop/2018-07-31-09-53-32-N2-all-results-together/smFISH-database";
		String smFishDbFilename = "N2-Table 1 updated.csv";
		
		File anotherChannelImagesPath = new File ("");
		File exonFolderPath = new File ("");
		File anotherChannelFolderPath = new File (""); 
		File smFishDbPath = Paths.get(root, smFishDbFilename).toFile();

		processImages( anotherChannelImagesPath,  exonFolderPath, anotherChannelFolderPath, smFishDbPath);
		
		System.out.println("DOGE!");
	}

}
