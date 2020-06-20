package com.eliasball.debtmanager.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.eliasball.debtmanager.R
import com.eliasball.debtmanager.data.db.entities.PersonDebt
import com.eliasball.debtmanager.data.providers.CurrencyProvider
import com.eliasball.debtmanager.databinding.PersonCardBinding
import kotlin.math.abs

class PeopleRecyclerAdapter(
    private var data: MutableList<PersonDebt>,
    private val currencyProvider: CurrencyProvider,
    private val onClick: (name: String, view: View) -> Unit
) :
    RecyclerView.Adapter<PeopleRecyclerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            PersonCardBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            currencyProvider,
            onClick
        )

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    fun setData(newData: List<PersonDebt>) {
        val diffCallback = PersonDebtDiffUtilCallback(data, newData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        data.clear()
        data.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }

    class ViewHolder(
        private val binding: PersonCardBinding,
        private val currencyProvider: CurrencyProvider,
        private val onClick: (name: String, view: View) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(dataItem: PersonDebt) {
            binding.nameText.text = dataItem.name
            binding.moneyCountText.text =
                itemView.context.getString(R.string.count_text, dataItem.moneyCount)
            binding.objectsCountText.text =
                itemView.context.getString(R.string.count_text, dataItem.objectsCount)

            if (dataItem.moneyCount > 0) {
                binding.totalText.text =
                    itemView.context.getString(
                        R.string.total_text,
                        abs(dataItem.total),
                        currencyProvider.getCurrencySymbol()
                    )
                binding.totalText.visibility = View.VISIBLE
                binding.colorLine.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context, when {
                            dataItem.total >= 0 -> R.color.lineGreen
                            else -> R.color.lineRed
                        }
                    )
                )
                binding.colorLine.contentDescription = itemView.context.getString(
                    when {
                        dataItem.total >= 0 -> R.string.i_get
                        else -> R.string.i_owe
                    }
                )
            } else {
                binding.totalText.visibility = View.GONE
                binding.colorLine.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        R.color.lineGray
                    )
                )
                binding.colorLine.contentDescription = itemView.context.getString(R.string.no_money_entries_desc)
            }
            itemView.setOnClickListener {
                onClick(dataItem.name, itemView)
            }
        }
    }

    class PersonDebtDiffUtilCallback(
        private val oldList: List<PersonDebt>,
        private val newList: List<PersonDebt>
    ) : DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].name == newList[newItemPosition].name

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition] === newList[newItemPosition]
    }
}