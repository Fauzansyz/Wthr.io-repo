// File: StateManager.kt
package com.fauzan.wthrio

object StateManager {
    private var _switchState: Boolean = false

    // Callback untuk mendengar perubahan state
    private var listener: ((Boolean) -> Unit)? = null

    var switchState: Boolean
        get() = _switchState
        set(value) {
            _switchState = value
            listener?.invoke(value) // Panggil listener setiap kali state diubah
        }

    fun setOnStateChangeListener(callback: (Boolean) -> Unit) {
        listener = callback
    }
}