package com.unan.nexxxup.utils

import com.fasterxml.jackson.annotation.JsonIgnore
import com.unan.nexxxup.AudioFile
import com.unan.nexxxup.IDownloadableMinimum
import com.unan.nexxxup.SubtitleFile
import com.unan.nexxxup.USER_AGENT
import com.unan.nexxxup.app
import com.unan.nexxxup.extractors.Acefile
import com.unan.nexxxup.extractors.Ahvsh
import com.unan.nexxxup.extractors.Aico
import com.unan.nexxxup.extractors.Asnwish
import com.unan.nexxxup.extractors.Auvexiug
import com.unan.nexxxup.extractors.Awish
import com.unan.nexxxup.extractors.BgwpCC
import com.unan.nexxxup.extractors.BigwarpArt
import com.unan.nexxxup.extractors.BigwarpIO
import com.unan.nexxxup.extractors.Blogger
import com.unan.nexxxup.extractors.ByseSX
import com.unan.nexxxup.extractors.Bysezejataos
import com.unan.nexxxup.extractors.ByseBuho
import com.unan.nexxxup.extractors.ByseVepoin
import com.unan.nexxxup.extractors.ByseQekaho
import com.unan.nexxxup.extractors.Cavanhabg
import com.unan.nexxxup.extractors.Cda
import com.unan.nexxxup.extractors.Cdnplayer
import com.unan.nexxxup.extractors.CdnwishCom
import com.unan.nexxxup.extractors.CloudMailRu
import com.unan.nexxxup.extractors.ContentX
import com.unan.nexxxup.extractors.CsstOnline
import com.unan.nexxxup.extractors.D0000d
import com.unan.nexxxup.extractors.D000dCom
import com.unan.nexxxup.extractors.DBfilm
import com.unan.nexxxup.extractors.Dailymotion
import com.unan.nexxxup.extractors.DatabaseGdrive
import com.unan.nexxxup.extractors.DatabaseGdrive2
import com.unan.nexxxup.extractors.Dhcplay
import com.unan.nexxxup.extractors.Dhtpre
import com.unan.nexxxup.extractors.Dokicloud
import com.unan.nexxxup.extractors.DoodCxExtractor
import com.unan.nexxxup.extractors.DoodLaExtractor
import com.unan.nexxxup.extractors.DoodPmExtractor
import com.unan.nexxxup.extractors.DoodShExtractor
import com.unan.nexxxup.extractors.DoodSoExtractor
import com.unan.nexxxup.extractors.DoodToExtractor
import com.unan.nexxxup.extractors.DoodWatchExtractor
import com.unan.nexxxup.extractors.DoodWfExtractor
import com.unan.nexxxup.extractors.DoodWsExtractor
import com.unan.nexxxup.extractors.DoodYtExtractor
import com.unan.nexxxup.extractors.Doodspro
import com.unan.nexxxup.extractors.Dsvplay
import com.unan.nexxxup.extractors.Doodporn
import com.unan.nexxxup.extractors.DoodstreamCom
import com.unan.nexxxup.extractors.Dooood
import com.unan.nexxxup.extractors.Ds2play
import com.unan.nexxxup.extractors.Ds2video
import com.unan.nexxxup.extractors.DsstOnline
import com.unan.nexxxup.extractors.Dumbalag
import com.unan.nexxxup.extractors.Dwish
import com.unan.nexxxup.extractors.Embedgram
import com.unan.nexxxup.extractors.EmturbovidExtractor
import com.unan.nexxxup.extractors.Evoload
import com.unan.nexxxup.extractors.Evoload1
import com.unan.nexxxup.extractors.Ewish
import com.unan.nexxxup.extractors.FEmbed
import com.unan.nexxxup.extractors.FEnet
import com.unan.nexxxup.extractors.Fastream
import com.unan.nexxxup.extractors.FeHD
import com.unan.nexxxup.extractors.Fembed9hd
import com.unan.nexxxup.extractors.FileMoon
import com.unan.nexxxup.extractors.FileMoonIn
import com.unan.nexxxup.extractors.FileMoonSx
import com.unan.nexxxup.extractors.FilemoonV2
import com.unan.nexxxup.extractors.Filesim
import com.unan.nexxxup.extractors.Multimoviesshg
import com.unan.nexxxup.extractors.FlaswishCom
import com.unan.nexxxup.extractors.FourCX
import com.unan.nexxxup.extractors.FourPichive
import com.unan.nexxxup.extractors.FourPlayRu
import com.unan.nexxxup.extractors.Fplayer
import com.unan.nexxxup.extractors.FsstOnline
import com.unan.nexxxup.extractors.GDMirrorbot
import com.unan.nexxxup.extractors.GUpload
import com.unan.nexxxup.extractors.GamoVideo
import com.unan.nexxxup.extractors.Gdriveplayer
import com.unan.nexxxup.extractors.Gdriveplayerapi
import com.unan.nexxxup.extractors.Gdriveplayerapp
import com.unan.nexxxup.extractors.Gdriveplayerbiz
import com.unan.nexxxup.extractors.Gdriveplayerco
import com.unan.nexxxup.extractors.Gdriveplayerfun
import com.unan.nexxxup.extractors.Gdriveplayerio
import com.unan.nexxxup.extractors.Gdriveplayerme
import com.unan.nexxxup.extractors.Gdriveplayerorg
import com.unan.nexxxup.extractors.Gdriveplayerus
import com.unan.nexxxup.extractors.Geodailymotion
import com.unan.nexxxup.extractors.Gofile
import com.unan.nexxxup.extractors.GoodstreamExtractor
import com.unan.nexxxup.extractors.Guccihide
import com.unan.nexxxup.extractors.Guxhag
import com.unan.nexxxup.extractors.HDMomPlayer
import com.unan.nexxxup.extractors.HDPlayerSystem
import com.unan.nexxxup.extractors.HDStreamAble
import com.unan.nexxxup.extractors.Habetar
import com.unan.nexxxup.extractors.Haxloppd
import com.unan.nexxxup.extractors.HglinkTo
import com.unan.nexxxup.extractors.HgplayCDN
import com.unan.nexxxup.extractors.Hotlinger
import com.unan.nexxxup.extractors.HubCloud
import com.unan.nexxxup.extractors.Hxfile
import com.unan.nexxxup.extractors.HlsWish
import com.unan.nexxxup.extractors.InternetArchive
import com.unan.nexxxup.extractors.JWPlayer
import com.unan.nexxxup.extractors.Jeniusplay
import com.unan.nexxxup.extractors.Jodwish
import com.unan.nexxxup.extractors.Keephealth
import com.unan.nexxxup.extractors.Kotakajair
import com.unan.nexxxup.extractors.Krakenfiles
import com.unan.nexxxup.extractors.Kswplayer
import com.unan.nexxxup.extractors.LayarKaca
import com.unan.nexxxup.extractors.Linkbox
import com.unan.nexxxup.extractors.LuluStream
import com.unan.nexxxup.extractors.Lulustream1
import com.unan.nexxxup.extractors.Lulustream2
import com.unan.nexxxup.extractors.Luluvdoo
import com.unan.nexxxup.extractors.Luxubu
import com.unan.nexxxup.extractors.Lvturbo
import com.unan.nexxxup.extractors.MailRu
import com.unan.nexxxup.extractors.Maxstream
import com.unan.nexxxup.extractors.Mediafire
import com.unan.nexxxup.extractors.Megacloud
import com.unan.nexxxup.extractors.MetaGnathTuggers
import com.unan.nexxxup.extractors.Minoplres
import com.unan.nexxxup.extractors.MixDrop
import com.unan.nexxxup.extractors.MixDropAg
import com.unan.nexxxup.extractors.MixDropBz
import com.unan.nexxxup.extractors.MixDropCh
import com.unan.nexxxup.extractors.MixDropTo
import com.unan.nexxxup.extractors.MixDropTop
import com.unan.nexxxup.extractors.MixDropPs
import com.unan.nexxxup.extractors.Mdy
import com.unan.nexxxup.extractors.MixDropSi
import com.unan.nexxxup.extractors.MxDropTo
import com.unan.nexxxup.extractors.Movhide
import com.unan.nexxxup.extractors.Moviehab
import com.unan.nexxxup.extractors.MoviehabNet
import com.unan.nexxxup.extractors.Moviesm4u
import com.unan.nexxxup.extractors.Mp4Upload
import com.unan.nexxxup.extractors.Multimovies
import com.unan.nexxxup.extractors.Mvidoo
import com.unan.nexxxup.extractors.MyVidPlay
import com.unan.nexxxup.extractors.Mwish
import com.unan.nexxxup.extractors.NathanFromSubject
import com.unan.nexxxup.extractors.Nekostream
import com.unan.nexxxup.extractors.Nekowish
import com.unan.nexxxup.extractors.Obeywish
import com.unan.nexxxup.extractors.Odnoklassniki
import com.unan.nexxxup.extractors.OkRuHTTP
import com.unan.nexxxup.extractors.OkRuHTTPMobile
import com.unan.nexxxup.extractors.OkRuSSL
import com.unan.nexxxup.extractors.OkRuSSLMobile
import com.unan.nexxxup.extractors.PeaceMakerst
import com.unan.nexxxup.extractors.Peytonepre
import com.unan.nexxxup.extractors.Pichive
import com.unan.nexxxup.extractors.PixelDrain
import com.unan.nexxxup.extractors.PixelDrainDev
import com.unan.nexxxup.extractors.PlayLtXyz
import com.unan.nexxxup.extractors.PlayRu
import com.unan.nexxxup.extractors.PlayerVoxzer
import com.unan.nexxxup.extractors.Playerwish
import com.unan.nexxxup.extractors.Rabbitstream
import com.unan.nexxxup.extractors.RapidVid
import com.unan.nexxxup.extractors.Rasacintaku
import com.unan.nexxxup.extractors.SBfull
import com.unan.nexxxup.extractors.Sbasian
import com.unan.nexxxup.extractors.Sbface
import com.unan.nexxxup.extractors.Sbflix
import com.unan.nexxxup.extractors.Sblona
import com.unan.nexxxup.extractors.Sblongvu
import com.unan.nexxxup.extractors.Sbnet
import com.unan.nexxxup.extractors.Sbrapid
import com.unan.nexxxup.extractors.Sbsonic
import com.unan.nexxxup.extractors.Sbspeed
import com.unan.nexxxup.extractors.Sbthe
import com.unan.nexxxup.extractors.SecvideoOnline
import com.unan.nexxxup.extractors.Sendvid
import com.unan.nexxxup.extractors.Server1uns
import com.unan.nexxxup.extractors.SfastwishCom
import com.unan.nexxxup.extractors.ShaveTape
import com.unan.nexxxup.extractors.SibNet
import com.unan.nexxxup.extractors.Simpulumlamerop
import com.unan.nexxxup.extractors.Smoothpre
import com.unan.nexxxup.extractors.Sobreatsesuyp
import com.unan.nexxxup.extractors.Ssbstream
import com.unan.nexxxup.extractors.StreamEmbed
import com.unan.nexxxup.extractors.MixPlayHD
import com.unan.nexxxup.extractors.MixTiger
import com.unan.nexxxup.extractors.CDNJWPlayer
import com.unan.nexxxup.extractors.DiskYandexComTr
import com.unan.nexxxup.extractors.DzenRu
import com.unan.nexxxup.extractors.JetPlayer
import com.unan.nexxxup.extractors.StreamHLS
import com.unan.nexxxup.extractors.StreamM4u
import com.unan.nexxxup.extractors.StreamSB
import com.unan.nexxxup.extractors.StreamSB1
import com.unan.nexxxup.extractors.StreamSB10
import com.unan.nexxxup.extractors.StreamSB11
import com.unan.nexxxup.extractors.StreamSB2
import com.unan.nexxxup.extractors.StreamSB3
import com.unan.nexxxup.extractors.StreamSB4
import com.unan.nexxxup.extractors.StreamSB5
import com.unan.nexxxup.extractors.StreamSB6
import com.unan.nexxxup.extractors.StreamSB7
import com.unan.nexxxup.extractors.StreamSB8
import com.unan.nexxxup.extractors.StreamSB9
import com.unan.nexxxup.extractors.StreamSilk
import com.unan.nexxxup.extractors.StreamTape
import com.unan.nexxxup.extractors.StreamTapeNet
import com.unan.nexxxup.extractors.StreamTapeXyz
import com.unan.nexxxup.extractors.Watchadsontape
import com.unan.nexxxup.extractors.StreamWishExtractor
import com.unan.nexxxup.extractors.StreamhideCom
import com.unan.nexxxup.extractors.StreamhideTo
import com.unan.nexxxup.extractors.Streamhub2
import com.unan.nexxxup.extractors.Streamix
import com.unan.nexxxup.extractors.Streamlare
import com.unan.nexxxup.extractors.StreamoUpload
import com.unan.nexxxup.extractors.Streamplay
import com.unan.nexxxup.extractors.Streamsss
import com.unan.nexxxup.extractors.Streamup
import com.unan.nexxxup.extractors.Streamwish2
import com.unan.nexxxup.extractors.Strwish
import com.unan.nexxxup.extractors.Strwish2
import com.unan.nexxxup.extractors.Supervideo
import com.unan.nexxxup.extractors.Swdyu
import com.unan.nexxxup.extractors.Swhoi
import com.unan.nexxxup.extractors.TRsTX
import com.unan.nexxxup.extractors.Tantifilm
import com.unan.nexxxup.extractors.TauVideo
import com.unan.nexxxup.extractors.Techinmind
import com.unan.nexxxup.extractors.Tubeless
import com.unan.nexxxup.extractors.Uasopt
import com.unan.nexxxup.extractors.Up4FunTop
import com.unan.nexxxup.extractors.Up4Stream
import com.unan.nexxxup.extractors.Upstream
import com.unan.nexxxup.extractors.UpstreamExtractor
import com.unan.nexxxup.extractors.Uqload
import com.unan.nexxxup.extractors.Uqload1
import com.unan.nexxxup.extractors.Uqload2
import com.unan.nexxxup.extractors.Uqloadcx
import com.unan.nexxxup.extractors.Uqloadbz
import com.unan.nexxxup.extractors.UqloadsXyz
import com.unan.nexxxup.extractors.Urochsunloath
import com.unan.nexxxup.extractors.Userload
import com.unan.nexxxup.extractors.Userscloud
import com.unan.nexxxup.extractors.Uservideo
import com.unan.nexxxup.extractors.Videa
import com.unan.nexxxup.extractors.Vicloud
import com.unan.nexxxup.extractors.VidHidePro
import com.unan.nexxxup.extractors.VidHidePro1
import com.unan.nexxxup.extractors.VidHidePro2
import com.unan.nexxxup.extractors.VidHidePro3
import com.unan.nexxxup.extractors.VidHidePro4
import com.unan.nexxxup.extractors.VidHidePro5
import com.unan.nexxxup.extractors.VidHidePro6
import com.unan.nexxxup.extractors.VidHideHub
import com.unan.nexxxup.extractors.Ryderjet
import com.unan.nexxxup.extractors.VidMoxy
import com.unan.nexxxup.extractors.VidStack
import com.unan.nexxxup.extractors.VideoSeyred
import com.unan.nexxxup.extractors.Videzz
import com.unan.nexxxup.extractors.Vidgomunime
import com.unan.nexxxup.extractors.Vidgomunimesb
import com.unan.nexxxup.extractors.VidhideExtractor
import com.unan.nexxxup.extractors.Vidmoly
import com.unan.nexxxup.extractors.Vidmolyme
import com.unan.nexxxup.extractors.Vidmolyto
import com.unan.nexxxup.extractors.Vidmolybiz
import com.unan.nexxxup.extractors.Vido
import com.unan.nexxxup.extractors.Vidoza
import com.unan.nexxxup.extractors.VinovoSi
import com.unan.nexxxup.extractors.VinovoTo
import com.unan.nexxxup.extractors.VidNest
import com.unan.nexxxup.extractors.Vidara
import com.unan.nexxxup.extractors.Vide0Net
import com.unan.nexxxup.extractors.Vidsonic
import com.unan.nexxxup.extractors.VkExtractor
import com.unan.nexxxup.extractors.Voe
import com.unan.nexxxup.extractors.Voe1
import com.unan.nexxxup.extractors.Vtbe
import com.unan.nexxxup.extractors.WishembedPro
import com.unan.nexxxup.extractors.Wishfast
import com.unan.nexxxup.extractors.Wishonly
import com.unan.nexxxup.extractors.XStreamCdn
import com.unan.nexxxup.extractors.Xenolyzb
import com.unan.nexxxup.extractors.Yipsu
import com.unan.nexxxup.extractors.YourUpload
import com.unan.nexxxup.extractors.YoutubeExtractor
import com.unan.nexxxup.extractors.YoutubeMobileExtractor
import com.unan.nexxxup.extractors.YoutubeNoCookieExtractor
import com.unan.nexxxup.extractors.YoutubeShortLinkExtractor
import com.unan.nexxxup.extractors.Yufiles
import com.unan.nexxxup.extractors.Yuguaab
import com.unan.nexxxup.extractors.Zplayer
import com.unan.nexxxup.extractors.ZplayerV2
import com.unan.nexxxup.extractors.Ztreamhub
import com.unan.nexxxup.mvvm.logError
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import me.xdrop.fuzzywuzzy.FuzzySearch
import org.jsoup.Jsoup
import java.net.URI
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException

