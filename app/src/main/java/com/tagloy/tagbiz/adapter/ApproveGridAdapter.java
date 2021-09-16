package com.tagloy.tagbiz.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tagloy.tagbiz.interfaces.ILoadMore;
import com.tagloy.tagbiz.R;
import com.tagloy.tagbiz.models.Feed;
import com.tagloy.tagbiz.utils.BackgroundClass;

import java.util.List;

import jp.wasabeef.glide.transformations.BlurTransformation;
import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class ApproveGridAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private Context context;
    private List<Feed> feedList;
    private BackgroundClass backgroundClass;
    private final int VIEW_TYPE_ITEM = 0, VIEW_TYPE_LOADING = 1;
    private ILoadMore iLoadMore;
    private boolean isLoading;
    private int visibleThreshold = 10;
    private int lastVisibleItem, totalItemCount;

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView pictureView, pictureViewBackground;
        TextView userText, messageText, timerText;
        Button rejectButton;

        public ViewHolder(View view){
            super(view);
            pictureView = view.findViewById(R.id.pictureViewApprove);
            userText = view.findViewById(R.id.usernameTextApprove);
            messageText = view.findViewById(R.id.feedMessageTextApprove);
            timerText = view.findViewById(R.id.uploadTimeTextApprove);
            rejectButton = view.findViewById(R.id.rejectButtonApprove);
            pictureViewBackground = view.findViewById(R.id.pictureViewApproveBackground);
        }
    }

    public class LoadViewHolder extends RecyclerView.ViewHolder{
        ProgressBar progressBar;

        public LoadViewHolder(View view){
            super(view);
            progressBar = view.findViewById(R.id.loadMoreProgress);
        }
    }

    public ApproveGridAdapter(RecyclerView recyclerView, Context context, List<Feed> feedList){
        this.context = context;
        this.feedList = feedList;
        final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                totalItemCount = linearLayoutManager.getItemCount();
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
                if (!isLoading && totalItemCount<= (lastVisibleItem + visibleThreshold)){
                    if (iLoadMore != null)
                        iLoadMore.onLoadMore();
                    isLoading = true;
                }
            }
        });
    }

    public void setiLoadMore(ILoadMore iLoadMore) {
        this.iLoadMore = iLoadMore;
    }

    @Override
    public int getItemViewType(int position) {
        return feedList.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM){
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.approved_feed_view, parent, false);
            return new ViewHolder(itemView);
        }else if (viewType == VIEW_TYPE_LOADING){
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_loading, parent, false);
            return new ViewHolder(itemView);
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof ViewHolder) {
            backgroundClass = new BackgroundClass(context);
            final Feed feed = feedList.get(position);
            ViewHolder viewHolder = (ViewHolder) holder;
            Glide.with(context).load(feed.getImgUri()).into(viewHolder.pictureView);
            Glide.with(context).load(feed.getImgUri()).apply(bitmapTransform(new BlurTransformation(25)))
                    .into(viewHolder.pictureViewBackground);
            viewHolder.userText.setText(feed.getUser_name());
            viewHolder.messageText.setText("#" + feed.getFeed_message());
            viewHolder.timerText.setText(feed.getUpload_time());
            viewHolder.rejectButton.setOnClickListener(view -> {
                AlertDialog.Builder alert = new AlertDialog.Builder(context);
                alert.setTitle("Reject!");
                alert.setMessage("Do you want to reject this feed?");
                alert.setPositiveButton("OK", (dialog, which) -> {
                    backgroundClass.rejectFeed(feed.getFeed_id(),"PUBLISHED");
                    notifyItemRemoved(position);
                });
                alert.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
                alert.show();
            });
        }else if (holder instanceof LoadViewHolder){
            LoadViewHolder loadViewHolder = (LoadViewHolder) holder;
            loadViewHolder.progressBar.setIndeterminate(true);
        }
    }

    public void setLoaded() {
        isLoading = false;
    }
}
