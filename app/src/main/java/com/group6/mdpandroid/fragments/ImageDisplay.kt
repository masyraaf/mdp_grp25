package com.group6.mdpandroid.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.group6.mdpandroid.R
import com.group6.mdpandroid.databinding.FragmentImageDisplayBinding
import com.group6.mdpandroid.viewmodels.RobotMapViewModel
import kotlinx.android.synthetic.main.fragment_image_display.*


class ImageDisplay : Fragment() {

    // Initialise binding, view model
    private var _binding: FragmentImageDisplayBinding? = null
    private val binding get() = _binding!!
    private val viewModel: RobotMapViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val viewModel: RobotMapViewModel by requireActivity().viewModels()

        _binding = FragmentImageDisplayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //TODO: Remove and replace with real data on receiving from bluetooth service
        //TODO: Changing of text doesnt work for all imageviews for some reason :(
        addImage(0, 1)
        addImage(0, 2)
        addImage(0, 3)
        addImage(0, 4)
        addImage(1, 5)
        addImage(1, 6)
        addImage(1, 7)
        addImage(1, 8)
        addImage(2, 9)
        addImage(2, 10)
        addImage(2, 11)
        addImage(2, 12)
    }

    private fun addImage(i: Int, imageID: Int) {

        // i refers to the i-th row for the image to populate
        //TODO: add check if row is maxed out and to increment i if so
        when (i) {
            0 -> {
                val imageItem = layoutInflater.inflate(R.layout.image_item, image_view0)
                val image = imageItem.findViewById<ImageView>(R.id.seen_image)
                val imageText = imageItem.findViewById<TextView>(R.id.imageid_text)
                //TODO: Pass in image and set image accordingly
                imageText.text = imageID.toString()
            }
            1 -> {
                val imageItem = layoutInflater.inflate(R.layout.image_item, image_view1)
                val image = imageItem.findViewById<ImageView>(R.id.seen_image)
                val imageText = imageItem.findViewById<TextView>(R.id.imageid_text)
                //TODO: Pass in image and set image accordingly
                imageText.text = imageID.toString()
            }
            2 -> {
                val imageItem = layoutInflater.inflate(R.layout.image_item, image_view2)
                val image = imageItem.findViewById<ImageView>(R.id.seen_image)
                val imageText = imageItem.findViewById<TextView>(R.id.imageid_text)
                //TODO: Pass in image and set image accordingly
                imageText.text = imageID.toString()
            }
        }

    }

}