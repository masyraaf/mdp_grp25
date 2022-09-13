package com.group6.mdpandroid.fragments

import android.content.ClipData
import android.content.ClipDescription
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import com.group6.mdpandroid.R

class ArenaDragControls : Fragment(), View.OnLongClickListener {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_arena_drag_controls, container, false)
        val obstacleImageButton = root.findViewById<ImageButton>(R.id.obstacleControl)
        val robotImageButton = root.findViewById<ImageButton>(R.id.robotControl)

        obstacleImageButton.setOnLongClickListener(this)
        robotImageButton.setOnLongClickListener(this)

        return root
    }

    override fun onLongClick(v: View?): Boolean {

        var clipText = ""

        when (v?.id) {
            R.id.obstacleControl -> {
                clipText = getString(R.string.type_obstacle)
            }
            R.id.robotControl -> {
                clipText = getString(R.string.type_robot)
            }
        }
        val item = ClipData.Item(clipText)
        val mimeTypes = arrayOf(ClipDescription.MIMETYPE_TEXT_PLAIN)
        val data = ClipData(clipText, mimeTypes, item)

        val dragShadowBuilder = View.DragShadowBuilder(v)
        v?.startDragAndDrop(data, dragShadowBuilder, v, 0)

        return true
    }
}