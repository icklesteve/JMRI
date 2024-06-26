package jmri.jmrit.turnoutoperations;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TurnoutOperationConfigTest {

    @Test
    public void testCTor() {
        jmri.TurnoutOperation to = new jmri.SensorTurnoutOperation();
        TurnoutOperationConfig t = new TurnoutOperationConfig(to);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testRawOperator() {
        jmri.TurnoutOperation to = new jmri.RawTurnoutOperation();
        TurnoutOperationConfig t = new TurnoutOperationConfig(to);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TurnoutOperationConfigTest.class);

}
