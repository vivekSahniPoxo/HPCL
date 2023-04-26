package com.example.hpcl

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.hpcl.databinding.FragmentHomeBinding


class HomeFragment : Fragment() {
   lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        binding = FragmentHomeBinding.bind(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

//        binding.mcardView.setOnClickListener {
//            FragMover.replaceFragStack(activity?.supportFragmentManager,
//                RegistrationFragment(),
//                R.id.fragment_container_view_tag
//            )
//        }
//
//        binding.mCardVisitor.setOnClickListener {
//            FragMover.replaceFragStack(activity?.supportFragmentManager,
//                IdentificationFragment(),
//                R.id.fragment_container_view_tag)
//        }
    }


}