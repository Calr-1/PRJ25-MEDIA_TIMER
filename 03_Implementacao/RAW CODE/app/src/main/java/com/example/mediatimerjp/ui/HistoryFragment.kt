package com.example.mediatimerjp.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.mediatimerjp.R
import com.example.mediatimerjp.databinding.FragmentHistoryBinding
import com.example.mediatimerjp.model.TimerCommon
import com.example.mediatimerjp.model.TimerWrapper
import com.github.mikephil.charting.charts.ScatterChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries


class HistoryFragment : Fragment() {

    private lateinit var chart: ScatterChart
    lateinit var series: LineGraphSeries<DataPoint>

    private lateinit var wrapper: TimerWrapper
    private lateinit var binding: FragmentHistoryBinding
    private lateinit var timer: TimerCommon

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        wrapper = TimerWrapper.getInstance()
        binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timer = arguments?.getParcelable("timer")!!
        val xAxisName = TextView(activity)
        xAxisName.text = "Date"
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params.gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
        params.setMargins(0, 0, 0, 20)

        val yAxisName = VerticalTextView(activity, null)
        yAxisName.text = "Seconds"
        val params2 = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        params2.gravity = Gravity.RIGHT or Gravity.CENTER_VERTICAL

        binding.root.addView(xAxisName, params)
        binding.root.addView(yAxisName, params2)
        chart = binding.chart
        scatterChartData()
    }

    @SuppressLint("ResourceAsColor")
    private fun scatterChartData() {
        val scatterEntry = ArrayList<Entry>()
        val xval = ArrayList<String>()
        val dates = timer.timer?.arrayDatesHistory
        val times = timer.timer?.arrayTimesHistory
        if (dates != null && times != null && dates.size > 0 && times.size > 0) {
            for (i in 0 until times.size) {
                scatterEntry.add(Entry((times[i] / 1000).toFloat(), i))
                xval.add("${dates[i][0]}/${dates[i][1] + 1}/${dates[i][2]}")
            }
            val scatterDataSet = ScatterDataSet(scatterEntry, "time (seconds)")
            scatterDataSet.color = resources.getColor(R.color.teal_700)
            scatterDataSet.scatterShape = ScatterChart.ScatterShape.CIRCLE
            val xAxis = chart.xAxis
            xAxis.position = XAxis.XAxisPosition.BOTTOM

            val scatterData = ScatterData(xval, scatterDataSet)
            chart.data = scatterData
            chart.setBackgroundColor(resources.getColor(R.color.white))
            chart.animateXY(1000, 1000)
        }
    }
}