/**
 *  Auto Dim lights after sunset
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
    name: "Auto Dim lights after sunset",
    namespace: "CoopeyB",
    author: "Cooper Bills",
    description: "Any selected light will slowly dim after sunset.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Lights to dim") {
        input "switches", "capability.switchLevel", required: true, multiple: true
    }
    section("Timing") {
        input "tickInMinutes", "enum", required: true, title: "Every X Minutes", options: ["1", "5", "10", "15", "30"]
        input "brightnessDelta", "number", required: true, title: "Reduce Brightness By %", range: "1..100"
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
	// TODO: subscribe to attributes, devices, locations, etc.
    log.debug "Tick Time: ${tickInMinutes}"
    if (tickInMinutes.equals("1")) {
    	log.debug "Setting up tick for 1 minute"
    	runEvery1Minute(onTick)
    } else if (tickInMinutes.equals("5")) {
    	log.debug "Setting up tick for 5 minutes"
    	runEvery5Minutes(onTick)
    } else if (tickInMinutes.equals("10")) {
    	log.debug "Setting up tick for 10 minutes"
    	runEvery10Minutes(onTick)
    } else if (tickInMinutes.equals("15")) {
    	log.debug "Setting up tick for 15 minutes"
    	runEvery15Minutes(onTick)
    } else if (tickInMinutes.equals("30")) {
    	log.debug "Setting up tick for 30 minutes"
    	runEvery30Minutes(onTick)
    } else {
    	log.debug "Unrecognized tick time, app will not work"
    }
}

def onTick() {
	log.debug "Tick!"
    def sunriseAndSunset = getSunriseAndSunset()
    log.debug "sunrise: ${sunriseAndSunset.sunrise}"
    log.debug "sunset: ${sunriseAndSunset.sunset}"
    log.debug "current time: ${new Date()}"
    def isDaytime = timeOfDayIsBetween(sunriseAndSunset.sunrise, sunriseAndSunset.sunset, new Date(), location.getTimeZone())
    if (isDaytime) {
    	log.debug "detected sun is up, not dimming lights"
    } else {
	    doDimming()
    }
}

def doDimming() {
	log.debug "switches: ${switches}"
    switches.each {
        if (it.currentSwitch == "off") {
        	log.debug "${it} is off, skipping dimming procedure"
        } else if (it.currentLevel <= 1) {
        	log.debug "${it} is already at max dimness, skipping dimming procedure"
        } else {
            def newLevel = Math.max(1, (it.currentLevel - brightnessDelta).toInteger())
        	log.debug "dimming ${it} from ${it.currentLevel} to ${newLevel}"
            it.setLevel(newLevel)
        }
    }
}