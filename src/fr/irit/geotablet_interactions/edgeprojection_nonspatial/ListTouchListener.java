package fr.irit.geotablet_interactions.edgeprojection_nonspatial;

import fr.irit.geotablet_interactions.common.MyTTS;
import android.content.Context;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Adapter;
import fr.irit.geotablet_interactions.edgeprojection_nonspatial.MapViewTouchListener;
/**
 * Listener to scroll with finger(s) and read with TTS a list from an adapter
 * 
 * @author helene jonin
 * @mail helene.jonin@gmail.com
 * 
 */
public class ListTouchListener implements OnTouchListener {
	private static final int INVALID_POINTER_ID = -1;

	private Context context;
	private Adapter adapter;
	private int lastAdapterIndex;
	private int activePointerId;
	private MapViewTouchListener mapViewTouchListener;
	private String logAnnounce = "mute";

	/**
	 * Constructor
	 * 
	 * @param context
	 *            The context
	 * @param adapter
	 *            An adapter (whatever adapter ?)
	 */
	public ListTouchListener(Context context, Adapter adapter) {
		super();
		this.context = context;
		this.adapter = adapter;
		this.lastAdapterIndex = -1;
		mapViewTouchListener = new MapViewTouchListener(context);
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

			// Calculate adapter index to say
			int adapterIndex = (int) (MotionEventCompat.getY(ev, pointerIndex) / (v.getHeight() / adapter.getCount()));
			float y = MotionEventCompat.getY(ev, pointerIndex);
			float x = MotionEventCompat.getX(ev, pointerIndex);

			// If the index is valid (>= 0 and less than items count),
			// and different from the last one said or the TTS is not speaking
			if ((adapterIndex >= 0)
					&& (adapterIndex < adapter.getCount())
					&& ((lastAdapterIndex != adapterIndex)
						|| !MyTTS.getInstance(context).isSpeaking())) {
				Object selectedItem = adapter.getItem(adapterIndex);
				((Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE)).vibrate(150);
				MyTTS.getInstance(context).setPitch(0.5f);
				MyTTS.getInstance(context).speak(selectedItem.toString(), TextToSpeech.QUEUE_FLUSH, null);
				((MainActivity) context).setSelectedItem(selectedItem);
				lastAdapterIndex = adapterIndex;
				logAnnounce = selectedItem.toString();
			}

			try {
				mapViewTouchListener.Datalogger(x, y, 0, 0, "list", logAnnounce);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			
			break;
			
			
		}

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
				activePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
			}

			break;
		}
		}
		return true;
	}

}
