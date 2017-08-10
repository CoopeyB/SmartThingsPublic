/**
 *  Battery Notifier
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
    name: "Battery Notifier",
    namespace: "CoopeyB",
    author: "Cooper Bills",
    description: "Send notification if any selected device&#39;s battery dips below a specified value (checked once per day at configured time).",
    category: "Convenience",
    iconUrl: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience.png",
    iconX2Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png",
    iconX3Url: "https://s3.amazonaws.com/smartapp-icons/Convenience/Cat-Convenience@2x.png")


preferences {
    section("If any of these devices") {
        input "devices", "capability.battery", required: true, multiple: true
    }
    section("Dip below") {
        input "threshold", "number", required: true, title: "Battery %", range: "1..100"
    }
    section("At") {
    	input "theTime", "time", required: true, title: "Time of day"
    }
    section("Send notifications") {
    	input "sendPush", "bool", required: false, title: "Push Notification"
        input "phone", "phone", required: false, title: "SMS number (optional)"
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
    schedule(theTime, checkBatteries)
}

def checkBatteries() {
	devices.each {
    	log.debug("${it} is at battery ${it.currentBattery}%")
        if (it.currentBattery <= threshold) {
        	sendNotification("${it} battery level is ${it.currentBattery}%");
        }
    }
}

def sendNotification(message) {
    if (sendPush) {
        sendPush(message)
    }
    if (phone) {
        sendSms(phone, message)
    }
}