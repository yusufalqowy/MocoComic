package yu.desk.mococomic.presentation.auth

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentLoginBinding
import yu.desk.mococomic.utils.navigateWithAnimation

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewListener()
    }

    private fun initViewListener() {
        binding.btnLogin.setOnClickListener {
            findNavController().navigateWithAnimation(R.id.action_authLogin_to_dashboardHost)
        }

    }

}
