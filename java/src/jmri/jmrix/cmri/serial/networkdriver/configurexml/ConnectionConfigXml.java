package jmri.jmrix.cmri.serial.networkdriver.configurexml;

import java.util.List;
import java.util.StringTokenizer;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialNode;
import jmri.jmrix.cmri.serial.SerialTrafficController;
import jmri.jmrix.cmri.serial.networkdriver.ConnectionConfig;
import jmri.jmrix.cmri.serial.networkdriver.NetworkDriverAdapter;
import jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXml;
import org.jdom2.Element;

/**
 * Handle XML persistence of layout connections by persisting the
 * NetworkDriverAdapter (and connections).
 * <p>
 * Note this is named as the XML version of a ConnectionConfig object, but it's
 * actually persisting the NetworkDriverAdapter.
 * <p>
 * This class is invoked from jmrix.JmrixConfigPaneXml on write, as that class
 * is the one actually registered. Reads are brought here directly via the class
 * attribute in the XML.
 *
 * @author Bob Jacobsen Copyright: Copyright (c) 2003, 2015
 * @author kcameron Copyright (C) 2010 added multiple connections
 */
public class ConnectionConfigXml extends AbstractNetworkConnectionConfigXml {

    public ConnectionConfigXml() {
        super();
    }

    @Override
    protected void getInstance() {
        if(adapter == null) {
           adapter = new NetworkDriverAdapter();
        }
    }

    @Override
    protected void getInstance(Object object) {
        adapter = ((ConnectionConfig) object).getAdapter();
    }

    /**
     * Write out the SerialNode objects too
     *
     * @param e Element being extended
     */
    @Override
    protected void extendElement(Element e) {
         // Create a polling list from the configured nodes
        StringBuilder polllist = new StringBuilder("");
        SerialTrafficController tcPL = ((CMRISystemConnectionMemo) adapter.getSystemConnectionMemo()).getTrafficController();
        SerialNode plNode = (SerialNode) tcPL.getNode(0);
        int index = 1;
        while (plNode != null) {
            if (index != 1) {
                polllist.append(",");
            }
            polllist.append(Integer.toString(plNode.getNodeAddress()));
            plNode = (SerialNode) tcPL.getNode(index);
            index++;
        }

        Element l = new Element("polllist");
        l.setAttribute("pollseq", polllist.toString());
        e.addContent(l);

        SerialTrafficController tc = ((CMRISystemConnectionMemo)adapter.getSystemConnectionMemo()).getTrafficController();
        SerialNode node = (SerialNode) tc.getNode(0);
        index = 1;

        while (node != null) {
            // add node as an element
            Element n = new Element("node");
            n.setAttribute("name", "" + node.getNodeAddress());
            e.addContent(n);
            // add parameters to the node as needed
            n.addContent(makeParameter("nodetype", "" + node.getNodeType()));
            n.addContent(makeParameter("bitspercard", "" + node.getNumBitsPerCard()));
            n.addContent(makeParameter("transmissiondelay", "" + node.getTransmissionDelay()));
            n.addContent(makeParameter("num2lsearchlights", "" + node.getNum2LSearchLights()));
            n.addContent(makeParameter("pulsewidth", "" + node.getPulseWidth()));
            StringBuilder value = new StringBuilder("");
            for (int i = 0; i < node.getLocSearchLightBits().length; i++) {
                value.append(Integer.toHexString(node.getLocSearchLightBits()[i] & 0xF));
            }
            n.addContent(makeParameter("locsearchlightbits", value.toString()));
            value = new StringBuilder("");
            for (int i = 0; i < node.getCardTypeLocation().length; i++) {
                value.append(Integer.toHexString(node.getCardTypeLocation()[i] & 0xF));
            }
            n.addContent(makeParameter("cardtypelocation", value.toString()));
            log.debug("Node {} Card Type Written = {}", node.nodeAddress, value);

            // CMRInet Options
            value = new StringBuilder("");
            for (int i = 0; i < SerialNode.NUMCMRINETOPTS; i++) {
                value.append(Integer.toHexString((node.getCMRInetOpts(i) & 0xF)));
            }
            n.addContent(makeParameter("cmrinetoptions", value.toString().toUpperCase()));
            log.debug("Node {} NET Options Written = {}", node.nodeAddress, value);

            // cpNode Options  Classic CMRI nodes do not have options
            if (node.getNodeType() == SerialNode.CPNODE || node.getNodeType() == SerialNode.CPMEGA) {
                value = new StringBuilder();
                for (int i = 0; i < SerialNode.NUMCPNODEOPTS; i++) {
                    value.append(Integer.toHexString((node.getcpnodeOpts(i) & 0xF)));
                }
                n.addContent(makeParameter("cpnodeoptions", value.toString().toUpperCase()));
                log.debug("Node {} NODE Options Written = {}", node.nodeAddress, value);
            }

            // node description
            n.addContent(makeParameter("cmrinodedesc", node.getcmriNodeDesc()));

            // look for the next node
            node = (SerialNode) tc.getNode(index);
            index++;
        }
    }

    protected Element makeParameter(String name, String value) {
        Element p = new Element("parameter");
        p.setAttribute("name", name);
        p.addContent(value);
        return p;
    }

