package com.eliasball.debtmanager.data.db.entities

import androidx.room.ColumnInfo

data class PersonDebt(
    @ColumnInfo(name = "person") val name: String,
    val total: Double,
    val moneyCount: Int,
    val objectsCount: Int
)