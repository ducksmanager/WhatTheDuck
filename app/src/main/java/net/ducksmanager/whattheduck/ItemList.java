package net.ducksmanager.whattheduck;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

import net.ducksmanager.inducks.coa.CountryListing;
import net.ducksmanager.inducks.coa.PublicationListing;
import net.ducksmanager.retrievetasks.CoverSearch;
import net.ducksmanager.util.CoverFlowFileHandler;
import net.ducksmanager.whattheduck.Collection.CollectionType;

import java.io.File;
import java.lang.ref.WeakReference;

import io.github.douglasjunior.androidSimpleTooltip.SimpleTooltip;

public abstract class ItemList<Item> extends AppCompatActivity {
    public static String type = CollectionType.USER.toString();
    private Boolean requiresDataDownload = false;

    private static final int REQUEST_IMAGE_CAPTURE = 1;

    protected abstract boolean needsToDownloadFullList();
    protected abstract void downloadFullList();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.wtd_list);

        RecyclerView recyclerView = this.findViewById(R.id.itemList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        View navigationAllCountries = this.findViewById(R.id.navigationAllCountries);
        if (navigationAllCountries != null) {
            navigationAllCountries.setOnClickListener(view ->
                goToView(CountryList.class)
            );
        }

        View navigationCurrentCountry = this.findViewById(R.id.navigationCountry);
        if (navigationCurrentCountry != null) {
            navigationCurrentCountry.findViewById(R.id.selected).setOnClickListener(view ->
                goToView(PublicationList.class)
            );
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadList();
    }

    private void loadList() {
        ((WhatTheDuckApplication) getApplication()).trackActivity(this);

        if (needsToDownloadFullList()) {
            this.requiresDataDownload = true;
            downloadFullList();
        }

        FloatingActionMenu addToCollection = this.findViewById(R.id.addToCollectionWrapper);
        if (addToCollection != null) {
            addToCollection.setMenuButtonColorNormalResId(R.color.fab_color);
            addToCollection.setMenuButtonColorPressedResId(R.color.fab_color);
            addToCollection.setVisibility(type.equals(CollectionType.USER.toString()) ? View.VISIBLE : View.GONE);
            addToCollection.close(false);

            if (type.equals(CollectionType.USER.toString())) {
                FloatingActionButton addToCollectionByPhotoButton = this.findViewById(R.id.addToCollectionByPhotoButton);
                addToCollectionByPhotoButton.setOnClickListener(view ->
                    takeCoverPicture()
                );

                FloatingActionButton addToCollectionBySelectionButton = this.findViewById(R.id.addToCollectionBySelectionButton);
                addToCollectionBySelectionButton.setOnClickListener(view -> {
                    addToCollection.setVisibility(View.GONE);
                    ItemList.this.goToAlternativeView(CollectionType.COA.toString());
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
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (type.equals(CollectionType.USER.toString())) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
            else {
                actionBar.setDisplayHomeAsUpEnabled(true);
                ((Toolbar) findViewById(R.id.toolbar)).setNavigationOnClickListener(v -> {
                    onBackFromAddIssueActivity();
                });
            }
        }

        setTitle(
            type.equals(CollectionType.USER.toString())
                    ? getString(R.string.my_collection)
                    : getString(R.string.select_issue)
        );
    }

    protected abstract boolean userHasItemsInCollectionForCurrent();

    private void goToView(Class<?> cls) {
        if (!ItemList.this.getClass().equals(cls)) {
            startActivity(new Intent(WhatTheDuck.wtd, cls));
        }
    }

    void goToAlternativeView(String collectionType) {
        type = collectionType;
        loadList();
        show();
    }

    protected abstract boolean shouldShow();

    protected abstract ItemAdapter<Item> getItemAdapter();

    void show() {
        if (!shouldShow()) {
            return;
        }
        ItemAdapter<Item> itemAdapter = getItemAdapter();
        setNavigation(WhatTheDuck.getSelectedCountry(), WhatTheDuck.getSelectedPublication());

        ProgressBar loadingProgressBar = this.findViewById(R.id.progressBarLoading);
        TextView emptyListText = this.findViewById(R.id.emptyList);

        java.util.List<Item> items = itemAdapter.getItems();
        if (this.requiresDataDownload) {
            itemAdapter.resetItems();
            if (emptyListText != null) {
                emptyListText.setVisibility(TextView.INVISIBLE);
            }
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(TextView.VISIBLE);
            }
        }
        else {
            if (emptyListText != null) {
                emptyListText.setVisibility(items.size() == 0 ? TextView.VISIBLE : TextView.INVISIBLE);
            }
            if (loadingProgressBar != null) {
                loadingProgressBar.setVisibility(TextView.INVISIBLE);
            }
        }

        RecyclerView recyclerView = findViewById(R.id.itemList);
        recyclerView.setAdapter(itemAdapter);

        EditText filterEditText = this.findViewById(R.id.filter);
        if (filterEditText != null) {
            if (items.size() > 20) {
                filterEditText.setVisibility(EditText.VISIBLE);
                filterEditText.setText("");

                filterEditText.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {}
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        itemAdapter.updateFilteredList(s.toString());
                    }
                });
            } else {
                filterEditText.setVisibility(EditText.GONE);
            }
        }

        DividerItemDecoration dividerDecoration = new DividerItemDecoration(
            recyclerView.getContext(),
            new LinearLayoutManager(this).getOrientation()
        );
        recyclerView.addItemDecoration(dividerDecoration);
    }

    private void takeCoverPicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            CoverFlowFileHandler.current = new CoverFlowFileHandler();
            Uri photoURI = CoverFlowFileHandler.current.createEmptyFileForCamera(ItemList.this);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            this.findViewById(R.id.addToCollectionWrapper).setVisibility(View.GONE);
            this.findViewById(R.id.progressBarLoading).setVisibility(View.VISIBLE);

            CoverFlowFileHandler.current.resizeUntilFileSize(this, new CoverFlowFileHandler.TransformationCallback() {
                @Override
                public void onComplete(File fileToUpload) {
                    new CoverSearch(new WeakReference<>(ItemList.this), fileToUpload).execute();
                }

                @Override
                public void onFail() {
                    ItemList.this.findViewById(R.id.addToCollectionWrapper).setVisibility(View.VISIBLE);
                    ItemList.this.findViewById(R.id.progressBarLoading).setVisibility(View.GONE);
                }
            });
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent i = null;

        switch (item.getItemId()) {
            case R.id.action_logout:
                WhatTheDuck.userCollection = new Collection();
                WhatTheDuck.coaCollection = new Collection();
                WhatTheDuck.setUsername(null);
                WhatTheDuck.setPassword(null);
                WhatTheDuck.saveSettings(false);
                i = new Intent(WhatTheDuck.wtd, WhatTheDuck.class);

                break;
            case R.id.action_about:
                WhatTheDuck.showAbout(this);

        }
        if (i == null) {
            return super.onOptionsItemSelected(item);
        }
        else {
            startActivity(i);
            return true;
        }
    }

    Collection getCollection() {
        return type == null || type.equals(CollectionType.USER.toString())
            ? WhatTheDuck.userCollection
            : WhatTheDuck.coaCollection;
    }

    private void setNavigation(String selectedCountry, String selectedPublication) {
        final View generalNavigationView = this.findViewById(R.id.navigation);
        final View countryNavigationView = this.findViewById(R.id.navigationCountry);
        final View publicationNavigationView = this.findViewById(R.id.navigationPublication);

        if (generalNavigationView != null) {
            generalNavigationView.setVisibility(selectedCountry == null ? View.GONE : View.VISIBLE);
            publicationNavigationView.setVisibility(selectedPublication == null ? View.INVISIBLE : View.VISIBLE);

            if (selectedCountry != null) {
                final String countryFullName = CountryListing.getCountryFullName(selectedCountry);

                String uri = "@drawable/flags_" + selectedCountry;
                int imageResource = getResources().getIdentifier(uri, null, getPackageName());

                if (imageResource == 0) {
                    imageResource = R.drawable.flags_unknown;
                }

                ImageView currentCountryFlag = countryNavigationView.findViewById(R.id.selectedBadgeImage);
                currentCountryFlag.setImageResource(imageResource);

                TextView currentCountryText = countryNavigationView.findViewById(R.id.selected);
                currentCountryText.setText(countryFullName);
            }

            if (selectedPublication != null) {
                final String publicationFullName = PublicationListing.getPublicationFullName(selectedCountry, selectedPublication);

                TextView currentPublicationBadgeText = publicationNavigationView.findViewById(R.id.selectedBadge);
                currentPublicationBadgeText.setText(selectedPublication.split("/")[1]);

                TextView currentPublicationText = publicationNavigationView.findViewById(R.id.selected);
                currentPublicationText.setText(publicationFullName);
            }
        }
    }

    void notifyCompleteList() {
        this.requiresDataDownload = false;
        this.show();
    }

    protected void onBackFromAddIssueActivity() {
        if (userHasItemsInCollectionForCurrent()) {
            goToAlternativeView(CollectionType.USER.toString());
        }
        else {
            type = CollectionType.USER.toString();
            startActivity(new Intent(WhatTheDuck.wtd, CountryList.class));
        }
    }
}
