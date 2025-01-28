package com.fauzan.wthrio
class HelperClass {
    fun updateState(isChecked: Boolean) {
        // Update state di Singleton
        StateManager.switchState = isChecked
    }
}