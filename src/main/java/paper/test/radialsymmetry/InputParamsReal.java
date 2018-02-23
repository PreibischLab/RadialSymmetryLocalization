package paper.test.radialsymmetry;

import parameters.GUIParams;
import parameters.RadialSymmetryParameters;

public class InputParamsReal {
	// stores input parameters for images and csv files
	public  String path;
	public  String imgName;
	public  int numDimensions;
	
	// DoG parameters
	public  float sigmaDog; 
	public  float threshold;

	// RANSAC parameters
	public  boolean RANSAC;
	public  int supportRadius; // this one I know
	public  float maxError; 
	public  float inlierRatio;

	// Background Subtraction parameters
	public  float bsMaxError;
	public  float bsInlierRatio;
	public  String bsMethod;

	// Gauss Fit over intensities
	public  boolean gaussFit;
	
	public RadialSymmetryParameters rsm;

	// TODO: 
	
	public InputParamsReal(String localPath, int type) {
		// 0 - 2D max projection
		// 1 - 3D image isotropic
		// 2 - 3D image anisotropic
		
		if (type == 0) {
			// input data params
			path = localPath.equals("") ? "/Users/kkolyva/Desktop/2018-02-21-paper-radial-symmetry-test/" : localPath;
			imgName = "test-2D-image";
			numDimensions = 2;
			
			// radial symmetry params
			// this parameters should come from the manual adjustment
			// DoG parameters
			sigmaDog = 1.0f; 
			threshold = 0.0120f;

			// RANSAC parameters
			RANSAC = true;
			supportRadius = 2; // this one I know
			maxError = 0.6f; 
			inlierRatio = 0.5f;

			// Background Subtraction parameters
			bsMaxError = 0.05f;
			bsInlierRatio = 0.75f;
			bsMethod = "No background subtraction";

			// Gauss Fit over intensities
			gaussFit = false;
			
			// set the parameters from the defaults
			final GUIParams params = new GUIParams();

			params.setRANSAC(RANSAC);
			params.setMaxError(maxError);
			params.setInlierRatio(inlierRatio);
			params.setSupportRadius(supportRadius);
			params.setBsMaxError(bsMaxError);
			params.setBsInlierRatio(bsInlierRatio);
			params.setBsMethod(bsMethod);
			params.setSigmaDog(sigmaDog);
			params.setThresholdDog(threshold);
			params.setGaussFit(gaussFit);

			// back up the parameter values to the default variables
			params.setDefaultValues();

			double [] calibration  = new double[numDimensions];
			for (int d = 0; d < numDimensions; d++)
				calibration[d] = 1;
			rsm = new RadialSymmetryParameters(params, calibration);
		}
		else if(type == 1) {
			
		}
		else if (type == 2 ) {
			
		}
		else 
			System.out.println("Wrong data type");
	} 
}
