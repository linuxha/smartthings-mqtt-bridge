/**
 *  MQTT Bridge
 *
 * 	Authors
 *   - st.john.johnson@gmail.com
 *   - jeremiah.wuenschel@gmail.com
 *   - ncherry@linuxha.com
 *
 *  Copyright 2016
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License. You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 *  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
 *  for the specific language governing permissions and limitations under the License.
 */
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

definition(
    name:        "MQTT Bridge",
    namespace:   "linuxha",
    author:      "St. John Johnson, Jeremiah Wuenschel and Neil Cherry",
    description: "A bridge between SmartThings and MQTT",
    category:    "My Apps",
    iconUrl:     "https://s3.amazonaws.com/smartapp-icons/Connections/Cat-Connections.png",
    iconX2Url:   "https://s3.amazonaws.com/smartapp-icons/Connections/Cat-Connections@2x.png",
    iconX3Url:   "https://s3.amazonaws.com/smartapp-icons/Connections/Cat-Connections@3x.png"
)

preferences {
    section("Choose which devices to monitor...") {
        input "switches",         "capability.switch",       title: "Switches",       multiple: true, required: false
        input "levels",           "capability.switchLevel",  title: "Levels",         multiple: true, required: false
        input "powerMeters",      "capability.powerMeter",   title: "Power Meters",   multiple: true, required: false
        input "motionSensors",    "capability.motionSensor", title: "Motion Sensors", multiple: true, required: false

        // stolen from https://github.com/davidsulpy/initialstate-smartapp
        input "accelerometers",   "capability.accelerationSensor", title: "Accelerometers", multiple: true, required: false
	input "batteries",        "capability.battery", title: "Batteries", multiple: true, required: false
        input "contacts",         "capability.contactSensor", title: "Contact Sensors", multiple: true, required: false
        input "humidities",       "capability.relativeHumidityMeasurement", title: "Humidity Meters", multiple: true, required: false
        input "illuminances",     "capability.illuminanceMeasurement", title: "Illuminance Meters", multiple: true, required: false
        input "presences",        "capability.presenceSensor", title: "Presence Sensors", multiple: true, required: false
        input "temperatures",     "capability.temperatureMeasurement", title: "Temperature Sensors", multiple: true, required: false

	/*
        input "alarms",           "capability.alarm", title: "Alarms", multiple: true, required: false
	input "beacons",          "capability.beacon", title: "Beacons", multiple: true, required: false
        input "cos",              "capability.carbonMonoxideDetector", title: "Carbon  Monoxide Detectors", multiple: true, required: false
        input "colors",           "capability.colorControl", title: "Color Controllers", multiple: true, required: false
        input "doorsControllers", "capability.doorControl", title: "Door Controllers", multiple: true, required: false
        input "energyMeters",     "capability.energyMeter", title: "Energy Meters", multiple: true, required: false
        input "locks",            "capability.lock", title: "Locks", multiple: true, required: false
        input "musicPlayers",     "capability.musicPlayer", title: "Music Players", multiple: true, required: false
        input "relaySwitches",    "capability.relaySwitch", title: "Relay Switches", multiple: true, required: false
        input "sleepSensors",     "capability.sleepSensor", title: "Sleep Sensors", multiple: true, required: false
        input "smokeDetectors",   "capability.smokeDetector", title: "Smoke Detectors", multiple: true, required: false
        input "peds",             "capability.stepSensor", title: "Pedometers", multiple: true, required: false
        input "thermostats",      "capability.thermostat", title: "Thermostats", multiple: true, required: false
        input "valves",           "capability.valve", title: "Valves", multiple: true, required: false
        input "waterSensors",     "capability.waterSensor", title: "Water Sensors", multiple: true, required: false
	*/
    }

    section ("Bridge") {
        input "bridge", "capability.notification", title: "Notify this Bridge", required: true, multiple: false
    }
}

def installed() {
    log.debug "Installed with settings: ${settings}"

    runEvery15Minutes(initialize)
    initialize()
}

def updated() {
    log.debug "Updated with settings: ${settings}"

    // Unsubscribe from all events
    unsubscribe()
    // Subscribe to stuff
    initialize()
}

// Return list of displayNames
def getDeviceNames(devices) {
    def list = []
    devices.each{device->
        list.push(device.displayName)
    }
    list
}

