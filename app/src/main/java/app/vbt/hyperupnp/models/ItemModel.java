package app.vbt.hyperupnp.models;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.preference.PreferenceManager;

import java.util.List;

import app.vbt.hyperupnp.R;
import app.vbt.hyperupnp.upnp.cling.model.meta.Service;
import app.vbt.hyperupnp.upnp.cling.support.model.DIDLObject;
import app.vbt.hyperupnp.upnp.cling.support.model.Res;
import app.vbt.hyperupnp.upnp.cling.support.model.container.Container;
import app.vbt.hyperupnp.upnp.cling.support.model.item.Item;

public class ItemModel extends CustomListItem {
    private final Context ctx;
    private final Service service;
    private final DIDLObject item;

    public ItemModel(Context ctx, int icon, Service service, DIDLObject item) {
        super(icon);

        this.ctx = ctx;
        this.service = service;
        this.item = item;
    }

    public String getUrl() {
        if (item == null)
            return "";

        Res resource = item.getFirstResource();

        if (resource == null || resource.getValue() == null)
            return "N/A";
        return resource.getValue();
    }


    public Item getItem() {
        if (isContainer())
            return null;

        return (Item) item;
    }

    public Container getContainer() {
        if (!isContainer())
            return null;

        return (Container) item;
    }

    public void Play() {
        String scp = "";
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
            Uri uri = Uri.parse(this.getUrl());
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uri, "video/*|audio/*|image/*");
            intent.putExtra("title", this.getTitle());
            scp = prefs.getString("settings_choose_player", "try_to_open");
            if (scp.equals("mpv")) {
                intent.setComponent(new ComponentName("is.xyz.mpv", "is.xyz.mpv.MPVActivity"));
            } else if (scp.equals("vlc")) {
                intent.setComponent(new ComponentName("org.videolan.vlc", "org.videolan.vlc.gui.video.VideoPlayerActivity"));
            }
            ctx.startActivity(intent);
        } catch (NullPointerException ex) {
            Toast.makeText(ctx, R.string.info_could_not_start_activity, Toast.LENGTH_SHORT).show();
        } catch (ActivityNotFoundException ex) {
            if (scp.equals("mpv")) {
                Toast.makeText(ctx, R.string.no_mpv_handler, Toast.LENGTH_SHORT).show();
            } else if (scp.equals("vlc")) {
                Toast.makeText(ctx, R.string.no_vlc_handler, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(ctx, R.string.info_no_handler, Toast.LENGTH_SHORT).show();
            }
        }
    }

    public Service getService() {
        return this.service;
    }

    public boolean isContainer() {
        return item instanceof Container;
    }

    @Override
    public String getId() {
        return item.getId();
    }

    @Override
    public String getTitle() {
        return item.getTitle();
    }

    @Override
    public String toString() {
        return item.getTitle();
    }

    @Override
    public String getDescription() {
        if (isContainer()) {
            Integer children = getContainer().getChildCount();

            if (children != null)
                return children + " " + ctx.getString(R.string.info_items);

            return ctx.getString(R.string.info_folder);
        }

        List<DIDLObject.Property> properties = item.getProperties();
        if (properties != null && properties.size() != 0) {
            for (DIDLObject.Property property : properties) {
                if (property.getDescriptorName().equalsIgnoreCase("date"))
                    return property.getValue().toString();
            }
        }

        List<Res> resources = item.getResources();
        if (resources != null && resources.size() != 0) {

            Res resource = item.getResources().get(0);
            String resolution = resource.getResolution();

            if (resolution != null)
                return resolution;

            String creator = item.getCreator();
            if (creator == null)
                return ctx.getString(R.string.info_file);

            if (creator.startsWith("Unknown"))
                return null;

            return creator;
        }

        return "N/A";
    }

    @Override
    public String getDescription2() {
        if (!isContainer()) {
            Uri uri = Uri.parse(getUrl());
            MimeTypeMap mime = MimeTypeMap.getSingleton();
            String ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            return mime.getMimeTypeFromExtension(ext);
        }

        String genre = item.getFirstPropertyValue(DIDLObject.Property.UPNP.GENRE.class);
        if (genre == null)
            return null;

        if (genre.startsWith("Unknown"))
            return null;

        return genre;
    }
}
