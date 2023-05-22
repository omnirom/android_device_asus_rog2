/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

#define LOG_TAG "VibratorService"

#include <log/log.h>

#include <android-base/logging.h>
#include <hardware/hardware.h>
#include <hardware/vibrator.h>
#include <cutils/properties.h>

#include "Vibrator.h"

#include <cinttypes>
#include <cmath>
#include <iostream>
#include <fstream>

static constexpr char ACTIVATE_PATH[] = "/sys/class/leds/vibrator/activate";
static constexpr char DURATION_PATH[] = "/sys/class/leds/vibrator/duration";
static constexpr char STATE_PATH[] = "/sys/class/leds/vibrator/state";
static constexpr char EFFECT_INDEX_PATH[] = "/sys/class/leds/vibrator/lp_trigger_effect";

namespace android {
namespace hardware {
namespace vibrator {
namespace V1_2 {
namespace implementation {

using Status = ::android::hardware::vibrator::V1_0::Status;
using EffectStrength = ::android::hardware::vibrator::V1_0::EffectStrength;

static constexpr uint32_t WAVEFORM_TICK_EFFECT_MS = 30;

static constexpr uint32_t WAVEFORM_CLICK_EFFECT_MS = 20;

static constexpr uint32_t WAVEFORM_HEAVY_CLICK_EFFECT_MS = 35;

static constexpr uint32_t WAVEFORM_DOUBLE_CLICK_EFFECT_MS = 35;

static constexpr uint32_t WAVEFORM_POP_EFFECT_MS = 20;

static constexpr uint32_t WAVEFORM_THUD_EFFECT_MS = 25;

static constexpr uint32_t WAVEFORM_RINGTONE_EFFECT_MS = 30000;

/*
 * Write value to path and close file.
 */
template <typename T>
static void set(const std::string& path, const T& value) {
    std::ofstream file(path);

    if (!file.is_open()) {
        LOG(ERROR) << "Unable to open: " << path << " (" <<  strerror(errno) << ")";
        return;
    }

    file << value;
}

Vibrator::Vibrator() {
    set(EFFECT_INDEX_PATH, 1);
}

// Methods from ::android::hardware::vibrator::V1_1::IVibrator follow.
Return<Status> Vibrator::on(uint32_t timeoutMs) {
    set(STATE_PATH, 1);
    set(DURATION_PATH, timeoutMs);
    set(ACTIVATE_PATH, 1);

    return Status::OK;
}

Return<Status> Vibrator::off()  {
    return Status::OK;
}

Return<bool> Vibrator::supportsAmplitudeControl() {
    return false;
}

Return<Status> Vibrator::setAmplitude(uint8_t) {
    return Status::UNSUPPORTED_OPERATION;
}

Return<void> Vibrator::perform(V1_0::Effect effect, EffectStrength strength,
        perform_cb _hidl_cb) {
    return performEffect(static_cast<Effect>(effect), strength, _hidl_cb);
}

Return<void> Vibrator::perform_1_1(V1_1::Effect_1_1 effect, EffectStrength strength,
        perform_cb _hidl_cb) {
    return performEffect(static_cast<Effect>(effect), strength, _hidl_cb);
}

Return<void> Vibrator::perform_1_2(Effect effect, EffectStrength strength,
        perform_cb _hidl_cb) {
    return performEffect(static_cast<Effect>(effect), strength, _hidl_cb);
}

Return<void> Vibrator::performEffect(Effect effect, EffectStrength,
        perform_cb _hidl_cb) {
    set(ACTIVATE_PATH, 0);
    Status status = Status::OK;
    uint32_t timeMs;

    switch (effect) {
    case Effect::TICK:
        timeMs = WAVEFORM_TICK_EFFECT_MS;
        break;
    case Effect::CLICK:
        timeMs = WAVEFORM_CLICK_EFFECT_MS;
        break;
    case Effect::HEAVY_CLICK:
        timeMs = WAVEFORM_HEAVY_CLICK_EFFECT_MS;
        break;
    case Effect::DOUBLE_CLICK:
        timeMs = WAVEFORM_DOUBLE_CLICK_EFFECT_MS;
        break;
    case Effect::POP:
        timeMs = WAVEFORM_POP_EFFECT_MS;
        break;
    case Effect::THUD:
        timeMs = WAVEFORM_THUD_EFFECT_MS;
        break;
    case Effect::RINGTONE_1:
        timeMs = WAVEFORM_RINGTONE_EFFECT_MS;
        break;
    case Effect::RINGTONE_2:
        timeMs = WAVEFORM_RINGTONE_EFFECT_MS;
        break;
    case Effect::RINGTONE_3:
        timeMs = WAVEFORM_RINGTONE_EFFECT_MS;
        break;
    case Effect::RINGTONE_4:
        timeMs = WAVEFORM_RINGTONE_EFFECT_MS;
        break;
    case Effect::RINGTONE_5:
        timeMs = WAVEFORM_RINGTONE_EFFECT_MS;
        break;
    case Effect::RINGTONE_6:
        timeMs = WAVEFORM_RINGTONE_EFFECT_MS;
        break;
    case Effect::RINGTONE_7:
        timeMs = WAVEFORM_RINGTONE_EFFECT_MS;
        break;
    case Effect::RINGTONE_8:
        timeMs = WAVEFORM_RINGTONE_EFFECT_MS;
        break;
    case Effect::RINGTONE_9:
        timeMs = WAVEFORM_RINGTONE_EFFECT_MS;
        break;
    case Effect::RINGTONE_10:
        timeMs = WAVEFORM_RINGTONE_EFFECT_MS;
        break;
    case Effect::RINGTONE_11:
        timeMs = WAVEFORM_RINGTONE_EFFECT_MS;
        break;
    case Effect::RINGTONE_12:
        timeMs = WAVEFORM_RINGTONE_EFFECT_MS;
        break;
    case Effect::RINGTONE_13:
        timeMs = WAVEFORM_RINGTONE_EFFECT_MS;
        break;
    case Effect::RINGTONE_14:
        timeMs = WAVEFORM_RINGTONE_EFFECT_MS;
        break;
    case Effect::RINGTONE_15:
        timeMs = WAVEFORM_RINGTONE_EFFECT_MS;
        break;
    default:
        _hidl_cb(Status::UNSUPPORTED_OPERATION, 0);
        return Void();
    }

    on(timeMs);
    _hidl_cb(status, timeMs);

    return Void();
}


} // namespace implementation
}  // namespace V1_2
}  // namespace vibrator
}  // namespace hardware
}  // namespace android