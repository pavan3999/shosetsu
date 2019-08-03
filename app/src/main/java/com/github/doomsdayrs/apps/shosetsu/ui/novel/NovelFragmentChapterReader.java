package com.github.doomsdayrs.apps.shosetsu.ui.novel;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.Doomsdayrs.api.novelreader_core.services.core.dep.Formatter;
import com.github.doomsdayrs.apps.shosetsu.R;
import com.github.doomsdayrs.apps.shosetsu.backend.async.NovelFragmentChapterViewLoad;
import com.github.doomsdayrs.apps.shosetsu.backend.database.Database;
import com.github.doomsdayrs.apps.shosetsu.ui.listeners.NovelFragmentChapterViewHideBar;
import com.github.doomsdayrs.apps.shosetsu.variables.DefaultScrapers;
import com.github.doomsdayrs.apps.shosetsu.variables.Settings;
import com.github.doomsdayrs.apps.shosetsu.variables.enums.Status;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.github.doomsdayrs.apps.shosetsu.backend.Utilities.changeIndentSize;
import static com.github.doomsdayrs.apps.shosetsu.backend.Utilities.changeParagraphSpacing;
import static com.github.doomsdayrs.apps.shosetsu.backend.Utilities.isReaderNightMode;
import static com.github.doomsdayrs.apps.shosetsu.backend.Utilities.isTapToScroll;
import static com.github.doomsdayrs.apps.shosetsu.backend.Utilities.setTextSize;
import static com.github.doomsdayrs.apps.shosetsu.backend.Utilities.swapReaderColor;
import static com.github.doomsdayrs.apps.shosetsu.backend.Utilities.toggleBookmarkChapter;
import static com.github.doomsdayrs.apps.shosetsu.backend.Utilities.toggleTapToScroll;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 * ====================================================================
 * Shosetsu
 * 9 / June / 2019
 *
 * @author github.com/doomsdayrs
 */
//TODO MarkDown support
public class NovelFragmentChapterReader extends AppCompatActivity {
    private String title;
    public ScrollView scrollView;
    public TextView textView;
    public ProgressBar progressBar;
    public Formatter formatter;
    public String chapterURL;
    private String novelURL;
    public String unformattedText = null;
    public String text = null;

    private MenuItem bookmark;
    private boolean bookmarked;

    private MenuItem tap_to_scroll;

    private MenuItem textSmall;
    private MenuItem textMedium;
    private MenuItem textLarge;

    private MenuItem pspaceNon;
    private MenuItem pspaceSmall;
    private MenuItem pspaceMedium;
    private MenuItem pspaceLarge;


    private MenuItem ispaceNon;
    private MenuItem ispaceSmall;
    private MenuItem ispaceMedium;
    private MenuItem ispaceLarge;


    //Tap to scroll
    private View scroll_up;
    private View scroll_down;

    //TODO FIX THE GOD DAM SCROLL FEATURE WTF HAPPENEEDDDDDD


    // ERROR SCREEN
    //TODO Handle ERRORs on loading, EVERYWHERE
    public ConstraintLayout errorView;
    public TextView errorMessage;
    public Button errorButton;

