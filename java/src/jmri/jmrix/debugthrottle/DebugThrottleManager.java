package jmri.jmrix.debugthrottle;

import java.util.EnumSet;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottleManager;

/**
 * Implementation of a ThrottleManager for debugging.
 *
 * @author Bob Jacobsen Copyright (C) 2003, 2005
 */
public class DebugThrottleManager extends AbstractThrottleManager {

    @Deprecated( since="5.13.3", forRemoval=true)
    public DebugThrottleManager() {
        this(jmri.InstanceManager.getDefault(jmri.jmrix.internal.InternalSystemConnectionMemo.class));
    }

    /**
     * Constructor.
     * @param memo system connection.
     */
    public DebugThrottleManager(jmri.SystemConnectionMemo memo) {
        super(memo);
    }

    @Override
    public void requestThrottleSetup(LocoAddress a, boolean control) {
        if (a instanceof DccLocoAddress) {
            // Immediately trigger the callback.
            DccLocoAddress address = (DccLocoAddress) a;
            log.debug("new debug throttle for {}", address);
            notifyThrottleKnown(new DebugThrottle(address, adapterMemo), a);
        }
        else {
            log.error("LocoAddress {} is not a DccLocoAddress",a);
        }
    }

    /**
     * Address 1 and above can be a long address
     *
     */
    @Override
    public boolean canBeLongAddress(int address) {
        return (address >= 1);
    }

    /**
     * Address 127 and below can be a short address
     *
     */
    @Override
    public boolean canBeShortAddress(int address) {
        return (address <= 127);
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    @Override
    public boolean addressTypeUnique() {
        return false;
    }

    @Override
    public boolean disposeThrottle(DccThrottle t, jmri.ThrottleListener l) {
        log.debug("disposeThrottle called for {}", t);
        if (super.disposeThrottle(t, l)) {
            if (t instanceof DebugThrottle) {
                DebugThrottle lnt = (DebugThrottle) t;
                lnt.throttleDispose();
                return true;
            }
            else {
                log.error("DccThrottle {} is not a DebugThrottle",t);
            }
        }
        return false;
    }

    /**
     * What speed modes are supported by this system?
     * {@inheritDoc }
     */
    @Override
    public EnumSet<SpeedStepMode> supportedSpeedModes() {
        return EnumSet.allOf(SpeedStepMode.class);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DebugThrottleManager.class);

}
