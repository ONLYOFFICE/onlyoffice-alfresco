/*
    Copyright (c) Ascensio System SIA 2026. All rights reserved.
    http://www.onlyoffice.com
*/

package com.parashift.onlyoffice.sdk.client;

import java.io.OutputStream;

public interface DocumentServerClient extends com.onlyoffice.client.DocumentServerClient {
    int getFileWithoutCloseOutputStream(String fileUrl, OutputStream outputStream);
}
