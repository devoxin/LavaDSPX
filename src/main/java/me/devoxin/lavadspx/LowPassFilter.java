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

public class LowPassFilter extends GenericPassFilter {
    public LowPassFilter(FloatPcmAudioFilter downstream, int sampleRate, int channelCount, int cutoffFrequency) {
        super(downstream, sampleRate, channelCount, cutoffFrequency);
    }

    public LowPassFilter(FloatPcmAudioFilter downstream, int sampleRate, int channelCount, int cutoffFrequency, float boostFactor) {
        super(downstream, sampleRate, channelCount, cutoffFrequency, boostFactor);
    }

    @Override
    public void setCutoffFrequency(int cutoffFrequency) {
        double omega = 2.0 * Math.PI * cutoffFrequency / sampleRate;
        double cos = Math.cos(omega);
        double sin = Math.sin(omega);
        double alpha = sin / (2.0 * 0.707);
        double scale = (1.0 + alpha);
        b[0] = (1.0 - cos) / 2.0 / scale;
        b[1] = (1.0 - cos) / scale;
        b[2] = (1.0 - cos) / 2.0 / scale;
        a[0] = 1.0;
        a[1] = -2.0 * cos / scale;
        a[2] = (1.0 - alpha) / scale;
    }
}
