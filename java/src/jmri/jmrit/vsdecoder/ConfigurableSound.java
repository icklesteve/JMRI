package jmri.jmrit.vsdecoder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import jmri.util.PhysicalLocation;
import org.jdom2.Element;

/**
 * Configurable Sound initial version.
 *
 * <hr>
 * This file is part of JMRI.
 * <p>
 * JMRI is free software; you can redistribute it and/or modify it under
 * the terms of version 2 of the GNU General Public License as published
 * by the Free Software Foundation. See the "COPYING" file for a copy
 * of this license.
 * <p>
 * JMRI is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * for more details.
 *
 * @author Mark Underwood Copyright (C) 2011
 * @author Klaus Killinger Copyright (C) 2025
 */
class ConfigurableSound extends VSDSound {

    protected String start_file;
    protected String mid_file;
    protected String end_file;
    protected String short_file;

    SoundBite start_sound;
    SoundBite mid_sound;
    SoundBite end_sound;
    SoundBite short_sound;

    boolean initialized = false;

    protected boolean use_start_sound = false;
    protected boolean use_mid_sound = false;
    protected boolean use_end_sound = false;
    protected boolean use_short_sound = false;

    private float rd;

    public ConfigurableSound(String name) {
        super(name);
    }

    public boolean init() {
        return this.init(null);
    }

    boolean init(VSDFile vf) {
        if (!initialized) {
            if (use_start_sound) {
                start_sound = new SoundBite(vf, start_file, name + "_Start", name + "_Start");
                if (start_sound.isInitialized()) {
                    start_sound.setLooped(false);
                    start_sound.setReferenceDistance(rd);
                    start_sound.setGain(gain);
                } else {
                    use_start_sound = false;
                }
            }
            if (use_mid_sound) {
                mid_sound = new SoundBite(vf, mid_file, name + "_Mid", name + "_Mid");
                if (mid_sound.isInitialized()) {
                    mid_sound.setLooped(false);
                    mid_sound.setReferenceDistance(rd);
                    mid_sound.setGain(gain);
                } else {
                    use_mid_sound = false;
                }
            }
            if (use_end_sound) {
                end_sound = new SoundBite(vf, end_file, name + "_End", name + "_End");
                if (end_sound.isInitialized()) {
                    end_sound.setLooped(false);
                    end_sound.setReferenceDistance(rd);
                    end_sound.setGain(gain);
                } else {
                    use_end_sound = false;
                }
            }
            if (use_short_sound) {
                short_sound = new SoundBite(vf, short_file, name + "_Short", name + "_Short");
                if (short_sound.isInitialized()) {
                    short_sound.setLooped(false);
                    short_sound.setReferenceDistance(rd);
                    short_sound.setGain(gain);
                } else {
                    use_short_sound = false;
                }
            }
        }
        return true;
    }

