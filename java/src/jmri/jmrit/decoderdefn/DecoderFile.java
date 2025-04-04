package jmri.jmrit.decoderdefn;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.swing.JLabel;

import jmri.LocoAddress;
import jmri.Programmer;
import jmri.jmrit.XmlFile;
import jmri.jmrit.symbolicprog.ResetTableModel;
import jmri.jmrit.symbolicprog.ExtraMenuTableModel;
import jmri.jmrit.symbolicprog.VariableTableModel;
import org.jdom2.DataConversionException;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents and manipulates a decoder definition, both as a file and in
 * memory. The internal storage is a JDOM tree.
 * <p>
 * This object is created by DecoderIndexFile to represent the decoder
 * identification info _before_ the actual decoder file is read.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Howard G. Penny Copyright (C) 2005
 * @see jmri.jmrit.decoderdefn.DecoderIndexFile
 */
public class DecoderFile extends XmlFile {

    public DecoderFile() {
    }

    /**
     * Create a mechanism to manipulate a decoder definition from up to 10 parameters.
     *
     * @param mfg manufacturer name
     * @param mfgID manufacturer's NMRA manufacturer number, typically a "CV8" value
     * @param model decoder model designation
     * @param lowVersionID decoder version low byte, where applicable
     * @param highVersionID decoder version high byte, where applicable
     * @param family decoder family name, where applicable
     * @param filename filename of decoder XML definition
     * @param numFns decoder's number of available functions
     * @param numOuts decoder's number of available function outputs
     * @param decoder Element containing decoder XML definition
     */
    public DecoderFile(String mfg, String mfgID, String model, String lowVersionID,
            String highVersionID, String family, String filename,
            int numFns, int numOuts, Element decoder) {
        _mfg = mfg;
        _mfgID = mfgID;
        _model = model;
        _family = family;
        _filename = filename;
        _numFns = numFns;
        _numOuts = numOuts;
        _element = decoder;

        log.trace("Create DecoderFile with Family \"{}\" Model \"{}\"", family, model);

        // store the default range of version id's
        setVersionRange(lowVersionID, highVersionID);
    }

    /**
     * Create a mechanism to manipulate a decoder definition from up to 12 parameters.
     *
     * @param mfg manufacturer name
     * @param mfgID manufacturer's NMRA manufacturer number, typically a "CV8" value
     * @param model decoder model designation
     * @param lowVersionID decoder version low byte, where applicable
     * @param highVersionID decoder version high byte, where applicable
     * @param family decoder family name, where applicable
     * @param filename filename of decoder XML definition
     * @param numFns decoder's number of available functions
     * @param numOuts decoder's number of available function outputs
     * @param decoder Element containing decoder XML definition
     * @param replacementModel name of decoder file (which replaces this one?)
     * @param replacementFamily name of decoder family (which replaces this one?)
     */
    public DecoderFile(String mfg, String mfgID, String model, String lowVersionID,
            String highVersionID, String family, String filename,
            int numFns, int numOuts, Element decoder, String replacementModel, String replacementFamily) {
        this(mfg, mfgID, model, lowVersionID,
                highVersionID, family, filename,
                numFns, numOuts, decoder);
        _replacementModel = replacementModel;
        _replacementFamily = replacementFamily;
        _developerID = "-1";
        if (mfgID.compareTo("") != 0) {
            // do not have manufacturerID, so take mfgID (which might not be set!)
            _manufacturerID = mfgID;
        } else {
            _manufacturerID = "-1";
        }
        _productID = "-1";
    }

