package jmri.jmrit.operations.locations.gui;

import java.beans.PropertyChangeEvent;

import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.setup.Control;

/**
 * Table Model for edit of yards used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 */
public class YardTableModel extends TrackTableModel {

    public YardTableModel() {
        super();
    }

    public void initTable(JTable table, Location location) {
        super.initTable(table, location, Track.YARD);
    }

    @Override
    protected void editTrack(int row) {
        log.debug("Edit yard");
        if (tef != null) {
            tef.dispose();
        }
        // use invokeLater so new window appears on top
        SwingUtilities.invokeLater(() -> {
            tef = new YardEditFrame();
            Track yard = _tracksList.get(row);
            tef.initComponents(yard);
        });
    }
    
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case NAME_COLUMN:
                return Bundle.getMessage("YardName");
            default:
                return super.getColumnName(col);
        }
    }

    // this table listens for changes to a location and its yards
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        super.propertyChange(e);
        if (e.getSource().getClass().equals(Track.class)) {
            Track track = ((Track) e.getSource());
            if (track.isYard()) {
                int row = _tracksList.indexOf(track);
                if (Control.SHOW_PROPERTY) {
                    log.debug("Update yard table row: {} track: ({})", row, track.getName());
                }
                if (row >= 0) {
                    fireTableRowsUpdated(row, row);
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(YardTableModel.class);
}
