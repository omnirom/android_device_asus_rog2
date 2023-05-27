#!/bin/bash
#
# Copyright (C) 2016 The CyanogenMod Project
#           (C) 2017 The LineageOS Project
#           (C) 2018 The Omnirom Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -e

# Required!
DEVICE=rog2
VENDOR=asus

INITIAL_COPYRIGHT_YEAR=2019

# Load extractutils and do some sanity checks
MY_DIR="${BASH_SOURCE%/*}"
if [[ ! -d "$MY_DIR" ]]; then MY_DIR="$PWD"; fi

CM_ROOT="$MY_DIR"/../../..

HELPER="$CM_ROOT"/vendor/omni/build/tools/extract_utils.sh
if [ ! -f "$HELPER" ]; then
    echo "Unable to find helper script at $HELPER"
    exit 1
fi
. "$HELPER"

# Initialize the helper
setup_vendor "$DEVICE" "$VENDOR" "$CM_ROOT"

# Copyright headers and guards
write_headers "rog2"

# The standard blobs
write_makefiles "$MY_DIR"/proprietary-files.txt

write_makefiles "$MY_DIR"/proprietary-files-product.txt

write_makefiles "$MY_DIR"/proprietary-files-vendor.txt

cat << EOF >> "$ANDROIDMK"

EOF

# Qualcomm Izat blobs - we put a conditional around here
# in case the Weekly build will not include this files
printf '\n%s\n' "ifeq (\$(ROM_BUILDTYPE),\$(filter \$(ROM_BUILDTYPE),GAPPS MICROG))" >> "$PRODUCTMK"

write_makefiles "$MY_DIR"/proprietary-files-Izat.txt

# Qualcomm Location blobs - conditional as well
# in order to support OmniRom OS builds
cat << EOF >> "$PRODUCTMK"
endif

EOF

# We are done!
write_footers