    /**
     * Create a mechanism to manipulate a decoder definition from up to 15 parameters.
     *
     * @param mfg manufacturer name
     * @param mfgID manufacturer's NMRA manufacturer number, typically a "CV8" value
     * @param model decoder model designation
     * @param lowVersionID decoder version low byte, where applicable
     * @param highVersionID decoder version high byte, where applicable
     * @param family decoder family name, where applicable
     * @param filename filename of decoder XML definition
     * @param developerID (typically LocoNet SV2) developerID number (8 bits)
     * @param manufacturerID manufacturerID number (8 bits)
     * @param productID product ID number (16 bits)
     * @param numFns decoder's number of available functions
     * @param numOuts decoder's number of available function outputs
     * @param decoder Element containing decoder XML definition
     * @param replacementModel name of decoder file (which replaces this one?)
     * @param replacementFamily name of decoder family (which replaces this one?)
     */
    public DecoderFile(String mfg, String mfgID, String model, String lowVersionID,
            String highVersionID, String family, String filename,
            String developerID, String manufacturerID, String productID,
            int numFns, int numOuts, Element decoder, String replacementModel,
            String replacementFamily) {
        this(mfg, mfgID, model, lowVersionID,
                highVersionID, family, filename,
                numFns, numOuts, decoder);
        _replacementModel = replacementModel;
        _replacementFamily = replacementFamily;
        _developerID = developerID;
        if (mfgID == null) {
            log.error("mfgID missing for decoder file {}", filename);
        }
        if ((!manufacturerID.isEmpty()) && (manufacturerID.compareTo("-1") != 0)) {
            // prefer manufacturerID over mfgID
            _manufacturerID = manufacturerID;
        } else if ((mfgID != null) && (mfgID.compareTo("") != 0)) {
            // do not have manufacturerID, so take mfgID (which might not be set!)
            _manufacturerID = mfgID;
        } else {
            _manufacturerID = "-1";
        }
        _productID = productID;
    }

    /**
     * Create a mechanism to manipulate a decoder definition from up to 16 parameters.
     *
     * @param mfg manufacturer name
     * @param mfgID manufacturer's NMRA manufacturer number, typically a "CV8" value
     * @param model decoder model designation
     * @param lowVersionID decoder version low byte, where applicable
     * @param highVersionID decoder version high byte, where applicable
     * @param family decoder family name, where applicable
     * @param filename filename of decoder XML definition
     * @param developerID (typically LocoNet SV2) developerID number (8 bits)
     * @param manufacturerID manufacturerID number (8 bits)
     * @param productID product ID number (16 bits)
     * @param numFns decoder's number of available functions
     * @param numOuts decoder's number of available function outputs
     * @param decoder Element containing decoder XML definition
     * @param replacementModel name of decoder file (which replaces this one?)
     * @param replacementFamily name of decoder family (which replaces this one?)
     * @param programmingModes a comma-separated list of supported programming modes
     */
    public DecoderFile(String mfg, String mfgID, String model, String lowVersionID,
                       String highVersionID, String family, String filename,
                       String developerID, String manufacturerID, String productID,
                       int numFns, int numOuts, Element decoder, String replacementModel,
                       String replacementFamily, String programmingModes) {
        this(mfg, mfgID, model, lowVersionID,
                highVersionID, family, filename,
                developerID, manufacturerID, productID,
                numFns, numOuts, decoder, replacementModel,
                replacementFamily);

        log.debug("DecoderFile {} created with ProgModes: {}", model, programmingModes);
        _programmingModes = Objects.requireNonNullElse(programmingModes, "");
    }

    // store acceptable version numbers
    boolean[] versions = new boolean[256];

    public void setOneVersion(int i) {
        versions[i] = true;
    }

    public void setVersionRange(int low, int high) {
        for (int i = low; i <= high; i++) {
            versions[i] = true;
        }
    }

    public void setVersionRange(String lowVersionID, String highVersionID) {
        if (lowVersionID != null) {
            // lowVersionID is not null; check high version ID
            if (highVersionID != null) {
                // low version and high version are not null
                setVersionRange(Integer.parseInt(lowVersionID),
                        Integer.parseInt(highVersionID));
            } else {
                // low version not null, but high is null. This is
                // a single value to match
                setOneVersion(Integer.parseInt(lowVersionID));
            }
        } else {
            // lowVersionID is null; check high version ID
            if (highVersionID != null) {
                // low version null, but high is not null
                setOneVersion(Integer.parseInt(highVersionID));
            //} else {
                // both low and high version are null; do nothing
            }
        }
    }

    /**
     * Test for correct decoder version number
     *
     * @param i the version to match
     * @return true if decoder version matches i
     */
    public boolean isVersion(int i) {
        return versions[i];
    }

    /**
     * return array of versions
     *
     * @return array of boolean where each element is true if version matches
     */
    public boolean[] getVersions() {
        return Arrays.copyOf(versions, versions.length);
    }

