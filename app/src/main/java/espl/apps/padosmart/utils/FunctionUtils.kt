package espl.apps.padosmart.utils

import kotlin.random.Random


fun generateOTP(): Int {
    return Random.nextInt(999, 9999)
}