package app.musicapp.music.ui.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialcab.MaterialCab;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import app.musicapp.music.adapter.song.SongAdapter;
import app.musicapp.music.helper.MusicPlayerRemote;
import app.musicapp.music.interfaces.CabHolder;
import app.musicapp.music.interfaces.LoaderIds;
import app.musicapp.music.loader.GenreLoader;
import app.musicapp.music.misc.WrappedAsyncTaskLoader;
import app.musicapp.music.model.Genre;
import app.musicapp.music.model.Song;
import app.musicapp.music.ui.activities.base.AbsSlidingMusicPanelActivity;
import app.musicapp.music.util.Music_AppColorUtil;
import app.musicapp.music.util.ViewUtil;
import app.musicapp.music.R;

/*
 * @author Karim Abou Zeid (kabouzeid)
 *
 * This file is part of Music App.

    Music App is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Music App is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Music App.  If not, see <https://www.gnu.org/licenses/>.
 */

public class GenreDetailActivity extends AbsSlidingMusicPanelActivity implements CabHolder, LoaderManager.LoaderCallbacks<List<Song>> {

    public static final String EXTRA_GENRE = "extra_genre";
    private static final int LOADER_ID = LoaderIds.GENRE_DETAIL_ACTIVITY;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(android.R.id.empty)
    TextView empty;

    private Genre genre;

    private MaterialCab cab;
    private SongAdapter adapter;

    private RecyclerView.Adapter wrappedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDrawUnderStatusbar();
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        genre = getIntent().getExtras().getParcelable(EXTRA_GENRE);

        setUpRecyclerView();

        setUpToolBar();

        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanel(R.layout.activity_genre_detail);
    }

    private void setUpRecyclerView() {
        ViewUtil.setUpFastScrollRecyclerViewColor(this, ((FastScrollRecyclerView) recyclerView), ThemeStore.accentColor(this));
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SongAdapter(this, new ArrayList<>(), R.layout.item_list, false, this);
        recyclerView.setAdapter(adapter);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkIsEmpty();
            }
        });
    }

    private void setUpToolBar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        toolbar.setTitleTextAppearance(this, R.style.ToolbarTextAppearace);
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(genre.name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_genre_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_shuffle_genre:
                MusicPlayerRemote.openAndShuffleQueue(adapter.getDataSet(), true);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public MaterialCab openCab(final int menu, final MaterialCab.Callback callback) {
        if (cab != null && cab.isActive()) cab.finish();
        cab = new MaterialCab(this, R.id.cab_stub)
                .setMenu(menu)
                .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
                .setBackgroundColor(Music_AppColorUtil.shiftBackgroundColorForLightText(ThemeStore.primaryColor(this)))
                .start(callback);
        return cab;
    }

    @Override
    public void onBackPressed() {
        if (cab != null && cab.isActive()) cab.finish();
        else {
            recyclerView.stopScroll();
            super.onBackPressed();
        }
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
        getSupportLoaderManager().restartLoader(LOADER_ID, null, this);
    }

    private void checkIsEmpty() {
        empty.setVisibility(
                adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE
        );
    }

    @Override
    protected void onDestroy() {
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
            recyclerView = null;
        }

        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter);
            wrappedAdapter = null;
        }
        adapter = null;

        super.onDestroy();
    }

    @Override
    @NonNull
    public Loader<List<Song>> onCreateLoader(int id, Bundle args) {
        return new GenreDetailActivity.AsyncGenreSongLoader(this, genre);
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Song>> loader, List<Song> data) {
        if (adapter != null)
            adapter.swapDataSet(data);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Song>> loader) {
        if (adapter != null)
            adapter.swapDataSet(new ArrayList<>());
    }

    private static class AsyncGenreSongLoader extends WrappedAsyncTaskLoader<List<Song>> {
        private final Genre genre;

        public AsyncGenreSongLoader(Context context, Genre genre) {
            super(context);
            this.genre = genre;
        }

        @Override
        public List<Song> loadInBackground() {
            return GenreLoader.getSongs(getContext(), genre.id);
        }
    }
}