    @Nonnull
    public String getVersionsAsString() {
        String ret = "";
        int partStart = -1;
        String part;
        for (int i = 0; i < 256; i++) {
            if (partStart >= 0) {
                /* working on part, found end of range */
                if (!versions[i]) {
                    if (i - partStart > 1) {
                        part = partStart + "-" + (i - 1);
                    } else {
                        part = "" + (i - 1);
                    }
                    if (ret.isEmpty()) {
                        ret = part;
                    } else {
                        ret = "," + part;
                    }
                    partStart = -1;
                }
            } else {
                /* testing for new part */
                if (versions[i]) {
                    partStart = i;
                }
            }
        }
        if (partStart >= 0) {
            if (partStart != 255) {
                part = partStart + "-" + 255;
            } else {
                part = "" + partStart;
            }
            if (ret.isEmpty()) {
                ret = ret + "," + part;
            } else {
                ret = part;
            }
        }
        return (ret);
    }

    // store indexing information
    String _mfg = null;
    String _mfgID = null;
    String _model = null;
    String _family = null;
    String _filename = null;
    String _productID = null;
    String _replacementModel = null;
    String _replacementFamily = null;
    String _developerID = null;
    String _manufacturerID = null;
    String _programmingModes = null;
    int _numFns = -1;
    int _numOuts = -1;
    Element _element = null;

    public String getMfg() {
        return _mfg;
    }

    public String getMfgID() {
        return _mfgID;
    }

    /**
     * Get the (LocoNet SV2) "Developer ID" number.
     * <p>
     * This value is assigned by the device
     * manufacturer and is an 8-bit number.
     * @return the developerID number
     */
    public String getDeveloperID() {
        return _developerID;
    }

    /**
     * Get the (LocoNet SV2/Uhlenbrock LNCV) "Manufacturer ID" number.
     * <p>
     * This value typically matches the NMRA
     * manufacturer ID number and is an 8-bit number.
     *
     * @return the manufacturer number
     */
    public String getManufacturerID() {
        return _manufacturerID;
    }

    public String getModel() {
        return _model;
    }

    public String getFamily() {
        return _family;
    }

    public String getReplacementModel() {
        return _replacementModel;
    }

    public String getReplacementFamily() {
        return _replacementFamily;
    }

    public String getFileName() {
        return _filename;
    }

    public int getNumFunctions() {
        return _numFns;
    }

    public int getNumOutputs() {
        return _numOuts;
    }

    public Showable getShowable() {
        if (_element.getAttribute("show") == null) {
            return Showable.YES; // default
        } else if (_element.getAttributeValue("show").equals("no")) {
            return Showable.NO;
        } else if (_element.getAttributeValue("show").equals("maybe")) {
            return Showable.MAYBE;
        } else {
            log.error("unexpected value for show attribute: {}", _element.getAttributeValue("show"));
            return Showable.YES; // default again
        }
    }

    public enum Showable {
        YES, NO, MAYBE
    }

    public String getModelComment() {
        return _element.getAttributeValue("comment");
    }

    public String getFamilyComment() {
        return ((Element) _element.getParent()).getAttributeValue("comment");
    }

    /**
     * Get the "Product ID" value.
     * <p>
     * When applied to LocoNet devices programmed using the LocoNet SV2 or the Uhlenbrock LNCV protocol,
     * this is a 16-bit value, and is used in identifying the decoder definition
     * file that matches an SV2 or LNCV device.
     * <p>
     * Decoders which do not support SV2 or LNCV programming may use the Product ID
     * value for other purposes.
     *
     * @return the productID number
     */
    public String getProductID() {
        _productID = _element.getAttributeValue("productID");
        return _productID;
    }

    public Element getModelElement() {
        return _element;
    }

    ArrayList<LocoAddress.Protocol> protocols = null;

    public LocoAddress.Protocol[] getSupportedProtocols() {
        if (protocols == null) {
            setSupportedProtocols();
        }
        return protocols.toArray(new LocoAddress.Protocol[0]);
    }

    private void setSupportedProtocols() {
        protocols = new ArrayList<>();
        if (_element.getChild("protocols") != null) {
            List<Element> protocolList = _element.getChild("protocols").getChildren("protocol");
            protocolList.forEach((e) -> protocols.add(LocoAddress.Protocol.getByShortName(e.getText())));
        }
    }

