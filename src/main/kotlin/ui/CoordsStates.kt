package ui

import androidx.compose.runtime.MutableState
import androidx.compose.ui.geometry.Offset
import data.State

data class CoordsStates(val offset: MutableState<Offset>, val id: State)