package com.example.android.inventoryapp3;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.example.android.inventoryapp3.data.InventoryContract.InventoryEntry;
import java.math.BigDecimal;
import java.text.NumberFormat;

/**
 * {@link InventoryCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of inventory data as its data source. This adapter knows
 * how to create list items for each row of inventory data in the {@link Cursor}.
 *
 * Created by yahir on 10/18/2018.
 */
public class InventoryCursorAdapter extends CursorAdapter {

    /** Value for converting cents to dollars */
    final private static int CENT_TO_DOLLAR = 100;

    /**
     * Constructs a new {@link InventoryCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public InventoryCursorAdapter(Context context, Cursor c) {
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
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the inventory data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current product can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView name = (TextView) view.findViewById(R.id.product_name);
        TextView price = (TextView) view.findViewById(R.id.product_price);
        TextView supplierNameTextView = (TextView) view.findViewById(R.id.supplier_name);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.product_quantity);
        LinearLayout parentView = (LinearLayout) view.findViewById(R.id.parent_layout);


        // Find the columns of inventory details that we're interested in
        int idColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
        int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME );
        int supplierNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_SUPPLIER_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);

        // Read the product details from the Cursor for the current product
        final int rowId = cursor.getInt(idColumnIndex);
        String productName = cursor.getString(nameColumnIndex);
        String supplierName = cursor.getString(supplierNameColumnIndex);
        int productPrice = cursor.getInt(priceColumnIndex);
        double dolproductPrice =Double.valueOf(productPrice) / CENT_TO_DOLLAR;
        NumberFormat nf = NumberFormat.getCurrencyInstance();
        final int productQuantity = cursor.getInt(quantityColumnIndex);

        // Update the TextViews with the details for the current product
        quantityTextView.setText(productQuantity + " " + context.getResources().getString(R.string.in_stock_text));
        name.setText(productName);
        supplierNameTextView.setText((supplierName));
        price.setText(nf.format(new BigDecimal(dolproductPrice)));

        parentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Open Detail activity
                Intent intent = new Intent(context, EditorActivity.class);

                // Form the content URI that represents clicked product.
                Uri currentInventoryUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, rowId);

                // Set the URI on the data field of the intent
                intent.setData(currentInventoryUri);
                context.startActivity(intent);
            }
        });


        // Change the quantity when you click the sale button
        Button sellButton = (Button) view.findViewById(R.id.sale_button);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the quantity string
                String mQuantityString  = quantityTextView.getText().toString();
                // Parse by empty space (num in stock)
                String[] quantityValue  = mQuantityString.split(" ");
                // get quantity value at index 0
                int quantity = Integer.parseInt(quantityValue [0]);

                // check if quantity is == 0 or > 0
                if (quantity == 0) {
                    // Inform user quantity can't be below zero
                     Toast.makeText(context, R.string.toast_zero_greater, Toast.LENGTH_SHORT).show();
                } else if (quantity > 0) {
                    quantity--;

                    //Prepare value pair to update db
                    String quantityString = Integer.toString(quantity);
                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantityString);
                    Uri currentInventoryUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, rowId);

                    //Update db
                    int rowsAffected = context.getContentResolver().update(currentInventoryUri, values, null, null);

                    // Based on return inform user if update was successful or n
                    if (rowsAffected != 0) {
                        /* update text view if database update is successful */
                        quantityTextView.setText(quantity + " " + context.getResources().getString(R.string.in_stock_text));
                    } else {
                        Toast.makeText(context, R.string.editor_update_product_failed, Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
}
