package com.example.android.bookstoreapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

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

                // Set the URI on the data field of the intent
                intent.setData(currentBookUri);

                startActivity(intent);
                return true;

            case R.id.action_delete:
                showDeleteConfirmationDialog();
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
            final String phoneNumber = cursor.getString(phoneNumberColumnIndex);
            String price = cursor.getString(priceColumnIndex);
            String quantity = cursor.getString(quantityColumnIndex);

            phoneNumberTextView.setOnClickListener(new EditText.OnClickListener(){

                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    intent.setData(Uri.parse("tel:" + String.valueOf(phoneNumber)));
                    startActivity(intent);
                }
            });
            new QuantityChanger(quantity);
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

    private class QuantityChanger implements View.OnClickListener{

        EditText detailsEditText = findViewById(R.id.details_edit_text_quantity_changer);
        Button increaseButton = findViewById(R.id.button_add);
        Button decreaseButton = findViewById(R.id.button_minus);
        String newBookQuantity;
        int quantityChanger;

        QuantityChanger(String bookQuantity){
            newBookQuantity = bookQuantity;
            increaseButton.setOnClickListener(this);
            decreaseButton.setOnClickListener(this);

        }


        private void increase(){
            ContentValues values = new ContentValues();
            if (detailsEditText.getText().toString().trim().equals("")) {
                quantityChanger = 1;
            } else {
                quantityChanger = Integer.parseInt(detailsEditText.getText().toString().trim());
            }
            newBookQuantity = String.valueOf(Integer.valueOf(newBookQuantity) + quantityChanger);
            values.put(BookContract.BookEntry.COLUMN_BOOK_QUANTITY, newBookQuantity);
            getApplicationContext().getContentResolver().update(currentBookUri, values, null, null);
        }

        private void decrease(){
            ContentValues values = new ContentValues();
            if (detailsEditText.getText().toString().trim().equals("")) {
                quantityChanger = 1;
            } else {
                quantityChanger = Integer.parseInt(detailsEditText.getText().toString().trim());
            }
            if (Integer.valueOf(newBookQuantity) - quantityChanger < 0) {
                Toast.makeText(getApplicationContext(), "Book quantity cannot be negative", Toast.LENGTH_SHORT).show();
                return;
            }
            newBookQuantity = String.valueOf(Integer.valueOf(newBookQuantity) - quantityChanger);
            values.put(BookContract.BookEntry.COLUMN_BOOK_QUANTITY, newBookQuantity);
            getApplicationContext().getContentResolver().update(currentBookUri, values, null, null);
        }

        @Override
        public void onClick(View view) {

            switch (view.getId()){
                case R.id.button_add:
                    increase();
                    break;
                case R.id.button_minus:
                    decrease();
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Prompt the user to confirm that they want to delete this book.
     */
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the book.
                deleteBook();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the book.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the book in the database.
     */
    private void deleteBook() {
        // Only perform the delete if this is an existing book.
        if (currentBookUri != null) {
            // Call the ContentResolver to delete the book at the given content URI.
            // Pass in null for the selection and selection args because the currentBookUri
            // content URI already identifies the book that we want.
            int rowsDeleted = getContentResolver().delete(currentBookUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_book_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_book_successful),
                        Toast.LENGTH_SHORT).show();
            }
        }

        // Close the activity
        finish();
    }

}
