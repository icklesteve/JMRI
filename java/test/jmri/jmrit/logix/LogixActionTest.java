package jmri.jmrit.logix;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the OPath class
 *
 * @author Pete Cressman Copyright 2014
 */
public class LogixActionTest {

    @Test
    public void testLogixAction() throws Exception {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };

        // load and display sample file. Panel file does not display screen
        java.io.File f = new java.io.File("java/test/jmri/jmrit/logix/valid/LogixActionTest.xml");
        cm.load(f);
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();

        Memory im6 = InstanceManager.memoryManagerInstance().getMemory("IM6");
        assertNotNull( im6, "Memory IM6");
        assertEquals( "EastToWestOnSiding", im6.getValue(),"Contents IM6");

        // Find Enable Logix button  <<< Use GUI, but need Container to find button in
        // JUnitUtil.pressButton(container, "Enable/Disable Tests");
        // OK, do it this way
        Sensor sensor = InstanceManager.sensorManagerInstance().getSensor("enableButton");
        assertNotNull( sensor, "Sensor IS5");
        sensor.setState(Sensor.ACTIVE);
        sensor.setState(Sensor.INACTIVE);
        sensor.setState(Sensor.ACTIVE);
        SignalHead sh1 = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH1");
        assertNotNull( sh1, "shi null");
        assertEquals( SignalHead.RED, sh1.getAppearance(), "SignalHead IH1");

        // do some buttons -Sensors
        sensor = InstanceManager.sensorManagerInstance().getSensor("ISLITERAL");
        assertNotNull( sensor, "sensor null");
        sensor.setState(Sensor.ACTIVE); // activate direct logix action
        Sensor is1 = InstanceManager.sensorManagerInstance().getSensor("sensor1");
        assertNotNull( is1, "is1 null");
        assertEquals( Sensor.ACTIVE, is1.getState(), "direct set Sensor IS1 active"); // action
        sensor = InstanceManager.sensorManagerInstance().getSensor("ISINDIRECT");
        assertNotNull( sensor, "sensor null");
        sensor.setState(Sensor.ACTIVE); // activate Indirect logix action
        assertEquals( Sensor.INACTIVE, is1.getState(), "Indirect set Sensor IS1 inactive"); // action

        // SignalHead buttons
        Sensor is4 = InstanceManager.sensorManagerInstance().getSensor("IS4");
        assertNotNull( is4, "is4 null");
        is4.setState(Sensor.ACTIVE); // activate direct logix action
        assertEquals( SignalHead.GREEN, sh1.getAppearance(), "direct set SignalHead IH1 to Green");
        is4.setState(Sensor.INACTIVE); // activate direct logix action
        assertEquals( SignalHead.RED, sh1.getAppearance(), "direct set SignalHead IH1 to Red");

        Memory im3 = InstanceManager.memoryManagerInstance().getMemory("IM3");
        assertNotNull( im3, "Memory IM3");
        assertEquals("IH1", im3.getValue(), "Contents IM3");
        Sensor is3 = InstanceManager.sensorManagerInstance().getSensor("IS3");
        assertNotNull( is3, "is3 null");
        is3.setState(Sensor.ACTIVE); // activate indirect logix action
        assertEquals( SignalHead.GREEN, sh1.getAppearance(), "Indirect set SignalHead IH1 to Green");
        is3.setState(Sensor.INACTIVE); // activate indirect logix action
        assertEquals( SignalHead.RED, sh1.getAppearance(), "Indirect set SignalHead IH1 to Red");
        // change memory value
        im3.setValue("IH2");
        is3.setState(Sensor.ACTIVE); // activate logix action
        assertEquals( "IH2", im3.getValue(), "Contents IM3");
        SignalHead sh2 = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH2");
        assertNotNull( sh2, "sh2 null");
        assertEquals( SignalHead.GREEN, sh2.getAppearance(), "Indirect SignalHead IH2");

        // Turnout Buttons
        Sensor is6 = InstanceManager.sensorManagerInstance().getSensor("IS6");
        assertNotNull( is6, "is6 null");
        is6.setState(Sensor.ACTIVE); // activate direct logix action
        Turnout it2 = InstanceManager.turnoutManagerInstance().getTurnout("IT2");
        assertNotNull( it2, "it2 null");
        assertEquals( Turnout.CLOSED, it2.getState(), "direct set Turnout IT2 to Closed");
        Memory im4 = InstanceManager.memoryManagerInstance().getMemory("IM4");
        assertNotNull( im4, "im4 null");
        assertEquals( "IT3", im4.getValue(), "Contents IM4");
        Sensor is7 = InstanceManager.sensorManagerInstance().getSensor("IS7");
        assertNotNull( is7, "is7 null");
        is7.setState(Sensor.INACTIVE); // activate indirect logix action
        Turnout it3 = InstanceManager.turnoutManagerInstance().getTurnout("IT3");
        assertNotNull( it3, "it3 null");
        assertEquals( Turnout.THROWN, it3.getState(), "Indirect set Turnout IT2 to Thrown");
        is7.setState(Sensor.ACTIVE); // activate indirect logix action
        assertEquals( Turnout.CLOSED, it3.getState(), "Indirect set Turnout IT2 to Closed");
        // change memory value
        im4.setValue("IT2");
        assertEquals( "IT2", im4.getValue(), "Contents IM4");
        is7.setState(Sensor.INACTIVE); // activate indirect logix action
        assertEquals( Turnout.THROWN, it2.getState(), "Indirect set Turnout IT2 to Thrown");
        is7.setState(Sensor.ACTIVE); // activate indirect logix action
        assertEquals( Turnout.CLOSED, it2.getState(), "Indirect set Turnout IT2 to Closed");

