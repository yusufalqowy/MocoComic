package yu.desk.mococomic.presentation.dashboard.favorite

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import yu.desk.mococomic.R
import yu.desk.mococomic.databinding.FragmentFavoriteBinding
import yu.desk.mococomic.presentation.adapter.ComicAdapter

class FavoriteFragment : Fragment() {

    private lateinit var binding: FragmentFavoriteBinding

    companion object{
        const val TAG = "FavoriteFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {

    }

}
