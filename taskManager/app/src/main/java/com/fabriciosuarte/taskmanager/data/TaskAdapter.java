package com.fabriciosuarte.taskmanager.data;

import android.content.res.ColorStateList;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.fabriciosuarte.taskmanager.R;
import com.fabriciosuarte.taskmanager.util.DateHelper;
import com.fabriciosuarte.taskmanager.view.TaskTitleView;

import java.util.Calendar;
import java.util.Date;

/**
 * The Adapter for the main Task list
 */
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskHolder> {

    //region constants

    private static final String SELECTED_ITEM_ID_STATE_KEY
            = TaskAdapter.class.getCanonicalName() + "_SelectedItemIdKey";

    private static final String SELECTED_ITEM_INDEX_STATE_KEY
            = TaskAdapter.class.getCanonicalName() + "_SelectedItemIndexKey";

    //endregion

    //region attributes

    private Cursor mCursor;
    private TextView mEmptyView;
    private long mSelectedItemId = -1;
    private int mSelectedItemIndex = -1;
    private OnItemClickListener mOnItemClickListener;

    //endregion

    //region inner classes and interfaces definitions

    /* Callback for list item click events */
    public interface OnItemClickListener {

        void onItemClick(View v, int position);
        void onItemToggled(boolean active, int position);
    }

    /* ViewHolder for each task item */
    class TaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TaskTitleView nameView;
        TextView dateView;
        ImageView priorityView;
        CheckBox checkBox;

        TaskHolder(View itemView) {
            super(itemView);

            nameView = (TaskTitleView) itemView.findViewById(R.id.text_description);
            dateView = (TextView) itemView.findViewById(R.id.text_date);
            priorityView = (ImageView) itemView.findViewById(R.id.priority);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);

            itemView.setOnClickListener(this);
            checkBox.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (v == checkBox) {
                completionToggled(this);
            } else {
                postItemClick(this);
            }
        }
    }

    //endregion

    //region constructor

    public TaskAdapter(Cursor cursor, @NonNull TextView emptyView, Bundle savedInstanceState) {
        mEmptyView = emptyView;
        mCursor = cursor;

        if(savedInstanceState != null) {
            mSelectedItemId = savedInstanceState.getLong(SELECTED_ITEM_ID_STATE_KEY);
            mSelectedItemIndex = savedInstanceState.getInt(SELECTED_ITEM_INDEX_STATE_KEY);
        }
    }

    //endregion

    //region overrides on RecyclerView.Adapter<TaskAdapter.TaskHolder>

    @Override
    public TaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_task, parent, false);

        itemView.setFocusable(true);

        return new TaskHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TaskHolder holder, int position) {

        if(mCursor.moveToPosition(position)) {

            Task task = new Task(mCursor);

            Date date = new Date(task.dueDateMillis);
            Date current = Calendar.getInstance().getTime();
            int taskState;

            if(task.isComplete) {
                taskState = TaskTitleView.DONE;
            }
            else if (task.hasDueDate() && date.after(current)) {
                taskState = TaskTitleView.NORMAL;
            }
            else if (task.hasDueDate() && date.before(current)) {
                taskState = TaskTitleView.OVERDUE;
            }
            else {
                taskState = TaskTitleView.NORMAL;
            }

            holder.nameView.setText(task.description);
            holder.nameView.setState(taskState);

            if(task.hasDueDate()) {
                holder.dateView.setText(DateHelper.format(date));
            }
            else
                holder.dateView.setText("");

            holder.checkBox.setChecked(task.isComplete);

            if(task.isPriority)
                holder.priorityView.setBackgroundResource(R.drawable.ic_priority);
            else
                holder.priorityView.setBackgroundResource(R.drawable.ic_not_priority);

            ViewCompat.setActivated(holder.itemView, (task.id == mSelectedItemId));
        }
    }

    @Override
    public int getItemCount() {
        return (mCursor != null) ? mCursor.getCount() : 0;
    }


    @Override
    public long getItemId(int position) {
        return getItem(position).id;
    }

    //endregion

    //region public methods

    public void onSaveInstanceState(Bundle outState) {
        outState.putLong(SELECTED_ITEM_ID_STATE_KEY, mSelectedItemId);
        outState.putInt(SELECTED_ITEM_INDEX_STATE_KEY, mSelectedItemIndex);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void swapCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;

        if(mCursor == null || mCursor.getCount() == 0){
            mEmptyView.setVisibility(View.VISIBLE);
        }
        else {
            mEmptyView.setVisibility(View.GONE);
        }

        notifyDataSetChanged();
    }

    /**
     * Retrieve a {@link Task} for the data at the given position.
     *
     * @param position Adapter item position.
     *
     * @return A new {@link Task} filled with the position's attributes.
     */
    public Task getItem(int position) {
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Invalid item position requested");
        }

        return new Task(mCursor);
    }

    //endregion

    //region private aux methods

    private void completionToggled(TaskHolder holder) {

        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemToggled(holder.checkBox.isChecked(), holder.getAdapterPosition());
        }
    }

    private void postItemClick(TaskHolder holder) {

        if(mSelectedItemIndex >= 0) {
            //notify the item that was previously selected...
            this.notifyItemChanged(mSelectedItemIndex);
        }

        int position = holder.getAdapterPosition();

        //Updates the current item's id and index
        mCursor.moveToPosition(position);
        mSelectedItemId = DatabaseContract.getColumnLong(mCursor, DatabaseContract.TaskColumns._ID);
        mSelectedItemIndex = position;

        //notify the new selected item
        this.notifyItemChanged(position);

        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(holder.itemView, position );
        }
    }

    //endregion

}
