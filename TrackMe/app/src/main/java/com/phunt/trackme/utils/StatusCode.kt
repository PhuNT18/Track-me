package com.phunt.trackme.utils

annotation class LocationService {
    companion object {
        const val PACKAGE_NAME = "com.phunt.trackme.service.LocationUpdatesService"
        const val PREF_NAME = "default-pref"
        const val ACTION_BROADCAST: String = "$PACKAGE_NAME.broadcast"
        const val EXTRA_LOCATION: String = "$PACKAGE_NAME.location"
        const val EXTRA_STARTED_FROM_NOTIFICATION: String = "$PACKAGE_NAME.started_from_notification"
    }
}