/**
 * For use in the ConcatenatingMediaSource.
 * If features are missing (headers), please report and we can add it.
 * @param durationUs use Long.toUs() for easier input
 * */
data class PlayListItem(
    val url: String,
    val durationUs: Long,
)

/**
 * Converts Seconds to MicroSeconds, multiplication by 1_000_000
 * */
fun Long.toUs(): Long {
    return this * 1_000_000
}

/**
 * If your site has an unorthodox m3u8-like system where there are multiple smaller videos concatenated
 * use this.
 * */
@Suppress("DEPRECATION")
data class ExtractorLinkPlayList(
    override val source: String,
    override val name: String,
    val playlist: List<PlayListItem>,
    override var referer: String,
    override var quality: Int,
    override var headers: Map<String, String> = mapOf(),
    /** Used for getExtractorVerifierJob() */
    override var extractorData: String? = null,
    override var type: ExtractorLinkType,
    override var audioTracks: List<AudioFile> = emptyList(),
) : ExtractorLink(
    source = source,
    name = name,
    url = "",
    referer = referer,
    quality = quality,
    headers = headers,
    extractorData = extractorData,
    type = type,
    audioTracks = audioTracks
) {
    constructor(
        source: String,
        name: String,
        playlist: List<PlayListItem>,
        referer: String,
        quality: Int,
        isM3u8: Boolean = false,
        headers: Map<String, String> = mapOf(),
        extractorData: String? = null,
    ) : this(
        source = source,
        name = name,
        playlist = playlist,
        referer = referer,
        quality = quality,
        type = if (isM3u8) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO,
        headers = headers,
        extractorData = extractorData,
    )
}

