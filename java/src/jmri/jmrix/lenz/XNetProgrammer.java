package jmri.jmrix.lenz;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;

import jmri.ProgrammingMode;
import jmri.jmrix.AbstractProgrammer;

/**
 * Convert the jmri.Programmer interface into commands for the Lenz XpressNet
 * <p>
 * The read operation state sequence is:
 * <ul>
 * <li>Send Register Mode / Paged mode /Direct Mode read request
 * <li>Wait for Broadcast Service Mode Entry message
 * <li>Send Request for Service Mode Results request
 * <li>Wait for results reply, interpret
 * <li>Send Resume Operations request
 * <li>Wait for Normal Operations Resumed broadcast
 * </ul>
 * <img src="doc-files/XPressNetProgrammer-StateDiagram.png" alt="UML State diagram">
 * <img src="doc-files/XPressNetProgrammer-SequenceDiagram.png" alt="UML Sequence diagram">
 *
 * @author Bob Jacobsen Copyright (c) 2002, 2007
 * @author Paul Bender Copyright (c) 2003-2010
 * @author Giorgio Terdina Copyright (c) 2007
 */

/*
 * @startuml jmri/jmrix/lenz/doc-files/XPressNetProgrammer-StateDiagram.png
 * state NormalMode{
 * [*] --> initialREQUESTSENT: readCV()
 * [*] --> initialREQUESTSENT: writeCV()
 * }
 * [*] --> NormalMode
 * state ServiceMode{
 * [*] --> INQUIRESENT
 * REQUESTSENT --> INQUIRESENT : Command Successfully Received
 * REQUESTSENT --> NOTPROGRAMMING : timeout()
 * INQUIRESENT --> NOTPROGRAMMING : Result Received
 * INQUIRESENT --> NOTPROGRAMMING : timeout()
 * NOTPROGRAMMING --> REQUESTSENT : readCV()
 * NOTPROGRAMMING --> REQUESTSENT : writeCV()
 * NOTPROGRAMMING --> RequestNormalOps : timeout()
 * RequestNormalOps --> [*]
 * }
 * NormalMode --> ServiceMode : Service Mode Entry Received
 * ServiceMode --> [*] : Normal Operations Resumed
 * @enduml
 *
 * @startuml jmri/jmrix/lenz/doc-files/XPressNetProgrammer-SequenceDiagram.png
 * actor user
 * control programmer
 * user -> programmer:read/write CV
 * programmer -> XNetProgrammer:readCV()/writeCV()
 * XNetProgrammer -> CommandStation: Read/Write CV in appropriate mode.
 * CommandStation -> XNetProgrammer: Service Mode Entry.
 * XNetProgrammer -> CommandStation: Request Service Mode Results.
 * CommandStation -> XNetProgrammer: Service Mode Result or Error Message
 * XNetProgrammer -> programmer: CV Value or Error Message
 * programmer -> user: CV value or Error Message
 * loop 0 or more times
 * user -> programmer:read/write CV
 * programmer -> XNetProgrammer:readCV()/writeCV()
 * XNetProgrammer -> CommandStation: Read/Write CV in appropriate mode.
 * CommandStation -> XNetProgrammer: Command Successfully Received.
 * XNetProgrammer -> CommandStation: Request Service Mode Results.
 * CommandStation -> XNetProgrammer: Service Mode Result or Error Message
 * XNetProgrammer -> programmer: CV Value or Error Message
 * programmer -> user: CV value or Error Message
 * end
 * XNetProgrammer -> CommandStation: Resume Normal Operations
 * CommandStation -> XNetProgrammer: Normal Operations Resumed
 * @enduml
 */
public class XNetProgrammer extends AbstractProgrammer implements XNetListener {

    protected static final int XNetProgrammerTimeout = 90000;

    // keep track of whether or not the command station is in service
    // mode.  Used for determining if "OK" message is an aproriate
    // response to a request to a programming request.
    protected boolean _service_mode = false;

