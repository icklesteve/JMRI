package jmri;

/**
 * Represent a single visible Variable Light on the physical layout.
 * <p>
 * Each Light may have one or more control mechanisms. Control mechanism types
 * are defined here. If a Light has any controls, the information is contained
 * in LightControl objects, which are referenced via that Light.
 * <p>
 * Lights have a state and an intensity.
 * <p>
 * The intensity of the hardware output is represented by the range from 0.0 to
 * 1.0, with 1.0 being brightest.
 * <p>
 * The primary states are:
 * <ul>
 * <li>ON, corresponding to maximum intensity
 * <li>INTERMEDIATE, some value between maximum and minimum
 * <li>OFF, corresponding to minimum intensity
 * </ul>
 * The underlying hardware may provide just the ON/OFF two levels, or have a
 * semi-continuous intensity setting with some number of steps.
 * <p>
 * The light has a TargetIntensity property which can be set directly. In
 * addition, it has a CurrentIntensity property which may differ from
 * TargetIntensity while the Light is being moved from one intensity to another.
 * <p>
 * Intensity is limited by MinIntensity and MaxIntensity parameters. Setting the
 * state to ON sets the TargetIntensity to MinIntensity, and to OFF sets the
 * TargetIntensity to MaxIntensity. Attempting to directly set the
 * TargetIntensity outside the values of MinIntensity and MaxIntensity
 * (inclusive) will result in the TargetIntensity being set to the relevant
 * limit.
 * <p>
 * Because the actual light hardware has only finite resolution, the intensity
 * value is mapped to the nearest setting. For example, in the special case of a
 * two-state (on/off) Light, setting a TargetIntensity of more than 0.5 will
 * turn the Light on, less than 0.5 will turn the light off.
 * <p>
 * Specific implementations will describe how the settings map to the particular
 * hardware commands.
 * <p>
 * The transition rate is absolute; the intensity changes at a constant rate
 * regardless of whether the change is a big one or a small one.
 *
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
 * @author Dave Duchamp Copyright (C) 2004, 2010
 * @author Ken Cameron Copyright (C) 2008
 * @author Bob Jacobsen Copyright (C) 2008
 */
public interface VariableLight extends Light, AnalogIO {

    /**
     * String constant for the property current intensity.
     */
    String PROPERTY_CURRENT_INTENSITY = "CurrentIntensity";

    /**
     * String constant for the property min intensity.
     */
    String PROPERTY_MIN_INTENSITY = "MinIntensity";

    /**
     * String constant for the property max intensity.
     */
    String PROPERTY_MAX_INTENSITY = "MaxIntensity";

