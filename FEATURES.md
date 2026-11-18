# Aether Music — Feature Registry

Statuses: **Implemented** | **Partial** | **Planned** | **Unsupported**

## Implemented (working in this alpha)

| ID | Name | Category |
|----|------|----------|
| F001 | MediaStore library scan | Library |
| F002 | Room track database | Library |
| F003 | Observe all tracks | Library |
| F004 | Favorites toggle | Library |
| F005 | Search tracks | Search |
| F006 | Recently played list | Home |
| F007 | Most played list | Home |
| F008 | Recently added list | Home |
| F009 | Media3 ExoPlayer playback | Player |
| F010 | MediaSession service | Player |
| F011 | Notification / system controls | Player |
| F012 | Play / Pause / Next / Prev | Player |
| F013 | Shuffle | Player |
| F014 | Repeat off/all/one | Player |
| F015 | Seek bar | Player |
| F016 | Queue from list | Player |
| F017 | Mini player | Player |
| F018 | Full player screen | Player |
| F019 | Audio focus + noisy handling | Player |
| F020 | Background playback FGS | Player |
| F021 | Playlist create | Playlists |
| F022 | Playlist list UI | Playlists |
| F023 | Glass mode presets (7) | Appearance |
| F024 | Glass intensity slider | Appearance |
| F025 | Liquid glass panels (blur API 31+) | Appearance |
| F026 | Quality profiles | Performance |
| F027 | Equalizer UI + presets | Audio |
| F028 | EqualizerEngine (HAL) | Audio |
| F029 | Bass boost / virtualizer API | Audio |
| F030 | Stats: counts + listen ms | Stats |
| F031 | Home greeting | Home |
| F032 | Scan status feedback | Library |
| F033 | Custom cover field in DB | Library |
| F034 | User tag fields in DB | Library |
| F035 | Settings DataStore persistence | Settings |
| F036 | Home screen widget shell | Widgets |
| F037 | Playback speed API on controller | Player |
| F038 | Album art via MediaStore albumart | Library |
| F039 | Navigation tabs | App |
| F040 | Local-only privacy model | Privacy |

## Partial

| ID | Name | Notes |
|----|------|-------|
| P001 | Widget controls | Opens app; live play/pause needs Glance/Media binding |
| P002 | System EQ live bind | Engine ready; session id wiring from ExoPlayer to expand |
| P003 | Playlist tracks add UI | Repo supports add; full editor UI planned |
| P004 | Crossfade | Setting stored; ExoPlayer concat smoothing planned |
| P005 | Lyrics | Local LRC storage planned |
| P006 | Home layout editor | Section order stored as CSV foundation |

## Planned (not claimed as working)

Home visual editor, 9 player skins, theme export/import, smart playlists, gapless tune, ReplayGain, LRC editor, voice search, app lock, 15-band EQ guarantee, full widget editor, gesture mapper, landscape tablet layouts, folder browser SAF multi-pick UI, duplicate detector UI, storage analyzer UI, etc.

**Count honesty:** ~40 implemented capabilities in alpha. The 300+ roadmap is tracked as Planned — do not treat Planned as shipped.
