package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.locations.gui.YardEditFrame;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ChangeTrackFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);

        Track track = loc.getTrackByName("NI Yard", null);

        YardEditFrame tf = new YardEditFrame();
        tf.initComponents(loc, track);
        Assert.assertNotNull("exists", tf);

        ChangeTrackFrame t = new ChangeTrackFrame(tf);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(tf);

    }

    @Test
    public void testFrameButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);

        Track track = loc.getTrackByName("NI Yard", null);

        YardEditFrame tef = new YardEditFrame();
        tef.initComponents(loc, track);
        Assert.assertNotNull("exists", tef);
        
        ChangeTrackFrame ctf = new ChangeTrackFrame(tef);
        Assert.assertNotNull("exists", ctf);
        
        // confirm default
        Assert.assertTrue(track.isYard());
        
        JemmyUtil.enterClickAndLeave(ctf.spurRadioButton);
        JemmyUtil.enterClickAndLeave(ctf.saveButton);
        
        // confirm change
        Assert.assertTrue(track.isSpur());
        
        JUnitUtil.dispose(ctf);
        JUnitUtil.dispose(tef);

    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);

        Track track = loc.getTrackByName("NI Yard", null);

        YardEditFrame tef = new YardEditFrame();
        tef.initComponents(loc, track);
        Assert.assertNotNull("exists", tef);
        
        ChangeTrackFrame f = new ChangeTrackFrame(tef);
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }


    // private final static Logger log = LoggerFactory.getLogger(ChangeTrackFrameTest.class);
}
