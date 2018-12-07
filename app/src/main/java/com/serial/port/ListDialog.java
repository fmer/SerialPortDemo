package com.serial.port;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class ListDialog extends Dialog {
    int layoutRes;// 布局文件
    Context context;

    public ListDialog(Context context) {
        super(context);
        this.context = context;
    }

    /**
     * * 自定义主题及布局的构造方法 * @param context * @param theme * @param resLayout
     */
    public ListDialog(Context context, int theme) {
        super(context, theme);

    }

    // Builder 方法创建dialog控件
    public static class Builder {
        private Context context;
        // Dialog 标题名称
        private String title;
        // Dialog 消息内容
        private String message;
        // listview中的数据
        private List< String> list;
        // 控件listview
        private ListView lv;
        // positiveButton Button的文字
        private String positiveButtonText;
        // lisetview的onitemclick事件
        private DialogInterface.OnClickListener listClickListener;
        // positiveButton 单击事件
        private DialogInterface.OnClickListener positiveButtonClickListener;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }

        /**
         * Set the Dialog title from resource
         *
         * @param title
         * @return
         */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }

        /**
         * Set the Dialog title from String
         *
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set the positive button resource and it's listener
         *
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(int positiveButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.positiveButtonText = (String) context
                    .getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }

        /**
         * Set the positive button text and it's listener
         *
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(String positiveButtonText,
                                         DialogInterface.OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        public Builder setList(List<String> l,
                               DialogInterface.OnClickListener listener) {
            this.list = l;
            this.listClickListener = listener;
            return this;
        }

        /**
         * Create the custom dialog
         * lisetview 的样式文件为 ：res/layout/ctl_list_dialog.xml
         * R.style.customDialog在： res/values/syles.xml 文件中
         * listdialogAdapter 应在com.sl.example.adapter包中，此设计写在了本文件中
         */
        public ListDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            // R.style.customDialog在 res/values/syles.xml 文件中
            final ListDialog dialog = new ListDialog(context,
                    R.style.customDialog);
            View layout = inflater.inflate(R.layout.ctl_list_dialog, null);
            dialog.addContentView(layout, new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            // set the dialog title
            ((TextView) layout.findViewById(R.id.ctl_dialog_title))
                    .setText(title);
            lv = (ListView) layout.findViewById(R.id.ctl_dialog_lv);
            lv.setAdapter(new listdialogAdapter());
            // set list
            lv.setOnItemClickListener(new ListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1,
                                        int arg2, long arg3) {
                    // TODO Auto-generated method stub
                    TextView v = (TextView) arg1;
                    //选中item 文字变色，背景变色，并触发自定义listClick事件
                    v.setTextColor(context.getResources().getColor(
                            R.color.white));
                    v.setBackgroundColor(context.getResources().getColor(
                            R.color.app_default));
                    listClickListener.onClick(dialog, arg2);
                }
            });
            dialog.setContentView(layout);
            return dialog;
        }

        private class listdialogAdapter extends BaseAdapter {

            public listdialogAdapter() {
            }

            @Override
            public int getCount() {
                // TODO Auto-generated method stub
                return list.size();
            }

            @Override
            public Object getItem(int arg0) {
                // TODO Auto-generated method stub
                return list.get(arg0);
            }

            @Override
            public long getItemId(int arg0) {
                // TODO Auto-generated method stub
                return arg0;
            }

            @Override
            public View getView(int arg0, View arg1, ViewGroup arg2) {
                // TODO Auto-generated method stub
                TextView t = new TextView(context);
                t.setText(list.get(arg0));
                t.setPadding(100, 20, 10, 20);
                t.setTextSize(14);
                t.setTextColor(context.getResources().getColor(R.color.gray));
                t.setGravity(Gravity.CENTER_VERTICAL);
                return t;
            }
        }
    }
}

