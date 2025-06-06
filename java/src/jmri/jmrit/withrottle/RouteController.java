package jmri.jmrit.withrottle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Route;
import jmri.RouteManager;
import jmri.Sensor;

/**
 *
 *
 * @author Brett Hoffman Copyright (C) 2010
 */
public class RouteController extends AbstractController implements PropertyChangeListener {

    private RouteManager manager = null;
    private HashMap<NamedBeanHandle<Sensor>, Route> indication;    //  Monitor turnouts for aligned status

    public RouteController() {
        manager = InstanceManager.getNullableDefault(jmri.RouteManager.class);
        if (manager == null) {
            log.info("No route manager instance.");
            isValid = false;
        } else {
            indication = new HashMap<>();
            isValid = true;
        }
    }

    @Override
    boolean verifyCreation() {

        return isValid;
    }

    @Override
    public void filterList() {
        ArrayList<String> tempList = new ArrayList<>(0);
        for (String sysName : sysNameList) {
            Route r = manager.getBySystemName(sysName);
            if (r != null) {
                Object o = r.getProperty("WifiControllable");
                if (o == null || Boolean.valueOf(o.toString())) {
                    //  Only skip if 'false'
                    tempList.add(sysName);
                }
            }
        }
        sysNameList = tempList;
    }

    /**
     * parse and process a route command message
     * <p>
     * Format: PRA[command][routename]
     *   where command is always '2' for Toggle
     *   routename is a complete system name
     * Can return HM error messages to client
     * @param message Command string to be parsed
     * @param deviceServer client to send responses (error messages) back to
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value="SLF4J_FORMAT_SHOULD_BE_CONST",
        justification="I18N of warn Message also sent to deviceServer")
    @Override
    void handleMessage(String message, DeviceServer deviceServer) {
        String rName = message.substring(2);                
        try {
            if (message.charAt(0) == 'A' && message.charAt(1) == '2') {
                Route r = manager.getBySystemName(rName);
                if (r != null) {
                    r.setRoute();
                    log.debug("Route '{}' set.", rName);
                } else {
                    String msg = Bundle.getMessage("ErrorRouteNotDefined", rName);                        
                    log.warn(msg);
                    deviceServer.sendAlertMessage(msg);
                }
            } else {
                String msg = Bundle.getMessage("ErrorRouteBadMessage", message);                        
                log.warn(msg);
                deviceServer.sendAlertMessage(msg);
            }

        } catch (NullPointerException exb) {
            String msg = Bundle.getMessage("ErrorRouteOther", rName);                        
            log.warn(msg);
            deviceServer.sendAlertMessage(msg);
        }
    }

    /**
     * Send Info on routes to devices, not specific to any one route.
     * <p>
     * Format: PRT]\[routeText}|{routeKey]\[stateText}|{stateKey]\[stateText}|{stateKey...
     */
    public void sendTitles() {
        if (listeners == null) {
            return;
        }

        StringBuilder labels = new StringBuilder("PRT");    //  Panel Route Titles

        labels.append("]\\[").append(Bundle.getMessage("MenuItemRouteTable")).append("}|{Route"); // should Route be translated?
        labels.append("]\\[").append(Bundle.getMessage("StateActive")).append("}|{2");
        labels.append("]\\[").append(Bundle.getMessage("StateInactive")).append("}|{4");
        labels.append("]\\[").append(Bundle.getMessage("StateUnknown")).append("}|{0");
        labels.append("]\\[").append(Bundle.getMessage("StateInconsistent")).append("}|{8");
        labels.append("]\\[").append(Bundle.getMessage("StateUnknown")).append("}|{"); //define no feedback as Unknown

        String message = labels.toString();

        for (ControllerInterface listener : listeners) {
            listener.sendPacketToDevice(message);
        }

    }

    private final jmri.NamedBeanHandleManager nbhm = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);

    /**
     * Send list of routes Format:
     * PRL]\[SysName}|{UsrName}|{CurrentState]\[SysName}|{UsrName}|{CurrentState
     * <p>
     * States: 1 - UNKNOWN, 2 - ACTIVE, 4 - INACTIVE (based on turnoutsAligned
     * sensor, if used)
     */
    public void sendList() {
        if (listeners == null) {
            return;
        }
        if (canBuildList) {
            buildList(manager);
        }
        if (sysNameList.isEmpty()) {
            return;
        }

        StringBuilder list = new StringBuilder("PRL");  //  Panel Route List

        for (String sysName : sysNameList) {
            Route r = manager.getBySystemName(sysName);
            if (r != null) {
                list.append("]\\[").append(sysName);
                list.append("}|{");
                if (r.getUserName() != null) {
                    list.append(r.getUserName());
                }
                list.append("}|{");
                String turnoutsAlignedSensor = r.getTurnoutsAlignedSensor();
                if (turnoutsAlignedSensor != null && !turnoutsAlignedSensor.isEmpty()) {  //only set if found
                    try {
                        Sensor routeAligned = InstanceManager.sensorManagerInstance()
                            .provideSensor(turnoutsAlignedSensor);
                        list.append(routeAligned.getKnownState());
                    } catch (IllegalArgumentException ex) {
                        log.warn("Failed to provide turnoutsAlignedSensor \"{}\" in sendList", turnoutsAlignedSensor);
                    }
                }
            }
        }
        String message = list.toString();

        for (ControllerInterface listener : listeners) {
            listener.sendPacketToDevice(message);
        }
    }

    /**
     * This is on the aligned sensor, not the route itself.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (Sensor.PROPERTY_KNOWN_STATE.equals(evt.getPropertyName())) {
            Sensor s = (Sensor) evt.getSource();
            for (Map.Entry<NamedBeanHandle<Sensor>, Route> entry : indication.entrySet()) {
                if (entry.getKey().getBean() == s) {
                    Route r = entry.getValue();
                    String message = "PRA" + s.getKnownState() + r.getSystemName();

                    for (ControllerInterface listener : listeners) {
                        listener.sendPacketToDevice(message);
                    }
                    return;
                }
            }
        }
    }

    /**
     * Register this as a listener of each managed route's aligned sensor.
     */
    @Override
    public void register() {
        for (String sysName : sysNameList) {
            Route r = manager.getBySystemName(sysName);
            if (r != null) {
                String turnoutsAlignedSensor = r.getTurnoutsAlignedSensor();
                if ( turnoutsAlignedSensor != null && !turnoutsAlignedSensor.isEmpty()) {  //only set if found
                    Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(turnoutsAlignedSensor);
                    NamedBeanHandle<Sensor> routeAligned = nbhm.getNamedBeanHandle(turnoutsAlignedSensor, sensor);
                    indication.put(routeAligned, r);
                    sensor.addPropertyChangeListener(this, routeAligned.getName(), "Wi Throttle Route Controller");
                    log.debug("Add listener to Sensor: {} for Route: {}", routeAligned.getName(), r.getSystemName());
                }
            }
        }
    }

    /**
     * Remove this from each managed route's aligned sensor.
     */
    @Override
    public void deregister() {
        if (sysNameList.isEmpty()) {
            return;
        }

        indication.keySet().forEach( namedSensor -> {
            namedSensor.getBean().removePropertyChangeListener(this);
            if (log.isDebugEnabled()) {
                log.debug("Removing listener from Sensor: {} for Route: {}",
                    namedSensor.getName(), indication.get(namedSensor).getSystemName());
            }
        });
        indication = new HashMap<>();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RouteController.class);

}
