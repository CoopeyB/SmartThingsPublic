/**
 *  Alarm Party Mode
 *
 *  Copyright 2017 COOPER BILLS
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
    name: "Alarm Party Mode",
    namespace: "CoopeyB",
    author: "Cooper Bills",
    description: '''Repeatedly switch all given lights when another \
switch (virtual or not) is on; Leave all on when the switch is turned off. \
Intended use: create a virtual switch controlled by external alarm system \
(e.g. Abode via IFTTT), that's on when the alarm is triggered, off when disarmed. \
Use it and this app to flash physical lights during an alarm state.''',
    category: "Safety & Security",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Parent Switch") {
        input "parentSwitch", "capability.switch", required: true, title: "The switch to control the party"
    }
    section("Timing (in seconds)") {
        input "onTime", "number", required: true, title: "Time to be on (e.g. 5 sec)", range: "1..120"
        input "offTime", "number", required: true, title: "Time to be off (e.g. 2 sec)", range: "1..120"
    }
    section("Lights for party mode") {
        input "switches", "capability.switch", required: true, multiple: true
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
    subscribe(parentSwitch, "switch", onSwitchEvent)
}

def onSwitchEvent(event) {
	turnOn()
}

def turnOn() {
    def parentSwitchState = parentSwitch.currentState("switch")
    log.debug "In turnOn method"
    switches.on()
    if (parentSwitchState.value == "on") {
		runIn(onTime, turnOff)
    }
}

def turnOff() {
    def parentSwitchState = parentSwitch.currentState("switch")
    log.debug "In turnOff method"
    if (parentSwitchState.value == "on") {
    	switches.off()
		runIn(offTime, turnOn)
    }
}