        // OBlock Buttons
        OBlock ob1 = InstanceManager.getDefault(OBlockManager.class).getOBlock("Left");
        assertEquals( (OBlock.OUT_OF_SERVICE | Sensor.INACTIVE), ob1.getState(), "OBlock OB1");
        OBlock ob2 = InstanceManager.getDefault(OBlockManager.class).getOBlock("Right");
        assertEquals( (OBlock.TRACK_ERROR | Sensor.INACTIVE), ob2.getState(), "OBlock OB2");
        Sensor is8 = InstanceManager.sensorManagerInstance().getSensor("IS8");
        assertNotNull( is8, "is8 null");
        is8.setState(Sensor.ACTIVE); // direct action
        assertEquals( Sensor.INACTIVE, ob1.getState(), "Direct set OBlock OB1 to normal");
        is8.setState(Sensor.INACTIVE); // direct action
        assertEquals( (OBlock.OUT_OF_SERVICE | Sensor.INACTIVE), ob1.getState(), "Direct set OBlock OB1 to OOS");
        Sensor is9 = InstanceManager.sensorManagerInstance().getSensor("IS9");
        assertNotNull( is9, "is9 null");
        is9.setState(Sensor.ACTIVE); // indirect action
        assertEquals( Sensor.INACTIVE, ob2.getState(), "Indirect set OBlock OB2 to normal");
        // change memory value
        Memory im5 = InstanceManager.memoryManagerInstance().getMemory("IM5");
        assertNotNull( im5, "im5 null");
        im5.setValue("OB1");
        is9.setState(Sensor.INACTIVE); // indirect action
        assertEquals( (OBlock.TRACK_ERROR | OBlock.OUT_OF_SERVICE | Sensor.INACTIVE),
            ob1.getState(), "Indirect set OBlock OB1 to normal");
        is9.setState(Sensor.ACTIVE); // indirect action
        is8.setState(Sensor.ACTIVE); // indirect action
        assertEquals( Sensor.INACTIVE, ob1.getState(), "Direct set OBlock OB1 to normal");

        // Warrant buttons
        Sensor is14 = InstanceManager.sensorManagerInstance().getSensor("IS14");
        assertNotNull( is14, "is14 null");
        is14.setState(Sensor.ACTIVE); // indirect action
        Warrant w = InstanceManager.getDefault(WarrantManager.class).getWarrant("EastToWestOnSiding");
        assertTrue( w.isAllocated(), "warrant EastToWestOnSiding allocated");
        Sensor is15 = InstanceManager.sensorManagerInstance().getSensor("IS15");
        assertNotNull( is15, "is15 null");
        is15.setState(Sensor.ACTIVE); // indirect action
        assertFalse( w.isAllocated(), "warrant EastToWestOnSiding deallocated");
        // change memory value
        im6.setValue("WestToEastOnMain");
        is14.setState(Sensor.INACTIVE); // toggle
        is14.setState(Sensor.ACTIVE); // indirect action
        Warrant w2 = InstanceManager.getDefault(WarrantManager.class).getWarrant("WestToEastOnMain");
        assertTrue( w2.isAllocated(), "warrant WestToEastOnMain allocated");
        im6.setValue("LeftToRightOnPath");
        is14.setState(Sensor.INACTIVE); // toggle
        is14.setState(Sensor.ACTIVE); // indirect action
        w = InstanceManager.getDefault(WarrantManager.class).getWarrant("LeftToRightOnPath");
        assertTrue( w.isAllocated(), "warrant LeftToRightOnPath allocated");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();

        JUnitUtil.initLogixManager();
        JUnitUtil.initConditionalManager();
        WarrantPreferences.getDefault().setShutdown(WarrantPreferences.Shutdown.NO_MERGE);
        JUnitUtil.initWarrantManager();
    }

    @AfterEach
    public void tearDown() {
        InstanceManager.getDefault(WarrantManager.class).dispose();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