    /**
     * Get all specified programming modes a decoder xml supports.
     * This does not include the programming attributes (like ops=false).
     *
     * @return a comma separated string of modes as specified in the decoder xml
     * or empty string when none are specified
     */
    public @Nonnull String getProgrammingModes() {
        if (_programmingModes == null) {
            _programmingModes = "";
        }
        return _programmingModes;
    }

    public boolean isProgrammingMode(String mode) {
        return getProgrammingModes().contains(mode);
    }

    // static service methods - extract info from a given Element
    public static String getMfgName(Element decoderElement) {
        return decoderElement.getChild("family").getAttribute("mfg").getValue();
    }

    public static String getProgrammingModes(Element decoderElement) {
        return decoderElement.getChild("programming").getChild("mode").getText();
    }

    boolean isProductIDok(Element e, String extraInclude, String extraExclude) {
        return isIncluded(e, _productID, _model, _family, extraInclude, extraExclude);
    }

    /**
     * @param e            XML element with possible "include" and "exclude"
     *                     attributes to be checked
     * @param productID    the specific ID of the decoder being loaded, to check
     *                     against include/exclude conditions
     * @param modelID      the model ID of the decoder being loaded, to check
     *                     against include/exclude conditions
     * @param familyID     the family ID of the decoder being loaded, to check
     *                     against include/exclude conditions
     * @param extraInclude additional "include" terms
     * @param extraExclude additional "exclude" terms
     * @return true if element is included; false otherwise
     */
    public static boolean isIncluded(Element e, String productID, String modelID, String familyID, String extraInclude, String extraExclude) {
        String include = e.getAttributeValue("include");
        if (include != null) {
            include = include + "," + extraInclude;
        } else {
            include = extraInclude;
        }
        // if there are any include clauses, then it has to match
        if (!include.isEmpty() && !(isInList(productID, include) || isInList(modelID, include) || isInList(familyID, include))) {
            if (log.isTraceEnabled()) {
                log.trace("include not in list of OK values: /{}/ /{}/ /{}/", include, productID, modelID);
            }
            return false;
        }

        String exclude = e.getAttributeValue("exclude");
        if (exclude != null) {
            exclude = exclude + "," + extraExclude;
        } else {
            exclude = extraExclude;
        }
        // if there are any exclude clauses, then it cannot match
        if (!exclude.isEmpty() && (isInList(productID, exclude) || isInList(modelID, exclude) || isInList(familyID, exclude))) {
            if (log.isTraceEnabled()) {
                log.trace("exclude match: /{}/ /{}/ /{}/", exclude, productID, modelID);
            }
            return false;
        }

        return true;
    }

