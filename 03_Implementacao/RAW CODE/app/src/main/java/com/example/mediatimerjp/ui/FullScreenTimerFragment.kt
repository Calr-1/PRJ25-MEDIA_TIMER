package com.example.mediatimerjp.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.mediatimerjp.R
import com.example.mediatimerjp.databinding.FragmentFullScreenTimerBinding
import com.example.mediatimerjp.model.AlarmPlayer
import com.example.mediatimerjp.model.TimerCommon
import com.example.mediatimerjp.model.TimerWrapper
import com.squareup.picasso.Picasso
import java.lang.Exception


class FullScreenTimerFragment : Fragment() {


    private lateinit var wrapper: TimerWrapper
    private lateinit var binding: FragmentFullScreenTimerBinding
    private lateinit var timer: TimerCommon
    private lateinit var media: Uri

    private lateinit var timerImage: ImageView
    private lateinit var timerVideo: VideoView
    private lateinit var playPause:ImageButton
    private var soundPlayer:AlarmPlayer? = null
    private lateinit var upload:String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        wrapper = TimerWrapper.getInstance()
        binding = FragmentFullScreenTimerBinding.inflate(inflater, container, false)
        return binding.root

    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timer = arguments?.getParcelable("timer")!!
        media = timer.timer!!.media
        upload = timer.timer!!.upload
        binding.timerName.text = timer.name
        timerImage = binding.timerImage
        timerVideo = binding.timerVideo
        playPause = binding.playPauseButton
        timer.playPauseFullScreen = playPause
        if (media!= Uri.EMPTY) setMedia(media)
        timer.bigTextView = binding.hoursEditView
        timer.mediumTextView = binding.minutesEditView
        timer.smallTextView = binding.secondsEditView
        timer.updateView("timer", timer.timer!!.finished)
        binding.restartButton.setOnClickListener {
            timer.restartTimer()
        }
        binding.playPauseButton.setOnClickListener {
            timer.doAction()
        }
        if(timer.timer!!.timerRunning){
            timer.alterButton(1)
        }
        else if(timer.timer!!.finished ){
            timer.alterButton(2)
        }
        val color = timer.timerColors.buttonBackground
        val hexColor = String.format("#%06X", 0xFFFFFF and color)

        binding.restartButton.background.setColorFilter(
            Color.parseColor(hexColor),
            PorterDuff.Mode.MULTIPLY
        )
        binding.playPauseButton.background.setColorFilter(
            Color.parseColor(hexColor),
            PorterDuff.Mode.MULTIPLY
        )


    }

    private fun setMedia(media: Uri) {
        when {
            wrapper.isImageFile(media) -> {
                addImage()
            }
            wrapper.isVideoFile(media) -> {
                addVideo()
            }
            else -> {
                addSound()
            }
        }
    }

    private fun addSound() {
        binding.showVideo.visibility = View.GONE
        binding.showImage.visibility = View.GONE
        binding.showSound.visibility = View.VISIBLE
        timer.timer!!.context = requireContext()
        timer.timer!!.initializeBackgroundSound()
        timer.timer!!.setBackgroundSound(Uri.parse(upload))
        soundPlayer = timer.timer!!.backgroundSound
        initializeSeekBar()
        soundControls()
    }

    private fun soundControls(){
        binding.playButton.setOnClickListener {
            soundPlayer?.startSoundObject(false, "", 0, 0L, context)
        }
        binding.pauseButton.setOnClickListener {
            soundPlayer?.pauseSoundObject()
        }
        binding.stopButton.setOnClickListener {
            soundPlayer?.stopSoundObject(context)
        }
        binding.seekBar.setOnSeekBarChangeListener(@SuppressLint("AppCompatCustomView")
        object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) soundPlayer!!.setNewTime(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
        binding.backgroundSoundVolume.text = timer.timer!!.backgroundSoundVolume.toString()
        binding.backgroundSoundBar.max = 100
        binding.backgroundSoundBar.progress = timer.timer!!.backgroundSoundVolume
        binding.backgroundSoundBar.setOnSeekBarChangeListener(@SuppressLint("AppCompatCustomView")
        object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser){
                    soundPlayer!!.setVolume(progress)
                    timer.timer!!.backgroundSoundVolume = progress
                    binding.backgroundSoundVolume.text = progress.toString()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }

        })
    }

    private fun initializeSeekBar(){
        binding.seekBar.max = soundPlayer!!.getDuration()
        val handler = Handler()
        handler.postDelayed(object:Runnable{
            override fun run() {
                try {
                    binding.seekBar.progress = soundPlayer!!.getCurrentTime()
                    binding.timeLeft.text = wrapper.millisecondsToMinutesAndSeconds(soundPlayer!!.getDuration()-soundPlayer!!.getCurrentTime())
                    handler.postDelayed(this, 1000)
                }catch (e: Exception){
                    binding.seekBar.progress = 0
                }
            }

        }, 0)

    }

    private fun addImage() {
        binding.showVideo.visibility = View.GONE
        binding.showSound.visibility = View.GONE
        binding.showImage.visibility = View.VISIBLE
        applyPicassoUrl()
    }


    private fun applyPicassoUrl() {
        if (upload != "") {
            Picasso.get()
                .load(upload)
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .into(timerImage)
        }
    }

    private fun addVideo() {
        binding.showImage.visibility = View.GONE
        binding.showSound.visibility = View.GONE
        binding.showVideo.visibility = View.VISIBLE
        binding.videoVolume.text = timer.timer!!.backgroundSoundVolume.toString()
        binding.videoSoundBar.max = 100
        binding.videoSoundBar.progress = timer.timer!!.backgroundSoundVolume
        timerVideo.setVideoURI(Uri.parse(upload))
        timerVideo.setOnPreparedListener { mp ->
            mp.isLooping = true
            mp.start()
            mp.setVolume(timer.timer!!.backgroundSoundVolume.toFloat(),
                timer.timer!!.backgroundSoundVolume.toFloat()
            )
            //isPlaying = true
            binding.videoSoundBar.setOnSeekBarChangeListener(@SuppressLint("AppCompatCustomView")
            object: SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser){
                        timer.timer!!.backgroundSoundVolume = progress
                        binding.videoVolume.text = progress.toString()
                        mp.setVolume(timer.timer!!.backgroundSoundVolume.toFloat(),
                            timer.timer!!.backgroundSoundVolume.toFloat()
                        )
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                }

            })
        }
        val mediaController = MediaController(context)
        mediaController.setAnchorView(timerVideo)
        timerVideo.setMediaController(mediaController)
    }

    override fun onStop() {
        super.onStop()
        context?.let { wrapper.saveTimersToSharedPreference(it, "SaveFile") }
    }

}