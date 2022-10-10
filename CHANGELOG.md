# Change Log

## 6.0.1
## Fixed
- issue with preview documents [#162](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/162)

## 6.0.0
## Added
- creating new file from document editor
- creating new file from document editor from document template
- add action "download as" from document manager
- compare file from storage
- mail merge from storage
- insert image from storage
- add history of file changes, highlighting changes between versions
- customization document editor view
- mark as favorite from document editor
- add goBack url for document editor
- change favicon in editor by document type
- detecting mobile browser
- Galician empty file templates

## Fixed
- loading preview on document-details page
- bug when user with role CONTRIBUTOR can't join to editing
- document server v6.0 and earlier is no longer supported

## 5.1.1
## Fixed
- fix support alfresco v6.2

## 5.1.0
## Added
- support docxf and oform formats
- create blank docxf from creation menu
- create docxf by tempalte from creation menu
- create docxf from docx from creation menu
- create oform from docxf from document manager
- "save as" in editor

## Fixed
- bug when System user was document modifier

## 5.0.1
## Fixed
- error on the "Document Details" page when the "Convert with ONLYOFFICE" button is displayed [#121](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/121)

## 5.0.0
## Added
- connection to the demo server
- support alfresco multi tenancy [#83](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/83)
- conversion for macro-enabled document and document template
- preview for shared files [#62](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/62)
- file conversion setting [#25](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/25)

## Changed
- deleting thumbnail generation code
- improving JWT validation
- fixed search on the preview page [#63](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/63)

## 4.3.0
## Added
- Opening not OOXML file formats for editing
- Empty file templates added in multiple new languages

## Fixed
- Fixed an issue where the file could not be saved by a user with permissions Collaborator

## 4.2.1
## Fixed
- Download button in document preview now works properly
- Changed document preview styles

## Added
- Brazilian Portuguese translation

## 4.2.0
## Fixed
- Document will not be marked as modified after opening and closing editors without changes
- Contributors will now be able to create new documents [#54](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/54)
- People without read access can't view files even if they have a link [#52](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/52)
- Fixed an issue when opening Document Details page would block the document when web preview option is disabled  [#56](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/56)
- Fixed an issue when file couldn't be opened for editing right afrer conversion

## Added
- On Document Library page banner will show on documents that are currently opened in editors
- JSON view for settings page
- RTF files can now be converted

## Changed
- Relative Document Server address can now be used in configuration. This may be useful when Document Server is located behind same reverse-proxy as Alfresco [#39](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/39)
- Changed the way locking mechanism works [#51](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/51)

## 4.1.1
## Fixed
- Disallow editing when user has no write permissions [#35](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/35)
- disallow editing non OOXML files by removing readonly param

## 4.1.0
## Added
- [Force saving](https://api.onlyoffice.com/editors/save#forcesave) for documents. Can be toggled on/off in settings [#23](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/23)
- Preview of the documents on `Document Details` page. Can be toggled on/off in settings [#24](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/24)
- Read button is now available for all supported documents that cannot be edited [#26](https://github.com/ONLYOFFICE/onlyoffice-alfresco/pull/26)
- `AMP` generation along with `JAR` files [#32](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/32)

## Changed
- `docId` is now randomly generated for each editing session

## Fixed
- File versioning tag was not added on document creation
- Convertation cannot be invoked if user has no write access to file
- Fixed an issue when document was saved under a different user than the one that edits a file

## 4.0.2
## Added
 - Ukrainian translations for `Create ...` menu [#22](https://github.com/ONLYOFFICE/onlyoffice-alfresco/pull/22)

## Fixed
 - Creating a new document was opening an editor in a current tab instead of a new one [#28](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/28)
 - `Ignore SSL certificate` setting now is taken into consideration when converting a document [#27](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/27)

## 4.0.1
## Fixed
 - fixed an issue when trying to create a new file [#18](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/18)
 - fixed an issue with ansi encoding for es, fr, de languages [#18](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/18)

## 4.0.0
## Added
 - `Create new..` context menu option in Share
 - Ukrainian translation [#14](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/14)

## Changed
 - Document Editing Service address is now splitted in two settings: inner address (address that alfresco will use to access service) and public address (address that user will use access editors)

## Fixed
 - an issue with `'` sign in document title [#15](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/15)
 - an issue when document wouldn't save due to automatic logoff [#12](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/12)
 - an issue when `Edit on ONLYOFFICE` button was available even if the document was blocked by other editors [#13](https://github.com/ONLYOFFICE/onlyoffice-alfresco/issues/13)

## 3.0.0
## Added
- configuration page in administration console
- saving settings will run a set of test to identify potential problems
- jwt support
- ability to convert `.odt .doc .xls .ods .ppt .odp` to Office Open XML

## Changed
- `Edit in ONLYOFFICE` button now displays only for `.docx .xlsx .pptx .csv .txt` formats
- `.csv .txt` formats will be converted to original format after editing
- document editors will now use user locale
