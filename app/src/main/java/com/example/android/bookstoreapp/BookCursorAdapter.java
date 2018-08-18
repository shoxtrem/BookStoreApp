package com.example.android.bookstoreapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.bookstoreapp.data.BookContract;

/**
 * {@link BookCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of book data as its data source. This adapter knows
 * how to create list items for each row of book data in the {@link Cursor}.
 */
class BookCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link BookCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public BookCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }


    /**
     * This method binds the book data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current book can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.name);
        TextView quantityTextView = view.findViewById(R.id.summary);
        TextView priceTextView = view.findViewById(R.id.price);
        Button salesButton = view.findViewById(R.id.button_sale);

        // Find the columns of book attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_NAME);
        int priceColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_QUANTITY);
        int idColumnIndex = cursor.getColumnIndex(BookContract.BookEntry._ID);


        // Read the book attributes from the Cursor for the current book
        String bookName = cursor.getString(nameColumnIndex);
        String bookPrice = "$" + cursor.getString(priceColumnIndex);
        String bookQuantity = cursor.getString(quantityColumnIndex);
        int id = cursor.getInt(idColumnIndex);

        // Add a special onClickListener
        salesButton.setOnClickListener(new SoldBook(context, bookQuantity, id));
        // Update the TextViews with the attributes for the current book
        nameTextView.setText(bookName);
        quantityTextView.setText(bookQuantity);
        priceTextView.setText(bookPrice);


    }

    private class SoldBook implements View.OnClickListener {
        final Context newContext;
        final int newId;
        /**
         * Global variables
         */
        String newBookQuantity;

        /**
         * @param context      context of the activity
         * @param bookQuantity is the quantity of that particular book
         * @param id           is the ID of that particular book in the database
         */
        SoldBook(Context context, String bookQuantity, int id) {

            newBookQuantity = bookQuantity;
            newContext = context;
            newId = id;
        }

        @Override
        public void onClick(View view) {

            ContentValues values = new ContentValues();
//            values.put(BookContract.BookEntry.COLUMN_BOOK_QUANTITY, newBookQuantity);

            if (Integer.valueOf(newBookQuantity).equals(0)) {
                Toast.makeText(newContext, "Book quantity cannot be negative", Toast.LENGTH_SHORT).show();
                return;
            }
            newBookQuantity = String.valueOf(Integer.valueOf(newBookQuantity) - 1);
            values.put(BookContract.BookEntry.COLUMN_BOOK_QUANTITY, newBookQuantity);
            Uri currentBookUri = ContentUris.withAppendedId(BookContract.BookEntry.CONTENT_URI, newId);
            newContext.getContentResolver().update(currentBookUri, values, null, null);

        }
    }

}


