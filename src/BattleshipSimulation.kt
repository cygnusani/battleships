/**
 * --------------------------------------------------------------------
 * ----------------------- BATTLESHIP SIMULATION ----------------------
 * --------------------------------------------------------------------
 *
 *                   [ ] [*] [ ] [ ] [ ]   <- YOU
 *
 *                                V        <- Guided torpedo
 *
 *                   [ ] [ ] [ ] [*] [ ]   <- ENEMY
 *
 *
 * You are tasked to write an AI for the battleship game simulation.
 * Each player has four battleships [] and one flagship [*] at random
 * places in a row. The goal is to guess the location of the opponent's
 * flagship and hit it with a guided torpedo, which can be fired once
 * from any ship. After torpedo's target is locked, its course can only
 * be slightly corrected. The battle is simulated many times and the
 * player with a better win ratio is the ultimate winner.
 *
 * The enemy has already overridden the Command Center and is now
 * stably winning the simulation. Can you find enemy's weak spots
 * and write an even better Command Center? Desperate times require
 * new ideas, so we are using Kotlin now. Place your code only inside
 * the `YourCommandCenter` class located at the bottom of this file.
 * Please document your logic so that we can understand not only how,
 * but also *why* it works.
 *
 * Send your solution to careers@icefire.ee
 */

val random = java.util.Random()

data class TorpedoAttack(val source: Int, val target: Int)

// CommandCenter interface with the default behaviour
interface CommandCenter {

    fun fireTorpedo(ships: BooleanArray): TorpedoAttack {
        // Flagship is marked as Boolean TRUE
        val flagship = ships.indexOfFirst { it }
        // Fire torpedo from flagship at random target
        return TorpedoAttack(flagship, random.nextInt(5))
    }

    fun guideTorpedo(attack: TorpedoAttack): Int {
        // Return either -1, 0, 1 to move torpedo a bit
        return random.nextInt(3) - 1
    }

    fun onTorpedoDetected(attack: TorpedoAttack) {
        // Triggered when the opponent fires his torpedo
    }

}

fun simulate(): Pair<Boolean, Boolean>  {
    // Initialize command centers
    val yourCC = YourCommandCenter()
    val enemyCC = EnemyCommandCenter()

    // Initialize battleships
    val yourShips = BooleanArray(5)
    val enemyShips = BooleanArray(5)

    // Randomly place flagships
    yourShips[random.nextInt(yourShips.size)] = true
    enemyShips[random.nextInt(yourShips.size)] = true

    // Your fire first
    val yourAttack = yourCC.fireTorpedo(yourShips)
    enemyCC.onTorpedoDetected(yourAttack)

    // Enemy fires second
    val enemyAttack = enemyCC.fireTorpedo(enemyShips)
    yourCC.onTorpedoDetected(enemyAttack)

    // You guide your torpedo
    val yourTarget = yourAttack.target + yourCC.guideTorpedo(yourAttack) % 2

    // Enemy guides his torpedo
    val enemyTarget = enemyAttack.target + enemyCC.guideTorpedo(enemyAttack) % 2

    // Check if each side's flagship has been hit
    // Note: Both players can be winners at the same time
    return Pair(
            yourShips.getOrElse(enemyTarget) { false },
            enemyShips.getOrElse(yourTarget) { false }
    )
}

fun main(args: Array<String>) {
    var yourWins = 0
    var enemyWins = 0

    for (i in 1..100000) {
        val (enemyWon, youWon) = simulate()
        if (youWon) yourWins++
        if (enemyWon) enemyWins++
    }

    val ratio = yourWins.toDouble() / enemyWins.toDouble()

    println("Your wins to enemy's wins ratio is $ratio")

    if (ratio < 1.0) println("You lost! Improve your battle logic!")
    else if (ratio < 1.25) println("You won by a small margin! Can you do better?")
    else println("Congratulations! Your victory is undeniable. Good job!")
}

class EnemyCommandCenter : CommandCenter {

    var attackedShip = -1

    override fun fireTorpedo(ships: BooleanArray): TorpedoAttack {
        // Enemy has overridden this method to hide his flagship
        val flagship = ships.indexOfFirst { it }
        var source: Int

        do source = random.nextInt(5) while (source == flagship || source == attackedShip)

        return TorpedoAttack(source, random.nextInt(5))
    }

    override fun guideTorpedo(attack: TorpedoAttack): Int {
        // Ensure that torpedo doesn't sway off and always hits at least some ship
        return if (attack.target in 1..3) random.nextInt(3) - 1 else 0
    }

    override fun onTorpedoDetected(attack: TorpedoAttack) {
        attackedShip = attack.target
    }
}

class YourCommandCenter : CommandCenter {
    // Source of the enemy torpedo
    private var enemySource = -1

    override fun fireTorpedo(ships: BooleanArray): TorpedoAttack {
        /*
        The enemy doesn't care about the source of my torpedo so it could be any value.
        I should  aim in the center area (at indexes 1, 2, 3). This way when I guide my torpedo, I'll
        always hit at least one ship.
        */
        return TorpedoAttack(-1, 1 + random.nextInt(3))
    }

    override fun guideTorpedo(attack: TorpedoAttack): Int {
        /*
        The goal is to keep away from the source of the enemy torpedo.
        When target is higher than the source, we should aim higher as well
        */
        if (attack.target > enemySource) return 1
        // When the target is lower, we should aim lower
        if (attack.target < enemySource) return -1
        // Otherwise set the random course change (-1, 0 or 1)
        return random.nextInt(3) - 1
    }

    override fun onTorpedoDetected(attack: TorpedoAttack) {
        // We know that the source of the torpedo is a fake (not a flagship)
        enemySource = attack.source
    }
}