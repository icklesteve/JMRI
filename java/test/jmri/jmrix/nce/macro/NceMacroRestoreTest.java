package jmri.jmrix.nce.macro;

import jmri.jmrix.nce.NceTrafficControlScaffold;
import jmri.jmrix.nce.usbdriver.UsbCmdStationMemory;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Ken Cameron Copyright (C) 2023
 */
public class NceMacroRestoreTest {

    private NceTrafficControlScaffold tcis = null;

    @Test
    public void testCTor() {
        tcis = new NceTrafficControlScaffold();
        tcis.csm = new UsbCmdStationMemory();
        NceMacroRestore t = new NceMacroRestore(tcis);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new NceTrafficControlScaffold();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(NceMacroRestoreTest.class);

}
