package com.example.cuisinesync


import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


class AccountFragment:Fragment() {

    private lateinit var dataStoreRepository: DataStoreRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_account, container, false)

        val logoutButton = view.findViewById<Button>(R.id.log_out_button)
        logoutButton.setOnClickListener {
            lifecycleScope.launch {
                dataStoreRepository.clearDataStore()
                navigateToLoginActivity()
            }
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Use the already defined dataStore extension property from the Context.
        val dataStore = (requireActivity().application as MyApplication).dataStore

        dataStoreRepository = DataStoreRepository(dataStore)
    }

    private fun navigateToLoginActivity() {
        val intent = Intent(requireContext(), LogInActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
    }

}