/**
 *  Turn Off After Delay
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
    name: "Turn Off After Delay",
    namespace: "CoopeyB",
    author: "Cooper Bills",
    description: "Turn off a light after some time after a given contact sensor closes.",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("Turn off this") {
        input "theswitch", "capability.switch", required: true, title: "Light"
    }
    section("After delay") {
        input "thedelay", "number", required: true, title: "Seconds"
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
    runIn(thedelay, checkDoor)
}

def checkDoor() {
    log.debug "In checkDoor scheduled method"

    // get the current state object
    def doorState = thedoor.currentState("contact")

    if (doorState.value == openOrClosed) {
        // get the time elapsed between now and when the sensor reported closed
        def elapsed = now() - doorState.date.time

        // elapsed time is in milliseconds, so the threshold must be converted to milliseconds too
        def threshold = 1000 * (thedelay - 1)

        if (elapsed >= threshold) {
            log.debug "Door has stayed ${openOrClosed} long enough since last check ($elapsed ms):  turning switch off"
            theswitch.off()
        } else {
            log.debug "Door has not stayed in state long enough since last check ($elapsed ms):  doing nothing"
        }
    } else {
        log.debug "Door is not in desired state (${openOrClosed}), do nothing and wait for desired state (${openOrClosed})"
    }
}