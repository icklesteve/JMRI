package jmri.jmrit.operations.locations.gui;

import java.awt.Color;
import java.beans.PropertyChangeEvent;

import javax.swing.JTable;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.locations.schedules.Schedule;
import jmri.jmrit.operations.setup.Control;

/**
 * Table Model for edit of spurs used by operations
 *
 * @author Daniel Boudreau Copyright (C) 2008
 */
public class SpurTableModel extends TrackTableModel {

    public SpurTableModel() {
        super();
    }

    public void initTable(JTable table, Location location) {
        super.initTable(table);
        super.initTable(table, location, Track.SPUR);
    }

    @Override
    protected void editTrack(int row) {
        log.debug("Edit spur");
        if (tef != null) {
            tef.dispose();
        }
        // use invokeLater so new window appears on top
        SwingUtilities.invokeLater(() -> {
            tef = new SpurEditFrame();
            Track spur = _tracksList.get(row);
            tef.initComponents(spur);
        });
    }
    
    @Override
    public String getColumnName(int col) {
        switch (col) {
            case NAME_COLUMN:
                return Bundle.getMessage("SpurName");
            default:
                return super.getColumnName(col);
        }
    }

    @Override
    protected Color getForegroundColor(int row) {
        Track spur = _tracksList.get(row);
        if (!spur.checkScheduleValid().equals(Schedule.SCHEDULE_OKAY)) {
            return Color.red;
        }
        return super.getForegroundColor(row);
    }

    // this table listens for changes to a location and it's spurs
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        super.propertyChange(e);
        if (e.getSource().getClass().equals(Track.class)) {
            Track track = ((Track) e.getSource());
            if (track.isSpur()) {
                int row = _tracksList.indexOf(track);
                if (Control.SHOW_PROPERTY) {
                    log.debug("Update spur table row: {} track: {}", row, track.getName());
                }
                if (row >= 0) {
                    fireTableRowsUpdated(row, row);
                }
            }
        }
    }

    private final static Logger log = LoggerFactory.getLogger(SpurTableModel.class);
}
