package com.yapp.buddycon.presentation.ui.giftcon

import android.content.Context
import android.content.Intent
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.os.Bundle
import androidx.activity.viewModels
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.yapp.buddycon.domain.model.GiftConDetail
import com.yapp.buddycon.presentation.R
import com.yapp.buddycon.presentation.base.BaseActivity
import com.yapp.buddycon.presentation.databinding.ActivityGiftConDetailBinding
import com.yapp.buddycon.presentation.ui.common.dialog.CouponDeleteDialogFragment
import com.yapp.buddycon.presentation.ui.common.dialog.CouponExpireDialogFragment
import com.yapp.buddycon.presentation.utils.getDday
import com.yapp.buddycon.presentation.utils.toPx
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.util.*

@AndroidEntryPoint
class GiftConDetailActivity : BaseActivity<ActivityGiftConDetailBinding>(R.layout.activity_gift_con_detail) {
    private val giftConDetailViewModel: GiftConDetailViewModel by viewModels()
    private val giftconId by lazy { intent?.getIntExtra(GIFTCON_ID, 0) ?: 0 }
    private val giftUsable by lazy { intent?.getBooleanExtra(GIFTCON_USABLE, false) ?: false }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.appBar.ibnAppbarBack.setOnClickListener { finish() }
        binding.btnCouponDelete.setOnClickListener {
            CouponDeleteDialogFragment(
                title = "쿠폰을 삭제할까요?",
                description = "삭제하면 앱에서 완전히 사라져요"
            ) { giftConDetailViewModel.deleteCoupon(giftconId) }
                .show(supportFragmentManager, null)
        }

        giftConDetailViewModel.getGiftconDetailInfo(giftconId)
        observeGiftConDetail()
        observeGiftConUserEvent()
    }

    private fun observeGiftConDetail() {
        giftConDetailViewModel.couponDetailState
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { showCouponInfo(it) }
            .launchIn(lifecycleScope)
    }

    private fun observeGiftConUserEvent() {
        giftConDetailViewModel.giftConUserEvent
            .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
            .onEach { event ->
                when (event) {
                    GiftConUserEvent.Delete -> {
                        // TODO : 삭제 이후 UI 처리
                        finish()
                    }
                    else -> Unit
                }
            }.launchIn(lifecycleScope)
    }

    private fun showCouponInfo(giftConDetail: GiftConDetail) {
        giftConDetail.also {
            initCouponeImage(it)
            initCouponDescription(it)
            checkMoneyCoupon(it)
            initEventButton(it)
            checkCouponUsable()
        }
    }

    private fun initCouponeImage(giftConDetail: GiftConDetail) = with(binding) {
        Glide.with(ivCoupon.context)
            .load(giftConDetail.imageUrl)
            .into(ivCoupon)

        binding.btnVolumeUp.setOnClickListener {
            GiftConImageDialogFragment(giftConDetail.imageUrl).show(supportFragmentManager, null)
        }

        if (giftUsable) {
            val (year, month, day) = giftConDetail.expireDate.split("-").map { it.toInt() }
            val diff = Calendar.getInstance().getDday(year, month, day)

            if (diff in 0..14) {
                binding.btnExpireDate.isVisible = true
                binding.btnExpireDate.text = "D-${diff}"
                binding.btnExpireDate.setBackgroundResource(
                    if (diff <= 7) R.drawable.bg_coupon_expire_date
                    else R.drawable.bg_coupon_gray_expire_date
                )
            } else {
                if (diff < 0) {
                    CouponExpireDialogFragment(
                        title = getString(R.string.giftcon_expire_date_message_title),
                        description = getString(R.string.giftcon_expire_date_message_description)
                    ).show(supportFragmentManager, null)
                }
                binding.btnExpireDate.isVisible = false
            }

            binding.btnUsedBadge.isVisible = false
            binding.vDim.isVisible = false
            binding.ivCoupon.colorFilter = null
            binding.btnVolumeUp.isVisible = true
        } else {
            binding.btnExpireDate.isVisible = false
            binding.btnUsedBadge.isVisible = true
            binding.btnVolumeUp.isVisible = false
            binding.vDim.isVisible = true
            binding.ivCoupon.colorFilter =
                ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0F) })
        }
    }

    private fun initCouponDescription(giftConDetail: GiftConDetail) = with(binding) {
        val (year, month, day) = giftConDetail.expireDate.split("-").map { it.toInt() }

        tvCouponTitle.setText(giftConDetail.name)
        tvExpirationDateInfo.setText("${year}년 ${month}월 ${day}일")
        tvUsePlaceInfo.setText(giftConDetail.storeName)
        tvMemoInfo.setText(giftConDetail.memo)
    }

    private fun checkMoneyCoupon(giftConDetail: GiftConDetail) = with(binding) {
        switchPriceCoupone.isChecked = giftConDetail.isMoneyCoupon
        clSparePrice.isVisible = giftConDetail.isMoneyCoupon
        vBorder4.isVisible = giftConDetail.isMoneyCoupon.not()

        if (giftConDetail.isMoneyCoupon.not()) {
            val layoutParam = (btnCouponDelete.layoutParams as ConstraintLayout.LayoutParams)
            layoutParam.apply {
                topToBottom = vBorder4.id
                topMargin = 10.toPx(this@GiftConDetailActivity).toInt()
            }
        }
    }

    private fun initEventButton(giftConDetail: GiftConDetail) = with(binding) {
        val (year, month, day) = giftConDetail.expireDate.split("-").map { it.toInt() }
        val diff = Calendar.getInstance().getDday(year, month, day)

        binding.btnUseComplete.isVisible = giftUsable
        binding.btnMake.isVisible = giftUsable
        binding.btnMake.setBackgroundColor(
            if (diff >= 0) getColor(R.color.skb_blue)
            else getColor(R.color.gray40)
        )
        binding.btnRollback.isVisible = giftUsable.not()
    }

    private fun checkCouponUsable() = with(binding) {
        tvCouponTitle.isEnabled = giftUsable
        tvExpirationDateInfo.isEnabled = giftUsable
        tvUsePlaceInfo.isEnabled = giftUsable
        tvMemoInfo.isEnabled = giftUsable
        switchPriceCoupone.isEnabled = giftUsable
    }

    companion object {
        const val GIFTCON_ID = "GIFTCON_ID"
        const val GIFTCON_USABLE = "GIFTCON_USABLE"

        fun newIntent(context: Context, giftconId: Int, usable: Boolean) =
            Intent(context, GiftConDetailActivity::class.java).apply {
                putExtra(GIFTCON_ID, giftconId)
                putExtra(GIFTCON_USABLE, usable)
            }
    }
}