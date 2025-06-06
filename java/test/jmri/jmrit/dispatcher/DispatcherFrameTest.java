package jmri.jmrit.dispatcher;

import java.awt.GraphicsEnvironment;

import jmri.InstanceManager;
import jmri.jmrit.dispatcher.DispatcherFrame.TrainsFrom;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Swing tests for dispatcher options
 *
 * @author Dave Duchamp
 * @author  Paul Bender Copyright(C) 2017
 */
public class DispatcherFrameTest {

    @Test
    public void testShowAndClose() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);

        // Find new table window by name
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));
        // Ask to close Dispatcher window
        dw.requestClose();
        // we still have a reference to the window, so make sure that clears
        JUnitUtil.dispose(d);
        InstanceManager.getDefault(jmri.SignalMastManager.class).dispose();
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).dispose();

    }

    @Test
    public void testParametersRead() {
        // The Dispatcher functionality is tightly coupled to the Dispatcher
        // Frame.  As a result, we can currently only test seting the
        // options file by creating a DispatcherFrame object.  A future
        // enhancement shold probably break this coupling.
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);

        // set all options
        d.setLayoutEditor(null);
        d.setUseConnectivity(false);
        d.setTrainsFrom(TrainsFrom.TRAINSFROMROSTER);
        d.setAutoAllocate(false);
        d.setAutoTurnouts(false);
        d.setHasOccupancyDetection(false);
        d.setUseScaleMeters(false);
        d.setShortActiveTrainNames(false);
        d.setShortNameInBlock(true);
        d.setExtraColorForAllocated(false);
        d.setNameInAllocatedBlock(false);
        d.setScale(jmri.ScaleManager.getScale("HO"));
        // test all options
        Assert.assertNull("LayoutEditor", d.getLayoutEditor());
        Assert.assertFalse("UseConnectivity", d.getUseConnectivity());
        Assert.assertEquals(TrainsFrom.TRAINSFROMROSTER, d.getTrainsFrom());
        Assert.assertFalse("AutoAllocate", d.getAutoAllocate());
        Assert.assertFalse("AutoTurnouts", d.getAutoTurnouts());
        Assert.assertFalse("HasOccupancyDetection", d.getHasOccupancyDetection());
        Assert.assertFalse("UseScaleMeters", d.getUseScaleMeters());
        Assert.assertFalse("ShortActiveTrainNames", d.getShortActiveTrainNames());
        Assert.assertTrue("ShortNameInBlock", d.getShortNameInBlock());
        Assert.assertFalse("ExtraColorForAllocated", d.getExtraColorForAllocated());
        Assert.assertFalse("NameInAllocatedBlock", d.getNameInAllocatedBlock());
        Assert.assertEquals("Scale", jmri.ScaleManager.getScale("HO"), d.getScale());
        // check changing some options
        d.setAutoTurnouts(true);
        Assert.assertTrue("New AutoTurnouts", d.getAutoTurnouts());
        d.setHasOccupancyDetection(true);
        Assert.assertTrue("New HasOccupancyDetection", d.getHasOccupancyDetection());
        d.setShortNameInBlock(false);
        Assert.assertFalse("New ShortNameInBlock", d.getShortNameInBlock());
        d.setScale(jmri.ScaleManager.getScale("N"));
        Assert.assertEquals("New Scale", jmri.ScaleManager.getScale("N"), d.getScale());

        // Find the window by name and close it.
        (new org.netbeans.jemmy.operators.JFrameOperator(Bundle.getMessage("TitleDispatcher"))).requestClose();
        JUnitUtil.dispose(d);
    }

    @Test
    public void testAddTrainButton() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);

        // Find new table window by name
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));

        // find the add train Button
        JButtonOperator bo = new JButtonOperator(dw,Bundle.getMessage("InitiateTrain") + "...");

        bo.push();

        // pushing the button should bring up the Add Train frame
        JFrameOperator atf = new JFrameOperator(Bundle.getMessage("AddTrainTitle"));
        // now close the add train frame.
        atf.requestClose();

        // Ask to close Dispatcher window
        dw.requestClose();
        // we still have a reference to the window, so make sure that clears
        JUnitUtil.dispose(d);
    }

    @Test
    public void testAllocateExtraSectionButton() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);

        // Find new table window by name
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));

        // find the Allocate Extra SectionsButton
        JButtonOperator bo = new JButtonOperator(dw,Bundle.getMessage("AllocateExtra") + "...");

        bo.push();

        // pushing the button should bring up the Extra Sections frame
        JFrameOperator atf = new JFrameOperator(Bundle.getMessage("ExtraTitle"));
        // now close the add train frame.
        atf.requestClose();

        // Ask to close Dispatcher window
        dw.requestClose();
        // we still have a reference to the window, so make sure that clears
        JUnitUtil.dispose(d);
    }

    @Test
    public void testCancelRestartButton() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        DispatcherFrame d = InstanceManager.getDefault(DispatcherFrame.class);

        // Find new table window by name
        JFrameOperator dw = new JFrameOperator(Bundle.getMessage("TitleDispatcher"));

        // find the Cancel Restart Button
        JButtonOperator bo = new JButtonOperator(dw,Bundle.getMessage("CancelRestart") + "...");

        bo.push();

        // we don't have an active train, so this shouldn't result in any
        // new windows or other results.  This part of the test just verifies
        // we don't have any exceptions.

        // Ask to close Dispatcher window
        dw.requestClose();
        // we still have a reference to the window, so make sure that clears
        JUnitUtil.dispose(d);
    }


    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initRosterConfigManager();
        JUnitUtil.initDebugThrottleManager();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }
}
