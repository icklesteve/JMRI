package jmri.jmrit.operations.locations.gui;

import java.beans.PropertyChangeEvent;

import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.setup.Control;

/**
 * Table Model for edit of staging tracks used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 */
public class StagingTableModel extends TrackTableModel {

    public StagingTableModel() {
        super();
    }

    public void initTable(JTable table, Location location) {
        super.initTable(table, location, Track.STAGING);
    }

    @Override
    protected void editTrack(int row) {
        log.debug("Edit staging");
        if (tef != null) {
            tef.dispose();
        }
        // use invokeLater so new window appears on top
        SwingUtilities.invokeLater(() -> {
            tef = new StagingEditFrame();
            Track staging = _tracksList.get(row);
            tef.initComponents(staging);
        });
    }
    
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case NAME_COLUMN:
                return Bundle.getMessage("StagingName");
            default:
                return super.getColumnName(col);
        }
    }

    // this table listens for changes to a location and it's staging tracks
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        super.propertyChange(e);
        if (e.getSource().getClass().equals(Track.class)) {
            Track track = ((Track) e.getSource());
            if (track.isStaging()) {
                int row = _tracksList.indexOf(track);
                if (Control.SHOW_PROPERTY) {
                    log.debug("Update staging table row: {} track: {}", row, track.getName());
                }
                if (row >= 0) {
                    fireTableRowsUpdated(row, row);
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(StagingTableModel.class);
}
