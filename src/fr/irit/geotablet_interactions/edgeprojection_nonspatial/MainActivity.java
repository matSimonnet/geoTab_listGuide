package fr.irit.geotablet_interactions.edgeprojection_nonspatial;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import fr.irit.edgeprojection_nonspatial.R;

import fr.irit.geotablet_interactions.common.MyMapView;
import fr.irit.geotablet_interactions.common.MyTTS;
import fr.irit.geotablet_interactions.common.OsmNode;

public class MainActivity extends Activity {
	private Object selectedItem;
	private Set<OsmNode> nodes;
	public MyMapView mapView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mapView = (MyMapView) findViewById(R.id.map_view);
		nodes = mapView.getNodes();
		

		// Populate adapter with nodes
		final ArrayAdapter<OsmNode> adapter = 
				new ArrayAdapter<OsmNode>(
						this,
						android.R.layout.simple_list_item_1, android.R.id.text1,
						new ArrayList<OsmNode>(nodes));
		// Sort nodes by alphabetical order
		adapter.sort(new Comparator<OsmNode>() {

			@Override
			public int compare(OsmNode node1, OsmNode node2) {
				return node1.getName().compareTo(node2.getName());
			}
		});

		final LinearLayout verticalListLayout = (LinearLayout) findViewById(R.id.vertical_list_layout);
		verticalListLayout.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@Override
					// Wait for display layout complete
					// to be able to get view height
					public void onGlobalLayout() {
						// Iterate through nodes and display them
						for (int i = 0; i < adapter.getCount(); i++) {
							View adapterView = adapter.getView(i, null, null);
							adapterView.setLayoutParams(
									new LayoutParams(
											LayoutParams.MATCH_PARENT,
											verticalListLayout.getHeight() / adapter.getCount()));
							verticalListLayout.addView(adapterView);
						}
						try {
							verticalListLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
						} catch (NoSuchMethodError x) {
							verticalListLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
						}
					}
				});

		// Set listener to the layout
		verticalListLayout.setOnTouchListener(new ListTouchListener(this, adapter));

		// Set listener to the map view
		mapView.setOnTouchListener(new MapViewTouchListener(this));
	}

	public Set<OsmNode> getOsmNodes(){
		return nodes;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		MyTTS.release();
		super.onDestroy();
	}

	/**
	 * Getter for selected item in the list (to be guided to)
	 * 
	 * @return The selected item
	 */
	public Object getSelectedItem() {
		return selectedItem;
	}

	/**
	 * Setter for selected item in the list (to be guided to)
	 * 
	 * @param selectedItem
	 *            The selected item
	 */
	public void setSelectedItem(Object selectedItem) {
		this.selectedItem = selectedItem;
	}
	


}
