package com.example.android.bookstoreapp;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.android.bookstoreapp.data.BookContract;

public class DetailsActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {


    /**
     * Identifier for the book data loader
     */
    private static final int EXISTING_BOOK_LOADER = 0;

    /**
     * Content URI for the existing book (null if it's a new book)
     */
    private Uri currentBookUri;

    /**
     * TextView field to enter the book's name
     */
    private TextView nameTextView;

    /**
     * TextView field to enter the book's supplier
     */
    private TextView supplierTextView;

    /**
     * TextView field to enter the book's price
     */
    private TextView priceTextView;

    /**
     * TextView field to enter the book's quantity
     */
    private TextView quantityTextView;

    /**
     * TextView field to enter the supplier's phone number
     */
    private TextView phoneNumberTextView;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        currentBookUri = intent.getData();

        getLoaderManager().initLoader(EXISTING_BOOK_LOADER, null, this);

        nameTextView = findViewById(R.id.details_name);
        supplierTextView = findViewById(R.id.details_supplier);
        priceTextView = findViewById(R.id.details_price);
        quantityTextView = findViewById(R.id.details_quantity);
        phoneNumberTextView = findViewById(R.id.details_phone_number);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_edit:
                Intent intent = new Intent(DetailsActivity.this, EditorActivity.class);

                Uri currentBookUri = BookContract.BookEntry.CONTENT_URI;

                // Set the URI on the data field of the intent
                intent.setData(currentBookUri);

                startActivity(intent);
                return true;

            case R.id.action_delete:
                // TODO Add the showDeleteConfirmationDialog() from EditorActivity
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        // Since the editor shows all book attributes, define a projection that contains
        // all columns from the books table
        String[] projection = {
                BookContract.BookEntry._ID,
                BookContract.BookEntry.COLUMN_BOOK_NAME,
                BookContract.BookEntry.COLUMN_BOOK_SUPPLIER_NAME,
                BookContract.BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER,
                BookContract.BookEntry.COLUMN_BOOK_PRICE,
                BookContract.BookEntry.COLUMN_BOOK_QUANTITY};

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                currentBookUri,         // Query the content URI for the current book
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // Bail early if the cursor is null or there is less than 1 row in the cursor
        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        // Proceed with moving to the first row of the cursor and reading data from it
        // (This should be the only row in the cursor)
        if (cursor.moveToFirst()) {
            // Find the columns of book attributes that we're interested in
            int nameColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_NAME);
            int supplierColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_SUPPLIER_NAME);
            int phoneNumberColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_SUPPLIER_PHONE_NUMBER);
            int priceColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_PRICE);
            int quantityColumnIndex = cursor.getColumnIndex(BookContract.BookEntry.COLUMN_BOOK_QUANTITY);

            // Extract out the value from the Cursor for the given column index
            String name = cursor.getString(nameColumnIndex);
            String supplier = cursor.getString(supplierColumnIndex);
            String phoneNumber = cursor.getString(phoneNumberColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String quantity = cursor.getString(quantityColumnIndex);

            // Update the views on the screen with the values from the database
            nameTextView.setText(name);
            supplierTextView.setText(supplier);
            priceTextView.setText(String.valueOf(price));
            quantityTextView.setText(String.valueOf(quantity));
            phoneNumberTextView.setText(String.valueOf(phoneNumber));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        nameTextView.setText("");
        supplierTextView.setText("");
        priceTextView.setText("");
        quantityTextView.setText("");
        phoneNumberTextView.setText("");
    }

}
