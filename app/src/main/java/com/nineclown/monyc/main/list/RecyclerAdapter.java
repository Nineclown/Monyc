package com.nineclown.monyc.main.list;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineclown.monyc.R;
import com.nineclown.monyc.add.ChangeActivity;

import java.util.ArrayList;

/**
 * Created by nineClown on 2017-11-18.
 */

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ItemViewHolder>
        implements OnListItemClickListener {
    ArrayList<AccountBook> accountBooks = new ArrayList<>();
    Context context;

    public RecyclerAdapter(ArrayList<AccountBook> items, Context context) {
        accountBooks = items;
        this.context = context;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //todo 새로운 ViewHolder 생성
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recycler_view, parent, false);
        ItemViewHolder holder = new ItemViewHolder(view);
        holder.setOnListItemClickListener(this);
        return holder;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        //todo view의 내용을 해당 포지션의 데이터로 바꾼다.
        AccountBook accountBook = accountBooks.get(position);
        if ("expenses".equals(accountBook.getType())) {
            holder.rl_category_box.setBackground(context.getDrawable(R.drawable.main_date_bg_r));
            holder.tv_price.setTextColor(Color.parseColor("#F74961"));
        } else if ("income".equals(accountBook.getType())) {
            holder.rl_category_box.setBackground(context.getDrawable(R.drawable.main_date_bg_b));
            holder.tv_price.setTextColor(Color.parseColor("#78B2F9"));
        }
        holder.tv_price.setText(accountBook.getPrice());
        holder.tv_category.setText(accountBook.getCategory());
        holder.tv_contents.setText(accountBook.getContents());
        holder.tv_pay.setText(accountBook.getPay());
        holder.tv_date.setText(accountBook.getDate());
    }


    @Override
    public long getItemId(int position) {
        //todo 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴

        return position;
    }

    @Override
    public int getItemCount() {
        return accountBooks.size();
    }

    @Override
    public void onListItemClickListener(int position) {
        Intent intent = new Intent(context.getApplicationContext(), ChangeActivity.class);
        intent.putExtra("id", accountBooks.get(position).getId());
        intent.putExtra("type", accountBooks.get(position).getType());
        intent.putExtra("price", accountBooks.get(position).getPrice());
        intent.putExtra("category", accountBooks.get(position).getCategory());
        intent.putExtra("pay", accountBooks.get(position).getPay());
        intent.putExtra("contents", accountBooks.get(position).getContents());
        intent.putExtra("date", accountBooks.get(position).getDate());
        Log.d("RecyclerAdapter", "data : " + accountBooks.get(position).getType() + ", " + accountBooks.get(position).getDate() + ", " + accountBooks.get(position).getPrice());
        context.startActivity(intent);

    }

    class ItemViewHolder extends RecyclerView.ViewHolder {
        OnListItemClickListener listener;
        private RelativeLayout rl_category_box;
        private TextView tv_price;
        private TextView tv_category;
        private TextView tv_contents;
        private TextView tv_pay;
        private TextView tv_date;

        //todo 반복적으로 사용할 뷰 객체를 찾아온다. listview였다면 getView에서 계속 find하므로 자원낭비가 심함
        public ItemViewHolder(View itemView) {
            super(itemView);

            rl_category_box = itemView.findViewById(R.id.irv_category_box);
            tv_price = itemView.findViewById(R.id.irv_tv_price);
            tv_category = itemView.findViewById(R.id.irv_tv_category);
            tv_contents = itemView.findViewById(R.id.irv_tv_contents);
            tv_pay = itemView.findViewById(R.id.irv_tv_pay);
            tv_date = itemView.findViewById(R.id.irv_tv_date);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onListItemClickListener(getAdapterPosition());
                }
            });
        }

        public void setOnListItemClickListener(OnListItemClickListener onListItemClickListener) {
            listener = onListItemClickListener;
        }


    }
}