    /**
     * Save data of view before destroyed
     *
     * @param outState output save
     */
    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("unformattedText", text);
        outState.putString("text", text);
        outState.putString("chapterURL", chapterURL);
        outState.putInt("formatter", formatter.getID());
        outState.putString("novelURL", novelURL);
        outState.putString("title", title);
    }

    /**
     * Creates the option menu (on the top toolbar)
     *
     * @param menu Menu reference to fill
     * @return if made
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();

        inflater.inflate(R.menu.toolbar_chapter_view, menu);
        // Night mode
        menu.findItem(R.id.chapter_view_nightMode).setChecked(isReaderNightMode());

        // Bookmark
        {
            bookmark = menu.findItem(R.id.chapter_view_bookmark);
            bookmarked = Database.DatabaseChapter.isBookMarked(chapterURL);
            if (bookmarked)
                bookmark.setIcon(R.drawable.ic_bookmark_black_24dp);

        }

        // Tap To Scroll
        {
            tap_to_scroll = menu.findItem(R.id.tap_to_scroll);
            tap_to_scroll.setChecked(isTapToScroll());
        }
        // Text size
        {
            textSmall = menu.findItem(R.id.chapter_view_textSize_small);
            textMedium = menu.findItem(R.id.chapter_view_textSize_medium);
            textLarge = menu.findItem(R.id.chapter_view_textSize_large);

            switch ((int) Settings.ReaderTextSize) {
                default:
                    setTextSize(14);
                case 14:
                    textSmall.setChecked(true);
                    break;
                case 17:
                    textMedium.setChecked(true);
                    break;
                case 20:
                    textLarge.setChecked(true);
                    break;
            }
        }

        // Paragraph Space
        {
            pspaceNon = menu.findItem(R.id.chapter_view_paragraphSpace_none);
            pspaceSmall = menu.findItem(R.id.chapter_view_paragraphSpace_small);
            pspaceMedium = menu.findItem(R.id.chapter_view_paragraphSpace_medium);
            pspaceLarge = menu.findItem(R.id.chapter_view_paragraphSpace_large);

            switch (Settings.paragraphSpacing) {
                case 0:
                    pspaceNon.setChecked(true);
                    break;
                case 1:
                    pspaceSmall.setChecked(true);
                    break;
                case 2:
                    pspaceMedium.setChecked(true);
                    break;
                case 3:
                    pspaceLarge.setChecked(true);
                    break;
            }
        }

        // Indent Space
        {
            ispaceNon = menu.findItem(R.id.chapter_view_indent_none);
            ispaceSmall = menu.findItem(R.id.chapter_view_indent_small);
            ispaceMedium = menu.findItem(R.id.chapter_view_indent_medium);
            ispaceLarge = menu.findItem(R.id.chapter_view_indent_large);

            switch (Settings.indentSize) {
                case 0:
                    ispaceNon.setChecked(true);
                    break;
                case 1:
                    ispaceSmall.setChecked(true);
                    break;
                case 2:
                    ispaceMedium.setChecked(true);
                    break;
                case 3:
                    ispaceLarge.setChecked(true);
                    break;
            }
        }


        return true;
    }

    /**
     * Changes the theme of the reader
     * TODO change the scroll position bars color
     */
    public void setUpReader() {
        scrollView.setBackgroundColor(Settings.ReaderTextBackgroundColor);
        textView.setBackgroundColor(Settings.ReaderTextBackgroundColor);
        textView.setTextColor(Settings.ReaderTextColor);
        textView.setTextSize(Settings.ReaderTextSize);

        if (unformattedText != null) {
            StringBuilder replaceSpacing = new StringBuilder("\n");
            for (int x = 0; x < Settings.paragraphSpacing; x++)
                replaceSpacing.append("\n");

            for (int x = 0; x < Settings.indentSize; x++)
                replaceSpacing.append("\t");

            text = unformattedText.replaceAll("\n", replaceSpacing.toString());
            textView.setText(text);
        }
    }


    /**
     * What to do when an menu item is selected
     *
     * @param item item selected
     * @return true if processed
     */
    @Override
    public boolean onOptionsItemSelected(@NotNull MenuItem item) {
        Log.d("item", item.toString());
        System.out.println("Text size" + textView.getTextSize());
        switch (item.getItemId()) {
            case R.id.chapter_view_nightMode:
                if (!item.isChecked()) {
                    swapReaderColor();
                    setUpReader();
                } else {
                    swapReaderColor();
                    setUpReader();
                }
                item.setChecked(!item.isChecked());
                return true;
            case R.id.tap_to_scroll:
                tap_to_scroll.setChecked(toggleTapToScroll());
                return true;
            case R.id.chapter_view_bookmark:


                bookmarked = toggleBookmarkChapter(chapterURL);
                if (bookmarked)
                    bookmark.setIcon(R.drawable.ic_bookmark_black_24dp);
                else bookmark.setIcon(R.drawable.ic_bookmark_border_black_24dp);
                return true;

            case R.id.chapter_view_textSize_small:
                setTextSize(14);
                setUpReader();

                item.setChecked(true);
                textMedium.setChecked(false);
                textLarge.setChecked(false);
                return true;
            case R.id.chapter_view_textSize_medium:
                setTextSize(17);
                setUpReader();

                item.setChecked(true);
                textSmall.setChecked(false);
                textLarge.setChecked(false);
                return true;
            case R.id.chapter_view_textSize_large:
                setTextSize(20);
                setUpReader();
                item.setChecked(true);
                textSmall.setChecked(false);
                textMedium.setChecked(false);
                return true;

            case R.id.chapter_view_paragraphSpace_none:
                changeParagraphSpacing(0);
                setUpReader();
                pspaceNon.setChecked(true);
                pspaceSmall.setChecked(false);
                pspaceMedium.setChecked(false);
                pspaceLarge.setChecked(false);
                return true;
            case R.id.chapter_view_paragraphSpace_small:
                changeParagraphSpacing(1);
                setUpReader();
                pspaceNon.setChecked(false);
                pspaceSmall.setChecked(true);
                pspaceMedium.setChecked(false);
                pspaceLarge.setChecked(false);
                return true;
            case R.id.chapter_view_paragraphSpace_medium:
                changeParagraphSpacing(2);
                setUpReader();
                pspaceNon.setChecked(false);
                pspaceSmall.setChecked(false);
                pspaceMedium.setChecked(true);
                pspaceLarge.setChecked(false);
                return true;
            case R.id.chapter_view_paragraphSpace_large:
                changeParagraphSpacing(3);
                setUpReader();
                pspaceNon.setChecked(false);
                pspaceSmall.setChecked(false);
                pspaceMedium.setChecked(false);
                pspaceLarge.setChecked(true);
                return true;


            case R.id.chapter_view_indent_none:
                changeIndentSize(0);
                setUpReader();
                ispaceNon.setChecked(true);
                ispaceSmall.setChecked(false);
                ispaceMedium.setChecked(false);
                ispaceLarge.setChecked(false);
                return true;
            case R.id.chapter_view_indent_small:
                changeIndentSize(1);
                setUpReader();
                ispaceNon.setChecked(false);
                ispaceSmall.setChecked(true);
                ispaceMedium.setChecked(false);
                ispaceLarge.setChecked(false);
                return true;
            case R.id.chapter_view_indent_medium:
                changeIndentSize(2);
                setUpReader();
                ispaceNon.setChecked(false);
                ispaceSmall.setChecked(false);
                ispaceMedium.setChecked(true);
                ispaceLarge.setChecked(false);
                return true;
            case R.id.chapter_view_indent_large:
                changeIndentSize(3);
                setUpReader();
                ispaceNon.setChecked(false);
                ispaceSmall.setChecked(false);
                ispaceMedium.setChecked(false);
                ispaceLarge.setChecked(true);
                return true;
        }
        return false;
    }


    /**
     * Create method
     *
     * @param savedInstanceState save object
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("OnCreate", "NovelFragmentChapterReader");
        switch (Settings.themeMode) {
            case 0:
                setTheme(R.style.Theme_MaterialComponents_Light_NoActionBar);
                break;
            case 1:
                setTheme(R.style.Theme_MaterialComponents_NoActionBar);
                break;
            case 2:
                setTheme(R.style.ThemeOverlay_MaterialComponents_Dark);
        }
        setContentView(R.layout.fragment_novel_chapter_reader);
        {
            errorView = findViewById(R.id.network_error);
            errorMessage = findViewById(R.id.error_message);
            errorButton = findViewById(R.id.error_button);
            progressBar = findViewById(R.id.fragment_novel_chapter_view_progress);
            scrollView = findViewById(R.id.fragment_novel_scroll);
            scroll_up = findViewById(R.id.scroll_up);
            scroll_down = findViewById(R.id.scroll_down);
        }

        {
            novelURL = getIntent().getStringExtra("novelURL");
            title = getIntent().getStringExtra("title");
            Toolbar toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            formatter = DefaultScrapers.formatters.get(getIntent().getIntExtra("formatter", -1) - 1);
            /*
             * Chooses the way the way to save the scroll position
             * the before marshmallow version is untested
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                scrollView.setOnScrollChangeListener((v, scrollX, scrollY, oldScrollX, oldScrollY) -> bottom());
            } else {
                scrollView.getViewTreeObserver().addOnScrollChangedListener(this::bottom);
            }


            textView = findViewById(R.id.fragment_novel_chapter_view_text);
            textView.setOnClickListener(new NovelFragmentChapterViewHideBar(toolbar));

            scroll_up.setOnClickListener(view -> {
                if (isTapToScroll()) {
                    Log.i("Scroll", "Up");
                    int y = scrollView.getScrollY();
                    if (y - 100 > 0)
                        y -= 100;
                    else y = 0;
                    scrollView.setScrollY(y);
                }
            });

            scroll_up.setOnClickListener(view -> {
                if (isTapToScroll()) {
                    Log.i("Scroll", "Down");
                    int y = scrollView.getScrollY();
                    int my = scrollView.getMaxScrollAmount();
                    if (y + 100 < my)
                        y += 100;
                    else y = my;
                    scrollView.setScrollY(y);
                }
            });
        }

        if (savedInstanceState != null) {
            unformattedText = savedInstanceState.getString("unformattedText");
            title = savedInstanceState.getString("title");
            chapterURL = savedInstanceState.getString("chapterURL");
            formatter = DefaultScrapers.formatters.get(savedInstanceState.getInt("formatter") - 1);
            text = savedInstanceState.getString("text");
        } else chapterURL = getIntent().getStringExtra("chapterURL");

        Database.DatabaseChapter.setChapterStatus(chapterURL, Status.READING);

        Log.d("novelURL", Objects.requireNonNull(chapterURL));

        if (Database.DatabaseChapter.isSaved(chapterURL)) {
            unformattedText = Objects.requireNonNull(Database.DatabaseChapter.getSavedNovelPassage(chapterURL));
            int y = Database.DatabaseChapter.getY(chapterURL);
            scrollView.scrollTo(0, y);
        } else if (text == null)
            if (chapterURL != null) {
                new NovelFragmentChapterViewLoad(this).execute();
            }

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(title);

        setUpReader();
    }

    public void bottom() {
        if (scrollView.canScrollVertically(1)) {
            int y = scrollView.getScrollY();
            if (y % 5 == 0) {
                Log.d("ScrollSave", Integer.toString(y));
                if (chapterURL != null)
                    Database.DatabaseChapter.updateY(chapterURL, y);
            }
        } else {
            Log.i("Scroll", "Marking chapter as READ");
            if (chapterURL != null)
                Database.DatabaseChapter.setChapterStatus(chapterURL, Status.READ);
            //TODO Get total word count of passage, then add to a storage counter that memorizes the total (Chapters read, Chapters Unread, Chapters reading, Word count)
        }
    }
}
