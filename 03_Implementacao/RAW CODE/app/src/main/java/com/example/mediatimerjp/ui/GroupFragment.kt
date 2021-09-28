package com.example.mediatimerjp.ui

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.mediatimerjp.R
import com.example.mediatimerjp.databinding.FragmentGroupBinding
import com.example.mediatimerjp.model.TimerWrapper


class GroupFragment : Fragment() {

    private lateinit var binding: FragmentGroupBinding
    private val viewModel: GroupViewModel by viewModels()
    private val wrapper = TimerWrapper()
    private var simpleCallBack = object: ItemTouchHelper.SimpleCallback(
        ItemTouchHelper.UP.or(
            ItemTouchHelper.DOWN.or(
                ItemTouchHelper.LEFT.or(
                    ItemTouchHelper.RIGHT
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
                        viewModel.swapTimers(startPosition, endPosition)
                        val adapter = TimerAdapter()
                        binding.timerList.adapter = adapter
                        viewModel.timerAdapter = adapter
                        subscribeUi(adapter)

                }
            }
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            TODO("Not yet implemented")
        }
    }


    override fun onResume() {
        super.onResume()
        TimerWrapper.getInstance().currentModel.getTimersToSave()
        TimerWrapper.getInstance().isOnMain = false
        val adapter = TimerAdapter()
        binding.timerList.adapter = adapter
        viewModel.timerAdapter = adapter
        subscribeUi(adapter)
        val itemTouchHelper = ItemTouchHelper(simpleCallBack)
        itemTouchHelper.attachToRecyclerView(binding.timerList)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        TimerWrapper.getInstance().isOnMain = false
        TimerWrapper.getInstance().currentGroup = viewModel
        binding = FragmentGroupBinding.inflate(layoutInflater, container, false)
        viewModel.group = arguments?.getParcelable("timer")!!
        binding.groupNameFrag.setText(viewModel.group?.name)

        if (viewModel.group?.group?.timersAssociated?.size != 0) {
            viewModel.setLists()
        }
        val view = binding.root

        val adapter = TimerAdapter()
        binding.timerList.adapter = adapter
        viewModel.timerAdapter = adapter
        subscribeUi(adapter)


        registerForContextMenu(binding.createSelector)
        binding.createSelector.setOnClickListener {
            if(TimerWrapper.getInstance().selectedTimer==null)
                activity?.openContextMenu(binding.createSelector)
        }

        binding.favouriteSelector.setOnClickListener {
            if (wrapper.mainFavSelected){
                viewModel.resetFavourites()
                binding.imageView.setImageResource(R.drawable.ic_baseline_favorite_border_24)
                wrapper.mainFavSelected=false
            }
            else{
                viewModel.listFavorites()
                binding.imageView.setImageResource(R.drawable.ic_baseline_favorite_24)
                wrapper.mainFavSelected=true
            }

        }
        binding.inputSelector.setOnClickListener {
            val navController = Navigation.findNavController(binding.root)
            val bundle = bundleOf("timer" to viewModel.group)
            navController.navigate(
                R.id.action_groupFragment_to_groupOptionsFragment,
                bundle)

        }
        if (wrapper.mainFavSelected){
            viewModel.resetFavourites()
            binding.imageView.setImageResource(R.drawable.ic_baseline_favorite_border_24)
        }
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)



        return view
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
        viewModel.group!!.name = binding.groupNameFrag.text.toString()
        viewModel.group!!.nameTextView.text = binding.groupNameFrag.text.toString()
        viewModel.group!!.observableName.value = binding.groupNameFrag.text.toString()
        context?.let {TimerWrapper.getInstance().saveTimersToSharedPreference(it, "SaveFile")}
    }

    private fun subscribeUi(adapter: TimerAdapter) {
        viewModel.timerList.observe(viewLifecycleOwner, {
            adapter.submitList(viewModel.timerList.value?.toList())
        })
    }

}