/** Metadata about the file type used for downloads and exoplayer hint,
 * if you respond with the wrong one the file will fail to download or be played */
enum class ExtractorLinkType {
    /** Single stream of bytes no matter the actual file type */
    VIDEO,

    /** Split into several .ts files, has support for encrypted m3u8s */
    M3U8,

    /** Like m3u8 but uses xml, currently no download support */
    DASH,

    /** No support at the moment */
    TORRENT,

    /** No support at the moment */
    MAGNET;

    // See https://www.iana.org/assignments/media-types/media-types.xhtml
    fun getMimeType(): String {
        return when (this) {
            VIDEO -> "video/mp4"
            M3U8 -> "application/x-mpegURL"
            DASH -> "application/dash+xml"
            TORRENT -> "application/x-bittorrent"
            MAGNET -> "application/x-bittorrent"
        }
    }
}

private fun inferTypeFromUrl(url: String): ExtractorLinkType {
    val path = try {
        URI(url).path
    } catch (_: Throwable) {
        // don't log magnet links as errors
        null
    }
    return when {
        path?.endsWith(".m3u8") == true -> ExtractorLinkType.M3U8
        path?.endsWith(".mpd") == true -> ExtractorLinkType.DASH
        path?.endsWith(".torrent") == true -> ExtractorLinkType.TORRENT
        url.startsWith("magnet:") -> ExtractorLinkType.MAGNET
        else -> ExtractorLinkType.VIDEO
    }
}

