package com.ruota.uicommon.screen

import com.ruota.core.engine.Outcome
import com.ruota.uicommon.format.formatMoney

/**
 * Maps an engine [Outcome] to a short Italian status line for the player. Pure and
 * JVM-testable.
 */
fun outcomeMessage(outcome: Outcome?): String? = when (outcome) {
    null -> null
    is Outcome.SpunCash -> "Hai girato ${formatMoney(outcome.amount)}. Scegli una consonante!"
    is Outcome.SpunBankrupt -> "BANCAROTTA! Turno a ${outcome.nextPlayer.name}."
    is Outcome.SpunLoseATurn -> "PASSA IL TURNO! Turno a ${outcome.nextPlayer.name}."
    is Outcome.ConsonantHit ->
        "'${outcome.letter}' × ${outcome.count} = +${formatMoney(outcome.earned)}!"
    is Outcome.ConsonantMiss -> "Nessuna '${outcome.letter}'. Turno a ${outcome.nextPlayer.name}."
    is Outcome.VowelBought -> "Vocale '${outcome.letter}' × ${outcome.count}."
    is Outcome.VowelMiss -> "Nessuna '${outcome.letter}'. Turno a ${outcome.nextPlayer.name}."
    is Outcome.SolvedCorrect ->
        "Esatto! ${outcome.winner.name} incassa ${formatMoney(outcome.awarded)}!"
    is Outcome.SolvedWrong -> "Sbagliato! Turno a ${outcome.nextPlayer.name}."
    is Outcome.RoundStarted ->
        "Round ${outcome.roundIndex + 1}: inizia ${outcome.startingPlayer.name}."
    is Outcome.GameOver -> "Partita finita!"
    is Outcome.Invalid -> outcome.reason
}

/** The wheel wedge index for a spin outcome, or null if the outcome was not a spin. */
fun spinIndexOf(outcome: Outcome?): Int? = when (outcome) {
    is Outcome.SpunCash -> outcome.index
    is Outcome.SpunBankrupt -> outcome.index
    is Outcome.SpunLoseATurn -> outcome.index
    else -> null
}
