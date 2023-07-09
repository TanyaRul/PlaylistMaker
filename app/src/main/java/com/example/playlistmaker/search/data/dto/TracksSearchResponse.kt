package com.example.playlistmaker.search.data.dto

import com.example.playlistmaker.search.data.dto.Response
import com.example.playlistmaker.search.domain.model.Track

class TracksSearchResponse(val results: List<Track>) : Response()
