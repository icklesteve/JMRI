package jmri.jmrix.can.cbus.swing.console;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of CbusConsolePacketPane
 *
 * @author Paul Bender Copyright (C) 2016
 * @author Steve Young Copyright (C) 2020
*/
@jmri.util.junit.annotations.DisabledIfHeadless
public class CbusConsolePacketPaneTest  {

    @Test
    public void testCbusConsolePacketPaneCtor() {

        CbusConsolePacketPane t = new CbusConsolePacketPane(mainConsolePane);
        Assertions.assertNotNull(t);
        
    }
    
    private CanSystemConnectionMemo memo = null;
    private TrafficControllerScaffold tc = null;
    private CbusConsolePane mainConsolePane = null;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new CanSystemConnectionMemo();
        tc = new TrafficControllerScaffold();
        memo.setTrafficController(tc);
        mainConsolePane = new CbusConsolePane();
        mainConsolePane.initComponents(memo,false);
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(mainConsolePane);
        mainConsolePane.dispose();
        Assertions.assertNotNull(tc);
        tc.terminateThreads();
        Assertions.assertNotNull(memo);
        memo.dispose();
        mainConsolePane = null;
        tc = null;
        memo = null;
        
        JUnitUtil.tearDown();
    }


}
