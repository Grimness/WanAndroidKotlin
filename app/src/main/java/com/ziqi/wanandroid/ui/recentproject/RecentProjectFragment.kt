package com.ziqi.wanandroid.ui.recentproject

import android.os.Parcelable
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.chad.library.adapter.base.animation.AlphaInAnimation
import com.ziqi.baselibrary.common.WebInfo
import com.ziqi.baselibrary.mvvm.ViewModelFragment
import com.ziqi.baselibrary.util.StringUtil
import com.ziqi.wanandroid.R
import com.ziqi.wanandroid.bean.Article
import com.ziqi.wanandroid.bean.ListProject
import com.ziqi.wanandroid.bean.WanList
import com.ziqi.wanandroid.databinding.FragmentProjectBinding
import com.ziqi.wanandroid.databinding.FragmentProjectBindingImpl
import com.ziqi.wanandroid.databinding.FragmentRecentBlogBinding
import com.ziqi.wanandroid.databinding.FragmentRecentProjectBinding
import com.ziqi.wanandroid.ui.common.BaseFragment
import com.ziqi.wanandroid.ui.imagepreview.ImagePreviewParams
import com.ziqi.wanandroid.ui.recentblog.RecentBlogViewModel
import com.ziqi.wanandroid.util.ImageLoad
import com.ziqi.wanandroid.util.StartUtil

class RecentProjectFragment :
    BaseFragment<RecentProjectViewModel, Parcelable, FragmentRecentProjectBinding>() {

    companion object {
        fun newInstance() = RecentProjectFragment()
    }


    private var mAdapter: BaseQuickAdapter<ListProject.DatasBean, BaseViewHolder>? = null

    var mData: ListProject? = null

    override fun onClick(v: View?) {
    }

    override fun zSetLayoutId(): Int {
        return R.layout.fragment_recent_project
    }

    override fun zContentViewId(): Int {
        return R.id.myRootView
    }

    override fun zVisibleToUser(isNewIntent: Boolean) {
    }

    override fun zLazyVisible() {
        super.zLazyVisible()
        initView()
        dealViewModel()
        onRefresh()
    }

    override fun initView() {
        mAdapter =
            object : BaseQuickAdapter<ListProject.DatasBean, BaseViewHolder>(
                R.layout.fragment_recent_project_item,
                null
            ) {
                override fun convert(holder: BaseViewHolder, item: ListProject.DatasBean) {
                    holder.setText(
                        R.id.author,
                        StringUtil.emptyTip(item.author, item.shareUser ?: "暂无")
                    )
                    holder.setText(
                        R.id.desc,
                        StringUtil.emptyTip(item.desc, "暂无介绍")
                    )
                    holder.setText(R.id.title, Html.fromHtml(item.title))
                    holder.setText(R.id.niceDate, """时间：${item.niceDate?.trim()}""")
                    holder.setText(
                        R.id.chapterName, """${item.chapterName}/${item.superChapterName}"""
                    )
                    ImageLoad.loadUrl(
                        this@RecentProjectFragment,
                        item.envelopePic,
                        holder.getView(R.id.envelopePic)
                    )
                    holder.getView<LinearLayout>(R.id.content).setOnClickListener {
                        activity?.let {
                            val webInfo = WebInfo()
                            webInfo.url = item.link
                            StartUtil.startWebFragment(it, this@RecentProjectFragment, -1, webInfo)
                        }
                    }
                    holder.getView<ImageView>(R.id.envelopePic).setOnClickListener {
                        activity?.let {
                            var params = ImagePreviewParams()
                            params.imgUrl = arrayListOf(item.envelopePic, item.envelopePic)
                            StartUtil.startImagePreviewFragment(
                                it,
                                this@RecentProjectFragment,
                                -1,
                                params
                            )
                        }
                    }
                }
            }

        mAdapter?.openLoadAnimation(AlphaInAnimation())
        mViewDataBinding?.recyclerview?.adapter = mAdapter
        //=================================================================================
        mViewDataBinding?.recyclerview?.layoutManager = LinearLayoutManager(context)
        mViewDataBinding?.recyclerview?.addItemDecoration(
            DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )
        mAdapter?.setOnItemClickListener { _, _, _ ->

        }
        //https://github.com/CymChad/BaseRecyclerViewAdapterHelper/blob/master/readme/8-LoadMore.md
        mAdapter?.setOnLoadMoreListener({
            val curPage = mData?.curPage
            val pageCount = mData?.pageCount
            if (curPage ?: 1 >= pageCount ?: 1) {
                mAdapter?.loadMoreEnd()
            } else {
                mViewModel?.loadListProject(mData?.curPage ?: 1)
            }
        }, mViewDataBinding?.recyclerview)
        mViewDataBinding?.myRootView?.setOnRefreshListener(this)
    }

    override fun dealViewModel() {
        mViewModel?.mRefresh?.observe(viewLifecycleOwner, Observer {
            mViewDataBinding?.myRootView?.isRefreshing = false
        })
        mViewModel?.mLoadMore?.observe(viewLifecycleOwner, Observer {
            it.apply {
                when (getContentIfNotHandled()) {
                    true -> {
                        mAdapter?.loadMoreComplete()
                    }
                    false -> {
                        mAdapter?.loadMoreFail()
                    }
                }
            }
        })
        mViewModel?.mListProject?.observe(viewLifecycleOwner, Observer {
            it?.let {
                it.datas?.apply {
                    if (it.curPage == 1) {
                        mAdapter?.setNewData(this)
                    } else {
                        mAdapter?.addData(this)
                    }
                }
                mAdapter?.setEnableLoadMore(it.pageCount > 1)
                mData = it
            }
        })
    }

    override fun onRefresh() {
        mViewModel?.loadListProject(0)
    }

}