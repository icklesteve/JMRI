package jmri.jmrix.jmriclient;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.util.JUnitUtil;

/**
 * Tests for the jmri.jmrix.jmriclient.JMRIClientTurnout class
 *
 * @author Bob Jacobsen
 * @author  Paul Bender Copyright (C) 2017
 */
public class JMRIClientTurnoutTest extends jmri.implementation.AbstractTurnoutTestBase  {

    @Override
    public int numListeners() {
        return jcins.numListeners();
    }

    protected JMRIClientTrafficControlScaffold jcins;

    @Override
    public void checkClosedMsgSent() {
        Assert.assertEquals("closed message", "TURNOUT "+ t.getSystemName()+ " CLOSED\n",
                jcins.outbound.elementAt(jcins.outbound.size() - 1).toString());
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertEquals("thrown message", "TURNOUT "+ t.getSystemName() + " THROWN\n",
                jcins.outbound.elementAt(jcins.outbound.size() - 1).toString());
    }

    @Override
    @Test
    public void testDispose() {
        t.setCommandedState(jmri.Turnout.CLOSED);    // in case registration with TrafficController

        // is deferred to after first use
        t.dispose();
        Assert.assertEquals("controller listeners remaining", 1, numListeners());
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        
        jcins = new JMRIClientTrafficControlScaffold();
        t = new JMRIClientTurnout(3, new JMRIClientSystemConnectionMemo(jcins));
    }

    @Override
    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

        jcins = null;
    }

}
