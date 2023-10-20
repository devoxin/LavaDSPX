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

public class NormalizationFilter implements FloatPcmAudioFilter {
    private final FloatPcmAudioFilter downstream;
    private float maxAmplitude;
    private boolean adaptive;

    private float peakAmplitude = 0.0f;


    public NormalizationFilter(FloatPcmAudioFilter downstream, float maxAmplitude) {
        this(downstream, maxAmplitude, true);
    }

    /**
     * A filter that will attenuate audio peaking, so that the difference between the loudest and softest parts of a track
     * is narrowed.
     *
     * @param downstream The next filter in the chain.
     * @param maxAmplitude A value ranging from 0.0 to 1.0.
     * @param adaptive Whether peak amplitude values should persist for the lifetime of this filter.
     *                 Setting this to true means that peak amplitude is more accurate over the duration
     *                 of a track, however it could take a while before the peak amplitude reaches its highest value.
     *                 Setting this to false means that peak amplitude is only calculated on a per-frame basis,
     *                 but may cause more noticeable volume changes.
     */
    public NormalizationFilter(FloatPcmAudioFilter downstream, float maxAmplitude, boolean adaptive) {
        this.downstream = downstream;
        this.adaptive = adaptive;
        setMaxAmplitude(maxAmplitude);
    }

    /**
     * Sets the maximum allowed amplitude.
     * @param maxAmplitude The maximum amplitude, valid range is 0.0-1.0f.
     */
    public void setMaxAmplitude(float maxAmplitude) {
        this.maxAmplitude = Math.max(0.0f, Math.min(1.0f, maxAmplitude));
    }

    public float getMaxAmplitude() {
        return this.maxAmplitude;
    }

    @Override
    public void process(float[][] input, int offset, int length) throws InterruptedException {
        if (!adaptive) {
            peakAmplitude = 0.0f;
        }

        for (int channel = 0; channel < input.length; channel++) {
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
        peakAmplitude = 0.0f;
    }

    @Override
    public void close() {

    }
}
