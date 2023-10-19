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

public class AdaptiveNormalizationFilter implements FloatPcmAudioFilter {
    private final FloatPcmAudioFilter downstream;
    private float maxAmplitude;

    private float peakAmplitude = 0.0f;

    /**
     * Instantiate a FixedNormalizationFilter with the provided configuration.
     * This filter will normalize audio volume progressively as it processes more audio samples.
     * This means that the louder a song is, the more the volume is adjusted relative to the rest of the audio,
     * which has the potential to reduce the audible volume changing effect that you might find with the
     * FixedNormalizationFilter.
     *
     * @param downstream The next filter in the chain.
     * @param maxAmplitude A value ranging from 0.0 to 1.0.
     */
    public AdaptiveNormalizationFilter(FloatPcmAudioFilter downstream, float maxAmplitude) {
        this.downstream = downstream;
        this.maxAmplitude = maxAmplitude;
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