    @Override
    public void play() {
        if (use_short_sound) {
            short_sound.play();
        } else {
            if (use_start_sound) {
                t = newTimer(start_sound.getLengthAsInt(), false, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        handleTimerPop(e);
                    }
                });
                start_sound.play();
                if (use_mid_sound) {
                    t.start();
                }
            } else if (use_mid_sound) {
                mid_sound.setLooped(true);
                mid_sound.play();
            }
        }
    }

    @Override
    public void loop() {
        if (use_start_sound) {
            start_sound.setLooped(false);
            start_sound.play();
            // The newTimer method in the super class makes sure that the delay value is positive
            t = newTimer(start_sound.getLengthAsInt() - 100, false, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    handleTimerPop(e);
                }
            });
            t.setRepeats(false); // timer pop only once to trigger the sustain sound.
            t.start();
        } else if (use_mid_sound) {
            mid_sound.setLooped(true);
            mid_sound.play();
        }
    }

    // Catch the timer pop after the start sound is played and trigger the (looped) sustain sound.
    protected void handleTimerPop(ActionEvent e) {
        log.debug("Received timer pop after start sound played.");
        //TODO: Need to validate that this is the timer pop
        if (use_mid_sound) {
            mid_sound.setLooped(true);
            mid_sound.play();
        }
        t.stop();
    }

    @Override
    public void stop() {
        log.debug("Stopping");
        // make sure the start sound is killed
        if (use_start_sound) {
            start_sound.stop();
        }

        // If the mid sound is used, turn off the looping.
        // this will allow it to naturally die.
        if (use_mid_sound) {
            mid_sound.setLooped(false);
            mid_sound.fadeOut();
        }

        // If the timer is running, stop it.
        if (t != null) {
            t.stop();
        }

        // If we're using the end sound, stop the mid sound
        // and play the end sound.
        if (use_end_sound) {
            if (use_mid_sound) {
                mid_sound.stop();
            }
            end_sound.setLooped(false);
            end_sound.play();
        }
    }

    @Override
    public void fadeIn() {
        this.play();
    }

    @Override
    public void fadeOut() {
        this.stop();
    }

    @Override
    public void shutdown() {
        if (use_start_sound) {
            start_sound.stop();
        }
        if (use_mid_sound) {
            mid_sound.stop();
        }
        if (use_end_sound) {
            end_sound.stop();
        }
        if (use_short_sound) {
            short_sound.stop();
        }
    }

    @Override
    public void mute(boolean m) {
        if (use_start_sound) {
            start_sound.mute(m);
        }
        if (use_mid_sound) {
            mid_sound.mute(m);
        }
        if (use_end_sound) {
            end_sound.mute(m);
        }
        if (use_short_sound) {
            short_sound.mute(m);
        }
    }

    @Override
    public void setVolume(float v) {
        if (use_start_sound) {
            start_sound.setVolume(v);
        }
        if (use_mid_sound) {
            mid_sound.setVolume(v);
        }
        if (use_end_sound) {
            end_sound.setVolume(v);
        }
        if (use_short_sound) {
            short_sound.setVolume(v);
        }
    }

    @Override
    public void setPosition(PhysicalLocation p) {
        super.setPosition(p);
        if (use_start_sound) {
            start_sound.setPosition(p);
            setTunnelEffect(start_sound);
        }
        if (use_mid_sound) {
            mid_sound.setPosition(p);
            setTunnelEffect(mid_sound);
        }
        if (use_end_sound) {
            end_sound.setPosition(p);
            setTunnelEffect(end_sound);
        }
        if (use_short_sound) {
            short_sound.setPosition(p);
            setTunnelEffect(short_sound);
        }
    }

    private void setTunnelEffect(SoundBite sb) {
        if (this.getTunnel()) {
            sb.attachSourcesToEffects();
        } else {
            sb.detachSourcesToEffects();
        }
    }

    @Override
    public Element getXml() {
        Element me = new Element("sound");

        if (log.isDebugEnabled()) {
            log.debug("Configurable Sound:");
            log.debug("  name: {}", this.getName());
            log.debug("  start_file: {}", start_file);
            log.debug("  mid_file: {}", mid_file);
            log.debug("  end_file: {}", end_file);
            log.debug("  short_file: {}", short_file);
            log.debug("  use_start_file: {}", start_file);
        }

        me.setAttribute("name", this.getName());
        me.setAttribute("type", "configurable");
        if (use_start_sound) {
            me.addContent(new Element("start-file").addContent(start_file));
        }
        if (use_mid_sound) {
            me.addContent(new Element("mid-file").addContent(mid_file));
        }
        if (use_end_sound) {
            me.addContent(new Element("end-file").addContent(end_file));
        }
        if (use_short_sound) {
            me.addContent(new Element("short-file").addContent(short_file));
        }

        return me;
    }

    @Override
    public void setXml(Element e) {
        this.setXml(e, null);
    }

    public void setXml(Element e, VSDFile vf) {
        log.debug("ConfigurableSound: {}", e.getAttributeValue("name"));
        if (((start_file = e.getChildText("start-file")) != null) && (!start_file.isEmpty())) {
            use_start_sound = true;
        } else {
            use_start_sound = false;
        }
        if (((mid_file = e.getChildText("mid-file")) != null) && (!mid_file.isEmpty())) {
            use_mid_sound = true;
        } else {
            use_mid_sound = false;
        }
        if (((end_file = e.getChildText("end-file")) != null) && (!end_file.isEmpty())) {
            use_end_sound = true;
        } else {
            use_end_sound = false;
        }
        if (((short_file = e.getChildText("short-file")) != null) && (!short_file.isEmpty())) {
            use_short_sound = true;
        } else {
            use_short_sound = false;
        }

        String g = e.getChildText("gain");
        if ((g != null) && (!g.isEmpty())) {
            gain = Float.parseFloat(g);
        } else {
            gain = default_gain;
        }

        String rds = e.getChildText("reference-distance");
        if ((rds != null) && (!rds.isEmpty())) {
            rd = Float.parseFloat(rds);
        } else {
            rd = default_reference_distance;
        }

        /*
         log.debug("Use:  start: {}, mid: {}, end: {}, short: {}", use_start_sound,
         use_mid_sound, use_end_sound, use_short_sound);
         */
        // Reboot the sound
        initialized = false;
        this.init(vf);
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConfigurableSound.class);

}
