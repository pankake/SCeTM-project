package com.backend.app.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class UrlController {
	private static final String BASE_URL = "http://example.com/share/";
	@GetMapping("/generateUrl")
	public String generateUrl(@RequestParam List<String> uid) {
		String uidsString = String.join(",", uid);
		String uniqueId = UUID.randomUUID().toString();
		return BASE_URL + uniqueId + "?uids=" + uidsString;
	}
}
