/*
   Copyright (c) 2015, The Linux Foundation. All rights reserved.
   Copyright (C) 2020 The OmniRom Project.
   Redistribution and use in source and binary forms, with or without
   modification, are permitted provided that the following conditions are
   met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above
      copyright notice, this list of conditions and the following
      disclaimer in the documentation and/or other materials provided
      with the distribution.
    * Neither the name of The Linux Foundation nor the names of its
      contributors may be used to endorse or promote products derived
      from this software without specific prior written permission.
   THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESS OR IMPLIED
   WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
   MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT
   ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS
   BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
   CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
   SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
   BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
   OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN
   IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#include <stdio.h>
#include <stdlib.h>
#include <fcntl.h>
#include <unistd.h>
#include <android-base/properties.h>
#include <android-base/logging.h>
#include "property_service.h"
#include <sys/resource.h>
#define _REALLY_INCLUDE_SYS__SYSTEM_PROPERTIES_H_
#include <sys/_system_properties.h>

namespace android {
namespace init {

using android::base::GetProperty;
using android::init::property_set;

void property_override(const std::string& name, const std::string& value)
{
    size_t valuelen = value.size();

    prop_info* pi = (prop_info*) __system_property_find(name.c_str());
    if (pi != nullptr) {
        __system_property_update(pi, value.c_str(), valuelen);
    }
    else {
        int rc = __system_property_add(name.c_str(), name.size(), value.c_str(), valuelen);
        if (rc < 0) {
            LOG(ERROR) << "property_set(\"" << name << "\", \"" << value << "\") failed: "
                       << "__system_property_add failed";
        }
    }
}

void property_override_dual(char const prop[], char const system_prop[],
    const std::string& value)
{
    property_override(prop, value);
    property_override(system_prop, value);
}

void vendor_load_properties()
{
    std::string name;
    std::string country;
    std::string build_id;
    std::string build_number;
    std::ostringstream fp;
    std::string fingerprint;
    std::ostringstream desc;
    std::string description;
    std::ostringstream disp;
    std::string display_id;
    std::string region;

    LOG(INFO) << "Starting custom init";

    name = android::base::GetProperty("ro.product.vendor.name", "");
    region = name.substr (0,2);
    country = android::base::GetProperty("ro.boot.country_code", "");

    LOG(INFO) << name;

    // These should be the only things to change for OTA updates
    build_id = "QKQ1.190825.002";
    build_number = "17.0240.2012.65-0";

    // Create the correct stock props based on the above values
    desc << name << "-user 10 " << build_id << " " << build_number << " release-keys";
    description = desc.str();

    disp << build_id << ".WW_Phone-" << build_number;
    display_id = disp.str();

    fp << "asus/" << name << "/ASUS_I001_1:10/" << build_id << "/" << build_number << ":user/release-keys";
    fingerprint = fp.str();

    // These properties are the same for all variants
    property_override("ro.build.description", description);
    property_override("ro.build.display.id", display_id);
    property_override_dual("ro.build.fingerprint", "ro.system.build.fingerprint", fingerprint);
    property_override("ro.product.carrier", "ASUS-ASUS_I001D-WW");

    // Set below properties based on variant name
    if (name == "WW_I001D") {
        // Set properties for IN and US variants
        if (country == "IN") {
            property_override_dual("ro.product.model", "ro.product.system.model", "ASUS_I001DE");
            property_override("ro.config.versatility", "IN");
        } else if (country == "US") {
            property_override("ro.config.versatility", "US");
        } else {
            property_override_dual("ro.product.model", "ro.product.system.model", "ASUS_I001DA");
        }
    } else {
        property_override("ro.product.system.name", name);
        property_override("ro.config.versatility", region);
    }
    if (name == "RU_I001D") {
        property_override_dual("ro.product.model", "ro.product.system.model", "ASUS_I001DB");
    }
    if (name == "EU_I001D") {
        property_override_dual("ro.product.model", "ro.product.system.model", "ASUS_I001DC");
    }
    if (name == "CN_I001D") {
        property_override_dual("ro.product.model", "ro.product.system.model", "ASUS_I001DD");
    }
}
}
}
