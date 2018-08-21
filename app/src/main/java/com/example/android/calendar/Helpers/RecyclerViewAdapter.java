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
    public static final String EX_DAY_ID = "exDayId";

    private Context mContext;
    private Fragment mTargetFragment;
    private ArrayList<Event> mEvents;
    private ArrayList<Event> mPressedItems;
    private ArrayList<View> mPressedViewItems;

    public class EventViewHolder extends RecyclerView.ViewHolder{

        public View mEventBlockView;
        public TextView mLabel, mComment, mStartTime, mDuration;

        public EventViewHolder(View itemView){
            super(itemView);
            mEventBlockView = itemView;
            mLabel = itemView.findViewById(R.id.eventBlockLabel);
            mComment = itemView.findViewById(R.id.eventBlockComment);
            mStartTime = itemView.findViewById(R.id.eventBlockStartingTime);
            mDuration = itemView.findViewById(R.id.eventBlockDuration);
        }

        private void onLongPressMode(EventViewHolder holder){
            int colorId;
            final Event event = mEvents.get(holder.getAdapterPosition());
            if (mPressedItems.contains(event)) {
                colorId = event.getBlockColorScheme()[0];
                mPressedItems.remove(mEvents.get(this.getAdapterPosition()));
            }
            else {
                colorId = event.getBlockColorScheme()[1];
                mPressedViewItems.add(holder.mEventBlockView);
                mPressedItems.add(mEvents.get(this.getAdapterPosition()));
            }

            this.itemView.setBackgroundColor(mContext.getResources().getColor(colorId, mContext.getTheme()));
        }
    }

    public RecyclerViewAdapter(Context context, ArrayList<Event> events, Fragment targetFragment){
        mContext = context;
        mEvents = events;
        mTargetFragment = targetFragment;
        mPressedItems = new ArrayList<>();
        mPressedViewItems = new ArrayList<>();
    }

    public void updateDataSet(ArrayList<Event> events){
        mEvents.clear();
        mEvents.addAll(events);
    }

    public int getPreviousPosition(UUID eventId){
        for(int i=0; i<mEvents.size(); i++){
            if(mEvents.get(i).getId().compareTo(eventId) == 0)
                return i;
        }
        return -1;
    }

    public ArrayList<Event> getPressedItems(){
        return this.mPressedItems;
    }

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
        holder.itemView.setBackgroundColor(mContext.getResources().getColor(event.getBlockColorScheme()[0], mContext.getTheme()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mPressedItems.isEmpty()){
                    holder.onLongPressMode(holder);
                    return;
                }
                Intent intent = new Intent(mContext.getApplicationContext(), EventCreatorActivity.class);
                intent.putExtra(EX_EVENT_ID, event.getId());
                intent.putExtra(EX_DAY_ID, event.getParent().getId());
                mTargetFragment.startActivityForResult(intent, DayViewFragment.RC_EDIT);
            }
        });
        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                holder.onLongPressMode(holder);
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mEvents.size();
    }
}
