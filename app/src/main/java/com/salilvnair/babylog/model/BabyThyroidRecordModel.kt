package com.salilvnair.babylog.model

import java.util.Date

data class BabyThyroidRecordModel(
    var key:String? = "",
    var completed:Boolean = false,
    var date:Date = Date()
)
