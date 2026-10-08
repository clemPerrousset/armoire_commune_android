package fr.larmoirecommune.app.utils

import android.content.Context
import androidx.core.content.ContextCompat
import fr.larmoirecommune.app.R
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/** Plan IGN v2 (Géoplateforme, tuiles publiques, sans clé). */
object IgnPlanTileSource : OnlineTileSourceBase(
    "IGN-PlanV2", 0, 19, 256, ".png",
    arrayOf("https://data.geopf.fr/wmts"),
    "© IGN"
) {
    override fun getTileURLString(pMapTileIndex: Long): String =
        baseUrl + "?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0" +
            "&LAYER=GEOGRAPHICALGRIDSYSTEMS.PLANIGNV2&STYLE=normal&FORMAT=image/png" +
            "&TILEMATRIXSET=PM" +
            "&TILEMATRIX=${MapTileIndex.getZoom(pMapTileIndex)}" +
            "&TILEROW=${MapTileIndex.getY(pMapTileIndex)}" +
            "&TILECOL=${MapTileIndex.getX(pMapTileIndex)}"
}

fun MapView.useIgnStyle() {
    setTileSource(IgnPlanTileSource)
    minZoomLevel = 5.0
    maxZoomLevel = 19.0
}

/** Applique le pin de l'app (pointe en bas au centre) à un marqueur. */
fun Marker.useAppPin(context: Context) {
    icon = ContextCompat.getDrawable(context, R.drawable.ic_map_pin)
    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
}
