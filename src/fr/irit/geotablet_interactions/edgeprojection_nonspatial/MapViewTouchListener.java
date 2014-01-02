package fr.irit.geotablet_interactions.edgeprojection_nonspatial;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Set;

import org.osmdroid.views.MapView;

import android.content.Context;
import android.graphics.Point;
import android.os.Environment;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import fr.irit.edgeprojection_nonspatial.R;
import fr.irit.geotablet_interactions.common.MyTTS;
import fr.irit.geotablet_interactions.common.OsmNode;

/**
 * Listener to guide user toward the last selected item when finger(s) on the
 * map view
 * 
 * @author helene jonin
 * @mail helene.jonin@gmail.com
 * 
 */
public class MapViewTouchListener implements OnTouchListener {
	private static final int TARGET_SIZE = 96; // Touch target size for on screen elements (dp)

	private static final int INVALID_POINTER_ID = -1;

	private Context context;
	private int activePointerId;
	
	//for logging
	private PrintWriter output;
	private Date myDate;
	private boolean firstTouch = true;
	private String logContact = "nothing";
	private String logAnnounce = "mute";

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The context
	 */
	public MapViewTouchListener(Context context) {
		super();
		this.context = context;
		//create file for logging
		 myDate = new Date();
		 SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd-HHmmss",Locale.getDefault()); 
		 new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/geoTablet/").mkdir();
		 String logFilename = simpleDateFormat.format(new Date())+ "_DirectGuidance_" +".csv";
		 File logFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+ "/geoTablet/" + logFilename);
		     try {
		       output = new PrintWriter(new FileWriter(logFile));
		     } catch (IOException e) {
		       e.printStackTrace();
		     }
	}

	@Override
	public boolean onTouch(View v, MotionEvent ev) {
		int action = MotionEventCompat.getActionMasked(ev);

		switch (action) {
		case MotionEvent.ACTION_DOWN: {
			// Save the ID of this pointer (for dragging)
			activePointerId = MotionEventCompat.getPointerId(ev, 0);
			break;
		}

		case MotionEvent.ACTION_MOVE: {
			// Find the index of the active pointer and fetch its position
			int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);

			float y = MotionEventCompat.getY(ev, pointerIndex);
			float x = MotionEventCompat.getX(ev, pointerIndex);

	
			
			OsmNode nodeToReach = (OsmNode) ((MainActivity) context).getSelectedItem();
			Set<OsmNode> otherNode = (Set<OsmNode>) ((MainActivity) context).getOsmNodes();
			otherNode.remove(nodeToReach);
					
			if (otherNode != null){
				for (OsmNode n : otherNode) {
					Point otherPoint = n.toPoint((MapView) v);
					if ((otherPoint.y <= y + TARGET_SIZE / 2)
							&& (otherPoint.y >= y - TARGET_SIZE / 2)
							&& (otherPoint.x <= x + TARGET_SIZE / 2)
							&& (otherPoint.x >= x - TARGET_SIZE / 2)) {
						if (!MyTTS.getInstance(context).isSpeaking()) {
							((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(150);
							MyTTS.getInstance(context).setPitch(1.5f);
							MyTTS.getInstance(context).speak(
									n.getName(),
									TextToSpeech.QUEUE_ADD,
									null);
							logAnnounce = n.getName();
						}
						 logContact = n.getName();
					}
				}
			}
			
			
			
			if (nodeToReach != null) {
				Point pointToReach = nodeToReach.toPoint((MapView) v);

				if (y - pointToReach.y < -TARGET_SIZE / 2) { // If too high
					if (!MyTTS.getInstance(context).isSpeaking()) {
						MyTTS.getInstance(context).setPitch(1.5f);
						MyTTS.getInstance(context).speak(
								context.getResources().getString(R.string.bottom),
								TextToSpeech.QUEUE_ADD, null);
					}
				} else if (y - pointToReach.y > TARGET_SIZE / 2) { // If too low
					if (!MyTTS.getInstance(context).isSpeaking()) {
						MyTTS.getInstance(context).setPitch(1.5f);
						MyTTS.getInstance(context).speak(
								context.getResources().getString(R.string.up),
								TextToSpeech.QUEUE_ADD, null);
					}
				} else if (x - pointToReach.x < -TARGET_SIZE / 2) { // If too much on the left
					if (!MyTTS.getInstance(context).isSpeaking()) {
						MyTTS.getInstance(context).setPitch(1.5f);
						MyTTS.getInstance(context).speak(
								context.getResources().getString(R.string.right),
								TextToSpeech.QUEUE_ADD, null);
					}
				} else if (x - pointToReach.x > TARGET_SIZE / 2) { // If too much on the right
					if (!MyTTS.getInstance(context).isSpeaking()) {
						MyTTS.getInstance(context).setPitch(1.5f);
						MyTTS.getInstance(context).speak(
								context.getResources().getString(R.string.left),
								TextToSpeech.QUEUE_ADD, null);
					}
				} else {
					if (!MyTTS.getInstance(context).isSpeaking()) { // Found
						((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(150);
						MyTTS.getInstance(context).setPitch(1.5f);
						MyTTS.getInstance(context).speak(
								nodeToReach.getName() + " " +
								context.getResources().getString(R.string.found),
								TextToSpeech.QUEUE_ADD, null);
						logAnnounce = nodeToReach.getName() + " found";
					}
					logContact = nodeToReach.getName() + " found";
				}
			}

			//for logging
			double lat = ((MainActivity) context).mapView.getProjection().fromPixels(x, y).getLatitudeE6();
			double lon = ((MainActivity) context).mapView.getProjection().fromPixels(x, y).getLongitudeE6();
			Datalogger(x,y,lat,lon,logContact,logAnnounce);
			logAnnounce = "mute";
			logContact = "nothing";

			break;
		}
		
		
		
		
		//Hélène's code to only display nodeToReach
		/*case MotionEvent.ACTION_MOVE: {
			// Find the index of the active pointer and fetch its position
			int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);

			float y = MotionEventCompat.getY(ev, pointerIndex);
			float x = MotionEventCompat.getX(ev, pointerIndex);

			OsmNode nodeToReach = (OsmNode) ((MainActivity) context).getSelectedItem();
			if (nodeToReach != null) {
				Point pointToReach = nodeToReach.toPoint((MapView) v);

				if (y - pointToReach.y < -TARGET_SIZE / 2) { // If too high
					if (!MyTTS.getInstance(context).isSpeaking()) {
						MyTTS.getInstance(context).speak(
								context.getResources().getString(R.string.bottom),
								TextToSpeech.QUEUE_ADD, null);
					}
				} else if (y - pointToReach.y > TARGET_SIZE / 2) { // If too low
					if (!MyTTS.getInstance(context).isSpeaking()) {
						MyTTS.getInstance(context).speak(
								context.getResources().getString(R.string.up),
								TextToSpeech.QUEUE_ADD, null);
					}
				} else if (x - pointToReach.x < -TARGET_SIZE / 2) { // If too much on the left
					if (!MyTTS.getInstance(context).isSpeaking()) {
						MyTTS.getInstance(context).speak(
								context.getResources().getString(R.string.right),
								TextToSpeech.QUEUE_ADD, null);
					}
				} else if (x - pointToReach.x > TARGET_SIZE / 2) { // If too much on the right
					if (!MyTTS.getInstance(context).isSpeaking()) {
						MyTTS.getInstance(context).speak(
								context.getResources().getString(R.string.left),
								TextToSpeech.QUEUE_ADD, null);
					}
				} else {
					if (!MyTTS.getInstance(context).isSpeaking()) { // Found
						((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(150);
						MyTTS.getInstance(context).speak(
								context.getResources().getString(R.string.found)
								+ " " + nodeToReach.getName(),
								TextToSpeech.QUEUE_ADD, null);
					}
				}
			}

			break;
		}*/

		case MotionEvent.ACTION_UP: {
			activePointerId = INVALID_POINTER_ID;
			break;
		}

		case MotionEvent.ACTION_CANCEL: {
			activePointerId = INVALID_POINTER_ID;
			break;
		}

		case MotionEvent.ACTION_POINTER_UP: {

			int pointerIndex = MotionEventCompat.getActionIndex(ev);
			int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);

			if (pointerId == activePointerId) {
				// This was our active pointer going up. Choose a new
				// active pointer and adjust accordingly.
				int newPointerIndex = pointerIndex == 0 ? 1 : 0;
				activePointerId = MotionEventCompat.getPointerId(ev,
						newPointerIndex);
			}

			break;
		}
		}
		return true;
	}


public void Datalogger (float x, float y, double lat, double lon, String logContact, String logAnnounce){
    if (firstTouch){
    output.println("time(ms);x;y;lat;lon;contact;annonce");
    firstTouch = false;
    }
    Date touchDate = new Date();
    String str = touchDate.getTime()-myDate.getTime() + ";" 
    + (int)x + ";" + (int)y + ";" 
    + lat/100000 + ";" + lon/100000 + ";"
    + logContact + ";" + logAnnounce;
    Log.e("log",str);
    output.println(str);
    output.flush();
  }
	
}
