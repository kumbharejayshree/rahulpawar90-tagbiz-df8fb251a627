package com.tagloy.tagbiz.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tagloy.tagbiz.R;
import com.tagloy.tagbiz.models.Creative;
import com.tagloy.tagbiz.utils.BackgroundClass;

import java.util.List;
import jp.wasabeef.glide.transformations.BlurTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class HighlightGridAdapter extends RecyclerView.Adapter<HighlightGridAdapter.ViewHolder> {

    Context context;
    List<Creative> creativeList;
    BackgroundClass backgroundClass;

    public HighlightGridAdapter(Context context, List<Creative> creativeList){
        this.context = context;
        this.creativeList = creativeList;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView pictureView, deleteButton, pictureViewBack;
        ImageButton playButton;
        TextView statusText, durationText;

        public ViewHolder(View view){
            super(view);
            pictureView = view.findViewById(R.id.highlightPictureView);
            pictureViewBack = view.findViewById(R.id.pictureViewBackgroundHighlight);
            deleteButton = view.findViewById(R.id.deleteHighlight);
            statusText = view.findViewById(R.id.highlightStatusText);
            durationText = view.findViewById(R.id.highlightDurationText);
            playButton = view.findViewById(R.id.playButtonHighlight);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.highlight_view, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int position) {

        final Creative creative = creativeList.get(position);
        backgroundClass = new BackgroundClass(context);

        if (creative.type.equals("IMAGE")){
            viewHolder.playButton.setVisibility(View.GONE);
        }else if (creative.type.equals("VIDEO")){
            viewHolder.playButton.setVisibility(View.VISIBLE);
            viewHolder.playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog dialog = new Dialog(context);
                    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    dialog.setContentView(R.layout.creative_video_view);
                    dialog.show();
                    WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT,
                            WindowManager.LayoutParams.WRAP_CONTENT);
                    layoutParams.dimAmount = 0;
                    layoutParams.copyFrom(dialog.getWindow().getAttributes());
                    dialog.getWindow().setAttributes(layoutParams);
                    VideoView videoView = dialog.findViewById(R.id.creativeVideoView);
                    videoView.setZOrderOnTop(true);
                    videoView.setVideoURI(Uri.parse(creative.getMedia()));
                    videoView.start();
                    videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                        @Override
                        public void onCompletion(MediaPlayer mediaPlayer) {
                            dialog.cancel();
                        }
                    });
                }
            });
        }
        Glide.with(context).load(creative.getMedia()).into(viewHolder.pictureView);
        Glide.with(context).load(creative.getMedia()).apply(bitmapTransform(new BlurTransformation(25)))
                .into(viewHolder.pictureViewBack);
        viewHolder.statusText.setText(creative.getStatus());
        viewHolder.durationText.setText("" + creative.getDuration() + " sec");
        if (creative.getStatus().equals("Live")){
            viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle("Delete!");
                    alert.setMessage("Do you want to delete this Highlight?");
                    alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            backgroundClass.deleteHighlight(creative.getId());
                        }
                    });
                    alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alert.show();
                }
            });
        }else {
            viewHolder.deleteButton.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return creativeList.size();
    }
}
