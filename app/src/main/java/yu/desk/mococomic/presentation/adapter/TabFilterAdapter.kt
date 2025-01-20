package yu.desk.mococomic.presentation.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class TabFilterAdapter(
    fragment: FragmentManager,
    lifecycle: Lifecycle,
) : FragmentStateAdapter(fragment, lifecycle) {
    private var listFragment: MutableList<Fragment> = mutableListOf()

    override fun getItemCount(): Int = listFragment.size

    override fun createFragment(position: Int): Fragment = listFragment[position]

    fun setItems(list: List<Fragment>) {
        listFragment = list.toMutableList()
        notifyItemRangeInserted(0, list.size)
    }

    fun changeFragment(
        position: Int,
        fragment: Fragment,
    ) {
        listFragment[position] = fragment
        notifyItemChanged(position, null)
    }
}