val INFER_TYPE: ExtractorLinkType? = null

/**
 * UUID for the ClearKey DRM scheme.
 *
 *
 * ClearKey is supported on Android devices running Android 5.0 (API Level 21) and up.
 */
val CLEARKEY_UUID = UUID(-0x1d8e62a7567a4c37L, 0x781AB030AF78D30EL)

/**
 * UUID for the Widevine DRM scheme.
 *
 *
 * Widevine is supported on Android devices running Android 4.3 (API Level 18) and up.
 */
val WIDEVINE_UUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)

/**
 * UUID for the PlayReady DRM scheme.
 *
 *
 * PlayReady is supported on all AndroidTV devices. Note that most other Android devices do not
 * provide PlayReady support.
 */
val PLAYREADY_UUID = UUID(-0x65fb0f8667bfbd7aL, -0x546d19a41f77a06bL)

suspend fun newExtractorLink(
    source: String,
    name: String,
    url: String,
    type: ExtractorLinkType? = null,
    initializer: suspend ExtractorLink.() -> Unit = { }
): ExtractorLink {

    @Suppress("DEPRECATION_ERROR")
    val builder =
        ExtractorLink(
            source = source,
            name = name,
            url = url,
            type = type ?: INFER_TYPE
        )

    builder.initializer()
    return builder
}

suspend fun newDrmExtractorLink(
    source: String,
    name: String,
    url: String,
    type: ExtractorLinkType? = null,
    uuid: UUID,
    initializer: suspend DrmExtractorLink.() -> Unit = { }
): DrmExtractorLink {

    @Suppress("DEPRECATION_ERROR")
    val builder =
        DrmExtractorLink(
            source = source,
            name = name,
            url = url,
            uuid = uuid,
            type = type ?: INFER_TYPE
        )

    builder.initializer()
    return builder
}

/** Class holds extracted DRM media info to be passed to the player.
 * @property source Name of the media source, appears on player layout.
 * @property name Title of the media, appears on player layout.
 * @property url Url string of media file
 * @property referer Referer that will be used by network request.
 * @property quality Quality of the media file
 * @property headers Headers <String, String> map that will be used by network request.
 * @property extractorData Used for getExtractorVerifierJob()
 * @property type the type of the media, use [INFER_TYPE] if you want to auto infer the type from the url
 * @property kid  Base64 value of The KID element (Key Id) contains the identifier of the key associated with a license.
 * @property key Base64 value of Key to be used to decrypt the media file.
 * @property uuid Drm UUID [WIDEVINE_UUID], [PLAYREADY_UUID], [CLEARKEY_UUID] (by default) .. etc
 * @property kty Key type "oct" (octet sequence) by default
 * @property keyRequestParameters Parameters that will used to request the key.
 * @see newDrmExtractorLink
 * */
