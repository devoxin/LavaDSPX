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

package me.devoxin.lavafx;

import com.sedmelluq.discord.lavaplayer.filter.FloatPcmAudioFilter;

public class FixedNormalizationFilter implements FloatPcmAudioFilter {
    private final FloatPcmAudioFilter downstream;
    private float maxAmplitude;


    /**
     * Instantiate a FixedNormalizationFilter with the provided configuration.
     * This filter will only normalize audio volume based on the peak amplitude for any given audio frame.
     * That is, the value is effectively reset on each call to process().
     * This may make sudden volume changes more noticeable, compared to the AdaptiveNormalizationFilter.
     *
     * @param downstream The next filter in the chain.
     * @param maxAmplitude A value ranging from 0.0 to 1.0.
     */
    public FixedNormalizationFilter(FloatPcmAudioFilter downstream, float maxAmplitude) {
        this.downstream = downstream;
        setMaxAmplitude(maxAmplitude);
    }

    /**
     * Sets the maximum allowed amplitude.
     * @param maxAmplitude The maximum amplitude, valid range is 0.0-1.0f.
     */
    public void setMaxAmplitude(float maxAmplitude) {
        this.maxAmplitude = Math.max(0.0f, Math.min(1.0f, maxAmplitude));
    }

    @Override
    public void process(float[][] input, int offset, int length) throws InterruptedException {
        for (int channel = 0; channel < input.length; channel++) {
            float peakAmplitude = 0.0f;

            for (int i = offset; i < offset + length; i++) {
                peakAmplitude = Math.max(peakAmplitude, Math.abs(input[channel][i]));
            }

            if (peakAmplitude > maxAmplitude) {
                for (int j = offset; j < offset + length; j++) {
                    input[channel][j] /= peakAmplitude / maxAmplitude;
                }
            }
        }

        downstream.process(input, offset, length);
    }

    @Override
    public void seekPerformed(long requestedTime, long providedTime) {

    }

    @Override
    public void flush() {

    }

    @Override
    public void close() {

    }
}