def initialize() {
    // Subscribe to new events from devices
    subscribe(powerMeters,   "power",  inputHandler)
    subscribe(motionSensors, "motion", inputHandler)
    subscribe(switches,      "switch", inputHandler)
    subscribe(levels,        "level",  inputHandler)

    // new devices stolen from Initial State SmartThings SmartApp
    if (accelerometers != null) {
	subscribe(accelerometers, "acceleration", inputHandler)
    }
    if (batteries != null) {
	subscribe(batteries, "battery", inputHandler)
    }
    if (contacts != null) {
	subscribe(contacts, "contact", inputHandler)
    }
    if (humidities != null) {
	subscribe(humidities, "humidity", inputHandler)
    }
    if (illuminances != null) {
	subscribe(illuminances, "illuminance", inputHandler)
    }
    if (presences != null) {
	subscribe(presences, "presence", inputHandler)
    }
    if (temperatures != null) {
	subscribe(temperatures, "temperature", inputHandler)
    }

    /*
    if (alarms != null) {
	subscribe(alarms, "alarm", inputHandler)
    }
    if (beacons != null) {
	subscribe(beacons, "presence", inputHandler)
    }
    if (cos != null) {
	subscribe(cos, "carbonMonoxide", inputHandler)
    }
    if (colors != null) {
	subscribe(colors, "hue", inputHandler)
	subscribe(colors, "saturation", inputHandler)
	subscribe(colors, "color", inputHandler)
    }
    if (energyMeters != null) {
	subscribe(energyMeters, "energy", inputHandler)
    }
    if (locks != null) {
	subscribe(locks, "lock", inputHandler)
    }
    if (musicPlayers != null) {
	subscribe(musicPlayers, "status", inputHandler)
	subscribe(musicPlayers, "level", inputHandler)
	subscribe(musicPlayers, "trackDescription", inputHandler)
	subscribe(musicPlayers, "trackData", inputHandler)
	subscribe(musicPlayers, "mute", inputHandler)
    }
    if (relaySwitches != null) {
	subscribe(relaySwitches, "switch", inputHandler)
    }
    if (sleepSensors != null) {
	subscribe(sleepSensors, "sleeping", inputHandler)
    }
    if (smokeDetectors != null) {
	subscribe(smokeDetectors, "smoke", inputHandler)
    }
    if (peds != null) {
	subscribe(peds, "steps", inputHandler)
	subscribe(peds, "goal", inputHandler)
    }
    if (thermostats != null) {
	subscribe(thermostats, "temperature", inputHandler)
	subscribe(thermostats, "heatingSetpoint", inputHandler)
	subscribe(thermostats, "coolingSetpoint", inputHandler)
	subscribe(thermostats, "thermostatSetpoint", inputHandler)
	subscribe(thermostats, "thermostatMode", inputHandler)
	subscribe(thermostats, "thermostatFanMode", inputHandler)
	subscribe(thermostats, "thermostatOperatingState", inputHandler)
    }
    if (valves != null) {
	subscribe(valves, "contact", inputHandler)
    }
    if (waterSensors != null) {
	subscribe(waterSensors, "water", inputHandler)
    }
    */
    // Subscribe to events from the bridge
    subscribe(bridge, "message", bridgeHandler)

    // Update the bridge
    updateSubscription()
}

// Update the bridge's subscription
def updateSubscription() {
    def json = new groovy.json.JsonOutput().toJson([
        path: '/subscribe',
        body: [
            devices: [
                power:        getDeviceNames(powerMeters),
                motion:       getDeviceNames(motionSensors),
                switch:       getDeviceNames(switches),
                level:        getDeviceNames(levels),
		/* */
		acceleration: getDeviceNames(accelerations),
		battery:      getDeviceNames(batteries),
		contact:      getDeviceNames(contacts),
		humidity:     getDeviceNames(humidities),
		illuminance:  getDeviceNames(illuminances),
		presence:     getDeviceNames(presences),
		temperature:  getDeviceNames(temperatures)
            ]
        ]
    ])

    log.debug "Updating subscription: ${json}"

    bridge.deviceNotification(json)
}

// Receive an event from the bridge
def bridgeHandler(evt) {
    def json = new JsonSlurper().parseText(evt.value)

    switch (json.type) {
        // Basically part of the default below
        //case "power":
        //case "motion":
        //    // Do nothing, we can change nothing here
        //    break
        case "switch":
            switches.each{device->
                if (device.displayName == json.name) {
                    if (json.value == 'on') {
                        device.on();
                    } else {
                        device.off();
                    }
                }
            }
            break
        case "level":
            levels.each{device->
                if (device.displayName == json.name) {
                    device.setLevel(json.value);
                }
            }
            break

	//    
	default:
	    break
    }

    log.debug "Receiving device event from bridge: ${json}"
}

// Receive an event from a device
def inputHandler(evt) {
    def json = new JsonOutput().toJson([
        path: '/push',
        body: [
            name: evt.displayName,
            value: evt.value,
            type: evt.name
        ]
    ])

    log.debug "Forwarding device event to bridge: ${json}"
    bridge.deviceNotification(json)
}
