package com.example.mediatimerjp.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Build
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.mediatimerjp.R
import com.example.mediatimerjp.databinding.TimerItemBinding
import com.example.mediatimerjp.model.TimerCommon
import com.example.mediatimerjp.model.TimerWrapper
import java.util.*

class TimerAdapter :
    androidx.recyclerview.widget.ListAdapter<TimerCommon, RecyclerView.ViewHolder>(PlantDiffCallback()) {
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return TimerViewHolder(
            TimerItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val plant = getItem(position)
        (holder as TimerViewHolder).bind(plant)
    }


    @RequiresApi(Build.VERSION_CODES.Q)
    class TimerViewHolder(
        private val binding: TimerItemBinding
    ) : RecyclerView.ViewHolder(binding.root), PopupMenu.OnMenuItemClickListener {
        private lateinit var currentTimer: TimerCommon
        private var wrapper: TimerWrapper = TimerWrapper.getInstance()

        init {
            binding.restartButton.tag = "restart"
            binding.playAndPauseGroupButton.tag = "action"
            binding.favouriteTimerSelector.tag = "favourite"
            binding.clickableOverlay.tag = "overlay"
            binding.menuProgressBar.tag = "notCickable"
            binding.selectorOverlay.tag = "selected"
            binding.setClickListener { view ->
                run {
                    if (view.tag == "selected") {
                        TimerWrapper.getInstance().selectedTimer?.let {
                            if (binding.timers?.group?.addTimer(it) == true) {
                                if (TimerWrapper.getInstance().isOnMain)
                                    TimerWrapper.getInstance().currentModel.resetAdapter()
                                else
                                    TimerWrapper.getInstance().currentGroup.resetAdapter()
                            }
                        }


                    } else if (view.tag == "overlay") {
                    } else if (view.tag == "restart") binding.timers?.restartTimer()
                    else if (view.tag == "action") binding.timers?.doAction()
                    else if (view.tag == "favourite") {
                        binding.timers?.changeFavourite()
                        if (binding.timers?.favourite == true) {
                            binding.favouriteTimerSelector.setImageResource(R.drawable.ic_baseline_favorite_24)
                        } else {
                            binding.favouriteTimerSelector.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                        }
                    } else if (view.tag == "notClickable") {
                    } else {
                        binding.timers?.let { timer ->
                            if (timer.type == "timer") {

                                val navController = Navigation.findNavController(binding.root)
                                val bundle = bundleOf("timer" to timer)
                                if (TimerWrapper.getInstance().isOnMain)
                                    navController.navigate(
                                        R.id.action_mainFragment_to_simpleTimerFragment,
                                        bundle
                                    )
                                else
                                    navController.navigate(
                                        R.id.action_groupFragment_to_simpleTimerFragment,
                                        bundle
                                    )
                            } else if (timer.type == "group") {
                                val navController = Navigation.findNavController(binding.root)
                                val bundle = bundleOf("timer" to timer)
                                if (TimerWrapper.getInstance().isOnMain)
                                    navController.navigate(
                                        R.id.action_mainFragment_to_groupFragment,
                                        bundle
                                    )
                                else
                                    navController.navigate(
                                        R.id.action_groupFragment_to_groupFragment,
                                        bundle
                                    )
                            }
                        }
                    }
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        fun bind(item: TimerCommon) {
            binding.apply {
                item.bigTextView = binding.hoursEditView
                item.mediumTextView = binding.minutesEditView
                item.smallTextView = binding.secondsEditView
                item.progressBar = binding.progressBar
                item.nameTextView = binding.timerFragName
                item.playPause = binding.playAndPauseGroupButton
                item.context = binding.root.context
                val menu = binding.MenuButton
                menu.setOnClickListener {
                    showMenu(it, binding.root.context, item)
                }
                if (item.favourite) {
                    binding.favouriteTimerSelector.setImageResource(R.drawable.ic_baseline_favorite_24)
                } else {
                    binding.favouriteTimerSelector.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                }
                if (item.type == "timer") {
                    item.updateView(item.type, item.timer!!.finished)
                    if (item.timer!!.enableProgressiveTimer) binding.typeOfTimer.setImageResource(R.drawable.ic_progressivetimer)
                    else binding.typeOfTimer.visibility = View.INVISIBLE
                } else {
                    item.updateView(item.type, true)
                    binding.typeOfTimer.setImageResource(R.drawable.ic_group)
                }
                if (item.type == "group") {
                    for (it in item.group?.timersAssociated!!) {
                        it.context = binding.root.context
                    }

                }
                if (item.timer?.timerRunning == true) {
                    item.timer?.startFromWhereItLeftOff(binding.root.context, item)
                    item.alterButton(1)
                } else if (item.timer?.finished == true) {
                    item.alterButton(2)
                    binding.progressBar.progress = 100
                }
                timers = item

                if (item.active) {
                    binding.inactiveOverlay.visibility = View.INVISIBLE
                    binding.clickableOverlay.visibility = View.INVISIBLE
                } else {
                    binding.inactiveOverlay.visibility = View.VISIBLE
                    binding.clickableOverlay.visibility = View.VISIBLE
                }
                setColors(item)
                executePendingBindings()
            }
        }

        fun showMenu(view: View, context: Context, timer: TimerCommon) {
            val popupMenu = PopupMenu(context, view) //View will be an anchor for PopupMenu
            if (timer.groupAssociated == null) {
                if(timer.type=="timer") popupMenu.inflate(R.menu.timer_menu)
                else popupMenu.inflate(R.menu.group_menu)
            }
            else {
                if (timer.type=="timer") popupMenu.inflate(R.menu.timer_menu_group)
                else popupMenu.inflate(R.menu.group_menu_group)
            }
            currentTimer = timer
            popupMenu.setOnMenuItemClickListener(this)
            popupMenu.show()
        }

        fun setColors(item: TimerCommon) {
            if (item.type == "timer") {
                val texts: ArrayList<Any> = ArrayList<Any>()
                texts.add(binding.timerFragName)
                texts.add(binding.separator1)
                texts.add(binding.separator2)
                texts.add(binding.hoursEditView)
                texts.add(binding.minutesEditView)
                texts.add(binding.secondsEditView)
                texts.add(binding.indicator)

                var color: Int = item.timerColors.textColor
                var hexColor = String.format("#%06X", 0xFFFFFF and color)

                for (tx in texts) {
                    (tx as TextView).setTextColor(Color.parseColor(hexColor))
                    tx.setHintTextColor(Color.parseColor(hexColor))
                }
                color = item.timerColors.backgroundColor
                hexColor = String.format("#%06X", 0xFFFFFF and color)
                val bg: LinearLayout = binding.frameLayout
                bg.background.setTint(Color.parseColor(hexColor))

                color = item.timerColors.buttonBackground
                hexColor = String.format("#%06X", 0xFFFFFF and color)

                val restartButton: ImageButton =
                    binding.restartButton
                val actionButton: ImageButton =
                    binding.playAndPauseGroupButton
                val optionsButton: ImageButton =
                    binding.MenuButton

                restartButton.background.setColorFilter(
                    Color.parseColor(hexColor),
                    PorterDuff.Mode.MULTIPLY
                )
                actionButton.background.setColorFilter(
                    Color.parseColor(hexColor),
                    PorterDuff.Mode.MULTIPLY
                )
                optionsButton.background.setColorFilter(
                    Color.parseColor(hexColor),
                    PorterDuff.Mode.MULTIPLY
                )


                color = item.timerColors.buttonIconColor
                hexColor = String.format("#%06X", 0xFFFFFF and color)

                restartButton.setColorFilter(Color.parseColor(hexColor))
                actionButton.setColorFilter(Color.parseColor(hexColor))
                optionsButton.setColorFilter(Color.parseColor(hexColor))

                color = item.timerColors.favouriteColor
                hexColor = String.format("#%06X", 0xFFFFFF and color)

                val favouriteB: ImageView = binding.favouriteTimerSelector
                favouriteB.setColorFilter(Color.parseColor(hexColor))
                var themeId = TimerWrapper.getInstance().getThemeID(binding.root.context)
                if(themeId == 0){
                    binding.progressBar.progressDrawable = binding.root.context.getDrawable(R.drawable.progress_vertical)
                }
                else if(themeId == 1){
                    binding.progressBar.progressDrawable = binding.root.context.getDrawable(R.drawable.progress_orange)
                }
                else if(themeId == 2){
                    binding.progressBar.progressDrawable = binding.root.context.getDrawable(R.drawable.progress_green)
                }
                else if(themeId == 3 || themeId == 4){
                    binding.progressBar.progressDrawable = binding.root.context.getDrawable(R.drawable.progress_red)
                }
            } else {
                if (item.group!!.propagateTheme) {

                    val texts: ArrayList<Any> = ArrayList<Any>()
                    texts.add(binding.timerFragName)
                    texts.add(binding.separator1)
                    texts.add(binding.separator2)
                    texts.add(binding.hoursEditView)
                    texts.add(binding.minutesEditView)
                    texts.add(binding.secondsEditView)
                    texts.add(binding.indicator)

                    var color: Int = item.timerColors.textColor
                    var hexColor = String.format("#%06X", 0xFFFFFF and color)

                    for (tx in texts) {
                        (tx as TextView).setTextColor(Color.parseColor(hexColor))
                        tx.setHintTextColor(Color.parseColor(hexColor))
                    }
                    color = item.timerColors.backgroundColor
                    hexColor = String.format("#%06X", 0xFFFFFF and color)
                    val bg: LinearLayout = binding.frameLayout
                    bg.background.setTint(Color.parseColor(hexColor))

                    color = item.timerColors.buttonBackground
                    hexColor = String.format("#%06X", 0xFFFFFF and color)

                    val restartButton: ImageButton =
                        binding.restartButton
                    val actionButton: ImageButton =
                        binding.playAndPauseGroupButton
                    val optionsButton: ImageButton =
                        binding.MenuButton

                    restartButton.background.setColorFilter(
                        Color.parseColor(hexColor),
                        PorterDuff.Mode.MULTIPLY
                    )
                    actionButton.background.setColorFilter(
                        Color.parseColor(hexColor),
                        PorterDuff.Mode.MULTIPLY
                    )
                    optionsButton.background.setColorFilter(
                        Color.parseColor(hexColor),
                        PorterDuff.Mode.MULTIPLY
                    )

                    color = item.timerColors.buttonIconColor
                    hexColor = String.format("#%06X", 0xFFFFFF and color)

                    restartButton.setColorFilter(Color.parseColor(hexColor))
                    actionButton.setColorFilter(Color.parseColor(hexColor))
                    optionsButton.setColorFilter(Color.parseColor(hexColor))



                    color = item.timerColors.favouriteColor
                    hexColor = String.format("#%06X", 0xFFFFFF and color)

                    val favouriteB: ImageView = binding.favouriteTimerSelector
                    favouriteB.setColorFilter(Color.parseColor(hexColor))
                    for (tm in item.group!!.timersAssociated) {
                        tm.timerColors = item.timerColors
                    }
                    var themeId = TimerWrapper.getInstance().getThemeID(binding.root.context)

                    if(themeId == 0){
                        binding.progressBar.progressDrawable = binding.root.context.getDrawable(R.drawable.progress_vertical)
                    }
                    else if(themeId == 1){
                        binding.progressBar.progressDrawable = binding.root.context.getDrawable(R.drawable.progress_orange)
                    }
                    else if(themeId == 2){
                        binding.progressBar.progressDrawable = binding.root.context.getDrawable(R.drawable.progress_green)
                    }
                    else if(themeId == 3 || themeId == 4){
                        binding.progressBar.progressDrawable = binding.root.context.getDrawable(R.drawable.progress_red)
                    }
                } else {
                    val texts: ArrayList<Any> = ArrayList<Any>()
                    texts.add(binding.timerFragName)
                    texts.add(binding.separator1)
                    texts.add(binding.separator2)
                    texts.add(binding.hoursEditView)
                    texts.add(binding.minutesEditView)
                    texts.add(binding.secondsEditView)
                    texts.add(binding.indicator)

                    var color: Int = item.timerColors.textColor
                    var hexColor = String.format("#%06X", 0xFFFFFF and color)

                    for (tx in texts) {
                        (tx as TextView).setTextColor(Color.parseColor(hexColor))
                        tx.setHintTextColor(Color.parseColor(hexColor))
                    }
                    color = item.timerColors.backgroundColor
                    hexColor = String.format("#%06X", 0xFFFFFF and color)
                    val bg: LinearLayout = binding.frameLayout
                    bg.background.setTint(Color.parseColor(hexColor))

                    color = item.timerColors.buttonBackground
                    hexColor = String.format("#%06X", 0xFFFFFF and color)

                    val restartButton: ImageButton =
                        binding.restartButton
                    val actionButton: ImageButton =
                        binding.playAndPauseGroupButton
                    val optionsButton: ImageButton =
                        binding.MenuButton

                    restartButton.background.setColorFilter(
                        Color.parseColor(hexColor),
                        PorterDuff.Mode.MULTIPLY
                    )
                    actionButton.background.setColorFilter(
                        Color.parseColor(hexColor),
                        PorterDuff.Mode.MULTIPLY
                    )
                    optionsButton.background.setColorFilter(
                        Color.parseColor(hexColor),
                        PorterDuff.Mode.MULTIPLY
                    )


                    color = item.timerColors.buttonIconColor
                    hexColor = String.format("#%06X", 0xFFFFFF and color)

                    restartButton.setColorFilter(Color.parseColor(hexColor))
                    actionButton.setColorFilter(Color.parseColor(hexColor))
                    optionsButton.setColorFilter(Color.parseColor(hexColor))

                    color = item.timerColors.favouriteColor
                    hexColor = String.format("#%06X", 0xFFFFFF and color)

                    val favouriteB: ImageView = binding.favouriteTimerSelector
                    favouriteB.setColorFilter(Color.parseColor(hexColor))
                    var themeId = TimerWrapper.getInstance().getThemeID(binding.root.context)
                    if(themeId == 0){
                        binding.progressBar.progressDrawable = binding.root.context.getDrawable(R.drawable.progress_vertical)
                    }
                    else if(themeId == 1){
                        binding.progressBar.progressDrawable = binding.root.context.getDrawable(R.drawable.progress_orange)
                    }
                    else if(themeId == 2){
                        binding.progressBar.progressDrawable = binding.root.context.getDrawable(R.drawable.progress_green)
                    }
                    else if(themeId == 3 || themeId == 4){
                        binding.progressBar.progressDrawable = binding.root.context.getDrawable(R.drawable.progress_red)
                    }

                }
            }
            if (item.selecting) {
                binding.selectorOverlay.visibility = View.VISIBLE
                if (item.group?.timersAssociated?.contains(wrapper.selectedTimer) == true) {
                    binding.selectorOverlay.setBackgroundColor(Color.parseColor("#aaaa0000"))
                } else {
                    binding.selectorOverlay.setBackgroundColor(Color.parseColor("#aa00aa00"))
                    item.group?.timersAssociated?.forEach {
                        if (it.id == wrapper.selectedTimer?.id) {
                            binding.selectorOverlay.setBackgroundColor(Color.parseColor("#aaaa0000"))
                            return
                        }
                    }
                }
            } else
                binding.selectorOverlay.visibility = View.INVISIBLE
        }

        override fun onMenuItemClick(item: MenuItem?): Boolean {
            val navController = Navigation.findNavController(binding.root)
            if (item != null) {
                when (item.itemId) {
                    R.id.fullScreen -> {
                        val bundle = bundleOf("timer" to currentTimer)
                        if (currentTimer.groupAssociated == null) navController.navigate(
                            R.id.action_mainFragment_to_fullScreenTimerFragment,
                            bundle
                        )
                        else navController.navigate(
                            R.id.action_groupFragment_to_fullScreenTimerFragment,
                            bundle
                        )
                    }
                    R.id.share -> {
                        wrapper.currentModel.shareTimer(currentTimer)
                    }
                    R.id.delete -> {
                        if (wrapper.isOnMain)
                            wrapper.currentModel.removeTimer(currentTimer)
                        else
                            wrapper.currentGroup.removeTimer(currentTimer)

                    }
                    R.id.timerTheme -> {
                        val navController = Navigation.findNavController(binding.root)
                        val bundle = bundleOf("timer" to currentTimer)
                        if (TimerWrapper.getInstance().isOnMain) navController.navigate(
                            R.id.action_mainFragment_to_fragmentColorPicker,
                            bundle
                        )
                        else navController.navigate(
                            R.id.action_groupFragment_to_fragmentColorPicker,
                            bundle
                        )
                    }
                    R.id.activate -> {
                        currentTimer.active = !currentTimer.active
                        if (currentTimer.active) {
                            binding.inactiveOverlay.visibility = View.INVISIBLE
                            binding.clickableOverlay.visibility = View.INVISIBLE
                        } else {
                            binding.inactiveOverlay.visibility = View.VISIBLE
                            binding.clickableOverlay.visibility = View.VISIBLE
                        }
                    }
                    R.id.removeFromGroup -> {
                        val bundle = bundleOf("timer" to currentTimer)
                        val groupAssociated = currentTimer.groupAssociated?.group?.groupAssociated
                        currentTimer.groupAssociated?.group?.removeTimer(currentTimer)
                        if (groupAssociated == null) {
                            wrapper.currentModel.addTimer(currentTimer)
                            navController.navigate(
                                R.id.action_groupFragment_to_mainFragment,
                                bundle
                            )

                        } else {
                            groupAssociated.group?.addTimer(currentTimer)
                            navController.navigate(
                                R.id.action_groupFragment_to_groupFragment,
                                bundle
                            )
                        }
                    }
                    R.id.addToGroup -> {
                        TimerWrapper.getInstance().selectedTimer = binding.timers
                        if (TimerWrapper.getInstance().isOnMain)
                            TimerWrapper.getInstance().currentModel.filterGroups()
                        else
                            TimerWrapper.getInstance().currentGroup.filterGroups()
                    }
                    else -> {
                    }
                }
            }
            return false
        }
    }
}

private class PlantDiffCallback : DiffUtil.ItemCallback<TimerCommon>() {
    override fun areItemsTheSame(oldItem: TimerCommon, newItem: TimerCommon): Boolean {
        return oldItem.name == newItem.name
    }

    @SuppressLint("DiffUtilEquals")
    override fun areContentsTheSame(oldItem: TimerCommon, newItem: TimerCommon): Boolean {
        return oldItem == newItem
    }
}