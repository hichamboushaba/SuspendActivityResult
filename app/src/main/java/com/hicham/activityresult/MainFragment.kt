package com.hicham.activityresult

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.hicham.activityresult.databinding.FragmentMainBinding
import com.hicham.activityresult.permission.regular.PermissionRegularFragment
import com.hicham.activityresult.permission.suspend.PermissionSuspendFragment

class MainFragment : Fragment(R.layout.fragment_main) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentMainBinding.bind(view)
        binding.permission1Button.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, PermissionRegularFragment())
                addToBackStack(null)
            }
        }
        binding.permission2Button.setOnClickListener {
            parentFragmentManager.commit {
                replace(R.id.fragment_container, PermissionSuspendFragment())
                addToBackStack(null)
            }
        }
    }
}


