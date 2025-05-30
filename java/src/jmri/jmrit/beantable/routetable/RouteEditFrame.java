package jmri.jmrit.beantable.routetable;

import jmri.InstanceManager;
import jmri.Route;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

/**
 * Edit frame for the Route Table.
 *
 * Split from {@link jmri.jmrit.beantable.RouteTableAction}
 *
 * @author Dave Duchamp Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2007
 * @author Simon Reader Copyright (C) 2008
 * @author Pete Cressman Copyright (C) 2009
 * @author Egbert Broerse Copyright (C) 2016
 * @author Paul Bender Copyright (C) 2020
 */
public class RouteEditFrame extends AbstractRouteAddEditFrame {

    private final String systemName;

    public RouteEditFrame() {
        this(Bundle.getMessage("TitleEditRoute"));
    }

    public RouteEditFrame(String systemName) {
        this(Bundle.getMessage("TitleEditRoute"), systemName);
    }

    public RouteEditFrame(String name, String systemName) {
        this(name,false,true, systemName);
    }

    public RouteEditFrame(String name, boolean saveSize, boolean savePosition, String systemName) {
        super(name, saveSize, savePosition);
        this.systemName = systemName;
        initComponents();
    }

    @Override
    public final void initComponents() {
        super.initComponents();
        _systemName.setText(systemName);
        // identify the Route with this name if it already exists
        String sName = _systemName.getText();
        Route g = InstanceManager.getDefault(jmri.RouteManager.class).getBySystemName(sName);
        if (g == null) {
            sName = _userName.getText();
            g = InstanceManager.getDefault(jmri.RouteManager.class).getByUserName(sName);
            if (g == null) {
                // Route does not exist, so cannot be edited
                status1.setText(Bundle.getMessage("RouteAddStatusErrorNotFound"));
                return;
            }
        }
        // Route was found, make its system name not changeable
        curRoute = g;
        _systemName.setVisible(true);
        _systemName.setText(sName);
        _systemName.setEnabled(false);
        nameLabel.setEnabled(true);
        _autoSystemName.setVisible(false);
        // deactivate this Route
        curRoute.deActivateRoute();
        // get information for this route
        _userName.setText(g.getUserName());

        setPageContent(g);

        // begin with showing all Turnouts
        // set up buttons and notes
        status1.setText(Bundle.getMessage("RouteAddStatusInitial3", Bundle.getMessage("ButtonUpdate")));
        status2.setText(Bundle.getMessage("RouteAddStatusInitial4", Bundle.getMessage("ButtonCancelEdit", Bundle.getMessage("ButtonEdit"))));
        status2.setVisible(true);
        setTitle(Bundle.getMessage("TitleEditRoute"));
        editMode = true;
    }

    @Override
    protected JPanel getButtonPanel() {
        final JButton cancelEditButton = new JButton( // I18N for word sequence "Cancel Edit"
            Bundle.getMessage("ButtonCancelEdit", Bundle.getMessage("ButtonEdit")));
        final JButton deleteButton = new JButton(Bundle.getMessage("ButtonDelete") + " "
            + Bundle.getMessage("BeanNameRoute")); // I18N "Delete Route"
        final JButton updateButton = new JButton(Bundle.getMessage("ButtonUpdate"));
        final JButton exportButton = new JButton(Bundle.getMessage("ButtonExport"));
        // add Buttons panel
        JPanel pb = new JPanel();
        pb.setLayout(new FlowLayout(FlowLayout.TRAILING));
        // CancelEdit button
        pb.add(cancelEditButton);
        cancelEditButton.addActionListener( e -> cancelEdit());
        cancelEditButton.setToolTipText(Bundle.getMessage("TooltipCancelRoute"));
        // Delete Route button
        pb.add(deleteButton);
        deleteButton.addActionListener( e -> deletePressed());
        deleteButton.setToolTipText(Bundle.getMessage("TooltipDeleteRoute"));
        // Update Route button
        pb.add(updateButton);
        updateButton.addActionListener((ActionEvent e1) -> updatePressed(false));
        updateButton.setToolTipText(Bundle.getMessage("TooltipUpdateRoute"));
        // Export button
        pb.add(exportButton);
        exportButton.addActionListener( e -> exportButtonPressed());
        exportButton.setToolTipText(Bundle.getMessage("TooltipExportRoute"));

        // Show the initial buttons, and hide the others
        deleteButton.setVisible(true);
        cancelEditButton.setVisible(true);
        updateButton.setVisible(true);
        exportButton.setVisible(true);
        return pb;
    }

    /**
     * Respond to the export button.
     */
    private void exportButtonPressed(){
        new RouteExportToLogix(_systemName.getText()).export();
        status1.setText(Bundle.getMessage("BeanNameRoute")
                + "\"" + _systemName.getText() + "\" " +
                Bundle.getMessage("RouteAddStatusExported") + " ("
                + get_includedTurnoutList().size() +
                Bundle.getMessage("Turnouts") + ", " +
                get_includedSensorList().size() + " " + Bundle.getMessage("Sensors") + ")");
        finishUpdate();
        closeFrame();
    }

    /**
     * Respond to the Delete button.
     */
    private void deletePressed() {
        // route is already deactivated, just delete it
        routeManager.deleteRoute(curRoute);

        curRoute = null;
        finishUpdate();
        closeFrame();
    }

}
