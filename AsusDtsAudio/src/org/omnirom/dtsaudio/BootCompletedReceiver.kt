/*
* Copyright (C) 2023 The OmniROM Project
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program. If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.omnirom.dtsaudio

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.audiofx.AudioEffect
import android.util.Log

import java.util.UUID

class BootCompletedReceiver : BroadcastReceiver() {
    private val audioEffect = AudioEffect(
        EFFECT_TYPE_DTS_AUDIO, EFFECT_UUID_IMPLEMENTATION_DTS_AUDIO, 0, 0
    )

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Starting")
        audioEffect.enabled = true
    }

    companion object {
        private const val TAG = "AsusDtsAudio"

        private val EFFECT_TYPE_DTS_AUDIO = UUID.fromString("1d4033c0-8557-11df-9f2d-0002a5d5c51b")
        private val EFFECT_UUID_IMPLEMENTATION_DTS_AUDIO = UUID.fromString("146edfc0-7ed2-11e4-80eb-0002a5d5c51b")
    }
}