    public XNetProgrammer(XNetTrafficController tc) {
        // error if more than one constructed?

        _controller = tc;

        // connect to listen
        controller().addXNetListener(XNetInterface.CS_INFO
                | XNetInterface.COMMINFO
                | XNetInterface.INTERFACE,
                this);

        setMode(ProgrammingMode.DIRECTBYTEMODE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public List<ProgrammingMode> getSupportedModes() {
        List<ProgrammingMode> ret = new ArrayList<>();
        ret.add(ProgrammingMode.DIRECTBYTEMODE);
        ret.add(ProgrammingMode.DIRECTBITMODE);
        ret.add(ProgrammingMode.PAGEMODE);
        ret.add(ProgrammingMode.REGISTERMODE);
        return ret;
    }

    /**
     * {@inheritDoc}
     *
     * Can we read from a specific CV in the specified mode? Answer may not be
     * correct if the command station type and version sent by the command
     * station mimics one of the known command stations.
     */
    @Override
    public boolean getCanRead(String addr) {
        if (log.isDebugEnabled()) {
            log.debug("check mode {} CV {}", getMode(), addr);
        }
        if (!getCanRead()) {
            return false; // check basic implementation first
        }
        // Multimaus cannot read CVs, unless Rocomotion interface is used, assume other Command Stations do.
        // To be revised if and when a Rocomotion adapter is introduced!!!
        if (controller().getCommandStation().getCommandStationType() == 0x10) {
            return false;
        }

        if (getMode().equals(ProgrammingMode.DIRECTBITMODE) || getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
            switch (controller().getCommandStation().getCommandStationType()) {
                case XNetConstants.CS_TYPE_LZ100:
                    if (controller().getCommandStation()
                            .getCommandStationSoftwareVersion() <= 3.5) {
                        return Integer.parseInt(addr) <= 256;
                    } else {
                        return Integer.parseInt(addr) <= 1024;
                    }
                default:
                    return Integer.parseInt(addr) <= 256;
            }
        } else {
            return Integer.parseInt(addr) <= 256;
        }
    }

    /**
     * {@inheritDoc}
     *
     * Can we write to a specific CV in the specified mode? Answer may not be
     * correct if the command station type and version sent by the command
     * station mimics one of the known command stations.
     */
    @Override
    public boolean getCanWrite(String addr) {
        if (log.isDebugEnabled()) {
            log.debug("check CV {} ", addr);
            log.debug("Command Station Version {}", controller().getCommandStation().getVersionString());

        }
        if (!getCanWrite()) {
            return false; // check basic implementation first
        }
        if (getMode().equals(ProgrammingMode.DIRECTBITMODE) || getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
            switch (controller().getCommandStation().getCommandStationType()) {
                case XNetConstants.CS_TYPE_LZ100:
                    if (controller().getCommandStation()
                            .getCommandStationSoftwareVersion() <= 3.5) {
                        return Integer.parseInt(addr) <= 256;
                    } else {
                        return Integer.parseInt(addr) <= 1024;
                    }
                default:
                    return Integer.parseInt(addr) <= 256;
            }
        } else {
            return Integer.parseInt(addr) <= 256;
        }
    }

    // members for handling the programmer interface
    protected int progState = 0;
    protected static final int NOTPROGRAMMING = 0; // is notProgramming
    protected static final int REQUESTSENT = 1; // waiting reply to command to go into programming mode
    protected static final int INQUIRESENT = 2; // read/write command sent, waiting reply
    protected boolean _progRead = false;
    protected int _val; // remember the value being read/written for confirmative reply
    protected int _cv; // remember the cv being read/written

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void writeCV(String CVname, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.debug("writeCV {} listens {} ",CV,p);
        useProgrammer(p);
        _progRead = false;
        // set new state & save values
        progState = REQUESTSENT;
        _val = val;
        _cv = 0xffff & CV;

        try {
            // start the error timer
            restartTimer(XNetProgrammerTimeout);

            // format and send message to go to program mode
            if (getMode().equals(ProgrammingMode.PAGEMODE)) {
                XNetMessage msg = XNetMessage.getWritePagedCVMsg(CV, val);
                controller().sendXNetMessage(msg, this);
            } else if (getMode().equals(ProgrammingMode.DIRECTBITMODE) || getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
                XNetMessage msg = XNetMessage.getWriteDirectCVMsg(CV, val);
                controller().sendXNetMessage(msg, this);
            } else { // register mode by elimination
                XNetMessage msg = XNetMessage.getWriteRegisterMsg(registerFromCV(CV), val);
                controller().sendXNetMessage(msg, this);
            }
        } catch (jmri.ProgrammerException e) {
            progState = NOTPROGRAMMING;
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void confirmCV(String CV, int val, jmri.ProgListener p) throws jmri.ProgrammerException {
        readCV(CV, p);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void readCV(String CVname, jmri.ProgListener p) throws jmri.ProgrammerException {
        final int CV = Integer.parseInt(CVname);
        log.debug("readCV {} listenes {}",CV,p);
        // If can't read (e.g. multiMaus CS), this shouldnt be invoked, but
        // still we need to do something rational by returning a NotImplemented error
        if (!getCanRead()) {
            notifyProgListenerEnd(p, CV, jmri.ProgListener.NotImplemented);
            return;
        }
        useProgrammer(p);
        _cv = 0xffff & CV;
        _progRead = true;
        // set new state
        progState = REQUESTSENT;
        try {
            // start the error timer
            restartTimer(XNetProgrammerTimeout);

            // format and send message to go to program mode
            if (getMode().equals(ProgrammingMode.PAGEMODE)) {
                XNetMessage msg = XNetMessage.getReadPagedCVMsg(CV);
                controller().sendXNetMessage(msg, this);
            } else if (getMode().equals(ProgrammingMode.DIRECTBITMODE) || getMode().equals(ProgrammingMode.DIRECTBYTEMODE)) {
                XNetMessage msg = XNetMessage.getReadDirectCVMsg(CV);
                controller().sendXNetMessage(msg, this);
            } else { // register mode by elimination
                XNetMessage msg = XNetMessage.getReadRegisterMsg(registerFromCV(CV));
                controller().sendXNetMessage(msg, this);
            }
        } catch (jmri.ProgrammerException e) {
            progState = NOTPROGRAMMING;
            throw e;
        }

    }

    private jmri.ProgListener _usingProgrammer = null;

    // internal method to remember who's using the programmer
    protected void useProgrammer(jmri.ProgListener p) throws jmri.ProgrammerException {
        // test for only one!
        if (_usingProgrammer != null && _usingProgrammer != p) {
            log.info("programmer already in use by {}",_usingProgrammer);
            throw new jmri.ProgrammerException("programmer in use");
        } else {
            _usingProgrammer = p;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void message(XNetReply m) {
        if (m.getElement(0) == XNetConstants.CS_INFO
                && m.getElement(1) == XNetConstants.BC_SERVICE_MODE_ENTRY) {
            if (!_service_mode) {
                // the command station is in service mode.  An "OK"
                // message can trigger a request for service mode
                // results if progrstate is REQUESTSENT.
                _service_mode = true;
            } else {  // _ service_mode == true
                // Since we get this message as both a broadcast and
                // a directed message, ignore the message if we're
                //already in the indicated mode
                return;
            }
        }
        if (m.getElement(0) == XNetConstants.CS_INFO
                && m.getElement(1) == XNetConstants.BC_NORMAL_OPERATIONS) {
            if (_service_mode) {
                // the command station is not in service mode.  An
                // "OK" message can not trigger a request for service
                // mode results if progrstate is REQUESTSENT.
                _service_mode = false;
            } else { // _service_mode == false
                // Since we get this message as both a broadcast and
                // a directed message, ignore the message if we're
                //already in the indicated mode
                return;
            }
        }
        if (progState == NOTPROGRAMMING) {
            // we get the complete set of replies now, so ignore these

        } else if (progState == REQUESTSENT) {
            if (log.isDebugEnabled()) {
                log.debug("reply in REQUESTSENT state");
            }
            // see if reply is the acknowledge of program mode; if not, wait for next
            if ((_service_mode && ( m.isOkMessage() || m.isTimeSlotRestored() ))
                    || (m.getElement(0) == XNetConstants.CS_INFO
                    && (m.getElement(1) == XNetConstants.BC_SERVICE_MODE_ENTRY
                    || m.getElement(1) == XNetConstants.PROG_CS_READY))) {
                if (!getCanRead()) {
                    // on systems like the Roco MultiMaus
                    // (which does not support reading)
                    // let a timeout occur so the system
                    // has time to write data to the
                    // decoder
                    restartTimer(SHORT_TIMEOUT);
                    return;
                }

                // here ready to request the results
                progState = INQUIRESENT;
                //start the error timer
                restartTimer(XNetProgrammerTimeout);

                controller().sendXNetMessage(XNetMessage.getServiceModeResultsMsg(),
                        this);
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.CS_NOT_SUPPORTED) {
                // programming operation not supported by this command station
                progState = NOTPROGRAMMING;
                notifyProgListenerEnd(_val, jmri.ProgListener.NotImplemented);
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.BC_NORMAL_OPERATIONS) {
                // We Exited Programming Mode early
                log.error("Service mode exited before sequence complete.");
                progState = NOTPROGRAMMING;
                stopTimer();
                notifyProgListenerEnd(_val, jmri.ProgListener.SequenceError);
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.PROG_SHORT_CIRCUIT) {
                // We experienced a short Circuit on the Programming Track
                log.error("Short Circuit While Programming Decoder");
                progState = NOTPROGRAMMING;
                stopTimer();
                notifyProgListenerEnd(_val, jmri.ProgListener.ProgrammingShort);
            } else if (m.isTimeSlotErrorMessage()){
                // we just ignore timeslot errors in the programmer.
            } else if (m.isCommErrorMessage()) {
                // We experienced a communications error
                if(_controller.hasTimeSlot()) {
                   // We have a timeslot, so report it as an error
                   log.error("Communications error in REQUESTSENT state while programming.  Error: {}",m);
                   progState = NOTPROGRAMMING;
                   stopTimer();
                   notifyProgListenerEnd(_val, jmri.ProgListener.CommError);
                }
            }
        } else if (progState == INQUIRESENT) {
            if (log.isDebugEnabled()) {
                log.debug("reply in INQUIRESENT state");
            }
            // check for right message, else return
            if (m.isPagedModeResponse()) {
                // valid operation response, but does it belong to us?
                try {
                    // we always save the cv number, but if
                    // we are using register mode, there is
                    // at least one case (CV29) where the value
                    // returned does not match the value we saved.
                    if (m.getServiceModeCVNumber() != _cv
                            && m.getServiceModeCVNumber() != registerFromCV(_cv)) {
                        log.debug(" result for CV {} expecting {}",m.getServiceModeCVNumber(),_cv);
                        return;
                    }
                } catch (jmri.ProgrammerException e) {
                    progState = NOTPROGRAMMING;
                    notifyProgListenerEnd(_val, jmri.ProgListener.UnknownError);
                }
                // see why waiting
                if (_progRead) {
                    // read was in progress - get return value
                    _val = m.getServiceModeCVValue();
                }
                progState = NOTPROGRAMMING;
                stopTimer();
                // if this was a read, we cached the value earlier.
                // If its a write, we're to return the original write value
                notifyProgListenerEnd(_val, jmri.ProgListener.OK);
            } else if (m.isDirectModeResponse()) {
                // valid operation response, but does it belong to us?
                if (m.getServiceModeCVNumber() != _cv) {
                    log.debug(" CV read {} expecting {}",m.getServiceModeCVNumber(),_cv);
                    return;
                }

                // see why waiting
                if (_progRead) {
                    // read was in progress - get return value
                    _val = m.getServiceModeCVValue();
                }
                progState = NOTPROGRAMMING;
                stopTimer();
                // if this was a read, we cached the value earlier.  If its a
                // write, we're to return the original write value
                notifyProgListenerEnd(_val, jmri.ProgListener.OK);
            } else if (m.getElement(0) == XNetConstants.CS_SERVICE_MODE_RESPONSE
                    && (m.getElement(1) & 0x14) == (0x14)) {
                // valid operation response, but does it belong to us?
                int sent_cv = m.getServiceModeCVNumber();
                if (sent_cv != _cv && (sent_cv == 0 && _cv != 0x0400)) {
                    return;
                }
                // see why waiting
                if (_progRead) {
                    // read was in progress - get return value
                    _val = m.getServiceModeCVValue();
                }
                progState = NOTPROGRAMMING;
                stopTimer();
                // if this was a read, we cached the value earlier.  If its a
                // write, we're to return the original write value
                notifyProgListenerEnd(_val, jmri.ProgListener.OK);
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.PROG_BYTE_NOT_FOUND) {
                // "data byte not found", e.g. no reply
                progState = NOTPROGRAMMING;
                stopTimer();
                notifyProgListenerEnd(_val, jmri.ProgListener.NoLocoDetected);
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.BC_NORMAL_OPERATIONS) {
                // We Exited Programming Mode early
                log.error("Service Mode exited before sequence complete.");
                progState = NOTPROGRAMMING;
                stopTimer();
                notifyProgListenerEnd(_val, jmri.ProgListener.SequenceError);
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.PROG_SHORT_CIRCUIT) {
                // We experienced a short Circuit on the Programming Track
                log.error("Short Circuit While Programming Decoder");
                progState = NOTPROGRAMMING;
                stopTimer();
                notifyProgListenerEnd(_val, jmri.ProgListener.ProgrammingShort);
            } else if (m.getElement(0) == XNetConstants.CS_INFO
                    && m.getElement(1) == XNetConstants.PROG_CS_BUSY) {
                // Command station indicated it was busy in
                // programming mode, request results again
                // (do not reset timer or change mode)
                // NOTE: Currently only sent by OpenDCC.
                controller().sendXNetMessage(XNetMessage.getServiceModeResultsMsg(),
                        this);
            } else if (m.isTimeSlotErrorMessage()){
                // we just ignore timeslot errors in the programmer.
            } else if (m.isCommErrorMessage()) {
                // We experienced a communications error
                if(_controller.hasTimeSlot()) {
                   // We have a timeslot, so report it as an error
                   log.error("Communications error in INQUIRESENT state while programming.  Error: {}", m);
                   progState = NOTPROGRAMMING;
                   stopTimer();
                   notifyProgListenerEnd(_val, jmri.ProgListener.CommError);
               }
            } else {
                // nothing important, ignore
                log.debug("Ignoring message {}",m);
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("reply in un-decoded state");
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public synchronized void message(XNetMessage l) {
      // The prgrammer does not use outgoing XpressNet messges.
    }

    /**
     * {@inheritDoc}
     *
     * Log and ignore
     */
    @Override
    public void notifyTimeout(XNetMessage msg) {
        log.debug("Notified of timeout on message {}",msg);
    }

    /**
     * Since the Lenz programming sequence requires several
     * operations, we want to be able to check and see if we are
     * currently programming before allowing the Traffic Controller
     * to send a request to exit service mode.
     * @return true if programmer busy, else false.
     */
    public synchronized boolean programmerBusy() {
        return (progState != NOTPROGRAMMING);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected synchronized void timeout() {
        if (progState != NOTPROGRAMMING) {
            // we're programming, time to stop
            if (log.isDebugEnabled()) {
                log.debug("timeout!");
            }
            // perhaps no loco present? Fail back to end of programming
            progState = NOTPROGRAMMING;
            if (getCanRead()) {
                notifyProgListenerEnd(_val, jmri.ProgListener.FailedTimeout);
            } else {
                notifyProgListenerEnd(_val, jmri.ProgListener.OK);
            }
        }
    }

    /**
     * Internal method to notify of the final result
     * @param value Value returned
     * @param status Status of operation
     */
    protected void notifyProgListenerEnd(int value, int status) {
        log.debug("notifyProgListenerEnd value {} status {}.",value,status);
        // programmingOpReply, called by noitfyProgListenerEnd
        // in the super class, might send an immediate reply, so
        // clear the current listener _first_
        jmri.ProgListener temp = _usingProgrammer;
        _usingProgrammer = null;
        notifyProgListenerEnd(temp,value, status);
    }

    XNetTrafficController _controller;

    protected XNetTrafficController controller() {
        return _controller;
    }

    @Override
    public Configurator getConfigurator() {
        return new XNetConfigurator();
    }


    public class XNetConfigurator implements Configurator {

        public boolean programmerBusy() {
            return XNetProgrammer.this.programmerBusy();
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XNetProgrammer.class);

}
