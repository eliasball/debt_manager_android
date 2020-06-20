package com.eliasball.debtmanager.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.eliasball.debtmanager.R
import com.eliasball.debtmanager.data.db.entities.Debt
import com.eliasball.debtmanager.data.providers.CurrencyProvider
import com.eliasball.debtmanager.databinding.DebtCardBinding
import com.eliasball.debtmanager.internal.DebtTypes
import kotlin.math.abs

class DebtRecyclerAdapter(
    private var data: MutableList<Debt>,
    private val currencyProvider: CurrencyProvider,
    private val hideNames: Boolean = false,
    private val onSendClick: (date: Long) -> Unit,
    private val onTickClick: (debt: Debt) -> Unit,
    private val onContainerClick: (debt: Debt) -> Unit
) :
    RecyclerView.Adapter<DebtRecyclerAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(
            DebtCardBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            currencyProvider,
            hideNames,
            onSendClick,
            onTickClick,
            onContainerClick
        )

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    fun setData(newData: List<Debt>) {
        val diffCallback = DebtDiffUtilCallback(data, newData)
        val diffResult = DiffUtil.calculateDiff(diffCallback)
        data.clear()
        data.addAll(newData)
        diffResult.dispatchUpdatesTo(this)
    }


    class ViewHolder(
        private val binding: DebtCardBinding,
        private val currencyProvider: CurrencyProvider,
        private val hideNames: Boolean,
        private val onSendClick: (date: Long) -> Unit,
        private val onTickClick: (debt: Debt) -> Unit,
        private val onContainerClick: (debt: Debt) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(dataItem: Debt) {
            if (hideNames) {
                binding.personText.visibility = View.GONE
                binding.personIcon.visibility = View.GONE
            } else {
                binding.personText.text = dataItem.person
            }

            binding.amountObjectsText.text = when (dataItem.type) {
                DebtTypes.MONEY.type -> itemView.context.getString(
                    R.string.debt_amount,
                    abs(dataItem.amount),
                    currencyProvider.getCurrencySymbol()
                )
                else -> dataItem.objects
            }

            binding.reasonText.text = dataItem.reason

            binding.colorLine.setBackgroundColor(
                ContextCompat.getColor(
                    itemView.context,
                    when (dataItem.iOwe) {
                        true -> R.color.lineRed
                        else -> R.color.lineGreen
                    }
                )
            )
            binding.colorLine.contentDescription = itemView.context.getString(when (dataItem.iOwe) {
                true -> R.string.i_owe
                else -> R.string.i_get
            })

            binding.iconImg.setImageResource(
                when (dataItem.type) {
                    DebtTypes.MONEY.type -> R.drawable.ic_money
                    else -> R.drawable.ic_objects
                }
            )

            binding.sendBtn.setOnClickListener {
                onSendClick(dataItem.date)
            }
            binding.tickBtn.setOnClickListener {
                onTickClick(dataItem)
            }
            itemView.setOnClickListener {
                onContainerClick(dataItem)
            }
        }

    }

    class DebtDiffUtilCallback(private val oldList: List<Debt>, private val newList: List<Debt>) :
        DiffUtil.Callback() {
        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition].date == newList[newItemPosition].date

        override fun getOldListSize(): Int = oldList.size

        override fun getNewListSize(): Int = newList.size

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
            oldList[oldItemPosition] === newList[newItemPosition]
    }
}