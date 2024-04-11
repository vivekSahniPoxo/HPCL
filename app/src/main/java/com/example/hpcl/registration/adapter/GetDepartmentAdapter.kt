package com.example.hpcl.registration.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.hpcl.R
import com.example.hpcl.registration.GetDepartment
import com.example.hpcl.registration.GetDepartmentName


class GetDepartmentAdapter(val context: Context, var mList: List<GetDepartmentName>) : BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val view: View
        val item: ItemHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.department_layout, parent, false)
            item = ItemHolder(view)
            view.tag = item
        } else {
            view = convertView
            item = view.tag as ItemHolder
        }

        item.cityName.text = mList[position].departmentName
//        item.depID.text = mList[position].id.toString()

        return view
    }

    override fun getItem(position: Int): Any {
        return mList[position]
    }

    override fun getCount(): Int {
        return mList.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private class ItemHolder(itemView: View) {
        val cityName = itemView.findViewById(R.id.tv_emp_name) as TextView
//        val depID:TextView = itemView.findViewById(R.id.tvId)

    }
}