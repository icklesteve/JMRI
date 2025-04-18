package jmri.jmrit.operations.trains.tools;

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import jmri.jmrit.operations.trains.gui.TrainsTableFrame;

/**
 * Swing action to create and register a TrainsScriptFrame.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Daniel Boudreau Copyright (C) 2011
 */
public class TrainsScriptAction extends AbstractAction {

    public TrainsScriptAction(TrainsTableFrame frame) {
        super(Bundle.getMessage("MenuItemScripts"));
        _frame = frame;
    }

    TrainsTableFrame _frame; // the parent frame that is launching the TrainScriptFrame.

    TrainsScriptFrame f = null;

    @Override
    public void actionPerformed(ActionEvent e) {
        // create a train scripts frame
        if (f != null && f.isVisible()) {
            f.dispose();
        }
        f = new TrainsScriptFrame();
        f.setLocation(_frame.getLocation());
        f.initComponents();
        f.setExtendedState(Frame.NORMAL);
        f.setTitle(Bundle.getMessage("MenuItemScripts"));
    }
}


