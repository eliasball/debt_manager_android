package com.eliasball.debtmanager.data.repository

import androidx.lifecycle.LiveData
import com.eliasball.debtmanager.data.db.entities.Debt
import com.eliasball.debtmanager.data.db.entities.PersonDebt
import com.eliasball.debtmanager.data.db.entities.PersonDetail

interface DebtRepository {

    fun upsertDebt(debt: Debt)

    fun deleteDebt(debt: Debt)

    fun getDebt(date: Long): Debt

    fun getOthersOweMe(): LiveData<List<Debt>>

    fun getIOweOthers(): LiveData<List<Debt>>

    fun getDebtsByPerson(person: String): LiveData<List<Debt>>

    fun getDebtsByPersonNotLive(person: String): List<Debt>

    fun getDebtsPerPeople(): LiveData<List<PersonDebt>>

    fun getPersonDetails(person: String): LiveData<PersonDetail>

    fun getNames(): List<String>
}