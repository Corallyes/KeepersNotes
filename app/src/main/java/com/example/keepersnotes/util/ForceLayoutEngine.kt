package com.example.keepersnotes.util

import androidx.compose.ui.geometry.Offset

/**
 * Shared force-directed layout engine.
 * Operates on (id, position, pinned) triples — no dependency on UI data classes.
 */
class ForceLayoutEngine {
    companion object {
        private const val REPULSION = 50000f
        private const val ATTRACTION = 0.005f
        private const val IDEAL_LENGTH = 200f
        private const val CENTER_GRAVITY = 0.01f
        private const val DAMPING = 0.85f
        private const val MIN_VELOCITY = 0.1f
        private const val MAX_VELOCITY = 50f
        private const val DT = 0.3f
    }

    private val velocityCache = mutableMapOf<String, Pair<Float, Float>>()

    data class InputNode(
        val id: String,
        val position: Offset,
        val pinned: Boolean = false
    )

    data class InputEdge(
        val sourceId: String,
        val targetId: String
    )

    /**
     * Runs force simulation. Returns map of nodeId → final position.
     */
    fun simulate(
        nodes: List<InputNode>,
        edges: List<InputEdge>,
        canvasWidth: Float,
        canvasHeight: Float
    ): Map<String, Offset> {
        if (nodes.isEmpty()) return emptyMap()

        val centerX = canvasWidth / 2f
        val centerY = canvasHeight / 2f

        val nodeIds = nodes.map { it.id }.toSet()
        velocityCache.keys.retainAll(nodeIds)

        data class State(
            val id: String,
            var position: Offset,
            val pinned: Boolean,
            var vx: Float = 0f,
            var vy: Float = 0f
        )

        val states = nodes.map { node ->
            val cached = velocityCache[node.id]
            val pos = if (node.position == Offset.Zero) {
                Offset(
                    centerX + (Math.random().toFloat() - 0.5f) * 200f,
                    centerY + (Math.random().toFloat() - 0.5f) * 200f
                )
            } else node.position
            State(
                id = node.id,
                position = pos,
                pinned = node.pinned,
                vx = cached?.first ?: 0f,
                vy = cached?.second ?: 0f
            )
        }

        val stateIndex = states.associateBy { it.id }

        for (iteration in 0 until 300) {
            var totalEnergy = 0f

            states.forEach { if (!it.pinned) { it.vx = 0f; it.vy = 0f } }

            // Repulsion
            for (i in states.indices) {
                for (j in i + 1 until states.size) {
                    val a = states[i]; val b = states[j]
                    var dx = b.position.x - a.position.x
                    var dy = b.position.y - a.position.y
                    var dist = kotlin.math.sqrt(dx * dx + dy * dy)
                    if (dist < 1f) {
                        dx = (Math.random().toFloat() - 0.5f) * 2f
                        dy = (Math.random().toFloat() - 0.5f) * 2f
                        dist = 1f
                    }
                    val force = REPULSION / (dist * dist)
                    val fx = (dx / dist) * force
                    val fy = (dy / dist) * force
                    if (!a.pinned) { a.vx -= fx; a.vy -= fy }
                    if (!b.pinned) { b.vx += fx; b.vy += fy }
                }
            }

            // Attraction
            for (edge in edges) {
                val src = stateIndex[edge.sourceId] ?: continue
                val tgt = stateIndex[edge.targetId] ?: continue
                val dx = tgt.position.x - src.position.x
                val dy = tgt.position.y - src.position.y
                val dist = kotlin.math.sqrt(dx * dx + dy * dy)
                if (dist > 0f) {
                    val displacement = dist - IDEAL_LENGTH
                    val force = ATTRACTION * displacement
                    val fx = (dx / dist) * force
                    val fy = (dy / dist) * force
                    if (!src.pinned) { src.vx += fx; src.vy += fy }
                    if (!tgt.pinned) { tgt.vx -= fx; tgt.vy -= fy }
                }
            }

            // Center gravity
            for (state in states) {
                if (!state.pinned) {
                    state.vx += (centerX - state.position.x) * CENTER_GRAVITY
                    state.vy += (centerY - state.position.y) * CENTER_GRAVITY
                }
            }

            // Apply velocities
            for (state in states) {
                if (state.pinned) continue
                state.vx *= DAMPING; state.vy *= DAMPING
                var speed = kotlin.math.sqrt(state.vx * state.vx + state.vy * state.vy)
                if (speed > MAX_VELOCITY) {
                    state.vx = (state.vx / speed) * MAX_VELOCITY
                    state.vy = (state.vy / speed) * MAX_VELOCITY
                    speed = MAX_VELOCITY
                }
                totalEnergy += speed
                state.position = Offset(
                    (state.position.x + state.vx * DT).coerceIn(40f, canvasWidth - 40f),
                    (state.position.y + state.vy * DT).coerceIn(40f, canvasHeight - 40f)
                )
            }

            if (totalEnergy < MIN_VELOCITY * states.size) break
        }

        for (state in states) {
            velocityCache[state.id] = Pair(state.vx, state.vy)
        }

        return states.associate { it.id to it.position }
    }
}
