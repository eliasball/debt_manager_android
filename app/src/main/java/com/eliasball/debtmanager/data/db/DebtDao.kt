package com.eliasball.debtmanager.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.eliasball.debtmanager.data.db.entities.Debt
import com.eliasball.debtmanager.data.db.entities.PersonDebt
import com.eliasball.debtmanager.data.db.entities.PersonDetail

@Dao
interface DebtDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertDebt(debt: Debt)

    @Delete
    fun deleteDebt(debt: Debt)

    @Query("select * from debt_table where date = :date")
    fun getDebt(date: Long): Debt

    @Query("select * from debt_table where not i_owe order by date desc")
    fun getOthersOweMe(): LiveData<List<Debt>>

    @Query("select * from debt_table where i_owe order by date desc")
    fun getIOweOthers(): LiveData<List<Debt>>

    @Query("select * from debt_table where person = :person order by date desc")
    fun getDebtsByPerson(person: String): LiveData<List<Debt>>

    @Query("select * from debt_table where person = :person order by date desc")
    fun getDebtsByPersonNotLive(person: String): List<Debt>

    @Query("select person, sum(amount) as total, count(case when type = 'money' then 1 end) as moneyCount, count(case when type = 'objects' then 1 end) as objectsCount from debt_table group by person order by person asc ")
    fun getDebtsPerPeople(): LiveData<List<PersonDebt>>

    @Query("select sum(amount) as total, count(case when type = 'money' then 1 end) as moneyCount, count(case when type = 'objects' then 1 end) as objectsCount from debt_table where person = :person")
    fun getPersonDetails(person: String): LiveData<PersonDetail>

    @Query("select person from debt_table group by person order by person asc")
    fun getNames(): List<String>
}