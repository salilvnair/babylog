package com.salilvnair.babylog.model

import java.util.Date

data class BabyLogModel(
    var id: Int?  = 0,
    var key:String? = "",
    var lastFed:Date? = Date()
)
