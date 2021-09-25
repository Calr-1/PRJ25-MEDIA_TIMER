package com.example.mediatimerjp.ui

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.mediatimerjp.R
import com.example.mediatimerjp.database.Database
import com.example.mediatimerjp.databinding.FragmentMainBinding
import com.example.mediatimerjp.model.TimerCommon
import com.example.mediatimerjp.model.TimerWrapper


class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private var wrapper: TimerWrapper = TimerWrapper.getInstance()
    private val viewModel = wrapper.currentModel
    private lateinit var array:ArrayList<TimerCommon>
    private var simpleCallBack = object: ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP.or(
            ItemTouchHelper.DOWN.or(ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT
            ))),0){
        private var startPosition = 0
        private var endPosition = 0
        private lateinit var recyclerView: RecyclerView
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            startPosition = viewHolder.adapterPosition
            endPosition = target.adapterPosition
            recyclerView.adapter?.notifyItemMoved(startPosition, endPosition)
            return true
        }

        override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
            super.onSelectedChanged(viewHolder, actionState)
            when (actionState) {
                ItemTouchHelper.ACTION_STATE_DRAG ->
                    Log.d("DragTest","Start to drag: $actionState")
                ItemTouchHelper.ACTION_STATE_SWIPE ->
                    Log.d("DragTest","Start to swipe: $actionState")
                ItemTouchHelper.ACTION_STATE_IDLE -> {
                    if (viewModel.timerList.value?.get(endPosition)?.type  == "timer") {
                        viewModel.swapTimers(startPosition, endPosition)
                        val adapter = TimerAdapter()
                        binding.timerList.adapter = adapter
                        viewModel.timerAdapter = adapter
                        subscribeUi(adapter)
                    }
                    else{
                        //meter o timer dentro do grupo, sendo que timer 2 e o grupo
                        var timer1 = viewModel.timerList.value?.get(startPosition)
                        var timer2 = viewModel.timerList.value?.get(endPosition)
                    }
                }
            }
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            TODO("Not yet implemented")
        }
    }

    override fun onResume() {
        super.onResume()
        TimerWrapper.getInstance().isOnMain = true

        val adapter = TimerAdapter()
        binding.timerList.adapter = adapter
        viewModel.timerAdapter = adapter
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
        subscribeUi(adapter)
        val itemTouchHelper = ItemTouchHelper(simpleCallBack)
        itemTouchHelper.attachToRecyclerView(binding.timerList)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        wrapper.currentModel = viewModel
        wrapper.isOnMain = true
        binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        val timers = context?.let {wrapper.getSavedObjectFromPreference(it,"SaveFile", "timers")}
        if(wrapper.currentModel.listOfTimers.size==0)wrapper.currentModel.setSavedTimers(timers)

        Log.e("getFromFirebase", "antes")
        if(wrapper.currentModel.timerCount==0 && wrapper.currentModel.groupCount==0){
            val timers = Database.getUser().timers
            Log.e("getFromFirebase", timers.size.toString())
            if(timers.size == 0){
                activity?.applicationContext?.let { wrapper.currentModel.getTimers(it) }
            }
            else{
                timers.forEach {
                    context?.let { it1 -> Database.getTimer(it, it1) }
                }
            }
        }
        val adapter = TimerAdapter()
        binding.timerList.adapter = adapter
        viewModel.timerAdapter = adapter
        subscribeUi(adapter)

        Log.e("LIST OF TIMERS " , TimerWrapper.getInstance().currentModel.timerList.value?.size.toString())

        registerForContextMenu(binding.createSelector)
        binding.createSelector.setOnClickListener {
            if(TimerWrapper.getInstance().selectedTimer==null)
                activity?.openContextMenu(binding.createSelector) }
        binding.uploadSelector.setOnClickListener{
            if(TimerWrapper.getInstance().selectedTimer==null){
                findNavController().navigate(
                    R.id.action_mainFragment_to_uploadFragment)}
        }


        binding.favouriteSelector.setOnClickListener {
            if (wrapper.mainFavSelected){
                viewModel.resetFavourites()
                binding.imageViewFav.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                wrapper.mainFavSelected=false
            }
            else{
                viewModel.listFavorites()
                binding.imageViewFav.setImageResource(R.drawable.ic_baseline_favorite_24)
                wrapper.mainFavSelected=true
            }

        }
        if (wrapper.mainFavSelected){
            viewModel.resetFavourites()
            binding.imageViewFav.setImageResource(R.drawable.ic_baseline_favorite_border_24)
        }
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)



        return binding.root
    }


    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menu.clear()
        activity?.menuInflater?.inflate(R.menu.choose_type_menu, menu)

    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.create_timer_option -> {
                activity?.let {
                    viewModel.addTimer("timer", it.applicationContext)
                    return true
                }
            }
            R.id.create_group_option -> {
                activity?.let {
                    viewModel.addTimer("group", it.applicationContext)
                    return true
                }
            }
        }
        return super.onContextItemSelected(item)
    }

    override fun onStop() {
        super.onStop()
        //context?.let { TimerWrapper.getInstance().saveObjectToSharedPreference(it, "SavedTimers", "timers",TimerWrapper.getInstance().currentModel.getTimersToSave()) }
        //context?.let { wrapper.saveTimersToSharedPreference(it, "TimersSaveFile") }
        context?.let {TimerWrapper.getInstance().saveTimersToSharedPreference(it, "SaveFile")}
    }


    private fun subscribeUi(adapter: TimerAdapter) {
        viewModel.timerList.observe(viewLifecycleOwner, {
            adapter.submitList(viewModel.timerList.value?.toList())
        })
    }


}