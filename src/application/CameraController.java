package application;

//Skip to content
//Personal Open source Business Explore
//Sign upSign inPricingBlogSupport
//This repository
//Search
// Watch 3  Star 3  Fork 21 opencv-java/getting-started
// Code  Issues 0  Pull requests 0  Pulse  Graphs
//Branch: master Find file Copy pathgetting-started/FXHelloCV/src/it/polito/elite/teaching/cv/FXHelloCVController.java
//a8fbfcf  Oct 13, 2015
//@luigidr luigidr update to OpenCV 3
//1 contributor
//RawBlameHistory     172 lines (151 sloc)  4.11 KB
//package it.polito.elite.teaching.cv;

import java.io.ByteArrayInputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * The controller for our application, where the application logic is
 * implemented. It handles the button for starting/stopping the camera and the
 * acquired video stream.
 * 
 * @author <a href="mailto:luigi.derussis@polito.it">Luigi De Russis</a>
 * @version 1.5 (2015-10-12)
 * @since 1.0 (2013-10-20)
 * 		
 */
public class CameraController {
	// the FXML button
	@FXML
	private Button cameraButton;
	// the FXML image view
	@FXML
	private ImageView currentFrame;
	
	// a timer for acquiring the video stream
	private ScheduledExecutorService timer;
	// the OpenCV object that realizes the video capture
	private VideoCapture capture = new VideoCapture();
	// a flag to change the button behavior
	private boolean cameraActive = false;
	
	private CascadeClassifier faceCascade;
	private int absoluteFaceSize;
	private MatOfRect faces = new MatOfRect();
	
	static{ System.loadLibrary(Core.NATIVE_LIBRARY_NAME); }
	
	/**
	 * The action triggered by pushing the button on the GUI
	 * 
	 * @param event
	 *            the push button event 
	 */
	@FXML
	protected void startCamera(ActionEvent event) {	
		if (!this.cameraActive) {
			// start the video capture
			this.capture.open(0);
			
			// is the video stream available?
			if (this.capture.isOpened()) {
				this.cameraActive = true;
				
				// grab a frame every 33 ms (30 frames/sec)
				Runnable frameGrabber = new Runnable() {
					
					@Override
					public void run() {
						Image imageToShow = grabFrame();
						currentFrame.setImage(imageToShow);
					}
				};

				this.timer = Executors.newSingleThreadScheduledExecutor();
				this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
				
				// update the button content
				this.cameraButton.setText("Stop Camera");
			} else {
				// log the error
				System.err.println("Impossible to open the camera connection...");
			}
		}else {
			// the camera is not active at this point
			this.cameraActive = false;
			// update again the button content
				this.cameraButton.setText("Start Camera");
			
			// stop the timer
			try {
				this.timer.shutdown();
				this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
			}catch (InterruptedException e) {
				// log the exception
				System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
			}
			
			// release the camera
			this.capture.release();
			// clean the frame
			this.currentFrame.setImage(null);
		}
	}
	
	
	
	/**
	 * Get a frame from the opened video stream (if any)
	 * 
	 * @return the {@link Image} to show
	 */
	private Image grabFrame() {
		// init everything
		Image imageToShow = null;
		Mat frame = new Mat();
		
		// check if the capture is open
		if (this.capture.isOpened()) {
			try {
				// read the current frame
				this.capture.read(frame);
				
				// if the frame is not empty, process it
				if (!frame.empty()) {
					
					detectAndDisplay(frame);
					
					// convert the Mat object (OpenCV) to Image (JavaFX)
					imageToShow = mat2Image(frame);
				}
				
			}
			catch (Exception e)
			{
				// log the error
				System.err.println("Exception during the image elaboration: " + e);
			}
		}
		
		return imageToShow;
	}
	
	private void detectAndDisplay(Mat frame) {
		
		Mat grayFrame = new Mat();
		
		// convert the image to gray scale
		Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
		Imgproc.equalizeHist(grayFrame, grayFrame);
		
		if (this.absoluteFaceSize == 0)
		{
		    int height = frame.rows();
		    if (Math.round(height * 0.1f) > 0)
		    {
		            this.absoluteFaceSize = Math.round(height * 0.1f);
		    }
		}
		
		faceCascade.detectMultiScale(frame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(absoluteFaceSize, absoluteFaceSize), new Size());
	
		Rect[] facesArray = faces.toArray();
		for (int i = 0; i < facesArray.length; i++){
		    Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
			Mat cropedImage = fullImage(new Rect(facesArray[i].tl(), facesArray[i].br()));
		}
	}



	/**
	 * Convert a Mat object (OpenCV) in the corresponding Image for JavaFX
	 * 
	 * @param frame
	 *            the {@link Mat} representing the current frame
	 * @return the {@link Image} to show
	 */
	private Image mat2Image(Mat frame) {
		// create a temporary buffer
		MatOfByte buffer = new MatOfByte();
		// encode the frame in the buffer
		Imgcodecs.imencode(".png", frame, buffer);
		// build and return an Image created from the image encoded in the
		// buffer
		return new Image(new ByteArrayInputStream(buffer.toArray()));
	}
	
	@FXML
	public void haarSelected(){
		this.checkboxSelection("resources/lbpcascades/haarcascade_frontalface_alt.xml");
	}
	
	
	@FXML
	public void lbpSelected(){
		this.checkboxSelection("resources/lbpcascades/lbpcascade_frontalface.xml");
	}
	
	
	private void checkboxSelection(String... classifierPath)
	{
	        // load the classifier(s)
	        for (String xmlClassifier : classifierPath)
	        {
	        		this.faceCascade = new CascadeClassifier(xmlClassifier);
//	                this.faceCascade.load(xmlClassifier);
	        }

	        // now the capture can start
	        this.cameraButton.setDisable(false);
	}
	
	
	
}
//Status API Training Shop Blog About
//© 2016 GitHub, Inc. Terms Privacy Security Contact Help