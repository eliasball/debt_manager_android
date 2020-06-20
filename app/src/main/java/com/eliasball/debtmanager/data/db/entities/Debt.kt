package com.eliasball.debtmanager.data.db.entities

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eliasball.debtmanager.internal.DebtTypes
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "debt_table")
data class Debt(
    @PrimaryKey(autoGenerate = false)
    val date: Long = System.currentTimeMillis(),
    var person: String = "",
    var type: String = DebtTypes.MONEY.type,
    var amount: Double = 0.0,
    var objects: String = "",
    var reason: String = "",
    @ColumnInfo(name = "i_owe")
    var iOwe: Boolean = false
) : Parcelable