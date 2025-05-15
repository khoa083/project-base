package com.kblack.project_base.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<VB: ViewBinding, VM: BaseViewModel> : Fragment() {
    private var _binding: VB? = null
    protected val fragmentBinding get() = _binding!!
    protected abstract val mViewModel: VM

    abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> VB

    /**
     * override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentGeneratePassphraseBinding =
     *     FragmentGeneratePassphraseBinding::inflate
     */

    // ViewDataBinding abstract val layoutId: Int

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        //ViewDataBinding
        /**
         * _binding = DataBindingUtil.inflate(inflater, layoutId, container, false)
         * fragmentBinding.lifecycleOwner = viewLifecycleOwner
         * */
        _binding = bindingInflater(inflater, container, false)
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView(fragmentBinding)
    }

    abstract fun setupView(fragmentBinding: VB)

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}