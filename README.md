# ONLYOFFICE Alfresco module package

This plugin enables users to edit office documents from Alfresco Share using ONLYOFFICE Docs packaged as Document Server - [Community or Enterprise Edition](#onlyoffice-docs-editions).

Tested with [Alfresco 6.\*](https://github.com/keensoft/alfresco-docker-template/tree/master/templates/201806-GA)

## Features

* Create and edit text documents, spreadsheets, and presentations.
* Share documents with other users.
* Co-edit documents in real-time: use two co-editing modes (Fast and Strict), Track Changes, comments, and built-in chat.

Supported formats:

* For viewing and editing: DOCX, XLSX, PPTX.
* For converting to Office Open XML: ODT, ODP, ODS, DOC, XLS, PPT.

To convert a specific file, select `Convert using ONLYOFFICE` action. Resulting file will be placed in the same folder. You can also configure rules for a folder, that will automatically convert files on upload or on change. Details [here](https://docs.alfresco.com/5.1/tasks/library-folder-rules-define-create.html).

## Installing ONLYOFFICE Docs

You will need an instance of ONLYOFFICE Docs (Document Server) that is resolvable and connectable both from Alfresco and any end clients. ONLYOFFICE Document Server must also be able to POST to Alfresco directly.

You can install free Community version of ONLYOFFICE Docs or scalable Enterprise Edition with pro features.

To install free Community version, use [Docker](https://github.com/onlyoffice/Docker-DocumentServer) (recommended) or follow [these instructions](https://helpcenter.onlyoffice.com/installation/docs-community-install-ubuntu.aspx) for Debian, Ubuntu, or derivatives.  

To install Enterprise Edition, follow instructions [here](https://helpcenter.onlyoffice.com/installation/docs-enterprise-index.aspx).

Community Edition vs Enterprise Edition comparison can be found [here](#onlyoffice-docs-editions).

## Installing ONLYOFFICE Alfresco module package

To start using ONLYOFFICE Document Server with Alfresco, the following steps must be performed for Ubuntu 14.04:

The latest compiled package files are available [here](https://github.com/onlyoffice/onlyoffice-alfresco/releases).

1. Upload the compiled **\*.jar** packages to directories accordingly for your Alfresco installation:
    * from `onlyoffice-alfresco/repo/target/` to the `/webapps/alfresco/WEB-INF/lib/` for Alfresco repository,
    * from `onlyoffice-alfresco/share/target/` to `/webapps/share/WEB-INF/lib/` for Share.

2. Make sure that Document Server will be able to POST to Alfresco.

    You may need to change these lines in `alfresco-global.properties` or you can set it using [configuration page](#configuration)

    ```
    alfresco.host=<hostname>
    alfresco.port=443
    alfresco.protocol=https

    share.host=<hostname>
    share.port=443
    share.protocol=https
    ```

    > Probably located here `/usr/local/tomcat/shared/classes/alfresco-global.properties`

3. Restart Alfresco:
    ```bash
    sudo ./alfresco.sh stop
    sudo ./alfresco.sh start
    ```
The module can be checked in administrator tools at `share/page/console/admin-console/module-package` in Alfresco.

## Configuration

Module configuration can be found inside `Alfresco Administration Console` or by simply navigating to `http://<alfrescohost>/alfresco/s/onlyoffice/onlyoffice-config`

> You can also add `onlyoffice.url` in `alfresco-global.properties`. Configuration made via settings page will override `alfresco-global.properties`.

JWT can be configured via [configuration page](#configuration) or by adding `onlyoffice.jwtsecret` in `alfresco-global.properties`.

The JWT configuration on the Document Server side can be found [here](https://api.onlyoffice.com/editors/signature/).

## Compiling ONLYOFFICE Alfresco module package

If you plan to compile the ONLYOFFICE Alfresco module package yourself (e.g. edit the source code and compile it afterwards), follow these steps. 

1. The latest stable Oracle Java version is necessary for the successful build. If you do not have it installed, use the following commands to install Oracle Java 8:
    ```bash
    sudo apt-get update
    sudo apt-get install openjdk-8-jdk
    ```

2. Install latest Maven:
Installation process is described [here](https://maven.apache.org/install.html)

3. Download the ONLYOFFICE Alfresco module package source code:
    ```bash
    git clone https://github.com/onlyoffice/onlyoffice-alfresco.git
    ```

4. Get a submodule:
    ```bash
    git submodule update --init --recursive
    ```

5. Compile packages in the `repo` and `share` directories:
    ```bash
    cd onlyoffice-alfresco/
    mvn clean install
    ```

Another way to build ONLYOFFICE Alfresco module package is using docker-compose file.

Use this command from project directory:

```bash
docker-compose up
```

## How it works

The ONLYOFFICE integration follows the API documented [here](https://api.onlyoffice.com/editors/basic):

* User navigates to a document within Alfresco Share and selects the `Edit in ONLYOFFICE` action.
* Alfresco Share makes a request to the repo end (URL of the form: `/parashift/onlyoffice/prepare?nodeRef={nodeRef}`).
* Alfresco Repo end prepares a JSON object for the Share with the following properties:
  * **url**: the URL that ONLYOFFICE Document Server uses to download the document (includes the `alf_ticket` of the current user),
  * **callbackUrl**: the URL that ONLYOFFICE Document Server informs about status of the document editing;
  * **onlyofficeUrl**: the URL that the client needs to reply to ONLYOFFICE Document Server (provided by the onlyoffice.url property);
  * **key**: the UUID+Modified Timestamp to instruct ONLYOFFICE Document Server whether to download the document again or not;
  * **title**: the document Title (name).
* Alfresco Share takes this object and constructs a page from a freemarker template, filling in all of those values so that the client browser can load up the editor.
* The client browser makes a request for the javascript library from ONLYOFFICE Document Server and sends ONLYOFFICE Document Server the docEditor configuration with the above properties.
* Then ONLYOFFICE Document Server downloads the document from Alfresco and the user begins editing.
* ONLYOFFICE Document Server sends a POST request to the `callback` URL to inform Alfresco that a user is editing the document.
* Alfresco locks the document, but still allows other users with write access the ability to collaborate in real time with ONLYOFFICE Document Server by leaving the Action present.
* When all users and client browsers are done with editing, they close the editing window.
* After 10 seconds of inactivity, ONLYOFFICE Document Server sends a POST to the `callback` URL letting Alfresco know that the clients have finished editing the document and closed it.
* Alfresco downloads the new version of the document, replacing the old one.

## ONLYOFFICE Docs editions

ONLYOFFICE offers different versions of its online document editors that can be deployed on your own servers.

* Community Edition (onlyoffice-documentserver package)
* Enterprise Edition (onlyoffice-documentserver-ee package)

The table below will help you make the right choice.

| Pricing and licensing | Community Edition | Enterprise Edition |
| ------------- | ------------- | ------------- |
| | [Get it now](https://www.onlyoffice.com/download-docs.aspx?utm_source=github&utm_medium=cpc&utm_campaign=GitHubAlfresco#docs-community)  | [Start Free Trial](https://www.onlyoffice.com/download-docs.aspx?utm_source=github&utm_medium=cpc&utm_campaign=GitHubAlfresco#docs-enterprise)  |
| Cost  | FREE  | [Go to the pricing page](https://www.onlyoffice.com/docs-enterprise-prices.aspx?utm_source=github&utm_medium=cpc&utm_campaign=GitHubAlfresco)  |
| Simultaneous connections | up to 20 maximum  | As in chosen pricing plan |
| Number of users | up to 20 recommended | As in chosen pricing plan |
| License | GNU AGPL v.3 | Proprietary |
| **Support** | **Community Edition** | **Enterprise Edition** | 
| Documentation | [Help Center](https://helpcenter.onlyoffice.com/installation/docs-community-index.aspx) | [Help Center](https://helpcenter.onlyoffice.com/installation/docs-enterprise-index.aspx) |
| Standard support | [GitHub](https://github.com/ONLYOFFICE/DocumentServer/issues) or paid | One year support included |
| Premium support | [Buy Now](https://www.onlyoffice.com/support.aspx?utm_source=github&utm_medium=cpc&utm_campaign=GitHubAlfresco) | [Buy Now](https://www.onlyoffice.com/support.aspx?utm_source=github&utm_medium=cpc&utm_campaign=GitHubAlfresco) |
| **Services** | **Community Edition** | **Enterprise Edition** | 
| Conversion Service                | + | + | 
| Document Builder Service          | + | + | 
| **Interface** | **Community Edition** | **Enterprise Edition** |
| Tabbed interface                       | + | + |
| Dark theme                             | + | + |
| 150% scaling                           | + | + |
| White Label                            | - | - |
| Integrated test example (node.js)     | - | + |
| Mobile web editors                    | - | + |
| **Plugins & Macros** | **Community Edition** | **Enterprise Edition** |
| Plugins                           | + | + |
| Macros                            | + | + |
| **Collaborative capabilities** | **Community Edition** | **Enterprise Edition** |
| Two co-editing modes              | + | + |
| Comments                          | + | + |
| Built-in chat                     | + | + |
| Review and tracking changes       | + | + |
| Display modes of tracking changes | + | + |
| Version history                   | + | + |
| **Document Editor features** | **Community Edition** | **Enterprise Edition** |
| Font and paragraph formatting   | + | + |
| Object insertion                | + | + |
| Adding Content control          | - | + | 
| Editing Content control         | + | + | 
| Layout tools                    | + | + |
| Table of contents               | + | + |
| Navigation panel                | + | + |
| Comparing Documents             | - | +* |
| **Spreadsheet Editor features** | **Community Edition** | **Enterprise Edition** |
| Font and paragraph formatting   | + | + |
| Object insertion                | + | + |
| Functions, formulas, equations  | + | + |
| Table templates                 | + | + |
| Pivot tables                    | + | + |
| Data validation                 | + | + |
| Conditional formatting  for viewing | +** | +** |
| Sheet Views                     | - | + |
| **Presentation Editor features** | **Community Edition** | **Enterprise Edition** |
| Font and paragraph formatting   | + | + |
| Object insertion                | + | + |
| Transitions                     | + | + |
| Presenter mode                  | + | + |
| Notes                           | + | + |
| | [Get it now](https://www.onlyoffice.com/download-docs.aspx?utm_source=github&utm_medium=cpc&utm_campaign=GitHubAlfresco#docs-community)  | [Start Free Trial](https://www.onlyoffice.com/download-docs.aspx?utm_source=github&utm_medium=cpc&utm_campaign=GitHubAlfresco#docs-enterprise)  |

\* It's possible to add documents for comparison from your local drive and from URL. Adding files for comparison from storage is not available yet.

\** Support for all conditions and gradient. Adding/Editing capabilities are coming soon

