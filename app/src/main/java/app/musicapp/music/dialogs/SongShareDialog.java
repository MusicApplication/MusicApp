package app.musicapp.music.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;

import app.musicapp.music.model.Song;
import app.musicapp.music.util.MusicUtil;
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

public class SongShareDialog extends DialogFragment {
    @NonNull
    public static SongShareDialog create(final Song song) {
        final SongShareDialog dialog = new SongShareDialog();
        final Bundle args = new Bundle();
        args.putParcelable("song", song);
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Song song = getArguments().getParcelable("song");
        return new MaterialDialog.Builder(getActivity())
                .title(R.string.what_do_you_want_to_share)
                .items(getString(R.string.the_audio_file))
                .itemsCallback((materialDialog, view, i, charSequence) -> {
                    switch (i) {
                        case 0:
                            startActivity(Intent.createChooser(MusicUtil.createShareSongFileIntent(song, getContext()), null));
                            break;
                        case 1:
                            getActivity().startActivity(
                                    Intent.createChooser(
                                            new Intent()
                                                    .setAction(Intent.ACTION_SEND)
                                                    .putExtra(Intent.EXTRA_TEXT, R.string.currently_listening)
                                                    .setType("text/plain"),
                                            null
                                    )
                            );
                            break;
                    }
                })
                .build();
    }
}
