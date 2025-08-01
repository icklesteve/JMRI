package jmri.jmrit.operations.locations.schedules.tools;

import java.awt.Dimension;
import java.awt.GridBagLayout;

import javax.swing.*;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.schedules.ScheduleEditFrame;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.swing.JmriJOptionPane;

/**
 * Frame used to edit alternate track selection and percentage of loads from
 * staging.
 *
 * @author Daniel Boudreau Copyright (C) 2010, 2011, 2015
 */
class ScheduleOptionsFrame extends OperationsFrame implements java.beans.PropertyChangeListener {

    // text field
    JTextField factorTextField = new JTextField(5);

    // combo boxes
    JComboBox<Track> trackBox = new JComboBox<>();

    // radio buttons
    // major buttons
    JButton saveButton = new JButton(Bundle.getMessage("ButtonSave"));

    Track _track;

    public ScheduleOptionsFrame(ScheduleEditFrame sef) {
        super(Bundle.getMessage("MenuItemScheduleOptions"));

        // the following code sets the frame's initial state
        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        _track = sef._track;

        // load the panel
        // row 1
        JPanel pFactor = new JPanel();
        pFactor.setLayout(new GridBagLayout());
        pFactor.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("ScheduleFactor")));
        addItem(pFactor, factorTextField, 0, 0);

        factorTextField.setToolTipText(Bundle.getMessage("TipScheduleFactor"));
        factorTextField.setText(Integer.toString(_track.getReservationFactor()));

        // row 2
        JPanel pAlternate = new JPanel();
        pAlternate.setLayout(new GridBagLayout());
        pAlternate.setBorder(BorderFactory.createTitledBorder(Bundle.getMessage("AlternateTrack")));
        addItem(pAlternate, trackBox, 0, 0);

        updateTrackCombobox();

        JPanel pControls = new JPanel();
        pControls.add(saveButton);

        // button action
        addButtonAction(saveButton);

        getContentPane().add(pFactor);
        getContentPane().add(pAlternate);
        getContentPane().add(pControls);
        
        _track.addPropertyChangeListener(this);
        _track.getLocation().addPropertyChangeListener(this);

        addHelpMenu("package.jmri.jmrit.operations.Operations_ControllingCarsToSpur", true); // NOI18N

        initMinimumSize(new Dimension(Control.panelWidth400, Control.panelHeight200));
        
    }

    @Override
    public void buttonActionPerformed(java.awt.event.ActionEvent ae) {
        if (ae.getSource() == saveButton) {
            // confirm that factor is between 0 and 1000
            try {
                int factor = Integer.parseInt(factorTextField.getText());
                if (factor < 0 || factor > 1000) {
                    JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("FactorMustBeNumber"),
                            Bundle.getMessage("ErrorFactor"), JmriJOptionPane.ERROR_MESSAGE);
                    return;
                }
            } catch (NumberFormatException e) {
                JmriJOptionPane.showMessageDialog(this, Bundle.getMessage("FactorMustBeNumber"),
                        Bundle.getMessage("ErrorFactor"), JmriJOptionPane.ERROR_MESSAGE);
                return;
            }
            _track.setReservationFactor(Integer.parseInt(factorTextField.getText()));
            if (trackBox.getSelectedItem() != null && !trackBox.getSelectedItem().equals(Location.NONE)) {
                _track.setAlternateTrack((Track) trackBox.getSelectedItem());
            } else {
                _track.setAlternateTrack(null);
            }
            OperationsXml.save();
            if (Setup.isCloseWindowOnSaveEnabled()) {
                dispose();
            }
        }
    }

    private void updateTrackCombobox() {
        _track.getLocation().updateComboBox(trackBox);
        trackBox.removeItem(_track); // remove this track from consideration
        trackBox.setSelectedItem(_track.getAlternateTrack());
    }

    @Override
    public void dispose() {
        _track.removePropertyChangeListener(this);
        _track.getLocation().removePropertyChangeListener(this);
        super.dispose();
    }

    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        if (Control.SHOW_PROPERTY) {
            log.debug("Property change: ({}) old: ({}) new: ({})", e.getPropertyName(), e.getOldValue(), e
                    .getNewValue());
        }
        if (e.getPropertyName().equals(Track.ALTERNATE_TRACK_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Location.TRACK_LISTLENGTH_CHANGED_PROPERTY)) {
            updateTrackCombobox();
        }
        if (e.getPropertyName().equals(Track.TRACK_FACTOR_CHANGED_PROPERTY)) {
            factorTextField.setText(Integer.toString(_track.getReservationFactor()));
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScheduleOptionsFrame.class);

}