    /** {@inheritDoc} */
    @Override
    @InvokeOnLayoutThread
    default void requestUpdateFromLayout() {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    default boolean isConsistentState() {
        return (getState() == DigitalIO.ON)
                || (getState() == DigitalIO.OFF)
                || (getState() == INTERMEDIATE);
    }
    
    /** {@inheritDoc} */
    @Override
    default boolean isConsistentValue() {
        // Assume that the value is consistent if the state is consistent.
        return isConsistentState();
    }
    
    /**
     * Set the intended new intensity value for the Light. If transitions are in
     * use, they will be applied.
     * <p>
     * Bound property between 0 and 1.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     * <p>
     * Attempting to set a value below the MinIntensity property value will
     * result in MinIntensity being set. Similarly, setting a value above
     * MaxIntensity will result in MaxINtensity being set.
     * <p>
     * Setting the intensity to the value of the MinIntensity property will
     * result in the Light going to the OFF state at the end of the transition.
     * Similarly, setting the intensity to the MaxIntensity value will result in
     * the Light going to the ON state at the end of the transition.
     * <p>
     * All others result in the INTERMEDIATE state.
     * <p>
     * Light implementations with isIntensityVariable false may not have their
     * TargetIntensity set to values between MinIntensity and MaxIntensity,
     * which would result in the INTERMEDIATE state, as that is invalid for
     * them.
     * <p>
     * If a non-zero value is set in the transitionTime property, the state will
     * be one of TRANSITIONTOFULLON, TRANSITIONHIGHER, TRANSITIONLOWER or
     * TRANSITIONTOFULLOFF until the transition is complete.
     *
     * @param intensity the desired brightness
     * @throws IllegalArgumentException when intensity is less than 0.0 or more
     *                                  than 1.0
     * @throws IllegalArgumentException if isIntensityVariable is false and the
     *                                  new value is between MaxIntensity and
     *                                  MinIntensity
     */
    @InvokeOnLayoutThread
    void setTargetIntensity(double intensity);

    /**
     * Get the current intensity value. If the Light is currently transitioning,
     * this may be either an intermediate or final value.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     *
     * @return the current brightness
     */
    double getCurrentIntensity();

    /**
     * Get the target intensity value for the current transition, if any. If the
     * Light is not currently transitioning, this is the current intensity
     * value.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     * <p>
     * Bound property
     *
     * @return the desired brightness
     */
    double getTargetIntensity();

    /**
     * Set the value of the maxIntensity property.
     * <p>
     * Bound property between 0 and 1.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     *
     * @param intensity the maximum brightness
     * @throws IllegalArgumentException when intensity is less than 0.0 or more
     *                                  than 1.0
     * @throws IllegalArgumentException when intensity is not greater than the
     *                                  current value of the minIntensity
     *                                  property
     */
    @InvokeOnLayoutThread
    void setMaxIntensity(double intensity);

    /**
     * Get the current value of the maxIntensity property.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     *
     * @return the maximum brightness
     */
    double getMaxIntensity();

    /**
     * Set the value of the minIntensity property.
     * <p>
     * Bound property between 0 and 1.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     *
     * @param intensity the minimum brightness
     * @throws IllegalArgumentException when intensity is less than 0.0 or more
     *                                  than 1.0
     * @throws IllegalArgumentException when intensity is not less than the
     *                                  current value of the maxIntensity
     *                                  property
     */
    @InvokeOnLayoutThread
    void setMinIntensity(double intensity);

    /**
     * Get the current value of the minIntensity property.
     * <p>
     * A value of 0.0 corresponds to full off, and a value of 1.0 corresponds to
     * full on.
     *
     * @return the minimum brightness
     */
    double getMinIntensity();

    /**
     * Can the Light change its intensity setting slowly?
     * <p>
     * If true, this Light supports a non-zero value of the transitionTime
     * property, which controls how long the Light will take to change from one
     * intensity level to another.
     * <p>
     * Unbound property
     *
     * @return true if brightness can fade between two states; false otherwise
     */
    boolean isTransitionAvailable();

    /**
     * Set the fast-clock duration for a transition from full ON to full OFF or
     * vice-versa.
     * <p>
     * Note there is no guarantee of how this scales when other changes in
     * intensity take place. In particular, some Light implementations will
     * change at a constant fraction per fastclock minute and some will take a
     * fixed duration regardless of the size of the intensity change.
     * <p>
     * Bound property
     * @param minutes time to fade
     * @throws IllegalArgumentException if isTransitionAvailable() is false and
     *                                  minutes is not 0.0
     * @throws IllegalArgumentException if minutes is negative
     */
    @InvokeOnLayoutThread
    void setTransitionTime(double minutes);

    /**
     * Get the number of fastclock minutes taken by a transition from full ON to
     * full OFF or vice versa.
     *
     * @return 0.0 if the output intensity transition is instantaneous
     */
    double getTransitionTime();

    /**
     * Convenience method for checking if the intensity of the light is
     * currently changing due to a transition.
     * <p>
     * Bound property so that listeners can conveniently learn when the
     * transition is over.
     *
     * @return true if light is between two states; false otherwise
     */
    boolean isTransitioning();

}
