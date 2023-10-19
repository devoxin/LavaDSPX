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
    private final int sampleRate;
    private float echoLength;
    private float decay;

    private float[] echoBuffer;
    private int position;

    public EchoFilter(final FloatPcmAudioFilter downstream, final int sampleRate) {
        this(downstream, sampleRate, 0.0f);
    }

    /**
     * @param downstream The next filter in the chain.
     * @param sampleRate The sample rate for the audio to be processed.
     * @param echoLength The echo length, in seconds.
     */
    public EchoFilter(final FloatPcmAudioFilter downstream, final int sampleRate, final float echoLength) {
        this(downstream, sampleRate, echoLength, 0.0f);
    }

    /**
     * @param downstream The next filter in the chain.
     * @param sampleRate The sample rate for the audio to be processed.
     * @param echoLength The echo length, in seconds.
     * @param decay The echo decay rate, between 0 and 1. A value of 1 means no decay, and a value of 0
     *              means immediate decay (no echo effect).
     */
    public EchoFilter(final FloatPcmAudioFilter downstream, final int sampleRate, final float echoLength,
                      final float decay) {
        this.downstream = downstream;
        this.sampleRate = sampleRate;
        this.echoLength = echoLength;
        this.decay = decay;

        applyNewEchoBuffer();
    }

    public void setEchoLength(final float seconds) {
        this.echoLength = seconds;
    }

    /**
     * @param decay The echo decay rate, between 0 and 1. A value of 1 means no decay,
     *              and a value of 0 means immediate decay (no echo effect).
     */
    public void setEchoDecay(final float decay) {
        this.decay = decay;
    }

    private void applyNewEchoBuffer() {
        if (echoLength == -1) {
            return;
        }

        float[] newEchoBuffer = new float[(int) (sampleRate * echoLength)];

        if (echoBuffer != null) {
            for (int i = 0; i < newEchoBuffer.length; i++) {
                checkPositionAndRewind();
                newEchoBuffer[i] = echoBuffer[position++];
            }
        }

        this.echoBuffer = newEchoBuffer;
        echoLength = -1;
    }

    @Override
    public void process(float[][] input, int offset, int length) throws InterruptedException {
        if (echoBuffer != null) {
            for (float[] floats : input) {
                processChannel(floats, offset, length);
            }
        }

        applyNewEchoBuffer();
        downstream.process(input, offset, length);
    }

    private void processChannel(float[] input, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            checkPositionAndRewind();

            float current = input[i] + echoBuffer[position] * decay;
            echoBuffer[position++] = input[i] = current;
        }
    }

    private void checkPositionAndRewind() {
        if (position >= echoBuffer.length) {
            position = 0;
        }
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
