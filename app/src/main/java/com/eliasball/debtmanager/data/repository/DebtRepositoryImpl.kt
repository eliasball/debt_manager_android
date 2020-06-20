package com.eliasball.debtmanager.data.repository

import androidx.lifecycle.LiveData
import com.eliasball.debtmanager.data.db.DebtDao
import com.eliasball.debtmanager.data.db.entities.Debt
import com.eliasball.debtmanager.data.db.entities.PersonDebt
import com.eliasball.debtmanager.data.db.entities.PersonDetail

class DebtRepositoryImpl(
    private val debtDao: DebtDao
) : DebtRepository {
    override fun upsertDebt(debt: Debt) = debtDao.upsertDebt(debt)

    override fun deleteDebt(debt: Debt) = debtDao.deleteDebt(debt)

    override fun getDebt(date: Long): Debt = debtDao.getDebt(date)

    override fun getOthersOweMe(): LiveData<List<Debt>> = debtDao.getOthersOweMe()

    override fun getIOweOthers(): LiveData<List<Debt>> = debtDao.getIOweOthers()

    override fun getDebtsByPerson(person: String): LiveData<List<Debt>> =
        debtDao.getDebtsByPerson(person)

    override fun getDebtsByPersonNotLive(person: String): List<Debt> = debtDao.getDebtsByPersonNotLive(person)

    override fun getDebtsPerPeople(): LiveData<List<PersonDebt>> = debtDao.getDebtsPerPeople()

    override fun getPersonDetails(person: String): LiveData<PersonDetail> =
        debtDao.getPersonDetails(person)

    override fun getNames(): List<String> = debtDao.getNames()
}