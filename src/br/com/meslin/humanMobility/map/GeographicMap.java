package br.com.meslin.humanMobility.map;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openstreetmap.gui.jmapviewer.Coordinate;
import org.openstreetmap.gui.jmapviewer.JMapViewer;
import org.openstreetmap.gui.jmapviewer.JMapViewerTree;
import org.openstreetmap.gui.jmapviewer.MapMarkerDot;
import org.openstreetmap.gui.jmapviewer.OsmTileLoader;
import org.openstreetmap.gui.jmapviewer.events.JMVCommandEvent;
import org.openstreetmap.gui.jmapviewer.interfaces.JMapViewerEventListener;
import org.openstreetmap.gui.jmapviewer.interfaces.TileLoader;
import org.openstreetmap.gui.jmapviewer.interfaces.TileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.BingAerialTileSource;
import org.openstreetmap.gui.jmapviewer.tilesources.OsmTileSource;

/**
 * Plots a map with persons<br>
 * JavaDoc at https://josm.openstreetmap.de/doc<br>
 * @author meslin
 *
 */
@SuppressWarnings("serial")
public class GeographicMap extends JFrame implements JMapViewerEventListener
{
	private final JMapViewerTree treeMap;
	private JLabel metersPerPixelLabel;
	private JLabel metersPerPixelValue;
	private JLabel zoomLabel;
	private JLabel zoomValue;
	
	private List<MapMarkerDot> mapMarkerDotList;

	/**
	 * Constructs {@code Demo}.
	 * @param regionList 
	 */
	public GeographicMap() {
		super("Map");
		mapMarkerDotList = new ArrayList<MapMarkerDot>();
		
        setSize(400, 400);
		
		treeMap = new JMapViewerTree("Human Mobility");
		
		map().addJMVListener(this);
		
		setLayout(new BorderLayout());
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		JPanel panel = new JPanel(new BorderLayout());
		JPanel panelTop = new JPanel();
		JPanel panelBottom = new JPanel();
		JPanel helpPanel = new JPanel();
		
		metersPerPixelLabel = new JLabel("Meters/Pixels:");
		metersPerPixelValue = new JLabel(String.format("%s", map().getMeterPerPixel()));
		
        zoomLabel = new JLabel("Zoom: ");
        zoomValue = new JLabel(String.format("%s", map().getZoom()));
		
        add(panel, BorderLayout.NORTH);
        add(helpPanel, BorderLayout.SOUTH);
        panel.add(panelTop, BorderLayout.NORTH);
        panel.add(panelBottom, BorderLayout.SOUTH);

        JLabel helpLabel = new JLabel("Use the mouse right button to move, double click or mouse wheel to zoom. © OpenStreetMap contributors");
        helpPanel.add(helpLabel);

        JButton marksBuyton = new JButton("Marks");
        marksBuyton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map().setDisplayToFitMapMarkers();
            }
        });
        JComboBox<TileSource> tileSourceSelector = new JComboBox<>(new TileSource[] {
                new OsmTileSource.Mapnik(),
                new OsmTileSource.CycleMap(),
                new BingAerialTileSource()
        });
        tileSourceSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                map().setTileSource((TileSource) e.getItem());
            }
        });

        JComboBox<TileLoader> tileLoaderSelector;
        tileLoaderSelector = new JComboBox<>(new TileLoader[] {
        		new OsmTileLoader(map())
        });
        tileLoaderSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                map().setTileLoader((TileLoader) e.getItem());
            }
        });
        map().setTileLoader((TileLoader) tileLoaderSelector.getSelectedItem());
        panelTop.add(tileSourceSelector);
        panelTop.add(tileLoaderSelector);

        final JCheckBox showTileGrid = new JCheckBox("Grid visible");
        showTileGrid.setSelected(map().isTileGridVisible());
        showTileGrid.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map().setTileGridVisible(showTileGrid.isSelected());
            }
        });
        panelBottom.add(showTileGrid);
        final JCheckBox showZoomControls = new JCheckBox("Zoom control visible");
        showZoomControls.setSelected(map().getZoomControlsVisible());
        showZoomControls.addActionListener(new ActionListener() {
            @SuppressWarnings("deprecation")
			@Override
            public void actionPerformed(ActionEvent e) {
                map().setZoomContolsVisible(showZoomControls.isSelected());
            }
        });
        panelBottom.add(showZoomControls);
        final JCheckBox scrollWrapEnabled = new JCheckBox("Scrollwrap enabled");
        scrollWrapEnabled.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                map().setScrollWrapEnabled(scrollWrapEnabled.isSelected());
            }
        });
        panelBottom.add(scrollWrapEnabled);
        panelBottom.add(marksBuyton);

        panelTop.add(zoomLabel);
        panelTop.add(zoomValue);
        panelTop.add(metersPerPixelLabel);
        panelTop.add(metersPerPixelValue);

        add(treeMap, BorderLayout.CENTER);
        
        map().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    map().getAttribution().handleAttribution(e.getPoint(), true);
                }
            }
        });

        map().addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                boolean cursorHand = map().getAttribution().handleAttributionCursor(p);
                if (cursorHand) {
                    map().setCursor(new Cursor(Cursor.HAND_CURSOR));
                } else {
                    map().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            }
        });        
	}

    @Override
	public void processCommand(JMVCommandEvent command)
	{
        if (command.getCommand().equals(JMVCommandEvent.COMMAND.ZOOM) ||
                command.getCommand().equals(JMVCommandEvent.COMMAND.MOVE)) {
            updateZoomParameters();
        }
	}

    private JMapViewer map() {
        return treeMap.getViewer();
    }
    
    private void updateZoomParameters() {
        if (metersPerPixelValue != null)
            metersPerPixelValue.setText(String.format("%s", map().getMeterPerPixel()));
        if (zoomValue != null)
            zoomValue.setText(String.format("%s", map().getZoom()));
    }
    
    /**
     * Adds a person to the map using geographic coordinates and its label
     * @param label
     * @param coordinate
     */
    public void addPerson(String label, Coordinate coordinate)
    {
    	MapMarkerDot avatar = new MapMarkerDot(coordinate);
    	avatar.setName(label);
    	map().addMapMarker(avatar);
    	mapMarkerDotList.add(avatar);
    }
	
	/**
	 * Removes a person form the map<br>
	 * @param person label
	 */
	public void remove(String label) {
		for(Iterator<MapMarkerDot> iterator = mapMarkerDotList.iterator(); iterator.hasNext();) {
			MapMarkerDot mapMarkerDot = iterator.next();
			if(mapMarkerDot.getName().equals(label)) {
				map().removeMapMarker(mapMarkerDot);
				iterator.remove();
			}
		}
	}

	/* (non-Javadoc)
	 * @see java.awt.Container#removeAll()
	 */
	public void removeAll() {
		for(Iterator<MapMarkerDot> iterator = mapMarkerDotList.iterator(); iterator.hasNext();) {
			map().removeMapMarker(iterator.next());
			iterator.remove();
		}
	}
}