@Suppress("DEPRECATION")
open class DrmExtractorLink private constructor(
    override val source: String,
    override val name: String,
    override val url: String,
    override var referer: String,
    override var quality: Int,
    override var headers: Map<String, String> = mapOf(),
    /** Used for getExtractorVerifierJob() */
    override var extractorData: String? = null,
    override var type: ExtractorLinkType,
    open var kid: String? = null,
    open var key: String? = null,
    open var uuid: UUID,
    open var kty: String? = null,
    open var keyRequestParameters: HashMap<String, String>,
    open var licenseUrl: String? = null,
    override var audioTracks: List<AudioFile> = emptyList(),
) : ExtractorLink(
    source, name, url, referer, quality, headers, extractorData, type, audioTracks
) {
    @Deprecated("Use newDrmExtractorLink", level = DeprecationLevel.ERROR)
    constructor(
        source: String,
        name: String,
        url: String,
        referer: String? = null,
        quality: Int? = null,
        /** the type of the media, use INFER_TYPE if you want to auto infer the type from the url */
        type: ExtractorLinkType? = INFER_TYPE,
        headers: Map<String, String> = mapOf(),
        /** Used for getExtractorVerifierJob() */
        extractorData: String? = null,
        kid: String? = null,
        key: String? = null,
        uuid: UUID = CLEARKEY_UUID,
        kty: String? = "oct",
        keyRequestParameters: HashMap<String, String> = hashMapOf(),
        licenseUrl: String? = null,
    ) : this(
        source = source,
        name = name,
        url = url,
        referer = referer ?: "",
        quality = quality ?: Qualities.Unknown.value,
        headers = headers,
        extractorData = extractorData,
        type = type ?: inferTypeFromUrl(url),
        kid = kid,
        key = key,
        uuid = uuid,
        keyRequestParameters = keyRequestParameters,
        kty = kty,
        licenseUrl = licenseUrl,
    )

    @Deprecated("Use newDrmExtractorLink", level = DeprecationLevel.ERROR)
    constructor(
        source: String,
        name: String,
        url: String,
        referer: String,
        quality: Int,
        /** the type of the media, use INFER_TYPE if you want to auto infer the type from the url */
        type: ExtractorLinkType?,
        headers: Map<String, String> = mapOf(),
        /** Used for getExtractorVerifierJob() */
        extractorData: String? = null,
        kid: String? = null,
        key: String? = null,
        uuid: UUID = CLEARKEY_UUID,
        kty: String? = "oct",
        keyRequestParameters: HashMap<String, String> = hashMapOf(),
        licenseUrl: String? = null,
    ) : this(
        source = source,
        name = name,
        url = url,
        referer = referer,
        quality = quality,
        headers = headers,
        extractorData = extractorData,
        type = type ?: inferTypeFromUrl(url),
        kid = kid,
        key = key,
        uuid = uuid,
        keyRequestParameters = keyRequestParameters,
        kty = kty,
        licenseUrl = licenseUrl,
    )
}

/** Class holds extracted media info to be passed to the player.
 * @property source Name of the media source, appears on player layout.
 * @property name Title of the media, appears on player layout.
 * @property url Url string of media file
 * @property referer Referer that will be used by network request.
 * @property quality Quality of the media file
 * @property headers Headers <String, String> map that will be used by network request.
 * @property extractorData Used for getExtractorVerifierJob()
 * @property type Extracted link type (Video, M3u8, Dash, Torrent or Magnet)
 * @property audioTracks List of separate audio tracks that can be used with this video
 * @see newExtractorLink
 * */
