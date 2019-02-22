# ONLYOFFICE Alfresco module package

This plugin enables users to edit office documents from Alfresco Share using ONLYOFFICE Document Server.

Tested with [Alfresco 6.\*](https://github.com/keensoft/alfresco-docker-template/tree/master/templates/201806-GA)


## Features
* Currently the following document formats can be opened and edited with this plugin: DOCX, XLSX, PPTX.
* The plugin will create a new **Edit in ONLYOFFICE** menu option within the document library for Office documents.
![editinonlyoffice](edit_in_onlyoffice.png)
* This allows multiple users to collaborate in real time and to save back those changes to Alfresco.


## Installing ONLYOFFICE Document Server

You will need an instance of ONLYOFFICE Document Server that is resolvable and connectable both from Alfresco and any end clients (version 3.0 and later are supported for use with the plugin). If that is not the case, use the official ONLYOFFICE Document Server documentation page: [Document Server for Linux](http://helpcenter.onlyoffice.com/server/linux/document/linux-installation.aspx). ONLYOFFICE Document Server must also be able to POST to Alfresco directly.

The easiest way to start an instance of ONLYOFFICE Document Server is to use [Docker](https://github.com/onlyoffice/Docker-DocumentServer).


## Installing ONLYOFFICE Alfresco module package

To start using ONLYOFFICE Document Server with Alfresco, the following steps must be performed for Ubuntu 14.04:

> Steps **1** &mdash; **4** are only necessary if you for some reason plan to compile the ONLYOFFICE Alfresco module package yourself (e.g. edit the source code and compile it afterwards). If you do not want to do that and plan to use the already compiled module files, please skip to step **5** directly. The latest compiled package files are available [here](https://github.com/onlyoffice/onlyoffice-alfresco/releases).

1. The latest stable Oracle Java version is necessary for the successful build. If you do not have it installed, use the following commands to install Oracle Java 8:
    ```bash
    sudo add-apt-repository ppa:webupd8team/java
    sudo apt-get update
    sudo apt-get install oracle-java8-installer
    ```

2. Install latest Maven:
Installation process is described [here](https://maven.apache.org/install.html)

3. Download the ONLYOFFICE Alfresco module package source code:
    ```bash
    git clone https://github.com/onlyoffice/onlyoffice-alfresco.git
    ```

4. Compile packages in the `repo` and `share` directories:
    ```bash
    cd onlyoffice-alfresco/
    mvn clean install
    ```

5. Upload the compiled **\*.jar** packages to directories accordingly for your Alfresco installation:
    * from `onlyoffice-alfresco/repo/target/` to the `/webapps/alfresco/WEB-INF/lib/` for Alfresco repository,
    * from `onlyoffice-alfresco/share/target/` to `/webapps/share/WEB-INF/lib/` for Share.
    > You can download the already compiled package files [here](https://github.com/onlyoffice/onlyoffice-alfresco/releases) and place them to the respective directories.

6. Add the **onlyoffice.url** property to `alfresco-global.properties`:

 Probably located here `/usr/local/tomcat/shared/classes/alfresco-global.properties`
    ```
    onlyoffice.url=http://documentserver/
    ```


 You may also want to change these (make sure that Document Server will be able to POST to Alfresco)
 ```
alfresco.host=<hostname>
alfresco.port=443
alfresco.protocol=https

share.host=<hostname>
share.port=443
share.protocol=https
```

7. Restart Alfresco:

```bash
sudo ./alfresco.sh stop
sudo ./alfresco.sh start
```

The module can be checked in administrator tools at `share/page/console/admin-console/module-package` in Alfresco.

## Building from docker-compose

Other way to build ONLYOFFICE Alfresco module package is using docker-compose file.

Use this command from project directory:

```bash
docker-compose up
```

## How it works

The ONLYOFFICE integration follows the API documented [here](https://api.onlyoffice.com/editors/basic):

* User navigates to a document within Alfresco Share and selects the `Edit in ONLYOFFICE` action.
* Alfresco Share makes a request to the repo end (URL of the form: `/parashift/onlyoffice/prepare?nodeRef={nodeRef}`).
* Alfresco Repo end prepares a JSON object for the Share with the following properties:
  * **docUrl**: the URL that ONLYOFFICE Document Server uses to download the document (includes the `alf_ticket` of the current user),
  * **callbackUrl**: the URL that ONLYOFFICE Document Server informs about status of the document editing;
  * **onlyofficeUrl**: the URL that the client needs to reply to ONLYOFFICE Document Server (provided by the onlyoffice.url property);
  * **key**: the UUID+Modified Timestamp to instruct ONLYOFFICE Document Server whether to download the document again or not;
  * **docTitle**: the document Title (name).
* Alfresco Share takes this object and constructs a page from a freemarker template, filling in all of those values so that the client browser can load up the editor.
* The client browser makes a request for the javascript library from ONLYOFFICE Document Server and sends ONLYOFFICE Document Server the docEditor configuration with the above properties.
* Then ONLYOFFICE Document Server downloads the document from Alfresco and the user begins editing.
* ONLYOFFICE Document Server sends a POST request to the `callback` URL to inform Alfresco that a user is editing the document.
* Alfresco locks the document, but still allows other users with write access the ability to collaborate in real time with ONLYOFFICE Document Server by leaving the Action present.
* When all users and client browsers are done with editing, they close the editing window.
* After 10 seconds of inactivity, ONLYOFFICE Document Server sends a POST to the `callback` URL letting Alfresco know that the clients have finished editing the document and closed it.
* Alfresco downloads the new version of the document, replacing the old one.

