package com.ruota.uicommon.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.ruota.uicommon.R

/**
 * Loads and plays the game's short sound effects via [SoundPool] (low-latency, made for
 * overlapping SFX). Sounds are the procedurally-generated WAVs in `res/raw`.
 */
class GameSounds(context: Context) {

    private val pool: SoundPool = SoundPool.Builder()
        .setMaxStreams(6)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        .build()

    private val spin = pool.load(context, R.raw.wheel_spin, 1)
    private val ding = pool.load(context, R.raw.letter_ding, 1)
    private val miss = pool.load(context, R.raw.letter_miss, 1)
    private val winner = pool.load(context, R.raw.winner, 1)

    /** Starts the wheel-spin sound; returns the stream id so it can be [stop]ped on settle. */
    fun playSpin(): Int = pool.play(spin, 1f, 1f, 1, 0, 1f)

    fun stop(streamId: Int) {
        if (streamId != 0) pool.stop(streamId)
    }

    fun playDing() {
        pool.play(ding, 1f, 1f, 1, 0, 1f)
    }

    fun playMiss() {
        pool.play(miss, 1f, 1f, 1, 0, 1f)
    }

    fun playWinner() {
        pool.play(winner, 1f, 1f, 1, 0, 1f)
    }

    fun release() {
        pool.release()
    }
}

/** Remembers a [GameSounds] tied to the composition and releases it on dispose. */
@Composable
fun rememberGameSounds(): GameSounds {
    val context = LocalContext.current
    val sounds = remember { GameSounds(context.applicationContext) }
    DisposableEffect(Unit) {
        onDispose { sounds.release() }
    }
    return sounds
}