open class ExtractorLink
@Deprecated("Use newExtractorLink", level = DeprecationLevel.WARNING)
constructor(
    open val source: String,
    open val name: String,
    override val url: String,
    override var referer: String,
    open var quality: Int,
    override var headers: Map<String, String> = mapOf(),
    /** Used for getExtractorVerifierJob() */
    open var extractorData: String? = null,
    open var type: ExtractorLinkType,
    /** List of separate audio tracks that can be merged with this video */
    open var audioTracks: List<AudioFile> = emptyList(),
) : IDownloadableMinimum {
    val isM3u8: Boolean get() = type == ExtractorLinkType.M3U8
    val isDash: Boolean get() = type == ExtractorLinkType.DASH

    // Cached video size
    private var videoSize: Long? = null

    /**
     * Get video size in bytes with one head request. Only available for ExtractorLinkType.Video
     * @param timeoutSeconds timeout of the head request.
     */
    suspend fun getVideoSize(timeoutSeconds: Long = 3L): Long? {
        // Content-Length is not applicable to other types of formats
        if (this.type != ExtractorLinkType.VIDEO) return null

        videoSize = videoSize ?: runCatching {
            val response =
                app.head(this.url, headers = headers, referer = referer, timeout = timeoutSeconds)
            response.headers["Content-Length"]?.toLong()
        }.getOrNull()

        return videoSize
    }

    @JsonIgnore
    fun getAllHeaders(): Map<String, String> {
        if (referer.isBlank()) {
            return headers
        } else if (headers.keys.none { it.equals("referer", ignoreCase = true) }) {
            return headers + mapOf("referer" to referer)
        }
        return headers
    }

    @Suppress("DEPRECATION")
    @Deprecated("Use newExtractorLink", level = DeprecationLevel.ERROR)
    constructor(
        source: String,
        name: String,
        url: String,
        referer: String? = null,
        quality: Int? = null,
        /** the type of the media, use INFER_TYPE if you want to auto infer the type from the url */
        type: ExtractorLinkType? = INFER_TYPE,
        headers: Map<String, String> = mapOf(),
        /** Used for getExtractorVerifierJob() */
        extractorData: String? = null,
    ) : this(
        source = source,
        name = name,
        url = url,
        referer = referer ?: "",
        quality = quality ?: Qualities.Unknown.value,
        headers = headers,
        extractorData = extractorData,
        type = type ?: inferTypeFromUrl(url)
    )

    @Suppress("DEPRECATION")
    @Deprecated("Use newExtractorLink", level = DeprecationLevel.ERROR)
    constructor(
        source: String,
        name: String,
        url: String,
        referer: String,
        quality: Int,
        /** the type of the media, use INFER_TYPE if you want to auto infer the type from the url */
        type: ExtractorLinkType?,
        headers: Map<String, String> = mapOf(),
        /** Used for getExtractorVerifierJob() */
        extractorData: String? = null,
    ) : this(
        source = source,
        name = name,
        url = url,
        referer = referer,
        quality = quality,
        headers = headers,
        extractorData = extractorData,
        type = type ?: inferTypeFromUrl(url)
    )

    /**
     * Old constructor without isDash, allows for backwards compatibility with extensions.
     * Should be removed after all extensions have updated their cloudstream.jar
     **/
    @Suppress("DEPRECATION_ERROR")
    @Deprecated("Use newExtractorLink", level = DeprecationLevel.ERROR)
    constructor(
        source: String,
        name: String,
        url: String,
        referer: String,
        quality: Int,
        isM3u8: Boolean = false,
        headers: Map<String, String> = mapOf(),
        /** Used for getExtractorVerifierJob() */
        extractorData: String? = null
    ) : this(source, name, url, referer, quality, isM3u8, headers, extractorData, false)

    @Suppress("DEPRECATION")
    @Deprecated("Use newExtractorLink", level = DeprecationLevel.ERROR)
    constructor(
        source: String,
        name: String,
        url: String,
        referer: String,
        quality: Int,
        isM3u8: Boolean = false,
        headers: Map<String, String> = mapOf(),
        /** Used for getExtractorVerifierJob() */
        extractorData: String? = null,
        isDash: Boolean,
    ) : this(
        source = source,
        name = name,
        url = url,
        referer = referer,
        quality = quality,
        headers = headers,
        extractorData = extractorData,
        type = if (isDash) ExtractorLinkType.DASH else if (isM3u8) ExtractorLinkType.M3U8 else ExtractorLinkType.VIDEO
    )

    override fun toString(): String {
        return "ExtractorLink(name=$name, url=$url, referer=$referer, type=$type)"
    }
}

/**
 * Removes https:// and www.
 * To match urls regardless of schema, perhaps Uri() can be used?
 */
val schemaStripRegex = Regex("""^(https:|)//(www\.|)""")

enum class Qualities(var value: Int, val defaultPriority: Int) {
    Unknown(400, 4),
    P144(144, 0), // 144p
    P240(240, 2), // 240p
    P360(360, 3), // 360p
    P480(480, 4), // 480p
    P720(720, 5), // 720p
    P1080(1080, 6), // 1080p
    P1440(1440, 7), // 1440p
    P2160(2160, 8); // 4k or 2160p

    companion object {
        fun getStringByInt(qual: Int?): String {
            return when (qual) {
                0 -> "Auto"
                Unknown.value -> ""
                P2160.value -> "4K"
                null -> ""
                else -> "${qual}p"
            }
        }

        fun getStringByIntFull(quality: Int): String {
            return when (quality) {
                0 -> "Auto"
                Unknown.value -> "Unknown"
                P2160.value -> "4K"
                else -> "${quality}p"
            }
        }
    }
}

fun getQualityFromName(qualityName: String?): Int {
    if (qualityName == null)
        return Qualities.Unknown.value

    val match = qualityName.lowercase().replace("p", "").trim()
    return when (match) {
        "4k" -> Qualities.P2160
        else -> null
    }?.value ?: match.toIntOrNull() ?: Qualities.Unknown.value
}

private val packedRegex = Regex("""eval\(function\(p,a,c,k,e,.*\)\)""")
fun getPacked(string: String): String? {
    return packedRegex.find(string)?.value
}

fun getAndUnpack(string: String): String {
    val packedText = getPacked(string)
    return JsUnpacker(packedText).unpack() ?: string
}

suspend fun unshortenLinkSafe(url: String): String {
    return try {
        if (ShortLink.isShortLink(url))
            ShortLink.unshorten(url)
        else url
    } catch (e: Exception) {
        logError(e)
        url
    }
}

suspend fun loadExtractor(
    url: String,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
): Boolean {
    return loadExtractor(
        url = url,
        referer = null,
        subtitleCallback = subtitleCallback,
        callback = callback
    )
}


/**
 * Tries to load the appropriate extractor based on link, returns true if any extractor is loaded.
 * */
@Throws(CancellationException::class)
suspend fun loadExtractor(
    url: String,
    referer: String? = null,
    subtitleCallback: (SubtitleFile) -> Unit,
    callback: (ExtractorLink) -> Unit
): Boolean {
    // Ensure this coroutine has not timed out
    coroutineScope { ensureActive() }

    val currentUrl = unshortenLinkSafe(url)
    val compareUrl = currentUrl.lowercase().replace(schemaStripRegex, "")

    // Iterate in reverse order so the new registered ExtractorApi takes priority
    for (index in extractorApis.lastIndex downTo 0) {
        val extractor = extractorApis[index]
        if (compareUrl.startsWith(extractor.mainUrl.replace(schemaStripRegex, ""))) {
            try {
                extractor.getUrl(currentUrl, referer, subtitleCallback, callback)
            } catch (e: Exception) {
                logError(e)
                // Rethrow if we have timed out
                if (e is CancellationException) {
                    throw e
                }
            }
            return true
        }
    }

    // this is to match mirror domains - like example.com, example.net
    for (index in extractorApis.lastIndex downTo 0) {
        val extractor = extractorApis[index]
        if (FuzzySearch.partialRatio(
                extractor.mainUrl,
                currentUrl
            ) > 80
        ) {
            try {
                extractor.getUrl(currentUrl, referer, subtitleCallback, callback)
            } catch (e: Exception) {
                logError(e)
                // Rethrow if we have timed out
                if (e is CancellationException) {
                    throw e
                }
            }
            return true
        }
    }

    return false
}

