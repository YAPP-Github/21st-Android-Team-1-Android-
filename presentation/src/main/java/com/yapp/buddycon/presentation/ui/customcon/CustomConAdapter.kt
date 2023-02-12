package com.yapp.buddycon.presentation.ui.customcon

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.yapp.buddycon.domain.model.CouponItem
import com.yapp.buddycon.presentation.R
import com.yapp.buddycon.presentation.databinding.ItemCouponBinding
import java.time.LocalDate
import java.time.Period

class CustomConAdapter : PagingDataAdapter<CouponItem, CustomConAdapter.CustommConViewHoler>(CUSTOMCON_DIFF_CALLBACK) {

    override fun onBindViewHolder(holder: CustommConViewHoler, position: Int) {
        getItem(position)?.let {
            holder.bind(it)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustommConViewHoler {
        return CustommConViewHoler(
            ItemCouponBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    class CustommConViewHoler(
        private val binding: ItemCouponBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(info: CouponItem) {
            binding.itemCouponTvTitle.text = info.name
            binding.itemTvExpirationPeriod.text = "~${info.expireDate.replace("-", ".")}"
            if (info.usable) {
                val (year, month, day) = info.expireDate.split("-").map { it.toInt() }
                binding.btnExpireDate.isVisible = true
                binding.btnExpireDate.text = "D${Period.between(LocalDate.now(), LocalDate.of(year, month, day)).days}"
            } else {
                binding.btnExpireDate.isVisible = false
            }

            binding.tvNoshared.isVisible = info.shared.not()

            Glide.with(binding.ivCoupon.context)
                .load(info.imageUrl)
                .placeholder(R.drawable.img_theme1)
                .into(binding.ivCoupon)
        }
    }


    companion object {
        private val CUSTOMCON_DIFF_CALLBACK = object : DiffUtil.ItemCallback<CouponItem>() {
            override fun areItemsTheSame(oldItem: CouponItem, newItem: CouponItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: CouponItem, newItem: CouponItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}