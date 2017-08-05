/**
 *  Dimmer After Sunset
 *
 *  Copyright 2017 Cooper Bills
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 *
 */
definition(
    name: "Auto-on light to have diffrent brightness after sunset",
    namespace: "CoopeyB",
    author: "Cooper Bills",
    description: "Trigger turning on lights by a contact sensor/door.  Light brightness can be configured to be different for when the sun is up vs. down.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Turn on this") {
        input "theswitch", "capability.switchLevel", required: true, title: "Dimmable Light"
    }
    section("At Brightness") {
        input "dayBrightness", "number", required: true, title: "During Day", range: "0..100"
        input "nightBrightness", "number", required: true, title: "During Night", range: "0..100"
    }
    section("When this") {
        input "thedoor", "capability.contactSensor", required: true, title: "Sensor"
    }
    section("is") {
        input "openOrClosed", "enum", required: true, title: "Open/Closed", options: ["open", "closed"]
    }
}

def installed() {
	log.debug "Installed with settings: ${settings}"

	initialize()
}

def updated() {
	log.debug "Updated with settings: ${settings}"

	unsubscribe()
	initialize()
}

def initialize() {
    subscribe(thedoor, "contact.${openOrClosed}", closeDetectedHandler)
}

def closeDetectedHandler(evt) {
    log.debug "closeDetectedHandler called: $evt"
    def sunriseAndSunset = getSunriseAndSunset()
    log.debug "sunrise: ${sunriseAndSunset.sunrise}"
    log.debug "sunset: ${sunriseAndSunset.sunset}"
    log.debug "current time: ${new Date()}"
    def isDaytime = timeOfDayIsBetween(sunriseAndSunset.sunrise, sunriseAndSunset.sunset, new Date(), location.getTimeZone())
    if (isDaytime) {
    	log.debug "Event detected during daytime, setting daytime brightness"
    	theswitch.setLevel(dayBrightness)
    } else {
    	log.debug "Event detected during nighttime, setting nighttime brightness"
    	theswitch.setLevel(nightBrightness)
    }
}