val extractorApis: MutableList<ExtractorApi> = arrayListOf(
    //AllProvider(),
    Mp4Upload(),
    StreamTape(),
    StreamTapeNet(),
    ShaveTape(),
    StreamTapeXyz(),
    Watchadsontape(),

    //mixdrop extractors
    MixDropBz(),
    MixDropCh(),
    MixDropTo(),
    MixDropTop(),
    MixDropAg(),
    MixDrop(),
    MixDropPs(),
    Mdy(),
    MxDropTo(),
    MixDropSi(),

    XStreamCdn(),

    StreamSB(),
    Sblona(),
    Vidgomunimesb(),
    StreamSilk(),
    StreamSB1(),
    StreamSB2(),
    StreamSB3(),
    StreamSB4(),
    StreamSB5(),
    StreamSB6(),
    StreamSB7(),
    StreamSB8(),
    StreamSB9(),
    StreamSB10(),
    StreamSB11(),
    SBfull(),
    // Streamhub(), cause Streamhub2() works
    Streamhub2(),
    Ssbstream(),
    Sbthe(),
    Vidgomunime(),
    Sbflix(),
    Streamsss(),
    Sbspeed(),
    Sbsonic(),
    Sbface(),
    Sbrapid(),
    Lvturbo(),

    Fastream(),
    Videa(),
    FEmbed(),
    FeHD(),
    Fplayer(),
    DBfilm(),
    Luxubu(),
    LayarKaca(),
    Rasacintaku(),
    FEnet(),
    Kotakajair(),
    Cdnplayer(),
    //  WatchSB(), 'cause StreamSB.kt works
    Uqload(),
    Uqload1(),
    Uqload2(),
    Uqloadcx(),
    Uqloadbz(),
    Evoload(),
    Evoload1(),
    UpstreamExtractor(),

    Odnoklassniki(),
    TauVideo(),
    SibNet(),
    ContentX(),
    Hotlinger(),
    FourCX(),
    PlayRu(),
    FourPlayRu(),
    Pichive(),
    FourPichive(),
    HDMomPlayer(),
    HDPlayerSystem(),
    VideoSeyred(),
    PeaceMakerst(),
    HDStreamAble(),
    RapidVid(),
    TRsTX(),
    VidMoxy(),
    Sobreatsesuyp(),
    PixelDrain(),
    PixelDrainDev(),
    MailRu(),

    OkRuSSL(),
    OkRuSSLMobile(),
    OkRuHTTP(),
    OkRuHTTPMobile(),
    Sendvid(),

    // dood extractors
    DoodCxExtractor(),
    DoodPmExtractor(),
    DoodToExtractor(),
    DoodSoExtractor(),
    DoodLaExtractor(),
    Dooood(),
    D0000d(),
    D000dCom(),
    DoodstreamCom(),
    DoodWsExtractor(),
    DoodShExtractor(),
    DoodWatchExtractor(),
    DoodWfExtractor(),
    DoodYtExtractor(),
    Doodspro(),
    Dsvplay(),

    // GenericM3U8(),
    Zplayer(),
    ZplayerV2(),
    Upstream(),

    Maxstream(),
    Tantifilm(),
    Userload(),
    Supervideo(),

    // StreamSB.kt works
    //  SBPlay(),
    //  SBPlay1(),
    //  SBPlay2(),

    PlayerVoxzer(),

    Blogger(),
    YourUpload(),

    Hxfile(),
    Yufiles(),
    Aico(),

    JWPlayer(),


    Keephealth(),
    Sbnet(),
    Sbasian(),
    Sblongvu(),
    Fembed9hd(),
    StreamM4u(),
    Krakenfiles(),
    Gofile(),
    Vicloud(),
    Uservideo(),
    Userscloud(),

    Movhide(),
    StreamhideCom(),
    StreamhideTo(),
    FileMoonIn(),
    Moviesm4u(),
    Filesim(),
    Multimoviesshg(),
    Ahvsh(),
    Guccihide(),
    FileMoon(),
    FileMoonSx(),
    FilemoonV2(),

    Vido(),
    Linkbox(),
    Acefile(),
    Minoplres(), // formerly SpeedoStream
    Embedgram(),
    Mvidoo(),
    Streamplay(),
    Vidmoly(),
    Vidmolyme(),
    Vidmolyto(),
    Vidmolybiz(),
    Voe(),
    Voe1(),
    Tubeless(),
    Moviehab(),
    MoviehabNet(),
    Jeniusplay(),
    StreamoUpload(),
    Streamup(),
    Streamix(),
    Vidara(),

    GamoVideo(),
    Gdriveplayerapi(),
    Gdriveplayerapp(),
    Gdriveplayerfun(),
    Gdriveplayerio(),
    Gdriveplayerme(),
    Gdriveplayerbiz(),
    Gdriveplayerorg(),
    Gdriveplayerus(),
    Gdriveplayerco(),
    GoodstreamExtractor(),
    Gdriveplayer(),
    DatabaseGdrive(),
    DatabaseGdrive2(),
    Mediafire(),

    YoutubeExtractor(),
    YoutubeShortLinkExtractor(),
    YoutubeMobileExtractor(),
    YoutubeNoCookieExtractor(),
    Streamlare(),
    PlayLtXyz(),

    Cda(),
    Dailymotion(),
    Ztreamhub(),
    Rabbitstream(),
    Dokicloud(),
    Megacloud(),
    VidhideExtractor(),
    VidHidePro(),
    VidHidePro1(),
    VidHidePro2(),
    VidHidePro3(),
    VidHidePro4(),
    VidHidePro5(),
    VidHidePro6(),
    VidHideHub(),
    Ryderjet(),
    VidNest(),
    Dhtpre(),

    // CineMM Redirects
    Dhcplay(),
    HglinkTo(),

    // CineMM mirrors
    HgplayCDN(),
    Habetar(),
    Yuguaab(),
    Guxhag(),
    Auvexiug(),
    Xenolyzb(),
    Haxloppd(),
    Cavanhabg(),
    Dumbalag(),
    Uasopt(),

    Smoothpre(),
    Peytonepre(),
    LuluStream(),
    Lulustream1(),
    Lulustream2(),
    Luluvdoo(),
    StreamWishExtractor(),
    StreamHLS(),
    BigwarpIO(),
    BigwarpArt(),
    BgwpCC(),
    WishembedPro(),
    CdnwishCom(),
    FlaswishCom(),
    SfastwishCom(),
    Playerwish(),
    StreamEmbed(),
    EmturbovidExtractor(),
    Vtbe(),
    SecvideoOnline(),
    FsstOnline(),
    CsstOnline(),
    DsstOnline(),
    Simpulumlamerop(),
    Urochsunloath(),
    NathanFromSubject(),
    Yipsu(),
    MetaGnathTuggers(),
    Geodailymotion(),
    Mwish(),
    Dwish(),
    Ewish(),
    Kswplayer(),
    Wishfast(),
    Streamwish2(),
    Strwish(),
    Strwish2(),
    Awish(),
    Obeywish(),
    Jodwish(),
    Swhoi(),
    Multimovies(),
    UqloadsXyz(),
    Doodporn(),
    Asnwish(),
    Nekowish(),
    Nekostream(),
    Swdyu(),
    Wishonly(),
    Ds2play(),
    Ds2video(),
    Vidsonic(),
    InternetArchive(),
    VidStack(),
    GDMirrorbot(),
    Techinmind(),
    Server1uns(),
    VinovoSi(),
    VinovoTo(),
    Vidoza(),
    Videzz(),
    CloudMailRu(),
    HubCloud(),
    VkExtractor(),
    Bysezejataos(),
    ByseSX(),
    ByseVepoin(),
    ByseBuho(),
    MyVidPlay(),
    Vide0Net(),
    Up4Stream(),
    Up4FunTop(),
    GUpload(),
    HlsWish(),
    ByseQekaho(),
    MixPlayHD(),
    MixTiger(),
    CDNJWPlayer(),
    DiskYandexComTr(),
    DzenRu(),
    JetPlayer(),
)


