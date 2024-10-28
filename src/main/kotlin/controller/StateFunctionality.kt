package controller

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import data.State
import data.TypeState
import ui.StateCoords
import ui.TransitionCoords
import ui.drawState

fun createState(states: MutableList<StateCoords>) {
    states.add(
        StateCoords(
            mutableStateOf(Offset(100f, 100f)),
            State(mutableStateOf(TypeState.NORMAL), mutableListOf())
        )
    )
}

fun deleteState(states: MutableList<StateCoords>, transitions: MutableList<TransitionCoords>, state: State) {
    states.removeIf { it.id == state }
    transitions.removeAll { it.coordState1.id == state || it.coordState2.id == state }
}

fun changeTypeState(states: List<StateCoords>, state: State, key: TypeState) {
    if (state.type.value == key)
        state.type.value = TypeState.NORMAL
    else
        if (!states.any { it.id.type.value == TypeState.INITIAL && key == TypeState.INITIAL })
            state.type.value = key
}

fun DrawScope.drawStates(states: List<StateCoords>) {
    states.forEachIndexed { i, state ->
        drawState(state, i)
    }
}
