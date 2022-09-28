package com.parashift.onlyoffice.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Map;

/*
    Copyright (c) Ascensio System SIA 2022. All rights reserved.
    http://www.onlyoffice.com
*/
@Service
public class JwtManager {

    @Autowired
    ConfigManager configManager;

    public Boolean jwtEnabled() {
        return configManager.demoActive() || configManager.get("jwtsecret") != null && !((String)configManager.get("jwtsecret")).isEmpty();
    }

    public String createToken(JSONObject payload) throws Exception {
        Algorithm algorithm = Algorithm.HMAC256(getJwtSecret());

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, ?> payloadMap = objectMapper.readValue(payload.toString(), Map.class);

        String token = JWT.create()
                .withPayload(payloadMap)
                .sign(algorithm);

        return token;
    }

    public String verify(String token) {
        Algorithm algorithm = Algorithm.HMAC256(getJwtSecret());
        Base64.Decoder decoder = Base64.getUrlDecoder();

        DecodedJWT jwt = JWT.require(algorithm)
                .build()
                .verify(token);

        return new String(decoder.decode(jwt.getPayload()));
    }

    public String getJwtHeader(){
        String header = configManager.demoActive() ? configManager.getDemo("header") : (String) configManager.getOrDefault("jwtheader", "");
        return header == null || header.isEmpty() ? "Authorization" : header;
    }

    private String getJwtSecret() {
        return configManager.demoActive() ? configManager.getDemo("secret") : (String) configManager.get("jwtsecret");
    }
}