    /**
     * @param checkFor see if this value is present within (this value could
     *                 also be a comma-separated list)
     * @param okList   this comma-separated list of items
     *                 (familyID/modelID/productID)
     */
    private static boolean isInList(String checkFor, String okList) {
        String test = "," + okList + ",";
        if (test.contains("," + checkFor + ",")) {
            return true;
        } else if (checkFor != null) {
            String[] testList = checkFor.split(",");
            if (testList.length > 1) {
                for (String item : testList) {
                    if (test.contains("," + item + ",")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Load a VariableTableModel for a given decoder Element, for the purposes of
     * programming.
     *
     * @param decoderElement element which corresponds to the decoder
     * @param variableModel resulting VariableTableModel
     */
    // use the decoder Element from the file to load a VariableTableModel for programming.
    public void loadVariableModel(Element decoderElement,
            VariableTableModel variableModel) {

        nextCvStoreIndex = 0;

        processVariablesElement(decoderElement.getChild("variables"), variableModel, "", "");

        variableModel.configDone();
    }

    int nextCvStoreIndex = 0;

    public void processVariablesElement(Element variablesElement,
            VariableTableModel variableModel, String extraInclude, String extraExclude) {

        // handle include, exclude on this element
        extraInclude = extraInclude
                + (variablesElement.getAttributeValue("include") != null ? "," + variablesElement.getAttributeValue("include") : "");
        extraExclude = extraExclude
                + (variablesElement.getAttributeValue("exclude") != null ? "," + variablesElement.getAttributeValue("exclude") : "");
        log.debug("extraInclude /{}/, extraExclude /{}/", extraInclude, extraExclude);

        // load variables to table
        for (Element e : variablesElement.getChildren("variable")) {
            try {
                // if it's associated with an inconsistent number of functions,
                // skip creating it
                if (getNumFunctions() >= 0 && e.getAttribute("minFn") != null
                        && getNumFunctions() < e.getAttribute("minFn").getIntValue()) {
                    continue;
                }
                // if it's associated with an inconsistent number of outputs,
                // skip creating it
                if (getNumOutputs() >= 0 && e.getAttribute("minOut") != null
                        && getNumOutputs() < Integer.parseInt(e.getAttribute("minOut").getValue())) {
                    continue;
                }
                // if not correct productID, skip
                if (!isProductIDok(e, extraInclude, extraExclude)) {
                    continue;
                }
            } catch (NumberFormatException | DataConversionException ex) {
                log.warn("Problem parsing minFn or minOut in decoder file, variable {} exception", e.getAttribute("item"), ex);
            }
            // load each row
            variableModel.setRow(nextCvStoreIndex++, e, _element == null ? null : this);
        }

        // load constants to table
        for (Element e : variablesElement.getChildren("constant")) {
            try {
                // if it's associated with an inconsistent number of functions,
                // skip creating it
                if (getNumFunctions() >= 0 && e.getAttribute("minFn") != null
                        && getNumFunctions() < e.getAttribute("minFn").getIntValue()) {
                    continue;
                }
                // if it's associated with an inconsistent number of outputs,
                // skip creating it
                if (getNumOutputs() >= 0 && e.getAttribute("minOut") != null
                        && getNumOutputs() < e.getAttribute("minOut").getIntValue()) {
                    continue;
                }
                // if not correct productID, skip
                if (!isProductIDok(e, extraInclude, extraExclude)) {
                    continue;
                }
            } catch (DataConversionException ex) {
                log.warn("Problem parsing minFn or minOut in decoder file, variable {} exception", e.getAttribute("item"), ex);
            }
            // load each row
            variableModel.setConstant(e);
        }

        for (Element e : variablesElement.getChildren("variables")) {
            processVariablesElement(e, variableModel, extraInclude, extraExclude);
        }

    }

    // use the decoder Element from the file to load a VariableTableModel for programming.
    public void loadResetModel(Element decoderElement,
            ResetTableModel resetModel) {
        if (decoderElement.getChild("resets") != null) {
            List<Element> resetList = decoderElement.getChild("resets").getChildren("factReset");
            for (int i = 0; i < resetList.size(); i++) {
                Element e = resetList.get(i);
                resetModel.setRow(i, e, decoderElement.getChild("resets"), _model);
            }
        }
    }

    // process "extraMenu" elements into data model(s)
    public void loadExtraMenuModel(Element decoderElement, ArrayList<ExtraMenuTableModel> extraMenuModelList, JLabel progStatus, Programmer mProgrammer) {
        var menus = decoderElement.getChildren("extraMenu");
        log.trace("loadExtraMenuModel {} {}", menus.size(), extraMenuModelList);
        int i = 0;
        for (var menuElement : menus) {
            if (i >= extraMenuModelList.size() || extraMenuModelList.get(i) == null) {
                log.trace("Add element {} in array of size {}",i,extraMenuModelList.size());
                var model = new ExtraMenuTableModel(progStatus, mProgrammer);
                model.setName(menuElement.getAttributeValue("name","Extra"));
                extraMenuModelList.add(i, model);
            }

            List<Element> itemList = menuElement.getChildren("extraMenuItem");
            var extraMenuModel = extraMenuModelList.get(i);
            for (int j = 0; j < itemList.size(); j++) {
                Element e = itemList.get(j);
                extraMenuModel.setRow(j, e, menuElement, _model);
            }
            i++;
        }
    }

    /**
     * Convert to a canonical text form for ComboBoxes, etc.
     * <p>
     * Must be able to distinguish identical models in different families.
     *
     * @return the title string for the decoder
     */
    public String titleString() {
        return titleString(getModel(), getFamily());
    }

    static public String titleString(String model, String family) {
        return model + " (" + family + ")";
    }

    @SuppressFBWarnings(value = "MS_SHOULD_BE_FINAL") // script access
    static public String fileLocation = "decoders" + File.separator;

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(DecoderFile.class);

}
