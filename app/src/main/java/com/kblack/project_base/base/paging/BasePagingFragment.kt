package com.kblack.project_base.base.paging

import android.os.Bundle
import android.view.View
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.kblack.project_base.R
import com.kblack.project_base.base.BaseFragment
import com.kblack.project_base.base.BasePagingAdapter
import kotlinx.coroutines.launch

abstract class BasePagingFragment<VB : ViewDataBinding, VM : BasePagingViewModel<Item>, Item : Any> :
    BaseFragment<VB, VM>() {

    override val layoutId: Int = R.layout.fragment_paging
    abstract val pagingAdapter: BasePagingAdapter<Item, out ViewDataBinding>
    abstract val swipeRefreshLayout: SwipeRefreshLayout?
    abstract val recyclerView: RecyclerView?

    open fun getLayoutManager(): RecyclerView.LayoutManager = GridLayoutManager(context, 2)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPaging()
    }

    /**
     * setup paging
     */
    private fun setupPaging() {
        swipeRefreshLayout?.setOnRefreshListener {
//            viewModel.doRefresh()
        }
        recyclerView?.layoutManager = getLayoutManager()
        recyclerView?.adapter = pagingAdapter
        recyclerView?.setHasFixedSize(true)
        lifecycleScope.launch {
//            viewModel.items.collectLatest {
//                pagingAdapter.submitData(it)
//            }
        }
        pagingAdapter.addLoadStateListener {
//            viewModel.handleLoadStates(
//                combinedLoadStates = it,
//                itemCount = pagingAdapter.itemCount
//            )
        }
    }

}