fun getExtractorApiFromName(name: String): ExtractorApi {
    for (api in extractorApis) {
        if (api.name == name) return api
    }
    return extractorApis[0]
}

fun requireReferer(name: String): Boolean {
    return getExtractorApiFromName(name).requiresReferer
}

fun httpsify(url: String): String {
    return if (url.startsWith("//")) "https:$url" else url
}

suspend fun getPostForm(requestUrl: String, html: String): String? {
    val document = Jsoup.parse(html)
    val inputs = document.select("Form > input")
    if (inputs.size < 4) return null
    var op: String? = null
    var id: String? = null
    var mode: String? = null
    var hash: String? = null

    for (input in inputs) {
        val value = input.attr("value")
        when (input.attr("name")) {
            "op" -> op = value
            "id" -> id = value
            "mode" -> mode = value
            "hash" -> hash = value
            else -> Unit
        }
    }
    if (op == null || id == null || mode == null || hash == null) {
        return null
    }
    delay(5000) // ye this is needed, wont work with 0 delay

    return app.post(
        requestUrl,
        headers = mapOf(
            "content-type" to "application/x-www-form-urlencoded",
            "referer" to requestUrl,
            "user-agent" to USER_AGENT,
            "accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9"
        ),
        data = mapOf("op" to op, "id" to id, "mode" to mode, "hash" to hash)
    ).text
}

fun ExtractorApi.fixUrl(url: String): String {
    if (url.startsWith("http") ||
        // Do not fix JSON objects when passed as urls.
        url.startsWith("{\"")
    ) {
        return url
    }
    if (url.isEmpty()) {
        return ""
    }

    val startsWithNoHttp = url.startsWith("//")
    if (startsWithNoHttp) {
        return "https:$url"
    } else {
        if (url.startsWith('/')) {
            return mainUrl + url
        }
        return "$mainUrl/$url"
    }
}

abstract class ExtractorApi {
    abstract val name: String
    abstract val mainUrl: String
    abstract val requiresReferer: Boolean

    /** Determines which plugin a given provider is from. This is the full path to the plugin. */
    var sourcePlugin: String? = null

    //suspend fun getSafeUrl(url: String, referer: String? = null): List<ExtractorLink>? {
    //    return safeAsync { getUrl(url, referer) }
    //}

    // this is the new extractorapi, override to add subtitles and stuff
    @Throws
    open suspend fun getUrl(
        url: String,
        referer: String? = null,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        getUrl(url, referer)?.forEach(callback)
    }

    suspend fun getSafeUrl(
        url: String,
        referer: String? = null,
        subtitleCallback: (SubtitleFile) -> Unit,
        callback: (ExtractorLink) -> Unit
    ) {
        try {
            getUrl(url, referer, subtitleCallback, callback)
        } catch (e: Exception) {
            logError(e)
        }
    }

    /**
     * Will throw errors, use getSafeUrl if you don't want to handle the exception yourself
     */
    @Throws
    open suspend fun getUrl(url: String, referer: String? = null): List<ExtractorLink>? {
        return emptyList()
    }

    open fun getExtractorUrl(id: String): String {
        return id
    }
}
