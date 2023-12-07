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

public class EchoFilter implements FloatPcmAudioFilter {
    private final FloatPcmAudioFilter downstream;
    private final EchoConverter[] converters;

    public EchoFilter(final FloatPcmAudioFilter downstream, final int sampleRate, final int channelCount) {
        this(downstream, sampleRate, channelCount, 0.0f);
    }

    /**
     * @param downstream The next filter in the chain.
     * @param sampleRate The sample rate for the audio to be processed.
     * @param channelCount The number of channels.
     * @param echoLength The echo length, in seconds.
     */
    public EchoFilter(final FloatPcmAudioFilter downstream,
                      final int sampleRate,
                      final int channelCount,
                      final float echoLength) {
        this(downstream, sampleRate, channelCount, echoLength, 0.0f);
    }

    /**
     * @param downstream The next filter in the chain.
     * @param sampleRate The sample rate for the audio to be processed.
     * @param channelCount The number of channels.
     * @param echoLength The echo length, in seconds.
     * @param decay The echo decay rate, between 0 and 1. A value of 1 means no decay, and a value of 0
     *              means immediate decay (no echo effect).
     */
    public EchoFilter(final FloatPcmAudioFilter downstream,
                      final int sampleRate,
                      final int channelCount,
                      final float echoLength,
                      final float decay) {
        this.downstream = downstream;
        this.converters = new EchoConverter[channelCount];

        for (int c = 0; c < channelCount; c++) {
            converters[c] = new EchoConverter(sampleRate, echoLength, decay);
        }
    }

    public void setEchoLength(final float seconds) {
        for (final EchoConverter converter : converters) {
            converter.setEchoLength(seconds);
        }
    }

    /**
     * @param decay The echo decay rate, between 0 and 1. A value of 1 means no decay,
     *              and a value of 0 means immediate decay (no echo effect).
     */
    public void setEchoDecay(final float decay) {
        for (final EchoConverter converter : converters) {
            converter.setEchoDecay(decay);
        }
    }

    @Override
    public void process(float[][] input, int offset, int length) throws InterruptedException {
        for (int c = 0; c < input.length; c++) {
            converters[c].process(input[c], offset, length);
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
