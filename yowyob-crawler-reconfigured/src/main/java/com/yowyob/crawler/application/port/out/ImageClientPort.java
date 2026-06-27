package com.yowyob.crawler.application.port.out;

public interface ImageClientPort {
    String findImageUrl(String name, double lat, double lng);
}
