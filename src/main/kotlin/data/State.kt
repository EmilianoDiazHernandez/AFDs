package data

import androidx.compose.runtime.MutableState

class State(var type: MutableState<TypeState>, val transitions: MutableList<Transition>)