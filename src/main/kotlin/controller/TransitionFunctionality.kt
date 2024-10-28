package controller

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import data.Transition
import ui.StateCoords
import ui.TransitionCoords
import ui.drawArrow

fun createTransition(states: List<StateCoords>, state1: StateCoords, state2: StateCoords, a: Char): TransitionCoords?{
    val transitionExists = state1.id.transitions.any {
        it.char == a && it.goTo == state2.id
    }

    if (!transitionExists) {
        val transition = Transition(a, state2.id)

        states.forEachIndexed { i, state ->
            if (state.id == state1.id) {
                states[i].id.transitions.add(transition)
                return TransitionCoords(state1, state2, transition)
            }
        }
    }
    return null
}

fun DrawScope.drawTransitions(transitions: List<TransitionCoords>) {
    val groupTransitions = transitions.groupBy(
        keySelector = { Pair(it.coordState1.offset.value, it.coordState2.offset.value) },
        valueTransform = { it.id.char }
    )

    groupTransitions.forEach { (key, chars) ->
        drawArrow(key.first, key.second, chars, Color.Black)
        //println("Q1: " + transition.coordState1.id + "  --" + transition.id.char + "-->  " + "Q2: " + transition.id.goTo)
    }
}

