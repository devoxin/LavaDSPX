/*
   Copyright 2023 devoxin

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package me.devoxin.lavadspx;

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter;

public abstract class GenericPassFilter implements FloatPcmAudioFilter {
    public static final float DEFAULT_BOOST_FACTOR = 1.0f;

    private final FloatPcmAudioFilter downstream;
    protected final int sampleRate;
    private float boostFactor;

    protected final double[] b = new double[3];
    protected final double[] a = new double[3];
    protected final float[][] x1;
    protected final float[][] y1;

    /**
     * Instantiate a GenericPassFilter with the provided configuration.
     * @param downstream The next filter in the chain.
     * @param sampleRate The sample rate for the audio to be processed.
     * @param channelCount The channel count of the audio to be processed.
     * @param cutoffFrequency The frequency, in Hz, for the filter to cut off at.
     */
    protected GenericPassFilter(final FloatPcmAudioFilter downstream, final int sampleRate,final int channelCount,
                             final int cutoffFrequency) {
        this(downstream, sampleRate, channelCount, cutoffFrequency, DEFAULT_BOOST_FACTOR);
    }

    /**
     * Instantiate a GenericPassFilter with the provided configuration.
     * @param downstream The next filter in the chain.
     * @param sampleRate The sample rate for the audio to be processed.
     * @param channelCount The channel count of the audio to be processed.
     * @param cutoffFrequency The frequency, in Hz, for the filter to cut off at.
     * @param boostFactor The amount to boost volume by. A value of 1.0 means the volume will be unchanged.
     */
    protected GenericPassFilter(FloatPcmAudioFilter downstream, int sampleRate, int channelCount, int cutoffFrequency, float boostFactor) {
        this.downstream = downstream;
        this.sampleRate = sampleRate;
        this.boostFactor = boostFactor;

        this.x1 = new float[channelCount][2];
        this.y1 = new float[channelCount][2];

        setCutoffFrequency(cutoffFrequency);
    }

    /**
     * The frequency, in Hz, for the filter to cut off at.
     * @param cutoffFrequency The cut-off frequency, in Hz.
     */
    public abstract void setCutoffFrequency(int cutoffFrequency);

    /**
     * Sets the volume boost.
     * @param boostFactor The amount to boost volume by. A value of 1.0 means the volume will be unchanged.
     */
    public void setBoostFactor(float boostFactor) {
        this.boostFactor = boostFactor;
    }

    public float getBoostFactor() {
        return this.boostFactor;
    }

    @Override
    public void process(float[][] input, int offset, int length) throws InterruptedException {
        for (int channel = 0; channel < input.length; channel++) {
            for (int i = offset; i < offset + length; i++) {
                float x0 = input[channel][i];
                float y0 = (float) (b[0] * x0 + b[1] * x1[channel][0] + b[2] * x1[channel][1] - a[1] * y1[channel][0] - a[2] * y1[channel][1]);
                x1[channel][1] = x1[channel][0];
                x1[channel][0] = x0;
                y1[channel][1] = y1[channel][0];
                y1[channel][0] = y0;
                input[channel][i] = y0 * boostFactor;
            }
        }

        downstream.process(input, offset, length);
    }

    @Override
    public void seekPerformed(long requestedTime, long providedTime) {

    }

    @Override
    public void flush() {
        for (int channel = 0; channel < x1.length; channel++) {
            x1[channel][0] = 0.0f;
            x1[channel][1] = 0.0f;
        }

        for (int channel = 0; channel < y1.length; channel++) {
            y1[channel][0] = 0.0f;
            y1[channel][1] = 0.0f;
        }
    }

    @Override
    public void close() {

    }
}
