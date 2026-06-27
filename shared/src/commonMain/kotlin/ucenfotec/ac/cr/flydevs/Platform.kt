package ucenfotec.ac.cr.flydevs

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getEpochMillis(): Long