package jmri.jmrix.ieee802154;

import jmri.jmrix.AbstractMRListener;
import jmri.jmrix.AbstractMRMessage;
import jmri.jmrix.AbstractMRReply;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * IEEE802154TrafficControllerTest.java
 *
 * Test for the jmri.jmrix.ieee802154.IEEE802154TrafficController
 * class
 *
 * @author Paul Bender
 */
public class IEEE802154TrafficControllerTest extends jmri.jmrix.AbstractMRNodeTrafficControllerTest {

    @Test
    public void testGetIEEE802154Messge() {
        Assert.assertNull("IEEE802154Message", ((IEEE802154TrafficController)tc).getIEEE802154Message(5));
    }

    @Test
    public void testGetPollReplyHandler() {
        Assert.assertNull("pollReplyHandler", ((IEEE802154TrafficController)tc).pollReplyHandler());
    }

    @Test
    public void checkPollMessageNoNodes() {
        // no nodes, should return null.
        Assert.assertNull("pollMessage", ((IEEE802154TrafficController)tc).pollMessage());
    }

    @Test
    public void checkPollReplyHandler() {
        // always returns null.
        Assert.assertNull("pollReplyHandler", ((IEEE802154TrafficController)tc).pollReplyHandler());
    }

    @Test
    public void checkEnterProgMode() {
        // No Programming Mode, returns null.
        Assert.assertNull("enterProgMode", ((IEEE802154TrafficController)tc).enterProgMode());
    }

    @Test
    public void checkExitProgMode() {
        // No Programming Mode, returns null.
        Assert.assertNull("enterNormalMode", ((IEEE802154TrafficController)tc).enterNormalMode());
    }

    @Test
    public void testGetNodeFromAddressTest() {
        // test the code to get an IEEE 802.15.4 node from its address
        // specified as a string to make sure it returns null on failure.
        IEEE802154Node node = ((IEEE802154TrafficController)tc).newNode();
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        ((IEEE802154TrafficController)tc).registerNode(node);
        IEEE802154Node n = (IEEE802154Node) ((IEEE802154TrafficController)tc).getNodeFromAddress("00 01");
        Assert.assertNull("node found", n);
    }

    @Test
    public void testGetNodeFromUserAddressIntTest() {
        // test the code to get an IEEE 802.15.4 node from its User address
        // specified as an integer array.
        IEEE802154Node node = ((IEEE802154TrafficController)tc).newNode();
        ((IEEE802154TrafficController)tc).registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        int iad[] = {0x6D, 0x97};
        node.setUserAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        IEEE802154Node n = (IEEE802154Node) ((IEEE802154TrafficController)tc).getNodeFromAddress(iad);
        Assert.assertNotNull("node not found", n);
    }

    @Test
    public void testGetNodeFromUserAddressByteTest() {
        // test the code to get an IEEE802.15.4 node from its User address
        // specified as a byte array.
        IEEE802154Node node = ((IEEE802154TrafficController)tc).newNode();
        ((IEEE802154TrafficController)tc).registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        IEEE802154Node n = (IEEE802154Node) ((IEEE802154TrafficController)tc).getNodeFromAddress(uad);
        Assert.assertNotNull("node not found", n);
    }

   @Test
    public void testGetNodeFromUserAddressTest() {
        // test the code to get an IEEE802154 node from its User address
        // specified as a string.
        IEEE802154Node node = ((IEEE802154TrafficController)tc).newNode();
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        ((IEEE802154TrafficController)tc).registerNode(node);
        IEEE802154Node n = (IEEE802154Node) ((IEEE802154TrafficController)tc).getNodeFromAddress("6D 97");
        Assert.assertNotNull("node not found", n);
    }

    @Test
    public void testGetNodeFromAddressGlobalByteTest() {
        // test the code to get an IEEE802154 node from its Global address
        // specified as a byte array.
        IEEE802154Node node = ((IEEE802154TrafficController)tc).newNode();
        ((IEEE802154TrafficController)tc).registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        node.setGlobalAddress(gad);
        ((IEEE802154TrafficController)tc).registerNode(node);
        IEEE802154Node n = (IEEE802154Node) ((IEEE802154TrafficController)tc).getNodeFromAddress(gad);
        Assert.assertNotNull("node not found", n);
    }

   @Test
    public void testGetNodeFromAddressGlobalIntTest() {
        // test the code to get an IEEE802154 node from its Global address
        // specified as an intger array.
        IEEE802154Node node = ((IEEE802154TrafficController)tc).newNode();
        ((IEEE802154TrafficController)tc).registerNode(node);
        node.setNodeAddress(28055);
        byte uad[] = {(byte) 0x6D, (byte) 0x97};
        node.setUserAddress(uad);
        node.setPANAddress(uad);
        byte gad[] = {(byte) 0x00, (byte) 0x13, (byte) 0xA2, (byte) 0x00, (byte) 0x40, (byte) 0xA0, (byte) 0x4D, (byte) 0x2D};
        int iad[] = {0x00, 0x13, 0xA2, 0x00, 0x40, 0xA0, 0x4D, 0x2D};
        node.setGlobalAddress(gad);
        ((IEEE802154TrafficController)tc).registerNode(node);
        IEEE802154Node n = (IEEE802154Node) ((IEEE802154TrafficController)tc).getNodeFromAddress(iad);
        Assert.assertNotNull("node not found", n);
    }

    @BeforeEach
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new IEEE802154TrafficController() {
            @Override
            protected AbstractMRReply newReply() {
                return null;
            }
            @Override
            public IEEE802154Node newNode() {
                return new IEEE802154Node(){
                     @Override
                     public AbstractMRMessage createInitPacket(){
                         return null;
                     }
                     @Override
                     public AbstractMRMessage createOutPacket(){
                         return null;
                     }
                     @Override
                     public boolean getSensorsActive(){
                         return false;
                     }
                     @Override
                     public boolean handleTimeout(AbstractMRMessage m,AbstractMRListener l){
                         return false;
                     }
                     @Override
                     public void resetTimeout(AbstractMRMessage m){
                     }
                };
            }
        };
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
