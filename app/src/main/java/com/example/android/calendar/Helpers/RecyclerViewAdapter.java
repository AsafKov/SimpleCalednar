package com.example.android.calendar.Helpers;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.calendar.Activities.EventCreatorActivity;
import com.example.android.calendar.Fragments.DayViewFragment;
import com.example.android.calendar.Model.Event;
import com.example.android.calendar.R;

import java.util.ArrayList;
import java.util.UUID;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.EventViewHolder>{

    public static final String EX_EVENT_ID = "exEventId";
    public static final String EX_TIME_STAMP = "exTimeStamp";

    private Context mContext;
    private Fragment mTargetFragment;
    private ArrayList<Event> mEvents;
    private ArrayList<Event> mPressedItems;

    public class EventViewHolder extends RecyclerView.ViewHolder{

        private TextView mLabel, mComment, mStartTime, mDuration;

        public EventViewHolder(View itemView){
            super(itemView);
            mLabel = itemView.findViewById(R.id.eventBlockLabel);
            mComment = itemView.findViewById(R.id.eventBlockComment);
            mStartTime = itemView.findViewById(R.id.eventBlockStartingTime);
            mDuration = itemView.findViewById(R.id.eventBlockDuration);
        }

        private void onLongPressMode(){
            int colorId;
            final Event event = mEvents.get(this.getAdapterPosition());
            if (mPressedItems.contains(event)) {
                colorId = event.getBlockDefaultColor();
                mPressedItems.remove(event);
            }
            else {
                colorId = event.getBlockDefaultColor()+1;
                mPressedItems.add(event);
            }

            this.itemView.setBackgroundColor(mContext.getResources().getColor(colorId, mContext.getTheme()));
        }
    }

    public RecyclerViewAdapter(Context context, ArrayList<Event> events, Fragment targetFragment){
        mContext = context;
        mEvents = events;
        mTargetFragment = targetFragment;
        mPressedItems = new ArrayList<>();
    }

    public void updateDataSet(ArrayList<Event> events){
        mEvents.clear();
        mEvents.addAll(events);
    }

    public ArrayList<Event> getPressedItems(){
        return this.mPressedItems;
    }

    public void layOffPressedItems(){ mPressedItems.clear(); }

    @Override
    public void onViewRecycled(RecyclerViewAdapter.EventViewHolder holder){
        super.onViewRecycled(holder);
    }

    @Override
    public RecyclerViewAdapter.EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View itemView = layoutInflater.inflate(R.layout.event_block_layout, parent, false);
        EventViewHolder viewHolder = new EventViewHolder(itemView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerViewAdapter.EventViewHolder holder, int position) {
        final Event event = mEvents.get(position);

        holder.mLabel.setText(event.getLabel());
        holder.mComment.setText(event.getComment());
        holder.mStartTime.setText(DateFormat.format("HH:mm", event.getTime()));
        holder.mDuration.setText(event.getDurationInFormat(mContext));

        holder.itemView.setBackgroundColor(mContext.getResources().getColor(event.getBlockDefaultColor(), mContext.getTheme()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mPressedItems.isEmpty()){
                    // If an item is already on long-press state, every click or press will get any
                    // other item into the same state, for a potential action on multiple items
                    holder.onLongPressMode();
                    return;
                }
                v.setClickable(false);
                Intent intent = new Intent(mContext.getApplicationContext(), EventCreatorActivity.class);
                intent.putExtra(EX_EVENT_ID, event.getId());
                intent.putExtra(EX_TIME_STAMP, event.getDayTimeStamp());
                mTargetFragment.startActivity(intent);
                v.setClickable(true);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                holder.onLongPressMode();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }
}
