package jmri.jmrix.mqtt.configurexml;

import org.jdom2.Element;

/**
 * Provides load and store functionality for configuring MqttTurnoutManagers.
 * <p>
 * Uses the store method from the abstract base class, but provides a load
 * method here.
 *
 * @author Lionel Jeanson Copyright: Copyright (c) 2017
 */
public class MqttTurnoutManagerXml extends jmri.managers.configurexml.AbstractTurnoutManagerConfigXML {

    public MqttTurnoutManagerXml() {
        super();
    }

    @Override
    public void setStoreElementClass(Element turnouts) {
        turnouts.setAttribute("class", "jmri.jmrix.mqtt.configurexml.MqttTurnoutManagerXml");
    }

    @Override
    public boolean load(Element shared, Element perNode) {
        // load individual turnouts
        return loadTurnouts(shared, perNode);
    }

    // initialize logging
//    private final static Logger log = LoggerFactory.getLogger(MqttTurnoutManagerXml.class);
}
