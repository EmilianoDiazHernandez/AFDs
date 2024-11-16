package controller

import data.State
import data.TypeState

fun validInput(input: String, states: List<State>): String {
    var currentStates = states.filter { it.type.value == TypeState.INITIAL }.toMutableList()

    if (currentStates.isEmpty()) return "No hay estado inicial"

    input.forEach { char ->
        currentStates = currentStates.flatMap { state ->
            state.transitions.filter { it.char == char || it.char == 'ε' }
                .flatMap { transition ->
                    if (transition.char == char) listOf(transition.goTo)
                    else transition.goTo.transitions.filter { it.char == char }.map { it.goTo }
                }
        }.toMutableList()
    }

    currentStates.addAll(
        currentStates.flatMap { state ->
            state.transitions.filter { it.char == 'ε' }.map { it.goTo }
        }
    )

    return if (currentStates.any { it.type.value == TypeState.FINAL }) "Cadena aceptada" else "Cadena rechazada"
}