    @Override
    protected void unpackElement(Element shared, Element perNode) {
        // --------------------------------------
        // Load the poll list sequence if present
        // --------------------------------------
        List<Element> pl = shared.getChildren("polllist");
        if (!pl.isEmpty()) {
            Element ps = pl.get(0);
            if (ps != null) {
                String pseq = ps.getAttributeValue("pollseq");
                if (pseq != null) {
                    StringTokenizer nodes = new StringTokenizer(pseq, " ,");
                    SerialTrafficController tcPL = ((CMRISystemConnectionMemo) adapter.getSystemConnectionMemo()).getTrafficController();
                    while (nodes.hasMoreTokens()) {
                        tcPL.cmriNetPollList.add(Integer.parseInt(nodes.nextToken()));
                    }
                    log.debug("Poll List = {}", tcPL.cmriNetPollList);
                }
            }
        }

        // Load the node specific parameters
        int pollListSize = ((CMRISystemConnectionMemo) adapter.getSystemConnectionMemo()).getTrafficController().cmriNetPollList.size();
        int nextPollPos = pollListSize + 1;

        List<Element> l = shared.getChildren("node");
        for (int i = 0; i < l.size(); i++) {
            Element n = l.get(i);
            int addr = Integer.parseInt(n.getAttributeValue("name"));
            int type = Integer.parseInt(findParmValue(n, "nodetype"));
            int bpc = Integer.parseInt(findParmValue(n, "bitspercard"));
            int delay = Integer.parseInt(findParmValue(n, "transmissiondelay"));
            int num2l = Integer.parseInt(findParmValue(n, "num2lsearchlights"));
            int pulseWidth = 500;
            if ((findParmValue(n, "pulsewidth")) != null) {
                pulseWidth = Integer.parseInt(findParmValue(n, "pulsewidth"));
            }

            String slb = findParmValue(n, "locsearchlightbits");
            String ctl = findParmValue(n, "cardtypelocation");
            String opts = "";

            // create node (they register themselves)
            SerialNode node = new SerialNode(addr, type,((CMRISystemConnectionMemo)adapter.getSystemConnectionMemo()).getTrafficController());
            node.setNumBitsPerCard(bpc);
            node.setTransmissionDelay(delay);
            node.setNum2LSearchLights(num2l);
            node.setPulseWidth(pulseWidth);

            // From the loaded poll list, assign the poll list position to the node
            boolean assigned = false;
            if (pollListSize > 0) {
                for (int pls = 0; pls < pollListSize; pls++) {
                    if (((CMRISystemConnectionMemo) adapter.getSystemConnectionMemo()).getTrafficController().cmriNetPollList.get(pls) == node.getNodeAddress()) {
                        node.setPollListPosition(pls + 1);
                        assigned = true;
                    }
                }
                if (!assigned) {
                    node.setPollListPosition(nextPollPos++);
                }
            }

            // CMRInet Options
            //----------------
            if (findParmValue(n, "cmrinetoptions") != null) {
                opts = findParmValue(n, "cmrinetoptions");
                // Convert and load the  value into the node options array
                for (int j = 0; j < SerialNode.NUMCMRINETOPTS; j++) {
                    node.setCMRInetOpts(j, (opts.charAt(j) - '0'));
                }
                log.debug("Node {} NET Options Read = {}", node.nodeAddress, opts);

            } else {
                // This must be the first time the nodes were loaded into a cpNode
                // supported version.  Set the Auto Poll option to avoid confusion
                // with installed configurations.
                for (int j = 0; j < SerialNode.NUMCMRINETOPTS; j++) {
                    node.setCMRInetOpts(j, 0);
                }
                node.setOptNet_AUTOPOLL(1);
                log.debug("Node {} AUTO POLL Set ", node.nodeAddress);

            }

            for (int j = 0; j < slb.length(); j++) {
                node.setLocSearchLightBits(j, (slb.charAt(j) - '0'));
            }

            for (int j = 0; j < ctl.length(); j++) {
                node.setCardTypeLocation(j, (ctl.charAt(j) - '0'));
            }

            if (type == SerialNode.CPNODE || type == SerialNode.CPMEGA) {
                // cpNode Options
                if (findParmValue(n, "cpnodeoptions") != null) {
                    opts = findParmValue(n, "cpnodeoptions");
                    // Convert and load the  value into the node options array
                    for (int j = 0; j < SerialNode.NUMCPNODEOPTS; j++) {
                        node.setcpnodeOpts(j, (opts.charAt(j) - '0'));
                    }
                }

                log.debug("Node {} NODE Options Read = {}", node.nodeAddress, opts);
            }

            if (findParmValue(n, "cmrinodedesc") != null) {
                node.setcmriNodeDesc(findParmValue(n, "cmrinodedesc"));
            } else {
                log.debug("No Description - Node {}", addr);
            }

            // Trigger initialization of this Node to reflect these parameters
            ((CMRISystemConnectionMemo)adapter.getSystemConnectionMemo()).getTrafficController().initializeSerialNode(node);
        }
    }

    @Override
    protected void register() {
        this.register(new ConnectionConfig(adapter));
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConnectionConfigXml.class);
}
