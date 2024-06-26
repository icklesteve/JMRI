package jmri.jmrit.symbolicprog;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Import CV values from a LokProgrammer CV list file written by the ESU
 * LokProgrammer software.
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under the
 * terms of version 2 of the GNU General Public License as published by the Free
 * Software Foundation. See the "COPYING" file for a copy of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * @author Alex Shepherd Copyright (C) 2003
 * @author Dave Heap Copyright (C) 2014
 */
public class LokProgImporter {

    private static final Logger log = LoggerFactory.getLogger(LokProgImporter.class);
    private static final String DECODER_PREFIX = "Decoder:";
    private static final String CREATED_PREFIX = "Created:";
    private static final String INDEX_PREFIX = "Index:";
    private static final String INDEX_1 = "CV31=";
    private static final String INDEX_1_TERMINATOR = ",";
    private static final String INDEX_2 = "CV32=";
    private static final String INDEX_2_TERMINATOR = ")";
    private static final String CV_PREFIX = "CV ";
    private static final String CV_SEPARATOR = " = ";
    private static final String NOWARN_THESE_CVs = "(1\\.1\\.\\d+|1\\.0\\.2(58|59|60))";

    @SuppressFBWarnings(value = "SBSC_USE_STRINGBUFFER_CONCATENATION",
        justification = "string not kept between iterations, reduces object creation on each iteration")
    public LokProgImporter(File file, CvTableModel cvModel) throws IOException {
        FileReader fileReader = null;
        BufferedReader bufferedReader = null;

        try {
            CvValue cvObject;
            String cvIndex = "";
            String line;
            String name;
            int value;

            fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);

            while ((line = bufferedReader.readLine()) != null) {
                if (line.startsWith(INDEX_PREFIX)) {
                    cvIndex = line.substring(line.indexOf(INDEX_1) + INDEX_1.length(), line.indexOf(INDEX_1_TERMINATOR)) + ".";
                    cvIndex = cvIndex + line.substring(line.indexOf(INDEX_2) + INDEX_2.length(), line.indexOf(INDEX_2_TERMINATOR)) + ".";
                } else if (line.startsWith(CV_PREFIX) && line.regionMatches(6, CV_SEPARATOR, 0, 3)) {
                    name = cvIndex + String.valueOf(Integer.parseInt(line.substring(3, 6)));
                    value = Integer.parseInt(line.substring(9, 12));
                    cvObject = cvModel.allCvMap().get(name);
                    if (cvObject == null) {
                        if (name.matches(NOWARN_THESE_CVs)) {
                            log.debug("Skipping warning for added CV {}, not yet supported by JMRI", name);
                        } else {
                            log.warn("CV {} was in import file, but not defined by the decoder definition", name);
                        }
                        cvModel.addCV(name, false, false, false);
                        cvObject = cvModel.allCvMap().get(name);
                    }
                    log.debug("Settting CV {} to {}", name, value);
                    cvObject.setValue(value);
                } else if (line.startsWith(DECODER_PREFIX) || line.startsWith(CREATED_PREFIX)) {
                    log.info("Imorting CVs from file {}", line);
                }
            }
        } catch (IOException e) {
            log.error("Error reading file", e);
        } finally {
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (fileReader != null) {
                fileReader.close();
            }
        }
        log.info("Completed import from LokProgrammer CV List file");
    }
}
