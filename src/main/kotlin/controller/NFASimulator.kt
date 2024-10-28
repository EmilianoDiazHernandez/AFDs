package controller

import data.State
import data.TypeState

fun validInput(input: String, states: List<State>): String {
    var currentStates: MutableList<State> = (mutableListOf())
    val newStates: MutableList<State> = (mutableListOf())

    states.firstOrNull { it.type.value == TypeState.INITIAL }?.let { currentStates.add(it) }
        ?: return "No hay estado inicial"

    input.forEach { char ->
        newStates.clear()
        currentStates.forEach { state ->
            state.transitions.forEach { transition ->
                if (transition.char == char) {
                    newStates.add(transition.goTo)
                }
            }
        }
        currentStates = newStates.toMutableList()
    }

    return if (currentStates.any { it.type.value == TypeState.FINAL }) "Cadena aceptada" else "Cadena rechazada"
}