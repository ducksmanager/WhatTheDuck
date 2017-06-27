package net.ducksmanager.whattheduck;


import android.app.ListActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import net.ducksmanager.util.CoverFlowFileHandler;
import net.ducksmanager.whattheduck.Collection.CollectionType;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

public abstract class List extends ListActivity{
    private static final int LOGOUT = 1;

    public String type;
    private ArrayList<String> items;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        type = extras != null && extras.getString("type") != null
                    ? extras.getString("type")
                    : CollectionType.USER.toString();

        setContentView(R.layout.wtd_list);

        this.findViewById(R.id.navigationAllCountries).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToView(CountryList.class);
            }
        });

        this.findViewById(R.id.navigationCountry).findViewById(R.id.selected).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToView(PublicationList.class);
            }
        });

        Switch onlyInCollectionSwitch = (Switch) this.findViewById(R.id.onlyInCollectionSwitch);
        onlyInCollectionSwitch.setChecked(type.equals(CollectionType.USER.toString()));

        onlyInCollectionSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Switch onlyInCollectionSwitch = (Switch) view;
            List.this.goToAlternativeView(
                onlyInCollectionSwitch.isChecked()
                    ? CollectionType.USER.toString()
                    : CollectionType.COA.toString()
            );
            }
        });

        if (type.equals(CollectionType.USER.toString())) {
            RelativeLayout addToCollection = (RelativeLayout) this.findViewById(R.id.addToCollectionWrapper);

            addToCollection.setVisibility(View.VISIBLE);
            addToCollection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    takeCoverPicture();
                }
            });

            if (WhatTheDuck.getShowCoverTooltip()) {
                new SimpleTooltip.Builder(this)
                    .anchorView(addToCollection)
                    .text(R.string.add_cover_tooltip)
                    .gravity(Gravity.TOP)
                    .animated(true)
                    .margin(5.0f)
                    .transparentOverlay(true)
                    .build()
                    .show();
            }

            WhatTheDuck.setShowCoverTooltip(false);
            WhatTheDuck.saveSettings(null);
        }

        setTitle(
            type.equals(CollectionType.USER.toString())
                    ? getString(R.string.my_collection)
                    : getString(R.string.referenced_issues)
        );
    }

    private void goToView(Class<?> cls) {
        if (!List.this.getClass().equals(cls)) {
            Intent i = new Intent(WhatTheDuck.wtd, cls);
            i.putExtra("type", type);
            startActivity(i);
        }
    }

    private void goToAlternativeView(String collectionType) {
        Intent i = new Intent(WhatTheDuck.wtd, this.getClass());
        i.putExtra("type", collectionType);
        startActivity(i);
    }

    public abstract void show();

    public void show(ArrayList<String> items) {
        this.items = items;

        if (items.size() == 0) {
            TextView emptyListText = (TextView) this.findViewById(R.id.emptyList);
            emptyListText.setVisibility(TextView.VISIBLE);
        }

        String[] lv_arr = items.toArray(new String[items.size()]);
        setListAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, lv_arr));

        EditText filterEditText = (EditText) this.findViewById(R.id.filter);
        if (items.size() > 20) {
            filterEditText.setVisibility(EditText.VISIBLE);

            filterEditText.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) { }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    String typedText = s.toString();
                    ArrayList<String> filteredItems = new ArrayList<>();
                    for (String item : List.this.items)
                        if (item.replace("* ", "").toLowerCase(Locale.FRANCE).contains(typedText.toLowerCase()))
                            filteredItems.add(item);

                    String[] lv_arr = filteredItems.toArray(new String[filteredItems.size()]);
                    setListAdapter(new ArrayAdapter<>(List.this, android.R.layout.simple_list_item_1, lv_arr));

                }
            });
        }
        else {
            filterEditText.setVisibility(EditText.GONE);
        }
    }

    private void takeCoverPicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            CoverFlowFileHandler.current = new CoverFlowFileHandler();
            Uri photoURI = CoverFlowFileHandler.current.createEmptyFileForCamera(List.this);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            this.findViewById(R.id.addToCollectionWrapper).setVisibility(View.GONE);
            this.findViewById(R.id.progressBarLoading).setVisibility(View.VISIBLE);

            CoverFlowFileHandler.current.resizeUntilFileSize(this, CoverFlowFileHandler.MAX_COVER_FILESIZE, new CoverFlowFileHandler.TransformationCallback() {
                @Override
                public void onComplete(File fileToUpload) {
                    new CoverSearch(List.this, fileToUpload).execute();
                }

                @Override
                public void onFail(File uploadFile) {
                    List.this.findViewById(R.id.addToCollectionWrapper).setVisibility(View.VISIBLE);
                    List.this.findViewById(R.id.progressBarLoading).setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, LOGOUT, 1, R.string.logout_menu);

        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        Intent i = null;
        switch(item.getItemId()) {
            case LOGOUT:
                WhatTheDuck.userCollection = new Collection();
                WhatTheDuck.coaCollection = new Collection();
                WhatTheDuck.setUsername(null);
                WhatTheDuck.setPassword(null);
                WhatTheDuck.setPassword(null);
                i = new Intent(WhatTheDuck.wtd, WhatTheDuck.class);

                break;
        }
        startActivity(i);

        return super.onMenuItemSelected(featureId, item);
    }

    protected Collection getCollection() {
        return type == null || type.equals(CollectionType.USER.toString())
            ? WhatTheDuck.userCollection
            : WhatTheDuck.coaCollection;
    }

    protected void setNavigationCountry(String countryFullName, String selectedCountry) {
        View countryNavigationView = this.findViewById(R.id.navigationCountry);

        String uri = "@drawable/flags_" + selectedCountry;
        int imageResource = getResources().getIdentifier(uri, null, getPackageName());

        if (imageResource == 0) {
            imageResource = R.drawable.flags_unknown;
        }

        ImageView currentCountryFlag = (ImageView) countryNavigationView.findViewById(R.id.selectedBadgeImage);
        currentCountryFlag.setVisibility(View.VISIBLE);
        currentCountryFlag.setImageResource(imageResource);

        TextView currentCountryText = (TextView) this.findViewById(R.id.navigationCountry).findViewById(R.id.selected);
        currentCountryText.setText(countryFullName);
    }

    protected void setNavigationPublication(String publicationFullName, String selectedPublication) {
        View publicationNavigationView = this.findViewById(R.id.navigationPublication);
        TextView currentPublicationBadgeText = (TextView) publicationNavigationView.findViewById(R.id.selectedBadge);

        if (selectedPublication == null) {
            publicationNavigationView.setVisibility(View.INVISIBLE);
        }
        else {
            publicationNavigationView.setVisibility(View.VISIBLE);

            currentPublicationBadgeText.setText(selectedPublication.split("/")[1]);

            TextView currentPublicationText = (TextView) publicationNavigationView.findViewById(R.id.selected);
            currentPublicationText.setText(publicationFullName);
        }
    }
}
