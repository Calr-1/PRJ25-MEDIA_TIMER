package com.example.mediatimerjp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mediatimerjp.databinding.FragmentGroupOptionsBinding
import com.example.mediatimerjp.model.TimerCommon

class GroupOptionsFragment : Fragment() {

    private lateinit var group : TimerCommon
    private lateinit var binding: FragmentGroupOptionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentGroupOptionsBinding.inflate(layoutInflater, container, false)

        group = arguments?.getParcelable("timer")!!

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(group.group!!.sequence) binding.enableSequenceCB.isChecked = true
        if(group.group!!.skipGroups) binding.enableIgnoreGroupsCB.isChecked = true
        if(group.group!!.propagateTheme) binding.enablePropagateCB.isChecked = true
        if(group.group!!.stopAtZero) binding.enableSkipZerosCB.isChecked = true

        binding.enableSequenceCB.setOnClickListener{
            group.group!!.sequence = binding.enableSequenceCB.isChecked
        }
        binding.enableIgnoreGroupsCB.setOnClickListener{
            group.group!!.skipGroups = binding.enableIgnoreGroupsCB.isChecked
        }
        binding.enablePropagateCB.setOnClickListener{
            group.group!!.propagateTheme = binding.enablePropagateCB.isChecked
        }
        binding.enableSkipZerosCB.setOnClickListener{
            group.group!!.stopAtZero = binding.enableSkipZerosCB.isChecked
        }




    }

}