package com.eliasball.debtmanager.data.providers

import com.eliasball.debtmanager.data.db.entities.Debt
import com.eliasball.debtmanager.data.repository.DebtRepository
import com.eliasball.debtmanager.internal.DebtTypes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

class ShareParser {

    suspend fun getShareDebtsForPersonString(
        person: String,
        debtRepository: DebtRepository,
        currency: String
    ): String {

        // Get the data
        val data = withContext(Dispatchers.IO) {
            debtRepository.getDebtsByPersonNotLive(person)
        }
        if (data.isEmpty()) return "No data found."

        // Categorize the list
        val iOwe = data.filter {
            it.iOwe
        }
        val iGet = data.filter {
            !it.iOwe
        }

        // Create the string builder
        val sBuilder = StringBuilder()

        // Append the header
        sBuilder.append("== ${data[0].person}'s debt report ==\n\n\n")

        // Append the formatted debts which i get
        sBuilder.append("== ${data[0].person} owes me ==\n")
        val totalGet = addFormattedDebts(iGet, sBuilder, currency)

        sBuilder.append("\n\n")

        // Append the formatted debts which i owe
        sBuilder.append("== I owe ${data[0].person} ==\n")
        val totalOwe = addFormattedDebts(iOwe, sBuilder, currency)

        sBuilder.append("\n\n")

        sBuilder.append("== Total ==\n\n")
        val totalMoney = totalGet[0] - totalOwe[0]
        sBuilder.append("Money:\n")
        sBuilder.append(
            when{
                totalMoney > 0 -> "I get from ${data[0].person}: %.2f %s\n".format(totalMoney, currency)
                totalMoney < 0 -> "${data[0].person} gets from me: %.2f %s\n".format(totalMoney, currency)
                else -> "The money sums up to 0 %s.\n".format(currency)
            }
        )
        sBuilder.append("\n")
        sBuilder.append("Objects:\n")
        sBuilder.append("I owe ${data[0].person} ${totalOwe[1].toInt()} object/s.\n")
        sBuilder.append("${data[0].person} owes me ${totalGet[1].toInt()} object/s.\n")

        return sBuilder.toString()
    }

    private fun addFormattedDebts(
        data: List<Debt>,
        stringBuilder: StringBuilder,
        currency: String
    ): List<Double> {
        if (data.isNotEmpty()) {
            // The totals
            var totalMoney = 0.0
            var totalObjects = 0.0

            // Parse all money
            stringBuilder.append("Money:\n")
            if (data.any { it.type == DebtTypes.MONEY.type }) {
                for (debt in data.filter { it.type == DebtTypes.MONEY.type }) {
                    totalMoney += abs(debt.amount)
                    stringBuilder.append(
                        "%.2f %s (${debt.reason})\n".format(
                            abs(debt.amount),
                            currency
                        )
                    )
                }
                stringBuilder.append("----------\n%.2f %s\n".format(totalMoney, currency))
            } else stringBuilder.append("- Nothing -\n")

            stringBuilder.append("\n")

            // Parse all objects
            stringBuilder.append("Objects:\n")
            if (data.any { it.type == DebtTypes.OBJECTS.type }) {
                for (debt in data.filter { it.type == DebtTypes.OBJECTS.type }) {
                    totalObjects += 1.0
                    stringBuilder.append(
                        "${debt.objects} (${debt.reason})\n"
                    )
                }
            } else stringBuilder.append("- Nothing -\n")

            return listOf(totalMoney, totalObjects)

        } else {
            stringBuilder.append("- Nothing -\n")
            return listOf(0.0, 0.0)
        }
    }


    suspend fun getShareDebtString(
        date: Long,
        repository: DebtRepository,
        currency: String
    ): String {
        val debt = withContext(Dispatchers.IO) {
            repository.getDebt(date)
        }
        return if (debt.iOwe) {
            when (debt.type) {
                DebtTypes.MONEY.type -> "I owe ${debt.person}:\n%.2f %s\nReason: ${debt.reason}".format(
                    abs(debt.amount),
                    currency
                )
                else -> "I owe ${debt.person}:\n${debt.objects} \nReason: ${debt.reason}"
            }
        } else {
            when (debt.type) {
                DebtTypes.MONEY.type -> "${debt.person} owes me:\n%.2f %s\nReason: ${debt.reason}".format(
                    abs(debt.amount),
                    currency
                )
                else -> "${debt.person} owes me:\n${debt.objects} \nReason: ${debt.reason}"
            }
        }
    }

}