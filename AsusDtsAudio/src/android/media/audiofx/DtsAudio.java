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

package android.media.audiofx;

import java.util.UUID;

public class DtsAudio extends AudioEffect {

    public DtsAudio(UUID type, UUID uuid, int priority, int audioSession) throws IllegalStateException,
                IllegalArgumentException, UnsupportedOperationException, RuntimeException {
        super(type, uuid, priority, audioSession);
    }

    @Override
    public int setEnabled(boolean enabled) throws IllegalStateException {
        return super.setEnabled(enabled);
    }

    @Override
    public boolean getEnabled() throws IllegalStateException {
        return super.getEnabled();
    }

    @Override
    public int setParameter(byte[] param, byte[] value) throws IllegalStateException {
        return super.setParameter(param, value);
    }

    @Override
    public int getParameter(byte[] param, byte[] value) throws IllegalStateException {
        return super.getParameter(param, value);
    }
}
