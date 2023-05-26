package com.xheng.mydaygram.fragments

import android.animation.Animator
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.xheng.mydaygram.R
import com.xheng.mydaygram.MainActivity
import com.xheng.mydaygram.adapter.SearchAdapter
import com.xheng.mydaygram.model.Diary
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.litepal.LitePal
import org.litepal.extension.find
import java.util.*

class SearchFragment: BaseFragment(), AdapterView.OnItemClickListener, TextWatcher{
    // 定义搜索日记的集合
    private var diaries = mutableListOf<Diary>()

    // 定义搜索关键字
    private  var keyWords: String? = null

    // 定义显示搜索结果的 ListView
    private lateinit var searchListView: ListView

    // 定义适配器
    private lateinit var searchAdapter: SearchAdapter

    private lateinit var searchBar: EditText

    private lateinit var noData: TextView

    private lateinit var view: View

    //  弹出键盘
    override fun onResume() {
        super.onResume()
        // 自动弹出软键盘
        searchBar.postDelayed({ // 密码框请求获取焦点
            searchBar.requestFocus()
            // 弹出软键盘
            (Objects.requireNonNull(requireContext().getSystemService(Context.INPUT_METHOD_SERVICE)) as InputMethodManager).showSoftInput(
                searchBar,
                InputMethodManager.RESULT_UNCHANGED_SHOWN
            )
        }, 250)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        view = inflater.inflate(R.layout.fragment_search, container,false)

        // 获取 ListView 的实例
        searchListView = view.findViewById(R.id.result_list_view)
        // 初始化 ListView 的适配器
        searchAdapter = SearchAdapter(diaries, requireContext())
        // 设置 ListView 的适配器
        searchListView.adapter = searchAdapter
        // 监听 ListView 的点击事件
        searchListView.onItemClickListener = this

        searchBar= view.findViewById(R.id.search_bar)
        // 监听搜索栏文本变化的事件,动态搜索
        searchBar.addTextChangedListener(this)
        searchBar.typeface = app.getAttrs("Georgia")

        // 获取 nodata 文本框实例，并设置可见性
        noData = view.findViewById(R.id.noData2)
        noData.typeface = app.getAttrs("Georgia")
        noData.isVisible = keyWords?.isNotEmpty() != true
        return view
    }

    //处理 ListView 的点击事件
    override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        val diary = p0?.getItemAtPosition(p2) as Diary
        val bundle = Bundle()
        bundle.putInt("year", diary.getYear())
        bundle.putInt("month", diary.getMonth())
        bundle.putInt("day", diary.getDay())
        bundle.putInt("week", diary.getWeek())
        bundle.putString("diary", diary.getDiary())
        (activity as MainActivity).switchFragment("DiaryFragment", bundle)
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun afterTextChanged(p0: Editable?) {
        keyWords = p0.toString().trim()
        // 当关键字内容不为空时
        if (keyWords!!.isNotEmpty()) {
            // 查询匹配的日记内容
            val results = LitePal.where("diary like ?", "%$keyWords%")
                .order("year, month, day")
                .find<Diary>()
            // 清除上一次的搜索结果
            this.diaries.clear()
            // 添加新的搜索结果
            this.diaries.addAll(results)
            // 设置关键字到适配器
            searchAdapter.setkeyWords(keyWords)

            animate()
            // 刷新界面
            //searchAdapter.notifyDataSetChanged()
        } else {
            this.diaries.clear()
            animate()
        }
    }

    fun fresh(){
        // 当关键字内容不为空时
        if (keyWords != null) {
            // 查询匹配的日记内容
            val results = LitePal.where("diary like ?", "%$keyWords%")
                .order("year, month, day")
                .find<Diary>()
            // 清除上一次的搜索结果
            this.diaries.clear()
            // 添加新的搜索结果
            this.diaries.addAll(results)
            // 设置关键字到适配器
            searchAdapter.setkeyWords(keyWords)

            animate()
            // 刷新界面
            //searchAdapter.notifyDataSetChanged()
        }
    }

    override fun onStop() {
        val manager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        manager.hideSoftInputFromWindow(view.windowToken, 0)
        super.onStop()
    }

    private fun animate() {
        searchListView.animate()
            .alpha(0f)
            .setDuration(135)
            .setListener(object: Animator.AnimatorListener{
                override fun onAnimationStart(p0: Animator) {}
                override fun onAnimationCancel(p0: Animator) {}
                override fun onAnimationRepeat(p0: Animator) {}
                override fun onAnimationEnd(p0: Animator) {
                    // 刷新界面
                    searchAdapter.notifyDataSetChanged()

                    if( diaries.isEmpty()) {
                        if (!noData.isVisible) {
                            noData.isVisible = true
                            noData.animate()
                                .alpha(1f)
                                .setDuration(135)
                                .setListener(null)
                        }
                    } else {
                        if (noData.isVisible) {
                            noData.animate()
                                .alpha(0f)
                                .setDuration(135)
                                .setListener(object: Animator.AnimatorListener{
                                    override fun onAnimationStart(p0: Animator) {}
                                    override fun onAnimationCancel(p0: Animator) {}
                                    override fun onAnimationRepeat(p0: Animator) {}
                                    override fun onAnimationEnd(p0: Animator){
                                        noData.isVisible = false
                                    }
                                })
                        }
                    }

                    // 播放动画
                    searchListView.animate()
                        .alpha(1f)
                        .setDuration(135)
                        .setListener(null)
                }
            })
    }
}